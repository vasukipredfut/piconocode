package com.aria.m11;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;

/**
 * Integration Hub operations — health check, script template generation,
 * field mapping CRUD.
 *
 * <h3>Endpoints</h3>
 * <pre>
 *   GET  /api/m11/instances/{code}/health
 *       Probes the configured endpoint(s); returns ok / status / latencyMs.
 *
 *   GET  /api/m11/instances/{code}/script?lang=python|java|nodejs
 *       Generates a runnable client snippet for that integration's
 *       connector type, parametrised with the instance config.
 *       For OUR-AGENT mode: this is the code the customer pastes into
 *         their cron / Airflow / our agent.
 *       For EXTERNAL mode: the snippet plus the manual setup steps.
 *
 *   GET  /api/m11/instances/{code}/mapping
 *   POST /api/m11/instances/{code}/mapping
 *       List / write the integration_field_mapping rows for that instance.
 *       Maps our entity.field -> external system's field (SAP / Tally / NIC).
 * </pre>
 */
@RestController
@RequestMapping("/api/m11/instances")
public class IntegrationOpsController {

    private final JdbcTemplate jdbc;

    public IntegrationOpsController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ====================================================================
    // 1. Health probe
    // ====================================================================

    @GetMapping("/{code}/health")
    public Map<String, Object> health(@PathVariable String code) {
        Map<String, Object> inst;
        try {
            inst = jdbc.queryForMap(
                "SELECT code, connector_code, name, status, config_json " +
                "FROM integration_instance WHERE code=?", code);
        } catch (Exception e) {
            return Map.of("ok", false, "error", "Integration not found: " + code);
        }
        long t0 = System.currentTimeMillis();
        boolean ok = false;
        int httpStatus = 0;
        String reason = "";
        try {
            // Extract endpoint URL from config_json if present
            String cfg = inst.get("config_json") == null ? "{}" : inst.get("config_json").toString();
            String url = extractFirst(cfg, "endpoint", "url", "baseUrl", "host");
            if (url == null) {
                reason = "no endpoint in config";
            } else {
                if (!url.startsWith("http")) url = "http://" + url;
                HttpURLConnection con = (HttpURLConnection) URI.create(url).toURL().openConnection();
                con.setRequestMethod("HEAD");
                con.setConnectTimeout(2500);
                con.setReadTimeout(2500);
                httpStatus = con.getResponseCode();
                ok = httpStatus < 500;
                reason = "HTTP " + httpStatus;
            }
        } catch (Exception probe) {
            reason = probe.getClass().getSimpleName() + ": " + probe.getMessage();
        }
        long ms = System.currentTimeMillis() - t0;
        // Record health timestamp
        try {
            jdbc.update(
                "UPDATE integration_instance SET last_health_at=? WHERE code=?",
                Instant.now(), code);
        } catch (Exception ignored) {}
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", ok);
        r.put("code", code);
        r.put("connector", inst.get("connector_code"));
        r.put("latencyMs", ms);
        r.put("httpStatus", httpStatus);
        r.put("reason", reason);
        r.put("checkedAt", Instant.now().toString());
        return r;
    }

    // ====================================================================
    // 2. Script template generator
    // ====================================================================

    @GetMapping("/{code}/script")
    public Map<String, Object> script(@PathVariable String code,
                                       @RequestParam(defaultValue = "python") String lang) {
        Map<String, Object> inst;
        try {
            inst = jdbc.queryForMap(
                "SELECT code, connector_code, name, status, config_json " +
                "FROM integration_instance WHERE code=?", code);
        } catch (Exception e) {
            return Map.of("error", "Integration not found: " + code);
        }
        String connector = (String) inst.get("connector_code");
        String cfg = inst.get("config_json") == null ? "{}" : inst.get("config_json").toString();

        String body = buildScript(connector, lang, code, cfg);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("code", code);
        r.put("connector", connector);
        r.put("language", lang);
        r.put("filename", suggestFilename(code, lang));
        r.put("setup", buildSetupSteps(connector, lang));
        r.put("script", body);
        return r;
    }

    // ====================================================================
    // 3. Field mapping CRUD
    // ====================================================================

    @GetMapping("/{code}/mapping")
    public List<Map<String, Object>> getMapping(@PathVariable String code) {
        try {
            return jdbc.queryForList(
                "SELECT id, our_entity, our_field, their_field, transform_expr, direction " +
                "FROM integration_field_mapping WHERE instance_code=? ORDER BY our_entity, our_field",
                code);
        } catch (Exception e) {
            // Table may not exist yet
            return Collections.emptyList();
        }
    }

    @PostMapping("/{code}/mapping")
    public Map<String, Object> postMapping(@PathVariable String code,
                                            @RequestBody List<Map<String, Object>> rows) {
        try {
            jdbc.update("DELETE FROM integration_field_mapping WHERE instance_code=?", code);
            int added = 0;
            for (Map<String, Object> r : rows) {
                jdbc.update(
                    "INSERT INTO integration_field_mapping " +
                    "(instance_code, our_entity, our_field, their_field, transform_expr, direction) " +
                    "VALUES (?,?,?,?,?,?)",
                    code,
                    r.getOrDefault("our_entity", "").toString(),
                    r.getOrDefault("our_field", "").toString(),
                    r.getOrDefault("their_field", "").toString(),
                    r.get("transform_expr") == null ? null : r.get("transform_expr").toString(),
                    r.getOrDefault("direction", "OUT").toString());
                added++;
            }
            return Map.of("ok", true, "added", added);
        } catch (Exception e) {
            return Map.of("ok", false, "error", e.getMessage());
        }
    }

    // ====================================================================
    // Script builders (per connector family)
    // ====================================================================

    private String buildScript(String connector, String lang, String code, String cfg) {
        if ("python".equalsIgnoreCase(lang)) return buildPython(connector, code, cfg);
        if ("nodejs".equalsIgnoreCase(lang) || "node".equalsIgnoreCase(lang)) return buildNode(connector, code, cfg);
        return buildJava(connector, code, cfg);
    }

    private String buildPython(String connector, String code, String cfg) {
        String esc = cfg.replace("\\", "\\\\").replace("\"", "\\\"");
        if (connector == null) connector = "GENERIC";
        String header = "# EARMS — Integration script for `" + code + "` (connector: " + connector + ")\n" +
                        "# Generated " + Instant.now() + "\n" +
                        "# Run as: python " + code + ".py\n\n" +
                        "import json, requests, os\n" +
                        "INSTANCE = '" + code + "'\n" +
                        "EARMS_URL = os.environ.get('EARMS_URL', 'http://localhost:8080')\n" +
                        "EARMS_TOKEN = os.environ.get('EARMS_TOKEN', '')\n" +
                        "CONFIG = json.loads(\"\"\"" + esc + "\"\"\")\n\n";

        if (connector.startsWith("TALLY"))
            return header + tallyPython();
        if (connector.startsWith("SAP"))
            return header + sapPython();
        if (connector.startsWith("NIC") || connector.startsWith("EINV") || connector.startsWith("EWAY"))
            return header + nicEinvPython();
        if (connector.startsWith("MSG91") || connector.startsWith("SMS"))
            return header + msg91Python();
        if (connector.startsWith("ZPL") || connector.startsWith("PRINTER"))
            return header + zplPython();
        if (connector.startsWith("MQTT"))
            return header + mqttPython();
        if (connector.startsWith("MODBUS"))
            return header + modbusPython();
        if (connector.startsWith("IMAP") || connector.startsWith("EMAIL"))
            return header + imapPython();
        return header + genericRestPython();
    }

    private String tallyPython() {
        return  "# Tally TDL — push a sales voucher\n" +
                "TDL = '''<ENVELOPE><HEADER><TALLYREQUEST>Import Data</TALLYREQUEST></HEADER>\n" +
                "<BODY><IMPORTDATA><REQUESTDESC><REPORTNAME>Vouchers</REPORTNAME></REQUESTDESC>\n" +
                "<REQUESTDATA><TALLYMESSAGE xmlns:UDF=\"TallyUDF\">\n" +
                "<VOUCHER VCHTYPE=\"Sales\" ACTION=\"Create\">\n" +
                "<DATE>{date}</DATE><VOUCHERNUMBER>{voucher_no}</VOUCHERNUMBER>\n" +
                "<PARTYLEDGERNAME>{customer}</PARTYLEDGERNAME>\n" +
                "<ALLLEDGERENTRIES.LIST><LEDGERNAME>{customer}</LEDGERNAME>\n" +
                "<ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE><AMOUNT>-{amount}</AMOUNT></ALLLEDGERENTRIES.LIST>\n" +
                "</VOUCHER></TALLYMESSAGE></REQUESTDATA></IMPORTDATA></BODY></ENVELOPE>'''\n\n" +
                "def push_voucher(voucher_no, customer, amount, date):\n" +
                "    body = TDL.format(voucher_no=voucher_no, customer=customer, amount=amount, date=date)\n" +
                "    r = requests.post(CONFIG['endpoint'], data=body, timeout=30)\n" +
                "    print('Tally response:', r.status_code, r.text[:200])\n\n" +
                "if __name__ == '__main__':\n" +
                "    # Pull next pending invoice from EARMS\n" +
                "    rows = requests.get(f'{EARMS_URL}/api/m6/sales-invoice?pushed=false',\n" +
                "        headers={'Authorization': f'Bearer {EARMS_TOKEN}'}).json()\n" +
                "    for inv in rows.get('content', []):\n" +
                "        push_voucher(inv['invoice_no'], inv['customer_name'], inv['amount'], inv['invoice_date'])\n";
    }

    private String sapPython() {
        return  "# SAP S/4 — outbound IDoc / OData via HTTPS basic-auth\n" +
                "def push_material(item_code, description, uom, hsn):\n" +
                "    url = CONFIG['endpoint'] + '/sap/opu/odata/sap/API_PRODUCT_SRV/A_Product'\n" +
                "    payload = {'Product': item_code, 'ProductDescription': description,\n" +
                "               'BaseUnit': uom, 'HSNCode': hsn}\n" +
                "    r = requests.post(url, json=payload, auth=(CONFIG['user'], CONFIG['password']),\n" +
                "                       timeout=30, headers={'X-CSRF-Token': 'Fetch'})\n" +
                "    print('SAP:', r.status_code, r.text[:200])\n" +
                "    return r\n\n" +
                "if __name__ == '__main__':\n" +
                "    items = requests.get(f'{EARMS_URL}/api/m5/items?pushed_to_sap=false',\n" +
                "                         headers={'Authorization': f'Bearer {EARMS_TOKEN}'}).json()\n" +
                "    for it in items.get('content', []):\n" +
                "        push_material(it['code'], it['name'], it['uom'], it.get('hsn',''))\n";
    }

    private String nicEinvPython() {
        return  "# NIC e-Invoice (IRP) — get IRN for a sales invoice\n" +
                "import hashlib, base64\n\n" +
                "def get_auth_token():\n" +
                "    r = requests.post(CONFIG['endpoint'] + '/eivital/v1.04/auth',\n" +
                "        json={'UserName': CONFIG['user'], 'Password': CONFIG['password'],\n" +
                "              'AppKey': CONFIG.get('app_key',''), 'ForceRefreshAccessToken': True})\n" +
                "    return r.json()['Data']['AuthToken']\n\n" +
                "def generate_irn(invoice):\n" +
                "    token = get_auth_token()\n" +
                "    r = requests.post(CONFIG['endpoint'] + '/eicore/v1.03/Invoice',\n" +
                "        headers={'Authorization': token, 'Gstin': CONFIG['gstin']},\n" +
                "        json=invoice, timeout=30)\n" +
                "    data = r.json().get('Data', {})\n" +
                "    return data.get('Irn'), data.get('AckNo'), data.get('SignedQRCode')\n\n" +
                "if __name__ == '__main__':\n" +
                "    invs = requests.get(f'{EARMS_URL}/api/m6/sales-invoice?irn_pending=true',\n" +
                "        headers={'Authorization': f'Bearer {EARMS_TOKEN}'}).json()\n" +
                "    for inv in invs.get('content', []):\n" +
                "        irn, ack, qr = generate_irn(inv)\n" +
                "        print(inv['invoice_no'], '->', irn)\n";
    }

    private String msg91Python() {
        return  "# MSG91 SMS / OTP\n" +
                "def send_otp(mobile, otp):\n" +
                "    headers = {'authkey': CONFIG['auth_key'], 'Content-Type': 'application/json'}\n" +
                "    payload = {'sender': CONFIG['sender_id'], 'route': '4',\n" +
                "               'country': '91', 'sms': [{'message': f'EARMS OTP: {otp}',\n" +
                "                'to': [mobile]}]}\n" +
                "    return requests.post('https://api.msg91.com/api/v2/sendsms', headers=headers,\n" +
                "                          json=payload, timeout=15).json()\n";
    }

    private String zplPython() {
        return  "# ZPL Label Printer (Zebra etc.)\n" +
                "def print_label(zpl):\n" +
                "    import socket\n" +
                "    s = socket.create_connection((CONFIG['host'], int(CONFIG.get('port', 9100))), timeout=10)\n" +
                "    s.sendall(zpl.encode())\n" +
                "    s.close()\n\n" +
                "if __name__ == '__main__':\n" +
                "    lots = requests.get(f'{EARMS_URL}/api/m5/lots?label_pending=true',\n" +
                "        headers={'Authorization': f'Bearer {EARMS_TOKEN}'}).json()\n" +
                "    for lot in lots.get('content', []):\n" +
                "        zpl = f'^XA^CF0,30^FO20,20^FD{lot[\"item_code\"]}^FS^FO20,60^FDLot: {lot[\"lot_no\"]}^FS^XZ'\n" +
                "        print_label(zpl)\n";
    }

    private String mqttPython() {
        return  "# MQTT subscriber for conveyor / sensor stream\n" +
                "import paho.mqtt.client as mqtt\n\n" +
                "def on_message(client, userdata, msg):\n" +
                "    requests.post(f'{EARMS_URL}/api/m11/inbound/' + CONFIG['inbound_token'],\n" +
                "                  data=msg.payload, timeout=5)\n\n" +
                "c = mqtt.Client()\n" +
                "c.on_message = on_message\n" +
                "if CONFIG.get('user'): c.username_pw_set(CONFIG['user'], CONFIG['password'])\n" +
                "c.connect(CONFIG['host'], int(CONFIG.get('port', 1883)), 60)\n" +
                "c.subscribe(CONFIG['topic'])\n" +
                "c.loop_forever()\n";
    }

    private String modbusPython() {
        return  "# Modbus-TCP poller (PLC / weighbridge)\n" +
                "from pymodbus.client import ModbusTcpClient\n\n" +
                "c = ModbusTcpClient(CONFIG['host'], port=int(CONFIG.get('port', 502)))\n" +
                "while True:\n" +
                "    rr = c.read_holding_registers(int(CONFIG['register_addr']), 2)\n" +
                "    if not rr.isError():\n" +
                "        val = (rr.registers[0] << 16) | rr.registers[1]\n" +
                "        requests.post(f'{EARMS_URL}/api/m11/inbound/' + CONFIG['inbound_token'],\n" +
                "                       json={'value': val}, timeout=5)\n";
    }

    private String imapPython() {
        return  "# IMAP inbox — pull attachments (PO / vendor invoice)\n" +
                "from imaplib import IMAP4_SSL\n\n" +
                "m = IMAP4_SSL(CONFIG['host'], int(CONFIG.get('port', 993)))\n" +
                "m.login(CONFIG['user'], CONFIG['password'])\n" +
                "m.select('INBOX')\n" +
                "typ, ids = m.search(None, '(UNSEEN)')\n" +
                "for eid in ids[0].split():\n" +
                "    typ, data = m.fetch(eid, '(RFC822)')\n" +
                "    requests.post(f'{EARMS_URL}/api/m11/inbound/' + CONFIG['inbound_token'],\n" +
                "                   data=data[0][1], timeout=10)\n";
    }

    private String genericRestPython() {
        return  "# Generic REST poller — pulls data from CONFIG['endpoint']\n" +
                "def main():\n" +
                "    r = requests.get(CONFIG['endpoint'], timeout=30,\n" +
                "        headers={'Authorization': CONFIG.get('auth_header', '')})\n" +
                "    requests.post(f'{EARMS_URL}/api/m11/inbound/' + CONFIG['inbound_token'],\n" +
                "                   json=r.json(), timeout=10)\n\n" +
                "if __name__ == '__main__': main()\n";
    }

    private String buildNode(String connector, String code, String cfg) {
        return "// EARMS — Node.js client for " + code + " (connector: " + connector + ")\n" +
               "const axios = require('axios');\n" +
               "const CONFIG = " + cfg + ";\n" +
               "(async () => {\n" +
               "  const r = await axios.get(CONFIG.endpoint, { timeout: 30000 });\n" +
               "  await axios.post(`${process.env.EARMS_URL || 'http://localhost:8080'}" +
               "/api/m11/inbound/${CONFIG.inbound_token}`, r.data,\n" +
               "    { headers: { Authorization: 'Bearer ' + process.env.EARMS_TOKEN } });\n" +
               "  console.log('pushed', r.data && r.data.length, 'rows');\n" +
               "})();\n";
    }

    private String buildJava(String connector, String code, String cfg) {
        return "// EARMS — Java snippet for " + code + " (connector: " + connector + ")\n" +
               "// Run inside a Spring Boot app or as a CLI with java -jar\n" +
               "import java.net.http.*;\nimport java.net.URI;\n\n" +
               "public class " + suggestClassName(code) + " {\n" +
               "  static final String CONFIG = \"\"\"\n" + cfg + "\n\"\"\";\n" +
               "  public static void main(String[] args) throws Exception {\n" +
               "    var c = HttpClient.newHttpClient();\n" +
               "    // TODO wire to your endpoint\n" +
               "  }\n" +
               "}\n";
    }

    private List<String> buildSetupSteps(String connector, String lang) {
        List<String> steps = new ArrayList<>();
        if ("python".equalsIgnoreCase(lang)) {
            steps.add("Install Python 3.10+");
            steps.add("pip install requests");
            if (connector != null && connector.startsWith("MQTT"))    steps.add("pip install paho-mqtt");
            if (connector != null && connector.startsWith("MODBUS"))  steps.add("pip install pymodbus");
        } else if ("nodejs".equalsIgnoreCase(lang)) {
            steps.add("Install Node 18+");
            steps.add("npm install axios");
        }
        steps.add("export EARMS_URL=https://your-earms-host");
        steps.add("export EARMS_TOKEN=<your JWT>");
        steps.add("Schedule via cron (e.g. */15 * * * *) or Airflow / our agent");
        return steps;
    }

    private String suggestFilename(String code, String lang) {
        String base = code.replaceAll("[^A-Za-z0-9_-]", "_");
        switch (lang.toLowerCase()) {
            case "python": return base + ".py";
            case "nodejs":
            case "node":   return base + ".js";
            case "java":   return suggestClassName(code) + ".java";
            default:       return base + "." + lang;
        }
    }

    private String suggestClassName(String code) {
        StringBuilder b = new StringBuilder();
        boolean cap = true;
        for (char c : code.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                b.append(cap ? Character.toUpperCase(c) : c);
                cap = false;
            } else cap = true;
        }
        if (b.length() == 0 || !Character.isLetter(b.charAt(0))) b.insert(0, "X");
        return b.toString();
    }

    /** Cheap JSON-string field extractor used for the health URL — avoids pulling in Jackson here. */
    private String extractFirst(String json, String... keys) {
        for (String k : keys) {
            int i = json.indexOf("\"" + k + "\"");
            if (i < 0) continue;
            int colon = json.indexOf(':', i);
            int q1 = json.indexOf('"', colon + 1);
            int q2 = json.indexOf('"', q1 + 1);
            if (q1 > 0 && q2 > q1) return json.substring(q1 + 1, q2);
        }
        return null;
    }
}

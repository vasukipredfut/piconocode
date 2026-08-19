package com.aria.mes.orderdispatch;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises {@code OrderDispatchDetailImportController#parseTruckOrderSheetRows}
 * (the printed truck/route order-sheet PDF parser) directly against a
 * synthetic text block shaped like PDFBox's extraction of that layout —
 * including the running header/column-header repeating mid-order across a
 * page break, and a wrapped order description and a wrapped SKU description,
 * both of which a naive line-by-line parser could misfile as data.
 */
class OrderDispatchDetailPdfParseTest {

    private static final String SAMPLE_TEXT = String.join("\n",
        "Truck No: ATTUR (ROU031405)                    Total Wgt: 10182.13",
        "Driver Details: N / 0000000000",
        "Total Orders: 08   Total Qty: 831   Final Amt: 1661840.00          Scheduled On: 18/08/2026",
        "Supervisor                                                 Load Weight",
        "Reason                                       ** on load weight greater than; 10182.13 Kgs",
        "ORD115282 / CHINNASALEM-SRI.MADHURA AGENCIES -",
        "CHINNA SALEM",
        "GST:33AMFPL1762K1Z1 Contact No: 9566641242          Created: 28/07/2026 09:56:37 AM",
        "Lines: 01 Qty: 200 Wgt: 2921.60 Amt: 504000.00      Submited: 28/07/2026 09:56:50 AM",
        "No  Check  SKU Description                L/Qty   Rate    Weight",
        "01         REF SUNFLOWER OIL 15 LTR TIN ROYAL           200   2520.00 2921.60",
        "Total                                                   200          2921.60",
        "ORD116917 / CHINNASASALEM-MOHAN R (MOHAN OIL&",
        "MALIGAI) - CHINNASASALEM",
        "GST:33BFOPM2294A1ZL Contact No: 6380117707          Created: 06/08/2026 07:42:26 PM",
        "Lines: 05 Qty: 225 Wgt: 2183.00 Amt: 382720.00      Submited: 06/08/2026 07:43:30 PM",
        "No  Check  SKU Description                L/Qty   Rate    Weight",
        "01         DAILY DOUBLE ROASTED RAVA 500gPOUCH-30Kg BAG     5   1840.00 152.47",
        "(COIN)",
        // page-break header repeats mid-order, before the next line item
        "Truck No: ATTUR (ROU031405)                    Total Wgt: 10182.13",
        "Driver Details: N / 0000000000",
        "Total Orders: 08   Total Qty: 831   Final Amt: 1661840.00          Scheduled On: 18/08/2026",
        "02         REF SUNFLOWER OIL 1 LTR BOX                 210   1735.00 2014.53",
        "Total                                                   225         2183.00"
    );

    /** No sku_master rows — cols[14] (sku_code) stays null; these tests don't concern themselves with it. */
    @SuppressWarnings("unchecked")
    private static List<List<String>> parse(String text) throws Exception {
        return parse(text, List.of());
    }

    @SuppressWarnings("unchecked")
    private static List<List<String>> parse(String text, List<String[]> skuMasterRows) throws Exception {
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        Mockito.when(jdbc.query(Mockito.anyString(), Mockito.any(RowMapper.class), Mockito.any(Object.class)))
            .thenReturn((List) skuMasterRows);
        OrderDispatchDetailImportController controller = new OrderDispatchDetailImportController(jdbc);
        Method m = OrderDispatchDetailImportController.class
            .getDeclaredMethod("parseTruckOrderSheetRows", String.class);
        m.setAccessible(true);
        return (List<List<String>>) m.invoke(controller, text);
    }

    @Test
    void firstRowIsSyntheticHeaderSkippedByCaller() throws Exception {
        List<List<String>> rows = parse(SAMPLE_TEXT);
        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0)).containsOnlyNulls();
    }

    @Test
    void mapsOrderVehicleRouteAndLineFieldsByColumnPosition() throws Exception {
        List<List<String>> rows = parse(SAMPLE_TEXT);
        // header row + 3 SKU lines (ORD115282 x1, ORD116917 x2)
        assertThat(rows).hasSize(4);

        List<String> r1 = rows.get(1);
        assertThat(r1.get(1)).isEqualTo("ORD115282");               // order_id
        assertThat(r1.get(3)).isEqualTo("CHINNASALEM-SRI.MADHURA AGENCIES - CHINNA SALEM"); // rd_desc (wrapped)
        assertThat(r1.get(4)).isEqualTo("28-07-2026/09:56:37/AM");  // created_on, normalized
        assertThat(r1.get(5)).isEqualTo("28-07-2026/09:56:50/AM");  // submitted_on
        assertThat(r1.get(15)).isEqualTo("REF SUNFLOWER OIL 15 LTR TIN ROYAL"); // sku_desc (leading "01" index stripped)
        assertThat(r1.get(19)).isEqualTo("2520.00");                // order_rate
        assertThat(r1.get(24)).isEqualTo("ROU031405");              // route_id
        assertThat(r1.get(26)).isEqualTo("200");                    // assign_qty
        assertThat(r1.get(7)).isEqualTo("200");                     // order_qty (order block's own "Qty:" total)
        assertThat(r1.get(33)).isEqualTo("2921.60");                // dispatch_wgt (order block's own "Wgt:" total)
        assertThat(r1.get(28)).isEqualTo("ATTUR");                  // vehicle_no
        assertThat(r1.get(29)).isEqualTo("N");                      // driver_name
        assertThat(r1.get(30)).isEqualTo("0000000000");             // driver_phone
    }

    @Test
    void wrappedSkuDescriptionContinuationIsAppendedNotMisfiled() throws Exception {
        List<List<String>> rows = parse(SAMPLE_TEXT);
        List<String> r2 = rows.get(2);
        assertThat(r2.get(1)).isEqualTo("ORD116917");
        assertThat(r2.get(15)).isEqualTo("DAILY DOUBLE ROASTED RAVA 500gPOUCH-30Kg BAG (COIN)");
    }

    @Test
    void pageBreakHeaderRepeatedMidOrderIsSkippedAsNoise() throws Exception {
        List<List<String>> rows = parse(SAMPLE_TEXT);
        // the 3rd SKU row belongs to the SAME order (ORD116917) as the row before
        // the repeated header — proving the header wasn't parsed as a new order
        // or appended into the prior row's description.
        List<String> r3 = rows.get(3);
        assertThat(r3.get(1)).isEqualTo("ORD116917");
        assertThat(r3.get(15)).isEqualTo("REF SUNFLOWER OIL 1 LTR BOX");
        assertThat(r3.get(26)).isEqualTo("210");
        // order_qty/dispatch_wgt are the ORDER block's totals (225 / 2183.00), not
        // this line's own L/Qty (210) or Weight (2014.53) — same value on both of
        // this order's rows (r2 and r3), unaffected by the header noise between them.
        assertThat(r3.get(7)).isEqualTo("225");
        assertThat(r3.get(33)).isEqualTo("2183.00");
        assertThat(rows.get(2).get(7)).isEqualTo("225");
        assertThat(rows.get(2).get(33)).isEqualTo("2183.00");
    }

    /**
     * sku_code resolution (cols[14]) against m5_sku_master.sku_name by fuzzy token
     * overlap. Thresholds here are calibrated against real data — see
     * OrderDispatchDetailImportController#SKU_MATCH_MIN_SCORE.
     */
    @Test
    void resolvesSkuCodeAndReplacesDescriptionWithCleanMasterNameWhenMatched() throws Exception {
        List<String[]> skuMaster = List.of(
            new String[]{"SKU1461", "SKU1461 (REF SUNFLOWER OIL 15 LTR TIN (ROYAL))"},
            new String[]{"SKU1459", "SKU1459 (REF SUNFLOWER OIL 15 LTR TIN)"},
            new String[]{"SKU1463", "SKU1463 (REF SUNFLOWER OIL 1 LTR BOX)"},
            new String[]{"SKU9383", "SKU9383 (DAILY DOUBLE ROASTED RAVA 500gPOUCH-30Kg BAG (COIN))"}
        );
        List<List<String>> rows = parse(SAMPLE_TEXT, skuMaster);

        // sku_desc becomes the master's own clean name (code prefix and outer parens
        // stripped) — not the noisy OCR text — whenever a match is found.
        assertThat(rows.get(1).get(15)).isEqualTo("REF SUNFLOWER OIL 15 LTR TIN (ROYAL)");
        assertThat(rows.get(1).get(14)).isEqualTo("SKU1461"); // not SKU1459 (missing "ROYAL")

        assertThat(rows.get(2).get(15)).isEqualTo("DAILY DOUBLE ROASTED RAVA 500gPOUCH-30Kg BAG (COIN)");
        assertThat(rows.get(2).get(14)).isEqualTo("SKU9383");

        assertThat(rows.get(3).get(15)).isEqualTo("REF SUNFLOWER OIL 1 LTR BOX");
        assertThat(rows.get(3).get(14)).isEqualTo("SKU1463");
    }

    @Test
    void leavesSkuCodeNullWhenNoMasterEntryClearsTheMatchThreshold() throws Exception {
        // Only an unrelated product in the master — real ATTUR LINE.pdf lines with
        // no seeded master entry (RSFO empty carton, Kozhukattai flour) landed at
        // 0.556 or below against the *closest* unrelated candidate; nothing here
        // should clear SKU_MATCH_MIN_SCORE (0.65) for "REF SUNFLOWER OIL...".
        List<String[]> skuMaster = List.<String[]>of(
            new String[]{"SKU9126", "SKU9126 (DAILY SAMBA RAVA 500g POUCH - 10Kg BAG)"}
        );
        List<List<String>> rows = parse(SAMPLE_TEXT, skuMaster);
        assertThat(rows.get(1).get(14)).isNull();
        // still gets the leading-noise strip even with no master match
        assertThat(rows.get(1).get(15)).isEqualTo("REF SUNFLOWER OIL 15 LTR TIN ROYAL");
    }
}

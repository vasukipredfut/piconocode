package com.aria.service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestServ {

    public static void main(String[] args) {

        double poQty = 100.0;
        double fixedQty = 2;
        if (  fixedQty > 0 && (poQty > fixedQty) ) {
            System.out.println("PoClosed");
        }else {
            System.out.println("open");
        }
    /*{
        HashMap<String,Double>map=new HashMap<String, Double>();
        map.put("1", 50.0);
        map.put("2", 60.0);
        map.put("3", 30.0);
        Double maxValueInMap=(Collections.max(map.values()));
        System.out.println(maxValueInMap);
        for (Map.Entry<String, Double> entry : map.entrySet()) {  // Iterate through HashMap
            if (entry.getValue() == maxValueInMap) {
                System.out.println(entry.getKey());     // Print the key with max value
            }
        }
*/
        /*Double d = 12234566.0;

        String  d1 = new BigDecimal(d).toPlainString();
        Double d2  = Double.valueOf(d1);
        System.out.println(d2);*/



        /*public TtmCatalog getCatalogForHelpElement(String elementName, String locale) {
            Jdbi dbiPostgres = db.getJdbi();
            return dbiPostgres.withHandle(handle ->
            {
                handle.registerRowMapper(FieldMapper.factory(TtmCatalog.class));
                return handle.createQuery("""
                               SELECT tc.* FROM ttm_catalog  tc
                               inner join help_section  hs  on hs.metadata->>'sku'=tc.training_code
                               and hs.element=:elementName
                               and tc.locale=:locale
                           """)
                        .bind("elementName", elementName)
                        .bind("locale", locale)
                        .mapTo(TtmCatalog.class)
                        .findFirst().orElse(null);
            });
        }*/

    }

}

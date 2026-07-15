package com.aria.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;

import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CheckMigrationService {


    @Autowired
    GstTaxCategoryRepository hsnMasterRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    PurchaseExpenseRepository purchaseExpenseRepository;


    public void updateMigrationHsnCategory() {

        List<GstTaxCategory> gstTaxCategory = hsnMasterRepository.findAll();
        if (!gstTaxCategory.isEmpty()) {

            for (GstTaxCategory taxCategory : gstTaxCategory) {
                System.out.println(" id == " + taxCategory.getId());
                Boolean product = false;
                Boolean productUpdate = false;
                Boolean machine = false;
                Boolean machineUpdate = false;
                Boolean expense = false;
                Boolean expenseUpdate = false;

                List<Map<Long, String>> maps = new ArrayList<>();
                List<ProductMaster> productlist = productMasterRepository.findByHsn(taxCategory.getHsnCode());
                if (!productlist.isEmpty()) {
                    product = true;
                }
                List<Machine> machinelist = machineRepository.findByHsn(taxCategory.getHsnCode());
                if (!machinelist.isEmpty()) {
                    machine = true;
                }
                List<PurchaseExpense> expenselist = purchaseExpenseRepository.findByHsn(taxCategory.getHsnCode());
                if (!expenselist.isEmpty()) {
                    expense = true;
                }


                if (product) {

                    productUpdate = true;
                    String values = null;
                    taxCategory.setCategory("P");
                    hsnMasterRepository.save(taxCategory);

                }

                if (productUpdate) {

                    if (machine) {

                        GstTaxCategory newGstTaxCategory = new GstTaxCategory(taxCategory);
                        machineUpdate = true;
                        String values = null;
                        newGstTaxCategory.setCategory("M");
                        newGstTaxCategory.setItemListMap(values);
                        hsnMasterRepository.save(newGstTaxCategory);

                    } else if (expense) {

                        GstTaxCategory newGstTaxCategory = new GstTaxCategory(taxCategory);
                        expenseUpdate = true;
                        String values = null;
                        newGstTaxCategory.setCategory("E");
                        newGstTaxCategory.setItemListMap(values);
                        hsnMasterRepository.save(newGstTaxCategory);

                    }

                } else {

                    if (machine) {

                        machineUpdate = true;
                        String values = null;
                        taxCategory.setCategory("M");
                        taxCategory.setItemListMap(values);
                        hsnMasterRepository.save(taxCategory);

                    } else if (expense) {

                        expenseUpdate = true;
                        String values = null;
                        taxCategory.setCategory("E");
                        taxCategory.setItemListMap(values);
                        hsnMasterRepository.save(taxCategory);

                    }

                    if (machineUpdate) {
                        if (expense) {

                            GstTaxCategory newGstTaxCategory = new GstTaxCategory(taxCategory);
                            String values = null;
                            newGstTaxCategory.setCategory("E");
                            newGstTaxCategory.setItemListMap(values);
                            hsnMasterRepository.save(newGstTaxCategory);

                        }
                    }

                }

            }
        }
    }

    public void updateMigrationHsnItem() {

        List<GstTaxCategory> gstTaxCategory = hsnMasterRepository.findAll();

        if (!gstTaxCategory.isEmpty()) {

            for (GstTaxCategory taxCategory : gstTaxCategory) {
                System.out.println(" id == "+taxCategory.getId());
                if (taxCategory.getCategory() != null && taxCategory.getCategory().equalsIgnoreCase("P")) {

                    List<ProductMaster> productlist = productMasterRepository.findByHsn(taxCategory.getHsnCode());

                    if (!productlist.isEmpty()) {

                        List<Map<Long, String>> maps = new ArrayList<>();
                        for (ProductMaster productMaster : productlist) {
                            Map<Long, String> stringMap = new HashMap<>();
                            stringMap.put(productMaster.getId(), productMaster.getProductName());
                            maps.add(stringMap);
                        }
                        String values = null;
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            if (!maps.isEmpty()) {
                                values = mapper.writeValueAsString(maps);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        taxCategory.setItemListMap(values);
                        hsnMasterRepository.save(taxCategory);

                    }

                } else if ( taxCategory.getCategory() != null && taxCategory.getCategory().equalsIgnoreCase("M")) {

                    List<Machine> machinelist = machineRepository.findByHsn(taxCategory.getHsnCode());

                    if (!machinelist.isEmpty()) {

                        List<Map<Long, String>> maps = new ArrayList<>();
                        for (Machine machine1 : machinelist) {
                            Map<Long, String> stringMap = new HashMap<>();
                            stringMap.put(machine1.getId(), machine1.getName());
                            maps.add(stringMap);
                        }
                        String values = null;
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            if (!maps.isEmpty()) {
                                values = mapper.writeValueAsString(maps);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        taxCategory.setItemListMap(values);
                        hsnMasterRepository.save(taxCategory);
                    }

                } else if ( taxCategory.getCategory() != null && taxCategory.getCategory().equalsIgnoreCase("E")) {

                    List<PurchaseExpense> expenselist = purchaseExpenseRepository.findByHsn(taxCategory.getHsnCode());

                    if (!expenselist.isEmpty()) {

                        List<Map<Long, String>> maps = new ArrayList<>();

                        for (PurchaseExpense purchaseExpense : expenselist) {
                            Map<Long, String> stringMap = new HashMap<>();
                            stringMap.put(purchaseExpense.getAmc_Id(), purchaseExpense.getAmc_Expense_Name());
                            maps.add(stringMap);
                        }

                        String values = null;
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            if (!maps.isEmpty()) {
                                values = mapper.writeValueAsString(maps);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }

                        taxCategory.setItemListMap(values);
                        hsnMasterRepository.save(taxCategory);
                    }

                }

            }
        }
    }

}

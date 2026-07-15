package com.aria.utils;

import com.aria.dao.BlendingStockDetailsRepostiory;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BlendingUtil {

    @Autowired
    public BlendingStockDetailsRepostiory blendingStockDetailsRepostiory;


    public void updateBlendingStock(DirectPurchase directPurchase, DpEntryStoredProduct storedProduct, DpEntryStoredBinDetails binDetails, User loggedUser) {

        if (storedProduct.getId() != null && storedProduct.getBatchNo() != null && binDetails.getBinNo() != null) {
            Optional<BlendingStockDetails> blendingStockDetails = blendingStockDetailsRepostiory.findByProductBatchAndBin(storedProduct.getId(), storedProduct.getBatchNo(), binDetails.getBinNo(), binDetails.getBinId());

            if (blendingStockDetails.isPresent()) {
                BlendingStockDetails stockDetails = blendingStockDetails.get();
                stockDetails.setBatchId(storedProduct.getBatchId());
                stockDetails.setBatchNo(storedProduct.getBatchNo());
                stockDetails.setRefId(storedProduct.getId());
                stockDetails.setBinId(binDetails.getBinNo());
                stockDetails.setBinName(binDetails.getBinId());
                stockDetails.setStkQty(binDetails.getAllottedQuantity());
                //stockDetails.setState(storedProduct.getCamp().getState());
                //stockDetails.setLocation(storedProduct.getCamp().getCamp());
                stockDetails.setBranch(loggedUser.getBranch_id());
                stockDetails.setDept(loggedUser.getDepartment().getId());
                blendingStockDetailsRepostiory.save(stockDetails);
            } else {
                BlendingStockDetails stockDetails = new BlendingStockDetails();
                stockDetails.setBatchId(storedProduct.getBatchId());
                stockDetails.setBatchNo(storedProduct.getBatchNo());
                stockDetails.setRefId(storedProduct.getId());
                stockDetails.setBinId(binDetails.getBinNo());
                stockDetails.setBinName(binDetails.getBinId());
                stockDetails.setStkQty(binDetails.getAllottedQuantity());
                //stockDetails.setState(storedProduct.getCamp().getState());
                //stockDetails.setLocation(storedProduct.getCamp().getCamp());
                stockDetails.setBranch(loggedUser.getBranch_id());
                stockDetails.setDept(loggedUser.getDepartment().getId());
                blendingStockDetailsRepostiory.save(stockDetails);
            }
        }

    }

    public void updateBlendingStockByLock(BinLock binLock, User loggedUser) {

        if (binLock.getProduct().getId() != null && binLock.getBatchNo() != null && binLock.getBinNo() != null) {

            List<BlendingStockDetails> blendingStockDetailsList = blendingStockDetailsRepostiory.findByProductBatchAndBin(binLock.getProduct().getId(), binLock.getBatchNo(), binLock.getBinNo());
            for (BlendingStockDetails blendingStockDetails : blendingStockDetailsList) {
                if (blendingStockDetails != null) {
                    BlendingStockDetails stockDetails = blendingStockDetails;
                    stockDetails.setBatchId(binLock.getBatchId());
                    stockDetails.setBatchNo(binLock.getBatchNo());
                    stockDetails.setRefId(binLock.getProduct().getId());
                    stockDetails.setBinId(binLock.getBinNo());
                    stockDetails.setBranch(loggedUser.getBranch_id());
                    stockDetails.setDept(loggedUser.getDepartment().getId());
                    stockDetails.setLockType(binLock.getLockType());
                    if (binLock.getLockStatus().equalsIgnoreCase("Lock")) {
                        //stockDetails.setLockBy(loggedUser.getId());
                        stockDetails.setLockStatus("Y");
                    } else {
                        stockDetails.setLockStatus("");
                        //stockDetails.setUnLockBy(loggedUser.getId());
                    }
                    stockDetails.setDept(loggedUser.getDepartment().getId());
                    blendingStockDetailsRepostiory.save(stockDetails);
                }
            }
        }
    }

    /*public void updateBlendingStockByFFA(SeedOilIVPVTest oilIVPVTest, User loggedUser) {

        if (oilIVPVTest.getInward().getBatchNo() != null) {

            List<BlendingStockDetails> blendingStockDetailsList = blendingStockDetailsRepostiory.findByProductBatchNo(oilIVPVTest.getInward().getBatchNo());
            for (BlendingStockDetails blendingStockDetails : blendingStockDetailsList) {
                if (blendingStockDetails != null) {
                    BlendingStockDetails stockDetails = blendingStockDetails;
                    stockDetails.setOilFFA(new Double(oilIVPVTest.getOilFFA()));
                    //stockDetails.setUpdatedOilFFABy(loggedUser.getId());
                    blendingStockDetailsRepostiory.save(stockDetails);
                }
            }
        }
    }*/
}

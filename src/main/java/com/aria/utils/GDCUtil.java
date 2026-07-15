package com.aria.utils;

import com.aria.dao.TRNIndexRepo;
import com.aria.model.TRNIndex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class GDCUtil {


    @Autowired
    TRNIndexRepo trnIndexRepo;

    @Autowired
    DateUtil dateUtil;

    public String getGDCNo(Long branch, Long company) {

        String gdcNo = null;
        String transactionNumber = null;
        Long lastTransactionNumber = null;
        Optional<TRNIndex> trnIndex = null;
        System.out.println(" GDC  :: check create Time Start :: " + dateUtil.getISTDate());
        try {

            if (branch == 1) {
                trnIndex = trnIndexRepo.findByPrefix("GVNR", company);
            } else if (branch == 2) {
                trnIndex = trnIndexRepo.findByPrefix("GVLP", company);
            } else if (branch == 3) {
                trnIndex = trnIndexRepo.findByPrefix("GVRL", company);
            } else if (branch == 4) {
                trnIndex = trnIndexRepo.findByPrefix("GSS", company);
            } else if (branch == 11) {
                trnIndex = trnIndexRepo.findByPrefix("GRJM", company);
            } else if (branch == 12) {
                trnIndex = trnIndexRepo.findByPrefix("GRJMT", company);
            } else if (branch == 22) {
                trnIndex = trnIndexRepo.findByPrefix("GROSALPT", company);
            }


            if (trnIndex.isPresent()) {
                lastTransactionNumber = trnIndex.get().getLastno();
                if (branch == 1 && company == 1) {
                    transactionNumber = "V/VS/"+lastTransactionNumber;
                } else if (branch == 1 && company == 2) {
                    transactionNumber = "V/VA/"+lastTransactionNumber;
                } else if (branch == 1 && company == 3) {
                    transactionNumber = "V/VR/"+lastTransactionNumber;
                } else if (branch == 1 && company == 6) {
                    transactionNumber = "V/RJ/"+lastTransactionNumber;
                } else if (branch == 2 && company == 1) {
                    transactionNumber = "A/VS/"+lastTransactionNumber;
                } else if (branch == 2 && company == 2) {
                    transactionNumber = "A/VA/"+lastTransactionNumber;
                } else if (branch == 2 && company == 3) {
                    transactionNumber = "A/VR/"+lastTransactionNumber;
                } else if (branch == 2 && company == 6) {
                    transactionNumber = "A/RJ/"+lastTransactionNumber;
                } else if (branch == 3 && company == 1) {
                    transactionNumber = "K/VS/"+lastTransactionNumber;
                } else if (branch == 3 && company == 2) {
                    transactionNumber = "K/VA/"+lastTransactionNumber;
                } else if (branch == 3 && company == 3) {
                    transactionNumber = "K/VR/"+lastTransactionNumber;
                } else if (branch == 3 && company == 6) {
                    transactionNumber = "K/RJ/"+lastTransactionNumber;
                } else if (branch == 4 && company == 1) {
                    transactionNumber = "S/VS/"+lastTransactionNumber;
                } else if (branch == 4 && company == 2) {
                    transactionNumber = "S/VA/"+lastTransactionNumber;
                } else if (branch == 4 && company == 3) {
                    transactionNumber = "S/VR/"+lastTransactionNumber;
                } else if (branch == 4 && company == 6) {
                    transactionNumber = "S/RJ/"+lastTransactionNumber;
                } else if (branch == 11 && company == 1) {
                    transactionNumber = "R/VS/"+lastTransactionNumber;
                } else if (branch == 11 && company == 2) {
                    transactionNumber = "R/VA/"+lastTransactionNumber;
                } else if (branch == 11 && company == 3) {
                    transactionNumber = "R/VR/"+lastTransactionNumber;
                } else if (branch == 11 && company == 6) {
                    transactionNumber = "R/RJ/"+lastTransactionNumber;
                } else if (branch == 12 && company == 1) {
                    transactionNumber = "RT/VS/"+lastTransactionNumber;
                } else if (branch == 12 && company == 2) {
                    transactionNumber = "RT/VA/"+lastTransactionNumber;
                } else if (branch == 12 && company == 3) {
                    transactionNumber = "RT/VR/"+lastTransactionNumber;
                } else if (branch == 12 && company == 6) {
                    transactionNumber = "RT/RJ/"+lastTransactionNumber;
                } else if (branch == 22 && company == 1) {
                    transactionNumber = "VVV/RT/"+lastTransactionNumber;
                } else if (branch == 22 && company == 3) {
                    transactionNumber = "VRM/RT/"+lastTransactionNumber;
                } else if (branch == 22 && company == 2) {
                    transactionNumber = "VRA/RT/"+lastTransactionNumber;
                }
                Long trnNo = lastTransactionNumber + 1 ;
                TRNIndex  trnrs=trnIndex.get();
                trnrs.setLastno(trnNo);
                trnIndexRepo.save(trnrs);
                String suffix = null;
                Map<String,Long> stringLongMap = dateUtil.getDateData();
                if(stringLongMap != null) {
                    if (stringLongMap.get("m") < 4) {
                        Long trnyear = stringLongMap.get("y") - 1;
                        Long trnToYear = trnyear + 1;
                        suffix = trnyear +"-"+trnToYear;
                    } else {
                        Long trnyear = stringLongMap.get("y");
                        Long trnToYear = trnyear + 1;
                        suffix = trnyear +"-"+trnToYear;
                    }
                    gdcNo = transactionNumber+"/"+suffix;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occurred while processing script batch" + e);
        }
        return gdcNo;
    }
}

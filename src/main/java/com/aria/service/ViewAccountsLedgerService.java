package com.aria.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.aria.dto.ViewAccountLedgerList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.ViewAccountsLedgerRepository;
import com.aria.dto.PageDataDto;
import com.aria.dto.ViewAccountLedgerDto;
import com.aria.utils.DateUtil;

@Service
public class ViewAccountsLedgerService {

    @Autowired
    ViewAccountsLedgerRepository viewAccountsLedgerRepository;

    @Autowired
    DateUtil dateUtil;

    public ResponseEntity<?> viewAccountsLedgerListAll(Long firmId , Long partyId, String dateFrom, String dateTo, Pageable pageable) {

        Date startDate = null;
        Date endDate = null;

        if (dateFrom != null) {
            startDate = dateUtil.getStringTODateWk(dateFrom);
        }

        if (dateTo != null) {
            endDate = dateUtil.getStringTODateWk(dateTo);
        }

        Map<String, Double> stringDoubleMap = new HashMap<>();

        double creditAmount = viewAccountsLedgerRepository.creditAmount(firmId, partyId, startDate);

        double debitAmount = viewAccountsLedgerRepository.debitAmount(firmId, partyId, startDate);

        double finalAm = debitAmount - creditAmount ;
        if(finalAm > 0){
            stringDoubleMap.put("drval", finalAm);
            stringDoubleMap.put("crval", 0.0);
        }else {
        	double finalCm = creditAmount - debitAmount  ;
            stringDoubleMap.put("crval", finalCm);
            stringDoubleMap.put("drval", 0.0);
        }

        Page<ViewAccountLedgerList> accountLedgerList = viewAccountsLedgerRepository.getAccountsLedgerList(firmId, partyId, startDate, endDate, pageable);

        System.out.println(accountLedgerList);


        List<ViewAccountLedgerDto> list = accountLedgerList.getContent().stream().map(data -> {
            ViewAccountLedgerDto viewAccountLedgerDto = new ViewAccountLedgerDto(data);
            if (viewAccountLedgerDto.getTrnType() == 2 || viewAccountLedgerDto.getTrnType() == 3 || viewAccountLedgerDto.getTrnType() == 4) {


                Double val = stringDoubleMap.get("drval") + data.getAmount();
                stringDoubleMap.put("drval", val);

                Double rs = stringDoubleMap.get("drval") - stringDoubleMap.get("crval");

                if(rs < 0){

                    String vre = rs.toString().replace("-","");
                    Double drs = Double.valueOf(vre);
                    stringDoubleMap.put("crval", drs);
                    viewAccountLedgerDto.setBalance(drs);
                    viewAccountLedgerDto.setBalanceVal(drs + "cr");

				} else {

                    stringDoubleMap.put("drval", rs);
                    viewAccountLedgerDto.setBalance(rs);
                    viewAccountLedgerDto.setBalanceVal(rs + "dr");
				}



            } else {

				Double val = stringDoubleMap.get("crval") + data.getAmount();
				stringDoubleMap.put("crval", val);

				Double rs = stringDoubleMap.get("crval") - stringDoubleMap.get("drval");

                if(rs < 0){

                    String vre = rs.toString().replace("-","");
                    Double drs = Double.valueOf(vre);
                    stringDoubleMap.put("drval", drs);
                    viewAccountLedgerDto.setBalance(drs);
                    viewAccountLedgerDto.setBalanceVal(drs + "dr");

                } else {

                    stringDoubleMap.put("crval", rs);
                    viewAccountLedgerDto.setBalance(rs);
                    viewAccountLedgerDto.setBalanceVal(rs + "cr");

                }

            }
            return viewAccountLedgerDto;
        }).collect(Collectors.toList());
        PageDataDto<ViewAccountLedgerDto> pageData = new PageDataDto<>(list, accountLedgerList);
        return new ResponseEntity<PageDataDto<ViewAccountLedgerDto>>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> printAccountsLedgerReport(Long partyId, String dateFrom, String dateTo) {

        Date startDate = null;
        Date endDate = null;

        if (dateFrom != null) {
            startDate = dateUtil.getStringTODateWk(dateFrom);
        }

        if (dateTo != null) {
            endDate = dateUtil.getStringTODateWk(dateTo);
        }

        List<ViewAccountLedgerList> accountLedgerList = viewAccountsLedgerRepository.getAccountsLedgerList(partyId, startDate, endDate);

        System.out.println(accountLedgerList);
        Map<String, Double> stringDoubleMap = new HashMap<>();
		stringDoubleMap.put("drval", 0.0);
		stringDoubleMap.put("crval", 0.0);
        List<ViewAccountLedgerDto> list = accountLedgerList.stream().map(data -> {
            ViewAccountLedgerDto viewAccountLedgerDto = new ViewAccountLedgerDto(data);
            if (viewAccountLedgerDto.getTrnType() == 2 || viewAccountLedgerDto.getTrnType() == 3 || viewAccountLedgerDto.getTrnType() == 4) {


                Double val = stringDoubleMap.get("drval") + data.getAmount();
                stringDoubleMap.put("drval", val);

                Double rs = stringDoubleMap.get("drval") - stringDoubleMap.get("crval");
                if(rs < 0){

                    String vre = rs.toString().replace("-","");
                    Double drs = Double.valueOf(vre);
                    stringDoubleMap.put("crval", drs);
                    viewAccountLedgerDto.setBalance(drs);
                    viewAccountLedgerDto.setBalanceVal(drs + "cr");

                } else {

                    stringDoubleMap.put("drval", rs);
                    viewAccountLedgerDto.setBalance(rs);
                    viewAccountLedgerDto.setBalanceVal(rs + "dr");
                }

            } else {

                Double val = stringDoubleMap.get("crval") + data.getAmount();
                stringDoubleMap.put("crval", val);

                Double rs = stringDoubleMap.get("crval") - stringDoubleMap.get("drval");

                if(rs < 0){

                    String vre = rs.toString().replace("-","");
                    Double drs = Double.valueOf(vre);
                    stringDoubleMap.put("drval", drs);
                    viewAccountLedgerDto.setBalance(drs);
                    viewAccountLedgerDto.setBalanceVal(drs + "dr");

                } else {

                    stringDoubleMap.put("crval", rs);
                    viewAccountLedgerDto.setBalance(rs);
                    viewAccountLedgerDto.setBalanceVal(rs + "cr");

                }

            }
            return viewAccountLedgerDto;
        }).collect(Collectors.toList());

        return new ResponseEntity<List<ViewAccountLedgerDto>>(list, HttpStatus.OK);
    }


}
package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aria.dao.CExpensesRepository;
import com.aria.dao.TRNIndexRepo;
import com.aria.dto.QcInProcessDto;
import com.aria.model.BinStock;
import com.aria.model.CExpenses;
import com.aria.model.TRNIndex;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class CExpenseService {
	
	 @Autowired
	    public DateUtil dateUtil;
	 @Autowired
	 TRNIndexRepo tRNIndexRepo;
	 @Autowired
	 CExpensesRepository cExpensesRepository;

	public void updateCexpenseInprocessQcTransaction(BinStock binstock, User user, QcInProcessDto dto) {
		 Date currentDate = dateUtil.getISTDate();
		 CExpenses cExpenses = new CExpenses();
		 String prefixType = "CONS";
         TRNIndex trnIndex = tRNIndexRepo.findByPrefix(prefixType)
                 .orElseGet(() -> {
                     TRNIndex t = new TRNIndex();
                     t.setPrefix(prefixType);
                     t.setVouno(0L);
                     return t;
                 });
         if(trnIndex.getVouno()==null)
         {
        	 trnIndex.setVouno(0L);
         }
         Long newNo = trnIndex.getVouno() + 1;
         trnIndex.setVouno(newNo);
         tRNIndexRepo.save(trnIndex);
         cExpenses.setTrnno(prefixType+"-"+newNo);
         cExpenses.setDate(currentDate);
         cExpenses.setCdesc("InprocessQc Based Consumption");
         if(binstock.getAvgCost()!=null) {
         Double amount=binstock.getAvgCost()* dto.getQuantity();
         cExpenses.setAmount(amount);
         }
         cExpenses.setCtype("Inprocess_Qc");
         cExpenses.setConid(binstock.getRefId());
         cExpenses.setContype("Product");
         cExpenses.setLogid(dto.getId());
         cExpenses.setLogtable("Qc_Inprocess");
         cExpenses.setCby(user.getId());
         ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
         long epoch = istNow.toEpochSecond();
         cExpenses.setCendt((String.valueOf(epoch)));
         cExpenses.setBranch(binstock.getBranch());
         cExpenses.setDept(binstock.getDept());
         cExpenses.setCdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));	
          cExpensesRepository.save(cExpenses);     
		 	
	}

	public void updateCexpenseInprocessQcTransactionForReport(BinStock binstock, User user, QcInProcessDto dto, Double consumed) {
		
		 Date currentDate = dateUtil.getISTDate();
		 CExpenses cExpenses = new CExpenses();
		 String prefixType = "CONS";
         TRNIndex trnIndex = tRNIndexRepo.findByPrefix(prefixType)
                 .orElseGet(() -> {
                     TRNIndex t = new TRNIndex();
                     t.setPrefix(prefixType);
                     t.setVouno(0L);
                     return t;
                 });
         if(trnIndex.getVouno()==null)
         {
        	 trnIndex.setVouno(0L);
         }
         Long newNo = trnIndex.getVouno() + 1;
         trnIndex.setVouno(newNo);
         tRNIndexRepo.save(trnIndex);
         cExpenses.setTrnno(prefixType+"-"+newNo);
         cExpenses.setDate(currentDate);
         cExpenses.setCdesc("InprocessQc Based Consumption");
         if(binstock.getAvgCost()!=null) {
         Double amount=binstock.getAvgCost()* consumed;
         cExpenses.setAmount(amount);
         }
         cExpenses.setCtype("Inprocess_Qc");
         cExpenses.setConid(binstock.getRefId());
         cExpenses.setContype("Product");
         cExpenses.setLogid(dto.getId());
         cExpenses.setLogtable("Qc_Inprocess");
         cExpenses.setCby(user.getId());
         ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
         long epoch = istNow.toEpochSecond();
         cExpenses.setCendt((String.valueOf(epoch)));
         cExpenses.setBranch(binstock.getBranch());
         cExpenses.setDept(binstock.getDept());
         cExpenses.setCdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));	
          cExpensesRepository.save(cExpenses); 
	}

	public void updateCexpenseDespatchTransaction(BinStock bin, User loggedUser, Long orderId) {
		 Date currentDate = dateUtil.getISTDate();
		 CExpenses cExpenses = new CExpenses();
		 String prefixType = "CONS";
         TRNIndex trnIndex = tRNIndexRepo.findByPrefix(prefixType)
                 .orElseGet(() -> {
                     TRNIndex t = new TRNIndex();
                     t.setPrefix(prefixType);
                     t.setVouno(0L);
                     return t;
                 });
         if(trnIndex.getVouno()==null)
         {
        	 trnIndex.setVouno(0L);
         }
         Long newNo = trnIndex.getVouno() + 1;
         trnIndex.setVouno(newNo);
         tRNIndexRepo.save(trnIndex);
         cExpenses.setTrnno(prefixType+"-"+newNo);
         cExpenses.setDate(currentDate);
         cExpenses.setCdesc("Despatch Based Consumption");
         if(bin.getAvgCost()!=null) {
         Double amount=bin.getAvgCost()* bin.getStkQty();
         cExpenses.setAmount(amount);
         }
         cExpenses.setCtype("Despatch");
         cExpenses.setConid(bin.getRefId());
         cExpenses.setContype("Product");
         cExpenses.setLogid(orderId);
         cExpenses.setLogtable("Qc_Inprocess");
         cExpenses.setCby(loggedUser.getId());
         ZonedDateTime istNow = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
         long epoch = istNow.toEpochSecond();
         cExpenses.setCendt((String.valueOf(epoch)));
         cExpenses.setBranch(bin.getBranch());
         cExpenses.setDept(bin.getDept());
         cExpenses.setCdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));	
          cExpensesRepository.save(cExpenses); 
		
	}
	

}

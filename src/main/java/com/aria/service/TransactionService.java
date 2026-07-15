package com.aria.service;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.aria.dao.TransactionRepository;

import com.aria.model.BillInward;
import com.aria.model.Cluster;
import com.aria.model.MaterialRequest;
import com.aria.model.Wcshift;

@Service
public class TransactionService {
/*
	 @Autowired
	    private TransactionRepository transactionRepository;

	    public ResponseEntity<?> getIntraMaterialMovementResults(String dept, String branch, Date startTime, Date endTime) {
	        List<MaterialRequest> entities = transactionRepository.findIntraMaterialMovementResults(dept, branch, startTime, endTime);
	        return ResponseEntity.ok(entities);
	    }

	    public ResponseEntity<?> getInterMaterialMovementInwardResults(String dept, String branch, Date startTime, Date endTime) {
	        List<MaterialRequest> entities = transactionRepository.findInterMaterialMovementInwardResults(dept, branch, startTime, endTime);
	        return ResponseEntity.ok(entities);
	    }

	    public ResponseEntity<?> getInterMaterialMovementOutwardResults(String dept, String branch, Date startTime, Date endTime) {
	        List<MaterialRequest> entities = transactionRepository.findInterMaterialMovementOutwardResults(dept, branch, startTime, endTime);
	        return ResponseEntity.ok(entities);
	    }

	    public ResponseEntity<?> getStockInwardPartyPurchase1Results(Date trndate, String branch) {
	        List<BillInward> entities = transactionRepository.findStockInwardPartyPurchase1Results(trndate, branch);
	        return ResponseEntity.ok(entities);
	    }

	    public ResponseEntity<?> getStockInwardPartyPurchase2Results(Date trndate) {
	        List<BillInward> entities = transactionRepository.findStockInwardPartyPurchase2Results(trndate);
	        return ResponseEntity.ok(entities);
	    }

	    public ResponseEntity<?> getStockJournalProductionProcessResults(Date trndate, String pshiftid, String dept, String branch) {
	        List<Wcshift> entities = transactionRepository.findStockJournalProductionProcessResults(trndate, pshiftid, dept, branch);
	        return ResponseEntity.ok(entities);
	    }

	    public ResponseEntity<?> getDuringQCInProcessResults(String dept, String branch) {
	        List<Cluster> entities = transactionRepository.findDuringQCInProcessResults(dept, branch);
	        return ResponseEntity.ok(entities);
	    }

	    public ResponseEntity<?> getIncentiveResults(Date trndate, String cid) {
	        List<ProcessData> entities = transactionRepository.findIncentiveResults(trndate, cid);
	        return ResponseEntity.ok(entities);
	    }

		public ResponseEntity<?> getTransactionDetails(Long shiftId, java.util.Date shiftDate,String transactionType, String branch, String dept) {
			List<MaterialRequest> entities = transactionRepository.getTransactionDetailsQuery(shiftId, new java.sql.Date(shiftDate.getTime()), transactionType, branch, dept);
			return ResponseEntity.ok(entities);
		}

//		public ResponseEntity<?> getCWagesList(String transactionId, String transactionType, String refId) {
//			// TODO Auto-generated method stub
//			return null;
//		}
//
//		public ResponseEntity<?> getEmployeeList(String shiftId, String shiftDate, List<String> dailyWagesEmployees) {
//			// TODO Auto-generated method stub
//			return null;
//		}*/
}

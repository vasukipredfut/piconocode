package com.aria.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.PurchaseBillApproveRepository;
import com.aria.dto.PurchaseBillApproveDto;
import com.aria.dto.QualityInspectionDTO;
import com.aria.model.PurchaseBillApprove;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class PurchaseBillApproveService {

	@Autowired
	public PurchaseBillApproveRepository purchaseBillApproveRepository;

	@Autowired
	public QualityInspectionService qualityInspectionService;

//PurchaseBillApprove

	public ResponseEntity<?> savePurchaseBillApprove(PurchaseBillApproveDto purchaseClaimPremiumDto, User loggedUser) {
		PurchaseBillApprove purchaseClaimPremium = new PurchaseBillApprove(purchaseClaimPremiumDto);
		PurchaseBillApprove result = purchaseBillApproveRepository.save(purchaseClaimPremium);
		updateQualityInspection(purchaseClaimPremiumDto, loggedUser);
		return new ResponseEntity<Status>(new Status("200", "PurchaseBillApprove created successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> updatePurchaseBillApprove(PurchaseBillApproveDto billApproveDto, User loggedUser) {
		PurchaseBillApprove purchaseBillApprove = new PurchaseBillApprove(billApproveDto);
		PurchaseBillApprove result = purchaseBillApproveRepository.save(purchaseBillApprove);
		return new ResponseEntity<Status>(new Status("200", "PurchaseBillApprove updated successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> listAllPurchaseBillApprove(Pageable pageable) throws UsernameNotFoundException {
		Page<PurchaseBillApprove> purchaseClaimPremiums = purchaseBillApproveRepository.findAll(pageable);
		return new ResponseEntity<List<PurchaseBillApproveDto>>(purchaseClaimPremiums.toList().stream()
				.map(purchaseClaimPremium -> new PurchaseBillApproveDto(purchaseClaimPremium))
				.collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> getPurchaseBillApproveDetails(String id) throws UsernameNotFoundException {
		Optional<PurchaseBillApprove> optionalPurchaseBillApprove = purchaseBillApproveRepository
				.findById(Long.parseLong(id));
		PurchaseBillApproveDto purchaseBillApproveDto = new PurchaseBillApproveDto(optionalPurchaseBillApprove.get());
		return new ResponseEntity<PurchaseBillApproveDto>(purchaseBillApproveDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deletePurchaseBillApproveDetails(String id) throws UsernameNotFoundException {
		try {
			Optional<PurchaseBillApprove> optionalPurchaseBillApprove = purchaseBillApproveRepository
					.findById(Long.parseLong(id));
			if (optionalPurchaseBillApprove.isPresent()) {
				PurchaseBillApprove purchaseClaimPremium = optionalPurchaseBillApprove.get();
				purchaseBillApproveRepository.delete(purchaseClaimPremium);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

	public void updateQualityInspection(PurchaseBillApproveDto purchaseBillApproveDto, User user) {
		DateUtil dateUtil = new DateUtil();
		QualityInspectionDTO qualityInspection = new QualityInspectionDTO();
		qualityInspection.setBillDate(purchaseBillApproveDto.getBillDate());
		// qualityInspection.setInwardDate(dateUtil.getStringToLocalDateTime(purchaseBillApproveDto.g()));
		qualityInspection.setDate(dateUtil.getDateTOStringWk(dateUtil.getISTDate()));
		qualityInspection.setApproveById(purchaseBillApproveDto.getReceivedById());
		qualityInspection.setBranch(purchaseBillApproveDto.getBranch());
		qualityInspection.setCompany(purchaseBillApproveDto.getCompany());
		qualityInspection.setBillRefId(purchaseBillApproveDto.getBillRefId());
		qualityInspection.setVendorRefId(purchaseBillApproveDto.getVendorRefId());
		// qualityInspection.setDepartment(Integer.valueOf(purchaseBillApproveDto.getReceivedDepartment()));
		qualityInspection.setBillNumber(purchaseBillApproveDto.getBillNumber());
		qualityInspection.setBillAmount(Integer.valueOf(purchaseBillApproveDto.getBillAmount()));

		qualityInspection.setLoyaltyScore(Double.valueOf(purchaseBillApproveDto.getLoyaltyScore()));

		if (Objects.isNull(purchaseBillApproveDto.getLoyaltyScore())) {
			qualityInspection.setLoyaltyScore(0D);
		} else {
			qualityInspection.setDeliveryScore(Double.valueOf(purchaseBillApproveDto.getDeliveryScore()));
		}
		if (Objects.isNull(purchaseBillApproveDto.getDeliveryScore())) {
			qualityInspection.setDeliveryScore(0D);
		} else {
			qualityInspection.setDeliveryScore(Double.valueOf(purchaseBillApproveDto.getDeliveryScore()));
		}
		if (Objects.isNull(purchaseBillApproveDto.getQcScore())) {
			qualityInspection.setQcScore(0D);
		} else {
			qualityInspection.setQcScore(Double.valueOf(purchaseBillApproveDto.getQcScore()));
		}
		double totalscore = qualityInspection.getLoyaltyScore() + qualityInspection.getDeliveryScore()
				+ qualityInspection.getQcScore();

		qualityInspection.setTotalScore(totalscore);

		// saveQualityInspection changed to save List of values
		// qualityInspectionService.saveQualityInspection(qualityInspection, user);

	}
}

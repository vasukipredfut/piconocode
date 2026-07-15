package com.aria.service;


import java.util.List;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.aria.dao.KpiNameRepository;
import com.aria.dao.ProductMasterRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.KpinameDto;
import com.aria.dto.PageDataDto;
import com.aria.model.Kpiname;


import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class KpiService {

	@Autowired
	KpiNameRepository kpiNameRepository;

	@Autowired
	ProductMasterRepository prodMasterRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	

	public ResponseEntity<?> listAllKpiDetails(String modeOfEntry, User user, Pageable pageable) {
		Page<Kpiname> list = kpiNameRepository.findAllKpiDetails(modeOfEntry, pageable);
		if (!list.toList().isEmpty()) {
			List<KpinameDto> result = list.toList().stream().map(data -> {
				KpinameDto kpiDto = new KpinameDto(data);

				/*
				 * if (data.getProduct() != null) { Optional<ProductMaster> prodName =
				 * prodMasterRepository.findById(data.getProduct().getId());
				 * 
				 * if (prodName.isPresent()) {
				 * kpiDto.setProductName(prodName.get().getProductName()); } }
				 */
				return kpiDto;

			}).collect(Collectors.toList());
			PageDataDto<KpinameDto> pageData = new PageDataDto<>(result, pageable, list);
			return new ResponseEntity<PageDataDto<KpinameDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<KpinameDto>>(new PageDataDto<>(), HttpStatus.OK);
		}
	}

}

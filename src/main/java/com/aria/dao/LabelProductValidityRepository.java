package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.LabelProductValidity;

public interface LabelProductValidityRepository extends JpaRepository<LabelProductValidity, Long> {

	LabelProductValidity findByProductGroupIdAndLocationTypeAndLocationId(Long productGroupID, String location,
			Long locationId);

}

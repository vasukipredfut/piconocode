package com.aria.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.aria.model.ExistingPageDetails;

@Repository
public interface ExistingPageDetailsRepository extends JpaRepository<ExistingPageDetails,Long> {

	Optional<ExistingPageDetails> findByPageName(String pageName);

	@Query(value = "SELECT DISTINCT ep FROM ExistingPageDetails ep WHERE (:screenName is null or ep.pageName=:screenName)")
	List<ExistingPageDetails> findAllByPageName(String screenName);

}

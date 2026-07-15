package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aria.model.FilmProductValidity;

public interface FilmProductValidityRepository extends JpaRepository<FilmProductValidity, Long> {

	FilmProductValidity findByProductGroupIdAndLocationType(Long productGroupID, String location);

}

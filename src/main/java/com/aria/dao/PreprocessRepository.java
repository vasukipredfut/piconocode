package com.aria.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.model.Action;
import com.aria.model.ActionConfiguration;
import com.aria.model.Preprocess;

@Repository
public interface PreprocessRepository extends JpaRepository<Preprocess,Long> {


}

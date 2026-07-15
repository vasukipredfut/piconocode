package com.aria.dao;

import com.aria.model.DetailedReasonEntity;
import com.aria.model.OffReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetailedReasonRepository extends JpaRepository<DetailedReasonEntity,Long> {

    List<DetailedReasonEntity> findByOffreason(Long offreason);
}

package com.aria.dao;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.aria.dto.IAssignDutyFilterListResponse;
import com.aria.model.DutyTransfer;

@Repository
public interface DutyTransferRepository extends JpaRepository<DutyTransfer, Long> {

}

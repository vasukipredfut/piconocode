package com.aria.dao;

import com.aria.model.CommonReportMenuTitle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommonReportMenuTitleRepository extends JpaRepository<CommonReportMenuTitle, Long> {


}

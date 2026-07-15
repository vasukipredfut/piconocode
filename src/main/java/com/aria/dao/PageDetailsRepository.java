package com.aria.dao;

import com.aria.model.PageDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageDetailsRepository extends JpaRepository<PageDetails,Long> {


}

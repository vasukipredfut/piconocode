package com.aria.dao;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.MaintainanceDetails;

@Repository
public interface MaintainanceDetailsRepository extends JpaRepository<MaintainanceDetails,Long> {


}
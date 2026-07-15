
package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.AdminInspection;

@Repository
public interface AdminInspectionRepository extends JpaRepository<AdminInspection, Long> {

}

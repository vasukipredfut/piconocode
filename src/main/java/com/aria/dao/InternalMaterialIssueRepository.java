
package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.InternalMaterialIssue;

@Repository
public interface InternalMaterialIssueRepository extends JpaRepository<InternalMaterialIssue, Long> {

}

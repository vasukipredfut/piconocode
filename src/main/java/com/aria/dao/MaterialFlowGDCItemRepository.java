package com.aria.dao;

import com.aria.model.MaterialFlowGDCItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialFlowGDCItemRepository extends JpaRepository<MaterialFlowGDCItem,Long> {

}

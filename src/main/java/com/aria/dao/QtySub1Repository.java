package com.aria.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.QtySub1;

@Repository
public interface QtySub1Repository extends JpaRepository<QtySub1, Long> {

	List<QtySub1> findByParent(Long itemId);

}

package com.aria.dao;

import com.aria.model.CategoryConstants;
import com.aria.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.persistence.Tuple;
import java.util.List;

@Repository
public interface CategoryConstantsRepository extends JpaRepository<CategoryConstants, Long> {


}

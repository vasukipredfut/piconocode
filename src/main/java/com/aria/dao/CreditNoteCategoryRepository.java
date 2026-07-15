package com.aria.dao;

import com.aria.model.CreditNoteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditNoteCategoryRepository extends JpaRepository<CreditNoteCategory, Long> {


}

package com.aria.dao;

import com.aria.model.PageField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageFieldRepository extends JpaRepository<PageField,Long> {


}

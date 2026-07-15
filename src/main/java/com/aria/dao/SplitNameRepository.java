package com.aria.dao;

import com.aria.model.SplitName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SplitNameRepository extends JpaRepository<SplitName, Long> {


}

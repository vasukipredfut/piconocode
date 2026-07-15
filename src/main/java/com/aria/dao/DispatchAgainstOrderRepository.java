package com.aria.dao;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.DispatchAgainstOrder;

@Repository
public interface DispatchAgainstOrderRepository extends JpaRepository<DispatchAgainstOrder, Long> {


}

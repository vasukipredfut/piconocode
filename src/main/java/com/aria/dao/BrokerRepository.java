package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.Broker;

@Repository
public interface BrokerRepository extends JpaRepository<Broker, Long> {



}

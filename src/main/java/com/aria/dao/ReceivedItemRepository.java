
package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.ReceivedItems;



@Repository
public interface ReceivedItemRepository  extends JpaRepository<ReceivedItems, Long> {

}
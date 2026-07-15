
package com.aria.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.aria.model.OutputProducts;

@Repository
public interface OutputProductsRepository extends JpaRepository<OutputProducts, Long> {

}

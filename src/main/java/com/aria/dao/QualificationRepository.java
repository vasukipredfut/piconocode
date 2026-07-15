package com.aria.dao;

import com.aria.model.Qualification;
import com.aria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QualificationRepository extends JpaRepository<Qualification, Long> {

    @Query(value = "SELECT *  FROM qualification u WHERE u.name =:name", nativeQuery = true)
    Optional<Qualification> findByName(@Param("name") String name);
}

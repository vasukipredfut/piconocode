package com.aria.dao;

import com.aria.model.Award;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AwardRepository extends JpaRepository<Award, Long> {


    @Query(value = "SELECT DISTINCT user_id FROM award where user_id IS NOT NULL", nativeQuery = true)
    List<Long> findAllByUser();

    @Query(value = "SELECT * FROM award WHERE user_id in (:userId) ", nativeQuery = true)
    List<Award> findAllByUser(@Param("userId") List<Long> userId);

    @Query(value = "SELECT * FROM award a where a.award_category =:category and a.user_id =:userId", nativeQuery = true)
    List<Award> findByCategoryUser(@Param("category") String category, @Param("userId") Long userId);

    @Query(value = "SELECT * FROM award a where a.user_id =:userId AND a.award_date BETWEEN :date1 and :date2", nativeQuery = true)
    List<Award> findByUser(@Param("userId") Long userId , @Param("date1") Date date1, @Param("date2") Date date2);


    @Query(value = "SELECT * FROM award a where a.user_id =:userId ", nativeQuery = true)
    List<Award> findByUser(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM award a where a.award_category =:category and a.user_id =:userId and a.award_date =:date1 ", nativeQuery = true)
    List<Award> findByCategoryUser(@Param("category") String category, @Param("userId") Long userId, @Param("date1") Date date1);


    @Query(value = "SELECT * FROM award a where a.user_id =:userId LIMIT  1 ", nativeQuery = true)
    Award findUser(@Param("userId") Long userId);

}

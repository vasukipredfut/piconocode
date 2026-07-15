package com.aria.dao;

import java.util.List;
import java.util.Optional;

import com.aria.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.aria.model.RefreshToken;


@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {


    Optional<RefreshToken> findByToken(String token);

    /**
     * Remove a refresh token row by its token value.  Returns the number of
     * rows deleted, which may be zero if the token had already gone.
     * Using a @Modifying JPQL query avoids loading the entity and prevents
     * optimistic locking exceptions when another thread has already deleted it.
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("delete from RefreshToken r where r.token = :token")
    int deleteByToken(@org.springframework.data.repository.query.Param("token") String token);

    @Query(value = "SELECT *  FROM refresh_token r WHERE r.user_id =:userId", nativeQuery = true)
    List<RefreshToken> findByUserId(@Param("userId") Long userId);

    /**
     * Remove all refresh tokens belonging to the supplied user id.  Using a
     * modifying query avoids loading the entities and also protects against
     * concurrent delete/expire operations which might otherwise lead to
     * unexpected row count exceptions.
     */
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("delete from RefreshToken r where r.user.id = :userId")
    int deleteByUserId(@org.springframework.data.repository.query.Param("userId") Long userId);


}

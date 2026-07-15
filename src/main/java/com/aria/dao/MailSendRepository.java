package com.aria.dao;

import com.aria.model.MailSendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailSendRepository extends JpaRepository<MailSendEntity,Long> {

    List<MailSendEntity> findByStatus(String status);

}

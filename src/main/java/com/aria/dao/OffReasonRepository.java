package com.aria.dao;

import com.aria.model.MailSendEntity;
import com.aria.model.OffReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffReasonRepository extends JpaRepository<OffReasonEntity,Long> {

}

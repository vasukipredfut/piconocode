package com.aria.model;

import com.aria.dto.AttendanceDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "mailsend")
public class MailSendEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "mailid")
	private String mailid;

	@Column(name = "subject")
	private String subject;

	@Column(name = "msg")
	private String msg;

	@Column(name = "status")
	private String status;

}

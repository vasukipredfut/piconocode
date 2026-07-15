package com.aria.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.MessVoucherDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mess_voucher")
public class MessVoucher {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long voucherno;

    private LocalDate voucherdate;

    private Long month;

    private Long year;

    private Long branch;

    private Long cid;

    private Long activeflag;

    private Integer createby;

    private LocalDateTime createon;

    private String createstatus;

    private Integer verifyby;

    private LocalDateTime verifyon;

    private String verifystatus;

    private Integer approveby;

    private LocalDateTime approveon;

    private String approvestatus;
    
    private String status;

	public MessVoucher(MessVoucherDto request) {
		super();
		String dateStr = request.getDate();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate date = LocalDate.parse(dateStr, formatter);

		this.voucherdate = date;
		this.month = request.getMonth();
		this.year = request.getYear();
		this.branch = request.getBranch();
		this.cid = request.getCompany();
	}
    
	
}

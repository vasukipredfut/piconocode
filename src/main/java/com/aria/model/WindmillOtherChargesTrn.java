package com.aria.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.WindmillOtherChargesTrnDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "windmill_othercharges_trn")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindmillOtherChargesTrn {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "windmillid")
	    private Long windmillId;

	    @Column(name = "windmillotherchargesid")
	    private Long windmillOtherChargesId;

	    @Column(name = "trndate")
	    private LocalDate trnDate;

	    @Column
	    private Double amount;

	    @Column(name = "udpatedby")
	    private Long updatedBy;

	    @Column
	    private Integer month;

	    @Column
	    private int year;

		public WindmillOtherChargesTrn(WindmillOtherChargesTrnDto request) {
			super();
			this.id = request.getId();
			this.windmillId = request.getWindmillId();
			this.windmillOtherChargesId = request.getWindmillOtherChargesId();
			this.trnDate = request.getTrnDate();
			this.amount = request.getAmount();
			this.updatedBy = request.getUpdatedBy();
			this.month = request.getMonth();
			this.year = request.getYear();
		}
	    
	    
	    
	
}

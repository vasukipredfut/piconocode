package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.WindmillOtherChargesDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "windmillothercharges")
@Getter
@Setter
@NoArgsConstructor
public class WindmillOtherCharges {

		@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "windmillid")
	    private Long windmillId;

	    @Column(name = "type", length = 255)
	    private String type;

	    @Column(name = "name", length = 200)
	    private String name;

	    @Column(name = "amount")
	    private Double amount;

	    @Column(name = "status", length = 1)
	    private String status;

	    @Column(name = "updatedby")
	    private Long updatedBy;

	    // "timestamp" is reserved keyword in PostgreSQL
	    @Column(name = "timestamp")
	    private LocalDateTime timestamp;

	    @Column(name = "displayorder")
	    private Integer displayOrder;
	    
	    public WindmillOtherCharges(WindmillOtherChargesDto request) {
			super();
			this.id = request.getId();
			this.windmillId = request.getWindmillId();
			this.type = request.getType();
			this.name = request.getName();
			this.amount = request.getAmount();
			this.status = request.getStatus();
			this.updatedBy = request.getUpdatedBy();
			this.timestamp = request.getTimestamp();
			this.displayOrder = request.getDisplayOrder();
		}
	
}

package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.WindmillAttributesDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "windmillattributes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindmillAttributes {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "updatedby")
    private Long updatedBy;

    // timestamp is a reserved keyword → quoted in DB
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "windmillid")
    private Long windmillId;

    @Column(name = "formulaflag")
    private Integer formulaFlag;

    @Column(name = "formula", length = 100)
    private String formula;

    @Column(name = "orderid")
    private Long orderId;

    public WindmillAttributes(WindmillAttributesDto request) {
		super();
		this.id = request.getId();
		this.name = request.getName();
		this.status = request.getStatus();
		this.updatedBy = request.getUpdatedBy();
		this.timestamp = request.getTimestamp();
		this.windmillId = request.getWindmillId();
		this.formulaFlag = request.getFormulaFlag();
		this.formula = request.getFormula();
		this.orderId = request.getOrderId();
	}

}

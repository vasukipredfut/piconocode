package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.WindmillDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "windmill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Windmill {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String name;

    @Column(length = 255)
    private String type;

    @Column(length = 100)
    private String description;

    @Column(length = 1)
    private String status;

    @Column(name = "td_loss")
    private Double tDLoss;

    @Column
    private Double mf;

    @Column(name = "otherchargespayablehtserviceid")
    private Integer otherChargesPayableHtServiceId;

    @Column
    private Double capacity;

	public Windmill(WindmillDto request) {
		super();
		this.id = request.getId();
		this.name = request.getName();
		this.type = request.getType();
		this.description = request.getDescription();
		this.status = request.getStatus();
		this.tDLoss = request.getTDLoss();
		this.mf = request.getMf();
		this.otherChargesPayableHtServiceId = request.getOtherChargesPayableHtServiceId();
		this.capacity = request.getCapacity();
	}
    
    
}

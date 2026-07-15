package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.WindmillTimmingDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "windmilltimming")
@Getter
@Setter
@NoArgsConstructor
public class WindmillTimming {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 300)
    private String name;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "updatedby")
    private Long updatedBy;

    // "timestamp" is a reserved keyword in PostgreSQL
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

	public WindmillTimming(WindmillTimmingDto request) {
		super();
		this.id = request.getId();
		this.name = request.getName();
		this.description = request.getDescription();
		this.status = request.getStatus();
		this.updatedBy = request.getUpdatedBy();
		this.timestamp = request.getTimestamp();
	}
    
    

}

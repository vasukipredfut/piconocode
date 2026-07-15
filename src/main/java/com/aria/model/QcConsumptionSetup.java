package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.QcConsumptionSetupDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "qc_consumption_setup")
public class QcConsumptionSetup {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column
	    private Long kpiId;

	    @Column
	    private Long productId;

	    @Column
	    private Long dept;

	    @Column
	    private Long branch;

	    @Column(name = "createdBy",updatable = false)
	    private Long createdBy;

	    @Column(updatable = false)
	    private LocalDateTime createdDate;

		public QcConsumptionSetup(QcConsumptionSetupDto qcConsumptionSetupDto) {
			super();
			this.id = qcConsumptionSetupDto.getId();
			this.kpiId = qcConsumptionSetupDto.getKpiId();
			this.productId = qcConsumptionSetupDto.getProductId();
			this.dept = qcConsumptionSetupDto.getDept();
			this.branch = qcConsumptionSetupDto.getBranch();
			this.createdBy = qcConsumptionSetupDto.getCreatedBy();
			this.createdDate = qcConsumptionSetupDto.getCreatedDate();
		}
	    
}

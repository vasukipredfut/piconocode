package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.aria.model.QcConsumptionSetup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QcConsumptionSetupDto {
	    private Long id;
	    private Long kpiId;
	    private Long productId;
	    private Long dept;
	    private Long branch;
	    private Long createdBy;
	    private LocalDateTime createdDate;
	    List<QcConsumptionMapDto> consumables;
		public QcConsumptionSetupDto(QcConsumptionSetup qConsumption) {
			super();
			this.id = qConsumption.getId();
			this.kpiId = qConsumption.getKpiId();
			this.productId = qConsumption.getProductId();
			this.dept = qConsumption.getDept();
			this.branch = qConsumption.getBranch();
			this.createdBy = qConsumption.getCreatedBy();
			this.createdDate = qConsumption.getCreatedDate();
		}

}

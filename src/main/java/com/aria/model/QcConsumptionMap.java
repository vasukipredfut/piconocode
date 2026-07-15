package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.QcConsumptionMapDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "qc_consumption_map")
public class QcConsumptionMap {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column
	    private Long configId;

	    @Column
	    private Long conProduct;

	    @Column(name="required_qty")
	    private Double requiredQty;
	    
	    @Column
	    private Double lot;

		public QcConsumptionMap(QcConsumptionMapDto qcConsumptionMap) {
			this.id = qcConsumptionMap.getId();
			this.configId = qcConsumptionMap.getConfigId();
			this.conProduct = qcConsumptionMap.getConProduct();
			this.requiredQty = qcConsumptionMap.getRequiredQty();	
			this.lot=qcConsumptionMap.getLot();
		}
	    
	    
	    
	    
}

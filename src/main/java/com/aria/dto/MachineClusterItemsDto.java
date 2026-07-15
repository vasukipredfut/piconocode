package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.Branch;
import com.aria.model.MachineClassification;
import com.aria.model.MachineCluster;
import com.aria.model.MachineClusterItems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineClusterItemsDto {

    private Long id;
    
    private Long clusterId;
    
    private Long auxMachineId;

    private String auxMachineName;
    
    private String isoNo;
    
    private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

    public MachineClusterItemsDto(MachineClusterItems field) {
        this.id = field.getId();
       if(field.getMachine()!=null) {
    	   this.auxMachineId = field.getMachine().getId();
    	   this.auxMachineName = field.getMachine().getName();
    	   this.isoNo = field.getMachine().getIso_no();
       }
       this.clusterId = field.getClusterId();
        this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();	
    }
}

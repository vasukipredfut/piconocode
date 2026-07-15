package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.Branch;
import com.aria.model.MachineClassification;
import com.aria.model.MachineCluster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineClusterDto {

    private Long id;
    
    private Long machineId;

    private String machineName;
    
    private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<MachineClusterItemsDto> machineClusterItems;

    private String isoNumber;

    public MachineClusterDto(MachineCluster field) {
        this.id = field.getId();
        if(field.getMachine()!=null) {
        	this.machineId = field.getMachine().getId();
        	this.machineName = field.getMachine().getName();
            this.isoNumber=field.getMachine().getIso_no();
        }
        
        this.machineClusterItems = field.getMachineClusterItems() != null ? field.getMachineClusterItems().stream()
				.map(obj -> new MachineClusterItemsDto(obj)).collect(Collectors.toList()) : null;

        this.createdBy = field.getCreatedBy();
		this.created_date = field.getCreated_date();
		this.updatedBy = field.getUpdatedBy();
		this.updated_date = field.getUpdated_date();

    }
}

package com.aria.dto;

import com.aria.model.Transport;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransportResponseDto {
	private Long id;
	private Long vehicleId;
    private String vehicleName;
    private String vehicleNo;
    private String capacity;
    private String tankCapacity;
    private String vehicleType;
    private String fuel;
    private String currentStatus;
    private Long driverId;
    private String driverName;
    private Integer maximumMilage;
    
    public TransportResponseDto(Transport transport,String status) {
    	
    	this.vehicleId=transport.getVid().getId();
    	this.vehicleName=transport.getVid().getName();
    	this.vehicleNo = extractVehicleNumber(transport.getVid().getName());
    	this.capacity=transport.getCapacity();
    	this.tankCapacity=transport.getTcapacity();
    	this.vehicleType=transport.getCate();
    	this.fuel=transport.getFtype();
    	this.currentStatus=status;
        this.driverId=transport.getDriver_id().getId();
        this.driverName=transport.getDriver_id().getUsername();
        this.maximumMilage=transport.getMmx();
        this.id=transport.getId();
    }

	private String extractVehicleNumber(String name) {
		String[] parts = vehicleName.split("\\s+");
        if (parts.length > 1) {
        	return parts[1];  
        } else {
            return "";
        }
	}
}

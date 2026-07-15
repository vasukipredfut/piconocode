package com.aria.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WindmillOtherChargesDto {
	
	private Long id;
    private Long windmillId;
    private String type;
    private String name;
    private Double amount;
    private String status;
    private Long updatedBy;
    private LocalDateTime timestamp;
    private Integer displayOrder;

}

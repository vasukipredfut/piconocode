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
public class WindmillAttributesDto {
	
	private Long id;
    private String name;
    private String status;
    private Long updatedBy;
    private LocalDateTime timestamp;
    private Long windmillId;
    private Integer formulaFlag;
    private String formula;
    private Long orderId;

}

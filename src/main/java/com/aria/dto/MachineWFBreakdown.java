package com.aria.dto;

import com.aria.model.MachineWFBreakdownReason;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MachineWFBreakdown {
	private Long machineWorkFlowId;
	private String productionShift;
	private String timeSlot;
	private String breakDownTime;
	private String breakDownDate;
	List<MachineWFBreakdownReasonDto>  reasons;
}

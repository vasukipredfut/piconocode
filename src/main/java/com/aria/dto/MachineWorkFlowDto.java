package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.Column;

import com.aria.dao.MachineWFBreakdownReasonRepository;
import com.aria.dao.PreventiveMaintainceAttributeRepository;
import com.aria.dao.UserRepository;
import com.aria.model.*;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineWorkFlowDto {

    private Long id;


    private int logId;

    private Long machineId;

    private String machineName;

    private String powerSource;

    private String reason;

    private String timeSlab;

    private String productionShift;

    private String cStatus;

    private String powerOnTime;

    private String powerOffTime;

    private String powerOnDate;

    private String powerOffDate;

    private String meterClosingReading;

    private String powerOffReason;
    private Long powerOffReasonId;

    private String detailedReason;
    private Long detailedReasonId;

    private Long operatedBy;

    private String operatedName;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private Long taskCategory;

    private String taskCategoryName;

    private Long taskGroup;

    private String taskGroupName;

    private boolean attributesPresent;

    private boolean breakdownPresent;

    List<PanelDto> panels;
    List<MachinePanelReadingMappingDto> panelsReadingMapping;

    public MachineWorkFlowDto(Machine field, DateUtil dateUtil, PreventiveMaintainceAttributeRepository preventiveMaintainceAttributeRepository, MachineWFBreakdownReasonRepository breakdownReasonRepository, UserRepository userRepository) {
        if (field != null) {
            this.machineId = field.getId();
            this.machineName = field.getName();
            if(field.getEb()!=null){
                this.powerSource = field.getEb().getServiceName();
            }
        }
        MachineWorkFlow machineWorkFlow = field.getMachineWorkFlow();
        if (machineWorkFlow != null) {
            System.out.println("check ALG");
            this.id = machineWorkFlow.getId();
            this.logId = machineWorkFlow.getLogId();
            this.reason = machineWorkFlow.getReason();
            this.cStatus = machineWorkFlow.getCStatus();
            this.timeSlab = machineWorkFlow.getTimeSlab();
            this.productionShift = machineWorkFlow.getProductionShift();
            this.operatedBy = machineWorkFlow.getOperatedBy();
            Optional<User> userOp=userRepository.findById(this.operatedBy);
            if(userOp.isPresent()){
                this.operatedName=userOp.get().getEmployeename();
            }

            this.powerOffReason = machineWorkFlow.getPowerOffReason();
            this.powerOffReasonId = machineWorkFlow.getPowerOffReasonId();
            this.powerOnTime = machineWorkFlow.getPowerOnTime();
            this.powerOffTime = machineWorkFlow.getPowerOffTime();
            this.meterClosingReading = machineWorkFlow.getMeterClosingReading();
            this.detailedReason = machineWorkFlow.getDetailedReason();
            this.detailedReasonId = machineWorkFlow.getDetailedReasonId();
            this.powerOnDate=dateUtil.getDateToStringDDMMYYYY(machineWorkFlow.getPowerOnDate());
            this.powerOffDate=dateUtil.getDateToStringDDMMYYYY(machineWorkFlow.getPowerOffDate());
            this.createdBy = machineWorkFlow.getCreatedBy();
            this.created_date = machineWorkFlow.getCreated_date();
            this.updatedBy = machineWorkFlow.getUpdatedBy();
            this.updated_date = machineWorkFlow.getUpdated_date();

            if(machineWorkFlow.getCStatus()!=null && machineWorkFlow.getCStatus().equalsIgnoreCase("OFF")) {

                List<MachineWFBreakdownReason> list = breakdownReasonRepository.findByMachineWorkFlowId(machineWorkFlow.getId());
                if (list != null && !list.isEmpty()) {
                    this.breakdownPresent = true;
                }

                List<PreventiveMaintainceAttributes> l = preventiveMaintainceAttributeRepository.findByMcId(field.getId());
                if (l != null && !l.isEmpty()) {
                    this.attributesPresent = true;
                }
            }
        }else {
            System.out.println("check ALG RA");
        }
    }
}

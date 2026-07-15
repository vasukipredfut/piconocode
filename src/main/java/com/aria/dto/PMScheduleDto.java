package com.aria.dto;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import com.aria.model.PMSchedule;
import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PMScheduleDto {

	private Long id;
	private String name;
	private Long scheduleId;
	private String scheduleName;
	private Long taskCategoryId;
	private String taskCategoryName;
	private Long taskGroupId;
	private String taskGroupName;
	private Long machineId;
	private String machineName;
	private String externalVendor;
	private Long remain;
	private Long panelId;
	private String panelName;
	private String lastCompleteDate;
	private String begintime;
	private Long clusres;
	private Long clusval;
	private Long remind;
	private Long ontime;
	private Long delaymf;
	private Date countdt;
	private String remaindMe;
	private String ppublic;
	private String author;
	private Long mailid;
	private Long unlockres;
	private Long responsibility;
	private Long responsibility1;
	private Long responsibility2;
	private Long validator;
	private Long approver;
	private String describe;
	private String option;
	private Long frequency;
	private String audit;
	private String active;
	private String pfinal;
	private Long tier1Id;
	private String tier1Name;
	private Long tier2Id;
	private String tier2Name;
	private Long tier3Id;
	private String tier3Name;
	private Long tier4Id;
	private String tier4Name;
	private String purposeBenefit;
	private String workSafetyInstructions;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime created_date;
	private LocalDateTime updated_date;
	private String schedule_date;
	private String status;



	public PMScheduleDto(PMSchedule field) {
		
		if (field.getPanel() != null) {
			this.panelId = field.getPanel().getId();
			this.panelName = field.getPanel().getPanelName();
		}

		if (field.getTier1() != null) {
			this.tier1Id = field.getTier1();
			//this.tier1Name = field.getTier1().getEmployeename();
		}
		
		if (field.getTier2() != null) {
			this.tier2Id = field.getTier2();
			//this.tier2Name = field.getTier2().getEmployeename();
		}
		
		if (field.getTier3() != null) {
			this.tier3Id = field.getTier3();
			//this.tier3Name = field.getTier3().getEmployeename();
		}
		
		if (field.getTier4() != null) {
			this.tier4Id = field.getTier4();
			//this.tier4Name = field.getTier4().getEmployeename();
		}

		if (field.getMachine() != null) {
			this.machineId = field.getMachine().getId();
			this.machineName = field.getMachine().getName();
		}

		if (field.getTaskCategory() != null) {
			this.taskCategoryId = field.getTaskCategory().getId();
			this.taskCategoryName = field.getTaskCategory().getTaskCategoryName();
		}
		
		if (field.getTaskGroup() != null) {
			this.taskGroupId = field.getTaskGroup().getId();
			this.taskGroupName = field.getTaskGroup().getTaskGroupName();
		}
			this.scheduleName = field.getName();
			this.scheduleId = field.getScheduleid();

		this.id = field.getId();
		this.name = field.getName();
		this.option = field.getOption();
		this.frequency = field.getFrequency();
		
		
		this.remain = field.getRemain();
		this.remind = field.getRemind();
		this.remaindMe = field.getRemaindMe();
		this.externalVendor = field.getExternalVendor();
		this.responsibility = field.getResponsibility();
		this.mailid = field.getMailid();
		this.responsibility1 = field.getResponsibility1();
		this.responsibility2 = field.getResponsibility2();
		this.validator = field.getValidator();
		this.approver = field.getApprover();
		this.describe = field.getDescribe();

		this.author = field.getAuthor();
		this.ppublic = field.getPpublic();
		this.ontime = field.getOntime();
		
		this.delaymf = field.getDelaymf();
		this.unlockres = field.getUnlockres();
		this.begintime = field.getBegintime();
		
		this.clusres = field.getClusres();
		this.clusval = field.getClusval();
		this.audit = field.getAudit();
		this.pfinal = field.getPfinal();
		this.active = field.getActive();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		if(field.getLastCompletedDate()!=null) {
			this.lastCompleteDate=sdf.format(field.getLastCompletedDate());
		}
		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreated_date();
		this.updated_date = field.getUpdated_date();

		if(field.getSchedule_date()!=null) {
			this.lastCompleteDate=sdf.format(field.getSchedule_date());
		}

	}


	public PMScheduleDto(PMScheduleList pmSchedule) {
		DateUtil dateUtil = new DateUtil();
		this.id = pmSchedule.getId();
		this.name = pmSchedule.getScheduleName();
		this.scheduleId = pmSchedule.getScheduleId();
		this.scheduleName = pmSchedule.getScheduleName();
		this.taskCategoryId = pmSchedule.getTaskCategoryId();
		this.taskCategoryName = pmSchedule.getTaskCategoryName();
		this.taskGroupId = pmSchedule.getTaskGroupId();
		this.taskGroupName = pmSchedule.getTaskGroupName();
		this.machineId = pmSchedule.getMachineId();
		this.machineName = pmSchedule.getMachineName();
		this.panelId = pmSchedule.getPanelId();
		this.panelName = pmSchedule.getPanelName();
		this.lastCompleteDate = dateUtil.getDateTOStringWk(pmSchedule.getLastCompleteDate());
		this.status = pmSchedule.getStatus();
		this.schedule_date = dateUtil.getDateTOStringWk(pmSchedule.getScheduleDate());
	}
}

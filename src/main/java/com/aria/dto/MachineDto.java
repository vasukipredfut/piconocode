package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.Machine;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MachineDto {

	private String name;

	private String iso_no;

	private String description;

	private String dateOfInstallation;

	private String category;

	private String vendor;

	private String purchaseAccName;

	private Long purchaseAccId;

	private String purchaseFromName;

	private Long purchaseFromId;

	private String picture;
	
//	private String pictureBase64;

	private String pictureName;

	private int pictureSize;

	private String pictureType;

	private Long pictureWidth;

	private Long pictureHeight;

	private Long cost;

	private String accGroup;

	private String purpose;

	private String tools;

	private String consumables;

	private String workinst;

	private String safetyinst;

	private String ice;

	private String slno;

	private String production;

	private String utilisation;

	private String main;

	private Long responsibility;

	private String classify;

	private int cluster;

	private int company;

	private int tcid;

	private String mfinal;

	private String dispName;

	private String cStatus;

	private String test;

	private String selectedBranchId;

	private String place;

	private String godownName;

	private Long godownId;

	private String make;

	private String capacity;

	private String size;

	private Long classifyId;

	private String hsnRefId;

	private float ebMF;

	private int costGroupId;

	private Long id;

	private Long branchId;

	private String branchName;

	private Long deptId;

	private String departmentName;

	private String status;

	private Long taskCategoryId;

	private String taskCategoryName;

	private Long gensetId;

	private String gensetName;


	private String panel;

	private Long ebId;

	private String ebName;

	private Long depreciationId;

	private String depreciationName;

	private Long resp1Id;

	private String resp1Name;

	private Long resp2Id;

	private String resp2Name;

	private Long resp3Id;

	private String resp3Name;

	private String aboutMachine;

	private String isMainMachine;

	private String emergency;
	
	private String locationpath;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public MachineDto(Machine field) {
		this.id = field.getId();
		this.name = field.getName();
		this.iso_no = field.getIso_no();
		this.description = field.getDescription();
		this.vendor = field.getVendor();
		
//		this.picture = field.getPicture();
		this.pictureName = field.getPictureName();
		this.pictureType = field.getPictureType();
		this.pictureWidth = field.getPictureWidth();
		this.pictureHeight = field.getPictureHeight();
		this.pictureSize = field.getPictureSize();

		this.cost = field.getCost();
		this.accGroup = field.getAccGroup();
		this.purpose = field.getPurpose();
		this.tools = field.getTools();
		this.consumables = field.getConsumables();
		this.workinst = field.getWorkinst();
		this.safetyinst = field.getSafetyinst();
		this.ice = field.getIce();
		this.slno = field.getSlno();
		this.production = field.getProduction();
		this.utilisation = field.getUtilisation();
//		this.main = field.getMain();
		this.responsibility = field.getResponsibility();
//		this.classify = field.getClassify();
		this.cluster = field.getCluster();
		this.company = field.getCompany();
		this.tcid = field.getTcid();
		this.mfinal = field.getMfinal();
		this.dispName = field.getDispName();
		this.cStatus = field.getCStatus();
		this.test = field.getTest();
		this.selectedBranchId = field.getBranchId();
		this.place = field.getPlace();
		this.godownName = field.getPlace();
		this.godownId = field.getGodown_id();
		this.make = field.getMake();
		this.capacity = field.getCapacity();
		this.size = field.getSize();

		
		if (field.getBranch() != null) {
			this.branchId = field.getBranch().getId();
			this.branchName = field.getBranch().getBranchName();
		}
		if (field.getDepartmentDetails() != null) {
			this.deptId = field.getDepartmentDetails().getId();
			this.departmentName = field.getDepartmentDetails().getName();
		}
		if (field.getClassifyDet() != null) {
			this.classifyId = field.getClassifyDet().getId();
			this.classify = field.getClassifyDet().getName();
		}
		if (field.getTaskCategory() != null) {
			this.taskCategoryId = field.getTaskCategory().getId();
			this.taskCategoryName = field.getTaskCategory().getTaskCategoryName();
		}

		this.panel=field.getPanel();

		if (field.getEb() != null) {
			this.ebId = field.getEb().getId();
			this.ebName = field.getEb().getServiceName();
		}
		if (field.getGenerator() != null) {
			this.gensetId = field.getGenerator().getId();
			this.gensetName = field.getGenerator().getName();
		}
		if (field.getDepreciation() != null) {
			this.depreciationId = field.getDepreciation().getId();
			this.depreciationName = field.getDepreciation().getName();
		}
		if (field.getResp1() != null) {
			this.resp1Id = field.getResp1().getId();
			this.resp1Name = field.getResp1().getEmployeename();
		}
		if (field.getResp2() != null) {
			this.resp2Id = field.getResp2().getId();
			this.resp2Name = field.getResp2().getEmployeename();
		}
		if (field.getResp3() != null) {
			this.resp3Id = field.getResp3().getId();
			this.resp3Name = field.getResp3().getEmployeename();
		}
		if (field.getPurchaseOnAcc() != null) {
			this.purchaseAccId = field.getPurchaseOnAcc().getId();
			this.purchaseAccName = field.getPurchaseOnAcc().getName();
		}
		if (field.getPurchaseFrom() != null) {
			this.purchaseFromId = field.getPurchaseFrom().getId();
			this.purchaseFromName = field.getPurchaseFrom().getName();
		}
		
		this.aboutMachine = field.getAboutMachine();
		this.isMainMachine = field.getIsMainMachine();
		this.emergency = field.getEmergency();
		this.hsnRefId = field.getHsnRefId();
		this.ebMF = field.getEbMF();
		this.costGroupId = field.getCostGroupId();
		this.status = field.getStatus();
		this.locationpath = field.getLocationpath();
		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreated_date();
		this.updated_date = field.getUpdated_date();

	}
}


package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.*;

import com.aria.dto.MachineDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "machine")
public class Machine {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String name;

	@Column
	private String iso_no;

	@Column
	private String description;

	@Column(name = "doi")
	private Date dateOfInstallation;

	@Column
	private String vendor;

	@Column
	private String purchaseac;

//	@Column
//	private String picture;

	@Column(name = "picturename")
	private String pictureName;
	
	@Column(name = "picturesize")
	private int pictureSize;

	@Column(name = "picturetype")
	private String pictureType;
	
	@Column(name = "picturewidth")
	private Long pictureWidth;
	
	@Column(name = "pictureheight")
	private Long pictureHeight;

	@Column
	private Long cost;
	
	@Column(name="acc_group")
	private String accGroup;
	
	@Column
	private String purpose;
	
	@Column
	private String tools;
	
	@Column
	private String consumables;
	
	@Column
	private String workinst;
	
	@Column
	private String safetyinst;
	
	@Column
	private String ice;
	
	@Column
	private String slno;
	
	@Column
	private String production;
	
	@Column
	private String utilisation;
	
	@Column(name="main")
	private String isMainMachine;
	
	@Column
	private Long responsibility;
	
	@Column
	private String classify;
	
	@Column
	private int cluster;
	
	@Column
	private int company;
	
	@Column
	private int tcid;
	
	@Column(name="final")
	private String mfinal;
	
	@OneToOne
	@JoinColumn(name = "genesetid", referencedColumnName = "id")
	private Generator generator;
	
	@OneToOne
	@JoinColumn(name = "ebservice", referencedColumnName = "id")
	private EB eb;
	
	@Column
	private String panelent;
	
	@Column
	private String genset;
	
	@Column(name="dispname")
	private String dispName;
	
	@Column(name="cstatus")
	private String cStatus;
	
	@Column
	private String status;
	
	@Column
	private String test;
	
	@Column(name = "branch_id")
	private String branchId;

	@Column
	private String place; // godownName

	@Column
	private Long godown_id;
	
	@Column
	private String make;
	
	@Column
	private String capacity;
	
	@Column
	private String size;
	
	@OneToOne
	@JoinColumn(name = "classifyid", referencedColumnName = "id")
	private MachineClassification classifyDet;
	
	
	@Column(name="hsn_ref_id")
	private String hsnRefId;
	
	@Column(name="ebmf")
	private float ebMF;
	
	@Column(name="costgroupid")
	private int costGroupId;
	
	@Column
	private String locationpath;
	
	@Column(name = "about_machine")
	private String aboutMachine;
	
	@Column
	private String emergency;
	
	@OneToOne
	@JoinColumn(name = "purchase_on_acc", referencedColumnName = "id")
	private Organisation purchaseOnAcc;
	
	@OneToOne
	@JoinColumn(name = "purchase_from", referencedColumnName = "id")
	private Vendor purchaseFrom;

	@OneToOne
	@JoinColumn(name = "branch", referencedColumnName = "id")
	private Branch branch;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails departmentDetails;
	
	@OneToOne
	@JoinColumn(name = "task_category_id", referencedColumnName = "id")
	private TaskCategory taskCategory;
	
	@OneToOne
	@JoinColumn(name = "resp_1", referencedColumnName = "id")
	private User resp1;
	
	@OneToOne
	@JoinColumn(name = "resp_2", referencedColumnName = "id")
	private User resp2;
	
	@OneToOne
	@JoinColumn(name = "resp_3", referencedColumnName = "id")
	private User resp3;

	@Column( name = "panel_presence")
	private String panel;
	
	@OneToOne
	@JoinColumn(name = "depreciation_id", referencedColumnName = "id")
	private DepreciationCategoryName depreciation;


	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;


	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@OneToOne(cascade = CascadeType.ALL, mappedBy = "machine", fetch = FetchType.LAZY)
	private MachineWorkFlow machineWorkFlow;
	

	public Machine(MachineDto field) {

		if (field.getSelectedBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(Long.valueOf(field.getSelectedBranchId()));
			this.branch = branch;
		}
		if (field.getDeptId() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(field.getDeptId());
			this.departmentDetails = departmentDetails;
		}
		if(field.getClassifyId()!=null) {
			MachineClassification mc = new MachineClassification();
			mc.setId(field.getClassifyId());
			this.classifyDet = mc;
		}
		if (field.getEbId() != null) {
			EB eb= new EB();
			eb.setId(field.getEbId());
			this.eb = eb;
		}
		if (field.getGensetId() != null) {
			Generator ref= new Generator();
			ref.setId(field.getGensetId());
			this.generator = ref;
		}

			this.panel = field.getPanel();

		if (field.getDepreciationId() != null) {
			DepreciationCategoryName ref= new DepreciationCategoryName();
			ref.setId(field.getDepreciationId());
			this.depreciation = ref;
		}
		if (field.getTaskCategoryId() != null) {
			TaskCategory ref= new TaskCategory();
			ref.setId(field.getTaskCategoryId());
			this.taskCategory = ref;
		}
		if (field.getResp1Id() != null) {
			User ref= new User();
			ref.setId(field.getResp1Id());
			this.resp1 = ref;
		}
		if (field.getResp2Id() != null) {
			User ref= new User();
			ref.setId(field.getResp2Id());
			this.resp2 = ref;
		}
		if (field.getResp3Id() != null) {
			User ref= new User();
			ref.setId(field.getResp3Id());
			this.resp3 = ref;
		}
		if (field.getPurchaseAccId() != null) {
			Organisation ref= new Organisation();
			ref.setId(field.getPurchaseAccId());
			this.purchaseOnAcc = ref;
		}
		if (field.getPurchaseFromId() != null) {
			Vendor ref= new Vendor();
			ref.setId(field.getPurchaseFromId());
			this.purchaseFrom = ref;
		}

		this.aboutMachine = field.getAboutMachine();
		this.isMainMachine = field.getIsMainMachine();
		this.emergency = field.getEmergency();
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
		this.branchId = field.getSelectedBranchId();
		this.place = field.getGodownName();
		this.godown_id = field.getGodownId();
		this.make = field.getMake();
		this.capacity = field.getCapacity();
		this.size = field.getSize();
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
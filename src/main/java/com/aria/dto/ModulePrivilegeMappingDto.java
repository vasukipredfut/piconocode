package com.aria.dto;

import com.aria.model.ModulePrivilegeMapping;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ModulePrivilegeMappingDto {

	private Long id;

	private Long roleId;

	private String roleName;

	private Long moduleId;

	private String moduleName;

	private Long subModuleId;

	private String subModuleName;

	private Long deptId;

	private String deptName;

	private int active;

	private String privilegeCreate;

	private String privilegeView;

	private String privilegeVerify;

	private String privilegeApprove;

	private String privilegeAuthorize;

	private String privilegeReject;

	private String privilegeUpdate;

	private String privilegePrint;

	private String privilegeAmendment;

	private String privilegeDelete;

	private String privilegeActivate;

	private String privilegeValidate;

	private String privilegeCreateAcrossBranch;

	private String privilegeViewAcrossBranch;

	private String privilegeVerifyAcrossBranch;

	private String privilegeApproveAcrossBranch;

	private String privilegeAuthorizeAcrossBranch;

	private String privilegeRejectAcrossBranch;

	private String privilegeUpdateAcrossBranch;

	private String privilegePrintAcrossBranch;

	private String privilegeAmendmentAcrossBranch;

	private String privilegeDeleteAcrossBranch;

	private String privilegeActivateAcrossBranch;

	private String privilegeValidateAcrossBranch;

	public ModulePrivilegeMappingDto(ModulePrivilegeMapping model) {
		this.id = model.getId();
		if (model.getRole() != null) {
			this.roleId = model.getRole().getId();
			this.roleName = model.getRole().getName();
		}

		if (model.getModule() != null) {
			this.moduleId = model.getModule().getId();
			this.moduleName = model.getModule().getName();
		}
		if (model.getSubModule() != null) {
			this.subModuleId = model.getSubModule().getId();
			this.subModuleName = model.getSubModule().getSubmoduleName();
		}

		if (model.getDept() != null) {
			this.deptId = model.getDept().getId();
			this.deptName = model.getDept().getName();
		}
		this.active = model.getActive();
		this.privilegeCreate = model.getPrivilegeCreate();
		this.privilegeView = model.getPrivilegeView();
		this.privilegeVerify = model.getPrivilegeVerify();
		this.privilegeApprove = model.getPrivilegeApprove();
		this.privilegeAuthorize = model.getPrivilegeAuthorize();
		this.privilegeReject = model.getPrivilegeReject();
		this.privilegeUpdate = model.getPrivilegeUpdate();
		this.privilegePrint = model.getPrivilegePrint();
		this.privilegeAmendment = model.getPrivilegeAmendment();
		this.privilegeDelete = model.getPrivilegeDelete();
		this.privilegeValidate = model.getPrivilegeValidate();
		this.privilegeActivate = model.getPrivilegeActivate();
		this.privilegeCreateAcrossBranch = model.getPrivilegeCreateAcrossBranch();
		this.privilegeViewAcrossBranch = model.getPrivilegeViewAcrossBranch();
		this.privilegeVerifyAcrossBranch = model.getPrivilegeVerifyAcrossBranch();
		this.privilegeApproveAcrossBranch = model.getPrivilegeApproveAcrossBranch();
		this.privilegeAuthorizeAcrossBranch = model.getPrivilegeAuthorizeAcrossBranch();
		this.privilegeRejectAcrossBranch = model.getPrivilegeRejectAcrossBranch();
		this.privilegeUpdateAcrossBranch = model.getPrivilegeUpdateAcrossBranch();
		this.privilegePrintAcrossBranch = model.getPrivilegePrintAcrossBranch();
		this.privilegeAmendmentAcrossBranch = model.getPrivilegeAmendmentAcrossBranch();
		this.privilegeDeleteAcrossBranch = model.getPrivilegeDeleteAcrossBranch();
		this.privilegeValidateAcrossBranch = model.getPrivilegeValidateAcrossBranch();
		this.privilegeActivateAcrossBranch = model.getPrivilegeActivateAcrossBranch();

	}

}

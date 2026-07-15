package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.ModulePrivilegeMappingDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "module_privilege_mapping")
public class ModulePrivilegeMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "role_id", referencedColumnName = "id")
	private Roles role;

	@OneToOne
	@JoinColumn(name = "module_id", referencedColumnName = "id")
	private Modules module;

	@OneToOne
	@JoinColumn(name = "submodule_id", referencedColumnName = "id")
	private SubModules subModule;

	@OneToOne
	@JoinColumn(name = "dept_id", referencedColumnName = "id")
	private DepartmentDetails dept;

	@Column(name = "active")
	private int active;

	@Column(name = "privilege_create")
	private String privilegeCreate;

	@Column(name = "privilege_view")
	private String privilegeView;

	@Column(name = "privilege_verify")
	private String privilegeVerify;

	@Column(name = "privilege_approve")
	private String privilegeApprove;

	@Column(name = "privilege_authorize")
	private String privilegeAuthorize;

	@Column(name = "privilege_reject")
	private String privilegeReject;

	@Column(name = "privilege_update")
	private String privilegeUpdate;

	@Column(name = "privilege_print")
	private String privilegePrint;

	@Column(name = "privilege_amendment")
	private String privilegeAmendment;

	@Column(name = "privilege_delete")
	private String privilegeDelete;

	@Column(name = "privilege_activate")
	private String privilegeActivate;

	@Column(name = "privilege_validate")
	private String privilegeValidate;

	@Column(name = "privilege_create_across_branch")
	private String privilegeCreateAcrossBranch;

	@Column(name = "privilege_view_across_branch")
	private String privilegeViewAcrossBranch;

	@Column(name = "privilege_verify_across_branch")
	private String privilegeVerifyAcrossBranch;

	@Column(name = "privilege_approve_across_branch")
	private String privilegeApproveAcrossBranch;

	@Column(name = "privilege_authorize_across_branch")
	private String privilegeAuthorizeAcrossBranch;

	@Column(name = "privilege_reject_across_branch")
	private String privilegeRejectAcrossBranch;

	@Column(name = "privilege_update_across_branch")
	private String privilegeUpdateAcrossBranch;

	@Column(name = "privilege_print_across_branch")
	private String privilegePrintAcrossBranch;

	@Column(name = "privilege_amendment_across_branch")
	private String privilegeAmendmentAcrossBranch;

	@Column(name = "privilege_delete_across_branch")
	private String privilegeDeleteAcrossBranch;

	@Column(name = "privilege_activate_across_branch")
	private String privilegeActivateAcrossBranch;

	@Column(name = "privilege_validate_across_branch")
	private String privilegeValidateAcrossBranch;

	public ModulePrivilegeMapping(ModulePrivilegeMappingDto dto) {
		this.id = dto.getId();
		if (dto.getRoleId() != null) {
			Roles role = new Roles();
			role.setId(dto.getRoleId());
			this.role = role;
		}

		if (dto.getModuleId() != null) {
			Modules module = new Modules();
			module.setId(dto.getModuleId());
			this.module = module;
		}

		if (dto.getModuleId() != null) {
			SubModules subModule = new SubModules();
			subModule.setId(dto.getSubModuleId());
			this.subModule = subModule;
		}

		if (dto.getDeptId() != null) {
			DepartmentDetails dept = new DepartmentDetails();
			dept.setId(dto.getDeptId());
			this.dept = dept;
		}
		this.active = 1;
		this.privilegeCreate = dto.getPrivilegeCreate();
		this.privilegeView = dto.getPrivilegeView();
		this.privilegeVerify = dto.getPrivilegeVerify();
		this.privilegeApprove = dto.getPrivilegeApprove();
		this.privilegeAuthorize = dto.getPrivilegeAuthorize();
		this.privilegeReject = dto.getPrivilegeReject();
		this.privilegeUpdate = dto.getPrivilegeUpdate();
		this.privilegePrint = dto.getPrivilegePrint();
		this.privilegeAmendment = dto.getPrivilegeAmendment();
		this.privilegeDelete = dto.getPrivilegeDelete();
		this.privilegeActivate = dto.getPrivilegeActivate();
		this.privilegeValidate = dto.getPrivilegeValidate();
		this.privilegeCreateAcrossBranch = dto.getPrivilegeCreateAcrossBranch();
		this.privilegeViewAcrossBranch = dto.getPrivilegeViewAcrossBranch();
		this.privilegeVerifyAcrossBranch = dto.getPrivilegeVerifyAcrossBranch();
		this.privilegeApproveAcrossBranch = dto.getPrivilegeApproveAcrossBranch();
		this.privilegeAuthorizeAcrossBranch = dto.getPrivilegeAuthorizeAcrossBranch();
		this.privilegeRejectAcrossBranch = dto.getPrivilegeRejectAcrossBranch();
		this.privilegeUpdateAcrossBranch = dto.getPrivilegeUpdateAcrossBranch();
		this.privilegePrintAcrossBranch = dto.getPrivilegePrintAcrossBranch();
		this.privilegeAmendmentAcrossBranch = dto.getPrivilegeAmendmentAcrossBranch();
		this.privilegeDeleteAcrossBranch = dto.getPrivilegeDeleteAcrossBranch();
		this.privilegeValidateAcrossBranch = dto.getPrivilegeValidateAcrossBranch();
		this.privilegeActivateAcrossBranch = dto.getPrivilegeActivateAcrossBranch();

	}

}

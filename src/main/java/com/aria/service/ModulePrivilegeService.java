package com.aria.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.ModulePrivilegeMappingRepository;
import com.aria.dao.ModuleRepository;
import com.aria.dao.RolesRepository;
import com.aria.dao.SubModuleRepository;
import com.aria.dto.ModulePrivilegeMappingDto;
import com.aria.dto.ModulesDto;
import com.aria.dto.RolesDto;
import com.aria.dto.SubModulesDto;
import com.aria.model.ModulePrivilegeMapping;
import com.aria.model.Modules;
import com.aria.model.Roles;
import com.aria.model.Status;
import com.aria.model.SubModules;

@Service
public class ModulePrivilegeService {

    @Autowired
    private ModulePrivilegeMappingRepository modulePrivilegeMappingRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private SubModuleRepository subModuleRepository;

    @Autowired
    private RolesRepository rolesRepository;

    /* Module Privilege Mapping */
    public ResponseEntity<?> saveModulePrivilegeMapping(List<ModulePrivilegeMappingDto> dtos) {
        List<ModulePrivilegeMapping> mappingsToSave = new ArrayList<>();

        for (ModulePrivilegeMappingDto dto : dtos) {
            Optional<ModulePrivilegeMapping> existingMapping = modulePrivilegeMappingRepository
                    .findByRoleIdAndModuleIdAndSubModuleIdAndDeptId(dto.getRoleId(), dto.getModuleId(),
                            dto.getSubModuleId(), dto.getDeptId());

            if (existingMapping.isPresent()) {
                ModulePrivilegeMapping model = existingMapping.get();
                model.setPrivilegeCreate(dto.getPrivilegeCreate());
                model.setPrivilegeView(dto.getPrivilegeView());
                model.setPrivilegeVerify(dto.getPrivilegeVerify());
                model.setPrivilegeApprove(dto.getPrivilegeApprove());
                model.setPrivilegeAuthorize(dto.getPrivilegeAuthorize());
                model.setPrivilegeReject(dto.getPrivilegeReject());
                model.setPrivilegeUpdate(dto.getPrivilegeUpdate());
                model.setPrivilegePrint(dto.getPrivilegePrint());
                model.setPrivilegeAmendment(dto.getPrivilegeAmendment());
                model.setPrivilegeDelete(dto.getPrivilegeDelete());
                model.setPrivilegeActivate(dto.getPrivilegeActivate());
                model.setPrivilegeValidate(dto.getPrivilegeValidate());
                // Across Branch Permissions
                model.setPrivilegeCreateAcrossBranch(dto.getPrivilegeCreateAcrossBranch());
                model.setPrivilegeViewAcrossBranch(dto.getPrivilegeViewAcrossBranch());
                model.setPrivilegeVerifyAcrossBranch(dto.getPrivilegeVerifyAcrossBranch());
                model.setPrivilegeApproveAcrossBranch(dto.getPrivilegeApproveAcrossBranch());
                model.setPrivilegeAuthorizeAcrossBranch(dto.getPrivilegeAuthorizeAcrossBranch());
                model.setPrivilegeRejectAcrossBranch(dto.getPrivilegeRejectAcrossBranch());
                model.setPrivilegeUpdateAcrossBranch(dto.getPrivilegeUpdateAcrossBranch());
                model.setPrivilegePrintAcrossBranch(dto.getPrivilegePrintAcrossBranch());
                model.setPrivilegeAmendmentAcrossBranch(dto.getPrivilegeAmendmentAcrossBranch());
                model.setPrivilegeDeleteAcrossBranch(dto.getPrivilegeDeleteAcrossBranch());
                model.setPrivilegeActivateAcrossBranch(dto.getPrivilegeActivateAcrossBranch());
                model.setPrivilegeValidateAcrossBranch(dto.getPrivilegeValidateAcrossBranch());
                mappingsToSave.add(model);
            } else {
                ModulePrivilegeMapping model = new ModulePrivilegeMapping(dto);
                mappingsToSave.add(model);
            }
        }

        modulePrivilegeMappingRepository.saveAll(mappingsToSave);
        return new ResponseEntity<>(new Status("200", "ModulePrivilegeMappings saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> getModulePrivilegesByFilter(Long roleId, Long moduleId, Long subModuleId, Long deptId) {
        List<ModulePrivilegeMapping> existingMapping = modulePrivilegeMappingRepository.findByFilter(roleId, moduleId,
                subModuleId, deptId);
        List<ModulePrivilegeMappingDto> dto = existingMapping.stream()
                /*.filter(val -> (val.getPrivilegeCreate() != null && !val.getPrivilegeCreate().equalsIgnoreCase("false"))
                        || (val.getPrivilegeView() != null && !val.getPrivilegeView().equalsIgnoreCase("false"))
                        || (val.getPrivilegeVerify() != null && !val.getPrivilegeVerify().equalsIgnoreCase("false"))
                        || (val.getPrivilegeApprove() != null && !val.getPrivilegeApprove().equalsIgnoreCase("false"))
                        || (val.getPrivilegeAuthorize() != null
                                && !val.getPrivilegeAuthorize().equalsIgnoreCase("false"))
                        || (val.getPrivilegeReject() != null && !val.getPrivilegeReject().equalsIgnoreCase("false"))
                        || (val.getPrivilegeUpdate() != null && !val.getPrivilegeUpdate().equalsIgnoreCase("false"))
                        || (val.getPrivilegePrint() != null && !val.getPrivilegePrint().equalsIgnoreCase("false"))
                        || (val.getPrivilegeAmendment() != null
                                && !val.getPrivilegeAmendment().equalsIgnoreCase("false"))
                        || (val.getPrivilegeDelete() != null && !val.getPrivilegeDelete().equalsIgnoreCase("false"))
                        || (val.getPrivilegeActivate() != null && !val.getPrivilegeActivate().equalsIgnoreCase("false"))
                        || (val.getPrivilegeValidate() != null
                                && !val.getPrivilegeValidate().equalsIgnoreCase("false")))*/
                .map(ModulePrivilegeMappingDto::new).collect(Collectors.toList());

        return new ResponseEntity<List<ModulePrivilegeMappingDto>>(dto, HttpStatus.OK);

    }

    public ResponseEntity<?> getModulePrivilegesById(Long id) {
        Optional<ModulePrivilegeMapping> existingMapping = modulePrivilegeMappingRepository.findById(id);
        if (existingMapping.isPresent()) {
            ModulePrivilegeMappingDto dto = new ModulePrivilegeMappingDto(existingMapping.get());
            return new ResponseEntity<ModulePrivilegeMappingDto>(dto, HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("A mapping with the given id doesn't exists");

    }

    /* Roles */
    public ResponseEntity<?> saveRole(RolesDto dto) {
        Optional<Roles> existingRole = rolesRepository.findByNameIgnoreCase(dto.getName());
        if (!existingRole.isPresent()) {
            Roles model = new Roles(dto);
            rolesRepository.save(model);
            return new ResponseEntity<Status>(new Status("200", "Roles saved successfully!!!"), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Role already exist");
    }

    public ResponseEntity<?> getAllRoles() {
        List<Roles> role = rolesRepository.findAll();
        return new ResponseEntity<List<Roles>>(role, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteRole(Long roleId) {
        Optional<Roles> existingRole = rolesRepository.findById(roleId);
        if (existingRole.isPresent()) {
            rolesRepository.deleteById(roleId);
            return new ResponseEntity<Status>(new Status("200", "Roles deleted successfully!!!"), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Role doesn't exist");
    }

    /* Modules */
    public ResponseEntity<?> saveModule(ModulesDto dto) {
        Optional<Modules> existingModule = moduleRepository.findByNameIgnoreCase(dto.getModuleName());
        if (!existingModule.isPresent()) {
            Modules model = new Modules(dto);
            moduleRepository.save(model);
            return new ResponseEntity<Status>(new Status("200", "Modules saved successfully!!!"), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Modules already exist");
    }

    public ResponseEntity<?> getAllModules() {
        List<Modules> module = moduleRepository.findAll();
        List<ModulesDto> dto = module.stream().map(val -> new ModulesDto(val)).collect(Collectors.toList());
        return new ResponseEntity<List<ModulesDto>>(dto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteModule(Long moduleId) {
        Optional<Modules> existingModule = moduleRepository.findById(moduleId);
        if (existingModule.isPresent()) {

            List<SubModules> subModules = subModuleRepository.findByModuleId(moduleId);
            if (!subModules.isEmpty()) {
                for (SubModules subModule : subModules) {
                    List<ModulePrivilegeMapping> mappings = modulePrivilegeMappingRepository.findBy(moduleId, subModule.getId());
                    if (mappings.size() > 0) {
                        modulePrivilegeMappingRepository.deleteAll(mappings);
                    }
                }
                subModuleRepository.deleteAll(subModules);
            }
            moduleRepository.deleteById(moduleId);
            return new ResponseEntity<Status>(new Status("200", "Module deleted successfully!!!"), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("Module doesn't exist");
    }

    /* SubModules */
    public ResponseEntity<?> saveSubModule(SubModulesDto dto) {
        Optional<SubModules> existingSubModule = subModuleRepository.findBySubmoduleNameIgnoreCase(dto.getName());
        if (!existingSubModule.isPresent()) {
            SubModules model = new SubModules(dto);
            subModuleRepository.save(model);
            return new ResponseEntity<Status>(new Status("200", "SubModule saved successfully!!!"), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("SubModule already exist");
    }

    public ResponseEntity<?> getSubModulesByModuleId(Long moduleId) {
        List<SubModules> subModule = subModuleRepository.findByModuleId(moduleId);
        if (subModule.isEmpty()) {
            return new ResponseEntity<List<SubModulesDto>>(new ArrayList<SubModulesDto>(), HttpStatus.OK);
        }
        List<SubModulesDto> dto = subModule.stream().map(val -> new SubModulesDto(val)).collect(Collectors.toList());
        return new ResponseEntity<List<SubModulesDto>>(dto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteSubModule(Long subModuleId) {
        Optional<SubModules> existingSubModule = subModuleRepository.findById(subModuleId);
        if (existingSubModule.isPresent()) {
            List<ModulePrivilegeMapping> mappings = modulePrivilegeMappingRepository.findBy(existingSubModule.get().getModuleId().getId(), existingSubModule.get().getId());
            if (mappings.size() > 0) {
                modulePrivilegeMappingRepository.deleteAll(mappings);
            }

            subModuleRepository.deleteById(subModuleId);
            return new ResponseEntity<Status>(new Status("200", "SubModule deleted successfully!!!"), HttpStatus.OK);
        }
        return ResponseEntity.badRequest().body("SubModule doesn't exist");
    }

}

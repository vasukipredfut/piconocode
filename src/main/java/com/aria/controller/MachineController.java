package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aria.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.*;
import com.aria.model.*;
import com.aria.service.MachineService;

@RestController
@RequestMapping("/api")
public class MachineController {

    @Autowired
    MachineService machineService;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/machine")
    public ResponseEntity<?> saveMachine(@RequestBody MachineDto MachineDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.saveMachine(MachineDto, user.get());
    }

    @PutMapping("/machine")
    public ResponseEntity<?> updateMachine(@RequestBody MachineDto MachineDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.updateMachine(MachineDto, user.get());
    }

    @GetMapping("/machine/{id}")
    public ResponseEntity<?> getMachineDetails(@PathVariable String id, Authentication authentication) {

        return machineService.getMachineDetails(id);
    }

    @DeleteMapping("/machine/{id}")
    public ResponseEntity<?> deleteMachineDetails(@PathVariable String id, Authentication authentication) {

        return machineService.deleteMachine(id);
    }

    @GetMapping("/machine")
    public ResponseEntity<?> Machine(@RequestParam(value = "deptId", required = false) Long deptId,
                                     @RequestParam(value = "branchId", required = false) Long branchId,
                                     @RequestParam(value = "mainMachine", required = false) String mainMachine,
                                     @RequestParam(value = "machineName", required = false) String machineName,
                                     @RequestParam(value = "isoNo", required = false) String isoNo,
                                     @RequestParam(value = "status", required = false) String status,
                                     @RequestParam(value = "isMachineSold", required = false) boolean isMachineSold,
                                     @RequestParam(value = "panel", required = false) String panel,
                                     @RequestParam(value = "fromDate", required = false) String fromDate,
                                     @RequestParam(value = "paging", required = false,defaultValue = "true") boolean paging,
                                     @RequestParam(value = "toDate", required = false) String toDate, Pageable pageable,
                                     Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.isPresent()) {
            if(paging)
            return machineService.machineFilterList(deptId, branchId, fromDate, toDate, mainMachine, machineName, isoNo, status, panel,isMachineSold,pageable);
            else {
                return machineService.machineFilterList(deptId, branchId, fromDate, toDate, mainMachine, machineName, isoNo, status, panel,isMachineSold);
            }
        } else {
            return new ResponseEntity<List<MachineDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    //Machine Classification

    @PostMapping("/machineClassification")
    public ResponseEntity<?> saveMachineClassification(@RequestBody MachineClassificationDto machineClassificationDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.saveMachineClassification(machineClassificationDto, user.get());
    }

    @PutMapping("/machineClassification")
    public ResponseEntity<?> updateMachineClassification(@RequestBody MachineClassificationDto machineClassificationDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.updateMachineClassification(machineClassificationDto, user.get());
    }

    @GetMapping("/machineClassification/{id}")
    public ResponseEntity<?> getMachineClassificationDetails(@PathVariable String id, Authentication authentication) {

        return machineService.getMachineClassificationDetails(id);
    }

    @DeleteMapping("/machineClassification/{id}")
    public ResponseEntity<?> deleteMachineClassificationDetails(@PathVariable String id, Authentication authentication) {

        return machineService.deleteMachineClassification(id);
    }

    @GetMapping("/machineClassification")
    public ResponseEntity<?> MachineClassification(
            @RequestParam(value = "name", required = false) String name, Pageable pageable,
            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return machineService.machineClassificationFilterList(name, pageable);
        } else {
            return new ResponseEntity<List<MachineClassificationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/machineClassificationDropDown")
    public ResponseEntity<?> MachineClassification(
            @RequestParam(value = "name", required = false) String name,
            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return machineService.machineClassificationFilterListDropDown(name);
        } else {
            return new ResponseEntity<List<MachineClassificationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Machine Cluster

    @PostMapping("/machineCluster")
    public ResponseEntity<?> saveMachineCluster(@RequestBody MachineClusterDto machineClusterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.saveMachineCluster(machineClusterDto, user.get());
    }

    @PutMapping("/machineCluster")
    public ResponseEntity<?> updateMachineCluster(@RequestBody MachineClusterDto machineClusterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.updateMachineCluster(machineClusterDto, user.get());
    }

    @GetMapping("/machineCluster/{id}")
    public ResponseEntity<?> getMachineClusterDetails(@PathVariable String id, Authentication authentication) {

        return machineService.getMachineClusterDetails(id);
    }

    @GetMapping("/machineCluster/machine/{id}")
    public ResponseEntity<?> getMachineClusterDetailsByMachine(@PathVariable Long id, Authentication authentication) {
        return machineService.getMachineClusterDetailsByMachine(id);
    }

    @DeleteMapping("/machineCluster/{id}")
    public ResponseEntity<?> deleteMachineClusterDetails(@PathVariable Long id, Authentication authentication) {

        return machineService.deleteMachineCluster(id);
    }

    @DeleteMapping("/machineCluster/item/{id}")
    public ResponseEntity<?> deleteMachineClusterItem(@PathVariable Long id, Authentication authentication) {

        return machineService.deleteMachineClusterItem(id);
    }

    @GetMapping("/machineCluster")
    public ResponseEntity<?> MachineCluster(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "id", required = false) Long id,
            Pageable pageable,
            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return machineService.machineClusterFilterList(name, id, pageable);
        } else {
            return new ResponseEntity<List<MachineClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Machine Workflow

    @GetMapping("/machineWorkFlow")
    public ResponseEntity<?> MachineWorkFlow(
            @RequestParam(value = "machineName", required = false) String machineName,
            @RequestParam(value = "machineClassificationId", required = false) Long machineClassificationId,
            Pageable pageable,
            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return machineService.getAllMachineWorkFlowDetails(machineName, machineClassificationId, pageable,user.get());
        } else {
            return new ResponseEntity<List<MachineWorkFlowDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    
    @GetMapping("/machine/getNames")
    public ResponseEntity<?> MachineList(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return machineService.machineList(user.get());
        } else {
            return new ResponseEntity<List<MachineDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/machineWorkFlow")
    public ResponseEntity<?> updateMachineWorkFlow(@RequestBody MachineWorkFlowDto machineWorkFlowDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.updateMachineWorkFlow(machineWorkFlowDto, user.get());
    }

    @PostMapping("/savePMAttributes")
    public ResponseEntity<?> savePMAttributes(@RequestBody PMSaveAttributesDto pmSaveAttributesDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.savePMAttributes(pmSaveAttributesDto, user.get());
    }

    @GetMapping("/machineWorkFlow/{id}")
    public ResponseEntity<?> getMachineWorkFlowDetails(@PathVariable Long id, Authentication authentication) {

        return machineService.getMachineWorkFlowDetails(id);
    }

    @DeleteMapping("/machineWorkFlow/{id}")
    public ResponseEntity<?> deleteMachineWorkFlowDetails(@PathVariable String id, Authentication authentication) {

        return machineService.deleteMachineWorkFlow(id);
    }


    @GetMapping("/machineWorkFlow/consumables")
    public ResponseEntity<?> getMachineWorkFlowConsumablesDetails(Authentication authentication, Pageable pageable) {

        return machineService.getMachineWorkFlowConsumablesDetails(pageable);
    }

    //Machine WorkFlow Maintenance Reason

    @PostMapping("/machineWorkFlow/maintenance")
    public ResponseEntity<?> saveMaintenance(@RequestBody List<MachineWFMaintenanceReasonDto> req, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.saveMaintenance(req, user.get());
    }

    @GetMapping("/machineWorkFlow/maintenance")
    public ResponseEntity<?> getMaintenance(Authentication authentication, Pageable pageable) {

        return machineService.getMachineWorkFlowMaintenanceReasonDetails(pageable);
    }

    @GetMapping("/machineWorkFlow/maintenance/{id}")
    public ResponseEntity<?> getMaintenanceById(@PathVariable Long id, Authentication authentication) {

        return machineService.getMachineWorkFlowMaintenanceReasonDetailsById(id);
    }

    //Machine WorkFlow Breakdown Reason

    @PostMapping("/machineWorkFlow/breakdown")
    public ResponseEntity<?> saveBreakdown(@RequestBody MachineWFBreakdown req, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return machineService.saveBreakdown(req, user.get());
    }

//    @GetMapping("/machineWorkFlow/breakdown")
//    public ResponseEntity<?> getBreakdown(Authentication authentication, Pageable pageable) {
//
//        return machineService.getMachineWorkFlowBreakdownReasonDetails(pageable);
//    }

    @GetMapping("/machineWorkFlow/breakdown")
    public ResponseEntity<?> getBreakdown(@RequestParam(value = "machineWorkFlowId", required = false) Long machineWorkFlowId,Authentication authentication) {

        return machineService.machineBreakDownByMachineWorkFlowId(machineWorkFlowId);
    }

    @GetMapping("/machineWorkFlow/breakdown/{id}")
    public ResponseEntity<?> getBreakdownById(@PathVariable Long id, Authentication authentication) {

        return machineService.getMachineWorkFlowBreakdownReasonDetailsById(id);
    }
    
    @GetMapping("/machine/getAllVehicleNames")
    public ResponseEntity<?> getAllVehicleNames(Authentication authentication){
    	 return machineService.getAllVehicleNames();
    }

}

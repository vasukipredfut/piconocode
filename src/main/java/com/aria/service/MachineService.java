
package com.aria.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.*;

@Service
public class MachineService {

    public static final String SOLD = "SOLD";
    @Autowired
    MachineRepository machineRepository;

    @Autowired
    MachineClassificationRepository machineClassificationRepository;

    @Autowired
    MachineClusterRepository machineClusterRepository;

    @Autowired
    MachineClusterItemsRepository machineClusterItemsRepository;

    @Autowired
    MachineWorkFlowRepository machineWorkFlowRepository;

    @Autowired
    EBRepository ebRepository;

    @Autowired
    MachineWFMaintenanceReasonRepository mwfMaintenanceReasonRepository;

    @Autowired
    MachineWFBreakdownReasonRepository mwfBreakdownReasonRepository;

    @Autowired
    MachineWFBreakdownReasonDetailsRepository mwfBreakdownReasonDetailsRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    PMScheduleRepository pmScheduleRepository;

    @Autowired
    PreventiveMaintainceAttributeRepository preventiveMaintainceAttributeRepository;

    @Autowired
    MachineWFBreakdownReasonRepository breakdownReasonRepository;

    @Autowired
    PanelRepository panelRepository;

    @Autowired
    MachinePanelReadingMappingRepository panelReadingMappingRepository;

    public ResponseEntity<?> saveMachine(MachineDto machineDto, User loggedUser) {
        Machine machine = new Machine(machineDto);
        machine.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        machine.setCreatedBy(loggedUser.getId());
        machine.setDateOfInstallation(dateUtil.getStringTODateDDMMYYYY(machineDto.getDateOfInstallation()));
        Machine res = machineRepository.save(machine);
		/*if (res != null) {
			MachineWorkFlowDto machineDto1 = new MachineWorkFlowDto();
			machineDto1.setMachineId(res.getId());
			Optional<EB> eb = ebRepository.findById(machineDto.getEbId());
			machineDto1.setMachineName(res.getName());
			machineDto1.setPowerSource(eb.get().getServiceName());
			machineDto1.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			machineDto1.setCreatedBy(loggedUser.getId());
			MachineWorkFlow mf = new MachineWorkFlow(machineDto1);
			machineWorkFlowRepository.save(mf);
		}*/
        return new ResponseEntity<Status>(new Status("200", "Machine save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateMachine(MachineDto machineDto, User loggedUser) {
        Machine machine = new Machine(machineDto);
        machine.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        machine.setUpdatedBy(loggedUser.getId());
        machine.setDateOfInstallation(dateUtil.getStringTODateDDMMYYYY(machineDto.getDateOfInstallation()));
        machineRepository.save(machine);

        return new ResponseEntity<Status>(new Status("200", "Machine update successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> getMachineDetails(String id) throws UsernameNotFoundException {
        Optional<Machine> optionalMachine = machineRepository.findById(Long.parseLong(id));
        MachineDto machineDto = new MachineDto(optionalMachine.get());
        machineDto
                .setDateOfInstallation(dateUtil.getDateToStringDDMMYYYY(optionalMachine.get().getDateOfInstallation()));
        return new ResponseEntity<MachineDto>(machineDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteMachine(String id) throws UsernameNotFoundException {
        try {
            Optional<Machine> machineOptional = machineRepository.findById(Long.parseLong(id));
            if (machineOptional.isPresent()) {
                Machine machine = machineOptional.get();
                machineRepository.delete(machine);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> machineFilterList(Long deptId, Long branchId, String dateFrom, String dateTo, String mainMachine,
                                               String machineName, String isoNo, String status, String panel, boolean isMachineSold, Pageable pageable) throws UsernameNotFoundException {
        Page<Machine> machines;
        if (!isMachineSold) {
            machines = machineRepository.machineFilterNotSold(deptId, branchId, dateUtil.getFilterDate(dateFrom),
                    dateUtil.getFilterDate(dateTo), mainMachine, machineName, isoNo, status, panel, pageable);
        } else {
            machines = machineRepository.machineFilterList(deptId, branchId, dateUtil.getFilterDate(dateFrom),
                    dateUtil.getFilterDate(dateTo), mainMachine, machineName, isoNo, status, panel, pageable);
        }

        List<MachineDto> result = new ArrayList<>();
        if (!machines.toList().isEmpty()) {

            Map<String, String> getAllLocationPath = new HashMap<>();
            for (Machine m : machines.toList()) {
                if (m.getLocationpath() != null) {
                    getAllLocationPath.put(m.getLocationpath(), m.getLocationpath());
                }
            }
            Map<String, String> getAllImages = new HashMap<>();
            try {
                getAllImages = fileStorageService.downloadFileAsBase64AsList(getAllLocationPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Machine data : machines.toList()) {
                MachineDto machineDto = new MachineDto(data);
                for (Entry<String, String> temp : getAllImages.entrySet()) {
                    if (temp.getKey().equals(data.getLocationpath())) {
                        machineDto.setPicture(temp.getValue());
                    }
                }
                machineDto.setDateOfInstallation(dateUtil.getDateToStringDDMMYYYY(data.getDateOfInstallation()));
                result.add(machineDto);
            }


            PageDataDto<MachineDto> pageData = new PageDataDto<>(result, pageable, machines);
            return new ResponseEntity<>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MachineDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> machineFilterList(Long deptId, Long branchId, String dateFrom, String dateTo, String mainMachine,
                                               String machineName, String isoNo, String status, String panel, boolean isMachineSold) throws UsernameNotFoundException {

        List<Machine> machinefilterList;
        if (!isMachineSold) {
            machinefilterList = machineRepository.machineFilterListNotSold(deptId, branchId, dateUtil.getFilterDate(dateFrom),
                    dateUtil.getFilterDate(dateTo), mainMachine, machineName, isoNo, status, panel);
        } else {
            machinefilterList = machineRepository.machinefilterList(deptId, branchId, dateUtil.getFilterDate(dateFrom),
                    dateUtil.getFilterDate(dateTo), mainMachine, machineName, isoNo, status, panel);
        }
        List<Machine> machines = new ArrayList<>(machinefilterList);

        List<MachineDto> result = new ArrayList<>();
        if (!machines.isEmpty()) {

            Map<String, String> getAllLocationPath = new HashMap<>();
            for (Machine m : machines) {
                if (m.getLocationpath() != null) {
                    getAllLocationPath.put(m.getLocationpath(), m.getLocationpath());
                }
            }
            Map<String, String> getAllImages = new HashMap<>();
            try {
                getAllImages = fileStorageService.downloadFileAsBase64AsList(getAllLocationPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Machine data : machines) {
                MachineDto machineDto = new MachineDto(data);
                for (Entry<String, String> temp : getAllImages.entrySet()) {
                    if (temp.getKey().equals(data.getLocationpath())) {
                        machineDto.setPicture(temp.getValue());
                    }
                }
                machineDto.setDateOfInstallation(dateUtil.getDateToStringDDMMYYYY(data.getDateOfInstallation()));
                result.add(machineDto);
            }


            return new ResponseEntity<List<MachineDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MachineDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Machine Classification

    public ResponseEntity<?> saveMachineClassification(MachineClassificationDto machineClassificationDto, User user) {
        MachineClassification machine = new MachineClassification(machineClassificationDto);
        machine.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        machine.setCreatedBy(user.getId());
        machineClassificationRepository.save(machine);

        return new ResponseEntity<Status>(new Status("200", "Machine Classification save successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> updateMachineClassification(MachineClassificationDto machineDto, User loggedUser) {
        MachineClassification machine = new MachineClassification(machineDto);
        machine.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        machine.setUpdatedBy(loggedUser.getId());
        machineClassificationRepository.save(machine);

        return new ResponseEntity<Status>(new Status("200", "MachineClassification update successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> getMachineClassificationDetails(String id) throws UsernameNotFoundException {
        Optional<MachineClassification> optionalMachineClassification = machineClassificationRepository
                .findById(Long.parseLong(id));
        MachineClassificationDto machineDto = new MachineClassificationDto(optionalMachineClassification.get());
        return new ResponseEntity<MachineClassificationDto>(machineDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteMachineClassification(String id) throws UsernameNotFoundException {
        try {
            Optional<MachineClassification> machineOptional = machineClassificationRepository
                    .findById(Long.parseLong(id));
            if (machineOptional.isPresent()) {
                MachineClassification machine = machineOptional.get();
                machineClassificationRepository.delete(machine);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> machineClassificationFilterList(String name, Pageable pageable)
            throws UsernameNotFoundException {
        Page<MachineClassification> list = machineClassificationRepository.machineClassificationFilterList(name,
                pageable);
        if (!list.toList().isEmpty()) {
            List<MachineClassificationDto> result = list.toList().stream().map(data -> {
                MachineClassificationDto machineDto = new MachineClassificationDto(data);
                return machineDto;
            }).collect(Collectors.toList());
            PageDataDto<MachineClassificationDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<MachineClassificationDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MachineClassificationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> machineClassificationFilterListDropDown(String name)
            throws UsernameNotFoundException {
        List<MachineClassification> list = machineClassificationRepository.machineClassificationFilterList(name
        );
        if (!list.isEmpty()) {
            List<MachineClassificationDto> result = list.stream().map(data -> {
                MachineClassificationDto machineDto = new MachineClassificationDto(data);
                return machineDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<MachineClassificationDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MachineClassificationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Machine Cluster

    public ResponseEntity<?> saveMachineCluster(MachineClusterDto machineClusterDto, User user) {
        if (machineClusterDto.getId() != null && machineClusterDto.getId() != 0) {
            return updateMachineCluster(machineClusterDto, user);
        }
        Optional<Machine> opMachine = machineRepository.findById(machineClusterDto.getMachineId());
        if (opMachine.isPresent()) {
            MachineCluster machineCluster = machineClusterRepository.findByMachine(opMachine.get());
            if (machineCluster != null) {
                return new ResponseEntity<Status>(new Status("400", "Record with same machine already exist"), HttpStatus.BAD_REQUEST);
            }
        }
        MachineCluster machine = new MachineCluster(machineClusterDto);
        machine.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        machine.setCreatedBy(user.getId());
        MachineCluster result = machineClusterRepository.save(machine);
        for (MachineClusterItemsDto reqDto : machineClusterDto.getMachineClusterItems()) {
            MachineClusterItems mc = new MachineClusterItems(reqDto);
            mc.setClusterId(result.getId());
            mc.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            mc.setCreatedBy(user.getId());
            machineClusterItemsRepository.save(mc);
        }

        return new ResponseEntity<Status>(new Status("200", "Machine Cluster save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateMachineCluster(MachineClusterDto machineClusterDto, User loggedUser) {
        MachineCluster machine = new MachineCluster(machineClusterDto);
        machine.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        machine.setUpdatedBy(loggedUser.getId());
        MachineCluster result = machineClusterRepository.save(machine);
        machineClusterItemsRepository.deleteByClusterId(result.getId());
        for (MachineClusterItemsDto reqDto : machineClusterDto.getMachineClusterItems()) {
            MachineClusterItems mc = new MachineClusterItems(reqDto);
            mc.setClusterId(result.getId());
            mc.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            mc.setCreatedBy(loggedUser.getId());
            machineClusterItemsRepository.save(mc);
        }

        return new ResponseEntity<Status>(new Status("200", "MachineCluster update successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> getMachineClusterDetails(String id) throws UsernameNotFoundException {
        Optional<MachineCluster> optionalMachineCluster = machineClusterRepository.findById(Long.parseLong(id));
        MachineClusterDto machineDto = new MachineClusterDto(optionalMachineCluster.get());
        return new ResponseEntity<MachineClusterDto>(machineDto, HttpStatus.OK);
    }

    public ResponseEntity<?> getMachineClusterDetailsByMachine(Long id) throws UsernameNotFoundException {
        Optional<Machine> opMachine = machineRepository.findById(id);
        if (opMachine.isPresent()) {
            MachineCluster machineCluster = machineClusterRepository.findByMachine(opMachine.get());
            if (machineCluster == null) {
                return new ResponseEntity<Status>(new Status("204", "No Record Found"), HttpStatus.NOT_FOUND);
            }

            MachineClusterDto machineDto = new MachineClusterDto(machineCluster);
            return new ResponseEntity<MachineClusterDto>(machineDto, HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("204", "No Record Found"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deleteMachineCluster(Long id) throws UsernameNotFoundException {
        try {
            Optional<MachineCluster> machineOptional = machineClusterRepository.findById(id);
            if (machineOptional.isPresent()) {
                MachineCluster machine = machineOptional.get();
                machineClusterRepository.delete(machine);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> deleteMachineClusterItem(Long id) throws UsernameNotFoundException {
        try {
            Optional<MachineClusterItems> machineOptional = machineClusterItemsRepository.findById(id);
            if (machineOptional.isPresent()) {
                MachineClusterItems machine = machineOptional.get();
                machineClusterItemsRepository.delete(machine);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> machineClusterFilterList(String name, Long id, Pageable pageable)
            throws UsernameNotFoundException {
        Page<MachineCluster> list = machineClusterRepository.machineClusterFilterList(name, id, pageable);
        if (!list.toList().isEmpty()) {
            List<MachineClusterDto> result = list.toList().stream().map(data -> {
                MachineClusterDto machineDto = new MachineClusterDto(data);
                return machineDto;
            }).collect(Collectors.toList());
            PageDataDto<MachineClusterDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<MachineClusterDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MachineClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Machine workflow

    public ResponseEntity<?> getAllMachineWorkFlowDetails(String machineName, Long machineClassificationId,
                                                          Pageable pageable, User user) {
        Page<Machine> list = machineClusterRepository.machineWorkFlowFilterList(machineName, machineClassificationId
                , user.getDepartment().getId(), user.getBranch_id(), pageable);
        if (!list.toList().isEmpty()) {
            List<MachineWorkFlowDto> result = list.toList().stream().map(data -> {
                return new MachineWorkFlowDto(data, dateUtil, preventiveMaintainceAttributeRepository, breakdownReasonRepository, userRepository);
            }).collect(Collectors.toList());
            PageDataDto<MachineWorkFlowDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<MachineWorkFlowDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MachineClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateMachineWorkFlow(MachineWorkFlowDto machineWorkFlowDto, User user) {
        Optional<Machine> optionalMachineCluster = machineRepository.findById(machineWorkFlowDto.getMachineId());
        if (optionalMachineCluster.isPresent()) {
            Machine machine = optionalMachineCluster.get();
            if (machine != null && machine.getEb() != null) {
                machineWorkFlowDto.setPowerSource(machine.getEb().getServiceName());
            }
            MachineWorkFlow machineWorkFlow = machine.getMachineWorkFlow();
            if (machineWorkFlow != null) {
                addIntoMachineWorkFlow(machineWorkFlow, machineWorkFlowDto, user);
                machineWorkFlow.setLogId(machineWorkFlow.getLogId() + 1);
                machineWorkFlow.setUpdatedBy(user.getId());
                machineWorkFlow.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                MachineWorkFlow resultWorkFlow = machineWorkFlowRepository.save(machineWorkFlow);
                if(machineWorkFlowDto.getPanelsReadingMapping() != null && !machineWorkFlowDto.getPanelsReadingMapping().isEmpty()) {
                    for (MachinePanelReadingMappingDto readingMapping : machineWorkFlowDto.getPanelsReadingMapping()) {
                        MachinePanelReadingMapping panelReadingMapping = null;
                        Optional<MachinePanelReadingMapping> optional = panelReadingMappingRepository.findBy(resultWorkFlow.getId(), readingMapping.getMachineId(), readingMapping.getPanelId());
                        if (optional.isPresent()) {
                            panelReadingMapping = new MachinePanelReadingMapping(optional.get(), readingMapping);
                        } else {
                            panelReadingMapping = new MachinePanelReadingMapping(readingMapping);
                            panelReadingMapping.setWorkFlowId(resultWorkFlow.getId());
                        }
                        panelReadingMappingRepository.save(panelReadingMapping);
                    }
                }
                List<MachinePanelReadingMapping> machinePanelReadingMapping = panelReadingMappingRepository.findByWorkFlowId(resultWorkFlow.getId());
                resultWorkFlow.setPanelsReadingMapping(machinePanelReadingMapping);
                machine.setMachineWorkFlow(resultWorkFlow);
            } else {
                machineWorkFlow = new MachineWorkFlow();
                machineWorkFlow.setLogId(machineWorkFlow.getLogId());
                machineWorkFlow.setMachine(machine);
                addIntoMachineWorkFlow(machineWorkFlow, machineWorkFlowDto, user);
                machineWorkFlow.setCreatedBy(user.getId());
                machineWorkFlow.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

                MachineWorkFlow resultWorkFlow = machineWorkFlowRepository.save(machineWorkFlow);
                for (MachinePanelReadingMappingDto readingMapping : machineWorkFlowDto.getPanelsReadingMapping()) {
                    MachinePanelReadingMapping panelReadingMapping = null;
                    Optional<MachinePanelReadingMapping> optional = panelReadingMappingRepository.findBy(resultWorkFlow.getId(), readingMapping.getMachineId(), readingMapping.getPanelId());
                    if (optional.isPresent()) {
                        panelReadingMapping = new MachinePanelReadingMapping(optional.get(), readingMapping);
                    } else {
                        panelReadingMapping = new MachinePanelReadingMapping(readingMapping);
                        panelReadingMapping.setWorkFlowId(resultWorkFlow.getId());
                    }
                    panelReadingMappingRepository.save(panelReadingMapping);
                }
                List<MachinePanelReadingMapping> machinePanelReadingMapping = panelReadingMappingRepository.findByWorkFlowId(resultWorkFlow.getId());
                resultWorkFlow.setPanelsReadingMapping(machinePanelReadingMapping);
                machine.setMachineWorkFlow(resultWorkFlow);

            }
            if (machineWorkFlow != null && machineWorkFlow.getCStatus().equalsIgnoreCase("OFF")) {
                preventiveMaintainceAttributeRepository.deleteByMcId(machineWorkFlow.getMachine().getId());
                breakdownReasonRepository.deleteByMachineWorkFlowId(machineWorkFlow.getId());
            }
            machineRepository.save(machine);
            return new ResponseEntity<Status>(new Status("200", "Machine Work Flow update successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("404", "Machine Cluster Not Found"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> savePMAttributes(PMSaveAttributesDto pmSaveAttributesDto, User user) {

        List<PreventiveMaintainceAttributes> result = new ArrayList<>();
        for (PMAttributesDto pmAttributesDto : pmSaveAttributesDto.getAttributes()) {
            PreventiveMaintainceAttributes preventiveMaintainceAttributes = preventiveMaintainceAttributeRepository.findBySchIdAndMcIdAndBatchNoAndTaskIdAndAtIdAndResId(pmSaveAttributesDto.getDetailedReasonId(), pmSaveAttributesDto.getMachineId(), "", pmSaveAttributesDto.getTaskId(), pmAttributesDto.getId(), 0L);
            if (preventiveMaintainceAttributes == null) {
                preventiveMaintainceAttributes = new PreventiveMaintainceAttributes();
            }
            preventiveMaintainceAttributes.setLogId(0L);
            preventiveMaintainceAttributes.setLogType("PM");
            preventiveMaintainceAttributes.setSchId(pmSaveAttributesDto.getDetailedReasonId());
            preventiveMaintainceAttributes.setMcId(pmSaveAttributesDto.getMachineId());
            preventiveMaintainceAttributes.setTaskId(pmSaveAttributesDto.getTaskId());
            preventiveMaintainceAttributes.setTgId(pmSaveAttributesDto.getTaskGroup());
            preventiveMaintainceAttributes.setTcId(pmSaveAttributesDto.getTaskCategory());
            preventiveMaintainceAttributes.setAtId(pmAttributesDto.getId());
            preventiveMaintainceAttributes.setResVal(pmAttributesDto.getValue());
            preventiveMaintainceAttributes.setBatchNo("");
            preventiveMaintainceAttributes.setResId(0L);
            result.add(preventiveMaintainceAttributes);
        }
        preventiveMaintainceAttributeRepository.saveAll(result);

        MachineWorkFlow machineWorkFlow = machineWorkFlowRepository.findByMachineId(pmSaveAttributesDto.getMachineId());
        if (machineWorkFlow != null) {
            machineWorkFlow.setProductionShift(pmSaveAttributesDto.getProductionShift());
            machineWorkFlow.setTimeSlab(pmSaveAttributesDto.getTimeSlab());
        }
        return new ResponseEntity<Status>(new Status("200", "Attributes successfully!!!"), HttpStatus.OK);

    }

    public ResponseEntity<?> getMachineWorkFlowDetails(Long id) throws UsernameNotFoundException {
        Optional<Machine> optionalMachineCluster = machineRepository.findById(id);
        if (optionalMachineCluster.isPresent()) {

            Machine machineCluster = optionalMachineCluster.get();

            MachineWorkFlowDto machineWorkFlowDto = new MachineWorkFlowDto(machineCluster, dateUtil, preventiveMaintainceAttributeRepository, breakdownReasonRepository, userRepository);

            if(machineWorkFlowDto.getId() != null) {
                List<MachinePanelReadingMapping> list = panelReadingMappingRepository.findByWorkFlowId(machineWorkFlowDto.getId());
                if (list != null && !list.isEmpty()) {
                    List<MachinePanelReadingMappingDto> readingMappingDtos = list.stream().map(data -> new MachinePanelReadingMappingDto(data)).collect(Collectors.toList());
                    machineWorkFlowDto.setPanelsReadingMapping(readingMappingDtos);
                }
            }
            if (machineWorkFlowDto.getPowerOffReason() != null && machineWorkFlowDto.getPowerOffReason().equalsIgnoreCase("Preventive Maintenance")) {
                Optional<PMSchedule> pmScheduleOp = pmScheduleRepository.findById(machineWorkFlowDto.getDetailedReasonId());
                if (pmScheduleOp.isPresent()) {
                    PMSchedule pmSchedule = pmScheduleOp.get();
                    machineWorkFlowDto.setTaskCategory(pmSchedule.getTaskCategory().getId());
                    machineWorkFlowDto.setTaskCategoryName(pmSchedule.getTaskCategory().getTaskCategoryName());
                    machineWorkFlowDto.setTaskGroup(pmSchedule.getTaskGroup().getId());
                    machineWorkFlowDto.setTaskGroupName(pmSchedule.getTaskGroup().getTaskGroupName());
                }
            }

            List<Panel> panelList = panelRepository.getPanelByMachineIdANDONOFFStatus(machineCluster.getId());
            if (panelList != null && !panelList.isEmpty()) {
                List<PanelDto> panelDtos = panelList.stream().map(data -> new PanelDto(data)).collect(Collectors.toList());
                machineWorkFlowDto.setPanels(panelDtos);
            }

            return new ResponseEntity<MachineWorkFlowDto>(machineWorkFlowDto, HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("404", "Machine Cluster Not Found"), HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> deleteMachineWorkFlow(String id) throws UsernameNotFoundException {
        try {
            Optional<MachineWorkFlow> machineOptional = machineWorkFlowRepository.findById(Long.parseLong(id));
            if (machineOptional.isPresent()) {
                MachineWorkFlow machine = machineOptional.get();
                machineWorkFlowRepository.delete(machine);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> getMachineWorkFlowConsumablesDetails(Pageable pageable) {
        Page<Tuple> list = machineWorkFlowRepository.getMachineWorkFlowConsumablesDetails(pageable);
        if (!list.toList().isEmpty()) {
            List<ConsumablesDto> result = list.toList().stream().map(data -> {
                ConsumablesDto consumablesDto = new ConsumablesDto(data.get("id"), data.get("bin_name"),
                        data.get("batch_no"), data.get("quantity"));
                return consumablesDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<ConsumablesDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ConsumablesDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveMaintenance(List<MachineWFMaintenanceReasonDto> req, User user) {
        List<MachineWFMaintenanceReason> list = new ArrayList<>();
        for (MachineWFMaintenanceReasonDto reqDto : req) {
            MachineWFMaintenanceReason machine = new MachineWFMaintenanceReason(reqDto);
            machine.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            machine.setCreatedBy(user.getId());
            machine.setMachineWorkFlowId(reqDto.getMachineWorkFlowId());
            list.add(machine);
        }
        mwfMaintenanceReasonRepository.saveAll(list);

        return new ResponseEntity<Status>(
                new Status("200", "Machine Work Flow Maintenance Reason save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> getMachineWorkFlowMaintenanceReasonDetails(Pageable pageable) {
        Page<MachineWFMaintenanceReason> list = mwfMaintenanceReasonRepository.machineWFMaintenanceList(pageable);
        if (!list.toList().isEmpty()) {
            List<MachineWFMaintenanceReasonDto> result = list.toList().stream().map(data -> {
                MachineWFMaintenanceReasonDto machineDto = new MachineWFMaintenanceReasonDto(data);
                return machineDto;
            }).collect(Collectors.toList());
            PageDataDto<MachineWFMaintenanceReasonDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<MachineWFMaintenanceReasonDto>>(pageData, HttpStatus.OK);
        } else {

            return new ResponseEntity<List<MachineWFMaintenanceReasonDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveBreakdown(MachineWFBreakdown request, User user) {
        Optional<MachineWorkFlow> machineOptional = machineWorkFlowRepository.findById(request.getMachineWorkFlowId());
        if (machineOptional.isPresent()) {
            MachineWorkFlow machine = machineOptional.get();
            machine.setBreakdownDate(dateUtil.getStringTODateDDMMYYYY(request.getBreakDownDate()));
            machine.setBreakDownTime(request.getBreakDownTime());
            machine.setProductionShift(request.getProductionShift());
            machine.setTimeSlab(request.getTimeSlot());
            machineWorkFlowRepository.save(machine);
            mwfBreakdownReasonRepository.deleteByMachineWorkFlowId(request.getMachineWorkFlowId());
            for (MachineWFBreakdownReasonDto req : request.getReasons()) {
                MachineWFBreakdownReason reason = new MachineWFBreakdownReason(req);
                reason.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                reason.setCreatedBy(user.getId());
                reason.setMachineWorkFlowId(request.getMachineWorkFlowId());
                MachineWFBreakdownReason re = mwfBreakdownReasonRepository.save(reason);
            }

        }


//        if (re != null) {
//            for (MachineWFBreakdownReasonDetailsDto reqDto : req.getBreakdownDetails()) {
//                MachineWFBreakdownReasonDetails breakDown = new MachineWFBreakdownReasonDetails(reqDto);
//                breakDown.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
//                breakDown.setCreatedBy(user.getId());
//                breakDown.setBreakdownReasonId(re.getId());
//                breakDown.setMachineWorkFlowId(req.getMachineWorkFlowId());
//                list.add(breakDown);
//            }
//            mwfBreakdownReasonDetailsRepository.saveAll(list);
//        }

        return new ResponseEntity<Status>(new Status("200", "Machine Work Flow Breakdown Reason save successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> machineBreakDownByMachineWorkFlowId(Long machineWorkFlowId) {
        MachineWFBreakdown machineWFBreakdown = new MachineWFBreakdown();
        Optional<MachineWorkFlow> machineOptional = machineWorkFlowRepository.findById(machineWorkFlowId);
        if (machineOptional.isPresent()) {
            MachineWorkFlow machine = machineOptional.get();
            machineWFBreakdown.setMachineWorkFlowId(machineWorkFlowId);
            machineWFBreakdown.setProductionShift(machine.getProductionShift());
            machineWFBreakdown.setTimeSlot(machine.getTimeSlab());
            machineWFBreakdown.setBreakDownTime(machine.getBreakDownTime());
            machineWFBreakdown.setBreakDownDate(dateUtil.getStringTODateF1(machine.getBreakdownDate()));
            ArrayList<MachineWFBreakdownReasonDto> list = new ArrayList<>();
            List<MachineWFBreakdownReason> reasons = mwfBreakdownReasonRepository.findByMachineWorkFlowId(machineWorkFlowId);
            for (MachineWFBreakdownReason req : reasons) {
                MachineWFBreakdownReasonDto reason = new MachineWFBreakdownReasonDto(req);
                list.add(reason);
            }
            machineWFBreakdown.setReasons(list);
            return new ResponseEntity<MachineWFBreakdown>(machineWFBreakdown, HttpStatus.OK);
        }

        return new ResponseEntity<Status>(new Status("404", "Data not found"),
                HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<?> getMachineWorkFlowBreakdownReasonDetails(Pageable pageable) {
        Page<MachineWFBreakdownReason> list = mwfBreakdownReasonRepository.machineWFBreakdownList(pageable);
        if (!list.toList().isEmpty()) {
            List<MachineWFBreakdownReasonDto> result = list.toList().stream().map(data -> {
                MachineWFBreakdownReasonDto machineDto = new MachineWFBreakdownReasonDto(data);
                return machineDto;
            }).collect(Collectors.toList());
            PageDataDto<MachineWFBreakdownReasonDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<MachineWFBreakdownReasonDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MachineWFBreakdownReasonDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getMachineWorkFlowMaintenanceReasonDetailsById(Long id) {
        Optional<MachineWFMaintenanceReason> record = mwfMaintenanceReasonRepository.findById(id);
        MachineWFMaintenanceReasonDto machineDto = new MachineWFMaintenanceReasonDto(record.get());
        return new ResponseEntity<MachineWFMaintenanceReasonDto>(machineDto, HttpStatus.OK);
    }

    public ResponseEntity<?> getMachineWorkFlowBreakdownReasonDetailsById(Long id) {
        Optional<MachineWFBreakdownReason> record = mwfBreakdownReasonRepository.findById(id);
        MachineWFBreakdownReasonDto machineDto = new MachineWFBreakdownReasonDto(record.get());
        return new ResponseEntity<MachineWFBreakdownReasonDto>(machineDto, HttpStatus.OK);
    }


    public ResponseEntity<?> machineList(User user) throws UsernameNotFoundException {

        List<MachineDropDown> machines = machineRepository.machinelist();

        List<MachineDropDownDto> machineDtos = machines.stream().map(val -> new MachineDropDownDto(val)).collect(Collectors.toList());

        return new ResponseEntity<List<MachineDropDownDto>>(machineDtos, HttpStatus.OK);

    }

    public ResponseEntity<?> getAllVehicleNames() {
        List<Machine> vehicles = machineRepository.findAllVehicles();
        if (vehicles.size() > 0) {
            List<MachineDropDownDto> response = vehicles.stream().map(machine -> {
                MachineDropDownDto m = new MachineDropDownDto();
                m.setId(machine.getId());
                m.setName(machine.getName());
                return m;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<MachineDropDownDto>>(response, HttpStatus.OK);
        }
        return new ResponseEntity<Object>(vehicles, HttpStatus.OK);
    }

    public void addIntoMachineWorkFlow(MachineWorkFlow machineWorkFlow, MachineWorkFlowDto machineWorkFlowDto, User user) {
        machineWorkFlow.setLogId(machineWorkFlowDto.getLogId());
        machineWorkFlow.setPowerSource(machineWorkFlowDto.getPowerSource());
        machineWorkFlow.setReason(machineWorkFlowDto.getReason());
        machineWorkFlow.setCStatus(machineWorkFlowDto.getCStatus());
        machineWorkFlow.setTimeSlab(machineWorkFlowDto.getTimeSlab());
        machineWorkFlow.setProductionShift(machineWorkFlowDto.getProductionShift());
        machineWorkFlow.setOperatedBy(user.getId());
        machineWorkFlow.setPowerOffReason(machineWorkFlowDto.getPowerOffReason());
        machineWorkFlow.setPowerOffReasonId(machineWorkFlowDto.getPowerOffReasonId());
        machineWorkFlow.setPowerOnTime(machineWorkFlowDto.getPowerOnTime());
        machineWorkFlow.setPowerOffTime(machineWorkFlowDto.getPowerOffTime());
        machineWorkFlow.setMeterClosingReading(machineWorkFlowDto.getMeterClosingReading());
        machineWorkFlow.setDetailedReason(machineWorkFlowDto.getDetailedReason());
        machineWorkFlow.setDetailedReasonId(machineWorkFlowDto.getDetailedReasonId());
        machineWorkFlow.setPowerOnDate(dateUtil.getStringTODateDDMMYYYY(machineWorkFlowDto.getPowerOnDate()));
        machineWorkFlow.setPowerOffDate(dateUtil.getStringTODateDDMMYYYY(machineWorkFlowDto.getPowerOffDate()));
    }
}
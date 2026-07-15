package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;

@Service
public class AttributesService {

    @Autowired
    ShiftsRepository shiftsRepository;

    @Autowired
    PatlogRepository patlogRepository;

    @Autowired
    TaskNameRepository taskNameRepository;

    @Autowired
    AttributesRepository attributes1Repository;

    @Autowired
    AttributesAssignRepository attributesRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AttributeFieldsProcessTypeOptionRepository processOptionRepository;

    
    @Autowired
    AttributeFieldsProcessTypeValueRepository processValuesRepository;


    @Autowired
    ProreportdbRepository proReportDbRepo;

    @Autowired
    KpiNameRepository kpiNameRepo;

    @Autowired
    KpiRepository kpiRepo;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    TaskGroupRepository taskGroupRepository;


    public ResponseEntity<?> getAttributesDetails(Long id) {
        Optional<AttributesAssign> getAttById = attributesRepository.findById(id);
        if (getAttById.isPresent()) {
            AttributesAssignDto aad = new AttributesAssignDto(getAttById.get());
            return new ResponseEntity<AttributesAssignDto>(aad, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteAttributes(Long id) {
        try {
            Optional<AttributesAssign> getAttById = attributesRepository.findById(id);
            if (getAttById.isPresent()) {
                AttributesAssign aa = getAttById.get();
                attributesRepository.delete(aa);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    /*public ResponseEntity<?> getAttributesStepsDetails(Long kpiNameId, Pageable pageable) {

        List<Tuple> stepsDetails = attributesStepsRepository.getAttributesStepsDetails(kpiNameId);

        List<AttributesStepsDto> result = new ArrayList<>();
        if (!stepsDetails.isEmpty()) {

            System.out.println("steps size " + stepsDetails.size());
            System.out.println("kpi name id " + kpiNameId);
            Long taskCategoryId;
            Long taskNameId;
            Long taskGroupId;
            Long productId;
            List<Tuple> tg = stepsDetails;
            for (int i = 0; i < tg.size(); i++) {
                AttributesStepsDto stepDto = new AttributesStepsDto();
                stepDto.setId(((BigInteger) tg.get(i).get("stepId")).longValue());
                stepDto.setStepNumber((Integer) tg.get(i).get("stepNumber"));
                stepDto.setKpiName(tg.get(i).get("kpiName").toString());
                stepDto.setKpiNameId(kpiNameId);
                productId = ((BigInteger) tg.get(i).get("productId")).longValue();
                stepDto.setProductMasterId(productId);
                stepDto.setProductMasterName(tg.get(i).get("productName").toString());
                stepDto.setAttibutesAssignId(((BigInteger) tg.get(i).get("attributesAssignId")).longValue());

                taskCategoryId = ((BigInteger) tg.get(i).get("taskCategoryId")).longValue();
                taskNameId = ((BigInteger) tg.get(i).get("taskNameId")).longValue();
                taskGroupId = ((BigInteger) tg.get(i).get("taskGroupId")).longValue();

                stepDto.setTaskGroupId(taskGroupId);
                stepDto.setTaskGroup(tg.get(i).get("taskGroupName").toString());

                stepDto.setTaskNameId(taskNameId);
                stepDto.setTaskName(tg.get(i).get("taskName").toString());

                stepDto.setTaskCategoryId(taskCategoryId);
                stepDto.setTaskCategory(tg.get(i).get("taskCategoryName").toString());

                List<AttributesDto> fieldsList = new ArrayList<>();

                Page<Proreportdb> getFieldsIdFromProReportDB = proReportDbRepo.getAttributesFields(taskGroupId, taskCategoryId,
                        kpiNameId, productId);
                if (getFieldsIdFromProReportDB.toList() != null) {

                    for (Proreportdb fieldDto : getFieldsIdFromProReportDB.toList()) {
                        AttributesDto fields = new AttributesDto();
                        fields.setId(fieldDto.getId());
                        fields.setName(fieldDto.getAtname());

                        fields.setTaskCategoryId(taskCategoryId);
                        fields.setTaskCategory(tg.get(i).get("taskGroupName").toString());
                        fields.setTaskNameId(taskNameId);
                        fields.setTaskName(tg.get(i).get("taskName").toString());

                        fields.setAttributeType(fieldDto.getAttype());
                        fields.setInputType(fieldDto.getInput_type());
                        fields.setFormula(fieldDto.getFormula());
                        fields.setMaxValue(fieldDto.getAttribute_max_value());
                        fields.setMinValue(fieldDto.getAttribute_min_value());
                        fields.setUnit(fieldDto.getUnits());

                        fieldsList.add(fields);
                    }

                    stepDto.setAttributeFieldsProcessType(fieldsList);
                    result.add(stepDto);

                }
            }
            return new ResponseEntity<List<AttributesStepsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AttributesStepsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }*/

    public ResponseEntity<?> getKpiNameStepsDetails(Long kpiNameId, Pageable pageable) {

        List<KpiList> stepList = kpiRepo.getKpiNameStepsDetails(kpiNameId);
        List<KpiListDto> result = new ArrayList<>();
        if (!stepList.isEmpty()) {

            System.out.println("steps size " + stepList.size());
            System.out.println("kpi name id " + kpiNameId);

            result = stepList.stream().map(stepData -> {
                KpiListDto step = new KpiListDto(stepData);
                updatesDetails(step);
                return step;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<KpiListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<KpiListDto>>(result, HttpStatus.OK);
        }
    }


    public void updatesDetails(KpiListDto step) {

        List<AttributesDto> fieldsList = new ArrayList<>();

        List<AttributesList> attributesLists = proReportDbRepo.getAttributesFields(step.getTaskGroupMapId(), step.getKpiNameId(), step.getProductMasterId());

        if (attributesLists != null) {

            List<AttributesListDto> result = attributesLists.stream().map(data -> {
                AttributesListDto fieldDto = new AttributesListDto(data);
                List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processOptionRepository.findBy(data.getId());
                if (!optionBaseds.isEmpty()) {
                    List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream().map(optionData -> new AttributeFieldsProcessTypeOptionBasedDto(optionData)).collect(Collectors.toList());
                    fieldDto.setOptions(te);
                }
                return fieldDto;
            }).collect(Collectors.toList());

            step.setAttributeFieldsProcessType(result);
        }
    }


    @Transactional()
    public ResponseEntity<?> addKpiNameSteps(ArrayList<KpiDto> reqDtos, User user) {
        try {
            for (KpiDto reqDto : reqDtos) {
                Optional<Kpi> optional = kpiRepo.findBy(reqDto.getId(), reqDto.getRefid(), reqDto.getTgid());
                if (optional.isPresent()) {
                    Kpi rsKpi = optional.get();
                    rsKpi.setDispord(reqDto.getDispord());
                    rsKpi.setSkip(reqDto.getSkip());
                    rsKpi.setActive("Y");
                    kpiRepo.save(rsKpi);
                } else {
                    Kpi rsKpi = new Kpi(reqDto);
                    kpiRepo.save(rsKpi);
                }
            }
            return new ResponseEntity<Status>(new Status("200", "KpiName steps added successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "KpiName steps not created"), HttpStatus.OK);
    }


    public ResponseEntity<?> searchAttributes(Long tgId, String attName) {
        List<AttributesDto> attributesDtos = new ArrayList<>();
        try {
            List<Attributes> attributes = attributes1Repository.getAttributes(tgId, attName);
            if (!attributes.isEmpty()) {
                attributesDtos = attributes.stream().map(data -> {
                    AttributesDto attributesDto = new AttributesDto(data);
                    return attributesDto;
                }).collect(Collectors.toList());
            }
            return new ResponseEntity<List<AttributesDto>>(attributesDtos, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<AttributesDto>>(new ArrayList<>(), HttpStatus.OK);
    }

    public ResponseEntity<?> searchAllAttributes(String attName) {
        List<AttributesDto> attributesDtos = new ArrayList<>();
        try {
            List<FormulaAttributesList> attributes = attributes1Repository.getAttributes(attName);
            if (!attributes.isEmpty()) {
                attributesDtos = attributes.stream().map(data -> {
                    AttributesDto attributesDto = new AttributesDto();
                    attributesDto.setId(data.getAttId());
                    attributesDto.setName(data.getAttName());
                    return attributesDto;
                }).collect(Collectors.toList());
            }
            return new ResponseEntity<List<AttributesDto>>(attributesDtos, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<AttributesDto>>(new ArrayList<>(), HttpStatus.OK);
    }


    public ResponseEntity<?> saveAttributeOption(List<String> strings, Long attId) {
        try {
            List<AttributeFieldsProcessTypeOptionBased> optList = new ArrayList<>();
            for (String optionV : strings) {

                AttributeFieldsProcessTypeOptionBased opt = new AttributeFieldsProcessTypeOptionBased();
                opt.setAttributes_id(attId);
                opt.setOptions(optionV);
                optList.add(opt);
            }
            if (optList.size() > 0) {
                processOptionRepository.saveAll(optList);
            }

            return new ResponseEntity<Status>(new Status("200", "Attributes Option details updated successfully!!!"),
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Attributes details not updated"), HttpStatus.OK);
    }


    public ResponseEntity<?> deleteAttributeOption(AttributeFieldsProcessTypeOptionBasedDto option) {
        try {

            AttributeFieldsProcessTypeOptionBased opt = new AttributeFieldsProcessTypeOptionBased(option);
            processOptionRepository.save(opt);

            return new ResponseEntity<Status>(new Status("200", "Attributes Option deleted successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Attributes details not deleted"), HttpStatus.OK);
    }


    public ResponseEntity<?> getAllAttributes(Long kpiId, String batchNo, String entryType) {
        try {      	
        	List<AttributesList>  optList = proReportDbRepo.getAttributesFieldList(kpiId, batchNo, entryType);
        	 List<AttributesListDto> result = optList.stream().map(data -> {
                AttributesListDto attributesDto = new AttributesListDto(data);
                if (attributesDto.getAttributesResValue() == null) {
                    List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processOptionRepository.findBy(data.getId());
                    if (!optionBaseds.isEmpty()) {
                        List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream().map(data1 -> new AttributeFieldsProcessTypeOptionBasedDto(data1)).collect(Collectors.toList());
                        attributesDto.setOptions(te);
                    }
                }
                return attributesDto;
            }).collect(Collectors.toList());

            return new ResponseEntity<List<AttributesListDto>>(result, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<AttributesListDto>>(new ArrayList<>(), HttpStatus.OK);
    }


    public ResponseEntity<?> saveAttributesFields(List<AttributesDto> reqDtos, User user) {
        try {
            for (AttributesDto reqDto : reqDtos) {
                Attributes temp = new Attributes(reqDto);
                temp.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                temp.setCreatedBy(user.getId());
                Attributes attributeDet = attributes1Repository.save(temp);
                List<AttributeFieldsProcessTypeOptionBased> optList = new ArrayList<>();
                if (reqDto.getOptions() != null) {
                    for (AttributeFieldsProcessTypeOptionBasedDto optionsValues : reqDto.getOptions()) {
                        AttributeFieldsProcessTypeOptionBased opt = new AttributeFieldsProcessTypeOptionBased(optionsValues);
                        opt.setAttributes_id(attributeDet.getId());
                        optList.add(opt);
                    }
                    processOptionRepository.saveAll(optList);
                }
            }
            return new ResponseEntity<Status>(new Status("200", "Attributes details saved successfully!!!"),
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Attributes details not created"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateAttributesFields(List<AttributesDto> reqDtos, User user) {
        try {
            for (AttributesDto reqDto : reqDtos) {
                Attributes temp = new Attributes(reqDto);
                temp.setId(reqDto.getId());
                temp.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                temp.setUpdatedBy(user.getId());
                Attributes attributeDet = attributes1Repository.save(temp);
                List<AttributeFieldsProcessTypeOptionBased> optList = new ArrayList<>();
                if (reqDto.getOptions() != null) {
                    for (AttributeFieldsProcessTypeOptionBasedDto optionsValues : reqDto.getOptions()) {
                        AttributeFieldsProcessTypeOptionBased opt = new AttributeFieldsProcessTypeOptionBased(optionsValues);
                        opt.setId(optionsValues.getId() != null ? optionsValues.getId() : null);
                        opt.setAttributes_id(attributeDet.getId());
                        optList.add(opt);
                    }
                    processOptionRepository.saveAll(optList);
                }
            }
            return new ResponseEntity<Status>(new Status("200", "Attributes details updated successfully!!!"),
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Attributes details not updated"), HttpStatus.OK);
    }

    public ResponseEntity<?> getAttributesDetails(Long taskNameId, Long taskCategoryId, Pageable pageable) {
        Page<Attributes> list = attributes1Repository.getAttributesStepsDetails(taskNameId, taskCategoryId, pageable);
        if (!list.toList().isEmpty()) {
            List<AttributesDto> result = list.toList().stream().map(data -> {
                AttributesDto resDto = new AttributesDto(data);
                return resDto;
            }).collect(Collectors.toList());
            PageDataDto<AttributesDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<AttributesDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AttributesDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }



  /*  public ResponseEntity<?> getMaintainaceAttributesDetailsFoTaskCategoryAndGroup(Long taskGroupId, Long taskCategoryId) {
        List<Long> taskNameIds = taskGroupRepository.findTaskNameByTaskCategoryAndGroup(taskCategoryId, taskGroupId);
        if (taskNameIds != null && taskNameIds.size() > 0) {
            List<Attributes> list = attributes1Repository.getAttributesStepsDetailsForTaskName(taskNameIds);
            if (!list.isEmpty()) {
                List<AttributesMaintenanceDto> result = list.stream().map(data -> {
                    AttributesMaintenanceDto resDto = new AttributesMaintenanceDto();
                    resDto.setTaskNameId(data.getTaskNameDet().getId());
                    resDto.setTaskName(data.getTaskNameDet().getTaskName());
                    resDto.setTaskCategoryId(data.getTaskCategory().getId());
                    resDto.setTaskCategory(data.getTaskCategory().getTaskCategoryName());
                    List<AttributeFieldsMaintenanceTypeDto> attributesList = new ArrayList<>();
                    List<AttributeFieldsMaintenanceType> maintainList = attributeFieldsMaintenanceRepository.findByAttributeId(data.getId());
                    for (AttributeFieldsMaintenanceType type : maintainList) {
                        AttributeFieldsMaintenanceTypeDto dto = new AttributeFieldsMaintenanceTypeDto();
                        dto.setId(type.getId());
                        dto.setConsumptions(type.getConsumptions());
                        dto.setPurposeBenefit(type.getPurposeBenefit());
                        dto.setWorkSafetyMeasure(type.getWorkSafetyMeasure());
                        dto.setValueType(type.getValueType());
                        dto.setFieldName(type.getFieldName());
                        dto.setCreatedBy(type.getCreatedBy());
                        dto.setCreated_date(type.getCreated_date());
                        List<AttributeFieldsMaintenanceTypeValueOptionBasedDto> maintenanceTypeValueOptionBased = new ArrayList<>();
                        for (AttributeFieldsMaintenanceTypeValueOptionBased optionBased : type.getMaintenanceType()) {
                            AttributeFieldsMaintenanceTypeValueOptionBasedDto basedDto = new AttributeFieldsMaintenanceTypeValueOptionBasedDto(optionBased);
                            maintenanceTypeValueOptionBased.add(basedDto);
                        }
                        dto.setMaintenanceTypeValueOptionBased(maintenanceTypeValueOptionBased);
                        attributesList.add(dto);
                    }
                    resDto.setAttributesList(attributesList);
                    return resDto;
                }).collect(Collectors.toList());

                return new ResponseEntity<List<AttributesMaintenanceDto>>(result, HttpStatus.OK);
            }
        }
        return new ResponseEntity<List<AttributesMaintenanceDto>>(new ArrayList<>(), HttpStatus.OK);
    }*/




/*


    public ResponseEntity<?> getAttributesInProcessData(Long kpiNameId) {


        System.out.println(" tamil : " + kpiNameId);
        List<Tuple> res = inProcessRepo.getAllDetails(
                kpiNameId
        );
        List<AttributeFieldsInProcessDto> list = new ArrayList<>();
        if (!res.isEmpty()) {
            for (int i = 0; i < res.size(); i++) {
                AttributeFieldsInProcessDto patlogDto = new AttributeFieldsInProcessDto(res.get(i).get("patId"), res.get(i).get("stepId"), res.get(i).get("stepNumber"),
                        res.get(i).get("taskCategoryId"), res.get(i).get("taskCategoryName"),
                        res.get(i).get("taskNameId"), res.get(i).get("taskName"), res.get(i).get("taskGroupId"),
                        res.get(i).get("taskGroupName"), res.get(i).get("kpiNameId"), res.get(i).get("kpiName"),
                        res.get(i).get("productId"), res.get(i).get("productName"), res.get(i).get("attributeId"),
                        res.get(i).get("attributeName"), res.get(i).get("attributeValue1"), res.get(i).get("attributeValue2"));


                list.add(patlogDto);
            }
            System.out.println(" res tamil : " + list.size());
            return new ResponseEntity<List<AttributeFieldsInProcessDto>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AttributeFieldsInProcessDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }*/

    public ResponseEntity<?> getAllAssignAttributesDetails1(User user, Pageable pageable) {
        Page<AttributesAssign> list = attributesRepository.getAllAssignAttributesDetailsAlone(pageable);
        if (!list.toList().isEmpty()) {
            List<AttributesAssignDto> result = list.toList().stream().map(data -> {
                AttributesAssignDto resDto = new AttributesAssignDto();
                resDto.setKpiName(data.getKpiName().getName());
                resDto.setKpiNameId(data.getKpiName().getId());
                resDto.setId(data.getId());
                resDto.setProcess(data.getProcess());
                resDto.setModeOfEntry(data.getModeOfEntry());
                resDto.setProductId(data.getProduct().getId());
                resDto.setProductName(data.getProduct().getProductName());
                resDto.setTaskType(data.getTaskType());
                return resDto;
            }).collect(Collectors.toList());
            PageDataDto<AttributesAssignDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<AttributesAssignDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<AttributesAssignDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getAllKpiName(String kpiName, String modeOfEntry, User user, Pageable pageable) {
        Page<Kpiname> list = kpiNameRepo.getAllKpiName(kpiName, modeOfEntry, pageable);
        if (!list.toList().isEmpty()) {
            List<KpinameDto> result = list.toList().stream().map(data -> {
                KpinameDto resDto = new KpinameDto();
                resDto.setName(data.getName());
                resDto.setId(data.getId());
                resDto.setType(data.getType());
                resDto.setAtType(data.getAtType());
                if (data.getProduct() != null) {
                    resDto.setProId(data.getProduct().getId());
                    resDto.setProductName(data.getProduct().getProductName());
                }
                return resDto;
            }).collect(Collectors.toList());
            PageDataDto<KpinameDto> pageData = new PageDataDto<>(result, pageable, list);
            return new ResponseEntity<PageDataDto<KpinameDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<KpinameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteAttributesFields(Long id) {
        try {
            Optional<Attributes> getAttById = attributes1Repository.findById(id);
            if (getAttById.isPresent()) {
                Attributes aa = getAttById.get();
                attributes1Repository.delete(aa);
            }

            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }

    }


    public ResponseEntity<?> saveProreportDbAttributes(List<ProreprtdbDto> reqDtos, User user) {
        try {
            for (ProreprtdbDto reqDto : reqDtos) {
                Proreportdb temp = new Proreportdb(reqDto);
                Long attId;
                if (reqDto.getId() != null) {
                    temp.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    temp.setUpdatedBy(user.getId());
                } else {
                    temp.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    temp.setCreatedBy(user.getId());
                }
                Attributes att = null;

                if (reqDto.getAtid() != null) {
                    Optional<TaskGroup> taskGroup = taskGroupRepository.findById(reqDto.getTgid());
                    if (taskGroup.isPresent()) {
                        if (taskGroup.get().getTaskCategory().getId() != null && taskGroup.get().getTaskName().getId() != null) {
                            Optional<Attributes> attributes = attributes1Repository.getAttributes(taskGroup.get().getTaskName().getId(), taskGroup.get().getTaskCategory().getId(), reqDto.getAtid());
                            if (attributes.isPresent()) {
                                att = attributes.get();
                                att.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                                att.setUpdatedBy(user.getId());
                                att.setDispOrder(reqDto.getDispOrder());
                            } else {
                                reqDto.setTaskid(taskGroup.get().getTaskName().getId());
                                reqDto.setTcat(taskGroup.get().getTaskCategory().getId());
                            }
                        }
                    }
                } else {
                    Optional<TaskGroup> taskGroup = taskGroupRepository.findById(reqDto.getTgid());
                    if (taskGroup.isPresent()) {
                        reqDto.setTaskid(taskGroup.get().getTaskName().getId());
                        reqDto.setTcat(taskGroup.get().getTaskCategory().getId());
                    }
                }

                if (att == null) {
                    att = new Attributes();
                    att.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    att.setCreatedBy(user.getId());
                    att.setName(reqDto.getAtname());
                    att.setDispOrder(reqDto.getDispOrder());
                    if (reqDto.getTcat() != null) {
                        TaskCategory tn = new TaskCategory();
                        tn.setId(reqDto.getTcat());
                        att.setTaskCategory(tn);
                    }
                    if (reqDto.getTaskid() != null) {
                        TaskName tn = new TaskName();
                        tn.setId(reqDto.getTaskid());
                        att.setTaskNameDet(tn);
                    }
                }

                if (reqDto.getPurpose_benefit() != null) {
                    att.setPurpose_benefit(reqDto.getPurpose_benefit());
                }
                if (reqDto.getWorkinst_saftmeasure() != null) {
                    att.setWorkinst_saftmeasure(reqDto.getWorkinst_saftmeasure());
                }

                Attributes res = attributes1Repository.save(att);
                attId = res.getId();
                temp.setAtid(attId);

                if (reqDto.getOptions() != null) {
                    List<AttributeFieldsProcessTypeOptionBased> list = new ArrayList<>();
                    for (AttributeFieldsProcessTypeOptionBasedDto op : reqDto.getOptions()) {

                        AttributeFieldsProcessTypeOptionBased opt = new AttributeFieldsProcessTypeOptionBased();
                        if (op.getId() != null) {
                            opt.setId(op.getId());
                        }
                        opt.setAttributes_id(res.getId());
                        opt.setOptions(op.getOptions());
                        list.add(opt);
                    }
                    processOptionRepository.saveAll(list);
                }

                proReportDbRepo.save(temp);

            }
            return new ResponseEntity<Status>(new Status("200", "Attributes details saved successfully!!!"),
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Attributes details not created"), HttpStatus.OK);
    }

    public ResponseEntity<?> deleteProreportDbAttributes(Long id) {
        try {
            Optional<Proreportdb> getProReportDbId = proReportDbRepo.findById(id);
            if (getProReportDbId.isPresent()) {
                Proreportdb pr = getProReportDbId.get();
                proReportDbRepo.delete(pr);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> deleteKpiNameSteps(Long id) {
        try {
            Optional<Kpi> getKpiId = kpiRepo.findById(id);
            if (getKpiId.isPresent()) {
                Kpi pr = getKpiId.get();
                pr.setActive("N");
                Kpi rs = kpiRepo.save(pr);
                removeKpiAttributes(rs);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void removeKpiAttributes(Kpi kpi) {
        List<Proreportdb> proreportdbs = proReportDbRepo.findAttributesFields(kpi.getTgid(), kpi.getId(), kpi.getRefid());
        if (!proreportdbs.isEmpty()) {
            proReportDbRepo.deleteAll(proreportdbs);
        }
    }


    public ResponseEntity<?> getMaintenanceList(Long taskCategoryId, Long taskId, String taskCategory, String taskName, Pageable pageable) {
        Page<MaintenanceList> maintenanceList = taskGroupRepository.findAllMaintenanceList(taskId, taskCategoryId, taskCategory, taskName, pageable);
        if (!maintenanceList.isEmpty()) {
            List<MaintenanceListDto> result = maintenanceList.toList().stream().map(data -> new MaintenanceListDto(data)).collect(Collectors.toList());
            PageDataDto<MaintenanceListDto> pageData = new PageDataDto<>(result, pageable, maintenanceList);
            return new ResponseEntity<PageDataDto<MaintenanceListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MaintenanceListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getMaintenanceAttributesDetails(Long taskId, Long taskCategoryId, Long taskGroupMapId) {
        Optional<MaintenanceList> optional = taskGroupRepository.findAllMaintenanceDetails(taskId, taskCategoryId, taskGroupMapId);
        if (optional.isPresent()) {
            MaintenanceListDto result = new MaintenanceListDto(optional.get());

            return new ResponseEntity<MaintenanceListDto>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<MaintenanceListDto>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> getMaintenanceAttributes(Long taskMapId, Long taskId, Long taskCategoryId) {
        List<MaintenanceAttributesList> maintenanceList = proReportDbRepo.getMaintenanceAttributesFields(taskMapId);
        if (!maintenanceList.isEmpty()) {
            List<MaintenanceAttributesListDto> result = maintenanceList.stream().map(data -> {
                MaintenanceAttributesListDto res = new MaintenanceAttributesListDto(data);
                List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processOptionRepository.findBy(data.getId());
                if (!optionBaseds.isEmpty()) {
                    List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream().map(optionData -> new AttributeFieldsProcessTypeOptionBasedDto(optionData)).collect(Collectors.toList());
                    res.setOptions(te);
                }
                List<AttributeFieldsProcessTypeValueBased> valuesBaseds = processValuesRepository.findBy(data.getId());
                if (!valuesBaseds.isEmpty()) {
                    List<AttributeFieldsProcessTypeValueBasedDto> te = valuesBaseds.stream().map(optionData -> new AttributeFieldsProcessTypeValueBasedDto(optionData)).collect(Collectors.toList());
                    res.setValues(te);
                }
                return res;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<MaintenanceAttributesListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MaintenanceAttributesListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> pmScheduleMaintenanceAttributesList(Long taskGroupId, Long taskId, Long taskCategoryId) {

        List<MaintenanceAttributesList> maintenanceList = proReportDbRepo.getMaintenanceAttributesFieldList(taskId, taskCategoryId, taskGroupId);
        if (!maintenanceList.isEmpty()) {
            List<MaintenanceAttributesListDto> result = maintenanceList.stream().map(data -> {
                MaintenanceAttributesListDto res = new MaintenanceAttributesListDto(data);
                List<AttributeFieldsProcessTypeOptionBased> optionBaseds = processOptionRepository.findBy(data.getId());
                if (!optionBaseds.isEmpty()) {
                    List<AttributeFieldsProcessTypeOptionBasedDto> te = optionBaseds.stream().map(optionData -> new AttributeFieldsProcessTypeOptionBasedDto(optionData)).collect(Collectors.toList());
                    res.setOptions(te);
                }

                List<AttributeFieldsProcessTypeValueBased> valuesBaseds = processValuesRepository.findBy(data.getId());
                if (!optionBaseds.isEmpty()) {
                    List<AttributeFieldsProcessTypeValueBasedDto> te = valuesBaseds.stream().map(optionData -> new AttributeFieldsProcessTypeValueBasedDto(optionData)).collect(Collectors.toList());
                    res.setValues(te);
                }

                return res;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<MaintenanceAttributesListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MaintenanceAttributesListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> updateList() {
        List<TaskGroup> maintenanceList = taskGroupRepository.findAllMaintenanceDetails1();
        if (!maintenanceList.isEmpty()) {
            for (TaskGroup taskGroup : maintenanceList) {
                if (taskGroup != null && taskGroup.getTaskName().getTaskCategroyDet() != null)
                    System.out.println("TaskGroup ###### " + taskGroup.getTaskName());
                List<Attributes> attributes = attributes1Repository.getAllAttributes(taskGroup.getTaskName().getId(), taskGroup.getTaskCategory().getId());
                if (!attributes.isEmpty()) {
                    for (Attributes attributes1 : attributes) {
                        System.out.println("Attributes ###### " + attributes1.getName());
                        Proreportdb proreportdb = new Proreportdb();
                        proreportdb.setAtid(attributes1.getId());
                        proreportdb.setTgid(taskGroup.getId());
                        proreportdb.setAtname(attributes1.getName());
                        proReportDbRepo.save(proreportdb);
                    }
                }
            }

            return new ResponseEntity<String>("update successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<String>("update successfully", HttpStatus.OK);
        }
    }


    public ResponseEntity<?> formulaAllAttributes(Long kpiNameId, Long id, Long tgId, String attName) {
        List<FormulaAttributesListDto> attributesDtos = new ArrayList<>();
        try {
            List<FormulaAttributesList> attributes = proReportDbRepo.formulaAttributesList(kpiNameId, id, tgId, attName);
            if (!attributes.isEmpty()) {
                attributesDtos = attributes.stream().map(data -> {
                    FormulaAttributesListDto attributesDto = new FormulaAttributesListDto(data);
                    return attributesDto;
                }).collect(Collectors.toList());
            }
            return new ResponseEntity<List<FormulaAttributesListDto>>(attributesDtos, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<FormulaAttributesListDto>>(new ArrayList<>(), HttpStatus.OK);
    }

}

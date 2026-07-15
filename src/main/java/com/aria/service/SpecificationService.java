package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.utils.DateUtil;



@Service
public class SpecificationService {

    @Autowired
    SpecificationRepository specificationRepo;

    @Autowired
    SpecificationParameterMappingRepository specificationParameterMappingRepo;

    @Autowired
    SpecificationParameterRepository specificationParameterRepo;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    PurchaseExpenseRepository purchaseExpenseRepository;

    @Autowired
    LotRepository lotRepository;

    // Product Specification

    public ResponseEntity<?> listAllSpecificationParameters(User user, Long specType) {
        List<Specification_parameter> list = specificationParameterRepo.findAllActive(specType);
        System.out.println(list.size());
        if (!list.isEmpty()) {
            List<SpecificationParameterDTO> result = list.stream().map(data -> {
                SpecificationParameterDTO generatorDto = new SpecificationParameterDTO(data);
                return generatorDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SpecificationParameterDTO>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SpecificationParameterDTO>>(new ArrayList<SpecificationParameterDTO>(),
                    HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveSpecification(SpecificationDto reqDto, User user) {
        try {
            Specification specification = new Specification(reqDto);
            specification.setBranch(user.getBranch_id());
            specification.setDepartment(user.getDepartment().getId());
            specification.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            specification.setCreatedBy(user.getId());
            specification.setOrganisation(user.getOrg_id());
            List<Specification> specifications = specificationRepo.ListSpecification(reqDto.getOrg(), reqDto.getRefId(),
                    reqDto.getSpecType());

            if (specifications.size() > 0) {
                specification.setId(specifications.get(0).getId());
            }
            Specification spec = specificationRepo.save(specification);
            List<Specification_parameter_mapping> specification_parameter_mappings = new ArrayList<Specification_parameter_mapping>();

            for (SpecificationParameterDTO req : reqDto.getParameters()) {

                if (java.util.Objects.nonNull(req) && java.util.Objects.nonNull(req.getParameterMapping())) {
                    for (SpecificationParameterMappingDto mapping : req.getParameterMapping()) {
                        mapping.setSpecification_id(spec.getId());
                        mapping.setParamter_type(req.getParameter_type());
                        mapping.setParamter_option_type(req.getParamter_option_type());
                        mapping.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                        mapping.setCreatedBy(user.getId());
                        mapping.setActive(1L);
                        Specification_parameter_mapping parameter_mapping = new Specification_parameter_mapping(mapping, req);
                        specification_parameter_mappings.add(parameter_mapping);
                    }

                }

            }
            if (specification_parameter_mappings.size() > 0) {
                specificationParameterMappingRepo.saveAll(specification_parameter_mappings);

            }
            return new ResponseEntity<Status>(new Status("200", "Specification details saved successfully!!!"),
                    HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "Specification details not created"), HttpStatus.BAD_REQUEST);
    }

    public ResponseEntity<?> listAllSpecificationDetails(User user, Pageable pageable, String name,
                                                         Long specType, String fromDate, String toDate) {

        Date fromDt = null;
        Date toDt = null;


        if (java.util.Objects.nonNull(fromDate)) {
            fromDt = dateUtil.getStringTODateWk(fromDate);
        }
        if (java.util.Objects.nonNull(toDate)) {
            toDt = dateUtil.getStringTODateWk(toDate);
        }

        if (specType != null) {
            if (specType == 28) {
                Page<SpecificationDetails> list = specificationRepo.ListSpecificationforProduct(name, fromDt, toDt,
                        pageable);
                if (!list.toList().isEmpty()) {
                    List<SpecificationDetailsDto> result = list.toList().stream().map(data -> {
                        SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                        return generatorDto;
                    }).collect(Collectors.toList());
                    PageDataDto<SpecificationDetailsDto> pageData = new PageDataDto<>(result, pageable, list);
                    return new ResponseEntity<PageDataDto<SpecificationDetailsDto>>(pageData, HttpStatus.OK);
                }
            } else if (specType == 29) {
                Page<SpecificationDetails> list = specificationRepo.ListSpecificationforMachine(name, fromDt, toDt,
                        pageable);
                if (!list.toList().isEmpty()) {
                    List<SpecificationDetailsDto> result = list.toList().stream().map(data -> {
                        SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                        return generatorDto;
                    }).collect(Collectors.toList());
                    PageDataDto<SpecificationDetailsDto> pageData = new PageDataDto<>(result, pageable, list);
                    return new ResponseEntity<PageDataDto<SpecificationDetailsDto>>(pageData, HttpStatus.OK);
                }
            } else if (specType == 30) {
                Page<SpecificationDetails> list = specificationRepo.ListSpecificationforExpense(name, fromDt, toDt,
                        pageable);
                if (!list.toList().isEmpty()) {
                    List<SpecificationDetailsDto> result = list.toList().stream().map(data -> {
                        SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                        return generatorDto;
                    }).collect(Collectors.toList());
                    PageDataDto<SpecificationDetailsDto> pageData = new PageDataDto<>(result, pageable, list);
                    return new ResponseEntity<PageDataDto<SpecificationDetailsDto>>(pageData, HttpStatus.OK);
                }
            } else if (specType == 31) {
                Page<SpecificationDetails> list = specificationRepo.ListSpecificationforAMCExpense(name, fromDt, toDt,
                        pageable);
                if (!list.toList().isEmpty()) {
                    List<SpecificationDetailsDto> result = list.toList().stream().map(data -> {
                        SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                        return generatorDto;
                    }).collect(Collectors.toList());
                    PageDataDto<SpecificationDetailsDto> pageData = new PageDataDto<>(result, pageable, list);
                    return new ResponseEntity<PageDataDto<SpecificationDetailsDto>>(pageData, HttpStatus.OK);
                }
            }
        }
        return new ResponseEntity<PageDataDto<SpecificationDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);

    }

    public ResponseEntity<?> listAllSpecificationDetails( User user, String productName, Long specType, String fromDate, String toDate,
                                                          String standingOrder, String standingOrderAuth) {

        Date fromDt = null;
        Date toDt = null;


        if (java.util.Objects.nonNull(fromDt)) {
            fromDt = dateUtil.getStringTODateDDMMYYYY(fromDate);
        }
        if (java.util.Objects.nonNull(toDt)) {
            toDt = dateUtil.getStringTODateDDMMYYYY(toDate);
        }
        if (specType == 28) {
            List<SpecificationDetails> list = specificationRepo.ListSpecificationForProductList(productName,  standingOrder , standingOrderAuth , fromDt, toDt  );
            List<SpecificationDetailsDto> result = list.stream().map(data -> {
                SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                if(generatorDto.getRefId() != null) {
                String lotValue = lotRepository.findBy(generatorDto.getRefId());
                generatorDto.setLotList(lotValue);
                }
                
                return generatorDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SpecificationDetailsDto>>(result, HttpStatus.OK);
        } else if (specType == 29) {
            List<SpecificationDetails> list = specificationRepo.ListSpecificationforMachine(productName, fromDt, toDt);
            List<SpecificationDetailsDto> result = list.stream().map(data -> {
                SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                return generatorDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SpecificationDetailsDto>>(result, HttpStatus.OK);

        } else if (specType == 30) {
            List<SpecificationDetails> list = specificationRepo.ListSpecificationforExpense(productName, fromDt, toDt);
            List<SpecificationDetailsDto> result = list.stream().map(data -> {
                SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                return generatorDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SpecificationDetailsDto>>(result, HttpStatus.OK);
        } else if (specType == 31) {
            List<SpecificationDetails> list = specificationRepo.ListSpecificationforAMCExpense(productName, fromDt, toDt);
            List<SpecificationDetailsDto> result = list.stream().map(data -> {
                SpecificationDetailsDto generatorDto = new SpecificationDetailsDto(data);
                return generatorDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<SpecificationDetailsDto>>(result, HttpStatus.OK);

        }

        return new ResponseEntity<PageDataDto<SpecificationDetailsDto>>(new PageDataDto<>(), HttpStatus.OK);

    }

    public ResponseEntity<SpecificationDto> getSpecificationDetails(Long id, Long specType) throws UsernameNotFoundException {

        if (specType == 28) {
            SpecificationDetails specificationDetails = specificationRepo.findbyProductSpecificationId(id);
            List<SpecificationParameterList> parameter_mappings = specificationParameterMappingRepo.findBySpecification(id);
            if (specificationDetails != null && specificationDetails.getId() != null) {
                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> list = parameter_mappings.stream().map(data -> {
                    SpecificationParameterDTO specificationParameterDTO = new SpecificationParameterDTO(data);
                    updateSpecParameterMapping(specificationDto, specificationParameterDTO);
                    return specificationParameterDTO;
                }).collect(Collectors.toList());
                specificationDto.setParameters(list);
                return new ResponseEntity<SpecificationDto>(specificationDto, HttpStatus.OK);
            }

        } else if (specType == 29) {
            SpecificationDetails specificationDetails = specificationRepo.findbyMachineSpecificationId(id);
            List<SpecificationParameterList> parameter_mappings = specificationParameterMappingRepo.findBySpecification(id);
            if (specificationDetails != null && specificationDetails.getId() != null) {
                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> list = parameter_mappings.stream().map(data -> {
                    SpecificationParameterDTO specificationParameterDTO = new SpecificationParameterDTO(data);
                    updateSpecParameterMapping(specificationDto, specificationParameterDTO);
                    return specificationParameterDTO;
                }).collect(Collectors.toList());
                specificationDto.setParameters(list);
                return new ResponseEntity<SpecificationDto>(specificationDto, HttpStatus.OK);
            }

        } else if (specType == 30) {
            SpecificationDetails specificationDetails = specificationRepo.findbyExpenseSpecificationId(id);
            List<SpecificationParameterList> parameter_mappings = specificationParameterMappingRepo.findBySpecification(id);
            if (specificationDetails != null && specificationDetails.getId() != null) {
                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> list = parameter_mappings.stream().map(data -> {
                    SpecificationParameterDTO specificationParameterDTO = new SpecificationParameterDTO(data);
                    updateSpecParameterMapping(specificationDto, specificationParameterDTO);
                    return specificationParameterDTO;
                }).collect(Collectors.toList());
                specificationDto.setParameters(list);
                return new ResponseEntity<SpecificationDto>(specificationDto, HttpStatus.OK);
            }

        } else if (specType == 31) {
            System.out.println("id === "+id);
            SpecificationDetails specificationDetails = specificationRepo.findbyAMCExpenseSpecificationId(id);
            List<SpecificationParameterList> parameter_mappings = specificationParameterMappingRepo.findBySpecification(id);
            if (specificationDetails != null && specificationDetails.getId() != null) {
                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> list = parameter_mappings.stream().map(data -> {
                    SpecificationParameterDTO specificationParameterDTO = new SpecificationParameterDTO(data);
                    updateSpecParameterMapping(specificationDto, specificationParameterDTO);
                    return specificationParameterDTO;
                }).collect(Collectors.toList());
                specificationDto.setParameters(list);
                return new ResponseEntity<SpecificationDto>(specificationDto, HttpStatus.OK);
            }
        }
        return new ResponseEntity<SpecificationDto>(new SpecificationDto(), HttpStatus.OK);

    }


    public void updateSpecParameterMapping(SpecificationDto specificationDto, SpecificationParameterDTO specificationParameterDTO) {

        List<Specification_parameter_mapping> parameter_mappings = specificationParameterMappingRepo.findBySpecification(specificationDto.getId(), specificationParameterDTO.getId());
        if (!parameter_mappings.isEmpty()) {
            List<SpecificationParameterMappingDto> list = parameter_mappings.stream().map(data -> {
                SpecificationParameterMappingDto specificationParameterMappingDto = new SpecificationParameterMappingDto(data);
                return specificationParameterMappingDto;
            }).collect(Collectors.toList());
            specificationParameterDTO.setParameterMapping(list);
        } else {
            specificationParameterDTO.setParameterMapping(new ArrayList<>());
        }
    }

    public ResponseEntity<?> deleteSpecification(User user, Long specId) {
        Optional<Specification> spec = specificationRepo.findById(specId);
        if (spec.isPresent()) {

            List<Specification_parameter_mapping> parameter_mappings = specificationParameterMappingRepo
                    .findBySpecificationId(specId);
            specificationParameterMappingRepo.deleteAll(parameter_mappings);
            specificationRepo.delete(spec.get());
            return new ResponseEntity<Status>(new Status("200", "Deleted Successfully"), HttpStatus.OK);

        } else {
            return new ResponseEntity<Status>(new Status("404", "Not Found"), HttpStatus.NOT_FOUND);
        }
    }

    public SpecificationDto getSpecificationDetailsByProdId(Long prodId, Long specType) throws UsernameNotFoundException {
        if (specType == 28) {
            SpecificationDetails specificationDetails = specificationRepo.findbyProductSpecificationIdAndProdId(prodId);
            if (specificationDetails != null) {
                List<Specification_parameter_mapping> parameter_mappings = specificationParameterMappingRepo
                        .findBySpecificationId(specificationDetails.getId());

                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> parameterDTOs = new ArrayList<SpecificationParameterDTO>();
                List<SpecificationParameterMappingDto> specificationParameterMappingDtos = new ArrayList<SpecificationParameterMappingDto>();
                SpecificationParameterDTO parameterDTO = new SpecificationParameterDTO();

                Long prevParamid = 0L;
                Specification_parameter_mapping prevmapping = new Specification_parameter_mapping();
                for (Specification_parameter_mapping mapping : parameter_mappings) {
                    SpecificationParameterMappingDto specificationParameterMappingDto = new SpecificationParameterMappingDto(mapping);
                    if (!prevParamid.equals(mapping.getParamter().getId())) {
                        if (!specificationParameterMappingDtos.isEmpty()) {
                            parameterDTO = new SpecificationParameterDTO(prevmapping);
                            parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                            parameterDTOs.add(parameterDTO);
                            specificationParameterMappingDtos = new ArrayList<>();
                        }
                    }
                    specificationParameterMappingDtos.add(specificationParameterMappingDto);
                    prevmapping = mapping;
                    prevParamid = mapping.getParamter().getId();

                }
                if (!specificationParameterMappingDtos.isEmpty()) {
                    parameterDTO = new SpecificationParameterDTO(prevmapping);
                    parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                    parameterDTOs.add(parameterDTO);
                }

                specificationDto.setParameters(parameterDTOs);

                return specificationDto;
            }

        } else if (specType == 29) {
            SpecificationDetails specificationDetails = specificationRepo.findbyMachineSpecificationIdAndProdId(prodId);
            if (specificationDetails != null) {
                List<Specification_parameter_mapping> parameter_mappings = specificationParameterMappingRepo
                        .findBySpecificationId(specificationDetails.getId());

                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> parameterDTOs = new ArrayList<SpecificationParameterDTO>();
                List<SpecificationParameterMappingDto> specificationParameterMappingDtos = new ArrayList<SpecificationParameterMappingDto>();
                SpecificationParameterDTO parameterDTO = new SpecificationParameterDTO();

                Long prevParamid = 0L;
                Specification_parameter_mapping prevmapping = new Specification_parameter_mapping();

                for (Specification_parameter_mapping mapping : parameter_mappings) {
                    SpecificationParameterMappingDto specificationParameterMappingDto = new SpecificationParameterMappingDto(mapping);
                    if (!prevParamid.equals(mapping.getParamter().getId())) {
                        if (!specificationParameterMappingDtos.isEmpty()) {
                            parameterDTO = new SpecificationParameterDTO(prevmapping);
                            parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                            parameterDTOs.add(parameterDTO);
                            specificationParameterMappingDtos = new ArrayList<>();
                        }
                    }
                    specificationParameterMappingDtos.add(specificationParameterMappingDto);
                    prevmapping = mapping;
                    prevParamid = mapping.getParamter().getId();

                }
                if (!specificationParameterMappingDtos.isEmpty()) {
                    parameterDTO = new SpecificationParameterDTO(prevmapping);
                    parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                    parameterDTOs.add(parameterDTO);
                }
                specificationDto.setParameters(parameterDTOs);
                return specificationDto;
            }

        } else if (specType == 30) {
            SpecificationDetails specificationDetails = specificationRepo.findbyExpenseSpecificationIdAndProdId(prodId);
            if (specificationDetails != null) {
                List<Specification_parameter_mapping> parameter_mappings = specificationParameterMappingRepo
                        .findBySpecificationId(specificationDetails.getId());

                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> parameterDTOs = new ArrayList<SpecificationParameterDTO>();
                List<SpecificationParameterMappingDto> specificationParameterMappingDtos = new ArrayList<SpecificationParameterMappingDto>();
                SpecificationParameterDTO parameterDTO = new SpecificationParameterDTO();

                Long prevParamid = 0L;
                Specification_parameter_mapping prevmapping = new Specification_parameter_mapping();

                for (Specification_parameter_mapping mapping : parameter_mappings) {
                    SpecificationParameterMappingDto specificationParameterMappingDto = new SpecificationParameterMappingDto(mapping);
                    if (!prevParamid.equals(mapping.getParamter().getId())) {
                        if (!specificationParameterMappingDtos.isEmpty()) {
                            parameterDTO = new SpecificationParameterDTO(prevmapping);
                            parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                            parameterDTOs.add(parameterDTO);
                            specificationParameterMappingDtos = new ArrayList<>();
                        }
                    }
                    specificationParameterMappingDtos.add(specificationParameterMappingDto);
                    prevmapping = mapping;
                    prevParamid = mapping.getParamter().getId();

                }
                if (!specificationParameterMappingDtos.isEmpty()) {
                    parameterDTO = new SpecificationParameterDTO(prevmapping);
                    parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                    parameterDTOs.add(parameterDTO);
                }
                specificationDto.setParameters(parameterDTOs);
                return specificationDto;
            }
        } else if (specType == 31) {
            SpecificationDetails specificationDetails = specificationRepo.findbyAMCExpenseSpecificationIdAndProdId(prodId);
            if (specificationDetails != null) {
                List<Specification_parameter_mapping> parameter_mappings = specificationParameterMappingRepo
                        .findBySpecificationId(specificationDetails.getId());

                SpecificationDto specificationDto = new SpecificationDto(specificationDetails);
                List<SpecificationParameterDTO> parameterDTOs = new ArrayList<SpecificationParameterDTO>();
                List<SpecificationParameterMappingDto> specificationParameterMappingDtos = new ArrayList<SpecificationParameterMappingDto>();
                SpecificationParameterDTO parameterDTO = new SpecificationParameterDTO();

                Long prevParamid = 0L;
                Specification_parameter_mapping prevmapping = new Specification_parameter_mapping();

                for (Specification_parameter_mapping mapping : parameter_mappings) {
                    SpecificationParameterMappingDto specificationParameterMappingDto = new SpecificationParameterMappingDto(mapping);
                    if (!prevParamid.equals(mapping.getParamter().getId())) {
                        if (!specificationParameterMappingDtos.isEmpty()) {
                            parameterDTO = new SpecificationParameterDTO(prevmapping);
                            parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                            parameterDTOs.add(parameterDTO);
                            specificationParameterMappingDtos = new ArrayList<>();
                        }
                    }
                    specificationParameterMappingDtos.add(specificationParameterMappingDto);
                    prevmapping = mapping;
                    prevParamid = mapping.getParamter().getId();

                }
                if (!specificationParameterMappingDtos.isEmpty()) {
                    parameterDTO = new SpecificationParameterDTO(prevmapping);
                    parameterDTO.setParameterMapping(specificationParameterMappingDtos);
                    parameterDTOs.add(parameterDTO);
                }
                specificationDto.setParameters(parameterDTOs);
                return specificationDto;
            }

        }
        return null;

    }




    public ResponseEntity<?> specificationListWithType(User user, Boolean pageFlag, String search, Long specType, Pageable pageable) {


        if (specType == 28) {
            List<ProductMaster> list = null;
            if(pageFlag){
                list = productMasterRepository.findBy(pageable);
            } else {
                list = productMasterRepository.findBy(search);
            }

            List<HsnSpecDto> result = list.stream().map(data -> new HsnSpecDto(data, specType)).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);

        } else if (specType == 29) {

            List<Machine> list = null;

            if(pageFlag){
                list = machineRepository.findBy(pageable);
            } else {
                list = machineRepository.findBy(search);
            }

            List<HsnSpecDto> result = list.stream().map(data -> new HsnSpecDto(data, specType)).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);

        } else if (specType == 30) {

            List<PurchaseExpense> list = null;

            if(pageFlag){
                list = purchaseExpenseRepository.findBy(specType, pageable);
            } else {
                list = purchaseExpenseRepository.findBy(specType, search);
            }

            List<HsnSpecDto> result = list.stream().map(data -> new HsnSpecDto(data, specType)).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);
        } else if (specType == 31) {
            List<PurchaseExpense> list = null;

            if(pageFlag){
                list = purchaseExpenseRepository.findBy(specType, pageable);
            } else {
                list = purchaseExpenseRepository.findBy(specType, search);
            }

            List<HsnSpecDto> result = list.stream().map(data -> {
                HsnSpecDto rs = new HsnSpecDto(data, specType);
                if(rs.getAmc_machine_ref_Id() != null && rs.getAmc_machine_ref_Id() != 0){
                    Optional<Machine> optional = machineRepository.findById(rs.getAmc_machine_ref_Id());
                    if(optional.isPresent()){
                        rs.setAmc_machine_ref_Name(optional.get().getName());
                    }
                }
                return rs;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);

        }else {
            return new ResponseEntity<List<HsnSpecDto>>(HttpStatus.NOT_FOUND);
        }

    }

}

package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.IdConstants;
import com.aria.utils.MapperUtil;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.utils.DateUtil;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class JournalService {

    @Autowired
    JournalRepository journalRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    JournalMachineMappingRepository journalMachineMappingRepository;

    @Autowired
    JournalProductMappingRepository journalProductMappingRepository;

    @Autowired
    PreprocessRepository preprocessRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    JournalAttributesMappingRepo journalAttributesMappingRepo;

    @Autowired
    JournalAttributesOptionRepo journalAttributesOptionRepo;

    @Autowired
    DepartmentDetailsRepository deptDetailsRepository;

    @Autowired
    MapperUtil mapperUtil;

    @Autowired
    PanelRepository panelRepository;

    @Autowired
    MachinePanelReadingMappingRepository panelReadingMappingRepository;


    @Autowired
    JournalBranchMapRepository journalBranchMapRepository;

    @Autowired
    ASRSJournalBranchMapRepository asrsJournalBranchMapRepository;


    public ResponseEntity<?> saveJournal(JournalDto journalDto, User loggedUser) {
        Journal journal = new Journal(journalDto);
        journal.setCompanyId(loggedUser.getOrg_id());
        journal.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        journal.setCreatedBy(loggedUser.getId());
        Journal result = journalRepository.save(journal);
        if (!journalDto.getBranchList().isEmpty()) {
            updateBranch(result, journalDto.getBranchList());
        }
        updateASRSBranch(result, journalDto);
        updateMapping(result, journalDto, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "Journal created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateJournal(JournalDto journalDto, User loggedUser) {
        Journal journal = null;
        Optional<Journal> journalOptional = journalRepository.findById(journalDto.getId());
        if (journalOptional.isPresent()) {
            journal = mapperUtil.getMapDataDto(journalOptional.get(), journalDto);
        } else {
            journal = new Journal(journalDto);
        }
        journal.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        journal.setUpdatedBy(loggedUser.getId());
        Journal result = journalRepository.save(journal);
        if (!journalDto.getBranchList().isEmpty()) {
            updateBranch(result, journalDto.getBranchList());
        }

        updateASRSBranch(result, journalDto);

        updateMapping(result, journalDto, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "Journal updated SuccessFully"), HttpStatus.OK);
    }

    public void updateMapping(Journal journal, JournalDto journalDto, User loggedUser) {

        if (journalDto.getProductDetails() != null && !journalDto.getProductDetails().isEmpty()) {
            for (JournalProductMappingDto journalProductMappingDto : journalDto.getProductDetails()) {

                System.out.println(journal.getId() + " === " + journalProductMappingDto.getProductId());
                System.out.println("BOM TAKE TYPE " + " === " + journalProductMappingDto.getBomTakeType());
                Optional<JournalProductMapping> optionalJournalProductMapping = null;
                System.out.println("BPTYPE " + journalProductMappingDto.getId() + " === " + journalProductMappingDto.getBatchProductType());

                if (journalProductMappingDto.getId() == null) {
                    if (journalProductMappingDto.getBatchProductType() != null && journalProductMappingDto.getBatchProductType().equalsIgnoreCase("ex")) {
                        optionalJournalProductMapping = journalProductMappingRepository.findByJournalProductMappingANDExcess(journal.getId(), journalProductMappingDto.getProductId(), journalProductMappingDto.getConsiderAs(), journalProductMappingDto.getBomTakeType(), journalProductMappingDto.getBatchProductType());
                    } else {
                        optionalJournalProductMapping = journalProductMappingRepository.findByJournalProductMappingNotInEX(journal.getId(), journalProductMappingDto.getProductId(), journalProductMappingDto.getConsiderAs(), journalProductMappingDto.getBomTakeType());
                    }
                } else {
                    optionalJournalProductMapping = journalProductMappingRepository.findById(journalProductMappingDto.getId());
                }

                JournalProductMapping resultProduct = null;
                if (!optionalJournalProductMapping.isPresent()) {
                    JournalProductMapping journalProductMapping = mapperUtil.getMapDataDto(journalProductMappingDto, journal, loggedUser);
                    journalProductMappingDto.setJournalId(journal.getId());
                    System.out.println("BOM TAKE TYPE journalProductMapping" + " === " + journalProductMapping.getBomTakeType());
                    resultProduct = journalProductMappingRepository.save(journalProductMapping);
                    System.out.println("BOM TAKE TYPE resultProduct " + " === " + resultProduct.getBomTakeType());
                    updatePAttributesMapping(journal, resultProduct, journalProductMappingDto, loggedUser);
                } else {
                    JournalProductMapping oldJournalProductMapping = optionalJournalProductMapping.get();
                    JournalProductMapping journalProductMapping = mapperUtil.getMapData(optionalJournalProductMapping.get(), journal, loggedUser, journalProductMappingDto);
                    System.out.println("Already BOM TAKE TYPE journalProductMapping" + " === " + journalProductMapping.getBomTakeType());
                    resultProduct = journalProductMappingRepository.save(journalProductMapping);
                    System.out.println("ALREADY BOM TAKE TYPE resultProduct " + " === " + resultProduct.getBomTakeType());
                    checkOldPAttributesMapping(journal, oldJournalProductMapping, resultProduct, journalProductMappingDto, loggedUser);
                    //updatePAttributesMapping(journal, resultProduct, journalProductMappingDto, loggedUser);
                }
            }
        }


        if (journalDto.getMachineDetails() != null && !journalDto.getMachineDetails().isEmpty()) {

            for (JournalMachineMappingDto journalMachineMappingDto : journalDto.getMachineDetails()) {

                Optional<JournalMachineMapping> optionalJournalMachineMapping = journalMachineMappingRepository.findByJournalMachineMapping(journal.getId(), journalMachineMappingDto.getMachineId());
                JournalMachineMapping resultMachine = null;

                if (!optionalJournalMachineMapping.isPresent()) {

                    JournalMachineMapping journalMachineMapping = mapperUtil.getMapDataDto(journalMachineMappingDto, journal, loggedUser);
                    journalMachineMappingDto.setJournalId(journal.getId());
                    resultMachine = journalMachineMappingRepository.save(journalMachineMapping);
                    updatePAttributesMapping(journal, resultMachine, journalMachineMappingDto, loggedUser);

                    if (!journalMachineMappingDto.getAuxlaryMachineList().isEmpty()) {

                        for (AuxiliaryMachineMappingDto auxiliary : journalMachineMappingDto.getAuxlaryMachineList()) {

                            Optional<JournalMachineMapping> optionalAuxiliary = journalMachineMappingRepository.findByJournalMachineMapping(journal.getId(), auxiliary.getMachineId(), resultMachine.getMachineId());
                            JournalMachineMapping resultAuxiliaryMachine = null;
                            if (!optionalAuxiliary.isPresent()) {

                                JournalMachineMapping journalAuxiliaryMachineMapping = mapperUtil.getMapDataDto(auxiliary, journal, loggedUser);
                                auxiliary.setJournalId(journal.getId());
                                resultAuxiliaryMachine = journalMachineMappingRepository.save(journalAuxiliaryMachineMapping);
                                updatePAttributesMapping(journal, resultAuxiliaryMachine, auxiliary, loggedUser);

                            } else {

                                JournalMachineMapping journalAuxiliaryMachineMapping = mapperUtil.getMapDataDto(optionalAuxiliary.get(), auxiliary, journal, loggedUser);
                                auxiliary.setJournalId(journal.getId());
                                resultAuxiliaryMachine = journalMachineMappingRepository.save(journalAuxiliaryMachineMapping);
                                updatePAttributesMapping(journal, resultAuxiliaryMachine, auxiliary, loggedUser);
                            }
                        }
                    }
                } else {
                    JournalMachineMapping journalMachineMapping = mapperUtil.getMapDataDto(optionalJournalMachineMapping.get(), journalMachineMappingDto, journal, loggedUser);
                    journalMachineMappingDto.setJournalId(journal.getId());
                    resultMachine = journalMachineMappingRepository.save(journalMachineMapping);
                    updatePAttributesMapping(journal, resultMachine, journalMachineMappingDto, loggedUser);

                    if (!journalMachineMappingDto.getAuxlaryMachineList().isEmpty()) {

                        for (AuxiliaryMachineMappingDto auxiliary : journalMachineMappingDto.getAuxlaryMachineList()) {

                            Optional<JournalMachineMapping> optionalAuxiliary = journalMachineMappingRepository.findByJournalMachineMapping(journal.getId(), auxiliary.getMachineId(), resultMachine.getMachineId());
                            JournalMachineMapping resultAuxiliaryMachine = null;
                            if (!optionalAuxiliary.isPresent()) {

                                JournalMachineMapping journalAuxiliaryMachineMapping = mapperUtil.getMapDataDto(auxiliary, journal, loggedUser);
                                auxiliary.setJournalId(journal.getId());
                                resultAuxiliaryMachine = journalMachineMappingRepository.save(journalAuxiliaryMachineMapping);
                                updatePAttributesMapping(journal, resultAuxiliaryMachine, auxiliary, loggedUser);

                            } else {

                                JournalMachineMapping journalAuxiliaryMachineMapping = mapperUtil.getMapDataDto(optionalAuxiliary.get(), auxiliary, journal, loggedUser);
                                auxiliary.setJournalId(journal.getId());
                                resultAuxiliaryMachine = journalMachineMappingRepository.save(journalAuxiliaryMachineMapping);
                                updatePAttributesMapping(journal, resultAuxiliaryMachine, auxiliary, loggedUser);
                            }
                        }
                    }
                }
            }
        }

        if (journalDto.getManWorkDetails() != null && !journalDto.getManWorkDetails().isEmpty()) {
            for (JournalMachineMappingDto journalMachineMappingDto : journalDto.getManWorkDetails()) {
                Optional<JournalMachineMapping> optionalJournalMachineMapping = journalMachineMappingRepository.findByJournalMachineMapping(journal.getId(), journalMachineMappingDto.getMachineId());
                JournalMachineMapping resultMachine = null;
                if (!optionalJournalMachineMapping.isPresent()) {

                    JournalMachineMapping journalMachineMapping = mapperUtil.getMapDataDto(journalMachineMappingDto, journal, loggedUser);
                    journalMachineMappingDto.setJournalId(journal.getId());
                    resultMachine = journalMachineMappingRepository.save(journalMachineMapping);
                    updatePAttributesMappingDWCluster(journal, resultMachine, journalMachineMappingDto, loggedUser);

                } else {
                    JournalMachineMapping journalMachineMapping = mapperUtil.getMapDataDto(optionalJournalMachineMapping.get(), journalMachineMappingDto, journal, loggedUser);
                    journalMachineMappingDto.setJournalId(journal.getId());
                    resultMachine = journalMachineMappingRepository.save(journalMachineMapping);
                    updatePAttributesMappingDWCluster(journal, resultMachine, journalMachineMappingDto, loggedUser);

                }
            }
        }
    }


    public void updatePAttributesMapping(Journal journal, JournalProductMapping resultProduct, JournalProductMappingDto productMappingDto, User loggedUser) {

        if (!productMappingDto.getDataSetList().isEmpty()) {

            for (JournalAttributesMappingDto dto : productMappingDto.getDataSetList()) {

                System.out.println(" journal.getId() " + journal.getId() + " resultProduct.getProductId()  " + resultProduct.getProductId() + "dto.getAttributesId()   " + dto.getAttributesId() + "  " + "product" + "dto.getBranchId()   " + dto.getBranchId());

                Optional<JournalAttributesMapping> journalAttributesTrue = journalAttributesMappingRepo.findBy(journal.getId(), resultProduct.getProductId(), dto.getAttributesId(), "product", dto.getBranchId(), resultProduct.getConsiderAs(), resultProduct.getBomTakeType());

                Optional<JournalAttributesMapping> journalAttributesFalse = journalAttributesMappingRepo.findByFalse(journal.getId(), resultProduct.getProductId(), dto.getAttributesId(), "product", dto.getBranchId(), resultProduct.getConsiderAs(), resultProduct.getBomTakeType());

                JournalAttributesMapping res = null;

                if (journalAttributesTrue.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesTrue.get();
                    updateProductMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else if (journalAttributesFalse.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesFalse.get();
                    updateProductMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else {

                    JournalAttributesMapping attributesMapping = new JournalAttributesMapping();
                    updateProductMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                }

                if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                    for (JournalAttributesOptionDto optionDto : dto.getOptions()) {
                        JournalAttributesOption result = new JournalAttributesOption(optionDto);
                        result.setAttributes_map_id(res.getId());
                        journalAttributesOptionRepo.save(result);
                    }
                }
            }
        }
    }


    public void checkOldPAttributesMapping(Journal journal, JournalProductMapping oldJournalProductMapping, JournalProductMapping resultProduct, JournalProductMappingDto productMappingDto, User loggedUser) {

        String oldBomTakeType = oldJournalProductMapping.getBomTakeType();
        String bomTakeType = resultProduct.getBomTakeType();
        Boolean update = false;
        if (!oldBomTakeType.equalsIgnoreCase(bomTakeType)) {
            update = true;
        }

        if (!productMappingDto.getDataSetList().isEmpty()) {

            for (JournalAttributesMappingDto dto : productMappingDto.getDataSetList()) {

                System.out.println(" journal.getId() " + journal.getId() + " resultProduct.getProductId()  " + resultProduct.getProductId() + "dto.getAttributesId()   " + dto.getAttributesId() + "  " + "product" + "dto.getBranchId()   " + dto.getBranchId());

                Optional<JournalAttributesMapping> journalAttributesTrue = null;
                Optional<JournalAttributesMapping> journalAttributesFalse = null;

                if (update) {
                    journalAttributesTrue = journalAttributesMappingRepo.findBy(journal.getId(), resultProduct.getProductId(), dto.getAttributesId(), "product", dto.getBranchId(), resultProduct.getConsiderAs(), oldBomTakeType);

                    journalAttributesFalse = journalAttributesMappingRepo.findByFalse(journal.getId(), resultProduct.getProductId(), dto.getAttributesId(), "product", dto.getBranchId(), resultProduct.getConsiderAs(), oldBomTakeType);

                } else {
                    journalAttributesTrue = journalAttributesMappingRepo.findBy(journal.getId(), resultProduct.getProductId(), dto.getAttributesId(), "product", dto.getBranchId(), resultProduct.getConsiderAs(), resultProduct.getBomTakeType());

                    journalAttributesFalse = journalAttributesMappingRepo.findByFalse(journal.getId(), resultProduct.getProductId(), dto.getAttributesId(), "product", dto.getBranchId(), resultProduct.getConsiderAs(), resultProduct.getBomTakeType());
                }
                JournalAttributesMapping res = null;

                if (journalAttributesTrue.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesTrue.get();
                    updateProductMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else if (journalAttributesFalse.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesFalse.get();
                    updateProductMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else {

                    JournalAttributesMapping attributesMapping = new JournalAttributesMapping();
                    updateProductMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                }

                if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                    for (JournalAttributesOptionDto optionDto : dto.getOptions()) {
                        JournalAttributesOption result = new JournalAttributesOption(optionDto);
                        result.setAttributes_map_id(res.getId());
                        journalAttributesOptionRepo.save(result);
                    }
                }
            }
        }


    }


    public void updatePAttributesMapping(Journal journal, JournalMachineMapping resultProduct, JournalMachineMappingDto productMappingDto, User loggedUser) {

        if (!productMappingDto.getDataSetList().isEmpty()) {
            for (JournalAttributesMappingDto dto : productMappingDto.getDataSetList()) {
                Optional<JournalAttributesMapping> journalAttributesTrue = journalAttributesMappingRepo.findBy(journal.getId(), resultProduct.getMachineId(), dto.getAttributesId(), "machine", dto.getBranchId());
                Optional<JournalAttributesMapping> journalAttributesFalse = journalAttributesMappingRepo.findByFalse(journal.getId(), resultProduct.getMachineId(), dto.getAttributesId(), "machine", dto.getBranchId());
                JournalAttributesMapping res = null;
                if (journalAttributesTrue.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesTrue.get();
                    updateMachineMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else if (journalAttributesFalse.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesFalse.get();
                    updateMachineMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else {

                    JournalAttributesMapping attributesMapping = new JournalAttributesMapping();
                    updateMachineMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);
                }

                if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                    for (JournalAttributesOptionDto optionDto : dto.getOptions()) {
                        JournalAttributesOption result = new JournalAttributesOption(optionDto);
                        result.setAttributes_map_id(res.getId());
                        journalAttributesOptionRepo.save(result);
                    }
                }
            }
        }
    }

    public void updateMachineMap(Journal journal, JournalMachineMapping resultProduct, JournalAttributesMapping attributesMapping, JournalAttributesMappingDto dto, User loggedUser) {
        attributesMapping.setStatus(true);
        attributesMapping.setAppCalStatus(dto.getAppCalStatus());
        attributesMapping.setAttributes_id(dto.getAttributesId());
        attributesMapping.setAttributes_name(dto.getAttributesName());
        attributesMapping.setDisporder(dto.getDisporder());
        attributesMapping.setJournal_id(journal.getId());
        attributesMapping.setDept_id(journal.getDeptId());
        if (attributesMapping.getBranch_id() == null) {
            if (dto.getBranchId() != null) {
                attributesMapping.setBranch_id(dto.getBranchId());
            } else {
                attributesMapping.setBranch_id(loggedUser.getBranch_id());
            }
        }
        attributesMapping.setDispunit(dto.getDispunit());
        attributesMapping.setDatatype(dto.getDatatype());
        attributesMapping.setAwithid(resultProduct.getMachineId());
        attributesMapping.setAwith(resultProduct.getConnect_table());
        attributesMapping.setAwithtable(resultProduct.getConnect_table());
        attributesMapping.setMaximum_value(dto.getMaximumValue());
        attributesMapping.setMinimum_value(dto.getMinimumValue());
        attributesMapping.setTypeOfMap(dto.getTypeOfMap());
        attributesMapping.setConnect_table(dto.getConnectTable());
    }


    public void updateProductMap(Journal journal, JournalProductMapping resultProduct, JournalAttributesMapping attributesMapping, JournalAttributesMappingDto dto, User loggedUser) {
        attributesMapping.setStatus(true);
        attributesMapping.setAppCalStatus(dto.getAppCalStatus());
        attributesMapping.setAttributes_id(dto.getAttributesId());
        attributesMapping.setAttributes_name(dto.getAttributesName());
        attributesMapping.setDisporder(dto.getDisporder());
        attributesMapping.setJournal_id(journal.getId());
        attributesMapping.setDept_id(journal.getDeptId());
        if (attributesMapping.getBranch_id() == null) {
            if (dto.getBranchId() != null) {
                attributesMapping.setBranch_id(dto.getBranchId());
            } else {
                attributesMapping.setBranch_id(loggedUser.getBranch_id());
            }
        }
        attributesMapping.setDispunit(dto.getDispunit());
        attributesMapping.setDatatype(dto.getDatatype());
        attributesMapping.setAwith("product");
        attributesMapping.setAwithid(resultProduct.getProductId());
        attributesMapping.setAwithtable("product");
        attributesMapping.setMaximum_value(dto.getMaximumValue());
        attributesMapping.setMinimum_value(dto.getMinimumValue());
        attributesMapping.setTypeOfMap(dto.getTypeOfMap());
        attributesMapping.setConnect_table(dto.getConnectTable());
        attributesMapping.setConsiderAs(resultProduct.getConsiderAs());
        attributesMapping.setBomIntakeType(resultProduct.getBomTakeType());
    }


    public void updatePAttributesMappingDWCluster(Journal journal, JournalMachineMapping resultProduct, JournalMachineMappingDto productMappingDto, User loggedUser) {

        if (!productMappingDto.getDataSetList().isEmpty()) {
            for (JournalAttributesMappingDto dto : productMappingDto.getDataSetList()) {
                Optional<JournalAttributesMapping> journalAttributesMapping = journalAttributesMappingRepo.findBy(journal.getId(), resultProduct.getMachineId(), dto.getAttributesId(), "dwcluster", dto.getBranchId());
                JournalAttributesMapping res = null;
                if (journalAttributesMapping.isPresent()) {
                    JournalAttributesMapping attributesMapping = journalAttributesMapping.get();
                    attributesMapping.setStatus(true);
                    attributesMapping.setAppCalStatus(dto.getAppCalStatus());
                    attributesMapping.setAttributes_id(dto.getAttributesId());
                    attributesMapping.setAttributes_name(dto.getAttributesName());
                    attributesMapping.setDisporder(dto.getDisporder());
                    attributesMapping.setJournal_id(journal.getId());
                    attributesMapping.setDept_id(journal.getDeptId());
                    if (attributesMapping.getBranch_id() == null) {
                        if (dto.getBranchId() != null) {
                            attributesMapping.setBranch_id(dto.getBranchId());
                        } else {
                            attributesMapping.setBranch_id(loggedUser.getBranch_id());
                        }
                    }
                    attributesMapping.setDispunit(dto.getDispunit());
                    attributesMapping.setDatatype(dto.getDatatype());
                    attributesMapping.setAwith(resultProduct.getConnect_table());
                    attributesMapping.setAwithid(resultProduct.getMachineId());
                    attributesMapping.setAwith(resultProduct.getConnect_table());
                    attributesMapping.setAwithtable(resultProduct.getConnect_table());
                    attributesMapping.setMaximum_value(dto.getMaximumValue());
                    attributesMapping.setMinimum_value(dto.getMinimumValue());
                    attributesMapping.setTypeOfMap(dto.getTypeOfMap());
                    attributesMapping.setConnect_table(dto.getConnectTable());
                    res = journalAttributesMappingRepo.save(attributesMapping);
                } else {
                    JournalAttributesMapping attributesMapping = new JournalAttributesMapping();
                    attributesMapping.setStatus(true);
                    attributesMapping.setAppCalStatus(dto.getAppCalStatus());
                    attributesMapping.setAttributes_id(dto.getAttributesId());
                    attributesMapping.setAttributes_name(dto.getAttributesName());
                    attributesMapping.setDisporder(dto.getDisporder());
                    attributesMapping.setJournal_id(journal.getId());
                    attributesMapping.setDept_id(journal.getDeptId());
                    if (dto.getBranchId() != null) {
                        attributesMapping.setBranch_id(dto.getBranchId());
                    } else {
                        attributesMapping.setBranch_id(loggedUser.getBranch_id());
                    }
                    attributesMapping.setDispunit(dto.getDispunit());
                    attributesMapping.setDatatype(dto.getDatatype());
                    attributesMapping.setAwithid(resultProduct.getMachineId());
                    attributesMapping.setAwith(resultProduct.getConnect_table());
                    attributesMapping.setAwithtable(resultProduct.getConnect_table());
                    attributesMapping.setMaximum_value(dto.getMaximumValue());
                    attributesMapping.setMinimum_value(dto.getMinimumValue());
                    attributesMapping.setTypeOfMap(dto.getTypeOfMap());
                    attributesMapping.setConnect_table(dto.getConnectTable());
                    res = journalAttributesMappingRepo.save(attributesMapping);
                }

                if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                    for (JournalAttributesOptionDto optionDto : dto.getOptions()) {
                        JournalAttributesOption result = new JournalAttributesOption(optionDto);
                        result.setAttributes_map_id(res.getId());
                        journalAttributesOptionRepo.save(result);
                    }
                }
            }
        }
    }


    public void updatePAttributesMapping(Journal journal, JournalMachineMapping resultProduct, AuxiliaryMachineMappingDto productMappingDto, User loggedUser) {

        if (!productMappingDto.getDataSetList().isEmpty()) {
            for (JournalAttributesMappingDto dto : productMappingDto.getDataSetList()) {
                Optional<JournalAttributesMapping> journalAttributesMappingTrue = journalAttributesMappingRepo.findBy(journal.getId(), resultProduct.getMachineId(), dto.getAttributesId(), "machine", dto.getBranchId());
                Optional<JournalAttributesMapping> journalAttributesMappingFalse = journalAttributesMappingRepo.findByFalse(journal.getId(), resultProduct.getMachineId(), dto.getAttributesId(), "machine", dto.getBranchId());
                JournalAttributesMapping res = null;
                if (journalAttributesMappingTrue.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesMappingTrue.get();
                    updateMachineMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else if (journalAttributesMappingFalse.isPresent()) {

                    JournalAttributesMapping attributesMapping = journalAttributesMappingFalse.get();
                    updateMachineMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);

                } else {
                    JournalAttributesMapping attributesMapping = new JournalAttributesMapping();
                    updateMachineMap(journal, resultProduct, attributesMapping, dto, loggedUser);
                    res = journalAttributesMappingRepo.save(attributesMapping);
                }

                if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                    for (JournalAttributesOptionDto optionDto : dto.getOptions()) {
                        JournalAttributesOption result = new JournalAttributesOption(optionDto);
                        result.setAttributes_map_id(res.getId());
                        journalAttributesOptionRepo.save(result);
                    }
                }
            }
        }
    }


    public ResponseEntity<?> listAll(String status, String journalName, String batchNumber, String productvityType, String processType, String productName, String department, Pageable pageable) throws UsernameNotFoundException {

        System.out.println(status);
        Page<Journal> list = journalRepository.filterJournalList(journalName, batchNumber, productvityType, processType, status, productName, department, pageable);
        if (!list.toList().isEmpty()) {
            List<JournalDto> result = list.toList().stream().map(data -> {
                JournalDto journalDto = new JournalDto(data);
                if (journalDto.getPrimaryProductId() != null) {
                    Optional<ProductMaster> productMaster = productMasterRepository.findById(journalDto.getPrimaryProductId());
                    if (productMaster.isPresent()) {
                        journalDto.setPrimaryProductName(productMaster.get().getProductName());
                    }
                }
                if (journalDto.getDeptId() != null) {
                    Optional<DepartmentDetails> departmentDetails = deptDetailsRepository.findById(journalDto.getDeptId());
                    if (departmentDetails.isPresent()) {
                        journalDto.setDeptName(departmentDetails.get().getName());
                    }
                }
                return journalDto;
            }).collect(Collectors.toList());
            PageDataDto<JournalDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<JournalDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<JournalDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> listAllJournal(User loggedUSer) throws UsernameNotFoundException {
        //List<JournalNameDto> list = journalRepository.getJournalList(loggedUSer.getOrg_id(), loggedUSer.getDepartment().getId(), loggedUSer.getBranch_id());
        List<JournalNameDto> list = journalRepository.getJournalList(loggedUSer.getDepartment().getId(), loggedUSer.getBranch_id());
        if (!list.isEmpty()) {
            List<JournalNameListDto> result = list.stream().map(data -> {
                JournalNameListDto journalDto = new JournalNameListDto(data);
                return journalDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<JournalNameListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalNameListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllPreprocess() throws UsernameNotFoundException {
        List<Preprocess> list = preprocessRepository.findAll();
        if (!list.isEmpty()) {
            List<PreprocessDto> result = list.stream().map(data -> {
                PreprocessDto preProcessDto = new PreprocessDto(data);
                return preProcessDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<PreprocessDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PreprocessDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listProductAll(Long journalId) throws UsernameNotFoundException {
        List<JournalProductMapping> list = journalProductMappingRepository.findByJournalProductMappingList(journalId);
        if (!list.isEmpty()) {
            List<JournalProductMappingDto> result = list.stream().map(data -> new JournalProductMappingDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<JournalProductMappingDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalProductMappingDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listMachineAll(Long journalId, User user) throws UsernameNotFoundException {
        List<JournalMachineMapping> list = journalMachineMappingRepository.findByJournalMachineMappingList(journalId, user.getBranch_id());
        if (!list.isEmpty()) {
            List<JournalMachineMappingDto> result = list.stream().map(data -> new JournalMachineMappingDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<JournalMachineMappingDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalMachineMappingDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getJournal(String id) throws UsernameNotFoundException {
        Optional<Journal> optionalJournal = journalRepository.findById(Long.parseLong(id));
        if (optionalJournal.isPresent()) {
            JournalDto journalDto = new JournalDto(optionalJournal.get());
            if (journalDto.getPrimaryProductId() != null) {
                Optional<ProductMaster> productMaster = productMasterRepository.findById(journalDto.getPrimaryProductId());
                if (productMaster.isPresent()) {
                    journalDto.setPrimaryProductName(productMaster.get().getProductName());
                }
                if (journalDto.getBatchProxId() != null) {
                    Optional<ProductMaster> productMaster1 = productMasterRepository.findById(journalDto.getBatchProxId());
                    if (productMaster1.isPresent()) {
                        journalDto.setBatchProductName(productMaster1.get().getProductName());
                    }
                }
            }
            List<JournalBranchMap> journalBranchMapList = journalBranchMapRepository.findBy(journalDto.getId());
            if (!journalBranchMapList.isEmpty()) {
                List<BranchDto> branchDtos = journalBranchMapList.stream().map(BranchDto::new).collect(Collectors.toList());
                journalDto.setBranchList(branchDtos);
            }

            List<ASRSJournalBranchMap> asrsJournalBranchMaps = asrsJournalBranchMapRepository.findBy(journalDto.getId());
            if (!asrsJournalBranchMaps.isEmpty()) {
                List<BranchDto> asrsBranchDtos = asrsJournalBranchMaps.stream().map(BranchDto::new).collect(Collectors.toList());
                journalDto.setAsrsBranchList(asrsBranchDtos);
            }
            updateGetMapping(journalDto);
            return new ResponseEntity<JournalDto>(journalDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> inActiveJournal(String id) throws UsernameNotFoundException {
        Optional<Journal> optionalJournal = journalRepository.findById(Long.parseLong(id));
        if (optionalJournal.isPresent()) {
            Journal journal = optionalJournal.get();
            journal.setActiveStatus("N");
            journalRepository.save(journal);
            return new ResponseEntity<Status>(new Status("200", "Updated Successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> removeJournalMap(Long id, String type) throws UsernameNotFoundException {

        if (type != null) {

            if (type.equalsIgnoreCase("Product")) {
                Optional<JournalProductMapping> journalProductMapping = journalProductMappingRepository.findById(id);
                if (journalProductMapping.isPresent()) {
                    JournalProductMapping rs = journalProductMapping.get();
                    rs.setStatus(false);
                    journalProductMappingRepository.save(rs);
                    deleteAllAttributes(type, rs, null);
                }
                return new ResponseEntity<Status>(new Status("200", "Journal Product Mapping Removed SuccessFully"), HttpStatus.OK);
            } else if (type.equalsIgnoreCase("Machine")) {
                Optional<JournalMachineMapping> journalMachineMapping = journalMachineMappingRepository.findById(id);
                if (journalMachineMapping.isPresent()) {
                    JournalMachineMapping rs = journalMachineMapping.get();
                    rs.setStatus(false);
                    journalMachineMappingRepository.save(rs);
                    deleteAllAttributes(type, null, rs);
                }
                return new ResponseEntity<Status>(new Status("200", "Journal Machine Mapping Removed SuccessFully"), HttpStatus.OK);
            } else if (type.equalsIgnoreCase("Attributes")) {
                Optional<JournalAttributesMapping> journalAttributesMapping = journalAttributesMappingRepo.findById(id);
                if (journalAttributesMapping.isPresent()) {
                    JournalAttributesMapping rs = journalAttributesMapping.get();
                    rs.setStatus(false);
                    journalAttributesMappingRepo.save(rs);
                }
                return new ResponseEntity<Status>(new Status("200", "Journal Attribute Mapping Removed SuccessFully"), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public void deleteAllAttributes(String type, JournalProductMapping productMapping, JournalMachineMapping machineMapping) {
        if (type.equalsIgnoreCase("Product")) {
            List<JournalAttributesMapping> list = journalAttributesMappingRepo.findByProductTrueList(productMapping.getJournalId(), productMapping.getProductId(), "product", productMapping.getConsiderAs(), productMapping.getBomTakeType());
            if (!list.isEmpty()) {
                for (JournalAttributesMapping journalAttributesMapping : list) {
                    journalAttributesMapping.setStatus(false);
                    journalAttributesMappingRepo.save(journalAttributesMapping);
                }
            }
        } else if (type.equalsIgnoreCase("Machine")) {
            List<JournalAttributesMapping> list = journalAttributesMappingRepo.findByTrueList(machineMapping.getJournalId(), machineMapping.getMachineId(), "machine");
            if (!list.isEmpty()) {
                for (JournalAttributesMapping journalAttributesMapping : list) {
                    journalAttributesMapping.setStatus(false);
                    journalAttributesMappingRepo.save(journalAttributesMapping);
                }
            }
        }
    }

    public ResponseEntity<?> removeJournalAttributeMap(Long journalId, String awith, Long awithId, Long attibutesId, Long branchId, String considerAs) throws UsernameNotFoundException {

        if (awith != null) {
            Optional<JournalAttributesMapping> journalAttributesMapping = journalAttributesMappingRepo.findByUnique(journalId, awithId, attibutesId, awith, branchId, considerAs);
            if (journalAttributesMapping.isPresent()) {
                JournalAttributesMapping journalAttributesMapping1 = journalAttributesMapping.get();
                journalAttributesMapping1.setStatus(false);
                journalAttributesMappingRepo.save(journalAttributesMapping1);
                return new ResponseEntity<Status>(new Status("200", "Journal Machine Mapping Removed SuccessFully"), HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public void updateGetMapping(JournalDto journalDto) {

        if (!journalDto.getProductDetails().isEmpty()) {
            List<JournalProductMappingDto> journalProductMappingDtos = new ArrayList<>();
            for (JournalProductMappingDto journalProductMappingDto : journalDto.getProductDetails()) {
                List<JournalAttributesMapping> list = journalAttributesMappingRepo.findByProductTrueList(journalProductMappingDto.getJournalId(), journalProductMappingDto.getProductId(), "product", journalProductMappingDto.getConsiderAs(), journalProductMappingDto.getBomTakeType());
                if (!list.isEmpty()) {
                    journalProductMappingDto.setDataSetList(list.stream().map(data -> new JournalAttributesMappingDto(data)).collect(Collectors.toList()));
                }
                journalProductMappingDtos.add(journalProductMappingDto);
            }
            journalDto.setProductDetails(journalProductMappingDtos);
        }

        if (!journalDto.getMachineDetails().isEmpty()) {
            List<JournalMachineMappingDto> journalMachineMappingDtos = new ArrayList<>();
            for (JournalMachineMappingDto journalMachineMappingDto : journalDto.getMachineDetails()) {
                List<JournalAttributesMapping> list = journalAttributesMappingRepo.findByTrueList(journalMachineMappingDto.getJournalId(), journalMachineMappingDto.getMachineId(), "machine");
                if (!list.isEmpty()) {
                    journalMachineMappingDto.setDataSetList(list.stream().map(data -> new JournalAttributesMappingDto(data)).collect(Collectors.toList()));
                }

                List<JournalMachineMapping> journalAuxiliaryMachineMappings = journalMachineMappingRepository.findByJournalAuxiliaryMachineList(journalDto.getId(), journalMachineMappingDto.getMachineId());

                List<AuxiliaryMachineMappingDto> journalAuxiliaryMachineList = journalAuxiliaryMachineMappings.stream().map(auxiliaryData -> {
                    AuxiliaryMachineMappingDto journalAuxiliaryMachineDto = new AuxiliaryMachineMappingDto(auxiliaryData);
                    List<JournalAttributesMapping> journalAuxiliaryAttributesList = journalAttributesMappingRepo.findByTrueList(auxiliaryData.getJournalId(), auxiliaryData.getMachineId(), "machine");
                    if (!journalAuxiliaryAttributesList.isEmpty()) {
                        journalAuxiliaryMachineDto.setDataSetList(journalAuxiliaryAttributesList.stream().map(data -> new JournalAttributesMappingDto(data)).collect(Collectors.toList()));
                    }
                    return journalAuxiliaryMachineDto;
                }).collect(Collectors.toList());

                journalMachineMappingDto.setAuxlaryMachineList(journalAuxiliaryMachineList);

                journalMachineMappingDtos.add(journalMachineMappingDto);
            }
            journalDto.setMachineDetails(journalMachineMappingDtos);
        }

        if (!journalDto.getManWorkDetails().isEmpty()) {
            List<JournalMachineMappingDto> journalMachineMappingDtos = new ArrayList<>();
            for (JournalMachineMappingDto journalMachineMappingDto : journalDto.getManWorkDetails()) {
                List<JournalAttributesMapping> list = journalAttributesMappingRepo.findByTrueList(journalMachineMappingDto.getJournalId(), journalMachineMappingDto.getMachineId(), "dwcluster");
                if (!list.isEmpty()) {
                    journalMachineMappingDto.setDataSetList(list.stream().map(data -> new JournalAttributesMappingDto(data)).collect(Collectors.toList()));
                }
                journalMachineMappingDtos.add(journalMachineMappingDto);
            }
            journalDto.setManWorkDetails(journalMachineMappingDtos);
        }
    }

    public ResponseEntity<?> deleteJournal(String id) throws UsernameNotFoundException {
        try {
            Optional<Journal> optionalJournal = journalRepository.findById(Long.parseLong(id));
            if (optionalJournal.isPresent()) {
                Journal journal = optionalJournal.get();

                List<JournalProductMapping> productMappings = journalProductMappingRepository.findByJournalProductMappingList(Long.parseLong(id));
                if (!productMappings.isEmpty()) {
                    journalProductMappingRepository.deleteAll(productMappings);
                }
                List<JournalMachineMapping> machineMappings = journalMachineMappingRepository.findByJournalMachineMappingList(Long.parseLong(id));
                if (!machineMappings.isEmpty()) {
                    journalMachineMappingRepository.deleteAll(machineMappings);
                }

                List<JournalAttributesMapping> attributesMappings = journalAttributesMappingRepo.findBy(Long.parseLong(id));
                if (!attributesMappings.isEmpty()) {
                    journalAttributesMappingRepo.deleteAll(attributesMappings);
                }

                journalRepository.delete(journal);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public ResponseEntity<?> listAll(Long dept) throws UsernameNotFoundException {
        List<JournalList> list = journalRepository.getDropDownList(dept);
        if (!list.isEmpty()) {
            List<JournalListDto> result = list.stream().map(data -> new JournalListDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<JournalListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> findBlendingEnabled() throws UsernameNotFoundException {
        List<JournalList> list = journalRepository.findBlendingEnabled();
        if (!list.isEmpty()) {
            List<BlendingJournalListDto> result = list.stream().map(data -> new BlendingJournalListDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<BlendingJournalListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BlendingJournalListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> listAllDataSet(Long journalId, User loggedUser, String withType, Long withid) throws UsernameNotFoundException {

        List<ProductionDataSetList> list = journalAttributesMappingRepo.findByProductionAttributesList(journalId, loggedUser.getBranch_id(), loggedUser.getDepartment().getId(), withType, withid);

        if (!list.isEmpty()) {

            List<ProductionDataSetListDto> dataSetDtoS = list.stream().map(data -> {
                ProductionDataSetListDto result = new ProductionDataSetListDto(data);
                if (data.getUnitName() != null) {
                    String colName = StringEscapeUtils.unescapeHtml4(data.getColName()) + "-" + data.getUnitName();
                    result.setDispColName(colName);
                } else {
                    result.setDispColName(StringEscapeUtils.unescapeHtml4(data.getColName()));
                }
                if (result.getDataType().equalsIgnoreCase("O")) {
                    result.setDropDownDtoList(dropDownList(data.getMapId(), result.getColid()));
                }
                if (result.getConnectTable() != null && result.getConnectTable().equalsIgnoreCase("panelmaster")) {
                    Optional<Panel> optional = panelRepository.findById(result.getColid());
                    if (optional.isPresent()) {
                        result.setMaxValue(String.valueOf(optional.get().getMaxUnits()));
                        Optional<MachinePanelReadingMapping> optional1 = panelReadingMappingRepository.findByReading(optional.get().getId());
                        if (optional1.isPresent()) {
                            result.setMinValue(String.valueOf(optional1.get().getMeterClosingReading()));
                        }
                    }
                }

                return result;
            }).collect(Collectors.toList());
            dataSetDtoS.addAll(getProductDetailsEx(journalId, loggedUser));
            return new ResponseEntity<List<ProductionDataSetListDto>>(dataSetDtoS, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductionDataSetListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public List<ProductionDataSetListDto> getMachineDetails(Long journalId, User loggedUser) throws UsernameNotFoundException {
        List<ProductionDataSetList> list = journalMachineMappingRepository.findByProductionAttributesList(journalId, loggedUser.getBranch_id(), loggedUser.getDepartment().getId());
        if (!list.isEmpty()) {
            List<ProductionDataSetListDto> dataSetDtoS = list.stream().map(data -> {
                ProductionDataSetListDto result = new ProductionDataSetListDto(data);
                if (data.getUnitName() != null) {
                    String colName = data.getColName() + "-" + data.getUnitName();
                    result.setDispColName(colName);
                } else {
                    result.setDispColName(data.getColName());
                }
                result.setDataType("V");
                result.setLinkWith("machine");
                return result;
            }).collect(Collectors.toList());
            return dataSetDtoS;
        } else {
            return new ArrayList<>();
        }
    }

    public List<ProductionDataSetListDto> getProductDetails(Long journalId, User loggedUser) throws UsernameNotFoundException {
        List<ProductionDataSetList> list = journalProductMappingRepository.findByProductionAttributesList(journalId, loggedUser.getBranch_id(), loggedUser.getDepartment().getId());
        if (!list.isEmpty()) {
            List<ProductionDataSetListDto> dataSetDtoS = list.stream().map(data -> {
                ProductionDataSetListDto result = new ProductionDataSetListDto(data);
                if (data.getUnitName() != null) {
                    String colName = data.getColName() + "-" + data.getUnitName();
                    result.setDispColName(colName);
                } else {
                    result.setDispColName(data.getColName());
                }
                result.setDataType("V");
                result.setLinkWith("product");
                return result;
            }).collect(Collectors.toList());
            return dataSetDtoS;
        } else {
            return new ArrayList<>();
        }
    }


    public List<ProductionDataSetListDto> getProductDetailsEx(Long journalId, User loggedUser) throws UsernameNotFoundException {

        List<JournalProductMapping> journalProductMappings = journalProductMappingRepository.findByJournalProductMappingOnlyExcess(journalId);

        if (!journalProductMappings.isEmpty()) {
            for (JournalProductMapping journalProductMapping : journalProductMappings) {
                List<ProductionDataSetList> list = journalAttributesMappingRepo.findByExcessProductionAttributesList(journalId, loggedUser.getBranch_id(), loggedUser.getDepartment().getId(), journalProductMapping.getConsiderAs(), "product", journalProductMapping.getProductId());
                if (!list.isEmpty()) {
                    List<ProductionDataSetListDto> dataSetDtoS = list.stream().map(data -> {
                        ProductionDataSetListDto result = new ProductionDataSetListDto(data);
                        if (data.getUnitName() != null) {
                            String colName = data.getColName() + "-" + data.getUnitName();
                            result.setDispColName(colName);
                        } else {
                            result.setDispColName(data.getColName());
                        }
                        result.setDataType("V");
                        result.setLinkWith("product");
                        result.setExcess("Y");
                        return result;
                    }).collect(Collectors.toList());
                    return dataSetDtoS;
                } else {
                    return new ArrayList<>();
                }
            }
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }


    public List<CommonDropDownDto> dropDownList(Long mapId, Long colId) throws UsernameNotFoundException {
        List<JournalAttributesOption> list = journalAttributesOptionRepo.findByAttributesOption(mapId, colId);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream().map(data -> new CommonDropDownDto(data.getAttributes_id(), data.getOptionvalue())).collect(Collectors.toList());
            return result;
        } else {
            return new ArrayList<>();
        }
    }


    public ResponseEntity<?> removeMultipleJournalMap(String ids, String type) throws UsernameNotFoundException {
        System.out.println("ids == " + ids);

        List<Long> idList = Stream.of(ids.split("\\,")).map(Long::parseLong).collect(Collectors.toList());

        if (type != null) {

            if (type.equalsIgnoreCase("Product")) {

                if (!idList.isEmpty()) {
                    for (Long id : idList) {
                        Optional<JournalProductMapping> journalProductMapping = journalProductMappingRepository.findById(id);
                        if (journalProductMapping.isPresent()) {
                            JournalProductMapping rs = journalProductMapping.get();
                            rs.setStatus(false);
                            journalProductMappingRepository.save(rs);
                            deleteAllAttributes(type, rs, null);
                        }
                    }
                    return new ResponseEntity<Status>(new Status("200", "Journal Product Mapping Removed SuccessFully"), HttpStatus.OK);
                } else {
                    return new ResponseEntity<Status>(new Status("400", "Journal Product Mapping Not Removed SuccessFully"), HttpStatus.OK);
                }
            } else if (type.equalsIgnoreCase("Machine")) {

                if (!idList.isEmpty()) {
                    for (Long id : idList) {
                        System.out.println("id == " + id);
                        Optional<JournalMachineMapping> journalMachineMapping = journalMachineMappingRepository.findById(id);
                        if (journalMachineMapping.isPresent()) {
                            JournalMachineMapping rs = journalMachineMapping.get();
                            rs.setStatus(false);
                            journalMachineMappingRepository.save(rs);
                            deleteAllAttributes(type, null, rs);
                        }
                    }
                    return new ResponseEntity<Status>(new Status("200", "Journal Machine Mapping Removed SuccessFully"), HttpStatus.OK);

                } else {
                    return new ResponseEntity<Status>(new Status("400", "Journal Machine Mapping Not Removed SuccessFully"), HttpStatus.OK);
                }
            } else if (type.equalsIgnoreCase("Attributes")) {

                if (!idList.isEmpty()) {
                    for (Long id : idList) {
                        Optional<JournalAttributesMapping> journalAttributesMapping = journalAttributesMappingRepo.findById(id);
                        if (journalAttributesMapping.isPresent()) {
                            JournalAttributesMapping rs = journalAttributesMapping.get();
                            rs.setStatus(false);
                            journalAttributesMappingRepo.save(rs);
                        }
                    }
                    return new ResponseEntity<Status>(new Status("200", "Journal Attribute Mapping Removed SuccessFully"), HttpStatus.OK);

                } else {
                    return new ResponseEntity<Status>(new Status("400", "Journal Attribute Mapping Not Removed SuccessFully"), HttpStatus.OK);
                }
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public void updateBranch(Journal journal, List<BranchDto> branchList) {


        List<Long> ids = new ArrayList<>();

        for (BranchDto branchDto : branchList) {
            ids.add(branchDto.getId());
            Optional<JournalBranchMap> optional = journalBranchMapRepository.findBy(journal.getId(), branchDto.getId());
            if (!optional.isPresent()) {
                JournalBranchMap journalBranchMap = new JournalBranchMap(branchDto);
                journalBranchMap.setJournalId(journal.getId());
                journalBranchMapRepository.save(journalBranchMap);
            }
        }

        List<JournalBranchMap> journalBranchMaps = journalBranchMapRepository.findBy(journal.getId(), ids);
        if (!journalBranchMaps.isEmpty()) {
            journalBranchMapRepository.deleteAll(journalBranchMaps);
        }
    }


    public void updateASRSBranch(Journal journal, JournalDto journalDto) {
        List<BranchDto> branchList = journalDto.getAsrsBranchList();

        if (!journalDto.getAsrsBranchList().isEmpty()) {
            List<Long> ids = new ArrayList<>();

            for (BranchDto branchDto : branchList) {
                ids.add(branchDto.getId());
                Optional<ASRSJournalBranchMap> optional = asrsJournalBranchMapRepository.findBy(journal.getId(), branchDto.getId());
                if (!optional.isPresent()) {
                    ASRSJournalBranchMap journalBranchMap = new ASRSJournalBranchMap(branchDto);
                    journalBranchMap.setJournalId(journal.getId());
                    asrsJournalBranchMapRepository.save(journalBranchMap);
                }
            }
            List<ASRSJournalBranchMap> journalBranchMaps = asrsJournalBranchMapRepository.findBy(journal.getId(), ids);
            if (!journalBranchMaps.isEmpty()) {
                asrsJournalBranchMapRepository.deleteAll(journalBranchMaps);
            }
        } else {
            List<ASRSJournalBranchMap> journalBranchMaps = asrsJournalBranchMapRepository.findBy(journal.getId());
            if (!journalBranchMaps.isEmpty()) {
                asrsJournalBranchMapRepository.deleteAll(journalBranchMaps);
            }
        }

    }


    public ResponseEntity<?> asrsJournalNameList(String name, User loggedUSer) throws UsernameNotFoundException {
        List<JournalNameDto> list = journalRepository.getJournalList(name, loggedUSer.getDepartment().getId(), loggedUSer.getBranch_id());
        if (!list.isEmpty()) {
            List<JournalNameListDto> result = list.stream().map(data -> {
                JournalNameListDto journalDto = new JournalNameListDto(data);
                return journalDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<JournalNameListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<JournalNameListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}

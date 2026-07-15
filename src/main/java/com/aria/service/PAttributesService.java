package com.aria.service;


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


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class PAttributesService {

    @Autowired
    PAttributesRepository pAttributesRepository;

    @Autowired
    PAttributesMappingRepository pAttributesMappingRepository;

    @Autowired
    DepartmentDetailsRepository departmentDetailsRepository;

    @Autowired
    JournalMachineMappingRepository journalMachineMappingRepository;

    @Autowired
    JournalProductMappingRepository journalProductMappingRepository;

    @Autowired
    PaOPtionsRepository paOPtionsRepository;


    public ResponseEntity<?> savePAttributes(List<PAttributesDto> pAttributesDtoList, User loggedUser) {
        if (!pAttributesDtoList.isEmpty()) {
            for (PAttributesDto pAttributesDto : pAttributesDtoList) {
                if (pAttributesRepository.existsByName(pAttributesDto.getAttributesName())) {
                    return ResponseEntity.badRequest().body(new Status("400", "Error: attributesName is already taken!"));
                }
                PAttributes pAttributes = new PAttributes(pAttributesDto);
                pAttributesRepository.save(pAttributes);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "PAttributes save successfully!!!"), HttpStatus.OK);
    }

    public PAttributes checkPAttributes(PAttributesDto pAttributesDto) {
        Optional<PAttributes> optionalPAttributes = pAttributesRepository.findByName(pAttributesDto.getAttributesName());
        if (optionalPAttributes.isPresent()) {
            PAttributes pAttributes = optionalPAttributes.get();
            return pAttributes;
        } else {
            PAttributes pAttributes = new PAttributes();
            pAttributes.setDept(pAttributesDto.getDeptId());
            pAttributes.setName(pAttributesDto.getAttributesName());
            PAttributes result = pAttributesRepository.save(pAttributes);
            return result;
        }
    }

    public ResponseEntity<?> updatePAttributes(List<PAttributesDto> pAttributesDtoList, User loggedUser) {
        if (!pAttributesDtoList.isEmpty()) {
            for (PAttributesDto pAttributesDto : pAttributesDtoList) {
                PAttributes pAttributes = new PAttributes(pAttributesDto);
                pAttributesRepository.save(pAttributes);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "PAttributes updated successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAll(String attributesName,String deptName,Pageable pageable) throws UsernameNotFoundException {
        Page<PAttributes> list = pAttributesRepository.filterPAttributes(attributesName,deptName,pageable);
        if (!list.isEmpty()) {
            List<PAttributesDto> result = list.toList().stream().map(data -> {
                PAttributesDto pAttributesDto = new PAttributesDto(data);
                updateDeptNAme(pAttributesDto);
                return pAttributesDto;
            }).collect(Collectors.toList());
            PageDataDto<PAttributesDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<PAttributesDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<PAttributesDto>>(new PageDataDto<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> listAll(String attributesName, Long deptId,Pageable pageable) throws UsernameNotFoundException {
        List<PAttributes> list = pAttributesRepository.filtersByPAttributes(attributesName,deptId,pageable);
        if (!list.isEmpty()) {
            List<PAttributesDto> result = list.stream().map(data -> {
                PAttributesDto pAttributesDto = new PAttributesDto(data);
                updateDeptNAme(pAttributesDto);
                return pAttributesDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<PAttributesDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PAttributesDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public void updateDeptNAme(PAttributesDto pAttributesDto) {
        Optional<DepartmentDetails> departmentDetailsOptional = departmentDetailsRepository.findById(pAttributesDto.getDeptId());
        if (departmentDetailsOptional.isPresent()) {
            pAttributesDto.setDeptName(departmentDetailsOptional.get().getName());
        }
    }

    public ResponseEntity<?> getPAttributes(String id) throws UsernameNotFoundException {
        Optional<PAttributes> optionalPAttributesMapping = pAttributesRepository.findById(Long.parseLong(id));
        PAttributesDto pAttributesDto = new PAttributesDto(optionalPAttributesMapping.get());
        updateDeptNAme(pAttributesDto);
        return new ResponseEntity<PAttributesDto>(pAttributesDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePAttributes(String id) throws UsernameNotFoundException {
        try {
            Optional<PAttributes> optionalPAttributes = pAttributesRepository.findById(Long.parseLong(id));
            if (optionalPAttributes.isPresent()) {
                PAttributes pAttributes = optionalPAttributes.get();
                pAttributesRepository.delete(pAttributes);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    // SJ Data Set

    public ResponseEntity<?> savePAttributesMapping(PAttributesMappingDto pAttributesDto, User loggedUser) {

        if (!pAttributesDto.getDataSetList().isEmpty()) {
            for (DataSetDto dataSetDto : pAttributesDto.getDataSetList()) {
                PAttributesMapping pAttributesMapping = new PAttributesMapping(pAttributesDto);
                pAttributesMapping.setAttributesId(dataSetDto.getAttributesId());
                pAttributesMapping.setAttributesName(dataSetDto.getAttributesName());
                pAttributesMapping.setDisporder(dataSetDto.getDispOrder());
                pAttributesMappingRepository.save(pAttributesMapping);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "PAttributesMapping save successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> updatePAttributesMapping(PAttributesMappingDto pAttributesDto, User loggedUser) {
        PAttributesMapping pAttributesMapping = new PAttributesMapping(pAttributesDto);
        pAttributesMappingRepository.save(pAttributesMapping);
        return new ResponseEntity<Status>(new Status("200", "PAttributesMapping updated successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllPAttributesMapping() throws UsernameNotFoundException {
        List<PAttributesMapping> list = pAttributesMappingRepository.findAll();
        if (!list.isEmpty()) {
            List<PAttributesMappingDto> pAttributesDtos = list.stream().map(data -> new PAttributesMappingDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<PAttributesMappingDto>>(pAttributesDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PAttributesMappingDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getPAttributesMapping(String id) throws UsernameNotFoundException {
        Optional<PAttributesMapping> optionalPAttributesMapping = pAttributesMappingRepository.findById(Long.parseLong(id));
        return new ResponseEntity<PAttributesMappingDto>(new PAttributesMappingDto(optionalPAttributesMapping.get()), HttpStatus.OK);
    }

    public ResponseEntity<?> getPAttributesMapping(Long journalId, String withType, Long withId) throws UsernameNotFoundException {
        PAttributesMapping optionalPAttributesMapping = pAttributesMappingRepository.findByPAttributesMapping(journalId, withType, withId);
        if (optionalPAttributesMapping != null) {
            PAttributesMappingDto pAttributesMappingDto = new PAttributesMappingDto(optionalPAttributesMapping);
            List<DataSetList> dataSetLists = pAttributesMappingRepository.findByPAttributesList(journalId, withType, withId);
            List<DataSetDto> dataSet = dataSetLists.stream().map(data -> new DataSetDto(data)).collect(Collectors.toList());
            pAttributesMappingDto.setDataSetList(dataSet);
            return new ResponseEntity<PAttributesMappingDto>(pAttributesMappingDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<PAttributesMappingDto>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deletePAttributesMapping(String id) throws UsernameNotFoundException {
        try {
            Optional<PAttributesMapping> optionalPAttributes = pAttributesMappingRepository.findById(Long.parseLong(id));
            if (optionalPAttributes.isPresent()) {
                PAttributesMapping pAttributes = optionalPAttributes.get();
                pAttributesMappingRepository.delete(pAttributes);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> listAllDataSet(Long journalId, Long productId, Long machineId) throws UsernameNotFoundException {
        List<DataSetList> list = pAttributesMappingRepository.findByPAttributesList(journalId, productId, machineId);
        if (!list.isEmpty()) {
            List<DataSetDto> dataSetDtos = list.stream().map(data -> new DataSetDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<DataSetDto>>(dataSetDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DataSetDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllDataSet(Long journalId) throws UsernameNotFoundException {
        List<DataSetList> list = pAttributesMappingRepository.findByPAttributesList(journalId);
        if (!list.isEmpty()) {
            List<DataSetDto> dataSetDtos = list.stream().map(data -> new DataSetDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<DataSetDto>>(dataSetDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DataSetDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

   /* public ResponseEntity<?> listAllDataSet(Long journalId, User loggedUser, String withType, Long withid) throws UsernameNotFoundException {
        List<ProductionDataSetList> list = pAttributesMappingRepository.findByProductionAttributesList(journalId, loggedUser.getBranch_id(), loggedUser.getDepartment().getId(), withType, withid);
        if (!list.isEmpty()) {
            List<ProductionDataSetListDto> dataSetDtos = list.stream().map(data -> {
                ProductionDataSetListDto result = new ProductionDataSetListDto(data);
                if(data.getUnitName() != null){
                    String colname = data.getColName() +"-"+data.getUnitName();
                    result.setDispColName(colname);
                }
                if (result.getLinkWith() != null && result.getDataType().equalsIgnoreCase("O")) {
                    result.setDropDownDtoList(dropDownList(result.getColid()));
                }
                return result;
            }).collect(Collectors.toList());
            dataSetDtos.addAll(getMachineDetails(journalId, loggedUser));
            dataSetDtos.addAll(getProductDetails(journalId, loggedUser));
            return new ResponseEntity<List<ProductionDataSetListDto>>(dataSetDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductionDataSetListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public List<ProductionDataSetListDto> getMachineDetails(Long journalId, User loggedUser) throws UsernameNotFoundException {
        List<ProductionDataSetList> list = journalMachineMappingRepository.findByProductionAttributesList(journalId, loggedUser.getBranch_id(), loggedUser.getDepartment().getId());
        if (!list.isEmpty()) {
            List<ProductionDataSetListDto> dataSetDtos = list.stream().map(data -> {
                ProductionDataSetListDto result = new ProductionDataSetListDto(data);
                result.setDataType("V");
                result.setLinkWith("machine");
                return result;
            }).collect(Collectors.toList());
            return dataSetDtos;
        } else {
            return new ArrayList<>();
        }
    }

    public List<ProductionDataSetListDto> getProductDetails(Long journalId, User loggedUser) throws UsernameNotFoundException {
        List<ProductionDataSetList> list = journalProductMappingRepository.findByProductionAttributesList(journalId, loggedUser.getBranch_id(), loggedUser.getDepartment().getId());
        if (!list.isEmpty()) {
            List<ProductionDataSetListDto> dataSetDtos = list.stream().map(data -> {
                ProductionDataSetListDto result = new ProductionDataSetListDto(data);
                result.setDataType("V");
                result.setLinkWith("product");
                return result;
            }).collect(Collectors.toList());
            return dataSetDtos;
        } else {
            return new ArrayList<>();
        }
    }

    public List<CommonDropDownDto> dropDownList(Long colId) throws UsernameNotFoundException {
        List<Tuple> list = paOPtionsRepository.findAll(colId);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return result;
        } else {
            return new ArrayList<>();
        }
    }*/

    public ResponseEntity<?> getSjMachine(String id) throws UsernameNotFoundException {
        try {
            List<JournalMachineMapping> list = journalMachineMappingRepository.findByJournalMachineMappingList(Long.parseLong(id));
            if (!list.isEmpty()) {
                List<JournalMachineMappingDto> machineMappingDtos = list.stream().map(data -> new JournalMachineMappingDto(data)).collect(Collectors.toList());
                return new ResponseEntity<List<JournalMachineMappingDto>>(machineMappingDtos, HttpStatus.OK);
            }
            return new ResponseEntity<List<JournalMachineMappingDto>>(new ArrayList<>(), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<List<JournalMachineMappingDto>>(new ArrayList<>(), HttpStatus.NOT_MODIFIED);
        }
    }
}

package com.aria.service;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import jakarta.persistence.Tuple;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class VendorService {

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    SupplierGroupRepo supplierGroupRepo;

    @Autowired
    RestService restService;

    @Autowired
    HOConfigRESTLogRepository restLogRepository;

    @Autowired
    BankInfoRepository bankInfoRepository;

    @Autowired
    CountryRepository countryRepository;


    public ResponseEntity<?> saveVendor(VendorDto vendorDto, User loggedUser) {
        Vendor vendor = new Vendor(vendorDto);
        vendor.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        Vendor result = vendorRepository.save(vendor);
        updateBankInfo(result, vendorDto);
        updateHoOfficeConfig(result, "CREATE");
        return new ResponseEntity<Status>(new Status("200", "vendor saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateVendor(VendorDto vendorDto, User loggedUser) {
        Vendor vendor = new Vendor(vendorDto);
        Vendor result = vendorRepository.save(vendor);
        updateBankInfo(result, vendorDto);
        updateHoOfficeConfig(result, "UPDATE");
        return new ResponseEntity<Status>(new Status("200", "vendor updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllVendor(User user) {
        List<VendorList> list = vendorRepository.findAllwithGroupName();
        if (!list.isEmpty()) {
            List<VendorDto> result = list.stream().map(data -> {
                VendorDto vendorDto = new VendorDto();
                BeanUtils.copyProperties(data, vendorDto);
                return vendorDto;
            }).sorted(Comparator.comparingLong(VendorDto::getId)).collect(Collectors.toList());
            return new ResponseEntity<List<VendorDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> listAllVendor(String vendorName, Long groupId, String gstNo, String mobileNo, Pageable pageable, User user) {
        Page<Vendor> list = vendorRepository.findAll(vendorName, groupId, gstNo, mobileNo, pageable);
        if (!list.isEmpty()) {
            List<VendorDto> result = list.toList().stream().map(vendor -> {
                VendorDto vendorDto = new VendorDto(vendor);
                if (vendorDto.getPartygroupid() != null && vendorDto.getPartygroupid() != 0) {
                    Optional<SupplierGroup> supplierGroup = supplierGroupRepo.findById(vendorDto.getPartygroupid());
                    if (supplierGroup.isPresent()) {
                        vendorDto.setPartygroupName(supplierGroup.get().getName());
                    }
                }
                return vendorDto;
            }).collect(Collectors.toList());
            PageDataDto<VendorDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<VendorDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<VendorDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getVendorDetails(String id) {
        try {
            Long id1 = Long.valueOf(id);
            Optional<Vendor> vendor = vendorRepository.findById(id1);
            if (vendor.isPresent()) {
                Vendor vendorDet = vendor.get();
                VendorDto vendorDto = new VendorDto(vendorDet);
                if (vendorDto.getPartygroupid() != null && vendorDto.getPartygroupid() != 0) {
                    Optional<SupplierGroup> supplierGroup = supplierGroupRepo.findById(vendorDto.getPartygroupid());
                    if (supplierGroup.isPresent()) {
                        vendorDto.setPartygroupName(supplierGroup.get().getName());
                    }
                }
                return new ResponseEntity<VendorDto>(vendorDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "vendor not found"), HttpStatus.OK);
    }

    public ResponseEntity<?> searchVendor(String name) {
        List<Vendor> list = null;
        if (name != null) {
            list = vendorRepository.findAllByVendorByName("%" + name + "%");
        } else {
            list = vendorRepository.findAll();
        }
        if (!list.isEmpty()) {
            List<VendorDto> result = list.stream().map(data -> {
                VendorDto vendorDto = new VendorDto();
                BeanUtils.copyProperties(data, vendorDto);
                return vendorDto;
            }).sorted(Comparator.comparingLong(VendorDto::getId)).collect(Collectors.toList());
            return new ResponseEntity<List<VendorDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteVendor(Long id) throws UsernameNotFoundException {
        try {
            Optional<Vendor> vendor = vendorRepository.findById(id);
            if (vendor.isPresent()) {
                Vendor vendorDet = vendor.get();
                vendorRepository.delete(vendorDet);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> saveVendor(VendorDto vendorDto, HttpServletRequest request) {
        try {
            Vendor vendor = new Vendor(vendorDto);
            vendorRepository.save(vendor);
            return new ResponseEntity<HOStatus>(new HOStatus("200", "", "successfully created!!", "Vendor Master created Successfully!!!", request.getServletPath()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<HOStatus>(new HOStatus("400", e.getMessage(), "failure", "Vendor Master Not created", request.getServletPath()), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> vendorDropDown(String name, String gst) {
        List<Tuple> list = vendorRepository.findAlls(name, gst);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllSearchVendor(String name, User user, Pageable pageable) {
        if (name != null) {
            List<VendorList> list = vendorRepository.findAllWithGroupName(name);
            if (!list.isEmpty()) {
                List<VendorDto> result = list.stream().map(data -> {
                    VendorDto vendorDto = new VendorDto();
                    BeanUtils.copyProperties(data, vendorDto);
                    return vendorDto;
                }).sorted(Comparator.comparingLong(VendorDto::getId)).collect(Collectors.toList());
                return new ResponseEntity<List<VendorDto>>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
            }
        } else {
            Page<VendorList> list = vendorRepository.findAllWithGroupName(pageable);
            if (!list.isEmpty()) {
                List<VendorDto> result = list.stream().map(data -> {
                    VendorDto vendorDto = new VendorDto();
                    BeanUtils.copyProperties(data, vendorDto);
                    return vendorDto;
                }).sorted(Comparator.comparingLong(VendorDto::getId)).collect(Collectors.toList());
                return new ResponseEntity<List<VendorDto>>(result, HttpStatus.OK);
            } else {
                return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
            }
        }
    }


    public void updateHoOfficeConfig(Vendor vendor, String createType) {

        VendorDto vendorDto = new VendorDto(vendor);
        String result = restService.saveVendor(vendorDto);
        Optional<Vendor> optional = vendorRepository.findById(vendorDto.getId());
        if (optional.isPresent()) {
            Vendor update = optional.get();
            update.setHoStatus(result);
            update.setSend_ft_to_ho_object(getFactoryData(vendorDto));
            vendorRepository.save(update);
            updateVendorLog("Vendor", createType, result, vendorDto);
        }
    }


    public String getFactoryData(VendorDto vendorDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(vendorDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void updateVendorLog(String type, String createType, String response, VendorDto jsonData) {
        String status = response;
        Long typeId = jsonData.getId();
        String responseData = response;
        String jsonData1 = getFactoryData(jsonData);
        updateRestLog(type, createType, typeId, status, responseData, jsonData1);
    }

    public void updateRestLog(String type, String createType, Long typeId, String status, String response, String jsonData) {
        HOConfigRESTLog hoConfigRESTLog = new HOConfigRESTLog();
        hoConfigRESTLog.setCreateType(createType);
        hoConfigRESTLog.setType(type);
        hoConfigRESTLog.setFrom_office("FT");
        hoConfigRESTLog.setTypeId(typeId);
        hoConfigRESTLog.setStatus(status);
        hoConfigRESTLog.setResponse(response);
        hoConfigRESTLog.setJson_data(jsonData);
        hoConfigRESTLog.setCreated_time(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        restLogRepository.save(hoConfigRESTLog);
    }

    public void updateBankInfo(Vendor vendor, VendorDto vendorDto) {

        if (!vendorDto.getBankInfo().isEmpty()) {
            for (BankInfoDTO bankInfoDTO : vendorDto.getBankInfo()) {
                if (bankInfoDTO.getId() == null) {
                    bankInfoRepository.updateBankInfo(vendor.getId());
                    BankInfo bankInfo = new BankInfo(bankInfoDTO);
                    bankInfo.setVid(vendor.getId());
                    bankInfo.setVtable("ledger");
                    bankInfo.setLastmodified(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    bankInfoRepository.save(bankInfo);
                } else {
                    BankInfo bankInfo = new BankInfo(bankInfoDTO);
                    bankInfo.setVid(vendor.getId());
                    bankInfo.setVtable("ledger");
                    bankInfo.setLastmodified(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                    bankInfoRepository.save(bankInfo);
                }
            }
        }

    }

    public ResponseEntity<?> vendorCountry(String id) {
        try {
            Long id1 = Long.valueOf(id);
            Optional<Country> country = countryRepository.findBy(id1);
            if (country.isPresent()) {
                CountryDto countryDto = new CountryDto(country.get());
                return new ResponseEntity<CountryDto>(countryDto, HttpStatus.OK);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "country not found"), HttpStatus.OK);
    }


}
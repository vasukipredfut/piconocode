package com.aria.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.VendorRepository;
import com.aria.dao.VendorSisterConcernMapRepository;
import com.aria.dto.PageDataDto;
import com.aria.dto.VendorSisterConcernMapDto;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.model.Vendor;
import com.aria.model.VendorSisterConcernMap;

@Service
public class VendorSisterConcernService {

    @Autowired
    VendorSisterConcernMapRepository vendorSisterConcernMapRepository;

    @Autowired
    VendorRepository vendorRepository;

    public ResponseEntity<?> saveVendorSisterConcern(VendorSisterConcernMapDto vendorDto, User loggedUser) {
        VendorSisterConcernMap vendor = new VendorSisterConcernMap(vendorDto);
        VendorSisterConcernMap result = vendorSisterConcernMapRepository.save(vendor);
        updateVendorDetails(result);
        return new ResponseEntity<Status>(new Status("200", "VendorSisterConcern saved successfully!!!"), HttpStatus.OK);
    }

    public void updateVendorDetails(VendorSisterConcernMap vendor) {

        VendorSisterConcernMapDto vendorSisterConcernMapDto = new VendorSisterConcernMapDto(vendor);

        for (Map<Long, String> stringMap : vendorSisterConcernMapDto.getVendors()) {

            for (Long vendorId : stringMap.keySet()) {
                updateVendor(vendorId, vendor.getId());
            }
        }
    }

    public void updateVendor(Long vendorId, Long groupId) {

        Optional<Vendor> optionalVendor = vendorRepository.findById(vendorId);
        if(optionalVendor.isPresent()){
            Vendor vendor = optionalVendor.get();
            vendor.setSisterConcernGroupId(groupId);
            vendorRepository.save(vendor);
        }

    }


    public ResponseEntity<?> updateVendorSisterConcern(VendorSisterConcernMapDto vendorDto, User loggedUser) {
        VendorSisterConcernMap vendor = new VendorSisterConcernMap(vendorDto);
        VendorSisterConcernMap result = vendorSisterConcernMapRepository.save(vendor);
        updateVendorDetails(result);
        return new ResponseEntity<Status>(new Status("200", "VendorSisterConcern updated successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllVendor(User user, Pageable pageable) {
    	Page<VendorSisterConcernMap> list = vendorSisterConcernMapRepository.findAll(pageable);
        if (!list.isEmpty()) {
            List<VendorSisterConcernMapDto> result = list.stream().map(data -> {
                VendorSisterConcernMapDto vendorDto = new VendorSisterConcernMapDto(data);
                return vendorDto;
            }).collect(Collectors.toList());
            PageDataDto<VendorSisterConcernMapDto> vendorSisterConcernDto = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<VendorSisterConcernMapDto>>(vendorSisterConcernDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<VendorSisterConcernMapDto>>(new PageDataDto<VendorSisterConcernMapDto>(), HttpStatus.OK);
        }
    }
}

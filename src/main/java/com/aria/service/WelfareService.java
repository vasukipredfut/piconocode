
package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class  WelfareService {

    @Autowired
    WelfareContributionRepository welfareContributionRepository;

    @Autowired
    WelfareAmountRepository welfareAmountRepository;
    
    @Autowired
    WelfareItemsRepository  welfareItemsRepository;


    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

  //welfare contribution
    
    public ResponseEntity<?> saveWelfareContribution(WelfareContributionDto welfareContributionDto, User loggedUser) {
    	WelfareContribution welfareContribution = new WelfareContribution(welfareContributionDto);
    	welfareContribution.setFromDate(dateUtil.getStringTODateF1(welfareContributionDto.getFromDate()));
    	welfareContribution.setToDate(dateUtil.getStringTODateF1(welfareContributionDto.getToDate()));
    	welfareContribution.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        welfareContribution.setCreatedBy(loggedUser.getId());
        welfareContributionRepository.save(welfareContribution);
        return new ResponseEntity<Status>(new Status("200", "welfareContribution save successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> updateWelfareContribution(WelfareContributionDto welfareContributionDto, User loggedUser) {
    	WelfareContribution welfareContribution = new WelfareContribution(welfareContributionDto);
    	welfareContribution.setFromDate(dateUtil.getStringTODateF1(welfareContributionDto.getFromDate()));
    	welfareContribution.setToDate(dateUtil.getStringTODateF1(welfareContributionDto.getToDate()));
    	welfareContribution.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        welfareContribution.setUpdatedBy(loggedUser.getId());
        welfareContributionRepository.save(welfareContribution);
        return new ResponseEntity<Status>(new Status("200", "welfareContribution  updated  successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllWelfareContribution() throws UsernameNotFoundException {

        List<WelfareContribution> list = welfareContributionRepository.findAll();
        if (!list.isEmpty()) {
            List<WelfareContributionDto> result = list.stream().map(data -> {
            	WelfareContributionDto welfareContributionDto = new WelfareContributionDto(data);
            	welfareContributionDto.setFromDate(dateUtil.getStringTODate(data.getFromDate()));
            	welfareContributionDto.setToDate(dateUtil.getStringTODate(data.getToDate()));
                updateEmployeeDetails(welfareContributionDto);
                return welfareContributionDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<WelfareContributionDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WelfareContributionDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getWelfareContributionDetails(String id) throws UsernameNotFoundException {
        Optional<WelfareContribution> optionalWelfareContribution = welfareContributionRepository.findById(Long.parseLong(id));
        WelfareContributionDto welfareContributionDto = new WelfareContributionDto(optionalWelfareContribution.get());
        welfareContributionDto.setFromDate(dateUtil.getStringTODate(optionalWelfareContribution.get().getFromDate()));
        welfareContributionDto.setToDate(dateUtil.getStringTODate(optionalWelfareContribution.get().getToDate()));
        updateEmployeeDetails(welfareContributionDto);
        return new ResponseEntity<WelfareContributionDto>(welfareContributionDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteWelfareContribution(String id) throws UsernameNotFoundException {
        try {
            Optional<WelfareContribution> optionalWelfareContribution = welfareContributionRepository.findById(Long.parseLong(id));
            if (optionalWelfareContribution.isPresent()) {
            	WelfareContribution welfareContribution = optionalWelfareContribution.get();
            	welfareContributionRepository.delete(welfareContribution);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updateEmployeeDetails(WelfareContributionDto welfareContribution) {

        Optional<User> user = userRepository.findById( welfareContribution.getCreatedBy());
        if (user.isPresent()) {
            // welfareContribution.setCreatedByName(user.get().getEmployeeName());
            // welfareContribution.setUpdatedByName(user.get().getEmployeeName());
        }
        if ( welfareContribution.getUpdatedBy() != null) {
            Optional<User> user1 = userRepository.findById(welfareContribution.getUpdatedBy());
            if (user1.isPresent()) {
                //welfareContribution.setUpdatedByName(user.get().getEmployeeName());
            }
        }
    }
    
    
	public ResponseEntity<?> listAllWelfareAmount(String fromDate, String toDate) throws UsernameNotFoundException {
		List<WelfareContribution> welfareContributionList = welfareContributionRepository.findAllDates(dateUtil.getStringTODateF1(fromDate),dateUtil.getStringTODateF1(toDate));
		return new ResponseEntity<List<WelfareContributionDto>>(welfareContributionList.stream().map(user -> {
			WelfareContributionDto welfareContributionDto = new WelfareContributionDto(user);
			welfareContributionDto.setFromDate(dateUtil.getStringTODate(user.getFromDate()));
			welfareContributionDto.setToDate(dateUtil.getStringTODate(user.getToDate()));
			updateEmployeeDetails(welfareContributionDto);
			return welfareContributionDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}
    
    
    
    
    
    
   /* 
    
    //welfare amount
    
    public ResponseEntity<?> saveWelfareAmount(WelfareAmountDto welfareAmountDto, User loggedUser) {
    	WelfareAmount welfareAmount = new WelfareAmount(welfareAmountDto);
    	welfareAmount.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    	welfareAmount.setCreatedBy(loggedUser.getId());
    	welfareAmount.setFromDate(dateUtil.getStringTODateF1(welfareAmountDto.getFromDate()));
    	welfareAmount.setToDate(dateUtil.getStringTODateF1(welfareAmountDto.getToDate()));
    	welfareAmountRepository.save(welfareAmount);
        return new ResponseEntity<Status>(new Status("200", "welfareAmount save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateWelfareAmount(WelfareAmountDto welfareAmountDto, User loggedUser) {
    	WelfareAmount welfareAmount = new WelfareAmount(welfareAmountDto);
    	welfareAmount.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    	welfareAmount.setUpdatedBy(loggedUser.getId());
    	welfareAmount.setFromDate(dateUtil.getStringTODateF1(welfareAmountDto.getFromDate()));
    	welfareAmount.setToDate(dateUtil.getStringTODateF1(welfareAmountDto.getToDate()));
    	welfareAmountRepository.save(welfareAmount);
        return new ResponseEntity<Status>(new Status("200", "welfareAmount updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllWelfareAmount() throws UsernameNotFoundException {
        List<WelfareAmount> list = welfareAmountRepository.findAll();
        if (!list.isEmpty()) {
            List<WelfareAmountDto> result = list.stream().map(data -> {
            	WelfareAmountDto welfareAmountDto = new WelfareAmountDto(data);
            	welfareAmountDto.setFromDate(dateUtil.getStringTODate(data.getFromDate()));
            	welfareAmountDto.setToDate(dateUtil.getStringTODate(data.getToDate()));
                 return welfareAmountDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<WelfareAmountDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WelfareAmountDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getWelfareAmountDetails(String id) throws UsernameNotFoundException {
        Optional<WelfareAmount> optionalWelfareAmount = welfareAmountRepository.findById(Long.parseLong(id));
        if (optionalWelfareAmount.isPresent()) {
        	WelfareAmountDto welfareAmountDto = new WelfareAmountDto(optionalWelfareAmount.get());
        	welfareAmountDto.setFromDate(dateUtil.getStringTODate(optionalWelfareAmount.get().getFromDate()));
        	welfareAmountDto.setToDate(dateUtil.getStringTODate(optionalWelfareAmount.get().getToDate()));
        	return new ResponseEntity<WelfareAmountDto>(welfareAmountDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteWelfareAmountDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<WelfareAmount> optionalWelfareAmount = welfareAmountRepository.findById(Long.parseLong(id));
            if (optionalWelfareAmount.isPresent()) {
            	WelfareAmount welfareAmount = optionalWelfareAmount.get();
            	welfareAmountRepository.delete(welfareAmount);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    */
    //welfare items
    public ResponseEntity<?> saveWelfareItems(WelfareItemsDto welfareItemsDto, User loggedUser) {
    	WelfareItems welfareItems = new WelfareItems(welfareItemsDto);
    	welfareItems.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    	welfareItems.setCreatedBy(loggedUser.getId());
    	welfareItems.setDate(dateUtil.getStringTODateF1(welfareItemsDto.getDate()));
    	welfareItemsRepository.save(welfareItems);
        return new ResponseEntity<Status>(new Status("200", "WelfareItems save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateWelfareItems(WelfareItemsDto welfareItemsDto, User loggedUser) {
    	WelfareItems welfareItems = new WelfareItems(welfareItemsDto);
    	welfareItems.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    	welfareItems.setUpdatedBy(loggedUser.getId());
    	welfareItems.setDate(dateUtil.getStringTODateF1(welfareItemsDto.getDate()));
    	welfareItemsRepository.save(welfareItems);
        return new ResponseEntity<Status>(new Status("200", "WelfareItems updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllWelfareItems() throws UsernameNotFoundException {
        List<WelfareItems> list = welfareItemsRepository.findAll();
        if (!list.isEmpty()) {
            List<WelfareItemsDto> result = list.stream().map(data -> {
            	WelfareItemsDto welfareItemsDto = new WelfareItemsDto(data);
            	welfareItemsDto.setDate(dateUtil.getStringTODate(data.getDate()));
                 return welfareItemsDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<WelfareItemsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WelfareItemsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getWelfareItemsDetails(String id) throws UsernameNotFoundException {
        Optional<WelfareItems> optionalWelfareItems = welfareItemsRepository.findById(Long.parseLong(id));
        if (optionalWelfareItems.isPresent()) {
        	WelfareItemsDto welfareItemsDto = new WelfareItemsDto(optionalWelfareItems.get());
        	welfareItemsDto.setDate(dateUtil.getStringTODate(optionalWelfareItems.get().getDate()));
            return new ResponseEntity<WelfareItemsDto>(welfareItemsDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteWelfareItemsDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<WelfareItems> optionalWelfareItems = welfareItemsRepository.findById(Long.parseLong(id));
            if (optionalWelfareItems.isPresent()) {
            	WelfareItems welfareItems = optionalWelfareItems.get();
            	welfareItemsRepository.delete(welfareItems);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

}

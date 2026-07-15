package com.aria.service;

import com.aria.dto.*;
import com.aria.dao.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class ScrapService {

    @Autowired
    ScrapRepository scrapRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    public ResponseEntity<?> saveScrap(ScrapDto scrapDto, User loggedUser) {
    	Scrap scrap = new Scrap(scrapDto);
    	scrap.setStoredOn(dateUtil.getStringTODateF1(scrapDto.getStoredOn()));
    	scrap.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    	scrap.setCreatedBy(loggedUser.getId());
    	scrapRepository.save(scrap);
        return new ResponseEntity<Status>(new Status("200", "Scrap save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateScrap(ScrapDto scrapDto, User loggedUser) {
    	Scrap scrap = new Scrap(scrapDto);
    	scrap.setStoredOn(dateUtil.getStringTODateF1(scrapDto.getStoredOn()));
    	scrap.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    	scrap.setUpdatedBy(loggedUser.getId());
    	scrapRepository.save(scrap);
        return new ResponseEntity<Status>(new Status("200", "Scrap updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllScrap(Pageable pageable) throws UsernameNotFoundException {
        Page<Scrap> list = scrapRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<ScrapDto> result = list.toList().stream().map(data -> {
            	ScrapDto scrapDto = new ScrapDto(data);
            	scrapDto.setStoredOn(dateUtil.getStringTODate(data.getStoredOn()));
            	return scrapDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<ScrapDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ScrapDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getScrap(String id) throws UsernameNotFoundException {
        Optional<Scrap> optionalScrap = scrapRepository.findById(Long.parseLong(id));
        if (optionalScrap.isPresent()) {
        	ScrapDto scrapDto = new ScrapDto(optionalScrap.get());
        	scrapDto.setStoredOn(dateUtil.getStringTODate(optionalScrap.get().getStoredOn()));

        	return new ResponseEntity<ScrapDto>(scrapDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    public ResponseEntity<?> scrapfilterList(String scrapItem, Long departmentId,String fromProcess,String selectStorageBin,String dateFrom,String dateTo)
			 throws UsernameNotFoundException {
         List<Scrap> list = scrapRepository.scrapfilterList(scrapItem, departmentId,fromProcess,selectStorageBin,dateUtil.getStringTODateF1(dateFrom), dateUtil.getStringTODateF1(dateTo));
		if (!list.isEmpty()) {
			List<ScrapDto> result = list.stream().map(data -> {
				ScrapDto scrapDto = new ScrapDto(data);
				scrapDto.setStoredOn(dateUtil.getStringTODate(data.getStoredOn()));
				return scrapDto;
			}).collect(Collectors.toList());
			return new ResponseEntity<List<ScrapDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<ScrapDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}  
    public ResponseEntity<?> deleteScrap(String id) throws UsernameNotFoundException {
        try {
            Optional<Scrap> optionalScrap = scrapRepository.findById(Long.parseLong(id));
            if (optionalScrap.isPresent()) {
            	Scrap scrap = optionalScrap.get();
            	scrapRepository.delete(scrap);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
   
	
}

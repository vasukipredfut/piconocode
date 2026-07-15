package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dto.*;
import com.aria.model.ShiftsList;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.ShiftsRepository;
import com.aria.dao.UserRepository;
import com.aria.model.ShiftCategory;
import com.aria.model.Shifts;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class ShiftsService {

    @Autowired
    ShiftsRepository shiftsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;
    
    public boolean checkShiftsAlreadyExisit(ShiftsDto shiftsDto) {
    	long res = shiftsRepository.checkShiftsAlreadyExisit(shiftsDto.getIntime(), shiftsDto.getOuttime(),
    			shiftsDto.getLunchtime());
    	if(res==0) return false;
    	return true;
    }


    public void createShifts(ShiftsDto shiftsDto, User user) {
        try {
            Shifts newShifts = new Shifts();
            BeanUtils.copyProperties(shiftsDto, newShifts);
            newShifts.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            newShifts.setCreatedBy(user.getId());
           
            shiftsRepository.save(newShifts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResponseEntity<?> saveShifts(ArrayList<ShiftsDto> shiftsDtos, User user) {
        try {
            for (ShiftsDto saveShiftsDto : shiftsDtos) {
                Shifts newShifts = new Shifts(saveShiftsDto);
                //BeanUtils.copyProperties(saveShiftsDto, newShifts, "id");
                newShifts.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                newShifts.setCreatedBy(user.getId());
                if(checkShiftsAlreadyExisit(saveShiftsDto)) {
                	return new ResponseEntity<Status>(new Status("200","This shift is already exisited!!"), HttpStatus.OK);
                }
                shiftsRepository.save(newShifts);
            }
            //shiftsRepository.saveAll(newShiftsLists);
            return new ResponseEntity<Status>(new Status("200", "shifts save successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "shifts not created"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateShifts(ShiftsDto shiftsDtos, User user) {
        try {
            Shifts newShifts = new Shifts(shiftsDtos);
            newShifts.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            newShifts.setUpdatedBy(user.getId());
            shiftsRepository.save(newShifts);
            return new ResponseEntity<Status>(new Status("200", "shifts update successfully!!!"), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<Status>(new Status("400", "shifts not updated"), HttpStatus.OK);
    }


    public ResponseEntity<?> deleteShifts(Long id) {

        Optional<Shifts> shifts = shiftsRepository.findById(id);
        if (shifts.isPresent()) {
            shiftsRepository.delete(shifts.get());
            return new ResponseEntity<Status>(new Status("200", "shifts deleted successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("400", "shifts not deleted"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllShifts(String shiftType, User user, Pageable pageable) {
        Page<ShiftsList> list = shiftsRepository.findByShifts(shiftType, pageable);
        if (!list.toList().isEmpty()) {
            List<ShiftsListDto> result = list.toList().stream().map(data -> new ShiftsListDto(data)).collect(Collectors.toList());
            PageDataDto<ShiftsListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<ShiftsListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ShiftsListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> findAllShiftCategory(String shiftType) {
        List<ShiftCategory> list = shiftsRepository.findAllShiftCategory(shiftType);
        return new ResponseEntity<List<ShiftCategory>>(list, HttpStatus.OK);
    }

    public ResponseEntity<?> listAllShifts(String shiftType) {
        List<Shifts> list = shiftsRepository.findByShifts(shiftType);
        if (!list.isEmpty()) {
            List<ShiftsDto> result = list.stream().map(data -> new ShiftsDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<ShiftsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
    public ResponseEntity<?> listAllShiftswopage() {
        List<Shifts> list = shiftsRepository.findAll();
        if (!list.isEmpty()) {
            List<ShiftsDto> result = list.stream().map(data -> new ShiftsDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<ShiftsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getShiftDetails(String id) {
        Optional<Shifts> shifts = shiftsRepository.findById(Long.valueOf(id));
        if (shifts.isPresent()) {
            ShiftsDto shiftDto = new ShiftsDto(shifts.get());
            return new ResponseEntity<ShiftsDto>(shiftDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

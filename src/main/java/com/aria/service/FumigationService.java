package com.aria.service;

import com.aria.dao.FumigationRepository;
import com.aria.dao.FumigationSchedulerRepository;
import com.aria.dto.FumigationDto;
import com.aria.dto.FumigationSchedulerDto;
import com.aria.model.Fumigation;
import com.aria.model.FumigationScheduler;
import com.aria.model.Status;
import com.aria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FumigationService {


    @Autowired
    FumigationRepository fumigationRepository;

    @Autowired
    FumigationSchedulerRepository fumigationSchedulerRepository;


    public ResponseEntity<?> saveFumigation(FumigationDto fumigationDto, User loggedUser) {
        Fumigation fumigation = new Fumigation(fumigationDto);
        fumigation.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        fumigation.setCreatedBy(loggedUser.getId());
        Fumigation result = fumigationRepository.save(fumigation);
        updateFumigationScheduler(result);
        return new ResponseEntity<Status>(new Status("200", "Fumigation save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateFumigation(FumigationDto FumigationDto, User loggedUser) {
        Fumigation fumigation = new Fumigation(FumigationDto);
        fumigation.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        fumigation.setUpdatedBy(loggedUser.getId());
        fumigationRepository.save(fumigation);
        return new ResponseEntity<Status>(new Status("200", "Fumigation  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAll(Pageable pageable) throws UsernameNotFoundException {

        List<Fumigation> list = fumigationRepository.findAll();
        if (!list.isEmpty()) {
            List<FumigationDto> result = list.stream().map(data -> {
                FumigationDto fumigationDto = new FumigationDto(data);
                return fumigationDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<FumigationDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<FumigationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getFumigation(String id) throws UsernameNotFoundException {
        Optional<Fumigation> optionalFumigation = fumigationRepository.findById(Long.parseLong(id));
        FumigationDto fumigationDto = new FumigationDto(optionalFumigation.get());
        return new ResponseEntity<FumigationDto>(fumigationDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteFumigation(String id) throws UsernameNotFoundException {
        try {
            Optional<Fumigation> optionalFumigation = fumigationRepository.findById(Long.parseLong(id));
            if (optionalFumigation.isPresent()) {
                Fumigation fumigation = optionalFumigation.get();
                fumigationRepository.delete(fumigation);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public void updateFumigationScheduler(Fumigation fumigation) {
        try {
            Optional<FumigationScheduler> optional = fumigationSchedulerRepository.findBy(fumigation.getFumigationType(), fumigation.getGodown().getId());
            if (optional.isPresent()) {
                FumigationScheduler fumigationScheduler = optional.get();
                fumigationScheduler.setLastDate(fumigation.getLastDate());
                fumigationScheduler.setNextDate(fumigation.getNextDate());
                fumigationSchedulerRepository.save(fumigationScheduler);
            } else {
                FumigationScheduler fumigationScheduler = new FumigationScheduler();
                fumigationScheduler.setGodown(fumigation.getGodown());
                fumigationScheduler.setFumigationType(fumigation.getFumigationType());
                fumigationScheduler.setLastDate(fumigation.getLastDate());
                fumigationScheduler.setNextDate(fumigation.getNextDate());
                fumigationSchedulerRepository.save(fumigationScheduler);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public ResponseEntity<?> listAll() throws UsernameNotFoundException {

        List<FumigationScheduler> list = fumigationSchedulerRepository.findAll();
        List<FumigationSchedulerDto> result = new ArrayList<>();
        if (!list.isEmpty()) {
            list.stream().map(data -> {
                FumigationSchedulerDto fumigationDto = new FumigationSchedulerDto(data);
                if (fumigationDto != null) {
                    result.add(fumigationDto);
                }
                return fumigationDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<FumigationSchedulerDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<FumigationSchedulerDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}

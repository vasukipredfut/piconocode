package com.aria.service;

import com.aria.dao.ProScheduleRepository;
import com.aria.dto.ProScheduleDto;
import com.aria.model.ProSchedule;
import com.aria.model.Status;
import com.aria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProScheduleService {


    @Autowired
    ProScheduleRepository proScheduleRepository;


    public ResponseEntity<?> saveProSchedule(ProScheduleDto proScheduleDto, User loggedUser) {
        ProSchedule proSchedule = new ProSchedule(proScheduleDto);
        //ProSchedule.setCreated_date(LocalDateTime.now());
        //ProSchedule.setCreated_by(loggedUser.getId());
        ProSchedule result = proScheduleRepository.save(proSchedule);
        return new ResponseEntity<Status>(new Status("200", "ProSchedule created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateProSchedule(ProScheduleDto proScheduleDto, User loggedUser) {
        ProSchedule proSchedule = new ProSchedule(proScheduleDto);
        ProSchedule result = proScheduleRepository.save(proSchedule);
        return new ResponseEntity<Status>(new Status("200", "ProSchedule updated SuccessFully"), HttpStatus.OK);
    }


    public ResponseEntity<?> getProSchedule(String id) throws UsernameNotFoundException {
        Optional<ProSchedule> optionalProSchedule = proScheduleRepository.findById(Long.parseLong(id));
        if (optionalProSchedule.isPresent()) {
            ProScheduleDto proScheduleDto = new ProScheduleDto(optionalProSchedule.get());
            return new ResponseEntity<ProScheduleDto>(proScheduleDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> deleteProSchedule(String id) throws UsernameNotFoundException {
        try {
            Optional<ProSchedule> optionalProSchedule = proScheduleRepository.findById(Long.parseLong(id));
            if (optionalProSchedule.isPresent()) {
                ProSchedule proSchedule = optionalProSchedule.get();
                proScheduleRepository.delete(proSchedule);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}

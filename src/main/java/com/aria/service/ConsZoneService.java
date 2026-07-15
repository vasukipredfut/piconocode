package com.aria.service;


import com.aria.dao.ConsZoneRepository;
import com.aria.dto.ConsZoneDto;
import com.aria.model.ConsZone;
import com.aria.model.Status;
import com.aria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsZoneService {

    @Autowired
    ConsZoneRepository consZoneRepository;


    public ResponseEntity<?> saveConsZone(ConsZoneDto consZoneDto, User loggedUser) {
        ConsZone consZone = new ConsZone(consZoneDto);
        ConsZone result = consZoneRepository.save(consZone);
        return new ResponseEntity<Status>(new Status("200", "ConsZone created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateConsZone(ConsZoneDto consZoneDto, User loggedUser) {
        ConsZone consZone = new ConsZone(consZoneDto);
        ConsZone result = consZoneRepository.save(consZone);
        return new ResponseEntity<Status>(new Status("200", "ConsZone updated SuccessFully"), HttpStatus.OK);
    }


    public ResponseEntity<?> getConsZone(String id) throws UsernameNotFoundException {
        Optional<ConsZone> optionalConsZone = consZoneRepository.findById(Long.parseLong(id));
        if (optionalConsZone.isPresent()) {
            ConsZoneDto consZoneDto = new ConsZoneDto(optionalConsZone.get());
            return new ResponseEntity<ConsZoneDto>(consZoneDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> deleteConsZone(String id) throws UsernameNotFoundException {
        try {
            Optional<ConsZone> optionalConsZone = consZoneRepository.findById(Long.parseLong(id));
            if (optionalConsZone.isPresent()) {
                ConsZone consZone = optionalConsZone.get();
                consZoneRepository.delete(consZone);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}

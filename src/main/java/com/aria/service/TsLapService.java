package com.aria.service;

import com.aria.dao.TsLapRepository;
import com.aria.dto.TsLapDto;
import com.aria.model.TsLap;
import com.aria.model.Status;
import com.aria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TsLapService {

    @Autowired
    TsLapRepository tsLapRepository;

    public ResponseEntity<?> saveTsLap(TsLapDto TsLapDto, User loggedUser) {
        TsLap tsLap = new TsLap(TsLapDto);
        tsLapRepository.save(tsLap);
        return new ResponseEntity<Status>(new Status("200", "TsLap saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateTsLap(TsLapDto TsLapDto, User loggedUser) {
        TsLap tsLap = new TsLap(TsLapDto);
        tsLapRepository.save(tsLap);
        return new ResponseEntity<Status>(new Status("200", "TsLap updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllTsLap(Long tsId) throws UsernameNotFoundException {
        List<TsLap> tsLaps = tsLapRepository.findByIds(tsId);
        return new ResponseEntity<List<TsLapDto>>(tsLaps.stream().map(user -> new TsLapDto(user)).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllTsLap() throws UsernameNotFoundException {
        List<TsLap> tsLaps = tsLapRepository.findAll();
        return new ResponseEntity<List<TsLapDto>>(tsLaps.stream().map(user -> new TsLapDto(user)).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllTsLapByShift(int shiftId) throws UsernameNotFoundException {
        List<TsLap> tsLaps = tsLapRepository.findByPshift(shiftId);
        return new ResponseEntity<List<TsLapDto>>(tsLaps.stream().map(user -> new TsLapDto(user)).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllTsLapByShiftId() throws UsernameNotFoundException {
        List<TsLap> tsLaps = tsLapRepository.findAll();
        return new ResponseEntity<List<TsLapDto>>(tsLaps.stream().map(user -> new TsLapDto(user)).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> getTsLap(String id) throws UsernameNotFoundException {
        Optional<TsLap> optionalTsLap = tsLapRepository.findById(Long.parseLong(id));
        TsLapDto tsLapDto = new TsLapDto(optionalTsLap.get());
        return new ResponseEntity<TsLapDto>(tsLapDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteTsLap(String id) throws UsernameNotFoundException {
        try {
            Optional<TsLap> tsLapOptional = tsLapRepository.findById(Long.parseLong(id));
            if (tsLapOptional.isPresent()) {
                TsLap TsLap = tsLapOptional.get();
                tsLapRepository.delete(TsLap);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}

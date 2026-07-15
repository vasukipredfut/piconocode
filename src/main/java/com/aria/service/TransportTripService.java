package com.aria.service;


import com.aria.dao.TransportTripRepository;
import com.aria.dto.*;
import com.aria.model.TransportTrip;
import com.aria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransportTripService {

    @Autowired
    private TransportTripRepository transportTripRepository;


    public ResponseEntity<?> getAllTransportTrip(Pageable pageable,String name,String cate) {
        Page<TransportTrip> expenses = transportTripRepository.filterTransportTrip(name,cate,pageable);
        List<TransportTripDto> expenseDtos = expenses.getContent().stream().map(val -> new TransportTripDto(val)).collect(Collectors.toList());
        PageDataDto<TransportTripDto> pageData = new PageDataDto<>(expenseDtos, expenses);
        return new ResponseEntity<PageDataDto<TransportTripDto>>(pageData, HttpStatus.OK);

    }

    public TransportTripDto getTransportTrip(Long id) {
        Optional<TransportTrip> optional = transportTripRepository.findById(id);
        if (optional.isPresent()) {
            return new TransportTripDto(optional.get());
        }
        return new TransportTripDto();
    }

    public void deleteTransportTrip(Long id) {
        transportTripRepository.deleteById(id);
    }

    public void saveTransportTrip(TransportTripDto expenseDto, User user) throws ParseException {
        TransportTrip expense = new TransportTrip(expenseDto);
        transportTripRepository.save(expense);
    }
}

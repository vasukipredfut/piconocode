package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransportTripExpenseService {

    @Autowired
    private TransportTripExpenseRepository transportTripExpenseRepository;

    @Autowired
    private TransportTripRepository transportTripRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    public void deleteTransportExpense(Long tripId){
        Optional<TransportTrip> optionalTransportTrip = transportTripRepository.findById(tripId);
        if (optionalTransportTrip.isPresent()) {
            transportTripExpenseRepository.deleteByTrip(optionalTransportTrip.get());
        }
    }

    public ResponseEntity getTransportExpense(Long tripId){
        Optional<TransportTrip> optionalTransportTrip = transportTripRepository.findById(tripId);
        if (optionalTransportTrip.isPresent()) {
            TransportTrip transportTrip=optionalTransportTrip.get();
            TransportTripExpenseDto transportTripExpenseDto = new TransportTripExpenseDto();
            transportTripExpenseDto.setTrip(new TransportTripDto(transportTrip));
            List<ExpenseDto> list = new ArrayList<>();
            for(TransportTripExpenses expense:transportTrip.getTransportTripExpensesList()){
                list.add(new ExpenseDto(expense.getExpense()));
            }
            transportTripExpenseDto.setExpenses(list);
            return new ResponseEntity<TransportTripExpenseDto>(transportTripExpenseDto, HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("404", "Not found!!!"), HttpStatus.NOT_FOUND);
    }

    public void mapTransportWithExpense(TransportTripExpenseDto transportTripExpenseDto) {
        List<TransportTripExpenses> list = new ArrayList<>();
        Optional<TransportTrip> optionalTransportTrip = transportTripRepository.findById(transportTripExpenseDto.getTrip().getId());
        if (optionalTransportTrip.isPresent()) {
            transportTripExpenseRepository.deleteByTrip(optionalTransportTrip.get());
            TransportTripExpenses transportTripExpenses = null;
            for (ExpenseDto expenseDto : transportTripExpenseDto.getExpenses()) {
                Optional<Expense> optionalExpense = expenseRepository.findById(expenseDto.getId());
                if (optionalExpense.isPresent()) {
                    transportTripExpenses = new TransportTripExpenses();
                    transportTripExpenses.setTrip(optionalTransportTrip.get());
                    transportTripExpenses.setExpense(optionalExpense.get());
                    transportTripExpenses.setSlab("");
                    transportTripExpenses.setSlabid(0L);
                    transportTripExpenses.setAdriver("");
                    transportTripExpenses.setDisporder(0L);
                    transportTripExpenses.setSwfare("");
                    list.add(transportTripExpenses);
                }
            }
            transportTripExpenseRepository.saveAll(list);
        }

    }

    public ResponseEntity<?> getAllTransportTripExpense(String tripName,String distance,Long tripId,Pageable pageable) {
        Page<TransportTrip> transportTripExpensesPage = transportTripExpenseRepository.getByTransportExpense(tripName,distance,tripId,pageable);
        List<TransportTripExpenseDto> result = new ArrayList<>();
        for(TransportTrip transportTrip:transportTripExpensesPage.getContent()){
            TransportTripExpenseDto transportTripExpenseDto = new TransportTripExpenseDto();
            transportTripExpenseDto.setTrip(new TransportTripDto(transportTrip));
            List<ExpenseDto> list = new ArrayList<>();
            for(TransportTripExpenses expense:transportTrip.getTransportTripExpensesList()){
                list.add(new ExpenseDto(expense.getExpense()));
            }
            transportTripExpenseDto.setExpenses(list);
            result.add(transportTripExpenseDto);
        }
        PageDataDto<TransportTripExpenseDto> pageData = new PageDataDto<>(result, transportTripExpensesPage);
        return new ResponseEntity<PageDataDto<TransportTripExpenseDto>>(pageData, HttpStatus.OK);
    }

    private TransportTripExpenseDto getTransportTripExpenseDto(List<TransportTripExpenses> list, TransportTrip trip) {
        TransportTripExpenseDto expenseDto = new TransportTripExpenseDto();
        expenseDto.setTrip(new TransportTripDto(trip));
        for (TransportTripExpenses transportTripExpenses : list) {
            expenseDto.getExpenses().add(new ExpenseDto(transportTripExpenses.getExpense()));
        }
        return expenseDto;
    }

}

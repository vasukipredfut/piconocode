package com.aria.service;

import com.aria.dao.ConsProductsRepository;
import com.aria.dto.ConsProductsDto;
import com.aria.model.ConsProducts;
import com.aria.model.Status;
import com.aria.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ConsProductsService {

    @Autowired
    ConsProductsRepository consProductsRepository;


    public ResponseEntity<?> saveConsProducts(ConsProductsDto consProductsDto, User loggedUser) {
        ConsProducts consProducts = new ConsProducts(consProductsDto);
        ConsProducts result = consProductsRepository.save(consProducts);
        return new ResponseEntity<Status>(new Status("200", "ConsProducts created SuccessFully"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateConsProducts(ConsProductsDto consProductsDto, User loggedUser) {
        ConsProducts consProducts = new ConsProducts(consProductsDto);
        ConsProducts result = consProductsRepository.save(consProducts);
        return new ResponseEntity<Status>(new Status("200", "ConsProducts updated SuccessFully"), HttpStatus.OK);
    }


    public ResponseEntity<?> getConsProducts(String id) throws UsernameNotFoundException {
        Optional<ConsProducts> optionalConsProducts = consProductsRepository.findById(Long.parseLong(id));
        if (optionalConsProducts.isPresent()) {
            ConsProductsDto consProductsDto = new ConsProductsDto(optionalConsProducts.get());
            return new ResponseEntity<ConsProductsDto>(consProductsDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<?> deleteConsProducts(String id) throws UsernameNotFoundException {
        try {
            Optional<ConsProducts> optionalConsProducts = consProductsRepository.findById(Long.parseLong(id));
            if (optionalConsProducts.isPresent()) {
                ConsProducts consProducts = optionalConsProducts.get();
                consProductsRepository.delete(consProducts);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
}

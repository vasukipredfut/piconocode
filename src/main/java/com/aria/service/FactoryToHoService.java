package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FactoryToHoService {


    @Autowired
    PurchaseClaimPremiumRepository purchaseClaimPremiumRepository;

    @Autowired
    PurchaseBrokerageBreakupRepository purchaseBrokerageBreakupRepository;

    @Autowired
    PurchaseBrokerageRepository brokerageRepository;

    @Autowired
    PurchaseClaimPremiumBreakupRepository purchaseClaimPremiumBreakupRepository;

    @Autowired
    PurchaseClaimPremiumDetailRepository premiumDetailRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BinStockRepository binStockRepository;


    // INVENTRY


    public ResponseEntity<?> currentStockList(Long branchId,  Long productId, String productName,
                                               String productCategory, String binNo, Double lot, Pageable pageable) throws UsernameNotFoundException {

        Page<HOBinStockList> list = binStockRepository.findBy(branchId, productId, productName, productCategory, binNo, lot, pageable);
        if (!list.isEmpty()) {
            List<HOBinStock> result = list.stream().map(data -> {
                HOBinStock binStockDto = new HOBinStock(data);
                return binStockDto;
            }).collect(Collectors.toList());
            PageDataDto<HOBinStock> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<HOBinStock>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<HOBinStock>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    //PurchaseClaimPremium

    public ResponseEntity<?> savePurchaseClaimPremium(PurchaseClaimPremiumDto purchaseClaimPremiumDto, User loggedUser) {
        PurchaseClaimPremium purchaseClaimPremium = new PurchaseClaimPremium(purchaseClaimPremiumDto);
        PurchaseClaimPremium result = purchaseClaimPremiumRepository.save(purchaseClaimPremium);
        return new ResponseEntity<Status>(new Status("200", "PurchaseClaimPremium created successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePurchaseClaimPremium(PurchaseClaimPremiumDto purchaseClaimPremiumDto, User loggedUser) {
        PurchaseClaimPremium purchaseClaimPremium = new PurchaseClaimPremium(purchaseClaimPremiumDto);
        PurchaseClaimPremium result = purchaseClaimPremiumRepository.save(purchaseClaimPremium);
        return new ResponseEntity<Status>(new Status("200", "PurchaseClaimPremium updated successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllPurchaseClaimPremium(Pageable pageable) throws UsernameNotFoundException {
        Page<PurchaseClaimPremium> purchaseClaimPremiums = purchaseClaimPremiumRepository.findAll(pageable);
        return new ResponseEntity<List<PurchaseClaimPremiumDto>>(purchaseClaimPremiums.toList().stream().map(purchaseClaimPremium -> new PurchaseClaimPremiumDto(purchaseClaimPremium)).collect(Collectors.toList()), HttpStatus.OK);
    }


    public ResponseEntity<?> getPurchaseClaimPremiumDetails(String id) throws UsernameNotFoundException {
        Optional<PurchaseClaimPremium> optionalPurchaseClaimPremium = purchaseClaimPremiumRepository.findById(Long.parseLong(id));
        PurchaseClaimPremiumDto purchaseClaimPremiumDto = new PurchaseClaimPremiumDto(optionalPurchaseClaimPremium.get());
        return new ResponseEntity<PurchaseClaimPremiumDto>(purchaseClaimPremiumDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePurchaseClaimPremiumDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<PurchaseClaimPremium> optionalPurchaseClaimPremium = purchaseClaimPremiumRepository.findById(Long.parseLong(id));
            if (optionalPurchaseClaimPremium.isPresent()) {
                PurchaseClaimPremium purchaseClaimPremium = optionalPurchaseClaimPremium.get();
                purchaseClaimPremiumRepository.delete(purchaseClaimPremium);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


//PurchaseClaimPremiumDetail


    public ResponseEntity<?> savePurchaseClaimPremiumDetail(PurchaseClaimPremiumDetailDto purchaseClaimPremiumDto, User loggedUser) {
        PurchaseClaimPremiumDetail purchaseClaimPremium = new PurchaseClaimPremiumDetail(purchaseClaimPremiumDto);
        PurchaseClaimPremiumDetail result = premiumDetailRepository.save(purchaseClaimPremium);
        return new ResponseEntity<Status>(new Status("200", "PurchaseClaimPremiumDetail created successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePurchaseClaimPremiumDetail(PurchaseClaimPremiumDetailDto purchaseClaimPremiumDto, User loggedUser) {
        PurchaseClaimPremiumDetail purchaseClaimPremium = new PurchaseClaimPremiumDetail(purchaseClaimPremiumDto);
        PurchaseClaimPremiumDetail result = premiumDetailRepository.save(purchaseClaimPremium);
        return new ResponseEntity<Status>(new Status("200", "PurchaseClaimPremiumDetail updated successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllPurchaseClaimPremiumDetail(Pageable pageable) throws UsernameNotFoundException {
        Page<PurchaseClaimPremiumDetail> purchaseClaimPremiums = premiumDetailRepository.findAll(pageable);
        return new ResponseEntity<List<PurchaseClaimPremiumDetailDto>>(purchaseClaimPremiums.toList().stream().map(purchaseClaimPremium -> new PurchaseClaimPremiumDetailDto(purchaseClaimPremium)).collect(Collectors.toList()), HttpStatus.OK);
    }


    public ResponseEntity<?> getPurchaseClaimPremiumDetailById(String id) throws UsernameNotFoundException {
        Optional<PurchaseClaimPremiumDetail> optionalPurchaseClaimPremium = premiumDetailRepository.findById(Long.parseLong(id));
        PurchaseClaimPremiumDetailDto purchaseClaimPremiumDto = new PurchaseClaimPremiumDetailDto(optionalPurchaseClaimPremium.get());
        return new ResponseEntity<PurchaseClaimPremiumDetailDto>(purchaseClaimPremiumDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePurchaseClaimPremiumDetailById(String id) throws UsernameNotFoundException {
        try {
            Optional<PurchaseClaimPremiumDetail> optionalPurchaseClaimPremium = premiumDetailRepository.findById(Long.parseLong(id));
            if (optionalPurchaseClaimPremium.isPresent()) {
                PurchaseClaimPremiumDetail purchaseClaimPremium = optionalPurchaseClaimPremium.get();
                premiumDetailRepository.delete(purchaseClaimPremium);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


//PurchaseClaimPremiumBreakup


    public ResponseEntity<?> savePurchaseClaimPremiumBreakup(PurchaseClaimPremiumBreakupDto purchaseClaimPremiumBreakupDto, User loggedUser) {
        PurchaseClaimPremiumBreakup purchaseClaimPremium = new PurchaseClaimPremiumBreakup(purchaseClaimPremiumBreakupDto);
        PurchaseClaimPremiumBreakup result = purchaseClaimPremiumBreakupRepository.save(purchaseClaimPremium);
        return new ResponseEntity<Status>(new Status("200", "PurchaseClaimPremiumBreakup created successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePurchaseClaimPremiumBreakup(PurchaseClaimPremiumBreakupDto purchaseClaimPremiumDto, User loggedUser) {
        PurchaseClaimPremiumBreakup purchaseClaimPremium = new PurchaseClaimPremiumBreakup(purchaseClaimPremiumDto);
        PurchaseClaimPremiumBreakup result = purchaseClaimPremiumBreakupRepository.save(purchaseClaimPremium);
        return new ResponseEntity<Status>(new Status("200", "PurchaseClaimPremiumDetail updated successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> listAllPurchaseClaimPremiumBreakup(Pageable pageable) throws UsernameNotFoundException {
        Page<PurchaseClaimPremiumBreakup> purchaseClaimPremiums = purchaseClaimPremiumBreakupRepository.findAll(pageable);
        return new ResponseEntity<List<PurchaseClaimPremiumBreakupDto>>(purchaseClaimPremiums.toList().stream().map(purchaseClaimPremium -> new PurchaseClaimPremiumBreakupDto(purchaseClaimPremium)).collect(Collectors.toList()), HttpStatus.OK);
    }


    public ResponseEntity<?> getPurchaseClaimPremiumBreakupDetailById(String id) throws UsernameNotFoundException {
        Optional<PurchaseClaimPremiumBreakup> optionalPurchaseClaimPremium = purchaseClaimPremiumBreakupRepository.findById(Long.parseLong(id));
        PurchaseClaimPremiumBreakupDto purchaseClaimPremiumDto = new PurchaseClaimPremiumBreakupDto(optionalPurchaseClaimPremium.get());
        return new ResponseEntity<PurchaseClaimPremiumBreakupDto>(purchaseClaimPremiumDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePurchaseClaimPremiumBreakupDetailById(String id) throws UsernameNotFoundException {
        try {
            Optional<PurchaseClaimPremiumBreakup> optionalPurchaseClaimPremium = purchaseClaimPremiumBreakupRepository.findById(Long.parseLong(id));
            if (optionalPurchaseClaimPremium.isPresent()) {
                PurchaseClaimPremiumBreakup purchaseClaimPremium = optionalPurchaseClaimPremium.get();
                purchaseClaimPremiumBreakupRepository.delete(purchaseClaimPremium);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    //PurchaseBrokerage

    public ResponseEntity<?> savePurchaseBrokerage(PurchaseBrokerageDto purchaseBrokerageDto, User loggedUser) {
        PurchaseBrokerage purchaseBrokerage = new PurchaseBrokerage(purchaseBrokerageDto);
        PurchaseBrokerage result = brokerageRepository.save(purchaseBrokerage);
        return new ResponseEntity<Status>(new Status("200", "PurchaseBrokerage created successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePurchaseBrokerage(PurchaseBrokerageDto purchaseBrokerageDto, User loggedUser) {
        PurchaseBrokerage purchaseBrokerage = new PurchaseBrokerage(purchaseBrokerageDto);
        PurchaseBrokerage result = brokerageRepository.save(purchaseBrokerage);
        return new ResponseEntity<Status>(new Status("200", "PurchaseBrokerage updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllPurchaseBrokerage(Pageable pageable) throws UsernameNotFoundException {
        Page<PurchaseBrokerage> purchaseClaimPremiums = brokerageRepository.findAll(pageable);
        return new ResponseEntity<List<PurchaseBrokerageDto>>(purchaseClaimPremiums.toList().stream().map(purchaseClaimPremium -> new PurchaseBrokerageDto(purchaseClaimPremium)).collect(Collectors.toList()), HttpStatus.OK);
    }


    public ResponseEntity<?> getPurchaseBrokerageById(String id) throws UsernameNotFoundException {
        Optional<PurchaseBrokerage> optional = brokerageRepository.findById(Long.parseLong(id));
        PurchaseBrokerageDto purchaseClaimPremiumDto = new PurchaseBrokerageDto(optional.get());
        return new ResponseEntity<PurchaseBrokerageDto>(purchaseClaimPremiumDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePurchaseBrokerageById(String id) throws UsernameNotFoundException {
        try {
            Optional<PurchaseBrokerage> optionalPurchaseClaimPremium = brokerageRepository.findById(Long.parseLong(id));
            if (optionalPurchaseClaimPremium.isPresent()) {
                PurchaseBrokerage purchaseClaimPremium = optionalPurchaseClaimPremium.get();
                brokerageRepository.delete(purchaseClaimPremium);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    //PurchaseBrokerageBreakup

    public ResponseEntity<?> savePurchaseBrokerageBreakup(PurchaseBrokerageBreakupDto purchaseBrokerageDto, User loggedUser) {
        PurchaseBrokerageBreakup purchaseBrokerage = new PurchaseBrokerageBreakup(purchaseBrokerageDto);
        PurchaseBrokerageBreakup result = purchaseBrokerageBreakupRepository.save(purchaseBrokerage);
        return new ResponseEntity<Status>(new Status("200", "PurchaseBrokerage created successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePurchaseBrokerageBreakup(PurchaseBrokerageBreakupDto purchaseBrokerageDto, User loggedUser) {
        PurchaseBrokerageBreakup purchaseBrokerage = new PurchaseBrokerageBreakup(purchaseBrokerageDto);
        PurchaseBrokerageBreakup result = purchaseBrokerageBreakupRepository.save(purchaseBrokerage);
        return new ResponseEntity<Status>(new Status("200", "PurchaseBrokerage updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllPurchaseBrokerageBreakup(Pageable pageable) throws UsernameNotFoundException {
        Page<PurchaseBrokerageBreakup> purchaseClaimPremiums = purchaseBrokerageBreakupRepository.findAll(pageable);
        return new ResponseEntity<List<PurchaseBrokerageBreakupDto>>(purchaseClaimPremiums.toList().stream().map(purchaseClaimPremium -> new PurchaseBrokerageBreakupDto(purchaseClaimPremium)).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> getPurchaseBrokerageBreakupById(String id) throws UsernameNotFoundException {
        Optional<PurchaseBrokerageBreakup> optional = purchaseBrokerageBreakupRepository.findById(Long.parseLong(id));
        PurchaseBrokerageBreakupDto purchaseClaimPremiumDto = new PurchaseBrokerageBreakupDto(optional.get());
        return new ResponseEntity<PurchaseBrokerageBreakupDto>(purchaseClaimPremiumDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletePurchaseBrokerageBreakupById(String id) throws UsernameNotFoundException {
        try {
            Optional<PurchaseBrokerageBreakup> optionalPurchaseClaimPremium = purchaseBrokerageBreakupRepository.findById(Long.parseLong(id));
            if (optionalPurchaseClaimPremium.isPresent()) {
                PurchaseBrokerageBreakup purchaseClaimPremium = optionalPurchaseClaimPremium.get();
                purchaseBrokerageBreakupRepository.delete(purchaseClaimPremium);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


}

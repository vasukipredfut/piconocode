package com.aria.service;


import com.aria.dao.*;
import com.aria.dto.HsnMasterDto;
import com.aria.dto.HsnSpecDto;
import com.aria.dto.PageDataDto;

import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HsnService {

    @Autowired
    GstTaxCategoryRepository hsnMasterRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    PurchaseExpenseRepository purchaseExpenseRepository;

    @Autowired
    ProductMappingRepository productMappingRepo;


    public ResponseEntity<?> saveHsnDetail(HsnMasterDto hsnMasterDto, User loggedUser) {

        GstTaxCategory gstTaxCategory = hsnMasterRepository.findByHsnCode(hsnMasterDto.getHsnCode());
        if (gstTaxCategory != null) {
            return new ResponseEntity<Status>(new Status("400", "HsnCode already exist!!!"), HttpStatus.OK);
        }

        GstTaxCategory hsn = new GstTaxCategory(hsnMasterDto);
        hsn.setEmasterId(loggedUser.getId());
        hsn.setCreatedTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        hsnMasterRepository.save(hsn);
        updateHsn(hsnMasterDto);
        return new ResponseEntity<Status>(new Status("200", "Hsn Detail saved successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> updateHsn(HsnMasterDto hsnMasterDto, User loggedUser) {
        GstTaxCategory hsn = new GstTaxCategory(hsnMasterDto);
        hsn.setEmasterId(loggedUser.getId());
        hsn.setCreatedTimestamp(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        hsnMasterRepository.save(hsn);
        updateHsn(hsnMasterDto);
        return new ResponseEntity<Status>(new Status("200", "Hsn Detail saved successfully!!!"), HttpStatus.OK);
    }


    public void updateHsn(HsnMasterDto hsn) {

        if (hsn.getCategory().equalsIgnoreCase("P")) {
            deleteItems(hsn.getHsnCode());
            for (Map<Long, String> stringMap : hsn.getMapItems()) {
                for (Long itemId : stringMap.keySet()) {
                    updateItems(itemId, hsn.getHsnCode());
                }
            }

        } else if (hsn.getCategory().equalsIgnoreCase("M")) {
            deleteMachines(hsn.getHsnCode());
            for (Map<Long, String> stringMap : hsn.getMapItems()) {
                for (Long machineId : stringMap.keySet()) {
                    updateMachines(machineId, hsn.getHsnCode());
                }
            }

        } else if (hsn.getCategory().equalsIgnoreCase("E")) {
            deleteExpense(hsn.getHsnCode());
            for (Map<Long, String> stringMap : hsn.getMapItems()) {
                for (Long expenseId : stringMap.keySet()) {
                    updateExpense(expenseId, hsn.getHsnCode());
                }
            }

        } else if (hsn.getCategory().equalsIgnoreCase("M")) {

        }
    }

    public void updateItems(Long itemId, String hsnCode) {

        Optional<ProductMaster> productMaster = productMasterRepository.findById(itemId);
        if (productMaster.isPresent()) {
            ProductMaster productMaster1 = productMaster.get();
            productMaster1.setHsnRefId(hsnCode);
            productMasterRepository.save(productMaster1);
        }
    }

    public void deleteItems(String hsnCode) {

         productMasterRepository.updateHsn(hsnCode);

    }

    public void updateMachines(Long machiId, String hsnCode) {

        Optional<Machine> optional = machineRepository.findById(machiId);
        if (optional.isPresent()) {
            Machine machine = optional.get();
            machine.setHsnRefId(hsnCode);
            machineRepository.save(machine);
        }
    }

    public void deleteMachines( String hsnCode) {
         machineRepository.updateHsn(hsnCode);
    }


    public void updateExpense(Long expenseId, String hsnCode) {

        Optional<PurchaseExpense> optional = purchaseExpenseRepository.findById(expenseId);
        if (optional.isPresent()) {
            PurchaseExpense purchaseExpense = optional.get();
            purchaseExpense.setHsn_Ref_Id(hsnCode);
            purchaseExpenseRepository.save(purchaseExpense);
        }
    }

    public void deleteExpense( String hsnCode) {

        purchaseExpenseRepository.updateHsn(hsnCode);

    }


    public ResponseEntity<?> getHsnDetail(Long id) {
        Optional<GstTaxCategory> hsnMasterOptional = hsnMasterRepository.findById(id);
        if (hsnMasterOptional.isPresent()) {
            HsnMasterDto hsn = new HsnMasterDto(hsnMasterOptional.get(), true);
            return new ResponseEntity<HsnMasterDto>(hsn, HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);

    }


    public ResponseEntity<?> getHsnItemId(Long itemId, String category) {

        if (category.equalsIgnoreCase("P")) {

            System.out.println("itemId == " + itemId);

            Optional<ProductMaster> productMaster = productMasterRepository.findById(itemId);
            if (productMaster.isPresent()) {
                GstTaxCategory gstTaxCategory = hsnMasterRepository.findByHsnCode(productMaster.get().getHsnRefId(), category);
                if (gstTaxCategory != null) {
                    HsnMasterDto hsn = new HsnMasterDto(gstTaxCategory, true);
                    return new ResponseEntity<HsnMasterDto>(hsn, HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);
            }
        } else if (category.equalsIgnoreCase("M")) {
            Optional<Machine> machine = machineRepository.findById(itemId);
            if (machine.isPresent()) {
                GstTaxCategory gstTaxCategory = hsnMasterRepository.findByHsnCode(machine.get().getHsnRefId(), category);
                if (gstTaxCategory != null) {
                    HsnMasterDto hsn = new HsnMasterDto(gstTaxCategory, true);
                    return new ResponseEntity<HsnMasterDto>(hsn, HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);
            }
        } else if (category.equalsIgnoreCase("E")) {
            Optional<PurchaseExpense> purchaseExpense = purchaseExpenseRepository.findById(itemId);
            if (purchaseExpense.isPresent()) {
                GstTaxCategory gstTaxCategory = hsnMasterRepository.findByHsnCode(purchaseExpense.get().getHsn_Ref_Id(), category);
                if (gstTaxCategory != null) {
                    HsnMasterDto hsn = new HsnMasterDto(gstTaxCategory, true);
                    return new ResponseEntity<HsnMasterDto>(hsn, HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);
            }
        }

        return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);
    }


    public ResponseEntity<?> deleteHsnDetail(Long id) {
        Optional<GstTaxCategory> hsnMasterOptional = hsnMasterRepository.findById(id);
        if (hsnMasterOptional.isPresent()) {
            hsnMasterRepository.delete(hsnMasterOptional.get());
            return new ResponseEntity<Status>(new Status("200", "Record Deleted!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);

    }

    public ResponseEntity<?> getHsnDetailAll(String hsn, String category, String itemCategory, Pageable pageable) {
        Page<GstTaxCategory> page = hsnMasterRepository.hsnFilterList(hsn, category, itemCategory, pageable);
        if (!page.toList().isEmpty()) {
            List<HsnMasterDto> result = page.toList().stream().map(data -> new HsnMasterDto(data, false)).collect(Collectors.toList());
            PageDataDto<HsnMasterDto> pageData = new PageDataDto<>(result, page);
            return new ResponseEntity<PageDataDto<HsnMasterDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<HsnMasterDto>>(new PageDataDto(), HttpStatus.OK);
        }

    }


    public ResponseEntity<?> hsnItemList(User user, String search, Long specType) {


        if (specType == 28) {
            List<ProductMaster> list = productMasterRepository.findBy(search);
            List<HsnSpecDto> result = list.stream().map(data -> new HsnSpecDto(data, specType)).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);

        } else if (specType == 29) {

            List<Machine> list = machineRepository.findBy(search);

            List<HsnSpecDto> result = list.stream().map(data -> new HsnSpecDto(data, specType)).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);

        } else if (specType == 30) {

            List<PurchaseExpense> list = purchaseExpenseRepository.findBy(specType, search);
            List<HsnSpecDto> result = list.stream().map(data -> new HsnSpecDto(data, specType)).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);

        } else if (specType == 31) {

            List<PurchaseExpense> list = purchaseExpenseRepository.findBy(specType, search);

            List<HsnSpecDto> result = list.stream().map(data -> {
                HsnSpecDto rs = new HsnSpecDto(data, specType);
                if (rs.getAmc_machine_ref_Id() != null && rs.getAmc_machine_ref_Id() != 0) {
                    Optional<Machine> optional = machineRepository.findById(rs.getAmc_machine_ref_Id());
                    if (optional.isPresent()) {
                        rs.setAmc_machine_ref_Name(optional.get().getName());
                    }
                }
                return rs;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<HsnSpecDto>>(result, HttpStatus.OK);

        } else {
            return new ResponseEntity<List<HsnSpecDto>>(HttpStatus.NOT_FOUND);
        }

    }


    public ResponseEntity<?> validateHsn(String hsn, Long itemId, String category) {

        if (category.equalsIgnoreCase("P")) {

            Optional<ProductMaster> productMaster = productMasterRepository.findByHsn(itemId, hsn);
            if (productMaster.isPresent()) {
                return new ResponseEntity<Status>(new Status("400", "Already Exist!!!"), HttpStatus.OK);
            } else {
                return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);
            }

        } else if (category.equalsIgnoreCase("M")) {

            Optional<Machine> machine = machineRepository.findByHsn(itemId, hsn);
            if (machine.isPresent()) {
                return new ResponseEntity<Status>(new Status("400", "Already Exist!!!"), HttpStatus.OK);
            } else {
                return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);
            }

        } else if (category.equalsIgnoreCase("E")) {

            Optional<PurchaseExpense> purchaseExpense = purchaseExpenseRepository.findByHsn(itemId, hsn);
            if (purchaseExpense.isPresent()) {
                return new ResponseEntity<Status>(new Status("400", "Already Exist!!!"), HttpStatus.OK);
            } else {
                return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);
            }

        }


        return new ResponseEntity<Status>(new Status("404", "Record not found!!!"), HttpStatus.NOT_FOUND);


    }

}

package com.aria.service;

import com.aria.dao.BinStockRepository;
import com.aria.dto.BinLockDto;
import com.aria.dto.BinLockStock;
import com.aria.dto.BinLockStockDto;
import com.aria.dto.PageDataDto;
import com.aria.model.BinLock;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BinStockLockService {

    private final BinStockRepository binStockRepository;

    public ResponseEntity<?> fetchAllBinLock(Long branchId, String lockType, Long binNumber, Long productId, Pageable pageable) {
        Page<BinLockStock> binStockPage = binStockRepository.filterBinLockByLockTypeAndStatus(branchId, productId, binNumber, lockType, pageable);

        if (!binStockPage.isEmpty()) {
            List<BinLockStockDto> binLockStocks = binStockPage.stream().map(BinLockStockDto::new).collect(Collectors.toList());
            PageDataDto<BinLockStockDto> pageData = new PageDataDto<>(binLockStocks, pageable, binStockPage);
            return new ResponseEntity<>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> fetchAllBinLock(Long branchId, String lockType, Long binNumber, Long productId) {
        List<BinLockStock> binStocks = binStockRepository.filterBinLockByLockTypeAndStatus( branchId, productId, binNumber, lockType);

        if (!binStocks.isEmpty()) {
            List<BinLockStockDto> binLockStocks = binStocks.stream().map(BinLockStockDto::new).collect(Collectors.toList());
            return new ResponseEntity<>(binLockStocks, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> getBinDropdown(Long branchId, String lockType) {
        List<BinLock> binLocks = binStockRepository.findAllBinNumberLocked(branchId,lockType);
        if (!binLocks.isEmpty()) {
            List<BinLockDto> binLockDtos = binLocks.stream().map(BinLockDto::new).collect(Collectors.toList());
            return new ResponseEntity<>(binLockDtos, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
        }

    }
}

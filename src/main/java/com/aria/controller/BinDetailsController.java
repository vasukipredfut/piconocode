
package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.aria.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.dao.UserRepository;
import com.aria.model.GoGroup;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.service.BinDetailsServices;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class BinDetailsController {

    @Autowired
    BinDetailsServices binDetailsServices;

    @Autowired
    UserRepository userRepository;


    @GetMapping("/filterBinStock")
    public ResponseEntity<?> filterBinStock(@RequestParam(value = "binStockId", required = false) Long binStockId,
                                            @RequestParam(value = "firmId", required = false) Long firmId,
                                            @RequestParam(value = "branchId", required = false) Long branchId,
                                            @RequestParam(value = "deptId", required = false) Long deptId,
                                            @RequestParam(value = "batchId", required = false) Long batchId,
                                            @RequestParam(value = "batchNo", required = false) String batchNo,
                                            @RequestParam(value = "productId", required = false) Long productId,
                                            @RequestParam(value = "binName", required = false) Long binName,
                                            @RequestParam(value = "lot", required = false) Double lot, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllBinStock(binStockId , firmId ,  branchId, deptId, productId, batchId, batchNo, lot, binName);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/cumulative/filterBinStock")
    public ResponseEntity<?> filterBinStockGroupByBranchDept(
            @RequestParam(value = "firmId", required = false) Long firmId,
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "batchNo", required = false) String batchNo,
            @RequestParam(value = "productId", required = false) Long productId,
            @RequestParam(value = "lot", required = false) Double lot, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllBinStockSearchGroupByBranchDept( firmId, branchId, deptId, productId, batchId,
                    batchNo, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/filterBinStockList")
    public ResponseEntity<?> filterBinStock(@RequestParam(value = "branchId", required = false) Long branchId,
                                            @RequestParam(value = "deptId", required = false) Long deptId,
                                            @RequestParam(value = "productId", required = false) Long productId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllBinStocklist(branchId, deptId, productId);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/filterBinStockWithPagination")
    public ResponseEntity<?> filterBinStockWithPagination(
            @RequestParam(value = "branchId", required = false) Long branchId,
            @RequestParam(value = "deptId", required = false) Long deptId,
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "batchNo", required = false) String batchNo,
            @RequestParam(value = "productId", required = false) Long productId, Pageable pageable,
            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.filterBinStockWithPagination(branchId, deptId, productId, batchId, batchNo,
                    pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/listAllQcBinList")
    public ResponseEntity<?> filterQcBinList(@RequestParam(value = "branch", required = false) Long branch,
                                             @RequestParam(value = "binNo", required = false) Long binNo,
                                             Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllQCBinList(branch, binNo);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }



    @GetMapping("/getDpBinDetails")
    public ResponseEntity<?> getDpBinDetails(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.dpBinANDStock(user.get().getBranch_id());
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getBinDetailsForBinNo")
    public ResponseEntity<?> getBinDetailsForBinNo( @RequestParam(value = "binNo", required = false) String binNo,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getBinDetailsForBinNo(binNo);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/listAllDeptWiseBinList")
    public ResponseEntity<?> listAllDeptWiseBinList(@RequestParam(value = "type", required = false) String type,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllDeptWiseBinList(user.get().getBranch_id(), user.get().getDepartment().getId(), type);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/asrsListAllDeptWiseBinList")
    public ResponseEntity<?> asrsListAllDeptWiseBinList(@RequestParam(value = "type", required = false) String type,
                                                        @RequestParam(value = "asrs", required = false) boolean asrs,
                                                        @RequestParam(value = "trnNo", required = false) String trnNo,
                                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.asrsListAllDeptWiseBinList(asrs , trnNo , user.get().getBranch_id(), user.get().getDepartment().getId(), type);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }





    @GetMapping("/listAllDeptWiseBinWithStockList")
    public ResponseEntity<?> listAllDeptWiseBinWithStockList(@RequestParam(value = "type", required = false) String type,
                                                    Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllDeptWiseBinWithStockList(user.get().getBranch_id(), user.get().getDepartment().getId(), type);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    // BinLock

    @PostMapping("/saveBinLock")
    public ResponseEntity<?> saveBinLock(@RequestBody BinLockRequestDto binDetailsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return binDetailsServices.saveBinLock(binDetailsDto, user.get());
    }

    @GetMapping("/listBinLockAll")
    public ResponseEntity<?> listBinLockAll(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listBinLockAll();
        } else {
            return new ResponseEntity<List<BinLockDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/getBinLockDetails/{id}")
    public ResponseEntity<?> getBinLockDetails(@PathVariable String id, Authentication authentication) {
        return binDetailsServices.getBinLockDetails(id);
    }



    @GetMapping("/filterBinLock")
    public ResponseEntity<?> filterBinLock(@RequestParam(value = "productId", required = false) Long productId,
                                           @RequestParam(value = "binNo", required = false) String binNo,
                                           @RequestParam(value = "batchNo", required = false) String batchNo,
                                           @RequestParam(value = "lockType", required = false) String lockType,
                                           @RequestParam(value = "status", required = false) String status,
                                           @RequestParam(value = "dateFrom", required = false) String dateFrom,
                                           @RequestParam(value = "dateTo", required = false) String dateTo, Pageable pageable,
                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.filterBinLock(productId, binNo, batchNo, lockType, status, dateFrom, dateTo, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getStockByBranch")
    public ResponseEntity<?> getStockByBranch(@RequestParam(value = "firmId", required = false) Long firmId,
                                              @RequestParam(value = "productId", required = false) Long productId,
                                              @RequestParam(value = "lockType", required = false) String lockType,
                                              @RequestParam(value = "status", required = false) String status,
                                              @RequestParam(value = "lot", required = false) Double lot,
                                              Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getStockByBranch(firmId , productId, lockType, status, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    // GoDown

    @GetMapping("/goDown")
    public ResponseEntity<?> listGoDown(@RequestParam(value = "machineId", required = false) String machineId,
                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllGoDown(machineId);
        } else {
            return new ResponseEntity<List<GoDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/saveGoDown")
    public ResponseEntity<?> saveGoDown(@RequestBody GoDownDto goDownDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return binDetailsServices.saveGoDownDetails(goDownDto, user.get());
    }

    @PutMapping("/updateGoDown")
    public ResponseEntity<?> updateGoDown(@RequestBody GoDownDto goDownDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return binDetailsServices.updateGoDownDetails(goDownDto, user.get());
    }

    @GetMapping("/getGoDownDetails/{id}")
    public ResponseEntity<?> getGoDownDetails(@PathVariable String id, Authentication authentication) {
        return binDetailsServices.getGoDownDetails(id);
    }

    @DeleteMapping("/deleteGoDownDetails/{id}")
    public ResponseEntity<?> deleteGoDownDetails(@PathVariable String id, Authentication authentication) {
        return binDetailsServices.deleteGoDownDetails(id);
    }

    @GetMapping("/goDownList")
    public ResponseEntity<?> goDownList(@RequestParam(value = "goDownName", required = false) String goDownName,
                                        @RequestParam(value = "goDownType", required = false) String goDownType,
                                        @RequestParam(value = "branch", required = false) String branch,
                                        @RequestParam(value = "status", required = false) String status, Pageable pageable,
                                        Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllGoDown(goDownName, goDownType, branch, status, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/goDownAllList")
    public ResponseEntity<?> goDownAllList(@RequestParam(value = "goDownName", required = false) String goDownName,
                                           @RequestParam(value = "goDownType", required = false) String goDownType,
                                           @RequestParam(value = "branch", required = false) String branch,
                                           @RequestParam(value = "status", required = false) String status,@RequestParam(value = "firmId", required = false) Long firmId, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllGoDown(goDownName, goDownType, branch, status,firmId);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    // BinRange or Binmaster

    @PostMapping("/saveBinRange")
    public ResponseEntity<?> saveBinRange(@RequestBody BinRangeDto binRangeDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return binDetailsServices.saveBinRangeDetails(binRangeDto, user.get());
    }

    @PutMapping("/updateBinRange")
    public ResponseEntity<?> updateGoDown(@RequestBody BinRangeDto binRangeDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return binDetailsServices.updateBinRangeDetails(binRangeDto, user.get());
    }

    @GetMapping("/getBinRangeDetails/{id}")
    public ResponseEntity<?> getBinRangeDetails(@PathVariable String id, Authentication authentication) {
        return binDetailsServices.getBinRangeDetails(id);
    }

    @DeleteMapping("/deleteBinRangeDetails/{id}")
    public ResponseEntity<?> deleteBinRangeDetails(@PathVariable String id, Authentication authentication) {
        return binDetailsServices.deleteBinRangeDetails(id);
    }

    @GetMapping("/binRangeList")
    public ResponseEntity<?> binRangeList(@RequestParam(value = "department", required = false) String department,
                                          @RequestParam(value = "binNo", required = false) Long binNo,
                                          @RequestParam(value = "goDownName", required = false) String goDownName, Pageable pageable,
                                          Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllBinRange(department, binNo, goDownName, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }


    @GetMapping("/findByBinFilterList")
    public ResponseEntity<?> findByBinFilterList(
            @RequestParam(value = "slfm", required = false) Long slfm,
            @RequestParam(value = "slto", required = false) Long slto,
            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.findByBinFilterList(slfm, slto);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    // Bin List

    @GetMapping("/binList")
    public ResponseEntity<?> binList(@RequestParam(value = "dept", required = false) Long dept,
                                     @RequestParam(value = "branch", required = false) Long branch,
                                     @RequestParam(value = "goDownName", required = false) String goDownName,
                                     @RequestParam(value = "firmName", required = false) String firmName, Pageable pageable,
                                     Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (dept != null && branch != null) {
                return binDetailsServices.listAllBinList(branch, dept, goDownName, firmName, pageable);
            } else {
                return binDetailsServices.listAllBinList(user.get().getBranch_id(), user.get().getDepartment().getId(),
                        goDownName, firmName, pageable);
            }
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @PostMapping("/createBin")
    public ResponseEntity<?> saveBinRange(@RequestBody List<GoGroupDto> goGroupDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        try {
        return binDetailsServices.saveGoGroupDetails(goGroupDto, user.get());
        }catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new Status("400", e.getMessage()));
        }
    }

    @GetMapping("/getPalletType")
    public ResponseEntity<?> getPalletType(@RequestParam("palletNumber") String palletNumber, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getPalletTye(palletNumber, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
       
    @GetMapping("/getBinDetailsList")
    public ResponseEntity<?> getBinDetailsList(@RequestParam(value = "godownId", required = false) Long godownId,
                                               @RequestParam(value = "firmId", required = false) Long firmId,
                                               @RequestParam(value = "branch", required = false) Long branch,
                                               @RequestParam(value = "dept", required = false) Long dept,
                                               @RequestParam(value = "slfm", required = false) Long slfm,
                                               @RequestParam(value = "slto", required = false) Long slto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (dept != null && branch != null) {
            return binDetailsServices.getGoGroupDetails(godownId, firmId, branch, dept, slfm, slto);
        } else {
            return binDetailsServices.getGoGroupDetails(godownId, firmId, user.get().getBranch_id(),
                    user.get().getDepartment().getId(), slfm, slto);
        }
    }

    @GetMapping("/findBinNo")
    public ResponseEntity<?> findBinNo(@RequestParam(value = "binType", required = false) String binType,
                                       @RequestParam(value = "binNo", required = false) Long binNo,
                                       @RequestParam(value = "branch", required = false) Long branch,
                                       @RequestParam(value = "godownId", required = false) Long godownId,
                                       @RequestParam(value = "dept", required = false) Long dept, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (dept != null || branch != null) {
            return binDetailsServices.findBinNo(binType, binNo, branch, dept, godownId);
        } else {
            return binDetailsServices.findBinNo(binType, binNo, user.get().getBranch_id(), user.get().getDepartment().getId(), godownId);
        }
    }

    @PostMapping("/filterBinStockByObject")
    public ResponseEntity<?> filterBinStockByObject(@RequestBody CommonFilterDto filterDto,
                                                    Authentication authentication) {
        Long branchId = null;
        Long batchId = null;
        String batchNo = null;
        Long deptId = null;
        Long productId = null;
        Double lot = null;

        if (filterDto.getBatchId() != null) {
            batchId = filterDto.getBatchId();
        }

        if (filterDto.getBatchNo() != null) {
            batchNo = filterDto.getBatchNo();
        }

        if (filterDto.getDeptId() != null) {
            deptId = filterDto.getDeptId();
        }

        if (filterDto.getProductId() != null) {
            productId = filterDto.getProductId();
        }

        if (filterDto.getBranchId() != null) {
            branchId = filterDto.getBranchId();
        }

        if (filterDto.getLot() != null) {
            lot = filterDto.getLot();
        }

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllBinStockSearch(branchId, deptId, productId, batchId, batchNo, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/filterPeekOfStock")
    public ResponseEntity<?> filterPeekOfStock(@RequestParam(value = "branchId", required = false) Long branchId,
                                               @RequestParam(value = "deptId", required = false) Long deptId,
                                               @RequestParam(value = "productId", required = false) Long productId,
                                               @RequestParam(value = "type", required = false) String type, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.filterPeekOfStock(branchId, deptId, productId, type);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/filterOfCurrentStock")
    public ResponseEntity<?> filterOfCurrentStock(@RequestParam(value = "branchId", required = false) Long branchId,
                                               @RequestParam(value = "deptId", required = false) Long deptId,
                                               @RequestParam(value = "productId", required = false) Long productId,
                                               @RequestParam(value = "type", required = false) String type, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.filterOfCurrentStock(branchId, deptId, productId, type);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }



    @GetMapping("/finishedGooseBinsList")
    public ResponseEntity<?> finishedGooseBinsList(@RequestParam(value = "binId", required = false) Long binId,
                                                   @RequestParam(value = "branchId", required = false) Long branchId,
                                                   @RequestParam(value = "productId", required = false) Long productId,
                                                   @RequestParam(value = "type", required = false) String type,
                                                   @RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "asrs", required = false) boolean asrs,
                                                   @RequestParam(value = "trnNo", required = false) String trnNo,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.finishedGooseBinsList(asrs, trnNo , binId, branchId, productId, type, name);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getBinLotWiseStock")
    public ResponseEntity<?> getBinLotWiseStock(@RequestParam(value = "binNo", required = false) Long binNo,
                                                @RequestParam(value = "productId", required = false) Long productId,
                                                @RequestParam(value = "lot", required = false) Double lot, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getBinLotWiseStock(productId, binNo, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getBinWiseLot")
    public ResponseEntity<?> getBinWiseLot(@RequestParam(value = "binNo", required = false) Long binNo,
                                           @RequestParam(value = "productId", required = false) Long productId,
                                           @RequestParam(value = "lot", required = false) Double lot,
                                           @RequestParam(value = "branch", required = false) Long branch,
                                           @RequestParam(value = "dept", required = false) Long dept, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getBinWiseLot(productId, binNo, lot, branch, dept);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @PostMapping("/getBinNoList")
    public ResponseEntity<?> getBinNoList(@RequestBody CommonFilterDto filterDto, Authentication authentication) {

        Long branchId = null;
        Long deptId = null;
        Long productId = null;
        String binNo = null;
        Double lot = null;

        if (filterDto.getDeptId() != null) {
            deptId = filterDto.getDeptId();
        }

        if (filterDto.getProductId() != null) {
            productId = filterDto.getProductId();
        }

        if (filterDto.getBranchId() != null) {
            branchId = filterDto.getBranchId();
        }

        if (filterDto.getLot() != null) {
            lot = filterDto.getLot();
        }

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getBinNoList(branchId, deptId, productId, binNo, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/getBinWiseSummary")
    public ResponseEntity<?> getBinWiseSummary(@RequestParam(value = "branchId", required = false) Long branchId,
                                               @RequestParam(value = "deptId", required = false) Long deptId,
                                               @RequestParam(value = "productId", required = false) Long productId,
                                               @RequestParam(value = "binNo", required = false) String binNo,
                                               @RequestParam(value = "productCategory", required = false) String productCategory,
                                               @RequestParam(value = "productName", required = false) String productName,
                                               @RequestParam(value = "lot", required = false) Double lot, Pageable pageable,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getBinWiseSummary(branchId, deptId, productId, productName, productCategory, binNo, lot, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/stockItemWiseLotList")
    public ResponseEntity<?> stockItemWiseLotList(@RequestParam(value = "productId", required = false) Long productId,
                                                  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.stockItemWiseLotList(productId);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/filterBinList")
    public ResponseEntity<?> filterBinList(@RequestParam(value = "productId", required = true) Long productId,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllBinStockByProductId(productId);
        } else {
            return new ResponseEntity<List<BinStockFilterListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/filterBinData")
    public ResponseEntity<?> filterBinData(@RequestParam(value = "branchId", required = false) Long branchId,
                                           @RequestParam(value = "deptId", required = false) Long deptId,
                                           @RequestParam(value = "batchId", required = false) Long batchId,
                                           @RequestParam(value = "batchNo", required = false) String batchNo,
                                           @RequestParam(value = "productId", required = false) Long productId,
                                           @RequestParam(value = "gid", required = false) Long gid,
                                           @RequestParam(value = "binNo", required = false) Long binNo,
                                           @RequestParam(value = "lot", required = false) Double lot, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.listAllBinStockData(branchId, deptId, productId, batchId, batchNo, lot, gid, binNo);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }


    @PostMapping("/filterBinStockOnlyBill")
    public ResponseEntity<?> filterBinStockOnlyBill(@RequestBody CommonFilterDto filterDto, Authentication authentication) {

        Long branchId = null;
        Long batchId = null;
        String batchNo = null;
        Long deptId = null;
        Long productId = null;
        Double lot = null;

        if (filterDto.getBatchId() != null) {
            batchId = filterDto.getBatchId();
        }

        if (filterDto.getBatchNo() != null) {
            batchNo = filterDto.getBatchNo();
        }

        if (filterDto.getDeptId() != null) {
            deptId = filterDto.getDeptId();
        }

        if (filterDto.getProductId() != null) {
            productId = filterDto.getProductId();
        }

        if (filterDto.getBranchId() != null) {
            branchId = filterDto.getBranchId();
        }

        if (filterDto.getLot() != null) {
            lot = filterDto.getLot();
        }

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.filterBinStockOnlyBill(branchId, deptId, productId, batchId, batchNo, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/filterBinStockOnlyDelite")
    public ResponseEntity<?> filterBinStockOnlyDelite(@RequestParam(value = "binStockIds", required = false) String binStockIds,
                                            @RequestParam(value = "branchId", required = false) Long branchId,
                                            @RequestParam(value = "deptId", required = false) Long deptId,
                                            @RequestParam(value = "batchId", required = false) Long batchId,
                                            @RequestParam(value = "batchNo", required = false) String batchNo,
                                            @RequestParam(value = "productId", required = false) Long productId,
                                            @RequestParam(value = "lot", required = false) Double lot, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.filterBinStockOnlyDelite(binStockIds,  branchId, deptId, productId, batchId, batchNo, lot);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/binLockByBinDetail")
    public ResponseEntity<?> binLockByBinDetail(@RequestParam(value = "binName", required = true) Long binName,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.filterFindBy(binName);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }

    @GetMapping("/findBinNoWithLockType")
    public ResponseEntity<?> findBinNoWithLockType(@RequestParam(value = "binType", required = false) String binType,
                                       @RequestParam(value = "binNo", required = false) Long binNo,
                                       @RequestParam(value = "branch", required = false) Long branch,
                                       @RequestParam(value = "dept", required = false) Long dept, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        if (dept != null || branch != null) {
            return binDetailsServices.findBinNoWithLockType(binType, binNo, branch, dept);
        } else {
            return binDetailsServices.findBinNoWithLockType(binType, binNo, user.get().getBranch_id(), user.get().getDepartment().getId());
        }
    }



    @GetMapping("/getBinStockWiseSummary")
    public ResponseEntity<?> getBinStockWiseSummary(@RequestParam(value = "branchId", required = true) Long branchId,
                                               @RequestParam(value = "deptId", required = true) Long deptId,
                                               @RequestParam(value = "productId", required = false) Long productId,
                                               @RequestParam(value = "binNo", required = true) Long binNo,
                                               @RequestParam(value = "productCategory", required = true) String productCategory,
                                               @RequestParam(value = "productName", required = false) String productName,
                                               @RequestParam(value = "lot", required = false) Double lot, Pageable pageable,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getBinStockWiseSummary(branchId, deptId, productId, productName, productCategory, binNo, lot, pageable);
        } else {
            throw new RuntimeException("Error: 403 Forbidden");
        }
    }
    @GetMapping("/getAsrsPalletType")
    public ResponseEntity<?> getAsrsPalletType(@RequestParam("palletNumber") String palletNumber, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.getAsrsPalletType(palletNumber, user.get());
        } else {
            return new ResponseEntity<List<ProductionEntryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
    @PostMapping("/palletNoValidation")
    public ResponseEntity<?> palletNoValidation(@RequestBody List<PalletDto> palletDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.palletNoValidation(palletDto, user.get());
        } else {
            return new ResponseEntity<List<PalletDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
    @PutMapping("/palletNoUpdate")
    public ResponseEntity<?> palletNoUpdate(@RequestBody List<PalletDto> palletDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return binDetailsServices.palletNoUpdate(palletDto, user.get());
        } else {
            return new ResponseEntity<List<PalletDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
    
    
    @GetMapping("/findPalletDetails")
    public ResponseEntity<?> findPalletDetails(
                                       @RequestParam(value = "binNo", required = false) Long binNo, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        
            return binDetailsServices.findPalletDetails( binNo, user.get());
       
           
        }
    
    
}

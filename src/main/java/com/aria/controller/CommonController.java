package com.aria.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aria.service.CommonService;

@RestController
@RequestMapping("/api")
public class CommonController {

    @Autowired
    CommonService commonService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    LotRepository lotRepository;

    @Autowired
    RMProLotsRepository rmProLotsRepository;

    @Autowired
    LotValuesDao lotValuesRepository;

    @PostMapping("/saveBranch")
    public ResponseEntity<?> saveBranch(@RequestBody BranchReqDto branchDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveBranch(branchDto, user.get());
    }

    @PutMapping("/updateBranch")
    public ResponseEntity<?> updateBranch(@RequestBody BranchReqDto branchDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateBranch(branchDto, user.get());
    }


    @GetMapping("/branch/{id}")
    public ResponseEntity<?> getBranch(@PathVariable(value = "id") Long id,
                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.getBranch(id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }



    @DeleteMapping("/deleteBranch/{id}")
    public ResponseEntity<?> deleteBranch(@PathVariable(value = "id") Long id,
                                       Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.deleteBranch(id);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/branchDetailList")
    public ResponseEntity<?> branchDetailList( @RequestParam (value = "orgId", required = false) Long orgId,
                                               @RequestParam (value = "branchName", required = false) String branchName,
                                               @RequestParam (value = "orgName", required = false) String orgName ,
                                               Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.branchDetailList(orgId, branchName, orgName, pageable);
        } else {
            return new ResponseEntity<List<BranchDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    @GetMapping("/branchList")
    public ResponseEntity<?> branchList( @RequestParam (value = "orgId", required = false) Long orgId,  Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listAll(orgId, pageable);
        } else {
            return new ResponseEntity<List<BranchDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/branchById")
    public ResponseEntity<?> getBranchById(@RequestParam(value = "branchId", required = true) Long branchId,
                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.getBranchById(branchId);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    // Organization

    @PostMapping("/saveOrganization")
    public ResponseEntity<?> saveOrganization(@RequestBody OrganizationDto organizationDto,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveOrganization(organizationDto, user.get());
    }



    @PutMapping("/updateOrganization")
    public ResponseEntity<?> updateOrganization(@RequestBody OrganizationDto organizationDto,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateOrganization(organizationDto, user.get());
    }

    @GetMapping("/OrganizationList")
    public ResponseEntity<?> OrganizationList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.organizationListAll(pageable);
        } else {
            return new ResponseEntity<List<OrganizationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/deleteOrganizationList")
    public ResponseEntity<?> deleteOrganizationList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.deletedOrganizationListAll();
        } else {
            return new ResponseEntity<List<OrganizationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/organizationById")
    public ResponseEntity<?> getOrganizationById(@RequestParam(value = "orgId", required = true) Long orgId,
                                                 Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return new ResponseEntity<Optional<Organisation>>(organisationRepository.findById(orgId), HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }

    }

    // Department details

    @PostMapping("/saveDepartment")
    public ResponseEntity<?> saveDepartment(@RequestBody DepartmentDto departmentDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveDepartment(departmentDto, user.get());
    }

    @PutMapping("/updateDepartment")
    public ResponseEntity<?> updateDepartment(@RequestBody DepartmentDto departmentDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateDepartment(departmentDto, user.get());
    }

    @GetMapping("/departmentList")
    public ResponseEntity<?> departmentList(@RequestParam(value = "orgId", required = false) Long orgId,
                                            Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.departmentListAll(orgId);
        } else {
            return new ResponseEntity<List<DepartmentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // ShiftCreation

    @PostMapping("/shitCreate")
    public ResponseEntity<?> shitCreate(@RequestBody ShiftDto shiftDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveShift(shiftDto, user.get());
    }

    @GetMapping("/listconstants")
    public ResponseEntity<?> getCategoryConstants(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.categoryConstants(user.get());
    }

    @PutMapping("/shitUpdate")
    public ResponseEntity<?> shitUpdate(@RequestBody ShiftDto shiftDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateShift(shiftDto, user.get());
    }

    @GetMapping("/shiftList")
    public ResponseEntity<?> shiftList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.departmentListAll(pageable);
        } else {
            return new ResponseEntity<List<ShiftDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // vendor details

    @PostMapping("/saveVendor")
    public ResponseEntity<?> saveVendor(@RequestBody VendorDto vendorDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveVendor(vendorDto, user.get());
    }

    @PutMapping("/updateVendor")
    public ResponseEntity<?> updateVendor(@RequestBody VendorDto vendorDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateVendor(vendorDto, user.get());
    }

    @GetMapping("/vendorList")
    public ResponseEntity<?> vendorList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.vendorListAll(pageable);
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

//Day

    @PostMapping("/saveDay")
    public ResponseEntity<?> saveDay(@RequestBody DayDto dayDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveDay(dayDto, user.get());
    }

    @PutMapping("/updateDay")
    public ResponseEntity<?> updateDay(@RequestBody DayDto dayDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateDay(dayDto, user.get());
    }

    @GetMapping("/dayList")
    public ResponseEntity<?> dayList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.dayListAll();
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // BillNumber

    @PostMapping("/saveBillNumber")
    public ResponseEntity<?> saveBillNumber(@RequestBody BillNumberDto billNumberDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveBillNumber(billNumberDto, user.get());
    }

    @PutMapping("/updateBillNumber")
    public ResponseEntity<?> updateBillNumber(@RequestBody BillNumberDto billNumberDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateBillNumber(billNumberDto, user.get());
    }

    @GetMapping("/billNumberList")
    public ResponseEntity<?> billNumberList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.billNumberListAll();
        } else {
            return new ResponseEntity<List<BillNumberDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/bloogroupList")
    public ResponseEntity<?> bloogroupList(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.bloodGroupListAll(pageable);
        } else {
            return new ResponseEntity<List<BillNumberDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

//	// Purchase Order
//
//	@PostMapping("/savePurchaseOrder")
//	public ResponseEntity<?> savePurchaseOrder(@RequestBody PurchaseOrderDto purchaseOrderDto,
//			Authentication authentication) {
//
//		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
//
//		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
//
//		return commonService.savePurchaseOrder(purchaseOrderDto, user.get());
//	}
//
//	@PutMapping("/updatePurchaseOrder")
//	public ResponseEntity<?> updatePurchaseOrder(@RequestBody PurchaseOrderDto purchaseOrderDto,
//			Authentication authentication) {
//
//		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
//
//		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
//
//		return commonService.updatePurchaseOrder(purchaseOrderDto, user.get());
//	}
//
//	@GetMapping("/purchaseOrderListAll")
//	public ResponseEntity<?> purchaseOrderListAll(Authentication authentication) {
//
//		UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
//		Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
//		if (user.get() != null) {
//			return commonService.purchaseOrderListAll();
//		} else {
//			return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
//		}
//	}

    @PostMapping("/saveUnit")
    public ResponseEntity<?> saveUnit(@RequestBody UnitsDto unitsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveUnits(unitsDto, user.get());
    }

    @PutMapping("/updateUnit")
    public ResponseEntity<?> updateUnit(@RequestBody UnitsDto unitsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateUnits(unitsDto, user.get());
    }

    @GetMapping("/unitList")
    public ResponseEntity<?> unitList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listUnitsAll();
        } else {
            return new ResponseEntity<List<BranchDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/rawMaterial")
    public ResponseEntity<?> rawMaterialList(@RequestParam(value = "rawMaterial", required = false) String rawMaterial,
                                             Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listAllRawMaterial(rawMaterial, pageable);
        } else {
            return new ResponseEntity<List<BranchDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/saveCluster")
    public ResponseEntity<?> saveCluster(@RequestBody ClusterDto clusterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveCluster(clusterDto, user.get());
    }

    @PutMapping("/updateCluster")
    public ResponseEntity<?> updateCluster(@RequestBody ClusterDto clusterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateCluster(clusterDto, user.get());
    }

    @GetMapping("/clusterList")
    public ResponseEntity<?> listClusterAll(Pageable pageable, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listClusterAll(pageable);
        } else {
            return new ResponseEntity<List<ClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/searchClusterList")
    public ResponseEntity<?> searchClusterList(Authentication authentication,
                                               @RequestParam(value = "name", required = false) String name) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.searchClusterList(name);
        } else {
            return new ResponseEntity<List<DropDownListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/saveProductCategory")
    public ResponseEntity<?> saveProductCategory(@RequestBody ProductCategoryDto productCategory,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveProductCategory(productCategory, user.get());
    }

    @PutMapping("/updateProductCategory")
    public ResponseEntity<?> updateProductCategory(@RequestBody ProductCategoryDto productCategoryDto,
                                                   Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateProductCategory(productCategoryDto, user.get());
    }

    @GetMapping("/productCategoryList")
    public ResponseEntity<?> productCategoryList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listProductCategoryAll();
        } else {
            return new ResponseEntity<List<ProductCategoryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Country

    @PostMapping("/saveCountry")
    public ResponseEntity<?> saveCountry(@RequestBody CountryDto countryDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveCountry(countryDto, user.get());
    }

    @PutMapping("/updateCountry")
    public ResponseEntity<?> updateCountry(@RequestBody CountryDto countryDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateCountry(countryDto, user.get());
    }

    @GetMapping("/countryList")
    public ResponseEntity<?> countryList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listAllCountry();
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // State

    @PostMapping("/saveState")
    public ResponseEntity<?> saveState(@RequestBody StateDto stateDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveState(stateDto, user.get());
    }

    @PutMapping("/updateState")
    public ResponseEntity<?> updateState(@RequestBody StateDto stateDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateState(stateDto, user.get());
    }

    @GetMapping("/stateList")
    public ResponseEntity<?> stateList(@RequestParam(value = "countryId", required = false) Long countryId,
                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (countryId == null)
                return commonService.listAllState();
            else
                return commonService.listAllState(countryId);
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Districts

    @PostMapping("/saveDistricts")
    public ResponseEntity<?> saveDistricts(@RequestBody DistrictsDto districtsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveDistrict(districtsDto, user.get());
    }

    @PutMapping("/updateDistricts")
    public ResponseEntity<?> updateDistricts(@RequestBody DistrictsDto districtsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateDistrict(districtsDto, user.get());
    }

    @GetMapping("/districtsList")
    public ResponseEntity<?> districtsList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listAllDistrict();
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/placeList")
    public ResponseEntity<?> placeList(@RequestParam(value = "placeName", required = false) String placeName,
                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.getAllDistrict(placeName);
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Cities

    @PostMapping("/saveCities")
    public ResponseEntity<?> saveCities(@RequestBody CityDto cityDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveCity(cityDto, user.get());
    }

    @PutMapping("/updateCities")
    public ResponseEntity<?> updateCities(@RequestBody CityDto cityDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateCity(cityDto, user.get());
    }

    @GetMapping("/citiesList")
    public ResponseEntity<?> citiesList(@RequestParam(value = "stateId", required = false) Long stateId,
                                        Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (stateId == null)
                return commonService.listAllCity();
            else
                return commonService.listAllCity(stateId);
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/cityList")
    public ResponseEntity<?> cityList(@RequestParam(value = "placeName", required = false) String placeName,
                                      Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.cityList(placeName);
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Lots

    @PostMapping("/saveLot")
    public ResponseEntity<?> saveLot(@RequestParam(value = "lot") String lotValue,
                                     @RequestParam(value = "refid") Long prodId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        Lot lot = new Lot();
        lot.setProductId(prodId);
        lot.setLot(Double.parseDouble(lotValue));
        lot.setLotString(lotValue);
        lot.setDispname(lotValue);
        return new ResponseEntity<Lot>(lotRepository.save(lot), HttpStatus.OK);
    }

    @PutMapping("/updateLot")
    public ResponseEntity<?> updateLot(@RequestParam(value = "lot") String lotValue,
                                       @RequestParam(value = "id") Long id, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        Lot lot = new Lot();
        lot.setId(id);
        lot.setLot(Double.parseDouble(lotValue));
        lot.setLotString(lotValue);
        lot.setDispname(lotValue);
        return new ResponseEntity<Lot>(lotRepository.save(lot), HttpStatus.OK);
    }

    @GetMapping("/lotList")
    public ResponseEntity<?> lotList(@RequestParam(value = "productId") Long productId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return new ResponseEntity<List<Lot>>(lotRepository.findByLot(productId), HttpStatus.OK);

    }

    @GetMapping("/lotAllList")
    public ResponseEntity<?> lotList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return new ResponseEntity<List<String>>(lotRepository.findByLotlist(), HttpStatus.OK);

    }

    @GetMapping("/rmProLotList")
    public ResponseEntity<?> rmProLotList(@RequestParam(value = "productId") Long productId,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return new ResponseEntity<List<RMProLots>>(rmProLotsRepository.findByRmProLot(productId), HttpStatus.OK);

    }

    // TrainingType

    @PostMapping("/saveTrainingType")
    public ResponseEntity<?> saveTrainingType(@RequestBody List<TrainingTypeDto> trainingTypeDtos,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTrainingType(trainingTypeDtos, user.get());
    }

    @PutMapping("/updateTrainingType")
    public ResponseEntity<?> updateTrainingType(@RequestBody List<TrainingTypeDto> trainingTypeDtos,
                                                Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateTrainingType(trainingTypeDtos, user.get());
    }

    @GetMapping("/trainingTypeList")
    public ResponseEntity<?> trainingTypeList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.trainingTypeList();
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // task Add

    @PostMapping("/task")
    public ResponseEntity<?> saveTask(@RequestBody TaskDto reqDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTask(reqDto, user.get());
    }

    @GetMapping("/allTask")
    public ResponseEntity<?> allTask(@RequestParam(value = "task", required = false) String task,
                                     Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        return commonService.allTask(task, user.get());
    }

    // task and taskcategory mapping as task_name table

    @PostMapping("/taskMap")
    public ResponseEntity<?> saveTaskMap(@RequestBody TaskNameDto reqDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTaskMap(reqDto, user.get());
    }

    @GetMapping("/searchTaskMap")
    public ResponseEntity<?> searchNames(Authentication authentication,
                                         @RequestParam(value = "taskCategoryName", required = false) String taskCategoryName,
                                         @RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                         @RequestParam(value = "search", required = false) String search, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.searchTaskMap(taskCategoryName, taskCategoryId, search, pageable);
        } else {
            return new ResponseEntity<List<TaskNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @PostMapping("/taskNames")
    public ResponseEntity<?> saveTaskNames(@RequestBody ArrayList<TaskNameDto> reqDto, Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTaskNames(reqDto, user.get());
    }

    @GetMapping("/taskNames")
    public ResponseEntity<?> taskNames(Authentication authentication,
                                       @RequestParam(value = "taskName", required = false) String taskName,
                                       @RequestParam(value = "taskCategoryName", required = false) String taskCategoryName,
                                       @RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                       @RequestParam(value = "search", required = false) String search, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskNames(taskName, taskCategoryName, taskCategoryId, search, pageable);
        } else {
            return new ResponseEntity<List<TaskNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/taskNamesDropDownList")
    public ResponseEntity<?> taskNames(Authentication authentication,
                                       @RequestParam(value = "taskName", required = false) String taskName,
                                       @RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskNames(taskName, taskCategoryId, pageable);
        } else {
            return new ResponseEntity<List<TaskNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/taskList")
    public ResponseEntity<?> taskList(Authentication authentication,
                                      @RequestParam(value = "taskName", required = false) String taskName,
                                      @RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                      @RequestParam(value = "taskGroupId", required = false) Long taskGroupId) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskList(taskName, taskCategoryId, taskGroupId);
        } else {
            return new ResponseEntity<List<TaskNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // task category

    @PostMapping("/taskCategories")
    public ResponseEntity<?> saveTaskCategories(@RequestBody ArrayList<TaskCategoryDto> reqDto,
                                                Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTaskCategories(reqDto, user.get());
    }

    @GetMapping("/taskCategories")
    public ResponseEntity<?> taskCategories(Authentication authentication,
                                            @RequestParam(value = "taskCategoryName", required = false) String taskCategoryName,
                                            @RequestParam(value = "modeOfEntry", required = false) String modeOfEntry) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskCategories(taskCategoryName, modeOfEntry);
        } else {
            return new ResponseEntity<List<TaskCategoryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/searchCategories")
    public ResponseEntity<?> searchCategories(Authentication authentication,
                                              @RequestParam(value = "taskCategoryName", required = false) String taskCategoryName) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.searchCategories(taskCategoryName);
        } else {
            return new ResponseEntity<List<DropDownListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // task group master

    @PostMapping("/taskGroupMaster")
    public ResponseEntity<?> saveTaskGroupsMaster(@RequestBody ArrayList<TaskGroupMasterDto> reqDto,
                                                  Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTaskGroupsMaster(reqDto, user.get());
    }

    @GetMapping("/taskGroupMaster")
    public ResponseEntity<?> taskGroupsMaster(Authentication authentication,
                                              @RequestParam(value = "taskGroupName", required = false) String taskGroupName, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskGroupsMaster(taskGroupName, pageable);
        } else {
            return new ResponseEntity<List<TaskGroupMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // task groups

    @PostMapping("/taskGroups")
    public ResponseEntity<?> saveTaskGroups(@RequestBody ArrayList<TaskGroupDto> reqDto,
                                            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTaskGroups(reqDto, user.get());
    }

    @PostMapping("/taskGroupsWithTaskName")
    public ResponseEntity<?> saveTaskGroupsWithTaskName(@RequestBody TaskGroupWithTaskNameDto reqDto,
                                                        Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveTaskGroupWithTaskName(reqDto);
    }

    @GetMapping("/taskGroups")
    public ResponseEntity<?> taskGroups(Authentication authentication,
                                        @RequestParam(value = "taskGroup", required = false) String taskGroup,
                                        @RequestParam(value = "search", required = false) String search,
                                        @RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId,
                                        @RequestParam(value = "taskNameId", required = false) Long taskNameId,
                                        @RequestParam(value = "taskCategoryName", required = false) String taskCategoryName, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskGroups(taskGroup, taskCategoryId, taskCategoryName, taskNameId, pageable, search);
        } else {
            return new ResponseEntity<List<TaskGroupDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/taskGroupsByCategory")
    public ResponseEntity<?> taskGroups(Authentication authentication,
                                        @RequestParam(value = "taskCategoryId", required = false) Long taskCategoryId) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return new ResponseEntity<List<TaskGroupDropDownDto>>(commonService.getTaskGroupForCategory(taskCategoryId),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TaskGroupDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Biomast

    @PostMapping("/saveBiomast")
    public ResponseEntity<?> saveBiomast(@RequestBody List<BiomastDto> biomastDtos, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveBiomast(biomastDtos, user.get());
    }

    @PutMapping("/updateBiomast")
    public ResponseEntity<?> updateBiomast(@RequestBody List<BiomastDto> biomastDtos, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateBiomast(biomastDtos, user.get());
    }

    @GetMapping("/biomastList")
    public ResponseEntity<?> biomastList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.biomastList();
        } else {
            return new ResponseEntity<List<BiomastDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Pshifts

    @PostMapping("/savePShifts")
    public ResponseEntity<?> savePShifts(@RequestBody PShiftsDto pShiftsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.savePShifts(pShiftsDto, user.get());
    }

    @PutMapping("/updatePShifts")
    public ResponseEntity<?> updatePShifts(@RequestBody PShiftsDto pShiftsDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updatePShifts(pShiftsDto, user.get());
    }

    @GetMapping("/pshiftsList")
    public ResponseEntity<?> pshiftsList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.pshiftsList();
        } else {
            return new ResponseEntity<List<BiomastDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/pshiftsDropDownList")
    public ResponseEntity<?> pshiftsDropDownList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.pshiftsListDropDown();
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Kpiname

    @PostMapping("/saveKpiname")
    public ResponseEntity<?> saveKpiname(@RequestBody KpinameDto kpinameDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveKpiname(kpinameDto, user.get());
    }

    @PutMapping("/updateKpiname")
    public ResponseEntity<?> updateKpiname(@RequestBody KpinameDto kpinameDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateKpiname(kpinameDto, user.get());
    }

    @GetMapping("/kpinameList")
    public ResponseEntity<?> kpinameList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.pshiftsList();
        } else {
            return new ResponseEntity<List<BiomastDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/kpinameDropDownList")
    public ResponseEntity<?> kpinameDropDownList(@RequestParam(value = "type", required = false) String type,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.kpiNameListDropDown(type);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/wcshiftList")
    public ResponseEntity<?> wcshiftList(@RequestParam(value = "proId", required = false) Long proId,
                                         @RequestParam(value = "dept", required = false) Long dept,
                                         @RequestParam(value = "branch", required = false) Long branch,
                                         @RequestParam(value = "pshift", required = false) String pshift,
                                         @RequestParam(value = "shiftDate", required = false) String shiftDate,
                                         @RequestParam(value = "type", required = false) String type,

                                         Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.wcshiftList(proId, dept, branch, pshift, shiftDate);
        } else {
            return new ResponseEntity<List<WcShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/taskGroupNameDropDownList")
    public ResponseEntity<?> taskGroupNameDropDownList(@RequestParam(value = "kpiId", required = false) Long kpiId,
                                                       Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskGroupList(kpiId);
        } else {
            return new ResponseEntity<List<WcShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Panel Name

    @PostMapping("/panelName")
    public ResponseEntity<?> savePanelNames(@RequestBody ArrayList<PanelNameDto> reqDto,
                                            Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.savePanelNames(reqDto, user.get());
    }

    @GetMapping("/panelName")
    public ResponseEntity<?> panelNames(Authentication authentication,
                                        @RequestParam(value = "panelName", required = false) String taskName, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.panelNames(taskName, pageable);
        } else {
            return new ResponseEntity<List<PanelNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/allPanelName")
    public ResponseEntity<?> getAllPanelNames(Authentication authentication,
                                              @RequestParam(value = "panelName", required = false) String taskName) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.getAllPanelNames(taskName);
        } else {
            return new ResponseEntity<List<PanelNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Schedule Name

    @PostMapping("/scheduleName")
    public ResponseEntity<?> saveScheduleNames(@RequestBody ArrayList<ScheduleNameDto> reqDto,
                                               Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveScheduleNames(reqDto, user.get());
    }

    @GetMapping("/scheduleName")
    public ResponseEntity<?> scheduleNames(Authentication authentication,
                                           @RequestParam(value = "scheduleName", required = false) String scheduleName, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.scheduleNames(scheduleName, pageable);
        } else {
            return new ResponseEntity<List<ScheduleNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/allScheduleName")
    public ResponseEntity<?> allScheduleName(Authentication authentication,
                                             @RequestParam(value = "scheduleName", required = false) String scheduleName) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.allscheduleName(scheduleName);
        } else {
            return new ResponseEntity<List<ScheduleNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // DepreciationCategory Name

    @PostMapping("/depreciationCategory")
    public ResponseEntity<?> saveDepreciationCategoryNames(@RequestBody ArrayList<DepreciationCategoryNameDto> reqDto,
                                                           Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveDepreciationCategoryNames(reqDto, user.get());
    }

    @GetMapping("/depreciationCategory")
    public ResponseEntity<?> depreciationCategoryNames(Authentication authentication,
                                                       @RequestParam(value = "depreciationCategoryName", required = false) String depreciationCategoryName,
                                                       Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.depreciationCategoryNames(depreciationCategoryName, pageable);
        } else {
            return new ResponseEntity<List<DepreciationCategoryNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Camp

    @PostMapping("/saveCamp")
    public ResponseEntity<?> saveCamp(@RequestBody List<CampDto> campDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveCamp(campDto, user.get());
    }

    @PutMapping("/updateCamp")
    public ResponseEntity<?> updateCamp(@RequestBody CampDto campDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateCamp(campDto, user.get());
    }

    @GetMapping("/campList")
    public ResponseEntity<?> campList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.campList();
        } else {
            return new ResponseEntity<List<BiomastDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/campDropDownList")
    public ResponseEntity<?> campDropDownList(@RequestParam(value = "state", required = false) String state,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.campListDropDown(state);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/campNameList")
    public ResponseEntity<?> campNameList(@RequestParam(value = "placeName", required = false) String placeName,
                                          Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.campNameList(placeName);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // ColorbslapDownList

    @PostMapping("/saveColor")
    public ResponseEntity<?> savePShifts(@RequestBody ColorDto colorDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveColor(colorDto, user.get());
    }

    @PutMapping("/updateColor")
    public ResponseEntity<?> updateColor(@RequestBody ColorDto colorDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateColor(colorDto, user.get());
    }

    @GetMapping("/colorList")
    public ResponseEntity<?> colorList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.colorList();
        } else {
            return new ResponseEntity<List<BiomastDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/colorDropDownList")
    public ResponseEntity<?> colorDropDownList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.colorListDropDown();
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Day

    @PostMapping("/saveLuZone")
    public ResponseEntity<?> saveLuZone(@RequestBody LuZoneDto luZoneDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveLuZone(luZoneDto, user.get());
    }

    @PutMapping("/updateLuZone")
    public ResponseEntity<?> updateLuZone(@RequestBody LuZoneDto luZoneDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateLuZone(luZoneDto, user.get());
    }

    @GetMapping("/luZoneList")
    public ResponseEntity<?> luZoneList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.luzoneListAll();
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/luZoneDropDownListList")
    public ResponseEntity<?> luZoneDropDownListList(@RequestParam(value = "staffType") String staffType,
                                                    @RequestParam(value = "actType") Long actType,
                                                    @RequestParam(value = "gender") String gender, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            System.out.println(user.get().getBranch_id() + "  +++  " + user.get().getDepartment().getId());
            return commonService.luzoneListAll(user.get().getBranch_id(), user.get().getDepartment().getId(), gender, staffType, actType);
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/luZoneStaffList")
    public ResponseEntity<?> luZoneStaffList(@RequestParam(value = "staffType") String staffType,
                                                    @RequestParam(value = "actType") Long actType,
                                                     Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            System.out.println(user.get().getBranch_id() + "  +++  " + user.get().getDepartment().getId());
            return commonService.luZoneStaffList(user.get().getBranch_id(), user.get().getDepartment().getId(), staffType, actType);
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/actCategoryDropDownList")
    public ResponseEntity<?> luZoneDropDownListList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.daCategoryAll();
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/bslapDownList")
    public ResponseEntity<?> bslapDownList(@RequestParam(value = "tsId", required = false) Long tsId,
                                           Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.bslapListDropDown(tsId);
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Supplier Group

    @PostMapping("/saveSupplierGroup")
    public ResponseEntity<?> saveSupplierGroup(@RequestBody SupplierGroupDto supplierGroupDto,
                                               Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveSupplierGroup(supplierGroupDto, user.get());
    }

    @PutMapping("/updateSupplierGroup")
    public ResponseEntity<?> updateSupplierGroup(@RequestBody SupplierGroupDto supplierGroupDto,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateSupplierGroup(supplierGroupDto, user.get());
    }

    @GetMapping("/supplierGroupAll")
    public ResponseEntity<?> supplierGroupAll(@RequestParam(value = "name", required = false) String name,
                                              Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.supplierGroupAll(name);
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/deleteSupplierGroup")
    public ResponseEntity<?> deleteSupplierGroup(@RequestParam ("id") Long id,
                                                 Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.deleteSupplierGroup(id);
    }


    @GetMapping("/taskGroupSearch")
    public ResponseEntity<?> taskGroupAll(Authentication authentication,
                                          @RequestParam(value = "taskCategoryName", required = false) String taskCategoryName,
                                          @RequestParam(value = "taskGroupName", required = false) String taskGroup,
                                          @RequestParam(value = "taskName", required = false) String taskName,
                                          @RequestParam(value = "modeOfEntry", required = false) String modeOfEntry, Pageable pageable) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.taskGroupsSearch(modeOfEntry, taskGroup, taskCategoryName, taskName, pageable);
        } else {
            return new ResponseEntity<List<TaskGroupDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @DeleteMapping("/taskGroupCategoryMapping/{id}")
    public ResponseEntity<?> deleteMachineClusterDetails(@PathVariable Long id, Authentication authentication) {
        commonService.deleteTaskGroupCategoryMapping(id);
        return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
    }

    @GetMapping("/mails")
    public ResponseEntity<?> getMailSender(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return new ResponseEntity<List<MailDropDownDto>>(commonService.getMailSender(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<MailDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/offReason")
    public ResponseEntity<?> offReason(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return new ResponseEntity<List<OffReasonDto>>(commonService.getOffReasonDropDown(), HttpStatus.OK);
        } else {
            return new ResponseEntity<List<OffReasonDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/detailedReason")
    public ResponseEntity<?> detailedReason(@RequestParam(value = "offReasonId", required = false) Long offReasonId,
                                            @RequestParam(value = "offReasonName", required = false) String offReasonName,
                                            @RequestParam(value = "machineId", required = false) Long machineId, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            if (offReasonId != null)
                return new ResponseEntity<List<OffReasonDto>>(commonService.getDetailedReasonDropDown(offReasonId), HttpStatus.OK);
            else if (offReasonName != null && offReasonName.equalsIgnoreCase("Preventive Maintenance") && machineId != null) {
                return new ResponseEntity<List<OffReasonDto>>(commonService.getScheduleName(machineId), HttpStatus.OK);
            } else if (machineId != null) {
                return new ResponseEntity<List<MachineDropDownDto>>(commonService.getCLusterAndItemMachineName(machineId), HttpStatus.OK);
            }
        }
        return new ResponseEntity<List<OffReasonDto>>(new ArrayList<>(), HttpStatus.OK);
    }

    // dwcluster

    @PostMapping("/saveDwCluster")
    public ResponseEntity<?> saveDwCluster(@RequestBody DwClusterDto dwClusterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.saveDwCluster(dwClusterDto, user.get());
    }

    @PutMapping("/updateDwCluster")
    public ResponseEntity<?> updateDwCluster(@RequestBody DwClusterDto dwClusterDto, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());

        return commonService.updateDwCluster(dwClusterDto, user.get());
    }

    @GetMapping("/dwclusterAll")
    public ResponseEntity<?> dwclusterAll(@RequestParam(value = "name", required = false) String name,
                                          @RequestParam(value = "name", required = false) Long branch,
                                          @RequestParam(value = "name", required = false) Long dept, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.dwclusterAll(name, branch, dept);
        } else {
            return new ResponseEntity<List<DwClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    @GetMapping("/unitType")
    public ResponseEntity<?> unitType(@RequestParam(value = "id", required = false) Long id,
                                      @RequestParam(value = "specType", required = false) Long specType, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        if (user.get() != null) {
            return commonService.listAllUnitType(specType, id);
        } else {
            return new ResponseEntity<List<DwClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    // Lots

    @PostMapping("/saveLotValue")
    public ResponseEntity<?> saveLotValue(@RequestBody LotValuesDto lotValue, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        LotValues lot = new LotValues();
        lot.setLot(lotValue.getLot());
        lot.setDispname(lotValue.getDispname());
        return new ResponseEntity<LotValues>(lotValuesRepository.save(lot), HttpStatus.OK);
    }

    @PutMapping("/updateLotValue")
    public ResponseEntity<?> updateLotValue(@RequestBody LotValuesDto lotValue, Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(userPrincipal.getUsername());
        LotValues lot = new LotValues();
        lot.setId(lotValue.getId());
        lot.setLot(lotValue.getLot());
        lot.setDispname(lotValue.getDispname());
        return new ResponseEntity<LotValues>(lotValuesRepository.save(lot), HttpStatus.OK);
    }


    @GetMapping("/lotValueAllList")
    public ResponseEntity<?> lotValueAllList(Authentication authentication) {

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return new ResponseEntity<List<String>>(lotValuesRepository.findByLotlist(), HttpStatus.OK);

    }
}
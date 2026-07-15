package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.utils.DateUtil;

@Service
public class CommonService {

    @Autowired
    PMScheduleRepository pmScheduleRepo;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    DepartmentDetailsRepository departmentRepository;

    @Autowired
    ShiftDetailsRepository shiftDetailsRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    DayRepository dayRepository;

    @Autowired
    BillNumberRepository billNumberRepository;

    @Autowired
    BloodGroupRepository bloodGroupRepository;

    @Autowired
    ClusterRepository clusterRepository;

    @Autowired
    ProductCategoryRepository productCategoryRepository;

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    UnitsRepository unitsRepository;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    DistrictsRepository districtsRepository;

    @Autowired
    CityRepository cityRepository;

    @Autowired
    TrainingTypeRepository trainingTypeRepository;

    @Autowired
    TaskNameRepository taskNameRepository;

    @Autowired
    TaskCategoryRepository taskCategoryRepository;

    @Autowired
    TaskGroupRepository taskGroupRepository;

    @Autowired
    TaskGroupMasterRepository taskGroupMasterRepository;

    @Autowired
    BiomastRepository biomastRepository;

    @Autowired
    PShiftsRepository pShiftsRepository;

    @Autowired
    KpiNameRepository kpiNameRepository;

    @Autowired
    WcShiftsRepository wcShiftsRepository;

    @Autowired
    KpiRepository kpiRepository;

    @Autowired
    PanelNameRepository panelNameRepository;

    @Autowired
    ScheduleNameRepository scheduleNameRepository;

    @Autowired
    DepreciationCategoryNameRepository depreciationCategoryNameRepository;

    @Autowired
    ColorRepository colorRepository;

    @Autowired
    CampRepository campRepository;

    @Autowired
    LuZoneRepository luZoneRepository;

    @Autowired
    DaCategoryRepository daCategoryRepository;

    @Autowired
    BsLapRepository bsLapRepository;

    @Autowired
    CategoryConstantsRepository constantsRepository;

    @Autowired
    SupplierGroupRepo supplierGroupRepo;

    @Autowired
    MailSendRepository mailSendRepository;

    @Autowired
    OffReasonRepository offReasonRepository;

    @Autowired
    DetailedReasonRepository detailedReasonRepository;

    @Autowired
    MachineClusterRepository machineClusterRepository;

    @Autowired
    TaskRepo taskRepo;

    @Autowired
    DwClusterRepository dwClusterRepository;

    @Autowired
    PurchaseOrderItemsRepository purchaseOrderItemsRepository;

    @Autowired
    PurchaseExpenseRepository purchaseExpenseRepository;

    @Autowired
    MachineRepository machineRepository;

    @Autowired
    BranchMappingRepository branchMappingRepository;


    public void saveBranch(BranchDto branchDto) {
        Branch branch = new Branch(branchDto);
        branchRepository.save(branch);
    }

    // branch Details add
    public ResponseEntity<?> saveBranch(BranchReqDto branchDto, User loggedUser) {
        Branch branch = new Branch(branchDto);
        branch.setCreatedBy(loggedUser.getId());
        branch.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        Branch result = branchRepository.save(branch);
        updateBranchMap(result, branchDto);
        return new ResponseEntity<Status>(new Status("200", "branch save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateBranch(BranchReqDto branchDto, User loggedUser) {
        Branch branch = new Branch(branchDto);
        branch.setUpdatedBy(loggedUser.getId());
        branch.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        Branch result = branchRepository.save(branch);
        updateBranchMap(result, branchDto);
        return new ResponseEntity<Status>(new Status("200", "branch updated  successfully!!!"), HttpStatus.OK);
    }


    public void updateBranchMap(Branch branch, BranchReqDto branchReqDto) {
        if (branchReqDto.getAddFirmIds() != null) {
            List<String> firmIds = new ArrayList<>(Arrays.asList(branchReqDto.getAddFirmIds().split("\\,")));
            for (String firmId : firmIds) {
                Optional<BranchMapping> lotOptional = branchMappingRepository.findByCompany(branch.getId(), Long.valueOf(firmId));
                if (!lotOptional.isPresent()) {
                    BranchMapping branchMapping = new BranchMapping();
                    branchMapping.setBranch(branch.getId());
                    branchMapping.setCompany(Long.valueOf(firmId));
                    branchMappingRepository.save(branchMapping);
                }
            }
        }

        if (branchReqDto.getRemoveFirmIds() != null && branchReqDto.getRemoveFirmIds() != "") {
            System.out.println(branchReqDto.getRemoveFirmIds());
            List<String> firmIds = new ArrayList<>(Arrays.asList(branchReqDto.getRemoveFirmIds().split("\\,")));
            for (String firmId : firmIds) {
                List<BranchMapping> list = branchMappingRepository.findBy(branch.getId(), Long.valueOf(firmId));
                if (list != null && !list.isEmpty()) {
                    branchMappingRepository.deleteAll(list);
                }
            }
        }
    }


    public ResponseEntity<?> getBranch(Long branchId) {
        Optional<Branch> branch = branchRepository.findById(branchId);
        BranchReqDto rs = new BranchReqDto(branch.get());
        String firmIds = branchMappingRepository.findBy(rs.getId());
        if(firmIds != null){
            rs.setFirmIds(firmIds);
        }
        return new ResponseEntity<BranchReqDto>(rs, HttpStatus.OK);
    }


    public ResponseEntity<?> deleteBranch(Long branchId) {
        Optional<Branch> branch = branchRepository.findById(branchId);
        if (branch.isPresent()) {
            Branch branch1 = branch.get();
            //branchRepository.delete(branch1);
        }
        return new ResponseEntity<Status>(new Status("200", "branch deleted  successfully!!!"), HttpStatus.OK);
    }


    public ResponseEntity<?> branchDetailList(Long orgId, String branchName, String orgName, Pageable pageable) throws UsernameNotFoundException {
        Page<Branch> list = branchRepository.branchDetailList(orgId, branchName, orgName, pageable);
        if (!list.isEmpty()) {
            List<BranchReqDto> result = list.stream().map(data -> new BranchReqDto(data)).collect(Collectors.toList());
            PageDataDto<BranchReqDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<BranchReqDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BranchDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> listAll(Long orgId, Pageable pageable) throws UsernameNotFoundException {
        List<Branch> list = branchRepository.findAll(orgId);
        if (!list.isEmpty()) {
            List<BranchDto> result = list.stream().map(data -> new BranchDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<BranchDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BranchDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAll() throws UsernameNotFoundException {
        List<Branch> list = branchRepository.findAll();
        if (!list.isEmpty()) {
            List<BranchDto> result = list.stream().map(data -> new BranchDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<BranchDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BranchDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Organisation

    public ResponseEntity<?> saveOrganization(OrganizationDto organizationDto, User loggedUser) {
        Organisation organisation = new Organisation(organizationDto);
        organisationRepository.save(organisation);
        return new ResponseEntity<Status>(new Status("200", "organization save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateOrganization(OrganizationDto organizationDto, User loggedUser) {
        Organisation organisation = new Organisation(organizationDto);
        organisationRepository.save(organisation);
        return new ResponseEntity<Status>(new Status("200", "organization updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> organizationListAll(Pageable pageable) throws UsernameNotFoundException {
        List<Organisation> list = organisationRepository.findAll();
        if (!list.isEmpty()) {
            List<OrganizationDto> result = list.stream().map(data -> new OrganizationDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<OrganizationDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<OrganizationDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deletedOrganizationListAll() throws UsernameNotFoundException {
        List<Organisation> list = organisationRepository.findAll();
        for (Organisation organisation : list) {
            organisationRepository.delete(organisation);
        }
        return new ResponseEntity<List<OrganizationDto>>(new ArrayList<>(), HttpStatus.OK);

    }

    // Department

    public void saveDepartment(DepartmentDto departmentDto) {
        DepartmentDetails departmentDetails = new DepartmentDetails(departmentDto);
        departmentRepository.save(departmentDetails);
    }

    public ResponseEntity<?> saveDepartment(DepartmentDto departmentDto, User loggedUser) {
        DepartmentDetails departmentDetails = new DepartmentDetails(departmentDto);
        departmentRepository.save(departmentDetails);
        return new ResponseEntity<Status>(new Status("200", "department save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateDepartment(DepartmentDto departmentDto, User loggedUser) {
        DepartmentDetails departmentDetails = new DepartmentDetails(departmentDto);
        departmentRepository.save(departmentDetails);
        return new ResponseEntity<Status>(new Status("200", "department updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> departmentListAll(Pageable pageable) throws UsernameNotFoundException {
        Page<DepartmentDetails> list = departmentRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<DepartmentDto> result = list.toList().stream().map(data -> new DepartmentDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<DepartmentDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DepartmentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> departmentListAll(Long orgId) throws UsernameNotFoundException {
        List<DepartmentDetails> list = departmentRepository.findAll(orgId);
        if (!list.isEmpty()) {
            List<DepartmentDto> result = list.stream().map(data -> new DepartmentDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<DepartmentDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DepartmentDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Department

    public void saveShift(ShiftDto shiftDto) {
        ShiftDetails shiftDetails = new ShiftDetails(shiftDto);
        Organisation organisation = new Organisation();
        organisation.setId(shiftDto.getOrgId());
        shiftDetails.setOrganisation(organisation);
        shiftDetailsRepository.save(shiftDetails);
    }

    public ResponseEntity<?> saveShift(ShiftDto shiftDto, User loggedUser) {
        ShiftDetails shiftDetails = new ShiftDetails(shiftDto);
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        shiftDetails.setOrganisation(organisation);
        shiftDetailsRepository.save(shiftDetails);
        return new ResponseEntity<Status>(new Status("200", "shift save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> categoryConstants(User loggedUser) {
        List<CategoryConstants> categoryConstants = constantsRepository.findAll();
        List<CategoryConstantsDto> categoryConstantsDtos = categoryConstants.stream()
                .map(val -> new CategoryConstantsDto(val)).collect(Collectors.toList());

        return new ResponseEntity<List<CategoryConstantsDto>>(categoryConstantsDtos, HttpStatus.OK);
    }

    public ResponseEntity<?> updateShift(ShiftDto shiftDto, User loggedUser) {
        ShiftDetails shiftDetails = new ShiftDetails(shiftDto);
        Organisation organisation = new Organisation();
        organisation.setId(loggedUser.getOrg_id());
        shiftDetails.setOrganisation(organisation);
        shiftDetailsRepository.save(shiftDetails);
        return new ResponseEntity<Status>(new Status("200", "shift updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> shiftListAll() throws UsernameNotFoundException {
        List<ShiftDetails> list = shiftDetailsRepository.findAll();
        if (!list.isEmpty()) {
            List<ShiftDto> result = list.stream().map(data -> new ShiftDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<ShiftDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ShiftDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

//vendor

    public void saveVendor(VendorDto vendorDto) {
        Vendor vendor = new Vendor(vendorDto);
        vendorRepository.save(vendor);
    }

    public ResponseEntity<?> saveVendor(VendorDto vendorDto, User loggedUser) {
        Vendor vendor = new Vendor(vendorDto);
        vendorRepository.save(vendor);
        return new ResponseEntity<Status>(new Status("200", "vendor save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateVendor(VendorDto vendorDto, User loggedUser) {
        Vendor vendor = new Vendor(vendorDto);
        vendorRepository.save(vendor);
        return new ResponseEntity<Status>(new Status("200", "vendor updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> vendorListAll(Pageable pageable) throws UsernameNotFoundException {
        List<Vendor> list = vendorRepository.findAll();
        if (!list.isEmpty()) {
            List<VendorDto> result = list.stream().map(data -> new VendorDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<VendorDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<VendorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Day
    public ResponseEntity<?> saveDay(DayDto dayDto, User loggedUser) {
        Day day = new Day(dayDto);
        dayRepository.save(day);
        return new ResponseEntity<Status>(new Status("200", "day save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateDay(DayDto dayDto, User loggedUser) {
        Day day = new Day(dayDto);
        dayRepository.save(day);
        return new ResponseEntity<Status>(new Status("200", "day updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> dayListAll() throws UsernameNotFoundException {
        List<Day> list = dayRepository.findAll();
        if (!list.isEmpty()) {
            List<DayDto> result = list.stream().map(data -> new DayDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<DayDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DayDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Bill Number
    public ResponseEntity<?> saveBillNumber(BillNumberDto billNumberDto, User loggedUser) {
        BillNumber billNumber = new BillNumber(billNumberDto);
        billNumberRepository.save(billNumber);
        return new ResponseEntity<Status>(new Status("200", "BillNumber save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateBillNumber(BillNumberDto billNumberDto, User loggedUser) {
        BillNumber billNumber = new BillNumber(billNumberDto);
        billNumberRepository.save(billNumber);
        return new ResponseEntity<Status>(new Status("200", "BillNumber updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> billNumberListAll() throws UsernameNotFoundException {
        List<BillNumber> list = billNumberRepository.findAll();
        if (!list.isEmpty()) {
            List<BillNumberDto> result = list.stream().map(data -> new BillNumberDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<BillNumberDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BillNumberDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> bloodGroupListAll(Pageable pageable) throws UsernameNotFoundException {
        List<BloodGroup> list = bloodGroupRepository.findAll();
        if (!list.isEmpty()) {
            List<BloodGroupDto> result = list.stream().map(data -> new BloodGroupDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<BloodGroupDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BloodGroupDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveCluster(ClusterDto clusterDto, User loggedUser) {
        Cluster cluster = new Cluster();
        clusterRepository.save(cluster);
        return new ResponseEntity<Status>(new Status("200", "cluster save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCluster(ClusterDto clusterDto, User loggedUser) {
        Cluster cluster = new Cluster();
        clusterRepository.save(cluster);
        return new ResponseEntity<Status>(new Status("200", "branch updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listClusterAll(Pageable pageable) throws UsernameNotFoundException {
        Page<Cluster> list = clusterRepository.findAll(pageable);
        if (!list.toList().isEmpty()) {
            List<ClusterDto> result = list.toList().stream().map(data -> new ClusterDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<ClusterDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> searchClusterList(String name) {
        List<DropDownList> list = clusterRepository.searchAllTClusterName(name);
        if (!list.isEmpty()) {
            List<DropDownListDto> result = list.stream().map(data -> new DropDownListDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<DropDownListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DropDownListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveUnits(UnitsDto unitsDto, User loggedUser) {
        Optional<Units> unitsOptional = unitsRepository.findByName(unitsDto.getName());
        if (unitsOptional.isPresent()) {
            return new ResponseEntity<Status>(new Status("400", "Unit Already exists!!!"), HttpStatus.OK);
        } else {
            Units units = new Units(unitsDto);
            unitsRepository.save(units);
            return new ResponseEntity<Status>(new Status("200", "units save successfully!!!"), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> updateUnits(UnitsDto unitsDto, User loggedUser) {
        Units units = new Units(unitsDto);
        unitsRepository.save(units);
        return new ResponseEntity<Status>(new Status("200", "units updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listUnitsAll() throws UsernameNotFoundException {
        List<Units> list = unitsRepository.findAll();
        if (!list.isEmpty()) {
            List<UnitsDto> result = list.stream().map(data -> new UnitsDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<UnitsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<UnitsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveProductCategory(ProductCategoryDto productCategoryDto, User loggedUser) {
        Optional<ProductCategory> optional = productCategoryRepository.findByName(productCategoryDto.getName());
        if (optional.isPresent()) {
            return new ResponseEntity<Status>(new Status("400", "product category name already exists!!!"),
                    HttpStatus.OK);
        } else {
            ProductCategory productCategory = new ProductCategory(productCategoryDto);
            productCategoryRepository.save(productCategory);
            return new ResponseEntity<Status>(new Status("200", "ProductCategory save successfully!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> updateProductCategory(ProductCategoryDto productCategoryDto, User loggedUser) {
        ProductCategory productCategory = new ProductCategory(productCategoryDto);
        productCategoryRepository.save(productCategory);
        return new ResponseEntity<Status>(new Status("200", "branch updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listProductCategoryAll() throws UsernameNotFoundException {
        List<ProductCategory> list = productCategoryRepository.findAll();
        if (!list.isEmpty()) {
            List<ProductCategoryDto> result = list.stream().map(data -> new ProductCategoryDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<ProductCategoryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductCategoryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // country

    public ResponseEntity<?> saveCountry(CountryDto countryDto, User loggedUser) {
        Country country = new Country(countryDto);
        countryRepository.save(country);
        return new ResponseEntity<Status>(new Status("200", "Country save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCountry(CountryDto countryDto, User loggedUser) {
        Country country = new Country(countryDto);
        countryRepository.save(country);
        return new ResponseEntity<Status>(new Status("200", "Country updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllCountry() throws UsernameNotFoundException {
        List<Country> list = countryRepository.findAll();
        if (!list.isEmpty()) {
            List<CountryDto> result = list.stream().map(data -> new CountryDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<CountryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CountryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // State

    public ResponseEntity<?> saveState(StateDto stateDto, User loggedUser) {
        State state = new State(stateDto);
        stateRepository.save(state);
        return new ResponseEntity<Status>(new Status("200", "State save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateState(StateDto stateDto, User loggedUser) {
        State state = new State(stateDto);
        stateRepository.save(state);
        return new ResponseEntity<Status>(new Status("200", "State updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllState() throws UsernameNotFoundException {
        List<State> list = stateRepository.findAll();
        if (!list.isEmpty()) {
            List<StateDto> result = list.stream().map(data -> new StateDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<StateDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<StateDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllState(long countryId) throws UsernameNotFoundException {
        List<State> list = stateRepository.findByCountryId(countryId);
        if (!list.isEmpty()) {
            List<StateDto> result = list.stream().map(data -> new StateDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<StateDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<StateDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Districts

    public ResponseEntity<?> saveDistrict(DistrictsDto districtsDto, User loggedUser) {
        Districts districts = new Districts(districtsDto);
        districtsRepository.save(districts);
        return new ResponseEntity<Status>(new Status("200", "Districts save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateDistrict(DistrictsDto districtsDto, User loggedUser) {
        Districts districts = new Districts(districtsDto);
        districtsRepository.save(districts);
        return new ResponseEntity<Status>(new Status("200", "Districts updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllDistrict() throws UsernameNotFoundException {
        List<Districts> list = districtsRepository.findAll();
        if (!list.isEmpty()) {
            List<DistrictsDto> result = list.stream().map(data -> new DistrictsDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<DistrictsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DistrictsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getAllDistrict(String place) throws UsernameNotFoundException {
        List<Districts> list = districtsRepository.findBy(place);
        if (!list.isEmpty()) {
            List<DistrictsDto> result = list.stream().map(data -> new DistrictsDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<DistrictsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DistrictsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // City

    public ResponseEntity<?> saveCity(CityDto cityDto, User loggedUser) {
        if (cityDto.getName() != null) {
            Optional<City> existingCity = cityRepository.findByName(cityDto.getName(), cityDto.getStateId());
            if (existingCity.isPresent()) {
                return ResponseEntity.badRequest().body(new Status("400", "City Already Exists!!!"));
            }
        }
        City city = new City(cityDto);
        cityRepository.save(city);
        return new ResponseEntity<Status>(new Status("200", "City save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCity(CityDto cityDto, User loggedUser) {
        City city = new City(cityDto);
        cityRepository.save(city);
        return new ResponseEntity<Status>(new Status("200", "City updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllCity() throws UsernameNotFoundException {
        List<City> list = cityRepository.findAll();
        if (!list.isEmpty()) {
            List<CityDto> result = list.stream().map(data -> new CityDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<CityDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CityDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllCity(long stateId) throws UsernameNotFoundException {
        List<City> list = cityRepository.findByStateId(stateId);
        if (!list.isEmpty()) {
            List<CityDto> result = list.stream().map(data -> new CityDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<CityDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CityDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> cityList(String place) throws UsernameNotFoundException {
        List<City> list = cityRepository.findBy(place);
        if (!list.isEmpty()) {
            List<CityDto> result = list.stream().map(data -> new CityDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<CityDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CityDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // TrainingType

    public ResponseEntity<?> saveTrainingType(List<TrainingTypeDto> trainingTypeDtos, User loggedUser)
            throws UsernameNotFoundException {

        List<TrainingType> trainingTypes = new ArrayList<>();
        for (TrainingTypeDto trainingTypeDto : trainingTypeDtos) {
            TrainingType trainingType = new TrainingType(trainingTypeDto);
            trainingTypes.add(trainingType);
        }
        trainingTypeRepository.saveAll(trainingTypes);
        return new ResponseEntity<Status>(new Status("200", "TrainingTypes saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateTrainingType(List<TrainingTypeDto> trainingTypeDtos, User loggedUser) {
        List<TrainingType> trainingTypes = new ArrayList<>();
        for (TrainingTypeDto trainingTypeDto : trainingTypeDtos) {
            TrainingType trainingType = new TrainingType(trainingTypeDto);
            trainingTypes.add(trainingType);
        }
        trainingTypeRepository.saveAll(trainingTypes);
        return new ResponseEntity<Status>(new Status("200", "TrainingTypes updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> trainingTypeList() throws UsernameNotFoundException {
        List<TrainingType> list = trainingTypeRepository.findAll();
        if (!list.isEmpty()) {
            List<TrainingTypeDto> result = list.stream().map(data -> new TrainingTypeDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TrainingTypeDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TrainingTypeDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Task Add

    public ResponseEntity<?> saveTask(TaskDto reqDto, User user) throws UsernameNotFoundException {
        Optional<Task> task = taskRepo.findByTaskName(reqDto.getTaskName());
        if (!task.isPresent()) {
            Task task1 = new Task(reqDto);
            taskRepo.save(task1);
            return new ResponseEntity<Status>(new Status("200", "Task saved successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Task Names AlReady exists!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> allTask(String task, User user) throws UsernameNotFoundException {
        List<Task> list = taskRepo.findAllTaskName(task);
        if (!list.isEmpty()) {
            List<TaskDto> result = list.stream().map(data -> new TaskDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<TaskDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TaskDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Task Names

    public ResponseEntity<?> saveTaskMap(TaskNameDto reqDto, User user) throws UsernameNotFoundException {

        Optional<TaskName> optional = taskNameRepository.findByTaskName(reqDto.getTaskCategoryId(),
                reqDto.getTaskName());
        if (!optional.isPresent()) {
            TaskName taskName = new TaskName(reqDto);
            TaskName rs = taskNameRepository.save(taskName);
            return new ResponseEntity<TaskNameDto>(new TaskNameDto(rs), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "Task name already exists!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveTaskNames(ArrayList<TaskNameDto> reqDto, User user) throws UsernameNotFoundException {
        List<TaskName> taskNames = new ArrayList<>();
        for (TaskNameDto taskNameDto : reqDto) {
            Optional<TaskName> optional = taskNameRepository.findByTaskName(taskNameDto.getTaskCategoryId(),
                    taskNameDto.getTaskName());
            if (!optional.isPresent()) {
                TaskName taskName = new TaskName(taskNameDto);
                taskNames.add(taskName);
            } else {
                return new ResponseEntity<Status>(new Status("400", "Task name already exists!!!"), HttpStatus.OK);
            }
        }
        taskNameRepository.saveAll(taskNames);
        return new ResponseEntity<Status>(new Status("200", "TaskNames saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> saveTaskGroupWithTaskName(TaskGroupWithTaskNameDto reqDto)
            throws UsernameNotFoundException {

        Optional<TaskGroup> optional = taskGroupRepository.findByTaskTaskGroup(reqDto.getTaskGroup(),
                reqDto.getTaskId(), reqDto.getTaskCategoryId());
        if (!optional.isPresent()) {
            Optional<TaskCategory> optionalTaskCategory = taskCategoryRepository.findById(reqDto.getTaskCategoryId());
            Optional<TaskName> optionalTaskName = taskNameRepository.findById(reqDto.getTaskId());
            if (optionalTaskCategory.isPresent() && optionalTaskName.isPresent()) {
                TaskName taskName = optionalTaskName.get();
                TaskGroup taskGroup = new TaskGroup();
                taskGroup.setTaskGroupName(reqDto.getTaskGroupName());
                taskGroup.setTaskName(taskName);
                taskGroup.setTaskCategory(optionalTaskCategory.get());
                taskGroup.setTaskgroup(reqDto.getTaskGroup());
                taskGroupRepository.save(taskGroup);
            } else {
                return new ResponseEntity<Status>(new Status("400", "TaskGroup Not saved!!!"), HttpStatus.OK);
            }
            return new ResponseEntity<Status>(new Status("200", "TaskGroup saved successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "TaskGroupMap  AlReady exists!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> searchTaskMap(String taskCategoryName, Long taskCategoryId, String search,
                                           Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 10);
        }
        List<TaskName> list = taskNameRepository.findAllTaskName(search, taskCategoryName, taskCategoryId);
        if (!list.isEmpty()) {
            List<TaskNameDto> result = list.stream().map(data -> new TaskNameDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<TaskNameDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TaskNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> taskNames(String taskName, String taskCategoryName, Long taskCategoryId, String search,
                                       Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 10);
        }
        Page<TaskName> list = null;
        if ((taskCategoryId == null || taskCategoryId == 0) && search != null) {
            List<TaskName> searched = taskNameRepository.searchByTaskName(search);
            List<TaskNameDto> result = searched.stream().map(data -> new TaskNameDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskNameDto>>(result, HttpStatus.OK);
        } else if (taskCategoryId == null || taskCategoryId == 0) {
            list = taskNameRepository.findAll(pageable);
        } else
            list = taskNameRepository.findAllTaskNameByCategory(taskCategoryId, pageable);
        if (!list.toList().isEmpty()) {
            List<TaskNameDto> result = list.toList().stream().map(data -> new TaskNameDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskNameDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TaskNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> taskList(String taskName, Long taskCategoryId, Long taskGroupId) {
        List<TaskName> list = taskNameRepository.findByTaskList(taskCategoryId, taskGroupId, taskName);
        List<TaskNameDto> result = list.stream().map(data -> new TaskNameDto(data)).collect(Collectors.toList());
        return new ResponseEntity<List<TaskNameDto>>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> taskNames(String taskName, Long taskCategoryId, Pageable pageable) {
        Page<TaskName> list = null;
        if (taskName == null) {
            list = taskNameRepository.findAllTaskNameByCategory(taskCategoryId, pageable);
            List<TaskNameDto> result = list.toList().stream().map(data -> new TaskNameDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskNameDto>>(result, HttpStatus.OK);
        } else {
            List<TaskName> searched = taskNameRepository.searchByTaskName(taskCategoryId, taskName);
            List<TaskNameDto> result = searched.stream().map(data -> new TaskNameDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskNameDto>>(result, HttpStatus.OK);
        }
    }

    public ResponseEntity<?> saveTaskCategories(ArrayList<TaskCategoryDto> reqDto, User user) {
        List<TaskCategory> taskCategorys = new ArrayList<>();
        for (TaskCategoryDto taskCategoryDto : reqDto) {
            Optional<TaskCategory> optional = taskCategoryRepository
                    .findByTaskCategoryName(taskCategoryDto.getTaskCategoryName());
            if (!optional.isPresent()) {
                TaskCategory taskCategory = new TaskCategory(taskCategoryDto);
                taskCategorys.add(taskCategory);
            } else {
                return new ResponseEntity<Status>(new Status("400", "TaskCategories name already exists!!!"),
                        HttpStatus.OK);
            }
        }
        taskCategoryRepository.saveAll(taskCategorys);
        return new ResponseEntity<Status>(new Status("200", "TaskCategories saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> taskCategories(String taskCategoryName, String modeOfEntry) {
        List<TaskCategory> list = taskCategoryRepository.findAllTaskCategoryName(taskCategoryName, modeOfEntry);
        if (!list.isEmpty()) {
            List<TaskCategoryDto> result = list.stream().map(data -> new TaskCategoryDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskCategoryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TaskCategoryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> searchCategories(String taskCategoryName) {
        List<DropDownList> list = taskCategoryRepository.searchAllTaskCategoryName(taskCategoryName);
        if (!list.isEmpty()) {
            List<DropDownListDto> result = list.stream().map(data -> new DropDownListDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<DropDownListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DropDownListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Task Group Master

    public ResponseEntity<?> saveTaskGroupsMaster(ArrayList<TaskGroupMasterDto> reqDto, User user) {
        List<TaskGroupMaster> taskGroups = new ArrayList<>();
        for (TaskGroupMasterDto taskGroupDto : reqDto) {
            System.out.println(taskGroupDto.getTaskGroupName());
            Optional<TaskGroupMaster> optional = taskGroupMasterRepository
                    .findByTaskGroupMasterName(taskGroupDto.getTaskGroupName());
            if (!optional.isPresent()) {
                TaskGroupMaster taskCategory = new TaskGroupMaster(taskGroupDto);
                taskGroups.add(taskCategory);
            } else {
                return new ResponseEntity<Status>(new Status("400", "TaskGroupsMaster name already exists!!!"),
                        HttpStatus.OK);
            }
        }
        if (!taskGroups.isEmpty()) {
            taskGroupMasterRepository.saveAll(taskGroups);
            return new ResponseEntity<Status>(new Status("200", "TaskGroupsMaster saved successfully!!!"),
                    HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "TaskGroupsMaster not saved!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> taskGroupsMaster(String taskGroupName, Pageable pageable) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 10);
        }
        Page<TaskGroupMaster> list = taskGroupMasterRepository.findAllByTaskGroupMasterName(taskGroupName, pageable);
        if (!list.toList().isEmpty()) {
            List<TaskGroupMasterDto> result = list.toList().stream().map(data -> new TaskGroupMasterDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskGroupMasterDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TaskGroupMasterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // task Group

    public ResponseEntity<?> saveTaskGroups(ArrayList<TaskGroupDto> reqDto, User user) {
        for (TaskGroupDto taskGroupDto : reqDto) {
            // if (taskGroupDto.getTaskgroup() == null) {
            TaskGroupMaster taskGroupMaster = new TaskGroupMaster(taskGroupDto.getTaskGroupName(), user.getId());
            taskGroupMaster = taskGroupMasterRepository.save(taskGroupMaster);
            taskGroupDto.setTaskgroup(taskGroupMaster.getId());
            // }
            // TaskGroup taskCategory = new TaskGroup(taskGroupDto);
            // taskGroupRepository.save(taskCategory);
        }
        return new ResponseEntity<Status>(new Status("200", "TaskGroups saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> taskGroups(String taskGroup, Long taskCategoryId, String taskCategoryName, Long taskNameId,
                                        Pageable pageable, String search) {
        if (pageable == null) {
            pageable = PageRequest.of(0, 10);
        }
        Page<TaskGroup> list = null;
        if ((taskCategoryId == null || taskNameId == null) && search != null) {
            List<TaskGroup> searched = taskGroupRepository.searchByTaskGroup(search);
            List<TaskGroupDto> result = searched.stream()
                    .map(data -> new TaskGroupDto(data.getId(), data.getTaskGroupName(), data.getTaskgroup()))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskGroupDto>>(result, HttpStatus.OK);
        } else if (taskCategoryId == null || taskNameId == null) {
            list = taskGroupRepository.findAll(pageable);
        } else
            list = taskGroupRepository.findAllByTaskGroupName(taskGroup, taskCategoryId, taskCategoryName, taskNameId,
                    pageable);
        if (!list.toList().isEmpty()) {
            List<TaskGroupDto> result = list.toList().stream().map(data -> new TaskGroupDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<TaskGroupDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<TaskGroupDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public List<TaskGroupDropDownDto> getTaskGroupForCategory(Long taskCategoryId) {
        List<TaskGroup> list = taskGroupRepository.findAllByTaskCategoryId(taskCategoryId);
        List<TaskGroupDropDownDto> dropDownDtos = new ArrayList<>();
        HashSet<Long> hashSet = new HashSet<>();
        if (!list.isEmpty()) {
            for (TaskGroup data : list) {
                if (data.getTaskgroup() != null) {
                    if (!hashSet.contains(data.getTaskgroup())) {
                        hashSet.add(data.getTaskgroup());
                        dropDownDtos.add(new TaskGroupDropDownDto(data.getTaskgroup(), data.getTaskGroupName()));
                    }
                }
            }
            return dropDownDtos;
        } else
            return new ArrayList<>();

    }

    // Biomast
    public ResponseEntity<?> saveBiomast(List<BiomastDto> biomastDtos, User loggedUser)
            throws UsernameNotFoundException {

        List<Biomast> biomasts = new ArrayList<>();
        for (BiomastDto biomastDto : biomastDtos) {
            Biomast biomast = new Biomast(biomastDto);
            biomasts.add(biomast);
        }
        biomastRepository.saveAll(biomasts);
        return new ResponseEntity<Status>(new Status("200", "Biomasts saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateBiomast(List<BiomastDto> biomastDtos, User loggedUser) {
        List<Biomast> biomasts = new ArrayList<>();
        for (BiomastDto biomastDto : biomastDtos) {
            Biomast biomast = new Biomast(biomastDto);
            biomasts.add(biomast);
        }
        biomastRepository.saveAll(biomasts);
        return new ResponseEntity<Status>(new Status("200", "Biomasts updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> biomastList() throws UsernameNotFoundException {
        List<Biomast> list = biomastRepository.findAll();
        if (!list.isEmpty()) {
            List<BiomastDto> result = list.stream().map(data -> new BiomastDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<BiomastDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<BiomastDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllRawMaterial(String rawMaterial, Pageable pageable) {
        List<Tuple> list = productMasterRepository.findAllByRawmaterial1(rawMaterial, pageable);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Pshifts

    public ResponseEntity<?> savePShifts(PShiftsDto pShiftsDto, User loggedUser) throws UsernameNotFoundException {
        PShifts pShifts = new PShifts(pShiftsDto);
        pShiftsRepository.save(pShifts);
        return new ResponseEntity<Status>(new Status("200", "Pshifts saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updatePShifts(PShiftsDto pShiftsDto, User loggedUser) {
        PShifts pShifts = new PShifts(pShiftsDto);
        pShiftsRepository.save(pShifts);
        return new ResponseEntity<Status>(new Status("200", "Pshifts updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> pshiftsList() throws UsernameNotFoundException {
        List<PShifts> list = pShiftsRepository.findAll();
        if (!list.isEmpty()) {
            List<PShiftsDto> result = list.stream().map(data -> new PShiftsDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<PShiftsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> pshiftsListDropDown() {
        List<Tuple> list = pShiftsRepository.findAlls();
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Kpiname

    public ResponseEntity<?> saveKpiname(KpinameDto kpinameDto, User loggedUser) throws UsernameNotFoundException {
        Kpiname kpiname = new Kpiname(kpinameDto);
        kpiNameRepository.save(kpiname);
        return new ResponseEntity<Status>(new Status("200", "kpiname saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateKpiname(KpinameDto kpinameDto, User loggedUser) {
        Kpiname kpiname = new Kpiname(kpinameDto);
        kpiNameRepository.save(kpiname);
        return new ResponseEntity<Status>(new Status("200", "kpiname updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> kpiNameList() throws UsernameNotFoundException {
        List<Kpiname> list = kpiNameRepository.findAll();
        if (!list.isEmpty()) {
            List<KpinameDto> result = list.stream().map(data -> new KpinameDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<KpinameDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<KpinameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> kpiNameListDropDown(String type) {
        List<Tuple> list = kpiNameRepository.findAlls(type);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> wcshiftList(Long proId, Long dept, Long branch, String pshift, String date) {
        DateUtil dateUtil = new DateUtil();
        List<Wcshift> list = wcShiftsRepository.findAlls(proId, dept, branch, pshift);
        if (!list.isEmpty()) {
            List<WcShiftsDto> result = list.stream().map(data -> new WcShiftsDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<WcShiftsDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<WcShiftsDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> taskGroupList(Long kpiId) {
        DateUtil dateUtil = new DateUtil();
        Optional<Kpiname> optionalKpiname = kpiNameRepository.findById(kpiId);
        if (optionalKpiname.isPresent()) {
            List<Long> tgids = kpiRepository.findAll(kpiId, optionalKpiname.get().getProduct().getId());
            if (!tgids.isEmpty()) {
                Set<String> list = taskGroupRepository.findAllTaskGroupById(tgids);
                if (!list.isEmpty()) {
                    return new ResponseEntity<Set<String>>(list, HttpStatus.OK);
                } else {
                    return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
                }
            } else {
                return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Panel Names

    public ResponseEntity<?> savePanelNames(ArrayList<PanelNameDto> reqDto, User user)
            throws UsernameNotFoundException {
        List<PanelName> panelNames = new ArrayList<>();
        for (PanelNameDto panelNameDto : reqDto) {
            PanelName panelName = new PanelName(panelNameDto);
            panelName.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            panelName.setCreatedBy(user.getId());
            panelNames.add(panelName);
        }
        panelNameRepository.saveAll(panelNames);
        return new ResponseEntity<Status>(new Status("200", "PanelNames saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> panelNames(String panelName, Pageable pageable) {
        Page<PanelName> list = panelNameRepository.findAllPanelName(panelName, pageable);
        if (!list.toList().isEmpty()) {
            List<PanelNameDto> result = list.toList().stream().map(data -> new PanelNameDto(data))
                    .collect(Collectors.toList());
            PageDataDto<PanelNameDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<PanelNameDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PanelNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getAllPanelNames(String panelName) {
        List<PanelName> list = panelNameRepository.findAllPanelName(panelName);
        if (!list.isEmpty()) {
            List<PanelNameDto> result = list.stream().map(data -> new PanelNameDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<PanelNameDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<PanelNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Schedule Names

    public ResponseEntity<?> saveScheduleNames(ArrayList<ScheduleNameDto> reqDto, User user)
            throws UsernameNotFoundException {
        List<ScheduleName> scheduleNames = new ArrayList<>();
        for (ScheduleNameDto scheduleNameDto : reqDto) {
            ScheduleName scheduleName = new ScheduleName(scheduleNameDto);
            scheduleName.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            scheduleName.setCreatedBy(user.getId());
            scheduleNames.add(scheduleName);
        }
        scheduleNameRepository.saveAll(scheduleNames);
        return new ResponseEntity<Status>(new Status("200", "ScheduleNames saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> scheduleNames(String scheduleName, Pageable pageable) {
        Page<ScheduleName> list = scheduleNameRepository.findAllScheduleName(scheduleName, pageable);
        if (!list.toList().isEmpty()) {
            List<ScheduleNameDto> result = list.toList().stream().map(data -> new ScheduleNameDto(data))
                    .collect(Collectors.toList());
            PageDataDto<ScheduleNameDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<ScheduleNameDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ScheduleNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> allscheduleName(String scheduleName) {
        List<ScheduleName> list = scheduleNameRepository.findAllScheduleName(scheduleName);
        if (!list.isEmpty()) {
            List<ScheduleNameDto> result = list.stream().map(data -> new ScheduleNameDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<ScheduleNameDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ScheduleNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // DepreciationCategory Names

    public ResponseEntity<?> saveDepreciationCategoryNames(ArrayList<DepreciationCategoryNameDto> reqDto, User user)
            throws UsernameNotFoundException {
        List<DepreciationCategoryName> depreciationCategoryNames = new ArrayList<>();
        for (DepreciationCategoryNameDto depreciationCategoryNameDto : reqDto) {
            DepreciationCategoryName depreciationCategoryName = new DepreciationCategoryName(
                    depreciationCategoryNameDto);
            depreciationCategoryName.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            depreciationCategoryName.setCreatedBy(user.getId());
            depreciationCategoryNames.add(depreciationCategoryName);
        }
        depreciationCategoryNameRepository.saveAll(depreciationCategoryNames);
        return new ResponseEntity<Status>(new Status("200", "DepreciationCategoryNames saved successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> depreciationCategoryNames(String depreciationCategoryName, Pageable pageable) {
        Page<DepreciationCategoryName> list = depreciationCategoryNameRepository
                .findAllDepreciationCategoryName(depreciationCategoryName, pageable);
        if (!list.toList().isEmpty()) {
            List<DepreciationCategoryNameDto> result = list.toList().stream()
                    .map(data -> new DepreciationCategoryNameDto(data)).collect(Collectors.toList());
            PageDataDto<DepreciationCategoryNameDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<DepreciationCategoryNameDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DepreciationCategoryNameDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // camp

    public ResponseEntity<?> saveCamp(List<CampDto> campDtoList, User loggedUser) throws UsernameNotFoundException {
        if (!campDtoList.isEmpty()) {
            for (CampDto campDto : campDtoList) {
                Camp camp = new Camp(campDto);
                campRepository.save(camp);
            }
        } else {
            return new ResponseEntity<Status>(new Status("200", "camp not saved successfully!!!"), HttpStatus.OK);
        }
        return new ResponseEntity<Status>(new Status("200", "camp saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCamp(CampDto campDto, User loggedUser) {
        Camp camp = new Camp(campDto);
        campRepository.save(camp);
        return new ResponseEntity<Status>(new Status("200", "Camp updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> campList() throws UsernameNotFoundException {
        List<Camp> list = campRepository.findAll();
        if (!list.isEmpty()) {
            List<CampDto> result = list.stream().map(data -> new CampDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<CampDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CampDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> campListDropDown(String state) {
        List<Tuple> list = campRepository.findAlls(state);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("camp"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> campNameList(String place) {
        List<Camp> list = campRepository.campNameList(place);
        if (!list.isEmpty()) {
            List<CampDto> result = list.stream().map(data -> new CampDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<CampDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CampDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // color

    public ResponseEntity<?> saveColor(ColorDto campDto, User loggedUser) throws UsernameNotFoundException {
        Color color = new Color(campDto);
        colorRepository.save(color);
        return new ResponseEntity<Status>(new Status("200", "Color saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateColor(ColorDto colorDto, User loggedUser) {
        Color color = new Color(colorDto);
        colorRepository.save(color);
        return new ResponseEntity<Status>(new Status("200", "Color updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> colorList() throws UsernameNotFoundException {
        List<Color> list = colorRepository.findAll();
        if (!list.isEmpty()) {
            List<ColorDto> result = list.stream().map(data -> new ColorDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<ColorDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ColorDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> colorListDropDown() {
        List<Tuple> list = colorRepository.findAlls();
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // Luzone

    public ResponseEntity<?> saveLuZone(LuZoneDto luZoneDto, User loggedUser) {
        LuZone luzone = new LuZone(luZoneDto);
        luZoneRepository.save(luzone);
        return new ResponseEntity<Status>(new Status("200", "luZoneDto save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateLuZone(LuZoneDto luZoneDto, User loggedUser) {
        LuZone luzone = new LuZone(luZoneDto);
        luZoneRepository.save(luzone);
        return new ResponseEntity<Status>(new Status("200", "luZoneDto updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> luzoneListAll() throws UsernameNotFoundException {
        List<LuZone> list = luZoneRepository.findAll();
        if (!list.isEmpty()) {
            List<LuZoneDto> result = list.stream().map(data -> new LuZoneDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<LuZoneDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<LuZoneDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> daCategoryAll() throws UsernameNotFoundException {
        List<DaCategory> list = daCategoryRepository.findAll();
        if (!list.isEmpty()) {
            List<DaCategoryDto> result = list.stream().map(data -> new DaCategoryDto(data))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<DaCategoryDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DaCategoryDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> luzoneListAll(Long branch, Long dept, String gender, String staffType, Long actType)
            throws UsernameNotFoundException {

        List<Tuple> list = new ArrayList<>();
        if (gender.equalsIgnoreCase("male")) {
            list = luZoneRepository.findMaleAll(branch, dept, actType, staffType);
        } else {
            list = luZoneRepository.findFeMaleAll(branch, dept, actType, staffType);
        }
        if (!list.isEmpty()) {
            List<CommonDutyDto> result = list.stream()
                    .map(data -> {
                        CommonDutyDto co = new CommonDutyDto(data.get("id"), data.get("name"), data.get("wcid"), data.get("wctable"));
                        if (co.getWctable() != null && co.getWctable().equalsIgnoreCase("dwcluster")) {
                            Optional<DwCluster> dwCluster = dwClusterRepository.findById(co.getWcid());
                            if (dwCluster.isPresent()) {
                                co.setWorkCenterName(co.getWcid() + "#" + dwCluster.get().getName());
                            }
                        } else {
                            Optional<Machine> dwCluster = machineRepository.findById(co.getWcid());
                            if (dwCluster.isPresent()) {
                                co.setWorkCenterName(co.getWcid() + "#" + dwCluster.get().getName());
                            }
                        }
                        return co;
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<List<CommonDutyDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDutyDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> luZoneStaffList(Long branch, Long dept, String staffType, Long actType)
            throws UsernameNotFoundException {

        if (actType != null && actType == 1) {
            actType = 6L;
        }
        List<Tuple> list = new ArrayList<>();

        list = luZoneRepository.luZoneStaffList(branch, dept, actType, staffType);

        if (!list.isEmpty()) {
            List<CommonDutyDto> result = list.stream()
                    .map(data -> {
                        CommonDutyDto co = new CommonDutyDto(data.get("id"), data.get("name"), data.get("wcid"), data.get("wctable"));
                        if (co.getWctable() != null && co.getWctable().equalsIgnoreCase("dwcluster")) {
                            Optional<DwCluster> dwCluster = dwClusterRepository.findById(co.getWcid());
                            if (dwCluster.isPresent()) {
                                co.setWorkCenterName(co.getWcid() + "#" + dwCluster.get().getName());
                            }
                        } else {
                            Optional<Machine> dwCluster = machineRepository.findById(co.getWcid());
                            if (dwCluster.isPresent()) {
                                co.setWorkCenterName(co.getWcid() + "#" + dwCluster.get().getName());
                            }
                        }
                        return co;
                    })
                    .collect(Collectors.toList());
            return new ResponseEntity<List<CommonDutyDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDutyDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> bslapListDropDown(Long tsId) {
        List<Tuple> list = bsLapRepository.findAlls(tsId);
        if (!list.isEmpty()) {
            List<CommonDropDownDto> result = list.stream()
                    .map(data -> new CommonDropDownDto(data.get("id"), data.get("name"))).collect(Collectors.toList());
            return new ResponseEntity<List<CommonDropDownDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CommonDropDownDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    // supplierGroup

    public ResponseEntity<?> saveSupplierGroup(SupplierGroupDto supplierGroupDto, User loggedUser) {
        SupplierGroup supplierGroup = new SupplierGroup(supplierGroupDto);
        supplierGroupRepo.save(supplierGroup);
        return new ResponseEntity<Status>(new Status("200", "supplierGroup save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateSupplierGroup(SupplierGroupDto supplierGroupDto, User loggedUser) {
        SupplierGroup supplierGroup = new SupplierGroup(supplierGroupDto);
        supplierGroupRepo.save(supplierGroup);
        return new ResponseEntity<Status>(new Status("200", "SupplierGroup updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> supplierGroupAll(String name) throws UsernameNotFoundException {
        List<SupplierGroup> list = supplierGroupRepo.findAll(name);
        if (!list.isEmpty()) {
            List<SupplierGroupDto> result = list.stream().map(data -> new SupplierGroupDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<SupplierGroupDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<SupplierGroupDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteSupplierGroup(Long id) throws UsernameNotFoundException {
        Optional<SupplierGroup> supplierGroup = supplierGroupRepo.findById(id);
        if (supplierGroup.isPresent()) {
            SupplierGroup supplierGroup1 = supplierGroup.get();
            supplierGroupRepo.delete(supplierGroup1);
            return new ResponseEntity<Status>(new Status("200", "SupplierGroup deleted  successfully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "SupplierGroup not deleted!!!"), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> taskGroupsSearch(String modeOfEntry, String taskGroup, String taskCategoryName,
                                              String taskName, Pageable pageable) {
        Page<TaskGroup> list = null;
        if (modeOfEntry != null && modeOfEntry.equalsIgnoreCase("M")) {
            list = taskGroupRepository.findAllByTaskGroupNameForMaintaince(taskGroup, taskCategoryName, taskName,
                    modeOfEntry, pageable);
        } else
            list = taskGroupRepository.findAllByTaskGroupNameForInProcess(taskGroup, taskCategoryName, taskName,
                    modeOfEntry, pageable);
        List<TaskGroupDto> result = list.toList().stream().map(data -> new TaskGroupDto(data))
                .collect(Collectors.toList());
        PageDataDto<TaskGroupDto> pageData = new PageDataDto<>(result, list);
        return new ResponseEntity<PageDataDto<TaskGroupDto>>(pageData, HttpStatus.OK);

    }

    public void deleteTaskGroupCategoryMapping(Long id) {
        taskGroupRepository.deleteById(id);
    }

    public List<MailDropDownDto> getMailSender() {
        List<MailSendEntity> list = mailSendRepository.findByStatus("Y");
        if (list.isEmpty())
            return new ArrayList<>();
        return list.stream().map(data -> new MailDropDownDto(data.getId(), data.getMailid()))
                .collect(Collectors.toList());
    }

    public List<OffReasonDto> getOffReasonDropDown() {
        List<OffReasonEntity> list = offReasonRepository.findAll();
        if (list.isEmpty())
            return new ArrayList<>();
        return list.stream().map(data -> new OffReasonDto(data.getId(), data.getName())).collect(Collectors.toList());
    }

    public List<OffReasonDto> getDetailedReasonDropDown(Long offreasonId) {
        List<DetailedReasonEntity> list = detailedReasonRepository.findByOffreason(offreasonId);
        if (list.isEmpty())
            return new ArrayList<>();
        return list.stream().map(data -> new OffReasonDto(data.getId(), data.getName())).collect(Collectors.toList());
    }

    public List<MachineDropDownDto> getCLusterAndItemMachineName(Long machineId) {
        MachineCluster machineCluster = machineClusterRepository.getByMachineId(machineId);
        List<MachineDropDownDto> list = new ArrayList<>();
        if (machineCluster != null) {
            list.add(
                    new MachineDropDownDto(machineCluster.getMachine().getId(), machineCluster.getMachine().getName()));
            for (MachineClusterItems clusterItems : machineCluster.getMachineClusterItems()) {
                if (clusterItems.getMachine() != null)
                    list.add(new MachineDropDownDto(clusterItems.getMachine().getId(),
                            clusterItems.getMachine().getName()));
            }
        }
        return list;
    }

    public List<OffReasonDto> getScheduleName(Long machineId) {
        List<Long> list = new ArrayList<>();
        list.add(machineId);
        MachineCluster machineCluster = machineClusterRepository.getByMachineId(machineId);
        if (machineCluster != null) {
            if (machineCluster.getMachineClusterItems() != null)
                for (MachineClusterItems clusterItems : machineCluster.getMachineClusterItems()) {
                    if (clusterItems.getMachine() != null)
                        list.add(clusterItems.getMachine().getId());
                }
        }
        List<PMSchedule> scheduleList = pmScheduleRepo.findByMachineIds(list);
        if (scheduleList != null && !scheduleList.isEmpty())
            return scheduleList.stream().map(data -> new OffReasonDto(data.getId(), data.getName()))
                    .collect(Collectors.toList());

        return new ArrayList<>();
    }

    // dwcluster

    public ResponseEntity<?> saveDwCluster(DwClusterDto dwClusterDto, User loggedUser) {
        DwCluster dwCluster = new DwCluster(dwClusterDto);
        dwClusterRepository.save(dwCluster);
        return new ResponseEntity<Status>(new Status("200", "DwCluster save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateDwCluster(DwClusterDto dwClusterDto, User loggedUser) {
        DwCluster dwCluster = new DwCluster(dwClusterDto);
        dwClusterRepository.save(dwCluster);
        return new ResponseEntity<Status>(new Status("200", "DwCluster updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> dwclusterAll(String name, Long branch, Long dept) throws UsernameNotFoundException {
        List<DwCluster> list = dwClusterRepository.findByList(name, branch, dept);
        if (!list.isEmpty()) {
            List<DwClusterDto> result = list.stream().map(data -> new DwClusterDto(data)).collect(Collectors.toList());
            return new ResponseEntity<List<DwClusterDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<DwClusterDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> listAllUnitType(Long specType, Long id) {
        if (specType == 28) {
            System.out.println("spectpe == " + specType);
            Optional<ProductMaster> optional = productMasterRepository.findById(id);
            if (optional.isPresent()) {
                System.out.println("ProductMaster == presented " + optional.get());
                Optional<Units> units = unitsRepository.findBy(optional.get().getUnit());
                if (units.isPresent()) {
                    System.out.println("units == units " + optional.get());
                    CommonDropDownDto result = new CommonDropDownDto(units.get().getId(), units.get().getName());
                    return new ResponseEntity<CommonDropDownDto>(result, HttpStatus.OK);
                } else {
                    return new ResponseEntity<CommonDropDownDto>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<CommonDropDownDto>(HttpStatus.NOT_FOUND);
            }
        } else if (specType == 30) {
            Optional<PurchaseExpense> optional = purchaseExpenseRepository.findById(id);
            if (optional.isPresent()) {
                Optional<Units> units = unitsRepository
                        .findById(Long.valueOf(optional.get().getAmc_Expense_unit().toString()));
                if (units.isPresent()) {
                    CommonDropDownDto result = new CommonDropDownDto(units.get().getId(), units.get().getName());
                    return new ResponseEntity<CommonDropDownDto>(result, HttpStatus.OK);
                } else {
                    return new ResponseEntity<CommonDropDownDto>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<CommonDropDownDto>(HttpStatus.NOT_FOUND);
            }

        } else if (specType == 31) {
            Optional<PurchaseExpense> optional = purchaseExpenseRepository.findById(id);
            if (optional.isPresent()) {
                Optional<Units> units = unitsRepository
                        .findById(Long.valueOf(optional.get().getAmc_Expense_unit().toString()));
                if (units.isPresent()) {
                    CommonDropDownDto result = new CommonDropDownDto(units.get().getId(), units.get().getName());
                    return new ResponseEntity<CommonDropDownDto>(result, HttpStatus.OK);
                } else {
                    return new ResponseEntity<CommonDropDownDto>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<CommonDropDownDto>(HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<CommonDropDownDto>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getBranchById(Long branchId) {
        List<bAddressList> branch = branchRepository.getBranchById(branchId);
        List<BranchAddressDto> list = branch.stream().map(data -> new BranchAddressDto(data))
                .collect(Collectors.toList());
        return new ResponseEntity<List<BranchAddressDto>>(list, HttpStatus.OK);

    }

}

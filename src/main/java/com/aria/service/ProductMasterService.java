package com.aria.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductMasterService {

    @Autowired
    ProductMasterRepository productMasterRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductMapCategoryRepository productMapCategoryRepo;

    @Autowired
    ProductMappingRepository productMappingRepo;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    RestService restService;

    @Autowired
    HOConfigRESTLogRepository restLogRepository;

    @Autowired
    LotRepository lotRepository;

    @Autowired
    ProductPieceCountRepository productPieceCountRepository;

    public ResponseEntity<?> saveProductMaster(ProductMasterDto productMasterDto, User logged)
            throws UsernameNotFoundException {
        ProductMaster productMaster = new ProductMaster(productMasterDto);
        productMaster.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        productMaster.setCreatedBy(logged.getId());
        ProductMaster result = productMasterRepository.save(productMaster);
        if (productMasterDto.getProductMappingCategory() != null
                && !productMasterDto.getProductMappingCategory().isEmpty()) {

            for (Long cat : productMasterDto.getProductMappingCategory()) {
                ProductMapping productMapping = new ProductMapping();
                productMapping.setProduct_category_ref_id(cat);
                productMapping.setProduct_ref_id(result);
                List<ProductMapping> productmappinglist = productMappingRepo.findidByCateIdandrefid(cat,
                        result.getId());
                if (productmappinglist != null && !productmappinglist.isEmpty()) {
                    productMapping.setProduct_mapping_id(productmappinglist.get(0).getProduct_mapping_id());
                }
                if (productMasterDto.getPieceQuantity() != null) {
                    if (cat.equals(1L) || cat.equals(3L)) {
                        ProductPieceQty res = new ProductPieceQty();
                        res.setProductId(result.getId());
                        res.setBoxQty(1L);
                        res.setPieceQty(Long.valueOf(result.getPieceQuantity()));
                        productPieceCountRepository.save(res);
                    }
                }
                productMappingRepo.save(productMapping);
            }
        }

        if (productMasterDto.getAddLotValues() != null) {
            List<String> lotList = new ArrayList<>(Arrays.asList(productMasterDto.getAddLotValues().split("\\,")));
            for (String lotvalue : lotList) {
                Optional<Lot> lotOptional = lotRepository.findByLotANDProductId(result.getId(),
                        Double.valueOf(lotvalue));
                if (!lotOptional.isPresent()) {
                    Lot lot = new Lot();
                    lot.setProductId(result.getId());
                    lot.setLot(Double.valueOf(lotvalue));
                    lot.setLotString(lotvalue);
                    lotRepository.save(lot);
                }
            }
        }
        updateHoOfficeConfig(result, "CREATE");
        return new ResponseEntity<Status>(new Status("200", "ProductMaster saved Successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateProductMaster(ProductMasterDto productMasterDto, User logged) throws UsernameNotFoundException {

        ProductMaster productMaster = new ProductMaster(productMasterDto);
        productMaster.setUpdatedBy(logged.getId());
        productMaster.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        ProductMaster result = productMasterRepository.save(productMaster);

        if (productMasterDto.getProductType() != null) {

            List<ProductMapping> productmappinglist = productMappingRepo.findidByrefid(productMasterDto.getId());
            if (productmappinglist != null && !productmappinglist.isEmpty()) {
                productMappingRepo.deleteAll(productmappinglist);
            }

            for (Long cat : productMasterDto.getProductMappingCategory()) {
                ProductMapping productMapping = new ProductMapping();
                productMapping.setProduct_category_ref_id(cat);
                productMapping.setProduct_ref_id(result);
                List<ProductMapping> mappingList = productMappingRepo.findidByCateIdandrefid(cat, result.getId());
                if (mappingList != null && !mappingList.isEmpty()) {
                    productMapping.setProduct_mapping_id(productmappinglist.get(0).getProduct_mapping_id());
                }
                if (productMasterDto.getPieceQuantity() != null) {
                    if (cat.equals(1L) || cat.equals(3L)) {
                        ProductPieceQty res = productPieceCountRepository.findByProductId(result.getId());
                        if (res != null) {
                            res.setProductId(result.getId());
                            res.setBoxQty(1L);
                            res.setPieceQty(Long.valueOf(result.getPieceQuantity()));
                        } else {
                            res = new ProductPieceQty();
                            res.setProductId(result.getId());
                            res.setBoxQty(1L);
                            res.setPieceQty(Long.valueOf(result.getPieceQuantity()));
                        }
                        productPieceCountRepository.save(res);
                    }
                }
                productMappingRepo.save(productMapping);
            }

        }

        if (productMasterDto.getAddLotValues() != null) {
            List<String> lotList = new ArrayList<>(Arrays.asList(productMasterDto.getAddLotValues().split("\\,")));
            for (String lotvalue : lotList) {
                Optional<Lot> lotOptional = lotRepository.findByLotANDProductId(result.getId(),
                        Double.valueOf(lotvalue));
                if (!lotOptional.isPresent()) {
                    Lot lot = new Lot();
                    lot.setProductId(result.getId());
                    lot.setLot(Double.valueOf(lotvalue));
                    lot.setLotString(lotvalue);
                    lotRepository.save(lot);
                }
            }
        }

        if (productMasterDto.getRemoveLotValues() != null && productMasterDto.getRemoveLotValues() != "") {
            System.out.println(productMasterDto.getRemoveLotValues());
            List<String> lotList = new ArrayList<>(Arrays.asList(productMasterDto.getRemoveLotValues().split("\\,")));
            for (String lotvalue : lotList) {
                List<Lot> list = lotRepository.findByLot(result.getId(), Double.valueOf(lotvalue));
                if (list != null && !list.isEmpty()) {
                    lotRepository.deleteAll(list);
                }
            }
        }

        updateHoOfficeConfig(result, "UPDATE");
        return new ResponseEntity<Status>(new Status("200", "ProductMaster updated Successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllProductMaster(String productname, String producttype, Pageable pageable)
            throws UsernameNotFoundException {

        Page<ProductList> userList = productMasterRepository.productFilterList(productname, producttype, pageable);
        if (!userList.toList().isEmpty()) {
            List<ProductListDto> result = userList.toList().stream()
                    .map(data -> new ProductListDto(data, fileStorageService)).collect(Collectors.toList());
            PageDataDto<ProductListDto> pageData = new PageDataDto<>(result, userList);
            return new ResponseEntity<PageDataDto<ProductListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ProductListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getProductMaster(String id) throws UsernameNotFoundException {
        Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(Long.parseLong(id));
        if (optionalProductMaster.isPresent()) {
            ProductMasterDto productMasterDto = new ProductMasterDto(optionalProductMaster.get());
            List<Long> productmappinglist = productMappingRepo.findBy(productMasterDto.getId());
            productMasterDto.setProductMappingCategory(productmappinglist);
            String lotValue = lotRepository.findBy(productMasterDto.getId());
            productMasterDto.setLotValues(lotValue);
            return new ResponseEntity<ProductMasterDto>(productMasterDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<ProductMasterDto>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getProductMasterList() throws UsernameNotFoundException {

        List<Long> productIdList = productMappingRepo.findidByCateId(2);

        List<ProductMaster> productMasters = productMasterRepository.findAllById(productIdList);

        List<ProductDropDownDto> productDropDownDtos = productMasters.stream().map(val -> new ProductDropDownDto(val))
                .collect(Collectors.toList());

        return new ResponseEntity<List<ProductDropDownDto>>(productDropDownDtos, HttpStatus.OK);

    }

    public ResponseEntity<?> getProductforBlending() throws UsernameNotFoundException {
        List<ProductMaster> productList = productMasterRepository.findBlendingEnabled();
        if (Objects.nonNull(productList) && productList.size() > 0) {
            return new ResponseEntity<List<ProductMaster>>(productList, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductMaster>>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> deleteProductMaster(String id) throws UsernameNotFoundException {
        try {
            Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(Long.parseLong(id));
            if (optionalProductMaster.isPresent()) {
                ProductMaster productMaster = optionalProductMaster.get();
                productMasterRepository.delete(productMaster);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> getDropdownList(String type, String typeString) throws UsernameNotFoundException {
        List<String> list = new ArrayList<>();
        if (type.equalsIgnoreCase("category")) {

            if (typeString != null) {
                list = productMasterRepository.findAllByCategory("%" + typeString + "%");
            } else {
                list = productMasterRepository.findAllByCategory();
            }

        } else if (type.equalsIgnoreCase("rawmaterial")) {

            if (typeString != null) {
                list = productMasterRepository.findAllByRawmaterial("%" + typeString + "%");
            } else {
                list = productMasterRepository.findAllByRawmaterial();
            }

        } else if (type.equalsIgnoreCase("productcategory")) {

            if (typeString != null) {
                list = productMasterRepository.findAllByProductCategory("%" + typeString + "%");
            } else {
                list = productMasterRepository.findAllByProductCategory();
            }

        } else if (type.equalsIgnoreCase("packingtype")) {

            if (typeString != null) {
                list = productMasterRepository.findAllByPackingType("%" + typeString + "%");
            } else {
                list = productMasterRepository.findAllByPackingType();
            }

        } else if (type.equalsIgnoreCase("unit")) {

            if (typeString != null) {
                list = productMasterRepository.findAllByUnit("%" + typeString + "%");
            } else {
                list = productMasterRepository.findAllByUnit();
            }

        } else if (type.equalsIgnoreCase("altunit")) {

            if (typeString != null) {
                list = productMasterRepository.findAllByAltUnit("%" + typeString + "%");
            } else {
                list = productMasterRepository.findAllByAltUnit();
            }

        } else if (type.equalsIgnoreCase("taskcategory")) {

            if (typeString != null) {
                list = productMasterRepository.findAllByTaskCategory("%" + typeString + "%");
            } else {
                list = productMasterRepository.findAllByTaskCategory();
            }

        }

        if (!list.isEmpty()) {
            return new ResponseEntity<List<String>>(list, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<String>>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<?> getAllSearchedProductd(String searchProduct) {
        if (searchProduct == null || searchProduct == "") {
            PageRequest pageable = PageRequest.of(0, 10);
            Page<ProductMaster> products = productMasterRepository.findAll(pageable);
            return new ResponseEntity<Object>(products.toList(), HttpStatus.OK);
        }
        List<ProductMaster> response = productMasterRepository.findByString(searchProduct);
        return new ResponseEntity<Object>(response, HttpStatus.OK);
    }

    public ResponseEntity<?> getProductByBranchAndDepartment(String searchProduct, Long branchId, Long departmentId) {
        List<ProductMaster> response = productMasterRepository.findByDepAndBranch(searchProduct, branchId,
                departmentId);
        List<ProductMasterDropDownDto> list = new ArrayList<>();
        ProductMasterDropDownDto productMasterDropDownDto = null;
        for (ProductMaster productMaster : response) {
            productMasterDropDownDto = new ProductMasterDropDownDto(productMaster.getId(),
                    productMaster.getProductName());
            list.add(productMasterDropDownDto);
        }
        return new ResponseEntity<Object>(list, HttpStatus.OK);
    }

    public ResponseEntity<?> saveProductMaster(HoProductDto ProductMasterDto, HttpServletRequest request)
            throws UsernameNotFoundException {
        try {
            ProductMaster productMaster = new ProductMaster(ProductMasterDto);
            productMaster.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            productMasterRepository.save(productMaster);
            return new ResponseEntity<HOStatus>(new HOStatus("200", "", "successfully created!!",
                    "Product Master created Successfully!!!", request.getServletPath()), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<HOStatus>(new HOStatus("400", e.getMessage(), "failure",
                    "Product Master Not created", request.getServletPath()), HttpStatus.OK);
        }

    }

    public ResponseEntity<?> listAllProductMasterlist(String productname, String pcategory)
            throws UsernameNotFoundException {

        List<ProductList> userList = productMasterRepository.productFilterList(productname, pcategory);
        if (!userList.isEmpty()) {
            List<ProductListDto> result = userList.stream().map(data -> new ProductListDto(data, fileStorageService))
                    .collect(Collectors.toList());
            return new ResponseEntity<List<ProductListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductListDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public void updateHoOfficeConfig(ProductMaster productMaster, String createType) {

        HoProductDto productDto = new HoProductDto(productMaster);
        String result = restService.saveItem(productDto);
        Optional<ProductMaster> optional = productMasterRepository.findById(productDto.getId());
        if (optional.isPresent()) {
            ProductMaster update = optional.get();
            update.setHoStatus(result);
            update.setSend_ft_to_ho_object(getFactoryData(productDto));
            productMasterRepository.save(update);
            updateItemLog("Item", createType, result, productDto);
        }
    }

    public String getFactoryData(HoProductDto hoProductDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoProductDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateRestLog(String type, String createType, Long typeId, String status, String response,
                              String jsonData) {

        HOConfigRESTLog hoConfigRESTLog = new HOConfigRESTLog();
        hoConfigRESTLog.setType(type);
        hoConfigRESTLog.setFrom_office("FT");
        hoConfigRESTLog.setTypeId(typeId);
        hoConfigRESTLog.setStatus(status);
        hoConfigRESTLog.setResponse(response);
        hoConfigRESTLog.setJson_data(jsonData);
        hoConfigRESTLog.setCreated_time(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        hoConfigRESTLog.setCreateType(createType);
        restLogRepository.save(hoConfigRESTLog);
    }

    public void updateItemLog(String type, String createType, String response, HoProductDto jsonData) {
        String status = response;
        Long typeId = jsonData.getId();
        String responseData = response;
        String jsonData1 = getFactoryData(jsonData);
        updateRestLog(type, createType, typeId, status, responseData, jsonData1);
    }

    public ResponseEntity<?> listAllProductMasterByCategory(String productName, String productType, Long categoryId,
                                                            Pageable pageable) {
        Page<ProductList> userList = productMasterRepository.productFilterListByCategory(productName, productType,
                categoryId, pageable);
        if (!userList.toList().isEmpty()) {
            List<ProductListDto> result = userList.toList().stream()
                    .map(data -> new ProductListDto(data, fileStorageService)).collect(Collectors.toList());
            PageDataDto<ProductListDto> pageData = new PageDataDto<>(result, userList);
            return new ResponseEntity<PageDataDto<ProductListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ProductListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getProductWithLotList(String productname) throws UsernameNotFoundException {

        List<ProductsWithLotList> products = productMasterRepository.findBY(productname);
        if (!products.isEmpty()) {
            List<ProductsWithLotDto> result = products.stream().map(data -> {
                ProductsWithLotDto prod = new ProductsWithLotDto(data);
                List<Lot> list = lotRepository.findByLot(prod.getProductId());
                prod.setLotList(list);
                return prod;
            }).collect(Collectors.toList());

            return new ResponseEntity<List<ProductsWithLotDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductsWithLotDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> findBYSplit(String productname) throws UsernameNotFoundException {

        List<ProductsWithLotList> products = productMasterRepository.findBYSplit(productname);
        if (!products.isEmpty()) {
            List<ProductsWithLotDto> result = products.stream().map(data -> {
                ProductsWithLotDto prod = new ProductsWithLotDto(data);
                return prod;
            }).collect(Collectors.toList());

            return new ResponseEntity<List<ProductsWithLotDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<ProductsWithLotDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> restrictLabelOrFlimProductMasterList(String productname, String producttype, Pageable pageable) throws UsernameNotFoundException {
        List<ProductList> userList = null;
        if (productname != null) {
            userList = productMasterRepository.restrictLabelOrFlimProductMasterListWithOutlimit(productname, producttype);
        } else {
            userList = productMasterRepository.restrictLabelOrFlimProductMasterListWithlimit();
        }
        if (!userList.isEmpty()) {
            List<ProductListDto> result = userList.stream().map(ProductListDto::new).collect(Collectors.toList());
            return new ResponseEntity<List<ProductListDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<ProductListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }
}

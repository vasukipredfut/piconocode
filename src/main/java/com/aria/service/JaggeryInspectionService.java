package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.utils.DateUtil;

@Service
public class JaggeryInspectionService {

    @Autowired
    JaggeryAttributesRepository jaggeryAttributesRepository;

    @Autowired
    BillInwardRepository billInwardRepository;

    @Autowired
    VendorRepository vendorRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    JaggeryInspectionRepository jaggeryInspectionRepository;

    @Autowired
    JaggeryAttributesValuesRepository jaggeryAttributesValuesRepository;

    @Autowired
    JaggeryInspectionScoreRepository jaggeryInspectionScoreRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    BillInwardItemReceivedRepository billInwardItemReceivedRepository;


    @Autowired
    RestService restService;

    @Autowired
    HOConfigRESTLogRepository restLogRepository;

    public ResponseEntity<?> getJaggeryAttributes(JaggeryInspectionSubmitReqDto submitReqDto) throws UsernameNotFoundException {

        Date billDate = null;
        if (submitReqDto.getBillDate() != null) {
            billDate = dateUtil.getStringTODateWk(submitReqDto.getBillDate());
        }

        Optional<JaggeryInspection> jaggeryInspection = jaggeryInspectionRepository.findBy(submitReqDto.getVendorId(), submitReqDto.getBillNo(), billDate);

        if (jaggeryInspection.isPresent()) {
            return new ResponseEntity<>("Jaggery Inspection with the same details already exists", HttpStatus.CONFLICT);
        }

        JaggeryInspectionSubmitRes jaggeryInspectionSubmitRes = billInwardRepository.findBy(submitReqDto.getVendorId(), submitReqDto.getBillNo(), billDate);

        JaggeryInspectionSubmitResDto result = new JaggeryInspectionSubmitResDto(jaggeryInspectionSubmitRes);

        List<String> lotlist = billInwardItemReceivedRepository.findByLots(result.getBillInwardId());
        if(!lotlist.isEmpty()){
            result.setLots(lotlist);
        }

        result.setBillDate(submitReqDto.getBillDate());
        result.setJagDate(submitReqDto.getJagDate());
        result.setUnitRate(jaggeryInspectionSubmitRes.getUnitRate());
        List<JaggeryAttributes> list = jaggeryAttributesRepository.findAll();
        result.setAttributesDtoList(list.stream().map(data -> new JaggeryAttributesDto(data)).collect(Collectors.toList()));

        return new ResponseEntity<JaggeryInspectionSubmitResDto>(result, HttpStatus.OK);

    }

    public ResponseEntity<?> saveJaggeryInspection(JaggeryInspectionReqDto reqDto, User loggedUser)
            throws UsernameNotFoundException {
        JaggeryInspection result = null;
        String uniqno = "JAGGINSP-";
        if (reqDto.getInspection() != null) {
            JaggeryInspection jaggeryInspection = new JaggeryInspection(reqDto.getInspection());
            Optional<BillInward> billInwardOptional = billInwardRepository.findById(reqDto.getBillInwardId());
            if (billInwardOptional.isPresent()) {
                jaggeryInspection.setBilltrnno("BILLS-" + billInwardOptional.get().getId());
                jaggeryInspection.setBranch(loggedUser.getBranch_id());
                jaggeryInspection.setDept(loggedUser.getDepartment().getId());
                jaggeryInspection.setInwardat(billInwardOptional.get().getUpdatedBy());
                jaggeryInspection.setPrepareby(billInwardOptional.get().getUpdatedBy());
                jaggeryInspection.setPreparedate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            }
            //jaggeryInspection.setStatus("Completed");
            result = jaggeryInspectionRepository.save(jaggeryInspection);

        }
        uniqno = uniqno + result.getId();
        result.setUniqno(uniqno);
        if (reqDto.getAttributesValues() != null) {
            JaggeryAttributesValues jaggeryAttributesValues = new JaggeryAttributesValues(reqDto.getAttributesValues());
            jaggeryAttributesValues.setUniqno(uniqno);
            jaggeryAttributesValuesRepository.save(jaggeryAttributesValues);
        }
        if (reqDto.getInspectionScore() != null) {
            JaggeryInspectionScore jaggeryInspectionScore = new JaggeryInspectionScore(reqDto.getInspectionScore());
            jaggeryInspectionScore.setUniqno(uniqno);
            jaggeryInspectionScoreRepository.save(jaggeryInspectionScore);
        }
        jaggeryInspectionRepository.save(result);
        return new ResponseEntity<Status>(new Status("200", "JaggeryInspection Created Successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> findAll(Long vendorId, String vendorName, String billNo, String billDate, String jagDate, String status , String trnNo ,
                                     Pageable pageable) throws UsernameNotFoundException {

        Date coBillDate = null;
        Date coJagDate = null;

        if (billDate != null) {
            coBillDate = dateUtil.getStringTODateWk(billDate);
        }

        if (jagDate != null) {
            coJagDate = dateUtil.getStringTODateWk(jagDate);
        }
        Page<JaggeryInspectionList> list = jaggeryInspectionRepository.findBy(vendorId, vendorName, billNo, coBillDate, coJagDate, status , trnNo , pageable);
        if (!list.isEmpty()) {
            List<JaggeryInspectionListDto> result = list.stream().map(data -> {
                JaggeryInspectionListDto inspectionListDto = new JaggeryInspectionListDto(data);
                return inspectionListDto;
            }).collect(Collectors.toList());
            PageDataDto<JaggeryInspectionListDto> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<JaggeryInspectionListDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<JaggeryInspectionListDto>>(new PageDataDto(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> findBy(Long id) throws UsernameNotFoundException {
        JaggeryInspectionReqDto result = null;
        Optional<JaggeryInspection> jaggeryInspection = jaggeryInspectionRepository.findById(id);
        if (jaggeryInspection.isPresent()) {
            String uniqNo = jaggeryInspection.get().getUniqno();
            result = new JaggeryInspectionReqDto();
            Long vendorId = jaggeryInspection.get().getParty();
            Long branchId = jaggeryInspection.get().getBranch();
            Optional<Vendor> vendor = vendorRepository.findById(vendorId);
            Optional<Branch> branch = branchRepository.findById(branchId);
            List<String> billSplit = new ArrayList<>(Arrays.asList(jaggeryInspection.get().getBilltrnno().split("\\-")));

            JaggeryInspectionDto jaggeryInspectionDto = new JaggeryInspectionDto(jaggeryInspection.get());

            if (jaggeryInspectionDto.getPrepareby() != null) {
                Optional<User> user = userRepository.findById(jaggeryInspectionDto.getPrepareby());
                if (user.isPresent()) {
                    jaggeryInspectionDto.setPreparebyName(user.get().getEmployeename());
                }
            }

			if (jaggeryInspectionDto.getAuby() != null) {
				Optional<User> user = userRepository.findById(jaggeryInspectionDto.getAuby());
				if (user.isPresent()) {
					jaggeryInspectionDto.setAuthByName(user.get().getEmployeename());
				}
			}
			if (jaggeryInspectionDto.getAby() != null) {
				Optional<User> appUser = userRepository.findById(jaggeryInspectionDto.getAby());
				if (appUser.isPresent()) {
					jaggeryInspectionDto.setApproveByName(appUser.get().getEmployeename());
				}
			}
			if (jaggeryInspectionDto.getVby() != null) {
				Optional<User> verifyUser = userRepository.findById(jaggeryInspectionDto.getVby());
				if (verifyUser.isPresent()) {
					jaggeryInspectionDto.setVerifyByName(verifyUser.get().getEmployeename());
				}

			}
            List<String> lotlist = billInwardItemReceivedRepository.findByLots(Long.valueOf(billSplit.get(1)));
            if(!lotlist.isEmpty()){
                jaggeryInspectionDto.setLots(lotlist);
            }

            result.setInspection(jaggeryInspectionDto);
            result.getInspection().setVendorName(vendor.isPresent() ? vendor.get().getName() : null);
            result.getInspection().setVendorPlace(vendor.isPresent() ? vendor.get().getCamp() : null);
            result.getInspection().setBranchName(branch.isPresent() ? branch.get().getBranchName() : null);
            Optional<JaggeryAttributesValues> attributesValues = jaggeryAttributesValuesRepository.findBy(uniqNo);
            if (attributesValues.isPresent()) {
                result.setAttributesValues(new JaggeryAttributesValuesDto(attributesValues.get()));
            }
            Optional<JaggeryInspectionScore> jaggeryInspectionScore = jaggeryInspectionScoreRepository.findBy(uniqNo);
            if (jaggeryInspectionScore.isPresent()) {
                result.setInspectionScore(new JaggeryInspectionScoreDto(jaggeryInspectionScore.get()));
            }
        }
        return new ResponseEntity<JaggeryInspectionReqDto>(result, HttpStatus.OK);
    }

    public ResponseEntity<?> approveAndRejectJaggeryInspection(Long id, String action, User loggedUser) {
        Optional<JaggeryInspection> optionalRequest = jaggeryInspectionRepository.findById(id);
        if (optionalRequest.isPresent()) {
            JaggeryInspection jaggerInspection = optionalRequest.get();
            if ("verify".equalsIgnoreCase(action)) {
                jaggerInspection.setStatus("VERIFIED");
                jaggerInspection.setVby(loggedUser.getId());
                jaggerInspection.setVdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                jaggerInspection.setVerify("Y");
            } else if ("approve".equalsIgnoreCase(action)) {
                jaggerInspection.setStatus("APPROVED");
                jaggerInspection.setAby(loggedUser.getId());
                jaggerInspection.setAdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                jaggerInspection.setApprove("Y");
            } else if ("reject".equalsIgnoreCase(action)) {
                jaggerInspection.setStatus("REJECTED");
                jaggerInspection.setRejectedBy(loggedUser.getId());
                jaggerInspection.setRejectdate(dateUtil.getISTDate());
            } else if ("authorise".equalsIgnoreCase(action)) {
                jaggerInspection.setStatus("AUTHORISED");
                jaggerInspection.setAuby(loggedUser.getId());
                jaggerInspection.setAuthenty("A");
                jaggerInspection.setAudate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            } else {
                throw new IllegalArgumentException("Invalid action: " + action);
            }
			jaggerInspection.setUpdatedBy(loggedUser.getId());
			jaggerInspection.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            JaggeryInspection result = jaggeryInspectionRepository.save(jaggerInspection);

            if(action.equalsIgnoreCase("authorise")){

                HoInwardInspectionDto hoInwardInspectionDto = getHoInwardInspectionData(result);
                hoInwardInspectionDto.setTRANSFER_DATE(dateUtil.getDateTOStringWk(dateUtil.getISTDate()));
                hoInwardInspectionDto.setTRANSFER_BY(loggedUser.getEmployeename());
                RestResponse response = restService.saveHoInwardInspection(hoInwardInspectionDto);
                String responseData = getHoData(response);
                updateLog("InwardInspection", responseData, hoInwardInspectionDto);
            }
            return new ResponseEntity<>(new Status("200", "Jaggery inspection updated successfully!"), HttpStatus.OK);
        } else {
            throw new NoSuchElementException("Jaggery inspection not found with ID: " + id);
        }
    }

    public ResponseEntity<?> updateJaggeryInspectionStatus(JaggeryInspectionReqDto jaggeryInspectionReqDto,
                                                           Authentication authentication, User loggedUser) {
        JaggeryInspection result = null;
        if (jaggeryInspectionReqDto.getInspection() != null) {
            Optional<JaggeryInspection> existjaggeryInspection = jaggeryInspectionRepository.findById(jaggeryInspectionReqDto.getInspection().getId());
            if (existjaggeryInspection.isPresent()) {
                JaggeryInspection jaggeryInspection = new JaggeryInspection(jaggeryInspectionReqDto.getInspection());
                jaggeryInspection.setUniqno(existjaggeryInspection.get().getUniqno());
                jaggeryInspection.setId(existjaggeryInspection.get().getId());
                jaggeryInspection.setUpdatedBy(loggedUser.getId());
                jaggeryInspection.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                result = jaggeryInspectionRepository.save(jaggeryInspection);
            } else {
                return new ResponseEntity<Status>(new Status("400", "JaggeryInspection not Updated "), HttpStatus.OK);
            }
        }
        if (jaggeryInspectionReqDto.getAttributesValues() != null) {
            Optional<JaggeryAttributesValues> existingAttributesValues = jaggeryAttributesValuesRepository.findByIdAndUniqno(jaggeryInspectionReqDto.getAttributesValues().getId(), result.getUniqno());
            if (existingAttributesValues.isPresent()) {
                JaggeryAttributesValues updatedAttributesValues = new JaggeryAttributesValues(jaggeryInspectionReqDto.getAttributesValues());
                updatedAttributesValues.setId(existingAttributesValues.get().getId());
                updatedAttributesValues.setUniqno(existingAttributesValues.get().getUniqno());
                jaggeryAttributesValuesRepository.save(updatedAttributesValues);
            }
        }
        if (jaggeryInspectionReqDto.getInspectionScore() != null) {
            Optional<JaggeryInspectionScore> existingInspectionScore = jaggeryInspectionScoreRepository.findByIdAndUniqno(jaggeryInspectionReqDto.getInspectionScore().getId(), result.getUniqno());
            if (existingInspectionScore.isPresent()) {
                JaggeryInspectionScore updatedInspectionScore = new JaggeryInspectionScore(jaggeryInspectionReqDto.getInspectionScore());
                updatedInspectionScore.setId(existingInspectionScore.get().getId());
                updatedInspectionScore.setUniqno(existingInspectionScore.get().getUniqno());
                jaggeryInspectionScoreRepository.save(updatedInspectionScore);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "JaggeryInspection Updated Successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> getAttributesList(JaggeryInspectionSubmitReqDto submitReqDto)
            throws UsernameNotFoundException {
        JaggeryInspectionSubmitResDto result = new JaggeryInspectionSubmitResDto();
        Date billDate = null;
        if (submitReqDto.getBillDate() != null) {
            billDate = dateUtil.getStringTODateWk(submitReqDto.getBillDate());
        }
        List<JaggeryAttributes> list = jaggeryAttributesRepository.findAll();
        result.setAttributesDtoList(
                list.stream().map(data -> new JaggeryAttributesDto(data)).collect(Collectors.toList()));
        return new ResponseEntity<JaggeryInspectionSubmitResDto>(result, HttpStatus.OK);
    }



    public HoInwardInspectionDto getHoInwardInspectionData(JaggeryInspection jaggeryInspection) {

        HoInwardInspectionDto hoInwardInspectionDto = new HoInwardInspectionDto();

        List<String> billsplit = new ArrayList<>(Arrays.asList(jaggeryInspection.getBilltrnno().split("\\-")));

        Optional<BillInward> billInward = billInwardRepository.findById(Long.valueOf(billsplit.get(1)));

        if (billInward.isPresent()) {

            hoInwardInspectionDto.setBILL_NO(billInward.get().getBillNo());
            hoInwardInspectionDto.setBILL_DATE(dateUtil.getDateTOStringWk(billInward.get().getBillDate()));
            hoInwardInspectionDto.setLORRY_DATE(dateUtil.getDateTOStringWk(billInward.get().getReceivedDate()));
            hoInwardInspectionDto.setLORRY_NO(billInward.get().getLorrySerialNo());
            hoInwardInspectionDto.setBILL_ID(billInward.get().getHoBillId());
            BillInwardItemReceived billInwardItemReceived = billInward.get().getItemReceived().get(0);
            Double qty = billInwardItemReceivedRepository.sumOfReceivedQty(billInward.get().getId(), billInwardItemReceived.getItem_id());
            String receivedQty = qty + " " + billInwardItemReceived.getLot() + "  (" + billInwardItemReceived.getConv_package_quantity_value() + " Kg ) ";

            hoInwardInspectionDto.setRECEIVED_QTY(receivedQty);
        }

        Optional<JaggeryInspectionScore> jaggeryInspectionScore = jaggeryInspectionScoreRepository.findBy(jaggeryInspection.getUniqno());
        if(jaggeryInspectionScore.isPresent()){
            hoInwardInspectionDto.setSCORE_OUTOFF_HUNDRED(jaggeryInspectionScore.get().getScore());
            if(jaggeryInspectionScore.get().getMoistavg() != null) {
                hoInwardInspectionDto.setMOISTURE(jaggeryInspectionScore.get().getMoistavg().toString());
            }
        }

        return hoInwardInspectionDto;

    }


    public void updateLog(String type, String response, HoInwardInspectionDto jsonData) {
        String status = response;
        Long typeId = jsonData.getBILL_ID();
        String responseData = response;
        String jsonData1 = getHoData(jsonData);
        updateRestLog(type, typeId, status, responseData, jsonData1);
    }

    public void updateRestLog(String type, Long typeId, String status, String response, String jsonData) {

        HOConfigRESTLog hoConfigRESTLog = new HOConfigRESTLog();
        hoConfigRESTLog.setType(type);
        hoConfigRESTLog.setFrom_office("FT");
        hoConfigRESTLog.setTypeId(typeId);
        hoConfigRESTLog.setStatus(status);
        hoConfigRESTLog.setResponse(response);
        hoConfigRESTLog.setJson_data(jsonData);
        hoConfigRESTLog.setCreated_time(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        restLogRepository.save(hoConfigRESTLog);
    }

    public String getHoData(HoInwardInspectionDto hoInwardInspectionDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoInwardInspectionDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHoData(RestResponse hoInwardInspectionDto) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.convertValue(hoInwardInspectionDto, new TypeReference<Map<String, Object>>() {
            });
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}

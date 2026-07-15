package com.aria.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.HtChargesRepository;
import com.aria.dao.HtDayTrnRepository;
import com.aria.dao.HtatResultsRepository;
import com.aria.dao.HtatmRepository;
import com.aria.dao.HtmasterRepository;
import com.aria.dao.HtmsummaryRepository;
import com.aria.dao.HttfgrpRepository;
import com.aria.dao.HttslapRepository;
import com.aria.dto.EBReadingDetailsDto;
import com.aria.dto.EBTableDetailsDto;
import com.aria.dto.HtDayTrnViewDto;
import com.aria.dto.HtDayTrnViewProjection;
import com.aria.dto.HtaChargesDto;
import com.aria.dto.HttfgrpDto;
import com.aria.dto.HttslapDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.ServiceNameDtoProjection;
import com.aria.dto.ServiceTimingProjection;
import com.aria.dto.ServicenameDto;
import com.aria.dto.WindmillWorkingDayDto;
import com.aria.model.HTMasters;
import com.aria.model.HtDayTrn;
import com.aria.model.HtaCharges;
import com.aria.model.HtatResults;
import com.aria.model.Htatm;
import com.aria.model.Httfgrp;
import com.aria.model.Httslap;
import com.aria.model.User;

@Service
public class EBService {

	@Autowired
	HttfgrpRepository httfgrpRepo;

	@Autowired
	HtChargesRepository htaChargesRepository;

	@Autowired
	HttslapRepository httslapRepo;

	@Autowired
	HtatmRepository htatmRepo;
	
	@Autowired
	HtatResultsRepository htatResultRepo;
	
	@Autowired
	HtmsummaryRepository htmsummaryRepo;
	
	@Autowired
	HtmasterRepository htmasterRepo;

	@Autowired
	HtDayTrnRepository htDayTrnRepo;

	public ResponseEntity<?> getListOfEbService(String shiftdate, Long serviceid, Long timmingid, Long month, Long year,String status, Pageable pageable) {
		
//		LocalDate readingdate= null;
//		if (shiftdate != null && !shiftdate.isEmpty()) {
//			readingdate = LocalDate.parse(shiftdate);
//		}
//		LocalDate createdate = null;
//		if (createddate != null && !createddate.isEmpty()) {
//			createdate = LocalDate.parse(createddate);
//		}
		 if ("created".equalsIgnoreCase(status)) {
		        status = "C";
		    }
		Page<HtDayTrnViewProjection> row = htDayTrnRepo.findListUsingFilters(shiftdate, serviceid, timmingid, month,
				year,status, pageable);
		if (!row.isEmpty()) {
			List<HtDayTrnViewDto> list = row.toList().stream().map(data -> {
				HtDayTrnViewDto dto = new HtDayTrnViewDto(data);
				 if ("C".equalsIgnoreCase(data.getStatus())) {
		                dto.setStatus("created");
		            }
				 long epochSeconds = Long.parseLong(data.getCreateddate());
				         LocalDate date = Instant.ofEpochSecond(epochSeconds)
				         .atZone(ZoneId.systemDefault())
				         .toLocalDate();
				         dto.setCreateddate(date);
				return dto;
			}).collect(Collectors.toList());
			PageDataDto<HtDayTrnViewDto> pageData = new PageDataDto<>(list, row);
			return new ResponseEntity<PageDataDto<HtDayTrnViewDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<HtDayTrnViewDto>>(new PageDataDto(), HttpStatus.OK);
		}
	}

	private String normalize(String name) {
		if (name == null)
			return "";
		return name.toUpperCase().replace("_", "").replace(" ", "").trim();
	}

	public ResponseEntity<?> getTableDetails(Long serviceid, Long timmingid, String shiftdate,Long month,Long year) {

		List<Htatm> atmList = htatmRepo.findList();
		List<HtDayTrn> trnList = htDayTrnRepo.findListdetails(shiftdate, serviceid, timmingid);

		List<EBReadingDetailsDto> readings = new ArrayList<>();
		
		   int m=month.intValue();
			int y=year.intValue();
			LocalDate lastDay = LocalDate.of(y, m, 1)
	                .withDayOfMonth(LocalDate.of(y, m, 1).lengthOfMonth());

// 1️⃣ From master (htatm)
		for (Htatm atm : atmList) {

			String atmKey = normalize(atm.getName());

			HtDayTrn matched = trnList.stream().filter(t -> normalize(t.getAtname()).equals(atmKey)).findFirst()
					.orElse(null);

			EBReadingDetailsDto dto = new EBReadingDetailsDto();
			dto.setAttributename(atm.getName());
			dto.setOrderid(atm.getEord());
			dto.setAttributeid(matched != null ? matched.getAtid() : atm.getId());

			if (matched != null) {
				dto.setOpening(matched.getMopening());
				dto.setClosing(matched.getMclosing());
			} else {
				dto.setOpening(0.0);
				dto.setClosing(0.0);
			}

			readings.add(dto);
		}

// 2️⃣ Add EXTRA attributes (only in htdaytrn)
		for (HtDayTrn trn : trnList) {

			String trnKey = normalize(trn.getAtname());

			boolean exists = atmList.stream().anyMatch(a -> normalize(a.getName()).equals(trnKey));

			if (!exists) {
				EBReadingDetailsDto dto = new EBReadingDetailsDto();
				dto.setAttributeid(trn.getAtid());
				dto.setAttributename(trn.getAtname());
				dto.setOpening(trn.getMopening());
				dto.setClosing(trn.getMclosing());
				readings.add(dto);
			}
		}

		HtDayTrn tn = trnList.get(0);
		ServiceTimingProjection ids = htDayTrnRepo.findServices(serviceid, timmingid);
		EBTableDetailsDto response = new EBTableDetailsDto();
		response.setReadingdate(tn.getShiftdate());
		response.setTodate(lastDay);
		response.setServiceid(ids.getServiceid());
		response.setServicename(ids.getServicename());
		response.setTimmingid(ids.getTimmingid());
		response.setTimmingSlap(ids.getTimeslap());
		response.setReadings(readings);

		return ResponseEntity.ok(response);
	}

//	public ResponseEntity<?> getTableDetail(Long serviceid, Long timmingid, String shiftdate) {
//
//// master attributes (common names)
//		List<Htatm> atmList = htatmRepo.findList();
//
//// service-specific transactions
//		List<HtDayTrn> trnList = htDayTrnRepo.findListdetails(shiftdate, serviceid, timmingid);
//
//		List<EBReadingDetailsDto> readings = new ArrayList<>();
//
//// 1️⃣ Build from master (by NAME)
//		for (Htatm atm : atmList) {
//
//			HtDayTrn matched = trnList.stream()
//					.filter(t -> t.getAtname() != null && t.getAtname().equalsIgnoreCase(atm.getName())).findFirst()
//					.orElse(null);
//
//			EBReadingDetailsDto dto = new EBReadingDetailsDto();
//			dto.setAttributeid(matched != null ? matched.getAtid() : atm.getId());
//			dto.setAttributename(atm.getName());
//
//			if (matched != null) {
//				dto.setOpening(matched.getMopening());
//				dto.setClosing(matched.getMclosing());
//			} else {
//				dto.setOpening(0.0);
//				dto.setClosing(0.0);
//			}
//
//			readings.add(dto);
//		}
//
//// 2️⃣ Add extra attributes present only in htdaytrn
//		for (HtDayTrn trn : trnList) {
//
//			boolean existsInMaster = atmList.stream().anyMatch(a -> a.getName().equalsIgnoreCase(trn.getAtname()));
//
//			if (!existsInMaster) {
//				EBReadingDetailsDto dto = new EBReadingDetailsDto();
//				dto.setAttributeid(trn.getAtid());
//				dto.setAttributename(trn.getAtname());
//				dto.setOpening(trn.getMopening());
//				dto.setClosing(trn.getMclosing());
//				readings.add(dto);
//			}
//		}
//
//// 3️⃣ Header
//		HtDayTrn tn = trnList.isEmpty() ? null : trnList.get(0);
//
//		EBTableDetailsDto response = new EBTableDetailsDto();
//		response.setTimmingSlap("6am-10pm");
//		response.setReadingdate(tn != null ? tn.getShiftdate() : null);
//		response.setReadings(readings);
//
//		return ResponseEntity.ok(response);
//	}
//	
	
	@Transactional
	public ResponseEntity<?> saveEBTableDetails(EBTableDetailsDto dto, User loggeduser) {

	    Map<String, Object> response = new HashMap<>();

	    try {
	        // ================= VALIDATE SERVICE =================
	        Optional<HTMasters> htm = htmasterRepo.findById(dto.getServiceid());
	        if (!htm.isPresent()) {
	            response.put("status", "FAILED");
	            response.put("message", "Service not found");
	            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	        }

	        HTMasters htMasters = htm.get();

	        // ================= FETCH DAY TRANSACTIONS =================
	        String shiftdate = dto.getReadingdate().toString();

	        List<HtDayTrn> trn = htDayTrnRepo.findListdetails(
	                shiftdate,
	                dto.getServiceid(),
	                dto.getTimmingid()
	        );

	        // ================= BUILD LOOKUP MAP =================
	        Map<String, HtDayTrn> dayTrnMap = trn.stream()
	                .collect(Collectors.toMap(
	                        d -> d.getAtname().replace("_", " ").toUpperCase(),
	                        d -> d
	                ));

	        long epochSeconds = LocalDateTime.now()
	                .atZone(ZoneId.systemDefault())
	                .toEpochSecond();

	        // ================= PROCESS READINGS =================
	        for (EBReadingDetailsDto reading : dto.getReadings()) {

	            /* ---------- VALIDATION ---------- */
	            if (reading.getOpening() != null && reading.getClosing() != null
	                    && reading.getClosing().compareTo(reading.getOpening()) < 0) {
	                throw new RuntimeException(
	                        "Closing less than opening for " + reading.getAttributename()
	                );
	            }

	            /* ---------- SAVE HTATRESULTS ---------- */
	            HtatResults result = new HtatResults();

	            result.setShiftdate(dto.getReadingdate());
	            result.setName(htMasters.getName());
	            result.setPanelid(htMasters.getPanelid());
	            result.setServid(dto.getServiceid());
	            result.setTsid(dto.getTimmingid());
	            result.setMonth(dto.getMonth() != null ? dto.getMonth().longValue() : null);
	            result.setYear(dto.getYear() != null ? dto.getYear().intValue() : null);

	            result.setAtid(reading.getAttributeid());
	            result.setAtname(reading.getAttributename());
	            result.setAtresults(
	                    reading.getClosing() != null
	                            ? reading.getClosing().toString()
	                            : "0"
	            );

	            result.setCby(loggeduser.getId());
	            result.setCon(String.valueOf(epochSeconds));

	            htatResultRepo.save(result);

	            /* ---------- UPDATE HTDAYTRN ---------- */
	            String key = reading.getAttributename().toUpperCase();
	            HtDayTrn dayTrn = dayTrnMap.get(key);

	            if (dayTrn != null) {
	                dayTrn.setMopening(reading.getOpening());
	                dayTrn.setMclosing(reading.getClosing());
	                dayTrn.setCunits(reading.getClosing()-reading.getOpening());
	                dayTrn.setUpdatedon(LocalDateTime.now());
	            }

	        }

	        // ================= BATCH SAVE =================
	        htDayTrnRepo.saveAll(trn);

	        response.put("status", "SUCCESS");
	        response.put("message", "EB Table details saved and updated successfully");
	        return new ResponseEntity<>(response, HttpStatus.CREATED);

	    } catch (Exception ex) {
	        response.put("status", "FAILED");
	        response.put("message", ex.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	
	@Transactional
	public ResponseEntity<?> saveEBTableDetail(EBTableDetailsDto dto, User loggeduser) {

	    Map<String, Object> response = new HashMap<>();
	    
	    List<HtDayTrn> httrn = new ArrayList<>();
	    
	    List<HtatResults> htResult = new ArrayList<>();
	    
	    int m= dto.getMonth().intValue();
		int y=dto.getYear().intValue();
		LocalDate lastDay = LocalDate.of(y, m, 1)
                .withDayOfMonth(LocalDate.of(y, m, 1).lengthOfMonth());

	    try {
	        // ================= VALIDATE SERVICE =================
	        HTMasters htMasters = htmasterRepo.findById(dto.getServiceid())
	                .orElseThrow(() -> new RuntimeException("Service not found"));

	        // ================= FETCH DAY TRANSACTIONS =================
	        String shiftdate = dto.getReadingdate().toString();

	        List<HtDayTrn> trn = htDayTrnRepo.findListdetails(
	                shiftdate,
	                dto.getServiceid(),
	                dto.getTimmingid()
	        );

	        // ================= BUILD LOOKUP MAP (USING ATID) =================
//	        Map<Long, HtDayTrn> dayTrnMap = trn.stream()
//	                .collect(Collectors.toMap(
//	                        HtDayTrn::getAtid,   // 🔑 KEY = ATTRIBUTE ID
//	                        d -> d
//	                ));
	        Map<String, HtDayTrn> dayTrnMap = trn.stream()
	                .collect(Collectors.toMap(
	                        d -> d.getAtname().replace("_", " ").toUpperCase(),
	                        d -> d
	                ));

	        long epochSeconds = LocalDateTime.now()
	                .atZone(ZoneId.systemDefault())
	                .toEpochSecond();

	        // ================= PROCESS READINGS =================
	        for (EBReadingDetailsDto reading : dto.getReadings()) {

	            /* ---------- VALIDATION ---------- */
	            if (reading.getOpening() != null && reading.getClosing() != null
	                    && reading.getClosing().compareTo(reading.getOpening()) < 0) {
	                throw new RuntimeException(
	                        "Closing less than opening for attribute ID " + reading.getAttributeid()
	                );
	            }

//	            Long atid = reading.getAttributeid();
	            Long originalAtid = reading.getAttributeid();
	            Long atid = originalAtid;

	            /* ================= SERVICE ID = 2 SPECIAL LOGIC ================= */
	            if (dto.getServiceid() == 2) {
	                if ("TEMP_DEMAND".equalsIgnoreCase(reading.getAttributename())) {
	                    atid = reading.getAttributeid();
	                }
	                else if ("C1 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C2 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C3 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C4 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C5 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("COMMERCIAL".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("TEMPERAVORY".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }
	                else {
	                    atid = originalAtid + 38;
	                }
	            }
	            else if(dto.getServiceid() == 3) {
	            	if ("TEMP_DEMAND".equalsIgnoreCase(reading.getAttributename())) {
	                    atid = reading.getAttributeid();
	                }
	                else if ("C1 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C2 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C3 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C4 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C5 KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("COMMERCIAL".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("TEMPERAVORY".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }else if ("C KWH".equalsIgnoreCase(reading.getAttributename())) {
	                	  atid=reading.getAttributeid();
	                }
	                else {
	                    atid = originalAtid + 76;
	                }
	            }

	            /* ---------- SAVE HTATRESULTS ---------- */
	            HtatResults result = new HtatResults();

	            result.setShiftdate(lastDay);
	            result.setName(htMasters.getName());
	            result.setPanelid(htMasters.getPanelid());
	            result.setServid(dto.getServiceid());
	            result.setTsid(dto.getTimmingid());
	            result.setMonth(dto.getMonth() != null ? dto.getMonth().longValue() : null);
	            result.setYear(dto.getYear() != null ? dto.getYear().intValue() : null);

	            result.setAtid(atid);                              // ✅ ID
	            result.setAtname(reading.getAttributename());     // optional (display only)
	            result.setAtresults(
	                    reading.getClosing() != null
	                            ? reading.getClosing().toString()
	                            : "0"
	            );

	            result.setCby(loggeduser.getId());
	            result.setCon(String.valueOf(epochSeconds));
	            
	            htResult.add(result);

//	            htatResultRepo.save(result);

	            /* ---------- UPDATE HTDAYTRN (USING ATID) ---------- */
//	            HtDayTrn dayTrn = dayTrnMap.get(atid);
	            String key=null;
	            if("TEMP_DEMAND".equalsIgnoreCase(reading.getAttributename())) {
	            key = "TEMP DEMAND";
	            }else {
	            	key = reading.getAttributename().toUpperCase();
	            }
	            HtDayTrn dayTrn = dayTrnMap.get(key);

	            if (dayTrn != null) {
	            dayTrn.setMopening(reading.getOpening());
	            dayTrn.setMclosing(reading.getClosing());
	            dayTrn.setCunits(reading.getClosing() - reading.getOpening());
	            dayTrn.setUpdatedon(LocalDateTime.now());
//	            dayTrn.setEntposition("");
	        
	            HtDayTrn HtTrn = new HtDayTrn();
	            HtTrn.setShiftdate(lastDay);
	            HtTrn.setServid(dto.getServiceid());
	            HtTrn.setTsid(dto.getTimmingid());
	            HtTrn.setBid(loggeduser.getBranch_id());
	            HtTrn.setName(dto.getServicename());
	            HtTrn.setPanelid(htMasters.getPanelid());
	            HtTrn.setAtid(reading.getAttributeid());
	            HtTrn.setAtname(reading.getAttributename());
	            HtTrn.setMopening(reading.getClosing());
	            HtTrn.setMonth(dto.getMonth().intValue());
	            HtTrn.setYear(dto.getYear().intValue());
	            HtTrn.setUsecid(loggeduser.getId()+"_"+String.valueOf(epochSeconds));
	            HtTrn.setEntposition("L");
	            HtTrn.setCby(loggeduser.getId());
	            HtTrn.setCon(String.valueOf(epochSeconds));
	            HtTrn.setVouid(0l);
	            HtTrn.setCid(loggeduser.getOrg_id());
	            HtTrn.setResbid(loggeduser.getBranch_id());
	            
	            httrn.add(HtTrn);
	            
	            }
	            
	        }

	        // ================= BATCH SAVE =================
	        htatResultRepo.saveAll(htResult);
	        htDayTrnRepo.saveAll(trn);
	        htDayTrnRepo.saveAll(httrn);

	        response.put("status", "SUCCESS");
	        response.put("message", "EB Table details saved and updated successfully");
	        return new ResponseEntity<>(response, HttpStatus.CREATED);

	    } catch (Exception ex) {
	        response.put("status", "FAILED");
	        response.put("message", ex.getMessage());
	        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	public ResponseEntity<?> getFromDate(Long serviceid,Long timmingid) {

		LocalDate fromDate = htDayTrnRepo.getFromDate(serviceid,timmingid);
		String dateStr = fromDate.toString();

		Map<String, String> response = new HashMap<>();
		response.put("fromdate", fromDate == null ? "" : dateStr);

		if (dateStr == null) {
			response.put("message", "FromDate not found for given id");
		}

		return ResponseEntity.ok(response);
	}

	
	public ResponseEntity<?> getServicename() {

	    List<ServiceNameDtoProjection> row = htmasterRepo.getList();

	    List<ServicenameDto> list = row.stream()
	            .map(data -> {
	                ServicenameDto dto = new ServicenameDto(data);
	                return dto;
	            })
	            .collect(Collectors.toList()); // ✅ Java 8

	    return ResponseEntity.ok(list);
	}
	
	public ResponseEntity<?> gettimeslap() {

	    List<ServiceNameDtoProjection> row = httslapRepo.gettimeslap();

	    List<ServicenameDto> list = row.stream()
	            .map(data -> {
	                ServicenameDto dto = new ServicenameDto(data);
	                return dto;
	            })
	            .collect(Collectors.toList()); // ✅ Java 8

	    return ResponseEntity.ok(list);
	}



	
}

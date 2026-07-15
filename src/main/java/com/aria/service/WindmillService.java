package com.aria.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.WindmillAttributesRepository;
import com.aria.dao.WindmillOtherChargesRepository;
import com.aria.dao.WindmillOtherChargesTrnRepository;
import com.aria.dao.WindmillRepository;
import com.aria.dao.WindmillTimmingRepository;
import com.aria.dao.WindmillTrnRepository;
import com.aria.dto.ChargeProjection;
import com.aria.dto.OtherDeductionBillResDto;
import com.aria.dto.OtherDeductionResList;
import com.aria.dto.OtherDeductionResListDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.PrintViewDto;
import com.aria.dto.TimeReadingPrintDto;
import com.aria.dto.TimeReadingSaveDto;
import com.aria.dto.TimingRawDataProjection;
import com.aria.dto.TimingReadingDto;
import com.aria.dto.WindmillAttributesDto;
import com.aria.dto.WindmillDto;
import com.aria.dto.WindmillOtherChargesDto;
import com.aria.dto.WindmillOtherChargesProjection;
import com.aria.dto.WindmillOtherChargesTrnDto;
import com.aria.dto.WindmillPlanDto;
import com.aria.dto.WindmillPrintDto;
import com.aria.dto.WindmillTimingResponseDto;
import com.aria.dto.WindmillTimmingDto;
import com.aria.dto.WindmillTrnDto;
import com.aria.dto.WindmillTrnProjection;
import com.aria.dto.WindmillWorkingDayDto;
import com.aria.dto.WindmillWorkingDayProjection;
import com.aria.dto.WindmillotherchargesSaveTrn;
import com.aria.dto.WindmillsaveDto;
import com.aria.model.User;
import com.aria.model.Windmill;
import com.aria.model.WindmillAttributes;
import com.aria.model.WindmillOtherCharges;
import com.aria.model.WindmillOtherChargesTrn;
import com.aria.model.WindmillTimming;
import com.aria.model.WindmillTrn;
import com.aria.utils.DateUtil;

@Service
public class WindmillService {

	@Autowired
	WindmillRepository windmillRepo;

	@Autowired
	WindmillOtherChargesTrnRepository windmillotherchargestrnRepo;

	@Autowired
	WindmillAttributesRepository windmillAttributeRepo;

	@Autowired
	WindmillOtherChargesRepository windmillOtherChargesRepo;

	@Autowired
	WindmillTimmingRepository windmillTimmingRepo;

	@Autowired
	WindmillTrnRepository windmillTrnRepo;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> getPlans(String type) {

		List<Windmill> wind = windmillRepo.getPlans(type);

		if (wind.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No plan found for type: " + type);
		}

		return ResponseEntity.ok(wind);
	}

	public ResponseEntity<?> getPlan(String type) {

		List<WindmillPlanDto> wind = windmillRepo.getPlan(type);

		if (wind.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No plan found for type: " + type);
		}

		return ResponseEntity.ok(wind);
	}

	public ResponseEntity<?> getFromDate(Long id) {

		LocalDate fromDate = windmillTrnRepo.getFromDate(id);
		String dateStr = fromDate.toString();

		Map<String, String> response = new HashMap<>();
		response.put("fromdate", fromDate == null ? "" : dateStr);

		if (dateStr == null) {
			response.put("message", "FromDate not found for given id");
		}

		return ResponseEntity.ok(response);
	}

	public WindmillTimingResponseDto getAllTimingDatas(Long windmillId, String fromDate) {

		LocalDate date = LocalDate.parse(fromDate);

		Windmill windmill = windmillRepo.findById(windmillId)
				.orElseThrow(() -> new RuntimeException("Windmill not found"));

		List<TimingRawDataProjection> rows = windmillTrnRepo.getAllTimingRawDatas(windmillId, date);

		Map<Long, TimingReadingDto> timingMap = new LinkedHashMap<>();

		for (TimingRawDataProjection row : rows) {
			Long timingId = row.getTimingid();

			timingMap.putIfAbsent(timingId, new TimingReadingDto(timingId, row.getTimingname(),0l, 0.0, 0.0,0l, 0.0, 0.0,0l, 0.0,
					0l,0.0,0l, 0.0, 0.0,0l, 0.0, 0.0,0l, 0.0,0l, 0.0));

			TimingReadingDto dto = timingMap.get(timingId);

			String attributeName = row.getAttributename();
			Long attributeid = row.getAttributeid();
			Double opening = row.getOpeningreading() != null ? row.getOpeningreading() : 0.0;
			Double closing = row.getClosingreading() != null ? row.getClosingreading() : 0.0;
			Double diff = row.getDifference() != null ? row.getDifference() : 0.0;

			if ("Export_Power".equalsIgnoreCase(attributeName)) {
				
				dto.setExportAttributeid(attributeid);
				dto.setExportOpening(opening);

			}
			if ("Import_Power".equalsIgnoreCase(attributeName)) {

				dto.setImportAttributeid(attributeid);
				dto.setImportOpening(opening);

			}
			if ("Net_Generation".equalsIgnoreCase(attributeName)) {

				dto.setNetGenerationAttributeid(attributeid);
				dto.setNetGeneration(0.0);
			}
			if ("T_&_D_Loss".equalsIgnoreCase(attributeName)) {

				dto.setTdLossAttributeid(attributeid);
				dto.setTdLoss(0.0);
			}
			if ("Generation Power".equalsIgnoreCase(attributeName)) {

				dto.setGenerationPowerAttributeid(attributeid);
				dto.setGenerationPowerOpening(opening);

			}
			if ("Export Power".equalsIgnoreCase(attributeName)) {

				dto.setExportPowerAttributeid(attributeid);
				dto.setExportPowerOpening(opening);

			}
			if ("Net_Export".equalsIgnoreCase(attributeName)) {

				dto.setNetexportAttributeid(attributeid);
				dto.setNetexport(diff);
			}
			if ("Consumed Unit".equalsIgnoreCase(attributeName)) {
				
				dto.setConsumedunitAttributeid(attributeid);
				dto.setConsumedunit(0.0);
			}

		}
		Double rkva = 0.0;
		if ("windmill".equalsIgnoreCase(windmill.getType())) {
			WindmillTrn trn = windmillTrnRepo.findByWindmillIdAndFromdate(windmillId, date);
			if (trn == null) {
				throw new RuntimeException(
						"No Windmill Transaction found for windmillId: " + windmillId + " and fromDate: " + fromDate);
			}
			rkva = trn.getOpeningReading();
		}

		WindmillTimingResponseDto response = new WindmillTimingResponseDto();
		response.setWindmillId(windmill.getId());
		response.setWindmillName(windmill.getName());
		response.setMf(windmill.getMf());
		response.setTdLoss(windmill.getTDLoss());
		response.setCapacity(windmill.getCapacity());
		response.setFromDate(date);
		response.setRkvaOpening(rkva);
		response.setTimings(new ArrayList<>(timingMap.values()));

		List<WindmillotherchargesSaveTrn> trn=null;
		Map<String, Double> othercharges = new LinkedHashMap<>();
		if ("windmill".equalsIgnoreCase(windmill.getType())) {
			List<ChargeProjection> list = windmillOtherChargesRepo.findChargesByType(windmill.getType());
			 trn  = list.stream().map(data -> {
				WindmillotherchargesSaveTrn dto = new WindmillotherchargesSaveTrn();
				dto.setId(data.getId());
				dto.setName(data.getName());
				if(data.getAmount() > 0) {
				  dto.setAmount((data.getAmount()*windmill.getCapacity())/250);
				}else {
					dto.setAmount(data.getAmount());
				}
				return dto;
			}).collect(Collectors.toList());

		} else {
			
			List<ChargeProjection> list = windmillOtherChargesRepo.findChargesByType(windmill.getType());

			 trn  = list.stream().map(data -> {
				WindmillotherchargesSaveTrn dto = new WindmillotherchargesSaveTrn();
				dto.setId(data.getId());
				dto.setName(data.getName());
				if(data.getAmount() > 0) {
				  dto.setAmount((data.getAmount()*windmill.getCapacity())/250);
				}else {
					dto.setAmount(data.getAmount());
				}
				return dto;
			}).collect(Collectors.toList());
		}
		
		response.setOtherCharges(othercharges);
		response.setCharges(trn);

		return response;
	}

	public ResponseEntity<?> getListForWindmill(Long month, Long year, Long plan, String type,
			String createddate, Long workingdays, Pageable pageable) {
		Page<WindmillWorkingDayProjection> list = windmillTrnRepo.findList(month, year, plan, type, createddate,
				workingdays, pageable);
		if (!list.isEmpty()) {
			List<WindmillWorkingDayDto> result = list.toList().stream().map(emp -> {
				WindmillWorkingDayDto dto = new WindmillWorkingDayDto(emp);
				return dto;
			}).collect(Collectors.toList());
			PageDataDto<WindmillWorkingDayDto> pageData = new PageDataDto<>(result, list);
			return new ResponseEntity<PageDataDto<WindmillWorkingDayDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<WindmillWorkingDayDto>>(new PageDataDto(), HttpStatus.OK);
		}

	}

	@Transactional
	public ResponseEntity<?> updateWindmillReading(WindmillsaveDto request, User loggedUser) {

		List<WindmillTrn> trnList = windmillTrnRepo.findlistForUpdate(request.getWindmillId(), request.getFromDate());

		if (trnList == null || trnList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No windmill records found for update");
		}

		Map<String, WindmillTrn> trnMap = trnList.stream()
				.collect(Collectors.toMap(trn -> trn.getAttributeId() + "_" + trn.getTimmingId(), trn -> trn));

		int updatedCount = 0;

		List<WindmillTrn> list = new ArrayList<>();
		if (request.getWindmillId() == 1 || request.getWindmillId() == 2 || request.getWindmillId() == 3) {
			WindmillTrn windtrn = new WindmillTrn();
			windtrn.setFromDate(request.getToDate());
			windtrn.setWindmillId(request.getWindmillId());
			windtrn.setAttributeId(0L);
			windtrn.setTimmingId(6L);
			windtrn.setOpeningReading(request.getRkvaClosing());
			windtrn.setEntPosition("L");
			list.add(windtrn);

		}else {
			
		}
		/*
		 * ------------------------------- 1️⃣ UPDATE FROM DTO
		 * --------------------------------
		 */
		for (TimeReadingSaveDto dto : request.getTimings()) {

			String key = dto.getAttributeeeid() + "_" + dto.getTimingId();
			WindmillTrn trn = trnMap.get(key);

			if (trn != null) {

				trn.setOpeningReading(dto.getOpening());
				trn.setClosingReading(dto.getClosing());

				if (trn.getOpeningReading() != null && dto.getClosing() != null) {
				    trn.setDifference(
				        Math.abs(dto.getClosing() - trn.getOpeningReading())
				    );
				}
				trn.setToDate(request.getToDate());
				trn.setWorkingDays(request.getWorkingDays());
				trn.setUpdatedBy(loggedUser.getId());
				trn.setCreatedDt(LocalDate.now());
				trn.setMonth(request.getMonth());
				trn.setYear(request.getYear());
				trn.setEntPosition("");

				updatedCount++;

				WindmillTrn windtrn = new WindmillTrn();
				windtrn.setFromDate(request.getToDate());
				windtrn.setWindmillId(request.getWindmillId());
				windtrn.setAttributeId(dto.getAttributeeeid());
				windtrn.setTimmingId(dto.getTimingId());
				windtrn.setOpeningReading(dto.getClosing());
				windtrn.setEntPosition("L");
				list.add(windtrn);
			}
		}

		/*
		 * ----------------------------------------- 2️⃣ DEFAULT RKVA RECORD (DB ONLY)
		 * attributeId = 0 AND timmingId = 6 ------------------------------------------
		 */
		String rkvaKey = "0_6";
		WindmillTrn rkvaTrn = trnMap.get(rkvaKey);

		if (rkvaTrn != null && request.getRkvaClosing() != null) {

			rkvaTrn.setClosingReading(request.getRkvaClosing());

			if (rkvaTrn.getOpeningReading() != null) {
				rkvaTrn.setDifference(request.getRkvaClosing() - rkvaTrn.getOpeningReading());
			}

			rkvaTrn.setToDate(request.getToDate());
			rkvaTrn.setWorkingDays(request.getWorkingDays());
			rkvaTrn.setUpdatedBy(loggedUser.getId());
			rkvaTrn.setCreatedDt(LocalDate.now());
			rkvaTrn.setMonth(request.getMonth());
			rkvaTrn.setYear(request.getYear());
			rkvaTrn.setEntPosition("");

			updatedCount++;

		}

		if (updatedCount == 0) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No records updated");
		}

		windmillTrnRepo.saveAll(list);
		
		List<WindmillOtherChargesTrn> otherchargetrn = request.getCharges().stream().map(data -> {
			WindmillOtherChargesTrn dto = new WindmillOtherChargesTrn();
			dto.setWindmillId(request.getWindmillId());
			dto.setTrnDate(request.getToDate());
			dto.setWindmillOtherChargesId(data.getId());
			dto.setAmount(data.getAmount());
			dto.setUpdatedBy(loggedUser.getId());
			dto.setMonth(request.getMonth().intValue());
			dto.setYear(request.getYear().intValue());
			return dto;
		}).collect(Collectors.toList());
		
		windmillotherchargestrnRepo.saveAll(otherchargetrn);
		

		Map<String, Object> response = new HashMap<>();
		response.put("message", "Windmill readings updated successfully");
		response.put("updatedRecords", updatedCount);

		return ResponseEntity.ok(response);
	}


	public ResponseEntity<?> getViewDetails(Long windmillId, String fromdate, String todate) {

		try {
			// 1️⃣ Validate input
			if (windmillId == null || fromdate == null || todate == null) {
				return ResponseEntity.badRequest().body("WindmillId, fromdate and todate are required");
			}

			LocalDate startdate;
			LocalDate enddate;

			try {
				startdate = LocalDate.parse(fromdate);
				enddate = LocalDate.parse(todate);
			} catch (DateTimeParseException e) {
				return ResponseEntity.badRequest().body("Invalid date format. Expected yyyy-MM-dd");
			}

			// 2️⃣ Fetch Windmill
			Windmill windmill = windmillRepo.findById(windmillId)
					.orElseThrow(() -> new EntityNotFoundException("Windmill not found"));

			// 3️⃣ Fetch readings
			List<WindmillTrnProjection> rows = windmillTrnRepo.findWindmillDetailsById(windmillId, startdate, enddate);

			if (rows == null || rows.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No readings found for the given date range");
			}

			// 4️⃣ Map readings
			List<TimeReadingSaveDto> result = rows.stream().map(data -> {
				TimeReadingSaveDto dto = new TimeReadingSaveDto();
				if ("Export_Power".equalsIgnoreCase(data.getAttributename())) {

					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());
				}
				if ("Import_Power".equalsIgnoreCase(data.getAttributename())) {
					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());
				}
				if ("Net_Generation".equalsIgnoreCase(data.getAttributename())) {
					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());

				}
				if ("T_&_D_Loss".equalsIgnoreCase(data.getAttributename())) {
					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());
				}
				if ("Generation Power".equalsIgnoreCase(data.getAttributename())) {
					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());

				}
				if ("Export Power".equalsIgnoreCase(data.getAttributename())) {
					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());

				}
				if ("Net_Export".equalsIgnoreCase(data.getAttributename())) {
					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());

				}
				if ("Consumed Unit".equalsIgnoreCase(data.getAttributename())) {
					dto.setTimingId(data.getTimmingid());
					dto.setTimingName(data.getTimmingname());
					dto.setAtname(data.getAttributename());
					dto.setAttributeeeid(data.getAttributeid());
					dto.setOpening(data.getOpening());
					dto.setClosing(data.getClosing());
					dto.setDifference(data.getDifference());
					dto.setDifference(data.getDifference());
				}
				return dto;
			}).collect(Collectors.toList());

			// 5️⃣ Prepare response
			WindmillTrnProjection firstRow = rows.get(0);

			WindmillsaveDto response = new WindmillsaveDto();
			response.setWindmillId(windmillId);
			response.setFromDate(startdate);
			response.setToDate(enddate);
			response.setCapacity(windmill.getCapacity());
			response.setMf(windmill.getMf());
			response.setWindmillName(windmill.getName());
			response.setSourcetype(windmill.getType());
			response.setTdLoss(windmill.getTDLoss());
			response.setWorkingDays(firstRow.getWorkingdays());
			response.setMonth(firstRow.getMonth());
			response.setYear(firstRow.getYear());
			response.setTimings(result);
			
			Map<String, Double> othercharges = new LinkedHashMap<>();
			if ("windmill".equalsIgnoreCase(windmill.getType())) {
				List<WindmillOtherChargesProjection> list = windmillotherchargestrnRepo.findByChargesUsingWindmillIdandTrndate(enddate,windmillId);

//		        Map<String, Long> response = new LinkedHashMap<>();

				for (WindmillOtherChargesProjection cp : list) {
					othercharges.put(cp.getName(), cp.getAmount());
				}

			} else {

				List<WindmillOtherChargesProjection> list = windmillotherchargestrnRepo.findByChargesUsingWindmillIdandTrndate(enddate,windmillId);

//		        Map<String, Long> response = new LinkedHashMap<>();

				for (WindmillOtherChargesProjection cp : list) {
					othercharges.put(cp.getName(), cp.getAmount());
				}

			}
			
			Double rkvaOpening = 0.0;
			Double rkvaClosing = 0.0;
			if ("windmill".equalsIgnoreCase(windmill.getType())) {
				WindmillTrn trn = windmillTrnRepo.findbyWindmillidandFromdateandTodate(windmillId, startdate,enddate);
				if (trn == null) {
					throw new RuntimeException(
							"No Windmill Transaction found for windmillId: " + windmillId + " and fromDate: " + startdate);
				}
				rkvaOpening = trn.getOpeningReading();
				rkvaClosing = trn.getClosingReading();
			}
			
			response.setRkvaOpening(rkvaOpening);
			response.setRkvaClosing(rkvaClosing);
			
			response.setOtherCharges(othercharges);

			return ResponseEntity.ok(response);

		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Something went wrong while fetching windmill details");
		}
	}
	
	public ResponseEntity<?> getAllTimingDetails(Long windmillId, String fromDate) {

		if (windmillId == null || fromDate == null ) {
			return ResponseEntity.badRequest().body("WindmillId, fromdate and todate are required");
		}

		LocalDate startdate;

		try {
			startdate = LocalDate.parse(fromDate);
		} catch (DateTimeParseException e) {
			return ResponseEntity.badRequest().body("Invalid date format. Expected yyyy-MM-dd");
		}

		// 2️⃣ Fetch Windmill
		Windmill windmill = windmillRepo.findById(windmillId)
				.orElseThrow(() -> new EntityNotFoundException("Windmill not found"));

		// 3️⃣ Fetch readings
		List<TimingRawDataProjection> rows = windmillTrnRepo.getAllTimingRawDatas(windmillId, startdate);

		if (rows == null || rows.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No readings found for the given date range");
		}

		// 4️⃣ Map readings
		List<TimeReadingSaveDto> result = rows.stream().map(data -> {
			TimeReadingSaveDto dto = new TimeReadingSaveDto();
			if ("Export_Power".equalsIgnoreCase(data.getAttributename())) {

				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());
			}
			if ("Import_Power".equalsIgnoreCase(data.getAttributename())) {
				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());
			}
			if ("Net_Generation".equalsIgnoreCase(data.getAttributename())) {
				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());

			}
			if ("T_&_D_Loss".equalsIgnoreCase(data.getAttributename())) {
				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());
			}
			if ("Generation Power".equalsIgnoreCase(data.getAttributename())) {
				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());

			}
			if ("Export Power".equalsIgnoreCase(data.getAttributename())) {
				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());

			}
			if ("Net_Export".equalsIgnoreCase(data.getAttributename())) {
				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());

			}
			if ("Consumed Unit".equalsIgnoreCase(data.getAttributename())) {
				dto.setTimingId(data.getTimingid());
				dto.setTimingName(data.getTimingname());
				dto.setAtname(data.getAttributename());
				dto.setAttributeeeid(data.getAttributeid());
				dto.setOpening(data.getOpeningreading());
				dto.setClosing(data.getClosingreading());
				dto.setDifference(data.getDifference());
				dto.setDifference(data.getDifference());
			}
			return dto;
		}).collect(Collectors.toList());
		
		Double rkva = 0.0;
		if ("windmill".equalsIgnoreCase(windmill.getType())) {
			WindmillTrn trn = windmillTrnRepo.findByWindmillIdAndFromdate(windmillId, startdate);
			if (trn == null) {
				throw new RuntimeException(
						"No Windmill Transaction found for windmillId: " + windmillId + " and fromDate: " + fromDate);
			}
			rkva = trn.getOpeningReading();
		}

		WindmillsaveDto response = new WindmillsaveDto();
		response.setWindmillId(windmillId);
		response.setFromDate(startdate);
		response.setCapacity(windmill.getCapacity());
		response.setMf(windmill.getMf());
		response.setWindmillName(windmill.getName());
		response.setTdLoss(windmill.getTDLoss());
		response.setRkvaOpening(rkva);
		response.setTimings(result);

		List<WindmillotherchargesSaveTrn> trn=null;
		Map<String, Double> othercharges = new LinkedHashMap<>();
		if ("windmill".equalsIgnoreCase(windmill.getType())) {
			List<ChargeProjection> list = windmillOtherChargesRepo.findChargesByType(windmill.getType());
			 trn  = list.stream().map(data -> {
				WindmillotherchargesSaveTrn dto = new WindmillotherchargesSaveTrn();
				dto.setId(data.getId());
				dto.setName(data.getName());
				if(data.getAmount() > 0) {
				  dto.setAmount((data.getAmount()*windmill.getCapacity())/250);
				}else {
					dto.setAmount(data.getAmount());
				}
				return dto;
			}).collect(Collectors.toList());

		} else {
			
			List<ChargeProjection> list = windmillOtherChargesRepo.findChargesByType(windmill.getType());

			 trn  = list.stream().map(data -> {
				WindmillotherchargesSaveTrn dto = new WindmillotherchargesSaveTrn();
				dto.setId(data.getId());
				dto.setName(data.getName());
				if(data.getAmount() > 0) {
				  dto.setAmount((data.getAmount()*windmill.getCapacity())/250);
				}else {
					dto.setAmount(data.getAmount());
				}
				return dto;
			}).collect(Collectors.toList());
		}
		response.setOtherCharges(othercharges);
		response.setCharges(trn);

		return ResponseEntity.ok(response);
	}
	
	
	
	public ResponseEntity<?> getPrintDetails(List<PrintViewDto> request) {

	    List<WindmillPrintDto> responses = new ArrayList<>();

	    try {
	        for (PrintViewDto view : request) {

	            // 1️⃣ Validation
	            if (view.getWindmillid() == null || view.getFromdate() == null || view.getTodate() == null) {
	                return ResponseEntity.badRequest()
	                        .body("WindmillId, fromdate and todate are required");
	            }

	            LocalDate startdate;
	            LocalDate enddate;
	            try {
	                startdate = LocalDate.parse(view.getFromdate());
	                enddate = LocalDate.parse(view.getTodate());
	            } catch (DateTimeParseException e) {
	                return ResponseEntity.badRequest()
	                        .body("Invalid date format. Expected yyyy-MM-dd");
	            }

	            // 2️⃣ Windmill
	            Windmill windmill = windmillRepo.findById(view.getWindmillid())
	                    .orElseThrow(() -> new EntityNotFoundException("Windmill not found"));

	            // 3️⃣ Readings
	            List<WindmillTrnProjection> rows =
	                    windmillTrnRepo.findWindmillDetailsById(
	                            view.getWindmillid(), startdate, enddate);

	            if (rows.isEmpty()) {
	                return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                        .body("No readings found for given date range");
	            }

	            // 4️⃣ Map readings
	            List<TimeReadingPrintDto> timings = rows.stream()
	                    .filter(r -> r.getAttributename() != null)
	                    .map(data -> {
	                    	TimeReadingPrintDto dto = new TimeReadingPrintDto();
	                        dto.setTimingId(data.getTimmingid());
	                        dto.setTimingName(data.getTimmingname());
	                        dto.setAtname(data.getAttributename());
	                        dto.setAttributeeeid(data.getAttributeid());
	                        if(data.getOpening() != 0 && data.getClosing() != 0) {
	                        BigDecimal unitRate = BigDecimal
	                                .valueOf(data.getDifference())
	                                .multiply(BigDecimal.valueOf(windmill.getMf()))
	                                .setScale(4, RoundingMode.DOWN);

	                        dto.setUnitRate(unitRate.doubleValue());
	                        }else {
	                        dto.setUnitRate(data.getDifference());
	                        }
	                        return dto;
	                    }).collect(Collectors.toList());

	            // 5️⃣ Response DTO
	            WindmillTrnProjection firstRow = rows.get(0);
	            WindmillPrintDto response = new WindmillPrintDto();

	            response.setWindmillId(view.getWindmillid());
	            response.setFromDate(startdate);
	            response.setToDate(enddate);
	            response.setWindmillName(windmill.getName());
	            response.setSourcetype(windmill.getType());
	            response.setWorkingDays(firstRow.getWorkingdays());
	            response.setMonth(firstRow.getMonth());
	            response.setYear(firstRow.getYear());
	            response.setTiming(timings);

	            responses.add(response);
	        }

	        return ResponseEntity.ok(responses);

	    } catch (EntityNotFoundException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body("Something went wrong while fetching windmill details");
	    }
	}

	public WindmillTimingResponseDto getAllTimingData(Long windmillId, String fromDate,Long month,Long year) {

		LocalDate date = LocalDate.parse(fromDate);
		int m= month.intValue();
		int y=year.intValue();
		LocalDate lastDay = LocalDate.of(y, m, 1)
                .withDayOfMonth(LocalDate.of(y, m, 1).lengthOfMonth());

		Windmill windmill = windmillRepo.findById(windmillId)
				.orElseThrow(() -> new RuntimeException("Windmill not found"));

		List<TimingRawDataProjection> rows = windmillTrnRepo.getAllTimingRawDatas(windmillId, date);

		Map<Long, TimingReadingDto> timingMap = new LinkedHashMap<>();

		for (TimingRawDataProjection row : rows) {
			Long timingId = row.getTimingid();

			timingMap.putIfAbsent(timingId, new TimingReadingDto(timingId, row.getTimingname(),0l, 0.0, 0.0,0l, 0.0, 0.0,0l, 0.0,
					0l,0.0,0l, 0.0, 0.0,0l, 0.0, 0.0,0l, 0.0,0l, 0.0));

			TimingReadingDto dto = timingMap.get(timingId);

			String attributeName = row.getAttributename();
			Long attributeid = row.getAttributeid();
			Double opening = row.getOpeningreading() != null ? row.getOpeningreading() : 0.0;
			Double closing = row.getClosingreading() != null ? row.getClosingreading() : 0.0;
			Double diff = row.getDifference() != null ? row.getDifference() : 0.0;

			if ("Export_Power".equalsIgnoreCase(attributeName)) {
				
				dto.setExportAttributeid(attributeid);
				dto.setExportOpening(opening);

			}
			if ("Import_Power".equalsIgnoreCase(attributeName)) {

				dto.setImportAttributeid(attributeid);
				dto.setImportOpening(opening);

			}
			if ("Net_Generation".equalsIgnoreCase(attributeName)) {

				dto.setNetGenerationAttributeid(attributeid);
				dto.setNetGeneration(0.0);
			}
			if ("T_&_D_Loss".equalsIgnoreCase(attributeName)) {

				dto.setTdLossAttributeid(attributeid);
				dto.setTdLoss(0.0);
			}
			if ("Generation Power".equalsIgnoreCase(attributeName)) {

				dto.setGenerationPowerAttributeid(attributeid);
				dto.setGenerationPowerOpening(opening);

			}
			if ("Export Power".equalsIgnoreCase(attributeName)) {

				dto.setExportPowerAttributeid(attributeid);
				dto.setExportPowerOpening(opening);

			}
			if ("Net_Export".equalsIgnoreCase(attributeName)) {

				dto.setNetexportAttributeid(attributeid);
				dto.setNetexport(diff);
			}
			if ("Consumed Unit".equalsIgnoreCase(attributeName)) {
				
				dto.setConsumedunitAttributeid(attributeid);
				dto.setConsumedunit(0.0);
			}

		}
		Double rkva = 0.0;
		if ("windmill".equalsIgnoreCase(windmill.getType())) {
			WindmillTrn trn = windmillTrnRepo.findByWindmillIdAndFromdate(windmillId, date);
			if (trn == null) {
				throw new RuntimeException(
						"No Windmill Transaction found for windmillId: " + windmillId + " and fromDate: " + fromDate);
			}
			rkva = trn.getOpeningReading();
		}

		WindmillTimingResponseDto response = new WindmillTimingResponseDto();
		response.setWindmillId(windmill.getId());
		response.setWindmillName(windmill.getName());
		response.setMf(windmill.getMf());
		response.setTdLoss(windmill.getTDLoss());
		response.setCapacity(windmill.getCapacity());
		response.setFromDate(date);
		response.setToDate(lastDay);
		response.setRkvaOpening(rkva);
		response.setTimings(new ArrayList<>(timingMap.values()));

		List<WindmillotherchargesSaveTrn> trn=null;
		Map<String, Double> othercharges = new LinkedHashMap<>();
		if ("windmill".equalsIgnoreCase(windmill.getType())) {
			List<ChargeProjection> list = windmillOtherChargesRepo.findChargesByType(windmill.getType());
			 trn  = list.stream().map(data -> {
				WindmillotherchargesSaveTrn dto = new WindmillotherchargesSaveTrn();
				dto.setId(data.getId());
				dto.setName(data.getName());
				if(data.getAmount() > 0) {
				  dto.setAmount((data.getAmount()*windmill.getCapacity())/250);
				}else {
					dto.setAmount(data.getAmount());
				}
				return dto;
			}).collect(Collectors.toList());

		} else {
			
			List<ChargeProjection> list = windmillOtherChargesRepo.findChargesByType(windmill.getType());

			 trn  = list.stream().map(data -> {
				WindmillotherchargesSaveTrn dto = new WindmillotherchargesSaveTrn();
				dto.setId(data.getId());
				dto.setName(data.getName());
				if(data.getAmount() > 0) {
				  dto.setAmount((data.getAmount()*windmill.getCapacity())/250);
				}else {
					dto.setAmount(data.getAmount());
				}
				return dto;
			}).collect(Collectors.toList());
		}
		
		response.setOtherCharges(othercharges);
		response.setCharges(trn);

		return response;
	}
	
	

}

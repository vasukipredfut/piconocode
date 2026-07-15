package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dto.EBDto;
import com.aria.dto.GeneratorDto;
import com.aria.dto.MachineDropDownDto;
import com.aria.dto.PageDataDto;
import com.aria.utils.DateUtil;

@Service
public class EBGeneratorService {
	public static final String PANEL_EB_SERVICE_NO = "NO";
	@Autowired
	EBRepository ebRepo;

	@Autowired
	PanelRepository panelRepository;

	@Autowired
	GeneratorRepository generatorRepo;

	@Autowired
	UserRepository userRepository;

	@Autowired
	MachineRepository machineRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> saveEB(ArrayList<EBDto> reqDtos, User user) {
		try {
			List<EB> ebLists = new LinkedList<>();
			List<EB> existingEbs = new ArrayList<>();
			for (EBDto reqDto : reqDtos) {
				EB requestEb = new EB(reqDto);
				requestEb.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				User ref = new User();
				ref.setId(user.getId());
				requestEb.setCreatedBy(ref);
				ebLists.add(requestEb);
				List<EB> ebList = ebRepo.getEBByServiceName(requestEb.getServiceName()); // To avoid duplicate request
				existingEbs.addAll(ebList);
			}
			if(existingEbs.isEmpty()) {
				ebRepo.saveAll(ebLists);
				return new ResponseEntity<Status>(new Status("200",
						"EB details saved successfully!!!"), HttpStatus.OK);
			}else{
				//This Duplicate failure should be 400 Status
				return new ResponseEntity<Status>(new Status("400", "Duplicate Request : EB details not created"), HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "EB details not created"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateEB(EBDto reqDtos, User user) {
		try {
			EB newEB = new EB(reqDtos);
			newEB.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			User ref = new User();
			ref.setId(user.getId());
			newEB.setUpdatedBy(ref);
			ebRepo.save(newEB);
			return new ResponseEntity<Status>(new Status("200", "EB details updated successfully!!!"), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "EB details not updated"), HttpStatus.OK);
	}

	public ResponseEntity<?> getEBDetails(String id) throws UsernameNotFoundException {
		Optional<EB> optionalEB = ebRepo.findById(Long.parseLong(id));
		EBDto ebDto = new EBDto(optionalEB.get());
		return new ResponseEntity<EBDto>(ebDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteEB(Long id) {

		Optional<EB> ebOptional = ebRepo.findById(id);
		if (ebOptional.isPresent()) {
			ebRepo.delete(ebOptional.get());
			return new ResponseEntity<Status>(new Status("200", "EB detail deleted successfully!!!"), HttpStatus.OK);
		}
		return new ResponseEntity<Status>(new Status("400", "EB details not deleted"), HttpStatus.OK);
	}

	public ResponseEntity<?> listAllEBDetails(String serviceNo, String serviceName, String serviceCategory,
											  String status, User user, String machineId, Pageable pageable) {
		Page<EB> ebPage = null;
		if(machineId != null) {
			Optional<Machine> machineOpt = machineRepository.findById(Long.valueOf(machineId));
			if(machineOpt.isPresent()){
				Long ebId = machineOpt.map(Machine::getEb).map(EB::getId).orElse(null);
				if(ebId != null){
					ebPage = ebRepo.findAllEBDetails(ebId,serviceNo, serviceName, serviceCategory, status, pageable);
				}
			}
		}else{
			ebPage = ebRepo.findAllEBDetails(null,serviceNo, serviceName, serviceCategory, status, pageable);
		}


		if (ebPage!=null && !ebPage.toList().isEmpty()) {
			List<EBDto> result = ebPage.toList().stream().map(data -> {
				EBDto ebDto = new EBDto(data);
				return ebDto;
			}).collect(Collectors.toList());
			PageDataDto<EBDto> pageData = new PageDataDto<>(result, pageable, ebPage);
			return new ResponseEntity<>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<EBDto>>(new PageDataDto<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> listAllEBDetails(String serviceNo, String serviceName, String serviceCategory,
											  String status,String machineId, User user) {
		List<EB> ebs = new ArrayList<>();
		if(machineId != null){
			Optional<Machine> machineOpt = machineRepository.findById(Long.valueOf(machineId));
			if(machineOpt.isPresent()){
				Long ebId = machineOpt.map(Machine::getEb).map(EB::getId).orElse(null);
				if(ebId != null){
					List<EB> ebList = ebRepo.findAllEBDetailsById(ebId,serviceNo, serviceName, serviceCategory, status);
					ebs.addAll(ebList);
				}

			}

		}else{
			List<EB> ebList = ebRepo.findAllEBDetails(serviceNo, serviceName, serviceCategory, status);
			ebs.addAll(ebList);
		}

		if (!ebs.isEmpty()) {
			List<EBDto> result = ebs.stream().map(data -> {
				EBDto ebDto = new EBDto(data);
				return ebDto;
			}).collect(Collectors.toList());
			return new ResponseEntity<List<EBDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<EBDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	// Generator services

	public ResponseEntity<?> saveGenerator(ArrayList<GeneratorDto> reqDtos, User user) {
		try {
			List<Generator> generatorLists = new LinkedList<>();
			for (GeneratorDto reqDto : reqDtos) {
				Generator newGenerator = new Generator(reqDto);
				newGenerator.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				User ref = new User();
				ref.setId(user.getId());
				newGenerator.setCreatedBy(ref);
				generatorLists.add(newGenerator);
			}
			generatorRepo.saveAll(generatorLists);
			return new ResponseEntity<Status>(new Status("200", "Generator details saved successfully!!!"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Generator details not created"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateGenerator(GeneratorDto reqDtos, User user) {
		try {
			Generator generator = new Generator(reqDtos);
			generator.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			User ref = new User();
			ref.setId(user.getId());
			generator.setUpdatedBy(ref);
			generatorRepo.save(generator);
			return new ResponseEntity<Status>(new Status("200", "Generator details updated successfully!!!"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Generator details not updated"), HttpStatus.OK);
	}

	public ResponseEntity<?> getGeneratorDetails(String id) throws UsernameNotFoundException {
		Optional<Generator> optionalGenerator = generatorRepo.findById(Long.parseLong(id));
		GeneratorDto generatorDto = new GeneratorDto(optionalGenerator.get());
		return new ResponseEntity<GeneratorDto>(generatorDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteGenerator(Long id) {

		Optional<Generator> generator = generatorRepo.findById(id);
		if (generator.isPresent()) {

			generatorRepo.delete(generator.get());
			return new ResponseEntity<Status>(new Status("200", "Generator detail deleted successfully!!!"),
					HttpStatus.OK);
		}
		return new ResponseEntity<Status>(new Status("400", "Generator details not deleted"), HttpStatus.OK);
	}

	public ResponseEntity<?> listAllGeneratorDetails(String name, String status, String machineId, String dateFrom,
			String dateTo, User user, Pageable pageable) {
		Page<Generator> list = generatorRepo.findAllGenDetails(name, status, machineId,
				dateUtil.getStringTODateF1(dateFrom), dateUtil.getStringTODateF1(dateTo), pageable);
		if (!list.toList().isEmpty()) {
			List<GeneratorDto> result = list.toList().stream().map(data -> {
				GeneratorDto generatorDto = new GeneratorDto(data);
				return generatorDto;
			}).collect(Collectors.toList());
			PageDataDto<GeneratorDto> pageData = new PageDataDto<>(result, pageable, list);
			return new ResponseEntity<PageDataDto<GeneratorDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<GeneratorDto>>(new PageDataDto<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getEBNames() {
		List<MachineDropDown> machines = ebRepo.getEbNames();
		
		List<MachineDropDownDto> machineDtos = machines.stream().map(val -> new MachineDropDownDto(val)).collect(Collectors.toList());
		
		return new ResponseEntity<List<MachineDropDownDto>>(machineDtos, HttpStatus.OK);
	}
	
	public ResponseEntity<?> getGeneratorNames() {
		List<MachineDropDown> machines = generatorRepo.getGeneratorNames();
		
		List<MachineDropDownDto> machineDtos = machines.stream().map(val -> new MachineDropDownDto(val)).collect(Collectors.toList());
		
		return new ResponseEntity<List<MachineDropDownDto>>(machineDtos, HttpStatus.OK);
	}

}

package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.RodentMonitorRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.PageDataDto;
import com.aria.dto.RodentMonitorDto;
import com.aria.model.RodentMonitor;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class RodentMonitorService {

	@Autowired
	RodentMonitorRepository rodentMonitorRepo;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> saveRodentMonitor(RodentMonitorDto reqDto, User user) {
		try {
//			List<RodentMonitor> generatorLists = new LinkedList<>();
//			for (RodentMonitorDto reqDto : reqDtos) {
				RodentMonitor newRodentMonitor = new RodentMonitor(reqDto);
				newRodentMonitor.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				newRodentMonitor.setCreatedBy(user.getId());
				newRodentMonitor.setRodentDate(dateUtil.getStringTODateWk(reqDto.getRodentDate()));
//				generatorLists.add(newRodentMonitor);
//			}
			rodentMonitorRepo.save(newRodentMonitor);
			return new ResponseEntity<Status>(new Status("200", "RodentMonitor details saved successfully!!!"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "RodentMonitor details not created"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateRodentMonitor(RodentMonitorDto reqDtos, User user) {
		try {
			RodentMonitor newRodentMonitor = new RodentMonitor(reqDtos);
			newRodentMonitor.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			newRodentMonitor.setUpdatedBy(user.getId());
			rodentMonitorRepo.save(newRodentMonitor);
			return new ResponseEntity<Status>(new Status("200", "RodentMonitor details updated successfully!!!"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "RodentMonitor details not updated"), HttpStatus.OK);
	}

	public ResponseEntity<?> getRodentMonitorDetails(String id) throws UsernameNotFoundException {
		Optional<RodentMonitor> optionalRodentMonitor = rodentMonitorRepo.findById(Long.parseLong(id));
		RodentMonitorDto generatorDto = new RodentMonitorDto(optionalRodentMonitor.get());
		return new ResponseEntity<RodentMonitorDto>(generatorDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteRodentMonitor(Long id) {

		Optional<RodentMonitor> generator = rodentMonitorRepo.findById(id);
		if (generator.isPresent()) {
			rodentMonitorRepo.delete(generator.get());
			return new ResponseEntity<Status>(new Status("200", "RodentMonitor detail deleted successfully!!!"), HttpStatus.OK);
		}
		return new ResponseEntity<Status>(new Status("400", "RodentMonitor details not deleted"), HttpStatus.OK);
	}

	public ResponseEntity<?> listAllRodentMonitorDetails(User user, Pageable pageable, String goDown, String reason) {
		Page<RodentMonitor> list = rodentMonitorRepo.findAllWithFilters(pageable, goDown, reason);
		if (!list.toList().isEmpty()) {
			List<RodentMonitorDto> result = list.toList().stream().map(data -> {
				RodentMonitorDto generatorDto = new RodentMonitorDto(data);
				generatorDto.setRodentDate(dateUtil.getDateTOStringWk(data.getRodentDate()));
				return generatorDto;
			}).collect(Collectors.toList());
			PageDataDto<RodentMonitorDto> pageData = new PageDataDto<>(result, pageable, list);
			return new ResponseEntity<PageDataDto<RodentMonitorDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<RodentMonitorDto>>(new PageDataDto<>(), HttpStatus.OK);
		}
	}

}

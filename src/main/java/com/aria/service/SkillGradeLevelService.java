package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.SkillGradeLevelRepository;
import com.aria.dto.SkillGradeLevelDto;
import com.aria.model.SkillGradeLevel;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class SkillGradeLevelService {

	@Autowired
	SkillGradeLevelRepository skillGradeLevelRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> saveSkillGradeLevel(List<SkillGradeLevelDto> skillGradeLevelDtos, User loggedUser) {

		List<SkillGradeLevel> skillGradeLevelList = new ArrayList<>();

		for (SkillGradeLevelDto skillGradeLevelDto : skillGradeLevelDtos) {
			SkillGradeLevel skillGradeLevel = new SkillGradeLevel(skillGradeLevelDto);
			skillGradeLevel.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			skillGradeLevel.setCreatedBy(loggedUser.getId());
			skillGradeLevelList.add(skillGradeLevel);
		}
		skillGradeLevelRepository.saveAll(skillGradeLevelList);
		return new ResponseEntity<Status>(new Status("200", "SkillGradeLevels saved successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateSkillGradeLevel(SkillGradeLevelDto skillGradeLevelDto, User loggedUser) {
		SkillGradeLevel skillGradeLevel = new SkillGradeLevel(skillGradeLevelDto);
		skillGradeLevel.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		skillGradeLevel.setUpdatedBy(loggedUser.getId());
		skillGradeLevelRepository.save(skillGradeLevel);
		return new ResponseEntity<Status>(new Status("200", "SkillGradeLevel updated successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> listAllSkillGradeLevel() throws UsernameNotFoundException {
		List<SkillGradeLevel> skillGradeLevelList = skillGradeLevelRepository.findAll();
		return new ResponseEntity<List<SkillGradeLevelDto>>(skillGradeLevelList.stream().map(data -> {
			SkillGradeLevelDto skillGradeLevelDto = new SkillGradeLevelDto(data);
			return skillGradeLevelDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> getSkillGradeLevel(String id) throws UsernameNotFoundException {
		Optional<SkillGradeLevel> skillGradeLevelOptional = skillGradeLevelRepository.findById(Long.parseLong(id));
		if (skillGradeLevelOptional.isPresent()) {
			SkillGradeLevelDto skillGradeLevelDto = new SkillGradeLevelDto(skillGradeLevelOptional.get());
			return new ResponseEntity<SkillGradeLevelDto>(skillGradeLevelDto, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	public ResponseEntity<?> deleteskillGradeLevel(String id) throws UsernameNotFoundException {
		try {
			Optional<SkillGradeLevel> skillGradeLevelOptional = skillGradeLevelRepository.findById(Long.parseLong(id));
			if (skillGradeLevelOptional.isPresent()) {
				SkillGradeLevel skillGradeLevel = skillGradeLevelOptional.get();
				skillGradeLevelRepository.delete(skillGradeLevel);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

}

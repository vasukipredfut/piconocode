package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
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

import com.aria.dao.PanelRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.MachineDropDownDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.PanelDto;
import com.aria.model.MachineDropDown;
import com.aria.model.Panel;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class PanelService {

	@Autowired
	PanelRepository panelRepo;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> savePanel(ArrayList<PanelDto> reqDtos, User user) {
		try {
			for (PanelDto reqDto : reqDtos) {
				Panel newPanel = new Panel(reqDto);
				newPanel.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				newPanel.setCreatedBy(user.getId());
				Panel rs = panelRepo.save(newPanel);
				updatePanel(rs);
			}
			return new ResponseEntity<Status>(new Status("200", "Panel details saved successfully!!!"), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Panel details not created"), HttpStatus.OK);
	}

	public ResponseEntity<?> updatePanel(PanelDto reqDtos, User user) {
		try {
			Panel newPanel = new Panel(reqDtos);
			newPanel.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			newPanel.setUpdatedBy(user.getId());
			Panel rs = panelRepo.save(newPanel);
			updatePanel(rs);
			return new ResponseEntity<Status>(new Status("200", "Panel details updated successfully!!!"),
					HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Panel details not updated"), HttpStatus.OK);
	}

	public ResponseEntity<?> getPanelDetails(String id) throws UsernameNotFoundException {
		Optional<Panel> optionalPanel = panelRepo.findById(Long.parseLong(id));
		PanelDto generatorDto = new PanelDto(optionalPanel.get());
		return new ResponseEntity<PanelDto>(generatorDto, HttpStatus.OK);
	}

	public void updatePanel (Panel panel) {
		panel.setPanelId(panel.getId());
		panelRepo.save(panel);
	}

	public ResponseEntity<?> deletePanel(Long id) {

		Optional<Panel> generator = panelRepo.findById(id);
		if (generator.isPresent()) {
			panelRepo.delete(generator.get());
			return new ResponseEntity<Status>(new Status("200", "Panel detail deleted successfully!!!"), HttpStatus.OK);
		}
		return new ResponseEntity<Status>(new Status("400", "Panel details not deleted"), HttpStatus.OK);
	}

	public ResponseEntity<?> listAllPanelDetails(User user, Pageable pageable, String panelName, String machineName,
			String ebServiceName) {
		Page<Panel> list = panelRepo.findAllWithFilters(pageable, panelName, machineName, ebServiceName);
		if (!list.toList().isEmpty()) {
			List<PanelDto> result = list.toList().stream().map(data -> {
				PanelDto generatorDto = new PanelDto(data);
				return generatorDto;
			}).collect(Collectors.toList());
			PageDataDto<PanelDto> pageData = new PageDataDto<>(result, pageable, list);
			return new ResponseEntity<PageDataDto<PanelDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<PanelDto>>(new PageDataDto<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getPanelNames(Long machineId) {

		List<MachineDropDown> machines = panelRepo.getPanelNames(machineId);

		List<MachineDropDownDto> machineDtos = machines.stream().map(val -> new MachineDropDownDto(val)).collect(Collectors.toList());

		return new ResponseEntity<List<MachineDropDownDto>>(machineDtos, HttpStatus.OK);
	}

}

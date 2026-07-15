
package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InternalMaterialIssueService {

	@Autowired
	InternalMaterialIssueRepository internalMaterialIssueRepository;

	@Autowired
	ReceivedItemRepository receivedItemRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> saveInternalMaterialIssue(InternalMaterialIssueDto internalMaterialIssueDto, User loggedUser) {
		InternalMaterialIssue internalMaterialIssue = new InternalMaterialIssue(internalMaterialIssueDto);

		internalMaterialIssue.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		internalMaterialIssue.setCreatedBy(loggedUser.getId());
		internalMaterialIssueRepository.save(internalMaterialIssue);
		for (ReceivedItemsDto receivedItemsDto : internalMaterialIssueDto.getReceivedItemsDetails()) {
			ReceivedItems receivedItems = new ReceivedItems(receivedItemsDto);

			receivedItems.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			receivedItems.setCreatedBy(loggedUser.getId());
			receivedItemRepository.save(receivedItems);
		}
		return new ResponseEntity<Status>(new Status("200", "InternalMaterial save successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateInternalMaterialIssue(InternalMaterialIssueDto internalMaterialIssueDto, User loggedUser) {
		InternalMaterialIssue internalMaterialIssue = new InternalMaterialIssue(internalMaterialIssueDto);
		internalMaterialIssue.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		internalMaterialIssue.setUpdatedBy(loggedUser.getId());
		internalMaterialIssueRepository.save(internalMaterialIssue);
		for (ReceivedItemsDto receivedItemsDto : internalMaterialIssueDto.getReceivedItemsDetails()) {
			ReceivedItems receivedItems = new ReceivedItems(receivedItemsDto);
			receivedItems.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			receivedItems.setUpdatedBy(loggedUser.getId());
			receivedItemRepository.save(receivedItems);
		}
		return new ResponseEntity<Status>(new Status("200", "InternalMaterial  updated  successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> listAll() throws UsernameNotFoundException {

		List<InternalMaterialIssue> list = internalMaterialIssueRepository.findAll();
		if (!list.isEmpty()) {
			List<InternalMaterialIssueDto> result = list.stream().map(data -> {
				InternalMaterialIssueDto internalMaterialIssueDto = new InternalMaterialIssueDto(data);

				return internalMaterialIssueDto;
			}).collect(Collectors.toList());
			return new ResponseEntity<List<InternalMaterialIssueDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<InternalMaterialIssueDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getInternalMaterialIssue(String id) throws UsernameNotFoundException {
		Optional<InternalMaterialIssue> optionalInternalMaterialIssue = internalMaterialIssueRepository
				.findById(Long.parseLong(id));
		InternalMaterialIssueDto internalMaterialIssueDto = new InternalMaterialIssueDto(
				optionalInternalMaterialIssue.get());

		return new ResponseEntity<InternalMaterialIssueDto>(internalMaterialIssueDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteInternalMaterialIssue(String id) throws UsernameNotFoundException {
		try {
			Optional<InternalMaterialIssue> optionalInternalMaterialIssue = internalMaterialIssueRepository
					.findById(Long.parseLong(id));
			if (optionalInternalMaterialIssue.isPresent()) {
				InternalMaterialIssue quantity = optionalInternalMaterialIssue.get();
				internalMaterialIssueRepository.delete(quantity);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

}

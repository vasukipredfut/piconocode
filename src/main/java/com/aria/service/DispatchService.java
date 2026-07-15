package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.BinStockRepository;
import com.aria.dao.DispatchAgainstBillRepository;
import com.aria.dao.DispatchAgainstOrderRepository;
import com.aria.dao.DispatchOrderWithPriceRepository;
import com.aria.dao.DpatchBillOrderItemsRepository;
import com.aria.dao.DpatchDespatchRepository;
import com.aria.dao.OrderRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.DispatchAgainstBillDto;
import com.aria.dto.DispatchAgainstOrderDto;
import com.aria.dto.DispatchOrderWithPriceDto;
import com.aria.model.DispatchAgainstBill;
import com.aria.model.DispatchAgainstOrder;
import com.aria.model.DispatchOrderWithPrice;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class DispatchService {

	@Autowired
	DispatchAgainstOrderRepository dispatchAgainstOrderRepository;

	@Autowired
	DispatchOrderWithPriceRepository dispatchOrderWithPriceRepository;

	@Autowired
	DispatchAgainstBillRepository dispatchAgainstBillRepository;

	@Autowired
	private DpatchDespatchRepository dpatchDespatchRepository;

	@Autowired
	private DpatchBillOrderItemsRepository dpatchBillOrderItemsRepository;

	@Autowired
	private BinStockRepository binStockRepository;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	// DispatchAgainstOrder

	public ResponseEntity<?> saveDispatchAgainstOrder(DispatchAgainstOrderDto dispatchAgainstOrderDto,
			User loggedUser) {
		DispatchAgainstOrder dispatchAgainstOrder = new DispatchAgainstOrder(dispatchAgainstOrderDto);
		dispatchAgainstOrder.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		dispatchAgainstOrder.setCreatedBy(loggedUser.getId());
		dispatchAgainstOrder.setOrderDate(dateUtil.getStringTODateF1(dispatchAgainstOrderDto.getOrderDate()));
		dispatchAgainstOrderRepository.save(dispatchAgainstOrder);
		return new ResponseEntity<Status>(new Status("200", "dispatchAgainstOrder saved successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> updateDispatchAgainstOrder(DispatchAgainstOrderDto dispatchAgainstOrderDto,
			User loggedUser) {
		DispatchAgainstOrder dispatchAgainstOrder = new DispatchAgainstOrder(dispatchAgainstOrderDto);
		dispatchAgainstOrder.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		dispatchAgainstOrder.setUpdatedBy(loggedUser.getId());
		dispatchAgainstOrder.setOrderDate(dateUtil.getStringTODateF1(dispatchAgainstOrderDto.getOrderDate()));
		dispatchAgainstOrderRepository.save(dispatchAgainstOrder);
		return new ResponseEntity<Status>(new Status("200", "dispatchAgainstOrder updated successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> listAllDispatchAgainstOrder() throws UsernameNotFoundException {
		List<DispatchAgainstOrder> dispatchAgainstOrder = dispatchAgainstOrderRepository.findAll();
		return new ResponseEntity<List<DispatchAgainstOrderDto>>(dispatchAgainstOrder.stream().map(user -> {
			DispatchAgainstOrderDto dispatchAgainstOrderDto = new DispatchAgainstOrderDto(user);
			dispatchAgainstOrderDto.setOrderDate(dateUtil.getStringTODate(user.getOrderDate()));
			updateEmployeeDetails(dispatchAgainstOrderDto);
			return dispatchAgainstOrderDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> getDispatchAgainstOrder(String id) throws UsernameNotFoundException {
		Optional<DispatchAgainstOrder> optionalDispatchAgainstOrder = dispatchAgainstOrderRepository
				.findById(Long.parseLong(id));
		DispatchAgainstOrderDto dispatchAgainstOrderDto = new DispatchAgainstOrderDto(
				optionalDispatchAgainstOrder.get());
		dispatchAgainstOrderDto
				.setOrderDate(dateUtil.getStringTODate(optionalDispatchAgainstOrder.get().getOrderDate()));
		return new ResponseEntity<DispatchAgainstOrderDto>(dispatchAgainstOrderDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteDispatchAgainstOrder(String id) throws UsernameNotFoundException {
		try {
			Optional<DispatchAgainstOrder> dispatchAgainstOrderOptional = dispatchAgainstOrderRepository
					.findById(Long.parseLong(id));
			if (dispatchAgainstOrderOptional.isPresent()) {
				DispatchAgainstOrder dispatchAgainstOrder = dispatchAgainstOrderOptional.get();
				dispatchAgainstOrderRepository.delete(dispatchAgainstOrder);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

	public void updateEmployeeDetails(DispatchAgainstOrderDto dispatchAgainstOrder) {

		/*
		 * Optional<User> user =
		 * userRepository.findById(dispatchAgainstOrder.getCreatedBy()); if
		 * (user.isPresent()) {
		 * //dispatchAgainstOrderDto.setCreatedByName(user.get().getEmployeeName());
		 * //dispatchAgainstOrderDto.setUpdatedByName(user.get().getEmployeeName()); }
		 * if (dispatchAgainstOrder.getUpdatedBy() != null) { Optional<User> user1 =
		 * userRepository.findById(dispatchAgainstOrder.getUpdatedBy()); if
		 * (user1.isPresent()) { //
		 * dispatchAgainstOrder.setUpdatedByName(user.get().getEmployeeName()); } }
		 */
	}

//DispatchOrderWithPrice

	public ResponseEntity<?> saveDispatchOrderWithPrice(DispatchOrderWithPriceDto dispatchOrderWithPriceDto,
			User loggedUser) {
		DispatchOrderWithPrice dispatchOrderWithPrice = new DispatchOrderWithPrice(dispatchOrderWithPriceDto);
		dispatchOrderWithPrice.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		dispatchOrderWithPrice.setCreatedBy(loggedUser.getId());
		dispatchOrderWithPrice.setOrderDate(dateUtil.getStringTODateF1(dispatchOrderWithPriceDto.getOrderDate()));
		dispatchOrderWithPriceRepository.save(dispatchOrderWithPrice);
		return new ResponseEntity<Status>(new Status("200", "dispatchOrderWithPrice saved successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> updateDispatchOrderWithPrice(DispatchOrderWithPriceDto dispatchOrderWithPriceDto,
			User loggedUser) {
		DispatchOrderWithPrice dispatchOrderWithPrice = new DispatchOrderWithPrice(dispatchOrderWithPriceDto);
		dispatchOrderWithPrice.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		dispatchOrderWithPrice.setUpdatedBy(loggedUser.getId());
		dispatchOrderWithPrice.setOrderDate(dateUtil.getStringTODateF1(dispatchOrderWithPriceDto.getOrderDate()));
		dispatchOrderWithPriceRepository.save(dispatchOrderWithPrice);
		return new ResponseEntity<Status>(new Status("200", "dispatchOrderWithPrice updated successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> listAllDispatchOrderWithPrice() throws UsernameNotFoundException {
		List<DispatchOrderWithPrice> dispatchOrderWithPrice = dispatchOrderWithPriceRepository.findAll();
		return new ResponseEntity<List<DispatchOrderWithPriceDto>>(dispatchOrderWithPrice.stream().map(user -> {
			DispatchOrderWithPriceDto dispatchOrderWithPriceDto = new DispatchOrderWithPriceDto(user);
			dispatchOrderWithPriceDto.setOrderDate(dateUtil.getStringTODate(user.getOrderDate()));
			updateEmployeeDetails(dispatchOrderWithPriceDto);
			return dispatchOrderWithPriceDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> getDispatchOrderWithPrice(String id) throws UsernameNotFoundException {
		Optional<DispatchOrderWithPrice> optionalDispatchOrderWithPrice = dispatchOrderWithPriceRepository
				.findById(Long.parseLong(id));
		DispatchOrderWithPriceDto dispatchOrderWithPriceDto = new DispatchOrderWithPriceDto(
				optionalDispatchOrderWithPrice.get());
		dispatchOrderWithPriceDto
				.setOrderDate(dateUtil.getStringTODate(optionalDispatchOrderWithPrice.get().getOrderDate()));
		return new ResponseEntity<DispatchOrderWithPriceDto>(dispatchOrderWithPriceDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteDispatchOrderWithPrice(String id) throws UsernameNotFoundException {
		try {
			Optional<DispatchOrderWithPrice> dispatchOrderWithPriceOptional = dispatchOrderWithPriceRepository
					.findById(Long.parseLong(id));
			if (dispatchOrderWithPriceOptional.isPresent()) {
				DispatchOrderWithPrice dispatchOrderWithPrice = dispatchOrderWithPriceOptional.get();
				dispatchOrderWithPriceRepository.delete(dispatchOrderWithPrice);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

	public void updateEmployeeDetails(DispatchOrderWithPriceDto dispatchOrderWithPrice) {

		/*
		 * Optional<User> user =
		 * userRepository.findById(dispatchOrderWithPrice.getCreatedBy()); if
		 * (user.isPresent()) {
		 * //dispatchOrderWithPriceDto.setCreatedByName(user.get().getEmployeeName());
		 * //dispatchOrderWithPriceDto.setUpdatedByName(user.get().getEmployeeName()); }
		 * if (dispatchOrderWithPrice.getUpdatedBy() != null) { Optional<User> user1 =
		 * userRepository.findById(dispatchOrderWithPrice.getUpdatedBy()); if
		 * (user1.isPresent()) { //
		 * dispatchOrderWithPrice.setUpdatedByName(user.get().getEmployeeName()); } }
		 */
	}

//DispatchAgainstBill

	public ResponseEntity<?> saveDispatchAgainstBill(DispatchAgainstBillDto dispatchAgainstBillDto, User loggedUser) {
		DispatchAgainstBill dispatchAgainstBill = new DispatchAgainstBill(dispatchAgainstBillDto);
		dispatchAgainstBill.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		dispatchAgainstBill.setCreatedBy(loggedUser.getId());
		dispatchAgainstBill.setBillDate(dateUtil.getStringTODateF1(dispatchAgainstBillDto.getBillDate()));
		dispatchAgainstBillRepository.save(dispatchAgainstBill);
		return new ResponseEntity<Status>(new Status("200", "dispatchAgainstBill saved successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> updateDispatchAgainstBill(DispatchAgainstBillDto dispatchAgainstBillDto, User loggedUser) {
		DispatchAgainstBill dispatchAgainstBill = new DispatchAgainstBill(dispatchAgainstBillDto);
		dispatchAgainstBill.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		dispatchAgainstBill.setUpdatedBy(loggedUser.getId());
		dispatchAgainstBill.setBillDate(dateUtil.getStringTODateF1(dispatchAgainstBillDto.getBillDate()));
		dispatchAgainstBillRepository.save(dispatchAgainstBill);
		return new ResponseEntity<Status>(new Status("200", "dispatchAgainstBill updated successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> listAllDispatchAgainstBill() throws UsernameNotFoundException {
		List<DispatchAgainstBill> dispatchAgainstBill = dispatchAgainstBillRepository.findAll();
		return new ResponseEntity<List<DispatchAgainstBillDto>>(dispatchAgainstBill.stream().map(user -> {
			DispatchAgainstBillDto dispatchAgainstBillDto = new DispatchAgainstBillDto(user);
			dispatchAgainstBillDto.setBillDate(dateUtil.getStringTODate(user.getBillDate()));
			updateEmployeeDetails(dispatchAgainstBillDto);
			return dispatchAgainstBillDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> getDispatchAgainstBill(String id) throws UsernameNotFoundException {
		Optional<DispatchAgainstBill> optionalDispatchAgainstBill = dispatchAgainstBillRepository
				.findById(Long.parseLong(id));
		DispatchAgainstBillDto dispatchAgainstBillDto = new DispatchAgainstBillDto(optionalDispatchAgainstBill.get());
		dispatchAgainstBillDto.setBillDate(dateUtil.getStringTODate(optionalDispatchAgainstBill.get().getBillDate()));
		return new ResponseEntity<DispatchAgainstBillDto>(dispatchAgainstBillDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteDispatchAgainstBill(String id) throws UsernameNotFoundException {
		try {
			Optional<DispatchAgainstBill> dispatchAgainstBillOptional = dispatchAgainstBillRepository
					.findById(Long.parseLong(id));
			if (dispatchAgainstBillOptional.isPresent()) {
				DispatchAgainstBill dispatchAgainstBill = dispatchAgainstBillOptional.get();
				dispatchAgainstBillRepository.delete(dispatchAgainstBill);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

	public void updateEmployeeDetails(DispatchAgainstBillDto dispatchAgainstBill) {

		/*
		 * Optional<User> user =
		 * userRepository.findById(dispatchAgainstBill.getCreatedBy()); if
		 * (user.isPresent()) {
		 * //dispatchAgainstBillDto.setCreatedByName(user.get().getEmployeeName());
		 * //dispatchAgainstBillDto.setUpdatedByName(user.get().getEmployeeName()); } if
		 * (dispatchAgainstBill.getUpdatedBy() != null) { Optional<User> user1 =
		 * userRepository.findById(dispatchAgainstBill.getUpdatedBy()); if
		 * (user1.isPresent()) { //
		 * dispatchAgainstBill.setUpdatedByName(user.get().getEmployeeName()); } }
		 */
	}

}
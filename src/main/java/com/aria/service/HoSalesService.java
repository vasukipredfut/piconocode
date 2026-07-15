package com.aria.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HoSalesService {

	@Autowired
	HoSalesInvoiceRepository hoSalesInvoiceRepository;

	@Autowired
	HoSalesInvoiceHsnRepository hoSalesInvoiceHsnRepository;

	@Autowired
	HoSalesInvoiceItemsRepository hoSalesInvoiceItemsRepository;

	@Autowired
	DateUtil dateUtil;

	@Autowired
	HOConfigRESTLogRepository hoConfigRESTLogRepository;

	@Autowired
	HoSalesOrderRepository hoSalesOrderRepository;

	@Autowired
	HoSalesOrderHsnRepository hoSalesOrderHsnRepository;

	@Autowired
	HoSalesOrderItemsRepository hoSalesOrderItemsRepository;


	@Autowired
	PurchaseOrderService purchaseOrderService;

	// HO Sales Invoice

	@Transactional
	public ResponseEntity<?> saveHoSalesInvoice(HoSalesInvoiceDTO hoSalesInvoiceDTO, HttpServletRequest request) {

		try {

			Optional<HoSalesInvoice> optional = hoSalesInvoiceRepository.findBy(hoSalesInvoiceDTO.getHoId());
			if(optional.isPresent()){

				HOStatus hoStatus = new HOStatus("400", "HoId  is already exits!!", "failure!!", " HoSalesInvoice Not Created",  request.getServletPath());
				updateLog("HoSalesInvoice", hoStatus, hoSalesInvoiceDTO);
				return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
			}

			HoSalesInvoice invoice = new HoSalesInvoice(hoSalesInvoiceDTO);

			invoice.setRaw_ho_sales_invoice(getHoData(hoSalesInvoiceDTO));

			HoSalesInvoice result = hoSalesInvoiceRepository.save(invoice);

			if (hoSalesInvoiceDTO.getInvoiceItem() != null && !hoSalesInvoiceDTO.getInvoiceItem().isEmpty()) {

				for (HoSalesInvoiceItemsDTO hoSalesInvoiceItemsDTO : hoSalesInvoiceDTO.getInvoiceItem()) {

					HoSalesInvoiceItems invoiceItems = new HoSalesInvoiceItems(hoSalesInvoiceItemsDTO);

					invoiceItems.setInvoiceId(result.getId());
					hoSalesInvoiceItemsRepository.save(invoiceItems);

				}
			}

			if (hoSalesInvoiceDTO.getInvoiceHsninfo() != null && !hoSalesInvoiceDTO.getInvoiceHsninfo().isEmpty()) {

				for (HoSalesInvoiceHsnDTO hoSalesInvoiceHsnDTO : hoSalesInvoiceDTO.getInvoiceHsninfo()) {

					HoSalesInvoiceHsn invoiceHsn = new HoSalesInvoiceHsn(hoSalesInvoiceHsnDTO);

					invoiceHsn.setHoInvoiceId(result.getId());

					hoSalesInvoiceHsnRepository.save(invoiceHsn);

				}
			}

			HOStatus hoStatus = new HOStatus("200", "", "successfully created!!",
					"HO Sales Invoice created Successfully!!!", request.getServletPath(), result.getId());
			updateLog("HoSalesInvoice", hoStatus, hoSalesInvoiceDTO);
			return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
			HOStatus hoStatus = new HOStatus("400", "", "Not created!!", e.getMessage(), request.getServletPath(),
					null);
			updateLog("HoSalesInvoice", hoStatus, hoSalesInvoiceDTO);
			return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);

		}

	}

	public String getHoData(HoSalesInvoiceDTO salesInvoiceDTO) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Map<String, Object> map = objectMapper.convertValue(salesInvoiceDTO,
					new TypeReference<Map<String, Object>>() {
					});
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateLog(String type, HOStatus response, HoSalesInvoiceDTO jsonData) {
		String status = response.getStatus();
		Long typeId = jsonData.getHoId();
		String responseData = getHoStatusData(response);
		String jsonData1 = getHoData(jsonData);
		updateRestLog(type, typeId, status, responseData, jsonData1);
	}

	public String getHoStatusData(HOStatus hoStatus) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Map<String, Object> map = objectMapper.convertValue(hoStatus, new TypeReference<Map<String, Object>>() {
			});
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateRestLog(String type, Long typeId, String status, String response, String jsonData) {

		HOConfigRESTLog hoConfigRESTLog = new HOConfigRESTLog();
		hoConfigRESTLog.setType(type);
		hoConfigRESTLog.setFrom_office("HO");
		hoConfigRESTLog.setTypeId(typeId);
		hoConfigRESTLog.setStatus(status);
		hoConfigRESTLog.setResponse(response);
		hoConfigRESTLog.setJson_data(jsonData);
		hoConfigRESTLog.setCreated_time(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		hoConfigRESTLogRepository.save(hoConfigRESTLog);
	}

	public ResponseEntity<?> getHoSalesInvoiceList(String vendorName, String billNo, String type, String productName,
			String dateFrom, String dateTo, String status, Pageable pageable) throws UsernameNotFoundException {

		Date startDate = null;
		Date endDate = null;
		if (dateFrom != null && dateTo != null) {
			startDate = dateUtil.getStringTODateWk(dateFrom);
			endDate = dateUtil.getStringTODateWk(dateTo);
		} else if (dateFrom != null) {
			startDate = dateUtil.getStringTODateWk(dateFrom);
		} else if (dateTo != null) {
			endDate = dateUtil.getStringTODateWk(dateTo);
		}

		Page<HoSalesInvoice> list = hoSalesInvoiceRepository.findAll(pageable);
		if (!list.toList().isEmpty()) {
			List<HoSalesInvoiceDTO> result = list.toList().stream().map(data -> new HoSalesInvoiceDTO(data))
					.collect(Collectors.toList());
			PageDataDto<HoSalesInvoiceDTO> pageData = new PageDataDto(result, list);
			return new ResponseEntity<PageDataDto<HoSalesInvoiceDTO>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<HoSalesInvoiceDTO>>(new PageDataDto(), HttpStatus.OK);
		}

	}

	public ResponseEntity<?> hoSalesInvoice(String id) throws UsernameNotFoundException {
		Optional<HoSalesInvoice> optional = hoSalesInvoiceRepository.findById(Long.parseLong(id));
		HoSalesInvoiceDTO salesInvoice = new HoSalesInvoiceDTO(optional.get());
		return new ResponseEntity<HoSalesInvoiceDTO>(salesInvoice, HttpStatus.OK);
	}

	// HoSalesOrder

	@Transactional
	public ResponseEntity<?> saveHoSalesOrder(HoSalesOrderDTO hoSalesOrderDTO, HttpServletRequest request) {

		try {


			Optional<HoSalesOrder> optional = hoSalesOrderRepository.findBy(hoSalesOrderDTO.getHoId());

			if(optional.isPresent()){
				HOStatus hoStatus = new HOStatus("400", "HoId  is already exits!!", "failure!!", " HoSalesOrder Not Created",  request.getServletPath());
				updateLog("HoSalesOrder", hoStatus, hoSalesOrderDTO);
				return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);
			}



			HoSalesOrder invoice = new HoSalesOrder(hoSalesOrderDTO);

			invoice.setRaw_ho_sales_Order(getHoData(hoSalesOrderDTO));

			HoSalesOrder result = hoSalesOrderRepository.save(invoice);

			if (hoSalesOrderDTO.getInvoiceItem() != null && !hoSalesOrderDTO.getInvoiceItem().isEmpty()) {

				for (HoSalesOrderItemsDTO hoSalesOrderItemsDTO : hoSalesOrderDTO.getInvoiceItem()) {

					HoSalesOrderItems orderItems = new HoSalesOrderItems(hoSalesOrderItemsDTO);

					orderItems.setOrderId(result.getId());
					hoSalesOrderItemsRepository.save(orderItems);

				}
			}

			if (hoSalesOrderDTO.getInvoiceHsninfo() != null && !hoSalesOrderDTO.getInvoiceHsninfo().isEmpty()) {

				for (HoSalesOrderHsnDTO hoSalesOrderHsnDTO : hoSalesOrderDTO.getInvoiceHsninfo()) {

					HoSalesOrderHsn orderHsn = new HoSalesOrderHsn(hoSalesOrderHsnDTO);

					orderHsn.setOrderId(result.getId());

					hoSalesOrderHsnRepository.save(orderHsn);

				}
			}

			HOStatus hoStatus = new HOStatus("200", "", "successfully created!!",
					"HO Sales Order created Successfully!!!", request.getServletPath(), result.getId());
			updateLog("HoSalesOrder", hoStatus, hoSalesOrderDTO);
			return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);

		} catch (Exception e) {

			HOStatus hoStatus = new HOStatus("400", "", "Not created!!", e.getMessage(), request.getServletPath(),
					null);
			updateLog("HoSalesOrder", hoStatus, hoSalesOrderDTO);
			return new ResponseEntity<HOStatus>(hoStatus, HttpStatus.OK);

		}

	}

	public String getHoData(HoSalesOrderDTO salesInvoiceDTO) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			Map<String, Object> map = objectMapper.convertValue(salesInvoiceDTO,
					new TypeReference<Map<String, Object>>() {
					});
			return objectMapper.writeValueAsString(map);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updateLog(String type, HOStatus response, HoSalesOrderDTO jsonData) {
		String status = response.getStatus();
		Long typeId = jsonData.getHoId();
		String responseData = getHoStatusData(response);
		String jsonData1 = getHoData(jsonData);
		updateRestLog(type, typeId, status, responseData, jsonData1);
	}

	public ResponseEntity<?> hoSalesOrderList(String vendorName, String billNo, String type, String productName,
			String dateFrom, String dateTo, String status, Pageable pageable) throws UsernameNotFoundException {

		Date startDate = null;
		Date endDate = null;
		if (dateFrom != null && dateTo != null) {
			startDate = dateUtil.getStringTODateWk(dateFrom);
			endDate = dateUtil.getStringTODateWk(dateTo);
		} else if (dateFrom != null) {
			startDate = dateUtil.getStringTODateWk(dateFrom);
		} else if (dateTo != null) {
			endDate = dateUtil.getStringTODateWk(dateTo);
		}

		Page<HoSalesOrder> list = hoSalesOrderRepository.findAll(pageable);
		if (!list.toList().isEmpty()) {
			List<HoSalesOrderDTO> result = list.toList().stream().map(data -> new HoSalesOrderDTO(data))
					.collect(Collectors.toList());
			PageDataDto<HoSalesOrderDTO> pageData = new PageDataDto(result, list);
			return new ResponseEntity<PageDataDto<HoSalesOrderDTO>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<PageDataDto<HoSalesOrderDTO>>(new PageDataDto(), HttpStatus.OK);
		}

	}

	public ResponseEntity<?> hoSalesOrder(String id) throws UsernameNotFoundException {
		Optional<HoSalesOrder> optional = hoSalesOrderRepository.findById(Long.parseLong(id));
		HoSalesOrderDTO salesOrder = new HoSalesOrderDTO(optional.get());
		return new ResponseEntity<HoSalesOrderDTO>(salesOrder, HttpStatus.OK);
	}

	public void updateGst(){
		purchaseOrderService.updateGst();
	}

}

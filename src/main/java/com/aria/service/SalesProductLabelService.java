package com.aria.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.stereotype.Service;

import com.aria.dto.BinStockQuantity;
import com.aria.dto.BinStockQuantityDto;
import com.aria.dto.LabelCeilingStockLimitDto;
import com.aria.dto.LabelLocationDto;
import com.aria.dto.LabelPricesDto;
import com.aria.dto.LabelProductNameDto;
import com.aria.dto.MaterialRequestDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.RequestItemDto;
import com.aria.dto.SalesProductLabelDto;
import com.aria.dto.SalesProductLabelOrderDto;
import com.aria.utils.DateUtil;

@Service
public class SalesProductLabelService {

	@Autowired
	private LabelProductGroupRepository labelProductGroupRepository;

	@Autowired
	private DateUtil dateUtil;

	@Autowired
	private LabelProductNameRepository labelProductNameRepository;

	@Autowired
	private LabelLocationRepository labelLocationRepository;

	@Autowired
	private LabelPriceRepository labelPriceRepository;

	@Autowired
	private LabelProductRepository labelProductRepository;

	@Autowired
	private LabelCeilingStockLimitRepository labelCeilingStockLimitRepository;

	@Autowired
	private LabelBranchesRepository labelBranchesRepository;

	@Autowired
	private SalesProductLabelOrderRepository salesProductLabelOrderRepository;

	@Autowired
	private MaterialRequestRepository materialRequestRepository;

	@Autowired
	private BinStockRepository binStockRepository;

	@Autowired
	private RequestItemRepository requestItemRepository;

	@Autowired
	private ProductMasterRepository productMasterRepository;

	@Autowired
	private MaterialIssueRepository materialIssueRepository;

	@Autowired
	private IssueItemsRepository issueItemsRepository;

	@Autowired
	private SalesProductLabelRepository saleProductLabelRepository;

	@Autowired
	private LabelProductValidityRepository labelProductValidityRepository;

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private UserRepository userRepository;

	public ResponseEntity<?> getSalesProductLabelGroup() {
		List<LabelGroup> list = labelProductGroupRepository.findAll();
		if (!list.isEmpty()) {
			return new ResponseEntity<List<LabelGroup>>(list, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<LabelGroup>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> saveLabelFinalProduct(LabelProductNameDto dto, User user) {

		if (dto.getProductGroupId() == null || dto.getProductName() == null || dto.getProductName().isEmpty()
				|| dto.getProductGroupId() == 0L) {
			return new ResponseEntity<Status>(new Status("400", "Label Product Not saved successfully"),
					HttpStatus.BAD_REQUEST);
		}

		LabelProductName model = new LabelProductName(dto);
		model.setCreatedBy(user);
		model.setFlag(1L);
		labelProductNameRepository.save(model);
		return new ResponseEntity<Status>(new Status("200", "Label Product saved successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllLabelProductByGroup(Long productGroupId) {
		List<LabelProductName> list = labelProductNameRepository.findByProductGroupId(productGroupId);
		if (!list.isEmpty()) {
			List<LabelProductNameDto> result = list.stream().map(data -> new LabelProductNameDto(data))
					.collect(Collectors.toList());
			return new ResponseEntity<List<LabelProductNameDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<LabelProductNameDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> saveLabelLocation(LabelLocationDto dto, User user) {
		if (dto.getName() == null || dto.getType() == null || dto.getType().isEmpty() || dto.getName().isEmpty()) {
			return new ResponseEntity<Status>(new Status("400", "Label Location Not saved successfully"),
					HttpStatus.BAD_REQUEST);
		}

		LabelLocation model = new LabelLocation(dto);
		model.setCreatedBy(user);
		model.setCeilingLimit("N");
		labelLocationRepository.save(model);
		return new ResponseEntity<Status>(new Status("200", "Label Location saved successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> savePriceList(List<LabelPricesDto> priceDtoList, User user) {
		List<LabelPrices> list = new ArrayList<>();
		for (LabelPricesDto dto : priceDtoList) {
			LabelPrices model = labelPriceRepository.findByProductIdAndLocationIdAndProductGroupId(dto.getProductId(),
					dto.getLocationId(), dto.getProductGroupId());
			if (model != null) {
				model.setPrice(dto.getPrice());
				model.setPer_ml_price(dto.getPermlprice());
				model.setUpdatedBy(user);
				model.setFromDate(dateUtil.getISTDate());
				model.setEnteredDate(dateUtil.getISTDate());
			} else {
				model = new LabelPrices(dto);
				model.setStatus("A");
				model.setUpdatedBy(user);
			}
			list.add(model);
		}
		labelPriceRepository.saveAll(list);
		return new ResponseEntity<Status>(new Status("200", "Prices saved successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> getLabelPrices(String type, Long productGroupId) {
		List<LabelLocation> lists = labelLocationRepository.findByType(type);
		List<Long> locationIds = new ArrayList<>();
		for (LabelLocation entity : lists) {
			locationIds.add(entity.getId());
		}
		List<LabelPrices> list = labelPriceRepository.findByLocationIdInAndProductGroupId(locationIds, productGroupId);
		if (!list.isEmpty()) {
			List<LabelPricesDto> result = list.stream().map(data -> new LabelPricesDto(data))
					.collect(Collectors.toList());
			return new ResponseEntity<List<LabelPricesDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<LabelPricesDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getLabelLocation(String type) {
		List<LabelLocation> list = labelLocationRepository.findByType(type);
		if (!list.isEmpty()) {
			List<LabelLocationDto> result = list.stream().map(data -> new LabelLocationDto(data))
					.collect(Collectors.toList());
			return new ResponseEntity<List<LabelLocationDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<LabelLocationDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> saveLabelProduct(SalesProductLabelDto dto, User user) {
		if (dto.getProductId() != null) {
			Optional<SalesProductLabel> optional = labelProductRepository.findByProductIdAndBranchId(dto.getProductId(),
					dto.getBranchId());
			if (optional.isPresent()) {
				return new ResponseEntity<Status>(new Status("400", "Sales Product Film Already Exists"),
						HttpStatus.OK);
			}
		}
		SalesProductLabel label = new SalesProductLabel(dto);
		label.setUpdatedBy(user);
		label.setCreatedDate(dateUtil.getISTDate());
		labelProductRepository.save(label);
		return new ResponseEntity<Status>(new Status("200", "Sales Product Film saved successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateLabelProduct(SalesProductLabelDto dto, User user) {
		SalesProductLabel label = new SalesProductLabel(dto);
		label.setUpdatedBy(user);
		labelProductRepository.save(label);
		return new ResponseEntity<Status>(new Status("200", "Sales Product Film Updated successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> getLabelProduct(Long productId, String productName, String productGroup, String branch,
			String finalProductName, String fromDate, String toDate, Pageable pageable) {

		Date start = null;
		Date end = null;
		if(fromDate != null){
			start = dateUtil.getStringTODateWk(fromDate);
		}
		if(toDate != null){
			end = dateUtil.getStringTODateWk(toDate);
		}

		Page<SalesProductLabel> list = labelProductRepository.getLabelProduct(productId, productName, productGroup,
				branch, finalProductName, start, end , pageable);
		List<SalesProductLabelDto> result = list.stream().map(data -> {
			SalesProductLabelDto dto = new SalesProductLabelDto(data);
			if (data.getBranchId() != null) {
				Optional<LabelBranches> branchs = labelBranchesRepository.findById(data.getBranchId());
				dto.setBranchName(branchs.get().getName());
			}
			if (data.getFinalProductId() != null) {
				System.out.println("data - " + data.getFinalProductId());
				Optional<LabelProductName> product = labelProductNameRepository.findById(data.getFinalProductId());
				dto.setFinalProductName(product.get().getProductName());
			}
			if (data.getProductGroupId() != null) {
				Optional<LabelGroup> productGrp = labelProductGroupRepository.findById(data.getProductGroupId());
				dto.setProductGroupName(productGrp.get().getName());
			}
			return dto;
		}).collect(Collectors.toList());
		PageDataDto<SalesProductLabelDto> pageData = new PageDataDto<>(result, list);
		return new ResponseEntity<PageDataDto<SalesProductLabelDto>>(pageData, HttpStatus.OK);
	}

	/* ---------- Ceiling Stock ---------- */

	public ResponseEntity<?> saveCeilingStockLimit(LabelCeilingStockLimitDto dto, User user) {
		LabelCeilingStockLimit ceilingStock = labelCeilingStockLimitRepository.findByBranchIdAndCapacityAndProductGroup(
				dto.getBranchId(), dto.getCapacity(), dto.getProductGroupId());
		if (ceilingStock != null) {
			return new ResponseEntity<Status>(new Status("400", "Ceiling Stock limit already Exists!!!"),
					HttpStatus.BAD_REQUEST);
		}
		LabelCeilingStockLimit model = new LabelCeilingStockLimit(dto);
		model.setUpdatedBy(user);
		model.setStatus("A");
		model.setDate(LocalDate.now());
		labelCeilingStockLimitRepository.save(model);
		return new ResponseEntity<Status>(new Status("200", "Ceiling Stock limit saved successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> updateCeilingStockLimit(LabelCeilingStockLimitDto dto, User user) {
		LabelCeilingStockLimit ceilingStock = labelCeilingStockLimitRepository.findByBranchIdAndCapacityAndProductGroup(
				dto.getBranchId(), dto.getCapacity(), dto.getProductGroupId());
		if (ceilingStock == null) {
			return new ResponseEntity<Status>(new Status("404", "Ceiling Stock limit doesn't Exists!!!"),
					HttpStatus.NOT_FOUND);
		}
		ceilingStock.setCeilingLimit(dto.getCeilingLimit());
		ceilingStock.setUpdatedBy(user);
		labelCeilingStockLimitRepository.save(ceilingStock);
		return new ResponseEntity<Status>(new Status("200", "Ceiling Stock limit Updated successfully!!!"),
				HttpStatus.OK);
	}

	public ResponseEntity<?> getLabelCeilingStockLimitById(Long id) {
		Optional<LabelCeilingStockLimit> stockLimit = labelCeilingStockLimitRepository.findById(id);
		if (stockLimit.isPresent()) {
			LabelCeilingStockLimitDto dto = new LabelCeilingStockLimitDto(stockLimit.get());
			if (stockLimit.get().getProductGroupId() != null) {
				Optional<LabelGroup> productGroup = labelProductGroupRepository
						.findById(stockLimit.get().getProductGroupId());
				productGroup.ifPresent(data -> dto.setProductGroupName(data.getName()));
			}
			return new ResponseEntity<LabelCeilingStockLimitDto>(dto, HttpStatus.OK);
		}
		return ResponseEntity.badRequest().body("Ceiling stock not found");
	}

	public ResponseEntity<?> getLabelCeilingStockLimitByFilter(String capacity, String productGroup, String branch,
			Pageable pageable) {
		Page<LabelCeilingStockLimit> list = labelCeilingStockLimitRepository.getLabelProductFilmList(capacity,
				productGroup, branch, pageable);
		if (list.isEmpty()) {
			return new ResponseEntity<>(list.toList(), HttpStatus.OK);
		}
		List<LabelCeilingStockLimitDto> result = list.getContent().stream().map(val -> {
			LabelCeilingStockLimitDto dto = new LabelCeilingStockLimitDto(val);
//			if (val.getBranchId() != null) {
//				Optional<SalesProductBranchesEntity> bran = salesProductBranchesRepository.findById(val.getBranchId());
//				bran.ifPresent(data -> dto.setBranchName(data.getName()));
//			}
			if (val.getProductGroupId() != null) {
				Optional<LabelGroup> group = labelProductGroupRepository.findById(val.getProductGroupId());
				group.ifPresent(data -> dto.setProductGroupName(data.getName()));
			}
			return dto;
		}).collect(Collectors.toList());

		PageDataDto<LabelCeilingStockLimitDto> pageData = new PageDataDto<>(result, list);
		return new ResponseEntity<>(pageData, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteSalesCeiling(Long id) {
		Optional<LabelCeilingStockLimit> model = labelCeilingStockLimitRepository.findById(id);
		if (model.isPresent()) {
			labelCeilingStockLimitRepository.deleteById(id);
			return new ResponseEntity<Status>(new Status("200", "Deleted successfully!!!"), HttpStatus.OK);
		}
		return new ResponseEntity<Status>(new Status("404", "Ceiling stock limit doesn't Exists!!!"),
				HttpStatus.NOT_FOUND);
	}

	/* --------------label Order-------------- */

	public ResponseEntity<?> saveLabelOrders(List<SalesProductLabelOrderDto> dtos, User user) {
		List<SalesProductLabelOrder> labelOrders = new ArrayList<>();

		for (SalesProductLabelOrderDto dto : dtos) {
			SalesProductLabelOrder labelOrder = new SalesProductLabelOrder(dto);
			labelOrder.setOrderDate(dateUtil.getISTDate());
			labelOrder.setOrderedBy(user.getId());
			labelOrder.setOrderBranch(user.getBranch_id());
			Optional<SalesProductLabel> optional = saleProductLabelRepository.findById(dto.getLabelId());
			LabelProductValidity validity = labelProductValidityRepository
					.findByProductGroupIdAndLocationTypeAndLocationId(optional.get().getProductGroupId(),
							dto.getLocation(), dto.getSubLocationId());
			if (validity != null) {
				LocalDate bestBefore = LocalDate.now().plusMonths(validity.getMonth());
				System.out.println(bestBefore.with(TemporalAdjusters.lastDayOfMonth()));
				labelOrder.setBestBefore(bestBefore.with(TemporalAdjusters.lastDayOfMonth()));
			}
			if (dto.getCompletedQty() != null) {
				labelOrder.setBalance(dto.getOrderQty() - dto.getCompletedQty());
			}
			labelOrder.setMrnStatus("P");
			saveMaterialFlowRequest(labelOrder, user);
			labelOrders.add(labelOrder);
		}

		salesProductLabelOrderRepository.saveAll(labelOrders);

		return new ResponseEntity<Status>(new Status("200", "Label orders saved successfully!!!"), HttpStatus.OK);
	}

	public void saveMaterialFlowRequest(SalesProductLabelOrder labelOrder, User loggedUser) {
		MaterialRequest materialRequest = createMaterialRequest(labelOrder, loggedUser);
		labelOrder.setMrnNo(materialRequest.getId());
		salesProductLabelOrderRepository.save(labelOrder);
		createRequestItem(materialRequest, labelOrder);
		updateMaterialIssue(materialRequest.getId());
	}

	private MaterialRequest createMaterialRequest(SalesProductLabelOrder res, User loggedUser) {
		MaterialRequestDto materialRequestDto = new MaterialRequestDto();
		materialRequestDto.setRequestFromFirmId(loggedUser.getOrg_id());
		materialRequestDto.setRequestName("Label Order");
		materialRequestDto.setRequestDescription("Label Order");
		materialRequestDto.setRequestById(res.getOrderedBy());
		materialRequestDto.setRequestDate(dateUtil.getDateTOStringWk(res.getOrderDate()));
		materialRequestDto.setRequestFromBranchId(res.getOrderBranch());
		materialRequestDto.setRequestFromDepartmentId(loggedUser.getDepartment().getId());
		materialRequestDto.setCreated_date(dateUtil.getDateToLocalDateTime(res.getOrderDate()));
		materialRequestDto.setCreatedBy(res.getOrderedBy());
		materialRequestDto.setStatus("Request Sent");
		MaterialRequest materialRequest = new MaterialRequest(materialRequestDto);
		materialRequest.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		//materialRequest.setRequestDate(Date.from(res.getOrderDate().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		materialRequest.setRequestDate(res.getOrderDate());
		return materialRequestRepository.save(materialRequest);
	}

	public void updateMaterialIssue(Long materialRequestId) {

		Optional<MaterialRequest> optionalMaterialFlowRequest = materialRequestRepository.findById(materialRequestId);

		if (optionalMaterialFlowRequest.isPresent()) {
			MaterialRequest materialRequest = optionalMaterialFlowRequest.get();
			MaterialIssue materialIssue = new MaterialIssue();
			materialIssue.setRequestDate(materialRequest.getRequestDate());
			materialIssue.setRequestBranch(materialRequest.getBranch());
			materialIssue.setRequestDepartment(materialRequest.getDepartment());
			materialIssue.setOrganisation(materialRequest.getOrganisation());
			materialIssue.setRequestDescription(materialRequest.getRequestDescription());
			materialIssue.setRequestId(materialRequest.getId());
			materialIssue.setRequestName(materialRequest.getRequestName());
			materialIssue.setCreated_date(materialRequest.getCreated_date());
			materialIssue.setCreatedBy(materialRequest.getCreatedBy());
			materialIssue.setStatus("Pending");
			Branch branch = new Branch();
			branch.setId(2L);
			materialIssue.setIssueBranch(branch);
			DepartmentDetails dept = new DepartmentDetails();
			dept.setId(32L);
			materialIssue.setIssueDepartment(dept);
			List<RequestItem> requestItems = requestItemRepository.findAllByRequestId(materialRequest.getId());

			MaterialIssue result = materialIssueRepository.save(materialIssue);

			for (RequestItem requestItem : requestItems) {
				IssueItems issueItems = new IssueItems();
				issueItems.setIssueItemId(result.getId());
				issueItems.setCreated_date(requestItem.getCreated_date());
				issueItems.setActive(0);
				issueItems.setSelectLot(requestItem.getSelectLot());
				issueItems.setBranchId(requestItem.getIssueBranch().getId());
				issueItems.setDeptId(requestItem.getIssueDepartment().getId());
				issueItems.setItemName(requestItem.getItemName());
				issueItems.setItemId(requestItem.getItemId());
				issueItems.setRequestedQuantity(requestItem.getStockRequired());
				issueItemsRepository.save(issueItems);
			}

		}

	}

	private void createRequestItem(MaterialRequest materialRequest, SalesProductLabelOrder labelOrder) {

		RequestItemDto requestItemDto = new RequestItemDto();
		requestItemDto.setRequestId(materialRequest.getId());
		requestItemDto.setIssueBranchId(2L);
		requestItemDto.setIssueDepartmentId(32L);
		requestItemDto.setSelectLot(String.valueOf(labelOrder.getLot()));
		requestItemDto.setStockQuantity(String.valueOf(labelOrder.getOrderQty()));
		requestItemDto.setStockRequired(String.valueOf(labelOrder.getOrderQty()));
		requestItemDto.setActive(materialRequest.getActive());
		requestItemDto.setCreatedBy(materialRequest.getCreatedBy());
		requestItemDto.setCreated_date(materialRequest.getRequestDate());
		requestItemDto.setUpdatedBy(materialRequest.getUpdatedBy());
		requestItemDto.setUpdated_date(materialRequest.getRequestDate());
		RequestItem requestItem = new RequestItem(requestItemDto);

		Optional<SalesProductLabel> label = saleProductLabelRepository.findById(labelOrder.getLabelId().getId());
		Optional<ProductMaster> product = productMasterRepository.findById(label.get().getProductId());
		product.ifPresent(productMaster -> {
			requestItem.setItemId(productMaster.getId());
			requestItem.setItemName(productMaster.getProductName());
		});
		requestItemRepository.save(requestItem);
	}

	public ResponseEntity<?> updateLabelPendingQty(Long mrnNo, Double completedQty) {
		Optional<SalesProductLabelOrder> optional = salesProductLabelOrderRepository.findByMrnNo(mrnNo);
		if (optional != null) {
			SalesProductLabelOrder label = optional.get();
			label.setPrintedQty(label.getPrintedQty() + completedQty);
			Double pendingQty = label.getBalance() - completedQty;
			label.setBalance(pendingQty);
			if (pendingQty == 0) {
				label.setMrnStatus("C");
			}
			salesProductLabelOrderRepository.save(label);
			return new ResponseEntity<Status>(new Status("200", "Label orders pendingQty updated successfully!!!"),
					HttpStatus.OK);

		}
		return new ResponseEntity<Status>(new Status("404", "RequestId doesn't Exists!!!"), HttpStatus.NOT_FOUND);
	}

	public ResponseEntity<?> getAll(Pageable pageable) {
		Page<SalesProductLabelOrder> label = salesProductLabelOrderRepository.findAll(pageable);
		if (label.isEmpty()) {
			return new ResponseEntity<>(label.toList(), HttpStatus.OK);
		}

		List<SalesProductLabelOrderDto> result = label.getContent().stream().map(data -> {
			SalesProductLabelOrderDto dto = new SalesProductLabelOrderDto(data);
			if(dto.getOrderedBy() != null){
				Optional<User> userOptional = userRepository.findById(dto.getOrderedBy());
				 if(userOptional.get().getEmployeename() != null ) {
					 dto.setOrderedByName(userOptional.get().getEmployeename());
				 }
			}
			if (data.getSubLocationId() != null) {
				Optional<LabelLocation> loc = labelLocationRepository.findById(data.getSubLocationId());
				loc.ifPresent(location -> dto.setSubLocationName(location.getName()));
			}
			return dto;
		}).collect(Collectors.toList());
		PageDataDto<SalesProductLabelOrderDto> pageData = new PageDataDto<>(result, label);
		return new ResponseEntity<>(pageData, HttpStatus.OK);

	}

	public ResponseEntity<?> getLabelOrderById(Long id) {
		Optional<SalesProductLabelOrder> label = salesProductLabelOrderRepository.findById(id);
		if (label.isPresent()) {
			SalesProductLabelOrderDto dto = new SalesProductLabelOrderDto(label.get());
			if(dto.getOrderedBy() != null){
				Optional<User> userOptional = userRepository.findById(dto.getOrderedBy());
				if(userOptional.get().getEmployeename() != null ) {
					dto.setOrderedByName(userOptional.get().getEmployeename());
				}
			}
			if (label.get().getSubLocationId() != null) {
				Optional<LabelLocation> loc = labelLocationRepository.findById(label.get().getSubLocationId());
				loc.ifPresent(location -> dto.setSubLocationName(location.getName()));
			}
			return new ResponseEntity<SalesProductLabelOrderDto>(dto, HttpStatus.OK);
		}
		return ResponseEntity.badRequest().body("label not found");
	}

	public ResponseEntity<?> getLabelMrpDetails(long locationId, Long productGroupId, Long productId) {
		LabelPrices price = labelPriceRepository.findByProductIdAndLocationIdAndProductGroupId(productId, locationId,
				productGroupId);
		if (price != null) {
			return new ResponseEntity<LabelPrices>(price, HttpStatus.OK);
		} else {
			return ResponseEntity.badRequest().body("Item not found");
		}
	}

	public ResponseEntity<?> getLabelStockInPouch(Long branch, Long dept, Long productId, Long lot) {
		List<BinStockQuantity> list = binStockRepository.getStockInPouch(branch, dept, productId, lot);
		if (!list.isEmpty()) {
			List<BinStockQuantityDto> result = list.stream().map(data -> {
				BinStockQuantityDto binStockDto = new BinStockQuantityDto();
				binStockDto.setQuantity(data.getQuantity());
				binStockDto.setDept(data.getDept());
				binStockDto.setBranch(data.getBranch());
				return binStockDto;
			}).collect(Collectors.toList());
			return new ResponseEntity<List<BinStockQuantityDto>>(result, HttpStatus.OK);
		}
		return new ResponseEntity<List<BinStockQuantityDto>>(new ArrayList<>(), HttpStatus.OK);
	}

	public ResponseEntity<?> getLabelPendingStock(String location, Long subLocationId, Long labelId, Long branch,
			Long lot) {
		SalesProductLabel label = new SalesProductLabel();
		label.setId(labelId);
		List<SalesProductLabelOrder> list = salesProductLabelOrderRepository
				.findByLabelIdAndLocationAndSubLocationIdAndOrderBranchAndLot(label, location, subLocationId, branch,
						lot);
		if (!list.isEmpty()) {
			List<SalesProductLabelOrderDto> result = list.stream().map(data -> new SalesProductLabelOrderDto(data))
					.collect(Collectors.toList());
			return new ResponseEntity<List<SalesProductLabelOrderDto>>(result, HttpStatus.OK);
		} else
			return new ResponseEntity<List<SalesProductLabelOrderDto>>(new ArrayList<>(), HttpStatus.OK);

	}

	public ResponseEntity<?> getLabelOrderByFilter(String status, String orderDate, String pageFlag,
			Pageable pageable) {
		Date oDate = null;
		if (java.util.Objects.nonNull(orderDate)) {
			oDate = dateUtil.getFilterDate(orderDate);
		}

		if (pageFlag != null && pageFlag.equalsIgnoreCase("N")) {
			List<SalesProductLabelOrder> list = salesProductLabelOrderRepository.findByMrnStatusAndOrderDate(status,
					oDate);
			List<SalesProductLabelOrderDto> result = list.stream().map(data -> {
				SalesProductLabelOrderDto d = new SalesProductLabelOrderDto(data);
				if(d.getOrderedBy() != null){
					Optional<User> userOptional = userRepository.findById(d.getOrderedBy());
					if(userOptional.get().getEmployeename() != null ) {
						d.setOrderedByName(userOptional.get().getEmployeename());
					}
				}
				Optional<Branch> branch = branchRepository.findById(data.getOrderBranch());
				d.setBranchName(branch.get().getBranchName());
				updateAltValue(d);
				return d;
			}).collect(Collectors.toList());
			return new ResponseEntity<List<SalesProductLabelOrderDto>>(result, HttpStatus.OK);
		}

		Page<SalesProductLabelOrder> pages = salesProductLabelOrderRepository.findByFilter(status, oDate, pageable);
		if (!pages.isEmpty()) {
			List<SalesProductLabelOrderDto> result = pages.toList().stream().map(data -> {
				SalesProductLabelOrderDto dto = new SalesProductLabelOrderDto(data);
				if(dto.getOrderedBy() != null){
					Optional<User> userOptional = userRepository.findById(dto.getOrderedBy());
					if(userOptional.get().getEmployeename() != null ) {
						dto.setOrderedByName(userOptional.get().getEmployeename());
					}
				}
				Optional<Branch> branch = branchRepository.findById(data.getOrderBranch());
				dto.setBranchName(branch.get().getBranchName());
				updateAltValue(dto);
				return dto;
			}).collect(Collectors.toList());
			PageDataDto<SalesProductLabelOrderDto> pageData = new PageDataDto<>(result, pages);
			return new ResponseEntity<>(pageData, HttpStatus.OK);
		}

		return new ResponseEntity<>(pages.toList(), HttpStatus.OK);
	}


	public void updateAltValue(SalesProductLabelOrderDto dto)
	{
		if(dto.getLabelId() != null) {
		Optional<SalesProductLabel> salesProductLabel = saleProductLabelRepository.findById(dto.getLabelId());
		if(salesProductLabel.isPresent()){
			Optional<ProductMaster> optionalProductMaster = productMasterRepository.findById(salesProductLabel.get().getProductId());
			if(optionalProductMaster.isPresent()){
				dto.setAltValue(optionalProductMaster.get().getWhere2());
			}
		}
		}
	}

}

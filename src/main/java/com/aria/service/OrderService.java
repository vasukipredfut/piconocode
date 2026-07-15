package com.aria.service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.BinStockLogRepository;
import com.aria.dao.BinStockRepository;
import com.aria.dao.DpatchBillOrderItemsRepository;
import com.aria.dao.DpatchDespatchRepository;
import com.aria.dao.OrderRepository;
import com.aria.dao.ProductMappingRepository;
import com.aria.dto.BinStockDto;

import com.aria.dto.DpatchBillorderItemsDto;
import com.aria.dto.OrderDto;
import com.aria.dto.OrderResponse;
import com.aria.dto.PageDataDto;
import com.aria.dto.productPieceList;
import com.aria.dto.productPieceQtyDTO;
import com.aria.model.BinStock;

import com.aria.model.DpatchBillorderItems;
import com.aria.model.Order;
import com.aria.model.ProductMapping;
import com.aria.model.ProductMaster;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class OrderService {

	@Autowired
	DateUtil dateUtil;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private DpatchBillOrderItemsRepository dpatchBillOrderItemsRepository;

	@Autowired
	private ProductMappingRepository productMappingRepository;
	@Autowired
	private BinStockRepository binStockRepository;
	 @Autowired
	    public BinStockLogRepository binStockLogRepository;

	public ResponseEntity<?> createOrder(OrderDto orderDto, User user) {

		if (orderDto.getOrderNumber() != null) {
			Optional<Order> existingOrder = orderRepository.findByOrderNumberANDBillDate(orderDto.getOrderNumber(), dateUtil.getStringTODateWk(orderDto.getBillDate()));
			if (existingOrder.isPresent()) {
				return ResponseEntity.badRequest().body(new Status("400", "Order ID Already Exists!!!"));
			}
		}

		if (orderDto.getBillNo() != null && orderDto.getBillNo() != "") {
			Optional<Order> existingOrder = orderRepository.findByBillNorANDBillDate(orderDto.getBillNo(), dateUtil.getStringTODateWk(orderDto.getBillDate()));
			if (existingOrder.isPresent()) {
				return ResponseEntity.badRequest().body(new Status("400", "Bill Number Already Exists!!!"));
			}
		}

		Order order = new Order(orderDto);
		order.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		order.setOrderStatus("pending");

		order.setFirmId(user.getOrg_id());
		order.setBranchId(user.getBranch_id());
		order.setCreatedBy(user);

		if (orderDto.getBillDate() != null) {
			order.setBillDate(dateUtil.getStringTODateWk(orderDto.getBillDate()));
		}
		orderRepository.save(order);

		if (order != null && !orderDto.getDpatchBillorderItems().isEmpty()) {
			for (DpatchBillorderItemsDto dto : orderDto.getDpatchBillorderItems()) {
				DpatchBillorderItems item = new DpatchBillorderItems();
				item.setOrderId(order.getId());
				if (dto.getProductId() != null) {
					ProductMaster prod = new ProductMaster();
					prod.setId(dto.getProductId());
					item.setProduct(prod);
				}
				item.setLot(dto.getLot());
				item.setPieceQuantity(dto.getPieceQuantity());
				item.setBoxQuantity(dto.getBoxQuantity());
				dpatchBillOrderItemsRepository.save(item);
			}

		}
		if (orderDto.getOrderNumber() != null) {
			return ResponseEntity.ok(new Status("200", "Order Created Successfully..."));
		} else {
			return ResponseEntity.ok(new Status("200", "Bill Created Successfully..."));
		}
	}

	public ResponseEntity<?> getAllOrdersByStatus(Long firmId, Long branchId,  Long orderNumber , String billNo , String billDate , String vendorName ,  String status, Long type, Pageable pageable) {
		Date billDateCon = null;
		if(billDate != null ){
			billDateCon = dateUtil.getStringTODateWk(billDate);
		}

		Page<OrderResponse> page = orderRepository.findAllByStatus(firmId, branchId, orderNumber, billNo, billDateCon , vendorName , status, type, pageable);
		List<OrderResponse> result = new ArrayList<>();
		for (OrderResponse orders : page.getContent()) {
			result.add(orders);
		}
		PageDataDto<OrderResponse> pageData = new PageDataDto<>(result, page);
		return new ResponseEntity<PageDataDto<OrderResponse>>(pageData, HttpStatus.OK);
	}

	public ResponseEntity<?> changeStatusOfOrder(Long orderId) throws ParseException {
		Optional<Order> order = orderRepository.findById(orderId);
		if (order.isPresent()) {
			Order existingOrder = order.get();
			existingOrder.setOrderStatus("completed");
			if (orderRepository.save(existingOrder) != null)
				return new ResponseEntity<Object>(new Status("200", "Order Status Changed..."), HttpStatus.OK);
		}
		return new ResponseEntity<Object>(new Status("200", "Order Not Exist..."), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllAgainstOrder(Long firmId, Long branchId,  Long orderNumber , String billDate , String vendorName , String status , Pageable pageable) {

		Date billDateCon = null;
		if(billDate != null ){
			billDateCon = dateUtil.getStringTODateWk(billDate);
		}

		Page<OrderResponse> page = orderRepository.getAllAgainstOrder(firmId , branchId, orderNumber , billDateCon, vendorName, status, pageable);
		List<OrderResponse> result = new ArrayList<>();
		for (OrderResponse orders : page.getContent()) {
			result.add(orders);
		}
		PageDataDto<OrderResponse> pageData = new PageDataDto<>(result, page);
		return new ResponseEntity<PageDataDto<OrderResponse>>(pageData, HttpStatus.OK);
	}

	public ResponseEntity<?> getAllorderAgainstBill(Long firmId, Long branchId,  String billNumber , String billDate , String vendorName , String status , Pageable pageable) {
		Date billDateCon = null;
		if(billDate != null ){
			billDateCon = dateUtil.getStringTODateWk(billDate);
		}

		Page<OrderResponse> page = orderRepository.getAllorderAgainstBill(firmId , branchId, billNumber , billDateCon, vendorName, status, pageable);
		List<OrderResponse> result = new ArrayList<>();
		for (OrderResponse orders : page.getContent()) {
			result.add(orders);
		}
		PageDataDto<OrderResponse> pageData = new PageDataDto<>(result, page);
		return new ResponseEntity<PageDataDto<OrderResponse>>(pageData, HttpStatus.OK);
	}

//	public ResponseEntity<?> getAllCompletedOrder(Pageable pageable) {
//		Page<OrderResponse> page = orderRepository.findCompletedOrders(pageable);
//		List<OrderResponse> result = new ArrayList<>();
//		for (OrderResponse orders : page.getContent()) {
//			result.add(orders);
//		}
//		PageDataDto<OrderResponse> pageData = new PageDataDto<>(result, page);
//		return new ResponseEntity<PageDataDto<OrderResponse>>(pageData, HttpStatus.OK);

//		List<OrderResponse> completedOrders = orderRepository.findCompletedOrders();
//		if (!completedOrders.isEmpty())
//			return new ResponseEntity<Object>(completedOrders, HttpStatus.OK);
//		return new ResponseEntity<Object>(completedOrders, HttpStatus.OK);
//	}

//	public ResponseEntity<?> getOrderDetails(Long orderId) {
//		Optional<Order> order = orderRepository.findById(orderId);
//		if (order.isPresent()) {
//			if (order.get().getActive() == 0) {
//				OrderDto orderResponse = new OrderDto();
//				Optional<Vendor> vendor = vendorRepository.findById(order.get().getVendorId());
//				orderResponse.setVendorName(vendor.get().getName());
//				orderResponse.setBillDate(order.get().getCreatedDate());
//				orderResponse.setDelieveryDate(order.get().getBillDate());
//
//				if (order.get().getOrderNumber() != null)
//					orderResponse.setOrderNumber(order.get().getOrderNumber());
//				else
//					orderResponse.setBillNumber(order.get().getBillNo());
//
//				List<DpatchBillorderItems> billOrderItems = dpatchBillOrderItemsRepository
//						.findByDpatch_bill_order_ref_id(order.get().getId());
//				List<Long> productItemIds = new ArrayList<Long>();
//				List<SingleProductForOrderResponseDto> prod = new ArrayList<SingleProductForOrderResponseDto>();
//				IntStream.range(0, billOrderItems.size()).forEach(i -> {
//					SingleProductForOrderResponseDto p = new SingleProductForOrderResponseDto();
//					p.setQuantity(billOrderItems.get(i).getPiecequantity());
//					p.setLot(billOrderItems.get(i).getLot_ref_id());
//					p.setBoxQuantity(billOrderItems.get(i).getBoxquantity());
//					productItemIds.add(billOrderItems.get(i).getProduct_ref_id());
//					orderResponse.setBoxQuantity(billOrderItems.get(i).getBoxquantity());
//					orderResponse.setPieceQuantity(billOrderItems.get(i).getPiecequantity());
//					prod.add(p);
//				});
//				orderResponse.setProducts(prod);
//				List<ProductMaster> products = productMasterRepository.findAllById(productItemIds);
//
//				IntStream.range(0, products.size()).forEach(i -> {
//					orderResponse.getProducts().get(i).setProductName(products.get(i).getProductName());
//					orderResponse.getProducts().get(i).setProductId(products.get(i).getId());
//				});
//
//				return new ResponseEntity<Object>(orderResponse, HttpStatus.OK);
//
//			}
//		}
//		return new ResponseEntity<Object>(new ArrayList<Object>(), HttpStatus.OK);
//	}

	public ResponseEntity<?> deleteOrderDetails(Long orderId) {
		Optional<Order> order = orderRepository.findById(orderId);
		if (order.isPresent()) {
			order.get().setActive(1);
			orderRepository.save(order.get());
			return new ResponseEntity<Object>(new Status("200", "Order is deleted successfully..."), HttpStatus.OK);
		}
		return new ResponseEntity<Object>(new Status("200", "Order is not found..."), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllProductsAgainstOrder() {
		List<ProductMapping> product = productMappingRepository.findAllByCateId(1);
		if (!product.isEmpty()) {
			List<ProductMaster> response = product.stream().map(prod -> prod.getProduct_ref_id())
					.collect(Collectors.toList());
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
		return new ResponseEntity<Object>(product, HttpStatus.OK);
	}

	public ResponseEntity<?> getAllProductsAgainstBill() {
		List<ProductMapping> product = productMappingRepository.findAllByCateId(3);
		if (!product.isEmpty()) {
			List<ProductMaster> response = product.stream().map(prod -> prod.getProduct_ref_id())
					.collect(Collectors.toList());
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
		return new ResponseEntity<Object>(product, HttpStatus.OK);
	}

	public ResponseEntity<?> getPieceQtyList(Long productId) {
		List<productPieceList> pieceList = orderRepository.getPieceQtyList(productId);
		List<productPieceQtyDTO> list = pieceList.stream().map(data -> new productPieceQtyDTO(data))
				.collect(Collectors.toList());

		return new ResponseEntity<List<productPieceQtyDTO>>(list, HttpStatus.OK);
	}

	public Optional<Order> getOrderById(Long orderId) {
		return orderRepository.findById(orderId);
	}

	public ResponseEntity<?> getOrderDetailsById(Long id) {
		Optional<Order> orderOptional = orderRepository.findById(id);
		if (orderOptional.isPresent()) {
			Order order = orderOptional.get();
			OrderDto dto = generateOrderDto(order);
			return new ResponseEntity<>(dto, HttpStatus.OK);
		} else {
			return ResponseEntity.badRequest().body("Order not found with id: " + id);
		}
	}

	public OrderDto generateOrderDto(Order order) {
		OrderDto dto = new OrderDto(order);
		List<DpatchBillorderItems> dispatchItems = dpatchBillOrderItemsRepository
				.findByDpatch_bill_order_ref_id(order.getId());
		List<BinStock> binDetails = binStockRepository.findByDispatchDetailss(order.getId());
		List<DpatchBillorderItemsDto> dispatchItemDtos = new ArrayList<>();
		for (DpatchBillorderItems dispatchItem : dispatchItems) {
			DpatchBillorderItemsDto dispatchItemDto = new DpatchBillorderItemsDto(dispatchItem);			
			 List<BinStockDto> binStockDtos = new ArrayList<>();
			if(!binDetails.isEmpty())
			{
				for(BinStock bin : binDetails) {
					if(bin.getRefId().equals(dispatchItemDto.getProductId()) && bin.getLot().longValue()==dispatchItemDto.getLot()) {
					BinStockDto binDto = new BinStockDto(bin);
					if(bin.getStkQty()<=0.0) {
//						binStockRepository.findByDispatchDetails(bin.getBatchNo(),bin.getBinName(),bin.getRefId(),bin.getLTrnNo());
						binDto.setStkQty(binStockRepository.findByDispatchDetails(bin.getBatchNo(),bin.getBinName(),bin.getRefId(),bin.getLTrnNo()));
					}
			            binStockDtos.add(binDto);
					}
					}
			}
			
			dispatchItemDto.setBinStockDto(binStockDtos);
			dispatchItemDtos.add(dispatchItemDto);
		}

		dto.setDpatchBillorderItems(dispatchItemDtos);

		return dto;
	}

}

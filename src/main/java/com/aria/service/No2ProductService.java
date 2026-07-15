package com.aria.service;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dto.BrokerDto;
import com.aria.dto.No2ProductsDto;
import com.aria.dto.No2ProductsPartyDto;
import com.aria.dto.No2ProductsPriceDto;
import com.aria.dto.No2RatehistoryListDto;
import com.aria.dto.No2billitemsDto;
import com.aria.dto.No2billsDto;
import com.aria.dto.No2billsUpdateDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.PartyPriceDto;
import com.aria.dto.ProductPriceListDto;
import com.aria.dto.RatehistoryGroupingDto;
import com.aria.dto.RatehistoryPartyGroupingDto;
import com.aria.dto.RatehistoryProductDto;
import com.aria.dto.StockbyProductDTO;
import com.aria.model.BinStock;
import com.aria.model.Broker;
import com.aria.model.GstTaxCategory;
import com.aria.model.No2Products;
import com.aria.model.No2ProductsParty;
import com.aria.model.No2ProductsPartyMapping;
import com.aria.model.No2Ratehistory;
import com.aria.model.No2billitems;
import com.aria.model.No2bills;
import com.aria.model.ProductMaster;
import com.aria.model.ProductPartyPrice;
import com.aria.model.RatehistoryGrouping;
import com.aria.model.Status;
import com.aria.model.StockbyProduct;
import com.aria.model.User;
import com.aria.model.Vendor;
import com.aria.utils.DateUtil;

@Service
public class No2ProductService {

	private static final DecimalFormat decfor = new DecimalFormat("0.0000");

	@Autowired
	No2ProductsRepository no2ProductsRepository;

	@Autowired
	No2ProductsPartyMappingRepository no2ProductsPartyMappingRepository;

	@Autowired
	No2ProductsPartyRepository no2ProductsPartyRepository;

	@Autowired
	No2RateHistoryRepository no2RateHistoryRepository;

	@Autowired
	No2billitemsRepository no2billitemsRepository;

	@Autowired
	No2BillsRepository no2BillsRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	BrokerRepository brokerRepository;

	@Autowired
	BinStockRepository binStockRepository;

	@Autowired
	ProductMasterRepository productMasterRepository;

	@Autowired
	GstTaxCategoryRepository gstTaxCategoryRepository;

	@Autowired
	DateUtil dateUtil;
	@Autowired
	VendorRepository vendorRepository;

	/* Product Services */
	public ResponseEntity<?> createproduct(No2ProductsDto dto, User user) {

		Optional<No2Products> existingProduct = no2ProductsRepository.findByProductIdANDType(dto.getProductId(), dto.getProductType());
		if (existingProduct.isPresent()) {
			if (existingProduct.get().getActive() == 1) {
				return ResponseEntity.badRequest().body(new Status("400", "Product Already Exists!!!"));
			}

			existingProduct.get().setActive(1L);
			if(dto.getLot() != null) {
				existingProduct.get().setLot(dto.getLot());
			}
			if(dto.getUnits() != null) {
				existingProduct.get().setUnits(dto.getUnits());
			}
			productMapping(dto, user);
			no2ProductsRepository.save(existingProduct.get());
			return new ResponseEntity<Status>(new Status("200", "Product is Active Now!!!"), HttpStatus.OK);
		}

		No2Products no2Product = new No2Products(dto);
		Optional<ProductMaster> product = productMasterRepository.findById(no2Product.getProductId());
		if (product.isPresent()) {
			no2Product.setName(product.get().getProductName());
		}
		no2Product.setActive(1L);
		no2Product.setCreatedBy(user.getId());
		no2Product.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		productMapping(dto, user);
		no2ProductsRepository.save(no2Product);

		return new ResponseEntity<Status>(new Status("200", "Product Successfully Added!!!"), HttpStatus.OK);

	}

	private void productMapping(No2ProductsDto product, User user) {
		List<No2ProductsParty> partyList = no2ProductsPartyRepository.findByProductType(product.getProductType());

		List<No2ProductsPartyMapping> MappingList = new ArrayList<No2ProductsPartyMapping>();

		for (No2ProductsParty party : partyList) {
			No2ProductsPartyMapping no2ProductsPartyMapping = new No2ProductsPartyMapping();
			no2ProductsPartyMapping.setActive(1L);
			no2ProductsPartyMapping.setAmount(0D);

			if (party.getRefParty() != null) {
				Optional<No2ProductsPartyMapping> optional = no2ProductsPartyMappingRepository.findByPartyAndProduct(
						party.getRefParty().getId(), product.getProductId(), product.getProductType());
				if (optional.isPresent()) {
					No2ProductsPartyMapping dto = optional.get();
					double amount = (dto.getAmount() / 100) * party.getPercentage();
					no2ProductsPartyMapping.setAmount(dto.getAmount() + amount);
				}
			}
			no2ProductsPartyMapping.setBrokerId(party.getBrokerId());
			no2ProductsPartyMapping.setBrokerName(party.getBrokerName());
			no2ProductsPartyMapping.setLot(product.getLot());
			no2ProductsPartyMapping.setPartyId(party.getPartyId());
			no2ProductsPartyMapping.setProductId(product.getProductId());
			no2ProductsPartyMapping.setUpdatedDate(LocalDate.now());
			no2ProductsPartyMapping.setUpdatedBy(user.getId());
			no2ProductsPartyMapping.setProductType(party.getProductType());
			MappingList.add(no2ProductsPartyMapping);

		}
		no2ProductsPartyMappingRepository.saveAll(MappingList);
	}

	public ResponseEntity<?> ListProductbyProductType(String ProductType, User user) {
		List<No2Products> productList = no2ProductsRepository.findAllByProductType(ProductType);
		List<No2ProductsDto> dto = productList.stream().map(val -> new No2ProductsDto(val))
				.collect(Collectors.toList());
		return new ResponseEntity<List<No2ProductsDto>>(dto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteProduct(Long productId, String productType, User user) {
		Optional<No2Products> no2 = no2ProductsRepository.findByProductIdANDType(productId, productType);

		if (no2.isPresent()) {
			List<No2ProductsPartyMapping> no2ProductsPartyMappings = no2ProductsPartyMappingRepository
					.getPartymappingforProduct(productId, productType);
			System.out.println(no2ProductsPartyMappings.size());
			no2ProductsPartyMappingRepository.deleteAll(no2ProductsPartyMappings);
			no2.get().setActive(0L);
			no2ProductsRepository.save(no2.get());
			return new ResponseEntity<Status>(new Status("200", "Product Successfully Removed!!!"), HttpStatus.OK);
		}

		return new ResponseEntity<Status>(new Status("400", "Product not removed"), HttpStatus.OK);
	}

	/* Party Services */
	public ResponseEntity<?> createparty(No2ProductsPartyDto dto, User user) {

		Optional<No2ProductsParty> existingParty = no2ProductsPartyRepository.findByPartyIdANDType(dto.getPartyId(),
				dto.getProductType());

		if (existingParty.isPresent()) {
			No2ProductsParty no2Party = existingParty.get();
			if (no2Party.getActive() == 1) {
				return ResponseEntity.badRequest().body(new Status("400", "Party Already Exists!!!"));
			}

			no2Party.setActive(1L);
			no2Party.setUpdatedBy(user.getId());
			no2Party.setUpdatedDate(LocalDate.now());
			no2Party.setBrokerId(dto.getBrokerId());
			no2Party.setBrokerName(dto.getBrokerName());
			if (dto.getRefParty() != null) {
				Vendor refParty = new Vendor();
				refParty.setId(dto.getRefParty());
				no2Party.setRefParty(refParty);
				no2Party.setPercentage(dto.getPercentage());
			} else {
				no2Party.setRefParty(null);
				no2Party.setPercentage(null);
			}
			partyMapping(dto, user);
			no2ProductsPartyRepository.save(no2Party);
			return new ResponseEntity<Status>(new Status("200", "Party is Active Now!!!"), HttpStatus.OK);
		}

		No2ProductsParty no2Party = new No2ProductsParty(dto);
		no2Party.setUpdatedBy(user.getId());
		no2Party.setUpdatedDate(LocalDate.now());
		partyMapping(dto, user);
		no2ProductsPartyRepository.save(no2Party);
		return new ResponseEntity<Status>(new Status("200", "Party Successfully Added!!!"), HttpStatus.OK);
	}

	public void partyMapping(No2ProductsPartyDto party, User user) {

		List<No2Products> productList = no2ProductsRepository.findAllByProductType(party.getProductType());

		List<No2ProductsPartyMapping> partyList = new ArrayList<No2ProductsPartyMapping>();

		for (No2Products products : productList) {
			No2ProductsPartyMapping no2ProductsPartyMapping = new No2ProductsPartyMapping();
			no2ProductsPartyMapping.setActive(1L);
			no2ProductsPartyMapping.setAmount(0D);

			if (party.getRefParty() != null) {
				Optional<No2ProductsPartyMapping> optional = no2ProductsPartyMappingRepository
						.findByPartyAndProduct(party.getRefParty(), products.getProductId(), party.getProductType());
				if (optional.isPresent()) {
					No2ProductsPartyMapping dto = optional.get();
					double amount = (dto.getAmount() / 100) * party.getPercentage();
					no2ProductsPartyMapping.setAmount(dto.getAmount() + amount);
				}
			}
			no2ProductsPartyMapping.setBrokerId(party.getBrokerId());
			no2ProductsPartyMapping.setBrokerName(party.getBrokerName());
			no2ProductsPartyMapping.setLot(products.getLot());
			no2ProductsPartyMapping.setPartyId(party.getPartyId());
			no2ProductsPartyMapping.setProductId(products.getProductId());
			no2ProductsPartyMapping.setUpdatedDate(LocalDate.now());
			no2ProductsPartyMapping.setUpdatedBy(user.getId());
			no2ProductsPartyMapping.setProductType(party.getProductType());
			partyList.add(no2ProductsPartyMapping);

		}
		no2ProductsPartyMappingRepository.saveAll(partyList);
	}

	public ResponseEntity<?> ListPartybyProductType(String ProductType, User user) {
		List<No2ProductsParty> partyList = no2ProductsPartyRepository.findByProductType(ProductType);
		List<No2ProductsPartyDto> dto = partyList.stream().map(val -> new No2ProductsPartyDto(val))
				.collect(Collectors.toList());
		return new ResponseEntity<List<No2ProductsPartyDto>>(dto, HttpStatus.OK);
	}

	public ResponseEntity<?> ListAllParty(String ProductType) {
		List<No2ProductsParty> partyList = no2ProductsPartyRepository.findAllByProductType(ProductType);
		List<No2ProductsPartyDto> dto = partyList.stream().map(val -> new No2ProductsPartyDto(val))
				.collect(Collectors.toList());
		return new ResponseEntity<List<No2ProductsPartyDto>>(dto, HttpStatus.OK);
	}

	public ResponseEntity<?> UpdatePrimaryParty(Long partyId, String productType) {
		Optional<No2ProductsParty> party = no2ProductsPartyRepository.findByPartyIdANDType(partyId, productType);

		if (party.isPresent()) {
			no2ProductsPartyRepository.UpdatePrimaryParty(partyId, productType);
			return new ResponseEntity<Status>(new Status("200", "Primary Party set Successfully!!!"), HttpStatus.OK);
		}

		return new ResponseEntity<Status>(new Status("400", "Primary Party not set"), HttpStatus.OK);
	}

	public ResponseEntity<?> deleteParty(Long partyId, String productType, User user) {
		Optional<No2ProductsParty> no2 = no2ProductsPartyRepository.findByPartyIdANDType(partyId, productType);

		if (no2.isPresent()) {
			List<No2ProductsPartyMapping> no2ProductsPartyMappings = no2ProductsPartyMappingRepository
					.getmappingforParty(partyId, productType);
			no2ProductsPartyMappingRepository.deleteAll(no2ProductsPartyMappings);
			no2.get().setActive(0L);
			no2ProductsPartyRepository.save(no2.get());
			return new ResponseEntity<Status>(new Status("200", "Party Successfully Removed!!!"), HttpStatus.OK);
		}

		return new ResponseEntity<Status>(new Status("400", "Party not removed"), HttpStatus.OK);
	}

	/* Listing */

	public ResponseEntity<?> ListPricebyProductPartyMapping(String ProductType, User user) {
		List<No2Products> productList = no2ProductsRepository.findAllByProductType(ProductType);

		List<ProductPriceListDto> productPriceList = new ArrayList<ProductPriceListDto>();

		for (No2Products products : productList) {
			List<ProductPartyPrice> result = no2ProductsPartyMappingRepository
					.findProductsByProductType(products.getProductId(), ProductType);

			List<PartyPriceDto> partyPriceDto = result.stream().map(val -> new PartyPriceDto(val))
					.collect(Collectors.toList());

			ProductPriceListDto productPriceListDto = new ProductPriceListDto(products.getProductId(),
					products.getLot(), products.getName(), partyPriceDto);
			productPriceList.add(productPriceListDto);
		}

		return new ResponseEntity<List<ProductPriceListDto>>(productPriceList, HttpStatus.OK);
	}

	public ResponseEntity<?> ListBroker(User user) {
		List<Broker> brokerList = brokerRepository.findAll();
		List<BrokerDto> brokerListDto = brokerList.stream().map(val -> new BrokerDto(val)).collect(Collectors.toList());
		return new ResponseEntity<List<BrokerDto>>(brokerListDto, HttpStatus.OK);
	}

	/* Price Update API */

	public ResponseEntity<?> UpdatePartyPrice(Long productId, Long partyId, String productType, Long lot, Double price,
			User user) {
		List<No2ProductsPartyMapping> PriceUpdatepartyMapping = new ArrayList<>();

		List<No2ProductsPartyMapping> partyMapping = no2ProductsPartyMappingRepository.findAllByPartyProductOrLot(productId, partyId, productType, lot);

		if (partyMapping.isEmpty()) {
			String message = Objects.nonNull(lot) ? "ProductId in given lot is not present!!!!"
					: "Product & Party is not mapped!!!!!";
			return ResponseEntity.ok(new Status("400", message));
		}

		for (No2ProductsPartyMapping mapping : partyMapping) {
			UpdateRefPartyPrice(mapping.getPartyId(), productId, productType, price);
			mapping.setOriginalAmount(mapping.getAmount());
			mapping.setAmount(price);
			mapping.setUpdatedBy(user.getId());
			mapping.setUpdatedDate(LocalDate.now());
			PriceUpdatepartyMapping.add(mapping);
		}
		no2ProductsPartyMappingRepository.saveAll(PriceUpdatepartyMapping);
		updateRatehistory(PriceUpdatepartyMapping, user);
		return ResponseEntity.ok(new Status("200", "Price Updated Successfully"));
	}

	public void UpdateRefPartyPrice(Long partyId, Long productId, String productType, Double price) {
		List<No2ProductsParty> partyList = no2ProductsPartyRepository.findByRefPartyIdANDType(partyId, productType);
		for (No2ProductsParty party : partyList) {
			Optional<No2ProductsPartyMapping> optional = no2ProductsPartyMappingRepository
					.findByPartyAndProduct(party.getPartyId(), productId, productType);
			No2ProductsPartyMapping dto = optional.get();
			dto.setOriginalAmount(dto.getAmount());
			double amount = (price / 100) * party.getPercentage();
			dto.setAmount(price + amount);
			no2ProductsPartyMappingRepository.save(dto);
		}
	}

	public ResponseEntity<?> UpdateProductPriceWithAllParty(No2ProductsPriceDto req, User user) {

		List<No2ProductsPartyMapping> priceUpdatePartyMapping = new ArrayList<>();

		List<No2ProductsPartyMapping> partyMapping = no2ProductsPartyMappingRepository
				.findAllProductWithLot(req.getProductId(), req.getLot(), req.getProductType());

		if (partyMapping.isEmpty()) {
			return new ResponseEntity<Status>(new Status("200", "ProductId in given lot is not present!!!!"),
					HttpStatus.OK);
		}

		for (No2ProductsPartyMapping mapping : partyMapping) {

			mapping.setOriginalAmount(mapping.getAmount());
			if (req.getPercentageType().equalsIgnoreCase("increase")) {
				Double amt = ((mapping.getAmount() / 100) * req.getPercentage());
				Double rsAMt = mapping.getAmount() + amt;
				mapping.setAmount(rsAMt);
			} else {
				Double amt = ((mapping.getAmount() / 100) * req.getPercentage());
				Double rsAMt = mapping.getAmount() - amt;
				mapping.setAmount(rsAMt);
			}
			mapping.setUpdatedBy(user.getId());
			mapping.setUpdatedDate(LocalDate.now());
			priceUpdatePartyMapping.add(mapping);
		}
		no2ProductsPartyMappingRepository.saveAll(priceUpdatePartyMapping);
		updateRatehistory(priceUpdatePartyMapping, user);
		return new ResponseEntity<Status>(new Status("200", "Price Updated Successfully"), HttpStatus.OK);

	}

	/* Rate history */
	public ResponseEntity<?> ListRateHistory(String ProductType, User user) {
		try {
			List<Long> productList = no2ProductsRepository.findRefidByProductType(ProductType);

			List<RatehistoryGrouping> ratehistoryGroupings = no2RateHistoryRepository
					.findRatehistoryGroupings(productList, null, null, null);

			Long productId = 0L;
			Long lot = 0L;
			List<RatehistoryProductDto> ratehistoryProductDtoList = new ArrayList<RatehistoryProductDto>();
			RatehistoryProductDto ratehistoryProductDto = new RatehistoryProductDto();
			List<RatehistoryGroupingDto> ratehistoryGroupingDtosList = new ArrayList<RatehistoryGroupingDto>();
			List<RatehistoryPartyGroupingDto> ratehistoryPartyGroupingDtoList = new ArrayList<RatehistoryPartyGroupingDto>();
			RatehistoryGroupingDto ratehistoryGroupingDto = new RatehistoryGroupingDto();

			Long partyId = 0L;

			for (RatehistoryGrouping ratehistory : ratehistoryGroupings) {

				if (!productId.equals(ratehistory.getProductId())) {
					if (Objects.nonNull(ratehistoryProductDto) && Objects.nonNull(ratehistoryProductDto.getProductId())
							&& ratehistoryProductDto.getProductId() > 0) {
						ratehistoryProductDto.setPartyList(ratehistoryGroupingDtosList);

						ratehistoryProductDtoList.add(ratehistoryProductDto);
					}

					ratehistoryProductDto = new RatehistoryProductDto();
					ratehistoryGroupingDtosList = new ArrayList<RatehistoryGroupingDto>();
					ratehistoryPartyGroupingDtoList = new ArrayList<RatehistoryPartyGroupingDto>();

				} else if (!lot.equals(ratehistory.getLot())) {
					if (Objects.nonNull(ratehistoryProductDto) && Objects.nonNull(ratehistoryProductDto.getProductId())
							&& ratehistoryProductDto.getProductId() > 0) {
						ratehistoryProductDto.setPartyList(ratehistoryGroupingDtosList);

						ratehistoryProductDtoList.add(ratehistoryProductDto);
					}

					ratehistoryProductDto = new RatehistoryProductDto();
					ratehistoryGroupingDtosList = new ArrayList<RatehistoryGroupingDto>();
					ratehistoryPartyGroupingDtoList = new ArrayList<RatehistoryPartyGroupingDto>();

				} else {
					if (partyId != ratehistory.getPartyId()) {

						if (Objects.nonNull(ratehistoryGroupingDto) && ratehistoryGroupingDto.getPartyId() != null) {
							ratehistoryGroupingDto.setPartyGroupingDto(ratehistoryPartyGroupingDtoList);
							ratehistoryGroupingDtosList.add(ratehistoryGroupingDto);
						}
						partyId = ratehistory.getPartyId();
						ratehistoryGroupingDto = new RatehistoryGroupingDto();

						ratehistoryPartyGroupingDtoList = new ArrayList<RatehistoryPartyGroupingDto>();
						ratehistoryGroupingDto.setPartyId(ratehistory.getPartyId());
						ratehistoryGroupingDto.setPartyName(ratehistory.getPartyName());
					}
				}
				productId = ratehistory.getProductId();
				lot = ratehistory.getLot();

				RatehistoryPartyGroupingDto ratehistoryPartyGroupingDto = new RatehistoryPartyGroupingDto();
				ratehistoryProductDto.setProductId(ratehistory.getProductId());
				ratehistoryProductDto.setProductName(ratehistory.getProductName() + " (" + ratehistory.getLot() + ")");
				ratehistoryProductDto.setLot(ratehistory.getLot());

				ratehistoryPartyGroupingDto.setIncDec(ratehistory.getIncDec());
				ratehistoryPartyGroupingDto.setOldAmount(ratehistory.getOldAmount());
				ratehistoryPartyGroupingDto.setAmount(ratehistory.getAmount());
				ratehistoryPartyGroupingDto.setDate(ratehistory.getcreatedDate());

				ratehistoryPartyGroupingDtoList.add(ratehistoryPartyGroupingDto);

			}
			ratehistoryGroupingDto.setPartyGroupingDto(ratehistoryPartyGroupingDtoList);

			ratehistoryProductDtoList.add(ratehistoryProductDto);
			ratehistoryProductDto.setPartyList(ratehistoryGroupingDtosList);

			return new ResponseEntity<List<RatehistoryProductDto>>(ratehistoryProductDtoList, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<List<No2ProductsDto>>(new ArrayList<No2ProductsDto>(), HttpStatus.OK);
	}

	public ResponseEntity<?> ListRateHistoryv2(String ProductType, User user, String fromDate, String toDate,
			Long partyId, Pageable pageable) {

		System.out.println("date == "+fromDate);
		System.out.println("date == "+toDate);

		try {
			List<Long> productList = no2ProductsRepository.findRefidByProductType(ProductType);

			Page<RatehistoryGrouping> ratehistoryGroupings = no2RateHistoryRepository.findRatehistoryGroupings(
					productList, dateUtil.getStringTODateDDMMYYYY(fromDate), dateUtil.getStringTODateDDMMYYYY(toDate),
					partyId, pageable);

			List<No2RatehistoryListDto> no2RatehistoryListDto = ratehistoryGroupings.stream()
					.map(val -> new No2RatehistoryListDto(val)).collect(Collectors.toList());

			PageDataDto<No2RatehistoryListDto> no2RatehistoryPageDto = new PageDataDto<>(no2RatehistoryListDto,
					ratehistoryGroupings);

			return new ResponseEntity<PageDataDto<No2RatehistoryListDto>>(no2RatehistoryPageDto, HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<PageDataDto<No2RatehistoryListDto>>(new PageDataDto<>(), HttpStatus.OK);
	}

	public void updateRatehistory(List<No2ProductsPartyMapping> PriceUpdatepartyMapping, User user) {

		List<No2Ratehistory> ratehistory = new ArrayList<No2Ratehistory>();
		for (No2ProductsPartyMapping products : PriceUpdatepartyMapping) {
			No2Ratehistory no2Ratehistory = new No2Ratehistory();
			no2Ratehistory.setAmount(products.getAmount());
			no2Ratehistory.setCreatedBy(user.getId());
			no2Ratehistory.setCreatedDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			if (products.getAmount() > products.getOriginalAmount()) {
				no2Ratehistory.setIncreaseDecrease(1L);

			} else {
				no2Ratehistory.setIncreaseDecrease(2L);

			}

			no2Ratehistory.setLot(products.getLot());
			no2Ratehistory.setOriginalAmount(products.getOriginalAmount());
			no2Ratehistory.setPartyId(products.getPartyId());
			no2Ratehistory.setProductId(products.getProductId());
			no2Ratehistory.setPercentage(products.getAmount() - products.getOriginalAmount());

			ratehistory.add(no2Ratehistory);
		}

		no2RateHistoryRepository.saveAll(ratehistory);
	}

	/* Bill Services */

	public ResponseEntity<?> createno2Bill(No2billsDto reqDto, User user) {
		try {
			No2bills no2bills = new No2bills(reqDto);

			no2bills.setTransactionBy(user);
			no2bills.setBillPrepareBy(user.getId());

			no2bills.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			no2bills.setBillStatus("Pending");
			if (reqDto.getProductType().equals("1")) {
				no2bills.setTrnNo("Gunny - " + System.currentTimeMillis());

			}
			if (reqDto.getProductType().equals("2")) {
				no2bills.setTrnNo("GroundNut - " + System.currentTimeMillis());

			}
			if (reqDto.getProductType().equals("3")) {
				no2bills.setTrnNo("Sesame - " + System.currentTimeMillis());

			}
			if (Objects.nonNull(no2bills) && Objects.nonNull(no2bills.getPartyId())) {

				No2bills result = no2BillsRepository.save(no2bills);

				if (Objects.nonNull(reqDto.getNo2billitems()) && reqDto.getNo2billitems().size() > 0) {
					List<No2billitems> no2billitemsList = reqDto.getNo2billitems().stream().map(val -> {
						No2billitems no2billitems = new No2billitems(val);
						no2billitems.setTrnNo(result.getTrnNo());
						no2billitems.setTransactionBy(user.getId());
						no2billitems.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
						no2billitems.setBillId(result.getId());
						return no2billitems;
					}).collect(Collectors.toList());

					no2billitemsRepository.saveAll(no2billitemsList);

				}

			}

			return new ResponseEntity<Status>(new Status("200", "Items Added SuccessFully!!!"), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Bills not added"), HttpStatus.OK);
	}

	public ResponseEntity<?> createno2BillList(List<No2billsDto> reqDto, User user) {

		for (No2billsDto reqbillDto : reqDto) {

			try {
				No2bills no2bills = new No2bills(reqbillDto);

				no2bills.setTransactionBy(user);
				no2bills.setBillPrepareBy(user.getId());

				no2bills.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				no2bills.setBillStatus("Pending");
				if (reqbillDto.getProductType().equals("1")) {
					no2bills.setTrnNo("Gunny - " + System.currentTimeMillis());
				}
				if (reqbillDto.getProductType().equals("2")) {
					no2bills.setTrnNo("GroundNut - " + System.currentTimeMillis());
				}
				if (reqbillDto.getProductType().equals("3")) {
					no2bills.setTrnNo("Sesame - " + System.currentTimeMillis());
				}
				if (Objects.nonNull(no2bills) && Objects.nonNull(no2bills.getPartyId())) {

					No2bills result = no2BillsRepository.save(no2bills);

					if (Objects.nonNull(reqbillDto.getNo2billitems()) && reqbillDto.getNo2billitems().size() > 0) {

						List<No2billitems> no2billitemsList = reqbillDto.getNo2billitems().stream().map(val -> {
							No2billitems no2billitems = new No2billitems(val);
							no2billitems.setTrnNo(result.getTrnNo());
							no2billitems.setTransactionBy(user.getId());
							no2billitems.setTransactionDate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
							no2billitems.setBillId(result.getId());
							result.setNetAmount(no2billitems.getAmount() + result.getNetAmount());
							result.setQuantity(result.getQuantity() + no2billitems.getQuantity());
							if (reqbillDto.getProductType().equals("2") || reqbillDto.getProductType().equals("3")) {
								no2billitems.setKattu(result.getKattu());
							} else {
								no2billitems.setKattu(val.getKattu());
							}
							result.setKattu(result.getKattu() + no2billitems.getKattu());
							if (result.getNetAmount() > 0) {

								result.setPerbagAmount(result.getNetAmount() / result.getQuantity());
								Double tcsAmount = (result.getNetAmount() / 100) * 0.1;
								result.setTcsAmount(Double.parseDouble(String.format("%.2f", tcsAmount)));
								result.setTcsPercent(0.1D);

								/* tax Calculation */
								Optional<ProductMaster> product = productMasterRepository.findById(val.getProductId());
								GstTaxCategory gst = gstTaxCategoryRepository
										.findByHsnCode(product.get().getHsnRefId(), "P");
								double cgst = 0.0 ;
								double igst = 0.0;
								double sgst = 0.0 ;
								if(gst != null) {
									
									 cgst = (no2billitems.getAmount() / 100) * gst.getCgst();
									
									 igst = (no2billitems.getAmount() / 100) * gst.getIgst();
									
									 sgst = (no2billitems.getAmount() / 100) * gst.getSgst();
									
								}
								
								no2billitems.setCgstAmt(cgst);
							
								no2billitems.setIgstAmt(igst);
								
								no2billitems.setSgstAmt(sgst);
								
								result.setTotalCost((double) Math
										.ceil(result.getNetAmount() + result.getGstAmt() + result.getTcsAmount()));
								result.setRoundOff(
										result.getTotalCost() - result.getNetAmount() - result.getTcsAmount());

							}
							return no2billitems;
						}).collect(Collectors.toList());

						no2billitemsRepository.saveAll(no2billitemsList);
						no2BillsRepository.save(result);
					}

					for (No2billitemsDto item : reqbillDto.getNo2billitems()) {

						long branch = 1;
						long dept = 7;

						List<BinStock> binstocks = binStockRepository.FindByProIdLockStatus(item.getProductId(), branch, dept);

						double remainingQty = item.getQuantity();

						for (BinStock binStock : binstocks) {

							if (remainingQty <= 0) {
								// If remaining quantity is 0 or negative, break the loop
								break;
							}

							if (binStock.getStkQty() >= remainingQty) {
								// If stkQty is greater than or equal to remaining quantity, create a new
								// binstock entry

								BinStock newBinstock = createNewBinstock(binStock, remainingQty, result.getTrnNo());
								binStock.setStkQty(binStock.getStkQty() - remainingQty);
								remainingQty = 0; // Set remaining quantity to 0
							} else {
								// If stkQty is less than remaining quantity, create a new binstock entry and
								// update remaining quantity

								BinStock newBinstock = createNewBinstock(binStock, binStock.getStkQty(), result.getTrnNo());
								remainingQty -= binStock.getStkQty();
								binStock.setStkQty(0.0); // Set stkQty to 0 as all stock has been used
							}
							// Save the updated selected binstock to the database
							binStockRepository.save(binStock);
						}

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return new ResponseEntity<Status>(new Status("200", "Items Added SuccessFully!!!"), HttpStatus.OK);
	}

	private BinStock createNewBinstock(BinStock binStock, double quantity, String trnNo) {
		BinStock newBinstock = new BinStock();
		newBinstock.setRefId(binStock.getRefId());
		newBinstock.setBatchNo(binStock.getBatchNo());
		newBinstock.setBatchId(binStock.getBatchId());
		newBinstock.setBinId(binStock.getBinId());
		newBinstock.setBinName(binStock.getBinName());
		newBinstock.setLot(binStock.getLot());
		newBinstock.setStkQty(quantity); // Set stkqty to the given quantity
		newBinstock.setSStkQty(binStock.getSStkQty());
		newBinstock.setAvgCost(binStock.getAvgCost());
		newBinstock.setDept(binStock.getDept());
		newBinstock.setBranch(binStock.getBranch());
		newBinstock.setLockStatus("Y");
		newBinstock.setLockType(binStock.getLockType());
		newBinstock.setGid(binStock.getGid());
		newBinstock.setDelStatus(binStock.getDelStatus());
		newBinstock.setBinType(binStock.getBinType());
		newBinstock.setCid(binStock.getCid());
		newBinstock.setOrderId(binStock.getOrderId());
		newBinstock.setPurchaseBillId(binStock.getPurchaseBillId());
		newBinstock.setMaterialTransactionId(binStock.getMaterialTransactionId());
		newBinstock.setInwarDate(binStock.getInwarDate());
		newBinstock.setLocation(binStock.getLocation());
		newBinstock.setCampName(binStock.getCampName());
		newBinstock.setSeedcolor(binStock.getSeedcolor());
		newBinstock.setSeedMoisture(binStock.getSeedMoisture());

		// Calculate the cost based on the average cost and the given quantity
		if (binStock.getAvgCost() != null) {
			newBinstock.setCost(quantity * binStock.getAvgCost());
		}

		newBinstock.setLTrnNo(trnNo);
		newBinstock.setMrnStatus("Y");
		newBinstock.setAdate(dateUtil.getISTDate());

		// Save the new binstock entry
		return binStockRepository.save(newBinstock);
	}

	public ResponseEntity<?> billsUpdateDto(No2billsUpdateDto reqDto, User user) {
		try {

			Optional<No2bills> no2Bills = no2BillsRepository.findById(reqDto.getId());

			if (no2Bills.isPresent()) {
				if (reqDto.getBillType().equals("Verified")) {
					no2Bills.get().setVerifyBy(user);
					no2Bills.get().setVerifyStatus(reqDto.getBillType());
					no2Bills.get().setBillStatus(reqDto.getBillType());
					no2Bills.get().setVdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				} else if (reqDto.getBillType().equals("Approved")) {
					no2Bills.get().setApproveBy(user);
					no2Bills.get().setApproveStatus(reqDto.getBillType());
					no2Bills.get().setBillStatus(reqDto.getBillType());
					no2Bills.get().setAdate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				} else if (reqDto.getBillType().equals("Authorized")) {
					no2Bills.get().setAuthorizeBy(user);
					no2Bills.get().setAuthorizeStatus(reqDto.getBillType());
					no2Bills.get().setBillStatus("Completed");
					no2Bills.get().setAodate(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
				}
				no2BillsRepository.save(no2Bills.get());
			}

			return new ResponseEntity<Status>(new Status("200", "Bill Status Updated SuccessFully!!!"), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<Status>(new Status("400", "Bills Status Update Failed"), HttpStatus.OK);
	}

	public ResponseEntity<?> billReject(Long billId) {
		Optional<No2bills> no2Bills = no2BillsRepository.findById(billId);

		long branch = 1;
		long dept = 7;

		List<No2billitems> billItems = no2billitemsRepository.findAllByBillId(no2Bills.get().getId());
		for (No2billitems item : billItems) {
			List<BinStock> bins = binStockRepository.findByLTrnNo(no2Bills.get().getTrnNo(), item.getProductId());
			if (!bins.isEmpty()) {
				for (BinStock bin : bins) {
					BinStock b = binStockRepository.findStockbyProductIdBatchNoAndLockStatus(item.getProductId(),
							branch, dept, bin.getBatchNo());

					// returning stock value to old binStock
					Double updatedStkQty = b.getStkQty() + Double.valueOf(bin.getStkQty());
					b.setStkQty(updatedStkQty);
					binStockRepository.save(b);

					// deleting allocated binstock
					binStockRepository.deleteById(bin.getId());
				}
			}
		}

		no2BillsRepository.deleteById(no2Bills.get().getId());
		return new ResponseEntity<Status>(new Status("200", "bill Successfully Removed!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> ListBillsByStatus(String ProductType, String billType, Pageable pageable) {
		try {

			Page<No2bills> no2bills = no2BillsRepository.findAllByStatus(ProductType, billType, pageable);

			if (Objects.nonNull(no2bills)) {

				Map<Long, String> partyName = new HashMap<Long, String>();

				List<No2billsDto> no2BillsDto = no2bills.stream().map(val -> {
					No2billsDto billsDto = new No2billsDto(val, no2ProductsRepository, no2ProductsPartyRepository);

					if (Objects.isNull(partyName.get(val.getPartyId()))) {
						Optional<No2ProductsParty> party = no2ProductsPartyRepository.findById(val.getPartyId());
						if (party.isPresent()) {
							partyName.put(party.get().getPartyId(), party.get().getPartyName());
							billsDto.setPartyName(party.get().getPartyName());
						}
					} else {
						billsDto.setPartyName(partyName.get(val.getPartyId()));
					}
					return billsDto;
				}).collect(Collectors.toList());

				PageDataDto<No2billsDto> pagedno2billsdtoDataDto = new PageDataDto<>(no2BillsDto, no2bills);

				return new ResponseEntity<PageDataDto<No2billsDto>>(pagedno2billsdtoDataDto, HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<PageDataDto<No2billsDto>>(new PageDataDto<No2billsDto>(), HttpStatus.OK);
	}

	public ResponseEntity<?> no2billsgetbyid(Long id, User user) {
		try {
			Optional<No2bills> no2Bills = no2BillsRepository.findById(id);
			if (no2Bills.isPresent()) {
				No2billsDto no2billsDto = new No2billsDto(no2Bills.get());
				Optional<No2ProductsParty> party = no2ProductsPartyRepository.findByPartyId(no2billsDto.getPartyId());
				if (party.isPresent()) {
					no2billsDto.setPartyName(party.get().getPartyName());
					Optional<Vendor> vendorOptional = vendorRepository.findById(no2billsDto.getPartyId());
					if(vendorOptional.isPresent()){
						no2billsDto.setState(vendorOptional.get().getState());
					}
					no2billsDto.setBrokerName(party.get().getBrokerName());
				}

				List<No2billitems> billitems = no2billitemsRepository.findAllByBillId(no2billsDto.getId());

				List<No2billitemsDto> billitemsDtos = billitems.stream().map(obj -> {
					No2billitemsDto no2billitemsDto = new No2billitemsDto(obj);
					System.out.println("productId  = " + no2billitemsDto.getProductId());
					Optional<No2Products> products = no2ProductsRepository
							.findByProductIdANDType(no2billitemsDto.getProductId(), no2billsDto.getProductType());
					if (products.isPresent()) {
						no2billitemsDto.setProductName(products.get().getName());
					}
					return no2billitemsDto;
				}).collect(Collectors.toList());

				no2billsDto.setNo2billitems(billitemsDtos);

				if (no2Bills.get().getApproveBy() != null) {
					no2billsDto.setApproverName(no2Bills.get().getApproveBy().getEmployeename());
				}
				if (no2Bills.get().getVerifyBy() != null) {
					no2billsDto.setVerifyName(no2Bills.get().getVerifyBy().getEmployeename());
				}
				if (no2Bills.get().getAuthorizeBy() != null) {
					no2billsDto.setAuthorizerName(no2Bills.get().getAuthorizeBy().getEmployeename());
				}
				if (no2Bills.get().getTransactionBy() != null) {
					no2billsDto.setBillPreparerName(no2Bills.get().getTransactionBy().getEmployeename());
				}

				return new ResponseEntity<No2billsDto>(no2billsDto, HttpStatus.OK);
			}

			return new ResponseEntity<No2billsDto>(new No2billsDto(), HttpStatus.OK);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<No2billsDto>(new No2billsDto(), HttpStatus.OK);
	}

	public ResponseEntity<?> ListBills(String ProductType, User user, String fromDate, String toDate, Long partyId,
			Pageable pageable) {
		try {

			Page<No2bills> no2bills = no2BillsRepository.findAllBills(ProductType,
					dateUtil.getStringTODateDDMMYYYY(fromDate), dateUtil.getStringTODateDDMMYYYY(toDate), partyId,
					pageable);

			System.out.println(no2bills);
			if (Objects.nonNull(no2bills)) {

				Map<Long, String> partyName = new HashMap<Long, String>();

				List<No2billsDto> no2BillsDto = no2bills.stream().map(val -> {
					No2billsDto billsDto = new No2billsDto(val, no2ProductsRepository, no2ProductsPartyRepository);

					if (Objects.isNull(partyName.get(val.getPartyId()))) {
						Optional<No2ProductsParty> party = no2ProductsPartyRepository.findById(val.getPartyId());
						if (party.isPresent()) {
							partyName.put(party.get().getPartyId(), party.get().getPartyName());
							billsDto.setPartyName(party.get().getPartyName());
						}

					} else {
						billsDto.setPartyName(partyName.get(val.getPartyId()));
					}

					return billsDto;
				}).collect(Collectors.toList());

				PageDataDto<No2billsDto> pagedno2billsdtoDataDto = new PageDataDto<>(no2BillsDto, no2bills);

				return new ResponseEntity<PageDataDto<No2billsDto>>(pagedno2billsdtoDataDto, HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<PageDataDto<No2billsDto>>(new PageDataDto<No2billsDto>(), HttpStatus.OK);
	}

	public ResponseEntity<?> ListBillsPending(String ProductType, User user) {
		try {

			List<No2bills> no2bills = no2BillsRepository.findAllPending(ProductType);

			if (Objects.nonNull(no2bills)) {

				Map<Long, String> partyName = new HashMap<Long, String>();
				List<No2billsDto> no2BillsDto = no2bills.stream().map(val -> {
					No2billsDto billsDto = new No2billsDto(val, no2ProductsRepository, no2ProductsPartyRepository);

					if (Objects.isNull(partyName.get(val.getPartyId()))) {
						Optional<No2ProductsParty> party = no2ProductsPartyRepository.findById(val.getPartyId());
						if (party.isPresent()) {
							partyName.put(party.get().getPartyId(), party.get().getPartyName());
							billsDto.setPartyName(party.get().getPartyName());
						}

					} else {
						billsDto.setPartyName(partyName.get(val.getPartyId()));

					}

					return billsDto;
				}).collect(Collectors.toList());

				return new ResponseEntity<List<No2billsDto>>(no2BillsDto, HttpStatus.OK);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ResponseEntity<List<No2billsDto>>(new ArrayList<No2billsDto>(), HttpStatus.OK);
	}

	public ResponseEntity<?> ListStockByProductType(String ProductType, User user) {
		long branch = 1;
		long dept = 7;
		List<StockbyProduct> binStocks = binStockRepository.findStockbyProductAndLockStatus(ProductType, branch, dept);

		List<StockbyProductDTO> binstockqtylist = binStocks.stream().map(val -> new StockbyProductDTO(val))
				.collect(Collectors.toList());

		return new ResponseEntity<List<StockbyProductDTO>>(binstockqtylist, HttpStatus.OK);
	}

}

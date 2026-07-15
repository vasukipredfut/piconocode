package com.aria.service;

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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SalesService {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	SalesProductRepository salesProductRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	DateUtil dateUtil;

	public ResponseEntity<?> saveProduct(ProductDto productDto, User loggedUser) {
		Product product = new Product(productDto);

		product.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		product.setCreatedBy(loggedUser.getId());
		Product result = productRepository.save(product);
		for (SalesProductDto salesProductDto : productDto.getSalesProduct()) {
			SalesProduct salesProduct = new SalesProduct(salesProductDto);
			salesProduct.setProductId(result.getId());
			salesProduct.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			salesProduct.setCreatedBy(loggedUser.getId());
			salesProductRepository.save(salesProduct);
		}
		return new ResponseEntity<Status>(new Status("200", "product save successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> updateProduct(ProductDto productDto, User loggedUser) {
		Product product = new Product(productDto);

		product.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		product.setUpdatedBy(loggedUser.getId());
		Product result = productRepository.save(product);
		for (SalesProductDto salesProductDto : productDto.getSalesProduct()) {
			SalesProduct salesProduct = new SalesProduct(salesProductDto);
			salesProduct.setProductId(result.getId());
			salesProduct.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			salesProduct.setUpdatedBy(loggedUser.getId());
			salesProductRepository.save(salesProduct);
		}
		return new ResponseEntity<Status>(new Status("200", "product  updated  successfully!!!"), HttpStatus.OK);
	}

	 public ResponseEntity<?> priceUpdate(SalesProductDto salesProductDto, User loggedUser) {
		 SalesProduct salesProduct = new SalesProduct(salesProductDto);
		 salesProduct.setOldPrice((salesProductDto.getOldPrice()));
		 salesProduct.setNewPrice((salesProductDto.getNewPrice()));

		 salesProduct.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		 salesProduct.setUpdatedBy(loggedUser.getId());
		 salesProductRepository.save(salesProduct);
	        return new ResponseEntity<Status>(new Status("200", "product price updated  successfully!!!"), HttpStatus.OK);
	    }
	 public ResponseEntity<?> listAllpriceUpdate() throws UsernameNotFoundException {

	        List<SalesProduct> list = salesProductRepository.findAll();
	        if (!list.isEmpty()) {
	            List<SalesProductDto> result = list.stream().map(data -> {
	            	SalesProductDto salesProductDto = new SalesProductDto(data);
	            	salesProductDto.setNewPrice((data.getNewPrice()));

	               // updateEmployeeDetails(salesProductDto);
	                return salesProductDto;
	            }).collect(Collectors.toList());
	            return new ResponseEntity<List<SalesProductDto>>(result, HttpStatus.OK);
	        } else {
	            return new ResponseEntity<List<SalesProductDto>>(new ArrayList<>(), HttpStatus.OK);
	        }
	    }



	public ResponseEntity<?> listAllProduct(Pageable pageable) throws UsernameNotFoundException {

		Page<Product> list = productRepository.findAll(pageable);
		if (!list.toList().isEmpty()) {
			List<ProductDto> result = list.toList().stream().map(data -> {
				ProductDto productDto = new ProductDto(data);
				
				updateEmployeeDetails(productDto);
				return productDto;
			}).collect(Collectors.toList());
			return new ResponseEntity<List<ProductDto>>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<ProductDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getProduct(String id) throws UsernameNotFoundException {
		Optional<Product> optionalProduct = productRepository.findById(Long.parseLong(id));
		ProductDto productDto = new ProductDto(optionalProduct.get());
		updateEmployeeDetails(productDto);
		return new ResponseEntity<ProductDto>(productDto, HttpStatus.OK);
	}

	public ResponseEntity<?> deleteProduct(String id) throws UsernameNotFoundException {
		try {
			Optional<Product> optionalProduct = productRepository.findById(Long.parseLong(id));
			if (optionalProduct.isPresent()) {
				Product lot = optionalProduct.get();
				productRepository.delete(lot);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

	public void updateEmployeeDetails(ProductDto product) {

		Optional<User> user = userRepository.findById(product.getCreatedBy());
		if (user.isPresent()) {
			// product.setCreatedByName(user.get().getEmployeeName());
			// product.setUpdatedByName(user.get().getEmployeeName());
		}
		if (product.getUpdatedBy() != null) {
			Optional<User> user1 = userRepository.findById(product.getUpdatedBy());
			if (user1.isPresent()) {
				// product.setUpdatedByName(user.get().getEmployeeName());
			}
		}
	}

}
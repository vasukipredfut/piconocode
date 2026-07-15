package com.aria.service;

import java.lang.reflect.AnnotatedType;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.dao.ExistingPageDetailsRepository;
import com.aria.dao.FieldsInExistingPageRepository;
import com.aria.dao.PageDetailsRepository;
import com.aria.dao.PageFieldRepository;
import com.aria.dao.SubModulesRepository;
import com.aria.dao.UserRepository;
import com.aria.dto.ExistingPageDto;
import com.aria.dto.FieldsInExistingPageDto;
import com.aria.dto.PageDto;
import com.aria.dto.PageFieldDto;
import com.aria.model.ExistingPageDetails;
import com.aria.model.FieldsInExistingPage;
import com.aria.model.PageDetails;
import com.aria.model.PageField;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.utils.DateUtil;

@Service
public class PageService {

	@Autowired
	PageDetailsRepository pageDetailsRepository;

	@Autowired
	PageFieldRepository pageFieldRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	SubModulesRepository subModulesRepository;

	@Autowired
	FieldsInExistingPageRepository fieldsByPageRepository;

	@Autowired
	ExistingPageDetailsRepository exPageDetailsRepository;

	@Autowired
	DateUtil dateUtil;

	@Autowired
	EntityManager em;

	public ResponseEntity<?> savePage(PageDto pageDto, User loggedUser) {
		PageDetails pageDetails = new PageDetails(pageDto);
		pageDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		pageDetails.setCreatedBy(loggedUser.getId());
		PageDetails pageDetails1 = pageDetailsRepository.save(pageDetails);
		List<PageFieldDto> fieldDtoList = pageDto.getFieldList();
		for (PageFieldDto fieldDto : fieldDtoList) {
			PageField pageField = new PageField(fieldDto);
			pageField.setPageId(pageDetails1.getId());
			pageField.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			pageField.setCreatedBy(loggedUser.getId());
			pageFieldRepository.save(pageField);
		}
		return new ResponseEntity<Status>(new Status("200", "pageDetails saved successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> updatePage(PageDto pageDto, User loggedUser) {
		PageDetails pageDetails = new PageDetails(pageDto);
		pageDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		pageDetails.setCreatedBy(loggedUser.getId());
		PageDetails pageDetails1 = pageDetailsRepository.save(pageDetails);
		List<PageFieldDto> fieldDtoList = pageDto.getFieldList();
		for (PageFieldDto fieldDto : fieldDtoList) {
			PageField pageField = new PageField(fieldDto);
			pageField.setPageId(pageDetails1.getId());
			pageField.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			pageField.setCreatedBy(loggedUser.getId());
			pageFieldRepository.save(pageField);
		}
		return new ResponseEntity<Status>(new Status("200", "pageDetails updated successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> listAllPage(Pageable pageable) throws UsernameNotFoundException {

		Page<PageDetails> pageDetailsList = pageDetailsRepository.findAll(pageable);

		return new ResponseEntity<List<PageDto>>(pageDetailsList.toList().stream().map(user -> {
			PageDto pagedto = new PageDto(user);
			updateEmployeeDetails(pagedto);
			return pagedto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> getPageDetails(String id) throws UsernameNotFoundException {
		Optional<PageDetails> optionalPageDetails = pageDetailsRepository.findById(Long.parseLong(id));
		return new ResponseEntity<PageDto>(new PageDto(optionalPageDetails.get()), HttpStatus.OK);
	}

	public ResponseEntity<?> deletePage(String userId) throws UsernameNotFoundException {

		try {
			Optional<PageDetails> pageDetailsOptional = pageDetailsRepository.findById(Long.parseLong(userId));
			if (pageDetailsOptional.isPresent()) {
				PageDetails user = pageDetailsOptional.get();
				pageDetailsRepository.delete(user);
			}
			return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
		} catch (Exception ex) {
			return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
		}
	}

	public void updateEmployeeDetails(PageDto pageDto) {

		Optional<User> user = userRepository.findById(pageDto.getCreatedBy());
		if (user.isPresent()) {
			pageDto.setCreatedByName(user.get().getEmployeename());
			pageDto.setUpdatedByName(user.get().getEmployeename());
		}
		if (pageDto.getUpdatedBy() != null) {
			Optional<User> user1 = userRepository.findById(pageDto.getUpdatedBy());
			if (user1.isPresent()) {
				pageDto.setUpdatedByName(user.get().getEmployeename());
			}
		}
	}

	public ResponseEntity<?> getAllPages(String pageName) {
		List<String> pageList = new LinkedList<>();
		Metamodel mm = em.getMetamodel();
		for (EntityType<?> e : mm.getEntities()) {
			Class<?> entityClass = e.getJavaType();
			String entityClassName = entityClass.getName();
			String a = entityClassName.split("\\.")[3];
			pageList.add(a);
		}
		if (pageName != null) {
			return new ResponseEntity<List<String>>(
					pageList.stream().filter(str -> str.toLowerCase().trim().contains(pageName.toLowerCase())).sorted()
							.collect(Collectors.toList()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<List<String>>(pageList.stream().sorted().collect(Collectors.toList()),
					HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getAllFieldsByPage(String pageName) throws ClassNotFoundException {

		Optional<ExistingPageDetails> optionalPageDetails = exPageDetailsRepository.findByPageName(pageName);

		if (!optionalPageDetails.isPresent()) {
			Map<String, List<FieldsInExistingPageDto>> fieldMap = new HashMap<>();
			Metamodel mm = em.getMetamodel();
			for (EntityType<?> e : mm.getEntities()) {
				if (e.getJavaType().getName().equals("com.aria.model." + pageName)) {
					Class<?> entityClass = e.getJavaType();
					AnnotatedType[] prop = entityClass.getClass().getAnnotatedInterfaces();
//					List<String> columnNameList = new LinkedList<>();
					//not working below code snippet
//					for(Field field : entityClass.getClass().getDeclaredFields()) {
//						Column column = field.getAnnotation(Column.class);
//						Class<?> fieldType= field.getType();
//						System.out.println(field.getType());
//						if(column!=null) {
//							columnNameList.add(column.name());
//							System.out.println(column.name());
//							} else {
//								System.out.println(field.getName());
//								
//							columnNameList.add(field.getName());
//						}
//						
//					}
					
					//{interface javax.persistence.OneToOne=@javax.persistence.OneToOne(optional=true, targetEntity=void.class, cascade={}, orphanRemoval=false, mappedBy="", fetch=EAGER), 
					//interface javax.persistence.JoinColumn=
					//@javax.persistence.JoinColumn(referencedColumnName="id", nullable=true,
					//unique=false, name="from_branch_id", updatable=true, columnDefinition="", foreignKey=@javax.persistence.ForeignKey(name="", value=PROVIDER_DEFAULT, foreignKeyDefinition=""), table="", insertable=true)}
						
					for (Attribute<?, ?> a : e.getAttributes()) {
						String dt = a.getJavaMember().toString().split(" ")[1].split("\\.")[2];
						String fieldName = a.getName();
						FieldsInExistingPageDto fields = new FieldsInExistingPageDto(dt, fieldName,
								entityClass.getAnnotation(Table.class).name());

						fieldMap.computeIfAbsent(dt, k -> new LinkedList<>()).add(fields);
//				private java.time.LocalDateTime com.aria.model.BillInward.updated_date
					}
				}
			}
			return new ResponseEntity<Map<String, List<FieldsInExistingPageDto>>>(fieldMap, HttpStatus.OK);
		} else {
			return new ResponseEntity<ExistingPageDetails>(optionalPageDetails.get(), HttpStatus.OK);
		}
	}

	public ResponseEntity<?> getAllTables(String table) {
		List<String> pageList = new LinkedList<>();
		Metamodel mm = em.getMetamodel();
		for (EntityType<?> e : mm.getEntities()) {
			Class<?> entityClass = e.getJavaType();
			String entityTableName = entityClass.getAnnotation(Table.class).name();
			pageList.add(entityTableName);
		}
		if (table != null) {
			return new ResponseEntity<List<String>>(
					pageList.stream().filter(str -> str.toLowerCase().trim().contains(table.toLowerCase())).sorted()
							.collect(Collectors.toList()),
					HttpStatus.OK);
		} else {
			return new ResponseEntity<List<String>>(pageList.stream().sorted().collect(Collectors.toList()),
					HttpStatus.OK);
		}
	}

	public ResponseEntity<?> saveFieldsInExistingPage(ExistingPageDto existingPageDto, User user) {
		ExistingPageDetails pageDetails = new ExistingPageDetails(existingPageDto);
		pageDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		pageDetails.setCreatedBy(user.getId());
		pageDetails.setPageCreatedDate(dateUtil.getStringTODateDDMMYYYY(existingPageDto.getPageCreatedDate()));
		ExistingPageDetails pageDetails1 = exPageDetailsRepository.save(pageDetails);

		for (FieldsInExistingPageDto fieldDto : existingPageDto.getFieldsList()) {
			FieldsInExistingPage pageField = new FieldsInExistingPage(fieldDto);
			pageField.setPageId(pageDetails1.getId());
			pageField.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
			pageField.setCreatedBy(user.getId());
			fieldsByPageRepository.save(pageField);
		}
		return new ResponseEntity<Status>(new Status("200", "Details saved successfully!!!"), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllFieldsTables(String table) {
		List<ExistingPageDetails> pageDetailsList = exPageDetailsRepository.findAll();

		return new ResponseEntity<List<ExistingPageDto>>(pageDetailsList.stream().map(user -> {
			ExistingPageDto pagedto = new ExistingPageDto(user);
			return pagedto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllScreens(String screenName) {
		List<ExistingPageDetails> pageDetailsList = exPageDetailsRepository.findAllByPageName(screenName);

		return new ResponseEntity<List<ExistingPageDto>>(pageDetailsList.stream().map(user -> {
			ExistingPageDto pagedto = new ExistingPageDto(user, "pageDetailsAlone");
			pagedto.setPageCreatedDate(dateUtil.getDateToStringDDMMYYYY(user.getPageCreatedDate()));
			return pagedto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	}

}

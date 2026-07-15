package com.aria.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;

@Service
public class FoodItemService {

    @Autowired
    FoodItemRepository foodItemRepository;

    @Autowired
    RequiredItemFoodRepository requiredItemFoodRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;

    public ResponseEntity<?> saveFoodItem(FoodItemDto foodItemDto, User loggedUser) {
        FoodItem foodItem = new FoodItem(foodItemDto);
        foodItem.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        foodItem.setCreatedBy(loggedUser.getId());
        FoodItem result = foodItemRepository.save(foodItem);
        for (RequiredItemFoodDto requiredItemFoodDto : foodItemDto.getRequiredItemFoodDtos()) {
            RequiredItemFood requiredItemFood = new RequiredItemFood(requiredItemFoodDto);
            requiredItemFood.setFoodItemId(result.getId());
            requiredItemFoodRepository.save(requiredItemFood);
        }
        return new ResponseEntity<Status>(new Status("200", "Food item save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateFoodItem(FoodItemDto foodItemDto, User loggedUser) {
        FoodItem foodItem = new FoodItem(foodItemDto);
        foodItem.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        foodItem.setUpdatedBy(loggedUser.getId());
        FoodItem result = foodItemRepository.save(foodItem);
        for (RequiredItemFoodDto requiredItemFoodDto : foodItemDto.getRequiredItemFoodDtos()) {
            RequiredItemFood requiredItemFood = new RequiredItemFood(requiredItemFoodDto);
            requiredItemFood.setFoodItemId(result.getId());
            requiredItemFoodRepository.save(requiredItemFood);
        }
        return new ResponseEntity<Status>(new Status("200", "Food Item updated  successfully!!!"), HttpStatus.OK);
    }

/*	public ResponseEntity<?> getFoodItemListByFoodNameAndStartDateAndEndDateAndOrganisationId(String foodName,
			String startDate, String endDate, User loggedInUser) {
		LocalDateTime startDateLocalDateTime = null, endDateLocalDateTime = null;
		
		if ((startDate != null && !startDate.isEmpty()) && (endDate != null && !endDate.isEmpty())) {
			startDateLocalDateTime = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(startDate))
					.with(LocalTime.MIN);
			endDateLocalDateTime = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(endDate))
					.with(LocalTime.MAX);
		} else if ((startDate == null || startDate.isEmpty()) && (endDate != null && !endDate.isEmpty())) {
			startDateLocalDateTime = null;
			endDateLocalDateTime = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(endDate))
					.with(LocalTime.MAX);
		} else if ((startDate != null && !startDate.isEmpty()) && (endDate == null || endDate.isEmpty())) {
			startDateLocalDateTime = dateUtil.getDateToLocalDateTime(dateUtil.getStringTODateF1(startDate))
					.with(LocalTime.MIN);
			endDateLocalDateTime = null;
		}
	}
/*		List<FoodItem> foodItemList = foodItemRepository
				.getFoodItemListByFoodNameAndStartDateAndEndDateAndOrganisationId(foodName, startDateLocalDateTime,
						endDateLocalDateTime, loggedInUser.getOrganisation().getId());
		return new ResponseEntity<List<FoodItemDto>>(foodItemList.stream().map(foodItem -> {
			FoodItemDto foodItemDto = new FoodItemDto(foodItem, false);
			foodItemDto.setCreated_date_String(dateUtil.getLocalDateTimeToStringDDMMMYYYY(foodItem.getCreated_date()));
			Optional<User> userOptional = userRepository.findById(foodItem.getCreatedBy());
			if (userOptional.isPresent()) {
				foodItemDto.setCreatedByEmployeeName(userOptional.get().getEmployeename());
			}
			return foodItemDto;
		}).collect(Collectors.toList()), HttpStatus.OK);
	} */

    public ResponseEntity<?> listAllfoodItem() throws UsernameNotFoundException {
        List<FoodItem> list = foodItemRepository.findAllFood();
        if (!list.isEmpty()) {
            List<FoodItemDto> result = list.stream().map(data -> {
                FoodItemDto foodItemDto = new FoodItemDto(data);

                return foodItemDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<FoodItemDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<FoodItemDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getFoodItem(String id) throws UsernameNotFoundException {
        Optional<FoodItem> optionalFoodItem = foodItemRepository.findById(Long.parseLong(id));
        FoodItemDto foodItemDto = new FoodItemDto(optionalFoodItem.get());
        return new ResponseEntity<FoodItemDto>(foodItemDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteFoodItem(String id) throws UsernameNotFoundException {
        try {
            Optional<FoodItem> optionalFoodItem = foodItemRepository.findById(Long.parseLong(id));
            if (optionalFoodItem.isPresent()) {
                FoodItem foodItem = optionalFoodItem.get();
                foodItemRepository.delete(foodItem);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }


    public ResponseEntity<?> FoodItemfilterList(String foodName, String dateFrom, String dateTo, Pageable pageable)
            throws UsernameNotFoundException {
        Page<FoodItem> list = foodItemRepository.getFoodItemFilterList(foodName, dateUtil.getStringTODateF1(dateFrom),
                dateUtil.getStringTODateF1(dateTo), pageable);
        if (!list.toList().isEmpty()) {
            List<FoodItemDto> result = list.toList().stream().map(data -> {
                FoodItemDto foodItemDto = new FoodItemDto(data);
                return foodItemDto;
            }).collect(Collectors.toList());
            PageDataDto<FoodItemDto> pageData = new PageDataDto<>(result, list);
            return new ResponseEntity<PageDataDto<FoodItemDto>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<FoodItemDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }
}

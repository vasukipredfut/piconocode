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
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;

@Service
public class CanteenMenuService {

    @Autowired
    CanteenMenuRepository canteenMenuRepository;

    @Autowired
    MenuItemsRepository menuItemsRepository;

    @Autowired
    CanteenTokenRepository canteenTokenRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CanteenAttendanceRepository canteenAttendanceRepository;

    @Autowired
    CanteenAttendanceMenuRepository canteenAttendanceMenuRepository;

    @Autowired
    CanteenAttendanceTokenRepository canteenAttendanceTokenRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    CanteenMenuDayMappingRepository canteenMenuDayMappingRepository;

    public ResponseEntity<?> saveCanteenMenu(CanteenMenuDto canteenMenuDto, User loggedUser) {
        CanteenMenu canteenMenu = new CanteenMenu(canteenMenuDto);
        canteenMenu.setFromDate(dateUtil.getStringTODateF1(canteenMenuDto.getFromDate()));
        if (canteenMenuDto.getToDate() != null) {
            canteenMenu.setToDate(dateUtil.getStringTODateF1(canteenMenuDto.getToDate()));
        }
        canteenMenu.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenMenu.setCreatedBy(loggedUser.getId());
        CanteenMenu result = canteenMenuRepository.save(canteenMenu);
        if (canteenMenuDto.getDayIds() != null & !canteenMenuDto.getDayIds().isEmpty()) {
            for (Long id : canteenMenuDto.getDayIds()) {
                CanteenMenuDayMapping canteenMenuDayMapping = new CanteenMenuDayMapping();
                canteenMenuDayMapping.setFoodId(result.getId());
                Day day = new Day();
                day.setId(id);
                canteenMenuDayMapping.setDay(day);
                canteenMenuDayMappingRepository.save(canteenMenuDayMapping);
            }
        }

        if (canteenMenuDto.getBreakfastItems() != null) {
            if (!canteenMenuDto.getBreakfastItems().isEmpty()) {
                for (MenuItemsDto menuItemsDto : canteenMenuDto.getBreakfastItems()) {
                    MenuItems menuItems = new MenuItems(menuItemsDto);
                    menuItems.setFoodId(result.getId());
                    menuItemsRepository.save(menuItems);
                }
            }
        }
        if (canteenMenuDto.getLunchItems() != null) {
            if (!canteenMenuDto.getLunchItems().isEmpty()) {
                for (MenuItemsDto menuItemsDto : canteenMenuDto.getLunchItems()) {
                    MenuItems menuItems = new MenuItems(menuItemsDto);
                    menuItems.setFoodId(result.getId());
                    menuItemsRepository.save(menuItems);
                }
            }
        }
        if (canteenMenuDto.getDinnerItems() != null) {
            if (!canteenMenuDto.getDinnerItems().isEmpty()) {
                for (MenuItemsDto menuItemsDto : canteenMenuDto.getDinnerItems()) {
                    MenuItems menuItems = new MenuItems(menuItemsDto);
                    menuItems.setFoodId(result.getId());
                    menuItemsRepository.save(menuItems);
                }
            }
        }
        if (canteenMenuDto.getBeverageItems() != null) {
            if (!canteenMenuDto.getBeverageItems().isEmpty()) {
                for (MenuItemsDto menuItemsDto : canteenMenuDto.getBeverageItems()) {
                    MenuItems menuItems = new MenuItems(menuItemsDto);
                    menuItems.setFoodId(result.getId());
                    menuItemsRepository.save(menuItems);
                }
            }
        }
        if (canteenMenuDto.getSnacksItems() != null) {
            if (!canteenMenuDto.getSnacksItems().isEmpty()) {
                for (MenuItemsDto menuItemsDto : canteenMenuDto.getSnacksItems()) {
                    MenuItems menuItems = new MenuItems(menuItemsDto);
                    menuItems.setFoodId(result.getId());
                    menuItemsRepository.save(menuItems);
                }
            }
        }
        return new ResponseEntity<Status>(new Status("200", "MenuItems save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCanteenMenu(CanteenMenuDto canteenMenuDto, User loggedUser) {
        CanteenMenu canteenMenu = new CanteenMenu(canteenMenuDto);
        canteenMenu.setFromDate(dateUtil.getStringTODateF1(canteenMenuDto.getFromDate()));
        if (canteenMenuDto.getToDate() != null) {
            canteenMenu.setToDate(dateUtil.getStringTODateF1(canteenMenuDto.getToDate()));
        }
        canteenMenu.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenMenu.setUpdatedBy(loggedUser.getId());
        CanteenMenu result = canteenMenuRepository.save(canteenMenu);
        if (canteenMenuDto.getDayIds() != null & !canteenMenuDto.getDayIds().isEmpty()) {
            for (Long id : canteenMenuDto.getDayIds()) {
                Optional<CanteenMenuDayMapping> optional = canteenMenuDayMappingRepository
                        .findByCanteenMenuDay(result.getId(), id);
                if (!optional.isPresent()) {
                    CanteenMenuDayMapping canteenMenuDayMapping = new CanteenMenuDayMapping();
                    canteenMenuDayMapping.setFoodId(result.getId());
                    Day day = new Day();
                    day.setId(id);
                    canteenMenuDayMapping.setDay(day);
                    canteenMenuDayMappingRepository.save(canteenMenuDayMapping);
                }
            }
        }
        if (canteenMenuDto.getBreakfastItems() != null && !canteenMenuDto.getBreakfastItems().isEmpty()) {
            for (MenuItemsDto menuItemsDto : canteenMenuDto.getBreakfastItems()) {
                MenuItems menuItems = new MenuItems(menuItemsDto);
                menuItems.setFoodId(result.getId());
                menuItemsRepository.save(menuItems);
            }
        }
        if (canteenMenuDto.getLunchItems() != null && !canteenMenuDto.getLunchItems().isEmpty()) {
            for (MenuItemsDto menuItemsDto : canteenMenuDto.getLunchItems()) {
                MenuItems menuItems = new MenuItems(menuItemsDto);
                menuItems.setFoodId(result.getId());
                menuItemsRepository.save(menuItems);
            }
        }
        if (canteenMenuDto.getDinnerItems() != null && !canteenMenuDto.getDinnerItems().isEmpty()) {
            for (MenuItemsDto menuItemsDto : canteenMenuDto.getDinnerItems()) {
                MenuItems menuItems = new MenuItems(menuItemsDto);
                menuItems.setFoodId(result.getId());
                menuItemsRepository.save(menuItems);
            }
        }
        if (canteenMenuDto.getBeverageItems() != null && !canteenMenuDto.getBeverageItems().isEmpty()) {
            for (MenuItemsDto menuItemsDto : canteenMenuDto.getBeverageItems()) {
                MenuItems menuItems = new MenuItems(menuItemsDto);
                menuItems.setFoodId(result.getId());
                menuItemsRepository.save(menuItems);
            }
        }
        if (canteenMenuDto.getSnacksItems() != null && !canteenMenuDto.getSnacksItems().isEmpty()) {
            for (MenuItemsDto menuItemsDto : canteenMenuDto.getSnacksItems()) {
                MenuItems menuItems = new MenuItems(menuItemsDto);
                menuItems.setFoodId(result.getId());
                menuItemsRepository.save(menuItems);
            }
        }
        return new ResponseEntity<Status>(new Status("200", "MenuItems  updated  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllCanteenMenu(Pageable pageable) throws UsernameNotFoundException {
        Page<CanteenMenu> canteenMenuPage = canteenMenuRepository.findAll(pageable);
        List<CanteenMenuDto> dtoList = canteenMenuPage.stream()
                .map(user -> {
                    CanteenMenuDto dto = new CanteenMenuDto(user, true);
                    dto.setFromDate(dateUtil.getStringTODate(user.getFromDate()));
                    dto.setToDate(dateUtil.getStringTODate(user.getToDate()));
                    updateEmployeeDetails(dto);
                    return dto;
                }).collect(Collectors.toList());
        PageDataDto<CanteenMenuDto> pageData = new PageDataDto<>(dtoList, canteenMenuPage);
        return new ResponseEntity<>(pageData, HttpStatus.OK);
    }

    public ResponseEntity<?> getCanteenMenuDetails(String id) throws UsernameNotFoundException {
        Optional<CanteenMenu> optionalCanteenMenu = canteenMenuRepository.findById(Long.parseLong(id));
        CanteenMenuDto canteenMenuDto = new CanteenMenuDto(optionalCanteenMenu.get(), true);
        canteenMenuDto.setFromDate(dateUtil.getDateToStringDDMMYYYY(optionalCanteenMenu.get().getFromDate()));
        canteenMenuDto.setToDate(dateUtil.getDateToStringDDMMYYYY(optionalCanteenMenu.get().getToDate()));
        return new ResponseEntity<CanteenMenuDto>(canteenMenuDto, HttpStatus.OK);
    }

    public ResponseEntity<?> canteenMenufilterList(Long dayId, Long foodItemId, String dateFrom, String dateTo)
            throws UsernameNotFoundException {
        List<CanteenMenu> list = null;
        if (dateFrom != null && dateTo != null) {
            list = canteenMenuRepository.canteenMenufilterList(dateUtil.getStringTODateF1(dateFrom),
                    dateUtil.getStringTODateF1(dateTo));
        } else if (dateFrom != null && dateTo == null) {
            list = canteenMenuRepository.canteenMenufilterListFromDate(dateUtil.getStringTODateF1(dateFrom));
        } else if (dateFrom == null && dateTo != null) {
            list = canteenMenuRepository.canteenMenufilterListToDate(dateUtil.getStringTODateF1(dateTo));
        }
        List<CanteenMenuDto> result = new ArrayList<>();

        if (!list.isEmpty()) {
            for (CanteenMenu canteenMenu : list) {
                CanteenMenuDto canteenMenuDto = new CanteenMenuDto(canteenMenu, false);
                List<CanteenMenuDayMapping> canteenMenuDayMappings = canteenMenuDayMappingRepository
                        .findAll(canteenMenuDto.getId(), dayId);
                boolean daystatus = false;
                boolean fooditem = false;
                if (dayId != null) {
                    if (canteenMenuDayMappings != null && !canteenMenuDayMappings.isEmpty()) {
                        daystatus = true;
                        canteenMenuDto.setDays(
                                canteenMenuDayMappings != null
                                        ? canteenMenuDayMappings.stream().map(field -> new CanteenMenuDayDto(field))
                                        .collect(Collectors.toList())
                                        : null);
                    }
                }
                if (foodItemId != null) {
                    List<MenuItems> dinner = menuItemsRepository.findAll(canteenMenuDto.getId(), foodItemId, "dinner");

                    if (dinner != null && !dinner.isEmpty()) {
                        daystatus = true;
                        canteenMenuDto.setDinnerItems(dinner != null
                                ? dinner.stream().map(field -> new MenuItemsDto(field)).collect(Collectors.toList())
                                : null);
                    }
                    List<MenuItems> breakfast = menuItemsRepository.findAll(canteenMenuDto.getId(), foodItemId,
                            "breakfast");

                    if (breakfast != null && !breakfast.isEmpty()) {
                        fooditem = true;
                        canteenMenuDto.setBreakfastItems(breakfast != null
                                ? breakfast.stream().map(field -> new MenuItemsDto(field)).collect(Collectors.toList())
                                : null);
                    }
                    List<MenuItems> lunch = menuItemsRepository.findAll(canteenMenuDto.getId(), foodItemId, "lunch");

                    if (lunch != null && !lunch.isEmpty()) {
                        fooditem = true;
                        canteenMenuDto.setLunchItems(lunch != null
                                ? lunch.stream().map(field -> new MenuItemsDto(field)).collect(Collectors.toList())
                                : null);
                    }
                    List<MenuItems> beverage = menuItemsRepository.findAll(canteenMenuDto.getId(), foodItemId,
                            "beverage");
                    if (beverage != null && !beverage.isEmpty()) {
                        fooditem = true;
                        canteenMenuDto.setBeverageItems(beverage != null
                                ? beverage.stream().map(field -> new MenuItemsDto(field)).collect(Collectors.toList())
                                : null);
                    }
                    List<MenuItems> snacks = menuItemsRepository.findAll(canteenMenuDto.getId(), foodItemId, "snacks");
                    if (snacks != null && !snacks.isEmpty()) {
                        fooditem = true;
                        canteenMenuDto.setSnacksItems(snacks != null
                                ? snacks.stream().map(field -> new MenuItemsDto(field)).collect(Collectors.toList())
                                : null);
                    }
                }
                canteenMenuDto.setFromDate(dateUtil.getStringTODate(canteenMenu.getFromDate()));
                canteenMenuDto.setToDate(dateUtil.getStringTODate(canteenMenu.getToDate()));

                if (dayId != null && foodItemId != null) {
                    if (daystatus && fooditem) {
                        result.add(canteenMenuDto);
                    }
                } else if (dayId != null && daystatus) {
                    result.add(canteenMenuDto);
                } else if (dayId != null && daystatus) {
                    result.add(canteenMenuDto);
                }

            }
            return new ResponseEntity<List<CanteenMenuDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CanteenMenuDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> deleteCanteenMenu(String id) throws UsernameNotFoundException {
        try {
            Optional<CanteenMenu> optionalCanteenMenu = canteenMenuRepository.findById(Long.parseLong(id));
            if (optionalCanteenMenu.isPresent()) {
                CanteenMenu canteenMenu = optionalCanteenMenu.get();
                canteenMenuRepository.delete(canteenMenu);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(CanteenMenuDto canteenMenuDto) {

        /*
         * Optional<User> user = userRepository.findById(medicalCheckup.getCreatedBy());
         * if (user.isPresent()) {
         * pageDto.setCreatedByName(user.get().getEmployeeName());
         * pageDto.setUpdatedByName(user.get().getEmployeeName()); } if
         * (medicalCheckup.getUpdatedBy() != null) { Optional<User> user1 =
         * userRepository.findById(medicalCheckup.getUpdatedBy()); if
         * (user1.isPresent()) {
         * medicalCheckup.setUpdatedByName(user.get().getEmployeeName()); } }
         */
    }

    // canteen token

    public ResponseEntity<?> saveCanteenToken(CanteenTokenDto canteenTokenDto, User loggedUser) {
        CanteenToken canteenToken = new CanteenToken(canteenTokenDto);
        canteenToken.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenToken.setCreatedBy(loggedUser.getId());
        canteenToken.setDate(dateUtil.getStringTODateF1(canteenTokenDto.getDate()));
        canteenTokenRepository.save(canteenToken);
        return new ResponseEntity<Status>(new Status("200", "canteenToken saved successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCanteenToken(CanteenTokenDto canteenTokenDto, User loggedUser) {
        CanteenToken canteenToken = new CanteenToken(canteenTokenDto);
        canteenToken.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenToken.setUpdatedBy(loggedUser.getId());
        canteenToken.setDate(dateUtil.getStringTODateF1(canteenTokenDto.getDate()));

        CanteenToken result = canteenTokenRepository.save(canteenToken);
        // updateReceived(result.getId(), loggedUser);
        return new ResponseEntity<Status>(new Status("200", "canteenToken updated successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllCanteenToken() throws UsernameNotFoundException {
        List<CanteenToken> canteenToken = canteenTokenRepository.findAll();
        return new ResponseEntity<List<CanteenTokenDto>>(canteenToken.stream().map(user -> {
            CanteenTokenDto canteenTokenDto = new CanteenTokenDto(user);
            canteenTokenDto.setDate(dateUtil.getStringTODate(user.getDate()));
            updateEmployeeDetails(canteenTokenDto);
            return canteenTokenDto;
        }).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> canteenTokenfilterList(Long userId, String tokenFor, Long shiftId, String dateFrom,
                                                    String dateTo) throws UsernameNotFoundException {
        List<CanteenToken> list = canteenTokenRepository.canteenTokenfilterList(userId, tokenFor, shiftId,
                dateUtil.getStringTODateF1(dateFrom), dateUtil.getStringTODateF1(dateTo));
        if (!list.isEmpty()) {
            List<CanteenTokenDto> result = list.stream().map(data -> {
                CanteenTokenDto canteenTokenDto = new CanteenTokenDto(data);
                canteenTokenDto.setDate(dateUtil.getStringTODate(data.getDate()));
                return canteenTokenDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<CanteenTokenDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CanteenTokenDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getCanteenTokenDetails(String id) throws UsernameNotFoundException {
        Optional<CanteenToken> optionalCanteenToken = canteenTokenRepository.findById(Long.parseLong(id));
        CanteenTokenDto canteenTokenDto = new CanteenTokenDto(optionalCanteenToken.get());
        canteenTokenDto.setDate(dateUtil.getStringTODate(optionalCanteenToken.get().getDate()));
        return new ResponseEntity<CanteenTokenDto>(canteenTokenDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deletecanteenToken(String id) throws UsernameNotFoundException {
        try {
            Optional<CanteenToken> canteenTokenOptional = canteenTokenRepository.findById(Long.parseLong(id));
            if (canteenTokenOptional.isPresent()) {
                CanteenToken canteenToken = canteenTokenOptional.get();
                canteenTokenRepository.delete(canteenToken);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public void updateEmployeeDetails(CanteenTokenDto canteenTokenDto) {

//		 * Optional<User> user = userRepository.findById(medicalCheckup.getCreatedBy());
//		 * if (user.isPresent()) {
//		 * //pageDto.setCreatedByName(user.get().getEmployeeName());
//		 * //pageDto.setUpdatedByName(user.get().getEmployeeName()); } if
//		 * (medicalCheckup.getUpdatedBy() != null) { Optional<User> user1 =
//		 * userRepository.findById(medicalCheckup.getUpdatedBy()); if
//		 * (user1.isPresent()) { //
//		 * medicalCheckup.setUpdatedByName(user.get().getEmployeeName()); } }

    }

//canteen Attendance

    public ResponseEntity<?> saveCanteenAttendance(CanteenAttendanceDto canteenAttendanceDto, User loggedUser) {
        CanteenAttendance canteenAttendance = new CanteenAttendance(canteenAttendanceDto);
        canteenAttendance.setDate(dateUtil.getStringTODateF1(canteenAttendanceDto.getDate()));

        canteenAttendance.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenAttendance.setCreatedBy(loggedUser.getId());
        CanteenAttendance result = canteenAttendanceRepository.save(canteenAttendance);
        if (canteenAttendanceDto.getBreakfast() != null) {
            if (!canteenAttendanceDto.getBreakfast().isEmpty()) {
                for (CanteenAttendanceMenuDto canteenAttendanceMenuDto : canteenAttendanceDto.getBreakfast()) {
                    CanteenAttendanceMenu canteenAttendanceMenu = new CanteenAttendanceMenu(canteenAttendanceMenuDto);
                    canteenAttendanceMenu.setAttendanceMenuId(result.getId());
                    canteenAttendanceMenu.setInTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getInTime()));
                    canteenAttendanceMenu.setOutTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getOutTime()));

                    canteenAttendanceMenuRepository.save(canteenAttendanceMenu);

                }
            }
        }
        if (canteenAttendanceDto.getLunch() != null) {
            if (!canteenAttendanceDto.getLunch().isEmpty()) {
                for (CanteenAttendanceMenuDto canteenAttendanceMenuDto : canteenAttendanceDto.getLunch()) {
                    CanteenAttendanceMenu canteenAttendanceMenu = new CanteenAttendanceMenu(canteenAttendanceMenuDto);
                    canteenAttendanceMenu.setAttendanceMenuId(result.getId());
                    canteenAttendanceMenu.setInTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getInTime()));
                    canteenAttendanceMenu.setOutTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getOutTime()));
                    canteenAttendanceMenuRepository.save(canteenAttendanceMenu);

                }
            }
        }
        if (canteenAttendanceDto.getDinner() != null) {
            if (!canteenAttendanceDto.getDinner().isEmpty()) {
                for (CanteenAttendanceMenuDto canteenAttendanceMenuDto : canteenAttendanceDto.getDinner()) {
                    CanteenAttendanceMenu canteenAttendanceMenu = new CanteenAttendanceMenu(canteenAttendanceMenuDto);
                    canteenAttendanceMenu.setAttendanceMenuId(result.getId());
                    canteenAttendanceMenu.setInTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getInTime()));
                    canteenAttendanceMenu.setOutTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getOutTime()));
                    canteenAttendanceMenuRepository.save(canteenAttendanceMenu);
                }
            }
        }
        return new ResponseEntity<Status>(new Status("200", "canteen attendance save successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCanteenAttendance(CanteenAttendanceDto canteenAttendanceDto, User loggedUser) {
        CanteenAttendance canteenAttendance = new CanteenAttendance(canteenAttendanceDto);
        canteenAttendance.setDate(dateUtil.getStringTODateF1(canteenAttendanceDto.getDate()));

        canteenAttendance.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenAttendance.setUpdatedBy(loggedUser.getId());
        CanteenAttendance result = canteenAttendanceRepository.save(canteenAttendance);
        if (!canteenAttendanceDto.getBreakfast().isEmpty()) {
            for (CanteenAttendanceMenuDto canteenAttendanceMenuDto : canteenAttendanceDto.getBreakfast()) {
                CanteenAttendanceMenu canteenAttendanceMenu = new CanteenAttendanceMenu(canteenAttendanceMenuDto);
                canteenAttendanceMenu.setAttendanceMenuId(result.getId());
                canteenAttendanceMenu.setInTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getInTime()));
                canteenAttendanceMenu.setOutTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getOutTime()));
                canteenAttendanceMenuRepository.save(canteenAttendanceMenu);
            }
        }

        if (!canteenAttendanceDto.getLunch().isEmpty()) {
            for (CanteenAttendanceMenuDto canteenAttendanceMenuDto : canteenAttendanceDto.getLunch()) {
                CanteenAttendanceMenu canteenAttendanceMenu = new CanteenAttendanceMenu(canteenAttendanceMenuDto);
                canteenAttendanceMenu.setAttendanceMenuId(result.getId());
                canteenAttendanceMenu.setInTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getInTime()));
                canteenAttendanceMenu.setOutTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getOutTime()));
                canteenAttendanceMenuRepository.save(canteenAttendanceMenu);
            }
        }
        if (!canteenAttendanceDto.getDinner().isEmpty()) {
            for (CanteenAttendanceMenuDto canteenAttendanceMenuDto : canteenAttendanceDto.getDinner()) {
                CanteenAttendanceMenu canteenAttendanceMenu = new CanteenAttendanceMenu(canteenAttendanceMenuDto);
                canteenAttendanceMenu.setAttendanceMenuId(result.getId());
                canteenAttendanceMenu.setInTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getInTime()));
                canteenAttendanceMenu.setOutTime(dateUtil.getStringTODateF1(canteenAttendanceMenuDto.getOutTime()));
                canteenAttendanceMenuRepository.save(canteenAttendanceMenu);
            }
        }

        return new ResponseEntity<Status>(new Status("200", "canteen attendance  updated  successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listAllCanteenAttendance(String typeKey) throws UsernameNotFoundException {
        List<CanteenAttendance> list = canteenAttendanceRepository.findByType(typeKey);
        if (!list.isEmpty()) {
            List<CanteenAttendanceDto> result = list.stream().map(data -> {
                CanteenAttendanceDto canteenAttendanceDto = new CanteenAttendanceDto(data);
                canteenAttendanceDto.setDate(dateUtil.getStringTODate(data.getDate()));
//snacks and tea separate cate
                if (typeKey.equalsIgnoreCase("breakfast")) {
                    List<CanteenAttendanceMenu> breakfast = canteenAttendanceMenuRepository
                            .findAllById(canteenAttendanceDto.getId());
                    List<CanteenAttendanceMenuDto> canteenAttendanceMenuDtos = breakfast.stream().map(data1 -> {

                        CanteenAttendanceMenuDto canteenAttendance = new CanteenAttendanceMenuDto(data1);
                        canteenAttendance.setInTime(dateUtil.getTime(data1.getInTime()));
                        canteenAttendance.setOutTime(dateUtil.getTime(data1.getOutTime()));
                        return canteenAttendance;
                    }).collect(Collectors.toList());
                    canteenAttendanceDto.setBreakfast(canteenAttendanceMenuDtos);
                }
                if (typeKey.equalsIgnoreCase("lunch")) {
                    List<CanteenAttendanceMenu> lunch = canteenAttendanceMenuRepository
                            .findAllById(canteenAttendanceDto.getId());
                    List<CanteenAttendanceMenuDto> canteenAttendanceMenuDtos1 = lunch.stream().map(data1 -> {

                        CanteenAttendanceMenuDto canteenAttendance = new CanteenAttendanceMenuDto(data1);
                        canteenAttendance.setInTime(dateUtil.getTime(data1.getInTime()));
                        canteenAttendance.setOutTime(dateUtil.getTime(data1.getOutTime()));
                        return canteenAttendance;
                    }).collect(Collectors.toList());

                    canteenAttendanceDto.setLunch(canteenAttendanceMenuDtos1);
                }
                if (typeKey.equalsIgnoreCase("dinner")) {
                    List<CanteenAttendanceMenu> dinner = canteenAttendanceMenuRepository
                            .findAllById(canteenAttendanceDto.getId());
                    List<CanteenAttendanceMenuDto> canteenAttendanceMenuDtos2 = dinner.stream().map(data1 -> {

                        CanteenAttendanceMenuDto canteenAttendance = new CanteenAttendanceMenuDto(data1);
                        canteenAttendance.setInTime(dateUtil.getTime(data1.getInTime()));
                        canteenAttendance.setOutTime(dateUtil.getTime(data1.getOutTime()));
                        return canteenAttendance;
                    }).collect(Collectors.toList());
                    canteenAttendanceDto.setDinner(canteenAttendanceMenuDtos2);
                }
                return canteenAttendanceDto;

            }).collect(Collectors.toList());

            return new ResponseEntity<List<CanteenAttendanceDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CanteenAttendanceDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getCanteenAttendanceDetails(String id) throws UsernameNotFoundException {
        Optional<CanteenAttendance> optionalCanteenAttendance = canteenAttendanceRepository
                .findById(Long.parseLong(id));
        ;
        if (optionalCanteenAttendance.isPresent()) {
            CanteenAttendanceDto canteenAttendanceDto = new CanteenAttendanceDto(optionalCanteenAttendance.get());
            canteenAttendanceDto.setDate(dateUtil.getStringTODate(optionalCanteenAttendance.get().getDate()));
            List<CanteenAttendanceMenu> breakfast = canteenAttendanceMenuRepository
                    .findAllById(canteenAttendanceDto.getId());
            List<CanteenAttendanceMenuDto> canteenAttendanceMenuDtos = breakfast.stream().map(data1 -> {

                CanteenAttendanceMenuDto canteenAttendance = new CanteenAttendanceMenuDto(data1);
                canteenAttendance.setInTime(dateUtil.getTime(data1.getInTime()));
                canteenAttendance.setOutTime(dateUtil.getTime(data1.getOutTime()));
                return canteenAttendance;
            }).collect(Collectors.toList());

            canteenAttendanceDto.setBreakfast(canteenAttendanceMenuDtos);
            List<CanteenAttendanceMenu> lunch = canteenAttendanceMenuRepository
                    .findAllById(canteenAttendanceDto.getId());
            List<CanteenAttendanceMenuDto> canteenAttendanceMenuDtos1 = lunch.stream().map(data1 -> {

                CanteenAttendanceMenuDto canteenAttendance = new CanteenAttendanceMenuDto(data1);
                canteenAttendance.setInTime(dateUtil.getTime(data1.getInTime()));
                canteenAttendance.setOutTime(dateUtil.getTime(data1.getOutTime()));
                return canteenAttendance;
            }).collect(Collectors.toList());

            canteenAttendanceDto.setLunch(canteenAttendanceMenuDtos1);
            List<CanteenAttendanceMenu> dinner = canteenAttendanceMenuRepository
                    .findAllById(canteenAttendanceDto.getId());
            List<CanteenAttendanceMenuDto> canteenAttendanceMenuDtos2 = dinner.stream().map(data1 -> {

                CanteenAttendanceMenuDto canteenAttendance = new CanteenAttendanceMenuDto(data1);
                canteenAttendance.setInTime(dateUtil.getTime(data1.getInTime()));
                canteenAttendance.setOutTime(dateUtil.getTime(data1.getOutTime()));
                return canteenAttendance;
            }).collect(Collectors.toList());

            canteenAttendanceDto.setDinner(canteenAttendanceMenuDtos2);

            return new ResponseEntity<CanteenAttendanceDto>(canteenAttendanceDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    public ResponseEntity<?> getAllData(Pageable pageable) {
    	Page<Tuple> list = canteenAttendanceRepository.getAllData(pageable);
		if (!list.toList().isEmpty()) {
			  List<CanteenAttendanceMenuGroupCountDto> result = list.toList().stream().map(data ->
			  new CanteenAttendanceMenuGroupCountDto(data.get("date"), data.get("count"), data.get("type"))
					  ).collect(Collectors.toList());
			PageDataDto<CanteenAttendanceMenuGroupCountDto> pageData = new PageDataDto<>(result, list);
			return new ResponseEntity<PageDataDto<CanteenAttendanceMenuGroupCountDto>>(pageData, HttpStatus.OK);
		} else {
			return new ResponseEntity<List<CanteenAttendanceMenuGroupCountDto>>(new ArrayList<>(), HttpStatus.OK);
		}
	}

    public ResponseEntity<?> deleteCanteenAttendance(String id) throws UsernameNotFoundException {
        try {
            Optional<CanteenAttendance> optionalCanteenAttendance = canteenAttendanceRepository
                    .findById(Long.parseLong(id));
            if (optionalCanteenAttendance.isPresent()) {
                CanteenAttendance canteenAttendance = optionalCanteenAttendance.get();
                canteenAttendanceRepository.delete(canteenAttendance);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }
//canteen Attendance token

    public ResponseEntity<?> saveCanteenAttendanceToken(CanteenAttendanceTokenDto canteenAttendanceTokenDto,
                                                        User loggedUser) {
        CanteenAttendanceToken canteenAttendanceToken = new CanteenAttendanceToken(canteenAttendanceTokenDto);
        canteenAttendanceToken.setDate(dateUtil.getStringTODateF1(canteenAttendanceTokenDto.getDate()));

        canteenAttendanceToken.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenAttendanceToken.setCreatedBy(loggedUser.getId());
        CanteenAttendanceToken result = canteenAttendanceTokenRepository.save(canteenAttendanceToken);

        return new ResponseEntity<Status>(new Status("200", "CanteenAttendanceToken  successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateCanteenAttendanceToken(CanteenAttendanceTokenDto canteenAttendanceTokenDto,
                                                          User loggedUser) {

        CanteenAttendanceToken canteenAttendanceToken = new CanteenAttendanceToken(canteenAttendanceTokenDto);
        canteenAttendanceToken.setDate(dateUtil.getStringTODateF1(canteenAttendanceTokenDto.getDate()));

        canteenAttendanceToken.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        canteenAttendanceToken.setUpdatedBy(loggedUser.getId());

        CanteenAttendanceToken result = canteenAttendanceTokenRepository.save(canteenAttendanceToken);

        return new ResponseEntity<Status>(new Status("200", "canteenAttendancetoken  updated  successfully!!!"),
                HttpStatus.OK);
    }

    public ResponseEntity<?> listAllcanteenAttendanceToken(String date) throws UsernameNotFoundException {

        List<CanteenAttendanceToken> list = canteenAttendanceTokenRepository
                .findByDate(dateUtil.getStringTODateF1(date));
        if (!list.isEmpty()) {
            List<CanteenAttendanceTokenDto> result = list.stream().map(data -> {
                CanteenAttendanceTokenDto canteenAttendanceTokenDto = new CanteenAttendanceTokenDto(data);
                canteenAttendanceTokenDto.setDate(dateUtil.getStringTODate(data.getDate()));

                return canteenAttendanceTokenDto;
            }).collect(Collectors.toList());
            return new ResponseEntity<List<CanteenAttendanceTokenDto>>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<List<CanteenAttendanceTokenDto>>(new ArrayList<>(), HttpStatus.OK);
        }
    }

    public ResponseEntity<?> getCanteenAttendanceToken(String id) throws UsernameNotFoundException {
        Optional<CanteenAttendanceToken> optionalCanteenAttendanceToken = canteenAttendanceTokenRepository
                .findById(Long.parseLong(id));
        CanteenAttendanceTokenDto canteenAttendanceTokenDto = new CanteenAttendanceTokenDto(
                optionalCanteenAttendanceToken.get());

        canteenAttendanceTokenDto.setDate(dateUtil.getStringTODate(optionalCanteenAttendanceToken.get().getDate()));

        return new ResponseEntity<CanteenAttendanceTokenDto>(canteenAttendanceTokenDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteCanteenAttendanceToken(String id) throws UsernameNotFoundException {
        try {
            Optional<CanteenAttendanceToken> optionalCanteenAttendanceToken = canteenAttendanceTokenRepository
                    .findById(Long.parseLong(id));
            if (optionalCanteenAttendanceToken.isPresent()) {
                CanteenAttendanceToken canteenAttendanceToken = optionalCanteenAttendanceToken.get();
                canteenAttendanceTokenRepository.delete(canteenAttendanceToken);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

    public ResponseEntity<?> checkDate(String fromDate, String toDate) {
        Map<String, String> stringMap = null;
        List<CanteenMenu> canteenMenus = null;
        if (fromDate != null && toDate != null) {
            stringMap = new HashMap<>();
            Date date1 = dateUtil.getStringTODateF1(fromDate);
            System.out.println(date1);
            Date date2 = dateUtil.getStringTODateF1(toDate);
            System.out.println(date2);
            canteenMenus = canteenMenuRepository.canteenMenuFilterList(date1);
            if (!canteenMenus.isEmpty()) {
                canteenMenus = canteenMenuRepository.canteenMenuFilterList(date2);
                if (!canteenMenus.isEmpty()) {
                    stringMap.put("message", "fromDate & toDate already allotted");
                } else {
                    stringMap.put("message", "fromDate  already allotted");
                }
            } else {
                canteenMenus = canteenMenuRepository.canteenMenuFilterList(date2);
                if (!canteenMenus.isEmpty()) {
                    stringMap.put("message", "toDate already allotted");
                } else {
                    stringMap.put("message", "date  not allotted");
                }
            }
        } else {
            stringMap = new HashMap<>();
            Date date1 = dateUtil.getStringTODateF1(fromDate);
            System.out.println(date1);
            canteenMenus = canteenMenuRepository.canteenMenuFilterList(date1);
            if (!canteenMenus.isEmpty()) {
                stringMap.put("message", "date already allotted");
            } else {
                stringMap.put("message", "date not allotted");
            }
        }

        return new ResponseEntity<Map<String, String>>(stringMap, HttpStatus.OK);
    }

	
}

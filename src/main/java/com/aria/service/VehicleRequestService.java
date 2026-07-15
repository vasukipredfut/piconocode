package com.aria.service;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import com.aria.utils.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VehicleRequestService {

    @Autowired
    private TransportTripRepository transportTripRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRequestRepository vehicleRequestRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private TransportRepository transportRepository;

    @Autowired
    private VehicleRequestAllotRepository vehicleRequestAllotRepository;

    @Autowired
    private VehicleRequestAllotDriverRepository vehicleRequestAllotDriverRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private VehicleRequestAllotShuffleRepository vehicleRequestAllotShuffleRepository;

    @Autowired
    private DateUtil dateUtil;


    public ResponseEntity<?> getAllVehicleRequest(String travelDate, String capacity, String typeofVehicle, String tripName, Pageable pageable) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date travel = null;
        if (travelDate != null)
            travel = simpleDateFormat.parse(travelDate);
        Page<VehicleRequestEntity> expenses = vehicleRequestRepository.filterVehicleRequest(travel, capacity, typeofVehicle, tripName, pageable);
        List<VehicleRequestDto> expenseDtos = expenses.getContent().stream().map(val -> entityToDto(val)).collect(Collectors.toList());
        PageDataDto<VehicleRequestDto> pageData = new PageDataDto<>(expenseDtos, expenses);
        return new ResponseEntity<PageDataDto<VehicleRequestDto>>(pageData, HttpStatus.OK);
    }

    public VehicleRequestDto getVehicleRequest(Long id) {
        Optional<VehicleRequestEntity> optional = vehicleRequestRepository.findById(id);
        if (optional.isPresent()) {
            return entityToDto(optional.get());
        }
        return new VehicleRequestDto();
    }

    public void deleteVehicleRequest(Long id) {
        vehicleRequestRepository.deleteById(id);
    }

    public void saveVehicleRequest(VehicleRequestDto vehicleRequestDto, User user) throws ParseException {
        Optional<TransportTrip> optional = transportTripRepository.findById(vehicleRequestDto.getTripId());
        VehicleRequestEntity vehicleRequestEntity = null;
        if (optional.isPresent()) {
            if (vehicleRequestDto.getId() == null)
                vehicleRequestEntity = new VehicleRequestEntity();
            else {
                Optional<VehicleRequestEntity> optionalVehicleRequestEntity = vehicleRequestRepository.findById(vehicleRequestDto.getId());
                vehicleRequestEntity = optionalVehicleRequestEntity.get();
            }
            TransportTrip transportTrip = optional.get();
            vehicleRequestEntity.setTripId(vehicleRequestDto.getTripId());
            vehicleRequestEntity.setStartTime(transportTrip.getStartTime());
            vehicleRequestEntity.setTravelDistance(transportTrip.getDistance());
            vehicleRequestEntity.setTypeofVehicle(vehicleRequestDto.getTypeofVehicle());
            vehicleRequestEntity.setNoofvehicle(vehicleRequestDto.getNofvehicle());
            vehicleRequestEntity.setCapacity(vehicleRequestDto.getCapacity());
            vehicleRequestEntity.setRequestedBy(user.getId());
            vehicleRequestEntity.setReqDepartment(user.getDepartment().getId());
            vehicleRequestEntity.setReqPlant(user.getBranch_id());
            vehicleRequestEntity.setTravelDate(new SimpleDateFormat("dd-MM-yyyy").parse(vehicleRequestDto.getTravelDate()));
            vehicleRequestRepository.save(vehicleRequestEntity);
        }
    }

    private VehicleRequestDto entityToDto(VehicleRequestEntity vehicleRequestEntity) {
        VehicleRequestDto dto = new VehicleRequestDto();
        dto.setId(vehicleRequestEntity.getId());
        dto.setTripId(vehicleRequestEntity.getTripId());
        Optional<TransportTrip> optional = transportTripRepository.findById(vehicleRequestEntity.getTripId());
        if (optional.isPresent()) {
            dto.setTripName(optional.get().getName());
        }
        dto.setTypeofVehicle(vehicleRequestEntity.getTypeofVehicle());
        dto.setTravelDate(new SimpleDateFormat("dd-MM-yyyy").format(vehicleRequestEntity.getTravelDate()));
        dto.setCapacity(vehicleRequestEntity.getCapacity());
        dto.setNofvehicle(vehicleRequestEntity.getNoofvehicle());
        Optional<User> optionalUser = userRepository.findById(vehicleRequestEntity.getRequestedBy());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            dto.setRequestedBy(user.getEmployeename());
            dto.setReqDepartment(user.getDepartment().getName());
        }

        Optional<Branch> optionalBranch = branchRepository.findById(vehicleRequestEntity.getReqPlant());
        if (optionalBranch.isPresent()) {
            dto.setReqPlant(optionalBranch.get().getBranchName());
        }
        VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity = vehicleRequestAllotRepository.findByReqId(vehicleRequestEntity.getId());
        if (vehicleRequestAllotmentEntity != null) {
            dto.setStatus(vehicleRequestAllotmentEntity.getStatus());
        } else
            dto.setStatus("Pending");
        return dto;
    }


    public List<VehicleRequestVehiclesDto> getVehicleRequestVehicles(Long reqId) {
        VehicleRequestDto vehicleRequestDto = getVehicleRequest(reqId);
        List<Transport> transportList = transportRepository.findByCapacityAndVehicleType(vehicleRequestDto.getCapacity(), vehicleRequestDto.getTypeofVehicle());
        List<VehicleRequestVehiclesDto> vehiclesDtos = new ArrayList<>();
        VehicleRequestVehiclesDto vehicleRequestVehiclesDto = null;
        for (Transport transport : transportList) {
            vehicleRequestVehiclesDto = new VehicleRequestVehiclesDto();
            vehicleRequestVehiclesDto.setVInfoId(transport.getId());
            vehicleRequestVehiclesDto.setVehicleId(transport.getVid().getId());
            vehicleRequestVehiclesDto.setVehicle(transport.getVid().getName());
            vehicleRequestVehiclesDto.setDriverId(transport.getDriver_id().getId());
            vehicleRequestVehiclesDto.setDriverName(transport.getDriver_id().getEmployeename());
            vehiclesDtos.add(vehicleRequestVehiclesDto);
        }
        return vehiclesDtos;
    }

    public List<VehicleRequestVehiclesDto> getVehicleRequestVehiclesShuffle(Long reqId) {
        VehicleRequestDto vehicleRequestDto = getVehicleRequest(reqId);
        HashSet<Long> transportId = new HashSet<>();
        List<VehicleRequestAllotmentEntity> vehicleRequestAllotmentEntities = vehicleRequestAllotRepository.findByStatus("Assigned");
        for (VehicleRequestAllotmentEntity entity : vehicleRequestAllotmentEntities) {
            transportId.add(entity.getVehicleInfoId());
        }
        List<Transport> transportList = transportRepository.findByCapacityAndVehicleType(vehicleRequestDto.getCapacity(), vehicleRequestDto.getTypeofVehicle());
        List<VehicleRequestVehiclesDto> vehiclesDtos = new ArrayList<>();
        VehicleRequestVehiclesDto vehicleRequestVehiclesDto = null;
        for (Transport transport : transportList) {
            if (transportId.contains(transport.getId())) {
                vehicleRequestVehiclesDto = new VehicleRequestVehiclesDto();
                vehicleRequestVehiclesDto.setVInfoId(transport.getId());
                vehicleRequestVehiclesDto.setVehicleId(transport.getVid().getId());
                vehicleRequestVehiclesDto.setVehicle(transport.getVid().getName());
                vehicleRequestVehiclesDto.setDriverId(transport.getDriver_id().getId());
                vehicleRequestVehiclesDto.setDriverName(transport.getDriver_id().getEmployeename());
                vehiclesDtos.add(vehicleRequestVehiclesDto);
            }
        }
        return vehiclesDtos;
    }

    public void assignVehicleRequest(VehicleAssignDto vehicleAssignDto, User user) throws ParseException {
        VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity = vehicleRequestAllotRepository.findByReqId(vehicleAssignDto.getVehicleRequestId());
        if (vehicleRequestAllotmentEntity == null)
            vehicleRequestAllotmentEntity = new VehicleRequestAllotmentEntity();

        vehicleRequestAllotmentEntity.setReqId(vehicleAssignDto.getVehicleRequestId());
        Optional<Transport> transport = transportRepository.findById(vehicleAssignDto.getVehicleInfoId());
        Optional<VehicleRequestEntity> request = vehicleRequestRepository.findById(vehicleAssignDto.getVehicleRequestId());
        if (transport.isPresent()) {
            vehicleRequestAllotmentEntity.setVehicleInfoId(vehicleAssignDto.getVehicleInfoId());
            try {
                SimpleDateFormat d = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                String startingDate = new SimpleDateFormat("dd-MM-yyyy").format(request.get().getTravelDate());
                String startingTime = new SimpleDateFormat("hh:mm:ss").format(request.get().getStartTime());
                Date dep = d.parse(startingDate + " " + startingTime);
                vehicleRequestAllotmentEntity.setDepartureOn(dep);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (request.isPresent()) {
            Optional<TransportTrip> optional = transportTripRepository.findById(request.get().getTripId());
            if (optional.isPresent()) {
                try {
                    vehicleRequestAllotmentEntity.setArrivalOn(DateUtils.addMinutes(vehicleRequestAllotmentEntity.getDepartureOn(), Integer.valueOf(optional.get().getJhrs())));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        vehicleRequestAllotmentEntity.setStatus("Assigned");
        vehicleRequestAllotmentEntity.setCreatedBy(user.getId());
        vehicleRequestAllotmentEntity.setCreatedDate(dateUtil.getISTDate());
        vehicleRequestAllotRepository.save(vehicleRequestAllotmentEntity);
    }

    public VehicleRequestAllotmentDto getVehicleRequestAllotmentDetails(Long requestId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity = vehicleRequestAllotRepository.findByReqId(requestId);
        VehicleRequestAllotmentDto allotmentDto = new VehicleRequestAllotmentDto();
        if (vehicleRequestAllotmentEntity != null) {
            List<VehicleRequestAllotmentDriverEntity> list = vehicleRequestAllotDriverRepository.findByAllotmentId(vehicleRequestAllotmentEntity.getId());
            VehicleRequestAllotmentShuffleEntity shuffle = vehicleRequestAllotShuffleRepository.findByAllotmentId(vehicleRequestAllotmentEntity.getId());
            allotmentDto.setVehicleRequestId(requestId);
            allotmentDto.setVehicleRequestAllotId(vehicleRequestAllotmentEntity.getId());
            Optional<Transport> transport = transportRepository.findById(vehicleRequestAllotmentEntity.getVehicleInfoId());
            if (transport.isPresent()) {
                Transport trans = transport.get();
                allotmentDto.setVehicleId(trans.getVid().getId());
                allotmentDto.setVehicle(trans.getVid().getName());
                allotmentDto.setDriverId(trans.getDriver_id().getId());
                allotmentDto.setDriverName(trans.getDriver_id().getEmployeename());
            }
            allotmentDto.setStatus(vehicleRequestAllotmentEntity.getStatus());
            try {
                allotmentDto.setDepartureOn(simpleDateFormat.format(vehicleRequestAllotmentEntity.getDepartureOn()));
                allotmentDto.setArrivalOn(simpleDateFormat.format(vehicleRequestAllotmentEntity.getArrivalOn()));
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (list != null && list.size() > 0) {
                for (VehicleRequestAllotmentDriverEntity driver : list) {
                    VehicleAssignAdditionalResponseDto dto = new VehicleAssignAdditionalResponseDto();
                    dto.setDriverId(driver.getDriverId());
                    dto.setDriverStatus(driver.getDriverStatus());
                    allotmentDto.getAdditionalDrivers().add(dto);
                }
            }

            if (shuffle != null) {
                allotmentDto.setVehicleAssignShuffleResponseDto(new VehicleAssignShuffleResponseDto());
                allotmentDto.getVehicleAssignShuffleResponseDto().setVehicleInfoId(shuffle.getVehicleInfoId());
                allotmentDto.getVehicleAssignShuffleResponseDto().setReason(shuffle.getReason());
                Optional<Transport> transportShuffle = transportRepository.findById(shuffle.getVehicleInfoId());
                if (transport.isPresent()) {
                    Transport trans = transportShuffle.get();
                    allotmentDto.getVehicleAssignShuffleResponseDto().setVehicleId(trans.getVid().getId());
                    allotmentDto.getVehicleAssignShuffleResponseDto().setVehicle(trans.getVid().getName());
                    allotmentDto.getVehicleAssignShuffleResponseDto().setDriverId(trans.getDriver_id().getId());
                    allotmentDto.getVehicleAssignShuffleResponseDto().setDriverName(trans.getDriver_id().getEmployeename());
                }
            }

        }

        return allotmentDto;
    }


    public void assignAdditionalDriver(VehicleAssignAdditionalDto vehicleAssignAdditionalDto, User user) {
        List<VehicleRequestAllotmentDriverEntity> list = new ArrayList<>();
        vehicleRequestAllotDriverRepository.deleteByAllotmentId(vehicleAssignAdditionalDto.getVehicleRequestAllotId());
        VehicleRequestAllotmentDriverEntity vehicleRequestAllotmentDriverEntity = null;
        for (Long driverId : vehicleAssignAdditionalDto.getDriverIds()) {
            vehicleRequestAllotmentDriverEntity = new VehicleRequestAllotmentDriverEntity();
            vehicleRequestAllotmentDriverEntity.setDriverStatus(vehicleAssignAdditionalDto.getDriverStatus());
            vehicleRequestAllotmentDriverEntity.setDriverId(driverId);
            vehicleRequestAllotmentDriverEntity.setAllotmentId(vehicleAssignAdditionalDto.getVehicleRequestAllotId());
            vehicleRequestAllotmentDriverEntity.setCreatedBy(user.getId());
            vehicleRequestAllotmentDriverEntity.setCreatedDate(dateUtil.getISTDate());
            list.add(vehicleRequestAllotmentDriverEntity);
        }
        vehicleRequestAllotDriverRepository.saveAll(list);
    }

    public void vehicleReject(Long vehicleRequestId, User user) {
        VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity = vehicleRequestAllotRepository.findByReqId(vehicleRequestId);
        if (vehicleRequestAllotmentEntity != null) {
            vehicleRequestAllotmentEntity.setStatus("REJECTED");
            vehicleRequestAllotmentEntity.setUpdatedBy(user.getId());
            vehicleRequestAllotmentEntity.setUpdatedDate(dateUtil.getISTDate());
            vehicleRequestAllotRepository.save(vehicleRequestAllotmentEntity);
        }
    }


    public void shuffleVehicleRequest(VehicleAssignShuffleDto vehicleAssignAdditionalDto, User user) {
        vehicleRequestAllotShuffleRepository.deleteByAllotmentId(vehicleAssignAdditionalDto.getVehicleRequestAllotId());
        VehicleRequestAllotmentShuffleEntity shuffle = new VehicleRequestAllotmentShuffleEntity();
        shuffle.setAllotmentId(vehicleAssignAdditionalDto.getVehicleRequestAllotId());
        shuffle.setVehicleInfoId(vehicleAssignAdditionalDto.getVehicleInfoId());
        shuffle.setReason(vehicleAssignAdditionalDto.getReason());
        shuffle.setCreatedBy(user.getId());
        shuffle.setCreatedDate(dateUtil.getISTDate());
        vehicleRequestAllotShuffleRepository.save(shuffle);
    }


}

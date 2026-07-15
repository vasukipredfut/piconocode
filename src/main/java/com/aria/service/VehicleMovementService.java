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
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class VehicleMovementService {

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
    private VehicleRequestRefTypeRepository vehicleRequestRefTypeRepository;

    @Autowired
    private VehicleRequestVanBoyRepository vehicleRequestVanBoyRepository;

    @Autowired
    private DateUtil dateUtil;

    public void begunVehicleMovement(VehicleMovementBegunDto vehicleMovementBegunDto,User user) throws ParseException {
        Optional<VehicleRequestAllotmentEntity> optional=vehicleRequestAllotRepository.findById(vehicleMovementBegunDto.getVehicleRequestAllotId());
        if(optional.isPresent()){
            SimpleDateFormat format=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity=optional.get();
            vehicleRequestAllotmentEntity.setActualDepartureOn(format.parse(vehicleMovementBegunDto.getDepartureTime()));
            vehicleRequestAllotmentEntity.setCapacity(vehicleMovementBegunDto.getLoad());
            vehicleRequestAllotRepository.save(vehicleRequestAllotmentEntity);
            List<VehicleRequestRefTypeEntity> listR=new ArrayList<>();
            for (VehicleRefTyeDto vehicleRefTyeDto : vehicleMovementBegunDto.getRefTypes())
            {
                VehicleRequestRefTypeEntity vehicleRequestRefTypeEntity=new VehicleRequestRefTypeEntity();
                vehicleRequestRefTypeEntity.setRefType(vehicleRefTyeDto.getRefType());
                vehicleRequestRefTypeEntity.setRefTypeValue(vehicleRefTyeDto.getRefValue());
                vehicleRequestRefTypeEntity.setAction("B");
                vehicleRequestRefTypeEntity.setAllotmentId(vehicleMovementBegunDto.getVehicleRequestAllotId());
                vehicleRequestRefTypeEntity.setCreatedBy(user.getId());
                vehicleRequestRefTypeEntity.setCreatedDate(dateUtil.getISTDate());
                listR.add(vehicleRequestRefTypeEntity);
            }
            vehicleRequestRefTypeRepository.saveAll(listR);

            List<VehicleRequestVanBoyEntity> listV=new ArrayList<>();
            for(VehicleVanBoyDto vehicleVanBoyDto: vehicleMovementBegunDto.getVanBoys()){
                VehicleRequestVanBoyEntity vehicleRequestVanBoyEntity=new VehicleRequestVanBoyEntity();
                vehicleRequestVanBoyEntity.setAllotmentId(vehicleMovementBegunDto.getVehicleRequestAllotId());
                vehicleRequestVanBoyEntity.setUserId(vehicleVanBoyDto.getId());
                vehicleRequestVanBoyEntity.setCreatedBy(user.getId());
                vehicleRequestVanBoyEntity.setCreatedDate(dateUtil.getISTDate());
                vehicleRequestAllotmentEntity.setDepartureBy(user.getId());
                listV.add(vehicleRequestVanBoyEntity);
            }
            vehicleRequestVanBoyRepository.saveAll(listV);
        }
    }

    public void endVehicleMovement(VehicleMovementEndDto vehicleMovementEndDto,User user) throws ParseException {
        Optional<VehicleRequestAllotmentEntity> optional=vehicleRequestAllotRepository.findById(vehicleMovementEndDto.getVehicleRequestAllotId());
        if(optional.isPresent()){
            SimpleDateFormat format=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity=optional.get();
            vehicleRequestAllotmentEntity.setActualArrivalOn(format.parse(vehicleMovementEndDto.getArrivalTime()));
            vehicleRequestAllotmentEntity.setCapacity(vehicleMovementEndDto.getLoad());
            vehicleRequestAllotmentEntity.setShift(vehicleMovementEndDto.getShift());
            vehicleRequestAllotmentEntity.setShiftSlab(vehicleMovementEndDto.getShiftSlab());
            vehicleRequestAllotmentEntity.setArrivalPlant(vehicleMovementEndDto.getArrivalPlant());
            vehicleRequestAllotmentEntity.setApproxDate(new SimpleDateFormat("dd-MM-yyyy").parse(vehicleMovementEndDto.getApproxDate()));
            vehicleRequestAllotmentEntity.setArrivalBy(user.getId());
            vehicleRequestAllotRepository.save(vehicleRequestAllotmentEntity);

            List<VehicleRequestRefTypeEntity> listR=new ArrayList<>();
            for (VehicleRefTyeDto vehicleRefTyeDto : vehicleMovementEndDto.getRefTypes())
            {
                VehicleRequestRefTypeEntity vehicleRequestRefTypeEntity=new VehicleRequestRefTypeEntity();
                vehicleRequestRefTypeEntity.setRefType(vehicleRefTyeDto.getRefType());
                vehicleRequestRefTypeEntity.setRefTypeValue(vehicleRefTyeDto.getRefValue());
                vehicleRequestRefTypeEntity.setAction("E");
                vehicleRequestRefTypeEntity.setAllotmentId(vehicleMovementEndDto.getVehicleRequestAllotId());
                vehicleRequestRefTypeEntity.setCreatedBy(user.getId());
                vehicleRequestRefTypeEntity.setCreatedDate(dateUtil.getISTDate());
                listR.add(vehicleRequestRefTypeEntity);
            }
            vehicleRequestRefTypeRepository.saveAll(listR);
        }
    }

    public ResponseEntity getAllVehicleMovement(Pageable pageable){
        Page<VehicleRequestAllotmentEntity> page =vehicleRequestAllotRepository.findByStatusOrderByIdDesc("Assigned",pageable);
        List<VehicleMovementResponseDto> responseDtoList=new ArrayList<>();
        for(VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity:page.getContent()){
            responseDtoList.add(entityToDTO(vehicleRequestAllotmentEntity));
        }
        PageDataDto<VehicleMovementResponseDto> pageData = new PageDataDto<>(responseDtoList, page);
        return new ResponseEntity<PageDataDto<VehicleMovementResponseDto>>(pageData, HttpStatus.OK);
    }

    private VehicleMovementResponseDto entityToDTO(VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity){
        VehicleMovementResponseDto vehicleMovementResponseDto=new VehicleMovementResponseDto();
        vehicleMovementResponseDto.setArrivalPlant(vehicleRequestAllotmentEntity.getArrivalPlant());
        vehicleMovementResponseDto.setVehicleRequestAllotId(vehicleRequestAllotmentEntity.getId());

        Optional<Transport> transport = transportRepository.findById(vehicleRequestAllotmentEntity.getVehicleInfoId());
        if (transport.isPresent()){
            vehicleMovementResponseDto.setVehicleNumber(transport.get().getVid().getName());
            vehicleMovementResponseDto.setMeterReading(transport.get().getUpby());
                    vehicleMovementResponseDto.setScheduleType(transport.get().getCate());
        }
        Optional<VehicleRequestEntity> request = vehicleRequestRepository.findById(vehicleRequestAllotmentEntity.getReqId());
        if (request.isPresent()) {
            Optional<TransportTrip> optional = transportTripRepository.findById(request.get().getTripId());
            if (optional.isPresent()){
                vehicleMovementResponseDto.setTripName(optional.get().getName());
                vehicleMovementResponseDto.setDelay(calculateDelay(vehicleRequestAllotmentEntity.getActualDepartureOn(),vehicleRequestAllotmentEntity.getActualArrivalOn(),optional.get().getJhrs()));
            }
        }
        SimpleDateFormat format=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        vehicleMovementResponseDto.setScheduleDep(vehicleRequestAllotmentEntity.getDepartureOn()==null?"":format.format(vehicleRequestAllotmentEntity.getDepartureOn()));
        vehicleMovementResponseDto.setScheduleArr(vehicleRequestAllotmentEntity.getArrivalOn()==null?"":format.format(vehicleRequestAllotmentEntity.getArrivalOn()));
        vehicleMovementResponseDto.setActualDep(vehicleRequestAllotmentEntity.getActualDepartureOn()==null?"":format.format(vehicleRequestAllotmentEntity.getActualDepartureOn()));
        vehicleMovementResponseDto.setActualArr(vehicleRequestAllotmentEntity.getActualArrivalOn()==null?"":format.format(vehicleRequestAllotmentEntity.getActualArrivalOn()));
        vehicleMovementResponseDto.setTripDuration(calculateDuration(vehicleRequestAllotmentEntity.getActualDepartureOn(),vehicleRequestAllotmentEntity.getActualArrivalOn()));
        vehicleMovementResponseDto.setKmTravel("");
        vehicleMovementResponseDto.setDelayReason("");
        if(vehicleRequestAllotmentEntity.getArrivalBy()!=null){
        Optional<User> optionalUser=userRepository.findById(vehicleRequestAllotmentEntity.getArrivalBy());
        if(optionalUser.isPresent()){
            vehicleMovementResponseDto.setActualArrBy(optionalUser.get().getEmployeename());
        }}
        if(vehicleRequestAllotmentEntity.getDepartureBy()!=null){
        Optional<User> optionalDep=userRepository.findById(vehicleRequestAllotmentEntity.getDepartureBy());
        if(optionalDep.isPresent()){
            vehicleMovementResponseDto.setActualDepBy(optionalDep.get().getEmployeename());
        }}
        return vehicleMovementResponseDto;
    }

    private String calculateDelay(Date start,Date end, String journeyHours){
        try{
        if(start!=null && end!=null) {
        long difference_In_Time
                = end.getTime() - start.getTime();
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(difference_In_Time);
        if(diffInMinutes>Integer.valueOf(journeyHours)){
            return String.valueOf(diffInMinutes-Integer.valueOf(journeyHours));
        }}}
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    private String calculateDuration(Date start,Date end){
        if(start!=null && end!=null) {
            long difference_In_Time
                    = end.getTime() - start.getTime();
            long difference_In_Minutes
                    = TimeUnit
                    .MILLISECONDS
                    .toMinutes(difference_In_Time)
                    % 60;
            long difference_In_Hours
                    = TimeUnit
                    .MILLISECONDS
                    .toHours(difference_In_Time)
                    % 24;

            long difference_In_Days
                    = TimeUnit
                    .MILLISECONDS
                    .toDays(difference_In_Time)
                    % 365;

            return difference_In_Days + " days," + difference_In_Hours + " hours," + difference_In_Minutes + "minutes";
        }
        return "";
    }

    public void vehicleMovementUpKm(VehicleMovementUpKmDto vehicleMovementUpKmDto,User user){
        Optional<VehicleRequestAllotmentEntity> optional=vehicleRequestAllotRepository.findById(vehicleMovementUpKmDto.getVehicleRequestAllotId());
        if(optional.isPresent()){
            VehicleRequestAllotmentEntity vehicleRequestAllotmentEntity=optional.get();
            vehicleRequestAllotmentEntity.setDistance(vehicleMovementUpKmDto.getDistance());
            vehicleRequestAllotRepository.save(vehicleRequestAllotmentEntity);
            Optional<Transport> transport = transportRepository.findById(vehicleRequestAllotmentEntity.getVehicleInfoId());
            if (transport.isPresent()){
                Transport transport1=transport.get();
                transport1.setUpby(vehicleMovementUpKmDto.getCurrentReading());
                transportRepository.save(transport1);
            }
        }
    }

    public List<VehicleVanBoyDto> getVanBoys(Long allotmentId){
        List<VehicleRequestVanBoyEntity> list=vehicleRequestVanBoyRepository.findByAllotmentId(allotmentId);
        List<VehicleVanBoyDto> dtoList=new ArrayList<>();
        for(VehicleRequestVanBoyEntity entity:list){
            Optional<User> optionalDep=userRepository.findById(entity.getUserId());
            if(optionalDep.isPresent()){
                dtoList.add(new VehicleVanBoyDto(entity.getUserId(),optionalDep.get().getEmployeename()));
            }
        }
        return dtoList;
    }

    public List<VehicleRefTyeDto> getRefType(Long allotmentId){
        List<VehicleRequestRefTypeEntity> list=vehicleRequestRefTypeRepository.findByAllotmentId(allotmentId);
        List<VehicleRefTyeDto> dtoList=new ArrayList<>();
        for(VehicleRequestRefTypeEntity entity:list){
            dtoList.add(new VehicleRefTyeDto(entity.getRefType(),entity.getRefTypeValue()));
        }
        return dtoList;
    }
}

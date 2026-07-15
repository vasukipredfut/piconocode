package com.aria.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import com.aria.dao.ContractVehicleRepository;
import com.aria.dao.MachineRepository;
import com.aria.dao.TransportRepository;
import com.aria.dao.VehicleGroupRepository;
import com.aria.dao.VehicleGroupingHeadRepository;
import com.aria.dto.ContractVehicleResponseDto;
import com.aria.dto.PageDataDto;
import com.aria.dto.TransportDto;
import com.aria.dto.TransportResponseDto;
import com.aria.dto.TransportVehicleTypeDto;
import com.aria.model.ContractVehicle;
import com.aria.model.Machine;
import com.aria.model.Status;
import com.aria.model.Transport;
import com.aria.model.VehicleGroup;
import com.aria.model.VehicleGroupingHead;

@Service
public class TransportService {
	@Autowired
	private TransportRepository transportRepository;

	@Autowired
	private MachineRepository machineRepository;

	@Autowired
	private VehicleGroupingHeadRepository vehicleGroupingHeadRepository;

	@Autowired
	private VehicleGroupRepository vehicleGroupRepository;

	@Autowired
	ContractVehicleRepository contractVehicleRepository;
	
	public ResponseEntity<?> creteTransport(TransportDto transportDto) {
		Machine m = new Machine();
		m.setId(transportDto.getSelectedVehicle());
		Transport transport = transportRepository.findByVid(m);

		if (transport == null) {
			Optional<Machine> machine = machineRepository.findById(transportDto.getSelectedVehicle());
			if (machine.isPresent()) {
				transport = new Transport(transportDto);
				if(machine.get().getCStatus() != null) {
					if (machine.get().getCStatus().equalsIgnoreCase("sold"))
						transport.setStatus("S");
					else
						transport.setStatus("");
				} else{
					transport.setStatus("");
				}
				if (transportRepository.save(transport) != null)
					return new ResponseEntity<Object>(new Status("200", "Saved Successfully..."), HttpStatus.OK);
				else
					return new ResponseEntity<Object>(new Status("200", "Something Went wrong"), HttpStatus.OK);
			} else
				return new ResponseEntity<Object>(new Status("200", "Machine Not Found"), HttpStatus.OK);
		}
		return new ResponseEntity<Object>(new Status("200", "It already Exist..."), HttpStatus.OK);
	}

	public ResponseEntity<?> getSingleVehicleDetail(Long transportId) {
		Optional<Transport> transport = transportRepository.findById(transportId);
		if (transport.isPresent()) {
			TransportResponseDto result=transport.get().getStatus().equalsIgnoreCase("s")?new TransportResponseDto(transport.get(),"Sold"):new TransportResponseDto(transport.get(),"Free");
			return new ResponseEntity<Object>(result, HttpStatus.OK);
		}
		return new ResponseEntity<Object>(new Status("200", "Not found !"), HttpStatus.OK);
	}

	public ResponseEntity<?> getAllvehiclesDetails(Pageable page) {
		Page<Transport> transports = transportRepository.findAll(page);
		if (!transports.isEmpty())
			return new ResponseEntity<List<Transport>>(transports.toList(), HttpStatus.OK);
		return new ResponseEntity<Object>(transports.toList(), HttpStatus.OK);
	}

	public ResponseEntity<?> getTransportVehicleDetails(String vehicleName,String vehicleType,String capacity,Pageable pageable) {
		Page<Transport> transports = transportRepository.filterTransport(vehicleName,vehicleType,capacity,pageable);
		List<TransportResponseDto> response = transports.toList().stream().map(tran -> {
			return tran.getStatus().equalsIgnoreCase("s") ? new TransportResponseDto(tran, "Sold")
					: new TransportResponseDto(tran, "Free");
		}).collect(Collectors.toList());
		PageDataDto<TransportResponseDto> pageData = new PageDataDto<>(response, transports);
		return new ResponseEntity<PageDataDto<TransportResponseDto>>(pageData, HttpStatus.OK);
	}

	public ResponseEntity<?> getVehicleTypeBasedOnVehicleOwner(Long transportMode) {
		if (transportMode == 1) {
			Map<Long, String> map = new HashMap<Long, String>();
			List<VehicleGroup> vehicles = vehicleGroupRepository.findAll();
			List<Long> ids = vehicles.stream().map(vehicle -> {
				map.put(vehicle.getVehicle_id(), vehicle.getVehicle_no());
				return vehicle.getVehicle_id();
			}).collect(Collectors.toList());
			List<Machine> machines = machineRepository.findAllById(ids);
			List<TransportVehicleTypeDto> response = machines.stream()
					.filter(machine -> machine.getCStatus().equalsIgnoreCase("INUSE")
							|| machine.getCStatus().equalsIgnoreCase("") || machine.getCStatus().equalsIgnoreCase(null))
					.map(vehicle -> {
						return new TransportVehicleTypeDto(vehicle.getName(), vehicle.getId(), map.get(vehicle.getId()));
					}).collect(Collectors.toList());
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		}
		List<TransportVehicleTypeDto> response = vehicleGroupingHeadRepository.findByType(transportMode).stream()
				.map(vehicle -> {
					return new TransportVehicleTypeDto(vehicle.getName(), vehicle.getId(), null);
				}).collect(Collectors.toList());
		return new ResponseEntity<Object>(response, HttpStatus.OK);
	}

	public ResponseEntity<?> getVehicleNumberBasedOnVehicleType(Long vehicleType) {
		VehicleGroupingHead vehicleGroup = new VehicleGroupingHead();
		vehicleGroup.setId(vehicleType);
		Map<Long, String> vehiclesNumber = new HashMap<Long, String>();
		List<Long> ids = vehicleGroupRepository.findByGroup_head(vehicleGroup).stream().map(vehicle -> {
			vehiclesNumber.put(vehicle.getVehicle_id(), vehicle.getVehicle_no());
			return vehicle.getVehicle_id();
		}).collect(Collectors.toList());

		List<TransportVehicleTypeDto> response = machineRepository.findAllById(ids).stream()
				.filter(machine -> machine.getCStatus().equalsIgnoreCase("INUSE")
						|| machine.getCStatus().equalsIgnoreCase("") || machine.getCStatus().equalsIgnoreCase(null))
				.map(vehicle -> {
					return new TransportVehicleTypeDto(null, vehicle.getId(), vehiclesNumber.get(vehicle.getId()));
				}).collect(Collectors.toList());
		if (response.size() > 0)
			return new ResponseEntity<Object>(response, HttpStatus.OK);
		return new ResponseEntity<Object>(new Status("200", "For this vehicle Type Numbers Not present..."),
				HttpStatus.OK);
	}
	
	public ResponseEntity<?> updateTransport(TransportDto transportDto,Long transportId) {
		Optional<Transport> transport=transportRepository.findById(transportId);
		Machine m=new Machine();
		m.setId(transportDto.getSelectedVehicle());
		
		Optional<Machine> machine=null;
		boolean chackeMachine=false;
		if(transport.isPresent()) {
			
			String status="";

			//If previous machine will change with new machine
			if(transport.get().getVid().getId()!=transportDto.getSelectedVehicle()) {				
				machine = machineRepository.findById(transportDto.getSelectedVehicle());
		
				if(machine.isPresent()) {
					//if vehicle already present then it will return
					Transport checkAlreadyExist=transportRepository.findByVid(machine.get());
					if(checkAlreadyExist!=null)
						return new ResponseEntity<Object>(new Status("200", "Selected Vehicle Already Present..."),HttpStatus.OK);
					
					chackeMachine=true;
					if(machine.get().getCStatus() != null) {
						if (machine.get().getCStatus().equalsIgnoreCase("sold"))
							status = "S";
					}
				}else
					return new ResponseEntity<Object>(new Status("200", "Selected Vehicle is not Present..."),HttpStatus.OK);
			}
			
			Transport transportUpdate=new Transport(transportDto);
			if(chackeMachine)
				transportUpdate.setStatus(status);
			
			Machine newMachine=new Machine();
			newMachine.setId(transportDto.getSelectedVehicle());
			
			transportUpdate.setVid(newMachine);
			transportUpdate.setId(transportId);
			
			if(transportRepository.save(transportUpdate)!=null)
				return new ResponseEntity<Object>(new Status("200", "Transport Updated..."),HttpStatus.OK);
			else
				return new ResponseEntity<Object>(new Status("500", "SomeThing went wrong..."),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Object>(new Status("200", "Transport Id Not Found..."), HttpStatus.OK);
	}
	
	public ResponseEntity<?> deleteTransport(Long transportId){
		Optional<Transport> t=transportRepository.findById(transportId);
		if(t.isPresent())
			transportRepository.deleteById(transportId);
		else
			return new ResponseEntity<Object>(new Status("200","Transport Id Not Matched.."),HttpStatus.OK);
		
		return new ResponseEntity<Object>(new Status("200","Transport Deleted..."),HttpStatus.OK);
	}


	public ResponseEntity<?> getVehicleTypeBasedOnVehicleName( Long transportMode, String vehicleType,  String vehicleName) {
		if (transportMode == 1) {
			List<Transport> transports = transportRepository.filterTransport(vehicleName,vehicleType);
			List<TransportResponseDto> response = transports.stream().map(tran -> {
	            return tran.getStatus().equalsIgnoreCase("s") ? new TransportResponseDto(tran, "Sold")
	                    : new TransportResponseDto(tran, "Free");
	        }).collect(Collectors.toList());
	        return new ResponseEntity<>(response, HttpStatus.OK);
		} else if (transportMode == 2) {
	        List<ContractVehicle> contractVehicles = contractVehicleRepository.filterContractVehicle();
	        List<ContractVehicleResponseDto> response = contractVehicles.stream().map(contractVehicle -> {
	            return new ContractVehicleResponseDto(contractVehicle);
	        }).collect(Collectors.toList());
	        return new ResponseEntity<>(response, HttpStatus.OK);
		}
		 return new ResponseEntity<>("Invalid transportMode", HttpStatus.BAD_REQUEST);
	}

}

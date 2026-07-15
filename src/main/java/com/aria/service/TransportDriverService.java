package com.aria.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import com.aria.dao.TransportDriverRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.aria.dao.UserRepository;
import com.aria.dto.TransportDriverBasedOnAssignedVehiclesResponseDto;
import com.aria.dto.TransportDriverDto;
import com.aria.dto.TransportDriverResponseDto;
import com.aria.model.Machine;
import com.aria.model.Status;
import com.aria.model.TransportDriver;
import com.aria.model.User;

@Service
public class TransportDriverService {
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TransportDriverRepo transportDriverRepo;

	public ResponseEntity<?> getAllDrivers() {
		List<User> users = userRepository.findByDacatid(3L);
		List<TransportDriverResponseDto> ans = users.stream().map(data -> new TransportDriverResponseDto(data))
				.collect(Collectors.toList());
		return new ResponseEntity<Object>(ans, HttpStatus.OK);
	}

	public ResponseEntity<?> getAllDriversBasedOnName(String driverName) {
		if (driverName != null && driverName != "") {
			List<User> ans = userRepository.findbByName(driverName);
			return new ResponseEntity<Object>(ans, HttpStatus.OK);
		}

		Pageable page = PageRequest.of(0, 10);
		List<User> user = userRepository.findbByName(page);
		return new ResponseEntity<Object>(user, HttpStatus.OK);
	}

	@Transactional(rollbackOn = Exception.class)
	public ResponseEntity<?> saveDriver(TransportDriverDto transportDriverDto) throws Exception {
		try {
			transportDriverDto.getDriverId().forEach(driverId -> {
					
				TransportDriver transportDriver = new TransportDriver(transportDriverDto.getVehicleId(), driverId);
				transportDriverRepo.save(transportDriver);
			});
			return new ResponseEntity<Object>(new Status("200", "Driver Assign successfully..."), HttpStatus.OK);
		}catch(Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public ResponseEntity<?> getAssignDriversToVehicle(Long vehicleId){
		Machine m=new Machine();
		m.setId(vehicleId);
		List<TransportDriver> transportDriver=transportDriverRepo.findByVehicleId(m);
		if(!transportDriver.isEmpty()) {
			TransportDriverBasedOnAssignedVehiclesResponseDto transport=new TransportDriverBasedOnAssignedVehiclesResponseDto();
			transport.setVehicleDetails(transportDriver.get(0).getVehicleId());
			List<User> drivers=transportDriver.stream().map(data -> {
					Optional<User> user=userRepository.findById(data.getDriverId());
					if(user.isPresent())
						return user.get();
					return null;
				}).collect(Collectors.toList());
			transport.setDriverDetails(drivers);
			return new ResponseEntity<Object>(transport,HttpStatus.OK);
		}
		return new ResponseEntity<Object>(transportDriver,HttpStatus.OK);
	}
}

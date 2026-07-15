package com.aria.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aria.dao.BinStockRepository;
import com.aria.dao.DpatchDeliveryRepository;
import com.aria.dao.DpatchItemsBatchRepository;
import com.aria.dao.VehicleGroupRepository;
import com.aria.dao.VehicleGroupingHeadRepository;
import com.aria.dto.DpatchDeliveryRequestDto;
import com.aria.dto.VehicleNameResponseDto;
import com.aria.model.BinStock;
import com.aria.model.DpatchDelivery;
import com.aria.model.DpatchDespatch;
import com.aria.model.DpatchItemsBatch;
import com.aria.model.Status;
import com.aria.model.User;
import com.aria.model.VehicleGroup;
import com.aria.model.VehicleGroupingHead;
import com.aria.model.Vendor;

@Service
public class DpatchDeliveryService {
	@Autowired
	private DpatchDeliveryRepository dpatchDeliveryRepository;

	@Autowired
	private VehicleGroupRepository vehicleGroupRepository;

	@Autowired
	private DpatchItemsBatchRepository dpatchItemsBatchRepository;

	@Autowired
	private BinStockRepository binStockRepository;

	@Autowired
	private VehicleGroupingHeadRepository vehicleGroupingHeadRepository;
	
	@Transactional(rollbackFor = Exception.class)
	public ResponseEntity<?> createDelivery(DpatchDeliveryRequestDto dpatchDeliveryRequestDto, User user)
			throws Exception {
		try {
			VehicleGroup vehicleGroup = new VehicleGroup();
			DpatchDelivery dpatchDelivery = new DpatchDelivery();
			VehicleGroup v = vehicleGroupRepository.findByVehicleNo(dpatchDeliveryRequestDto.getVehicleNumber());
			vehicleGroup.setId(v.getId());
			dpatchDelivery.setVehicle_id(vehicleGroup); 
			
			dpatchDelivery.setVehicle_Type_Id(dpatchDeliveryRequestDto.getVehicleType());
			dpatchDelivery.setVehicle_no(vehicleGroup.getVehicle_no());
			
			User u = new User();
			u.setId(user.getId());
			dpatchDelivery.setCreated_By(u);
			dpatchDelivery.setDelivery_date(java.sql.Date.valueOf(dpatchDeliveryRequestDto.getDeliveryDate()));

			DpatchDespatch dpatchDespatch = new DpatchDespatch();
			dpatchDespatch.setId(dpatchDeliveryRequestDto.getDispatchId());
			dpatchDelivery.setDespatch_id(dpatchDespatch);

			Vendor vendor = new Vendor();
			vendor.setId(dpatchDeliveryRequestDto.getPartyName());
			dpatchDelivery.setParty_id(vendor);

			dpatchDelivery.setActive(0L);
			dpatchDelivery.setCreated_Timestamp(new Timestamp(System.currentTimeMillis()));

			List<DpatchItemsBatch> dpatchItemsBatch = dpatchItemsBatchRepository
					.findByDpatch_desptach_ref_id(dpatchDeliveryRequestDto.getDispatchId());

			for (DpatchItemsBatch batch : dpatchItemsBatch) {
				BinStock bin = batch.getBinstock_Ref_Id();
				bin.setLockStatus("n");
				bin = binStockRepository.save(bin);
			}

			if (dpatchDeliveryRepository.save(dpatchDelivery) != null)
				return new ResponseEntity<Object>(new Status("200", "Delivery Added..."), HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Exception Occur!");
		}
		return new ResponseEntity<Object>(new Status("200", "Delivery Not Added..."), HttpStatus.OK);
	}
	
	public ResponseEntity<?> getAllVehicalNumber(Long vehicleType){
		List<VehicleGroupingHead> vehicleGroupingHead=vehicleGroupingHeadRepository.findByType(vehicleType,0);
		List<VehicleGroup> vehicleGroups=new ArrayList<VehicleGroup>();
		
		for(int i=0;i<vehicleGroupingHead.size();i++) {
			VehicleGroupingHead vehi=new VehicleGroupingHead();
			vehi.setId(vehicleGroupingHead.get(i).getId());
			List<VehicleGroup> vehicles=vehicleGroupRepository.findByGroup_head(vehi);
			vehicleGroups.addAll(vehicles);
		}
		
		List<VehicleNameResponseDto> resp=new ArrayList<VehicleNameResponseDto>();
		if(vehicleGroups.size()>0) {
			resp=vehicleGroups.stream().map(v -> {VehicleNameResponseDto vehicle=new VehicleNameResponseDto();
			vehicle.setId(v.getId());
			vehicle.setVehicleNumber(v.getVehicle_no());
			return vehicle;
			}).collect(Collectors.toList());
			return new ResponseEntity<Object>(resp,HttpStatus.OK);
		}
		return new ResponseEntity<Object>(vehicleGroups,HttpStatus.OK);
	}
}

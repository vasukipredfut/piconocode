package com.aria.utils;

import com.aria.dao.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Component
public class WorkFlowUtil {

	@Autowired
	WorkFlowDetailsRepository workFlowDetailsRepository;

	@Autowired
	WorkFlowStepsRepository workFlowStepsRepository;

	@Autowired
	WorkFlowRepository workFlowRepository;

	/*public void saveWorkFlowDetails(InwardInspectionEntry inwardInspectionEntry, User loggedUser) {

		*//*
		 * WorkFlowDetails workFlowDetails = new WorkFlowDetails();
		 * workFlowDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		 * workFlowDetails.setCreatedBy(loggedUser.getId());
		 * workFlowDetails.setType("InwardInspectionEntry");
		 * workFlowDetails.setTypeId(inwardInspectionEntry.getId()); Optional<WorkFlow>
		 * workFlow = workFlowRepository.findByWorkFlowName("InwardInspectionEntry"); if
		 * (workFlow.isPresent()) { workFlowDetails.setWorkFlow(workFlow.get()); }
		 * WorkFlowDetails flowDetails =
		 * workFlowDetailsRepository.save(workFlowDetails); List<WorkFlowSteps>
		 * workFlowSteps =
		 * workFlowStepsRepository.findAllByFlowId(flowDetails.getWorkFlow().getId());
		 * for (WorkFlowSteps flowSteps : workFlowSteps) { WorkFlowStatusSteps
		 * flowStatusSteps = new WorkFlowStatusSteps();
		 * flowStatusSteps.setFlowId(flowDetails.getId());
		 * flowStatusSteps.setStepName(flowSteps.getStepName());
		 * flowStatusSteps.setStepNumber(flowSteps.getStepNumber());
		 * flowStatusSteps.setStatus("Pending");
		 * workFlowStatusStepsRepository.save(flowStatusSteps); }
		 *//*
	}*/

	/*public void updateWorkFlowDetails(InwardInspectionEntry inwardInspectionEntry, User loggedUser) {

		*//*
		 * Optional<WorkFlowDetails> workFlowDetails =
		 * workFlowDetailsRepository.findById();
		 * workFlowDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
		 * workFlowDetails.setCreatedBy(loggedUser.getId());
		 * workFlowDetails.setType("InwardInspectionEntry");
		 * workFlowDetails.setTypeId(inwardInspectionEntry.getId()); Optional<WorkFlow>
		 * workFlow = workFlowRepository.findByWorkFlowName("InwardInspectionEntry"); if
		 * (workFlow.isPresent()) { workFlowDetails.setWorkFlow(workFlow.get()); }
		 * WorkFlowDetails flowDetails =
		 * workFlowDetailsRepository.save(workFlowDetails); List<WorkFlowSteps>
		 * workFlowSteps =
		 * workFlowStepsRepository.findAllByFlowId(flowDetails.getWorkFlow().getId());
		 * for (WorkFlowSteps flowSteps : workFlowSteps) { WorkFlowStatusSteps
		 * flowStatusSteps = new WorkFlowStatusSteps();
		 * flowStatusSteps.setFlowId(flowDetails.getId());
		 * flowStatusSteps.setStepName(flowSteps.getStepName());
		 * flowStatusSteps.setStepNumber(flowSteps.getStepNumber());
		 * flowStatusSteps.setStatus("Pending");
		 * workFlowStatusStepsRepository.save(flowStatusSteps); }
		 *//*
	}*/
}

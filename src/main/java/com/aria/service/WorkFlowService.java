package com.aria.service;

import com.aria.dao.WorkFlowDetailsRepository;
import com.aria.dao.WorkFlowRepository;
import com.aria.dao.WorkFlowStepsRepository;
import com.aria.dto.*;
import com.aria.model.*;
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

@Service
public class WorkFlowService {


    @Autowired
    WorkFlowRepository workFlowRepository;

    @Autowired
    WorkFlowStepsRepository workFlowStepsRepository;

    @Autowired
    WorkFlowDetailsRepository workFlowDetailsRepository;

    public ResponseEntity<?> saveWorkFlow(WorkFlowDto workFlowDto, User loggedUser) {
        WorkFlow workFlow = new WorkFlow(workFlowDto);
        workFlow.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        workFlow.setCreatedBy(loggedUser.getId());
        WorkFlow result = workFlowRepository.save(workFlow);
        updateWorkFlow(result, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "WorkFlow created successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> upateFlow(FlowDto flowDto, User loggedUser) {
        WorkFlowDetails workFlow = new WorkFlowDetails();
        if (flowDto.getWorkFlowId() != null) {
            WorkFlow workFlow1 = new WorkFlow();
            workFlow1.setId(flowDto.getWorkFlowId());
            workFlow.setWorkFlow(workFlow1);
        }
        workFlow.setFlowName(flowDto.getFlowName());
        if (flowDto.getFlowParentName() != null) {
            workFlow.setParentFlowId(workFlowDetailsRepository.findByFlowName(flowDto.getFlowParentName()).get().getId());
        }
        workFlow.setMain(1);
        workFlow.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        workFlow.setCreatedBy(loggedUser.getId());
        WorkFlowDetails result = workFlowDetailsRepository.save(workFlow);
        updateFlowDetails(result, flowDto, loggedUser);
        return new ResponseEntity<Status>(new Status("200", "WorkFlow created successfully!!!"), HttpStatus.OK);
    }

    public ResponseEntity<?> updateWorkFlow(WorkFlowDto workFlowDto, User loggedUser) {
        WorkFlow workFlow = new WorkFlow(workFlowDto);
        workFlow.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        workFlow.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        workFlow.setUpdatedBy(loggedUser.getId());
        WorkFlow result = workFlowRepository.save(workFlow);
        return new ResponseEntity<Status>(new Status("200", "WorkFlow updated successfully!!!"), HttpStatus.OK);
    }


    public void updateWorkFlow(WorkFlow workFlow, User loggedUser) {
        WorkFlowDetails flowDetails = new WorkFlowDetails();
        flowDetails.setWorkFlow(workFlow);
        flowDetails.setMain(0);
        flowDetails.setFlowName(workFlow.getWorkFlowName());
        flowDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
        flowDetails.setCreatedBy(loggedUser.getId());
        workFlowDetailsRepository.save(flowDetails);
    }

    public void updateFlowDetails(WorkFlowDetails workFlow, FlowDto flowDto, User loggedUser) {
        Optional<WorkFlowDetails> optionalWorkFlowDetails = workFlowDetailsRepository.findByFlowName(flowDto.getFlowParentName());
        if (optionalWorkFlowDetails.isPresent()) {
            WorkFlowDetails flowDetails = optionalWorkFlowDetails.get();
            flowDetails.setMain(1);
            flowDetails.setChildFlowId(workFlowDetailsRepository.findByFlowName(flowDto.getFlowName()).get().getId());
            flowDetails.setCreated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            flowDetails.setUpdatedBy(loggedUser.getId());
            workFlowDetailsRepository.save(flowDetails);
        }
    }

    public ResponseEntity<?> listAllWorkFlow(Pageable pageable) throws UsernameNotFoundException {
        Page<WorkFlow> workFlows = workFlowRepository.findAll(pageable);
        return new ResponseEntity<List<WorkFlowDto>>(workFlows.toList().stream().map(workFlow -> new WorkFlowDto(workFlow)).collect(Collectors.toList()), HttpStatus.OK);
    }

    public ResponseEntity<?> listAllWorkFlowDetailsMap() throws UsernameNotFoundException {
        List<WorkFlowDetails> workFlows = workFlowDetailsRepository.findByWorkFlowList();
        List<WorkFlowDetailsMap> resultWorkFlowDetailsMaps = new ArrayList<>();
        for (WorkFlowDetails workFlow : workFlows) {
            WorkFlowDetailsMap result = new WorkFlowDetailsMap(workFlow);
            List<WorkFlowDetailsMap> childResultWorkFlowList = new ArrayList<>();
            List<WorkFlowDetails> updateWorkFlowList = workFlowDetailsRepository.findByWorkFlowList(workFlow.getWorkFlow().getId());

            for (WorkFlowDetails finalWorkflow : updateWorkFlowList) {

                WorkFlowDetailsMap flowDetailsMap = new WorkFlowDetailsMap(finalWorkflow);

                if (finalWorkflow.getParentFlowId() != null) {
                    flowDetailsMap.setSuccessorsteponFailure(getWorkFlowName(finalWorkflow.getParentFlowId()));
                } else {
                    flowDetailsMap.setSuccessorsteponFailure(flowDetailsMap.getTaskName());
                    flowDetailsMap.setPrecedessorstep("Start");
                }

                if (finalWorkflow.getChildFlowId() != null) {
                    flowDetailsMap.setSuccessorsteponSuccess(getWorkFlowName(finalWorkflow.getChildFlowId()));
                } else {
                    flowDetailsMap.setPrecedessorstep("Stop");
                }

                childResultWorkFlowList.add(flowDetailsMap);
            }
            result.setWorkFlowDetails(childResultWorkFlowList);
            resultWorkFlowDetailsMaps.add(result);
        }
        return new ResponseEntity<List<WorkFlowDetailsMap>>(resultWorkFlowDetailsMaps, HttpStatus.OK);
    }

    public void updateWorkFlowDetails(List<WorkFlow> flowDetailsMapList, Long taskId) {
       /* if (id != null) {
            Optional<WorkFlow> workFlowOptional = workFlowRepository.findByWorkParentFlowId(id);
            if (workFlowOptional.isPresent()) {
                flowDetailsMapList.add(workFlowOptional.get());
                updateWorkFlowDetails(flowDetailsMapList, workFlowOptional.get().getId());
            }
        }*/
        flowDetailsMapList = workFlowRepository.findByWorkFlowList(taskId);
    }

    public String getWorkFlowName(Long id) {
        Optional<WorkFlowDetails> optionalWorkFlow = workFlowDetailsRepository.findById(id);
        if (optionalWorkFlow.isPresent()) {
            return optionalWorkFlow.get().getFlowName();
        } else {
            return null;
        }
    }


    public ResponseEntity<?> getWorkFlowDetails(String id) throws UsernameNotFoundException {
        Optional<WorkFlow> optionalWorkFlow = workFlowRepository.findById(Long.parseLong(id));
        WorkFlowDto workFlowDto = new WorkFlowDto(optionalWorkFlow.get());
        return new ResponseEntity<WorkFlowDto>(workFlowDto, HttpStatus.OK);
    }

    public ResponseEntity<?> deleteWorkFlowDetails(String id) throws UsernameNotFoundException {
        try {
            Optional<WorkFlow> optionalWorkFlow = workFlowRepository.findById(Long.parseLong(id));
            if (optionalWorkFlow.isPresent()) {
                WorkFlow workFlow = optionalWorkFlow.get();
                workFlowRepository.delete(workFlow);
            }
            return new ResponseEntity<Status>(new Status("200", "Deleted SuccessFully"), HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<Status>(new Status("304", "Could not Delete Record"), HttpStatus.NOT_MODIFIED);
        }
    }

}

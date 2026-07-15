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
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MaterialReturnService {

    @Autowired
    OtherBranchIssueRepository otherBranchIssueRepository;

    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    DateUtil dateUtil;

    @Autowired
    MaterialReturnItemsRepository materialReturnItemsRepository;

    @Autowired
    MaterialReturnRepository materialReturnRepository;

    @Autowired
    BranchRepository branchRepository;

    @Autowired
    OrganisationRepository organisationRepository;


    @Autowired
    GoDownRepository goDownRepository;


    public ResponseEntity<?> filterListAll(Long firmId , String requestDeptName, String requestBranchName, String itemName, String fromDate, String toDate, Long issuebranch , Long issueDept , Pageable pageable) throws UsernameNotFoundException {

        Date startDate = null;
        Date endDate = null;

        if (fromDate != null && toDate != null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
            endDate = dateUtil.getStringTODateWk(toDate);
        } else if (fromDate != null && toDate == null) {
            startDate = dateUtil.getStringTODateWk(fromDate);
        } else if (fromDate == null && toDate != null) {
            endDate = dateUtil.getStringTODateWk(toDate);
        }

        Page<MaterialReturnList> list = materialReturnRepository.materialReturnList(firmId , requestDeptName, requestBranchName, itemName, startDate, endDate, issuebranch , issueDept , pageable);
        if (!list.toList().isEmpty()) {
            List<MaterialReturnListDTO> result = list.toList().stream().map(data -> {
                MaterialReturnListDTO materialReturn = new MaterialReturnListDTO(data);

                if (materialReturn.getRequestFrom() != null && materialReturn.getRequestFrom().equalsIgnoreCase("Branch")) {

                    Optional<Branch> optionalBranch = branchRepository.findById(materialReturn.getRequestFromId());
                    if (optionalBranch.isPresent())
                        materialReturn.setRequestFromName(optionalBranch.get().getBranchName());

                } else if (materialReturn.getRequestFrom() != null && materialReturn.getRequestFrom().equalsIgnoreCase("Godown")) {

                    Optional<GoDown> optionalGoDown = goDownRepository.findById(materialReturn.getRequestFromId());
                    if (optionalGoDown.isPresent())
                        materialReturn.setRequestFromName(optionalGoDown.get().getName());
                }
                return materialReturn;
            }).collect(Collectors.toList());
            PageDataDto<MaterialReturnListDTO> pageData = new PageDataDto(result, list);
            return new ResponseEntity<PageDataDto<MaterialReturnListDTO>>(pageData, HttpStatus.OK);
        } else {
            return new ResponseEntity<PageDataDto<MaterialReturnListDTO>>(new PageDataDto(), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> updateMaterialReturn(Long id,  Long returnId , User loggedUser) throws UsernameNotFoundException {

        Optional<MaterialReturn> optionalMaterialReturn = materialReturnRepository.findById(id);

        if (optionalMaterialReturn.isPresent()) {

            MaterialReturn materialReturn = optionalMaterialReturn.get();

            materialReturn.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
            materialReturn.setUpdatedBy(loggedUser.getId());

            MaterialReturn result = materialReturnRepository.save(materialReturn);

            Optional<MaterialReturnItems> materialReturnItems = materialReturnItemsRepository.findById(returnId);

            if(materialReturnItems.isPresent()){
                MaterialReturnItems materialReturnItems1 = materialReturnItems.get();
                materialReturnItems1.setReturnDate(dateUtil.getISTDate());
                materialReturnItems1.setUpdated_date(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
                materialReturnItems1.setUpdatedBy(loggedUser.getId());
                materialReturnItems1.setStatus("Received");
                materialReturnItemsRepository.save(materialReturnItems1);
            }

            return new ResponseEntity<Status>(new Status("200", "MaterialReturn Updated SuccessFully!!!"), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("400", "MaterialReturn Not Updated!!!"), HttpStatus.OK);
        }
    }


    public ResponseEntity<?> getMaterialReturn(Long id, Long returnId) throws UsernameNotFoundException {
        Optional<MaterialReturn> optionalMaterialReturn = materialReturnRepository.findById(id);
        if (optionalMaterialReturn.isPresent()) {
            MaterialReturn materialReturn = optionalMaterialReturn.get();

            MaterialReturnDto returnDto = new MaterialReturnDto(optionalMaterialReturn.get());

            if (materialReturn.getRequestDate() != null) {
                returnDto.setRequestDate(dateUtil.getDateTOStringWk(materialReturn.getRequestDate()));
                returnDto.setRequestDateString(dateUtil.getDateTOStringWk(materialReturn.getRequestDate()));
            }
            if (materialReturn.getIssueDate() != null) {
                returnDto.setIssueDate(dateUtil.getDateTOStringWk(materialReturn.getIssueDate()));
                returnDto.setIssueDateString(dateUtil.getDateTOStringWk(materialReturn.getIssueDate()));
            }

            if (materialReturn.getDispatchDate() != null) {
                returnDto.setDispatchOn(dateUtil.getDateTOStringWk(materialReturn.getDispatchDate()));
            }

            returnDto.setRequestFromId(materialReturn.getFromId());

            if (materialReturn.getFromName() != null && materialReturn.getFromName().equalsIgnoreCase("Branch")) {

                Optional<Branch> optionalBranch = branchRepository.findById(materialReturn.getFromId());
                if (optionalBranch.isPresent())
                    returnDto.setRequestFromName(optionalBranch.get().getBranchName());

            } else if (materialReturn.getFromName() != null && materialReturn.getFromName().equalsIgnoreCase("Godown")) {

                Optional<GoDown> optionalGoDown = goDownRepository.findById(materialReturn.getFromId());
                if (optionalGoDown.isPresent())
                    returnDto.setRequestFromName(optionalGoDown.get().getName());
            }

            returnDto.setIssueFrom(materialReturn.getToName());
            returnDto.setIssueFromId(materialReturn.getToId());

            if (materialReturn.getToName() != null && materialReturn.getToName().equalsIgnoreCase("Branch")) {

                Optional<Branch> optionalBranch = branchRepository.findById(materialReturn.getToId());
                if (optionalBranch.isPresent())
                    returnDto.setIssueFromName(optionalBranch.get().getBranchName());

            } else if (materialReturn.getToName() != null && materialReturn.getToName().equalsIgnoreCase("Godown")) {

                Optional<GoDown> optionalGoDown = goDownRepository.findById(materialReturn.getToId());
                if (optionalGoDown.isPresent())
                    returnDto.setIssueFromName(optionalGoDown.get().getName());
            }

            List<MaterialReturnItems> returnItems = materialReturnItemsRepository.findBy(id, returnId);
            List<MaterialReturnItemsDto> rs = returnItems.stream().map(data -> new MaterialReturnItemsDto(data)).collect(Collectors.toList());

            returnDto.setReturnItems(rs);

            return new ResponseEntity<MaterialReturnDto>(returnDto, HttpStatus.OK);
        } else {
            return new ResponseEntity<MaterialReturnDto>(HttpStatus.NOT_FOUND);
        }

    }

}

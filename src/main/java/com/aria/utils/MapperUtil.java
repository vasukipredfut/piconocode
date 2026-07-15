package com.aria.utils;


import com.aria.dto.AuxiliaryMachineMappingDto;
import com.aria.dto.JournalDto;
import com.aria.dto.JournalMachineMappingDto;
import com.aria.dto.JournalProductMappingDto;
import com.aria.model.Journal;
import com.aria.model.JournalMachineMapping;
import com.aria.model.JournalProductMapping;
import com.aria.model.User;
import org.springframework.stereotype.Component;


@Component
public class MapperUtil {


    public JournalProductMapping getMapDataDto(JournalProductMappingDto journalProductMappingDto, Journal journal, User loggedUser) {


        JournalProductMapping journalProductMapping = new JournalProductMapping(journalProductMappingDto);
        journalProductMapping.setStatus(true);
        journalProductMapping.setJournalId(journal.getId());
        journalProductMapping.setDeptId(journal.getDeptId());
        journalProductMapping.setBatchProductType(journalProductMappingDto.getBatchProductType());
        if (journalProductMappingDto.getBranchId() != null) {
            journalProductMapping.setBranchId(journalProductMappingDto.getBranchId());
        } else {
            journalProductMapping.setBranchId(loggedUser.getBranch_id());
        }

        return journalProductMapping;
    }

    public JournalProductMapping getMapData(JournalProductMapping resultProduct, Journal journal, User loggedUser, JournalProductMappingDto journalProductMappingDto) {

        resultProduct.setStatus(true);
        resultProduct.setJournalId(journal.getId());
        resultProduct.setDeptId(journal.getDeptId());

        if (journalProductMappingDto.getBranchId() != null) {
            resultProduct.setBranchId(journalProductMappingDto.getBranchId());
        } else {
            resultProduct.setBranchId(loggedUser.getBranch_id());
        }
        if (journalProductMappingDto.getBomTakeType() != null) {
            resultProduct.setBomTakeType(journalProductMappingDto.getBomTakeType());
        }
        resultProduct.setTrackno(journalProductMappingDto.getTrackno());
        resultProduct.setCost(journalProductMappingDto.getCost());
        resultProduct.setActivity(journalProductMappingDto.getActivity());
        resultProduct.setPridunit(journalProductMappingDto.getPridunit());
        resultProduct.setConsiderAs(journalProductMappingDto.getConsiderAs());
        resultProduct.setCost_amount(journalProductMappingDto.getCostAmount());
        resultProduct.setCost_per(journalProductMappingDto.getCostPer());
        resultProduct.setFqty(journalProductMappingDto.getFqty());
        resultProduct.setQuantity(journalProductMappingDto.getQuantity());
        if (journalProductMappingDto.getBatchProductType() != null && journalProductMappingDto.getBatchProductType() != "") {
            resultProduct.setBatchProductType(journalProductMappingDto.getBatchProductType());
        }else {
            resultProduct.setBatchProductType(null);
        }
        if (journalProductMappingDto.getApplicableForJcal() != null) {
            resultProduct.setApplicableForJcal(journalProductMappingDto.getApplicableForJcal());
        }
        if (journalProductMappingDto.getBottleWeight() != null && journalProductMappingDto.getBottleWeight() != "") {
            resultProduct.setBottleWeight(journalProductMappingDto.getBottleWeight());
        }else {
            resultProduct.setBottleWeight(null);
        }

        return resultProduct;
    }


    public JournalMachineMapping getMapDataDto(JournalMachineMappingDto journalMachineMappingDto, Journal journal, User loggedUser) {


        JournalMachineMapping journalMachineMapping = new JournalMachineMapping(journalMachineMappingDto);
        journalMachineMapping.setJournalId(journal.getId());
        journalMachineMapping.setDeptId(journal.getDeptId());
        journalMachineMapping.setStatus(true);
        if (journalMachineMappingDto.getBranchId() != null) {
            journalMachineMapping.setBranchId(journalMachineMappingDto.getBranchId());
        } else {
            journalMachineMapping.setBranchId(loggedUser.getBranch_id());
        }

        return journalMachineMapping;
    }


    public JournalMachineMapping getMapDataDto(JournalMachineMapping resultMachine, JournalMachineMappingDto journalMachineMappingDto, Journal journal, User loggedUser) {

        resultMachine.setStatus(true);
        resultMachine.setJournalId(journal.getId());
        resultMachine.setDeptId(journal.getDeptId());
        if (journalMachineMappingDto.getBranchId() != null) {
            resultMachine.setBranchId(journalMachineMappingDto.getBranchId());
        } else {
            resultMachine.setBranchId(loggedUser.getBranch_id());
        }
        journalMachineMappingDto.setJournalId(journal.getId());
        resultMachine.setDisplayName(journalMachineMappingDto.getDisplayName());
        resultMachine.setPrimaryWorkCenter(journalMachineMappingDto.getPrimaryWorkCenter());
        resultMachine.setPrimary_machine_id(journalMachineMappingDto.getMachineId());
        resultMachine.setPrimary_machine_name(journalMachineMappingDto.getMachineName());
        return resultMachine;
    }


    public JournalMachineMapping getMapDataDto(AuxiliaryMachineMappingDto journalMachineMappingDto, Journal journal, User loggedUser) {


        JournalMachineMapping journalMachineMapping = new JournalMachineMapping(journalMachineMappingDto);
        journalMachineMapping.setJournalId(journal.getId());
        journalMachineMapping.setDeptId(journal.getDeptId());
        journalMachineMapping.setStatus(true);
        if (journalMachineMappingDto.getBranchId() != null) {
            journalMachineMapping.setBranchId(journalMachineMappingDto.getBranchId());
        } else {
            journalMachineMapping.setBranchId(loggedUser.getBranch_id());
        }

        return journalMachineMapping;
    }


    public JournalMachineMapping getMapDataDto(JournalMachineMapping resultAuxiliaryMachine, AuxiliaryMachineMappingDto auxiliary, Journal journal, User loggedUser) {

        resultAuxiliaryMachine.setStatus(true);
        resultAuxiliaryMachine.setJournalId(journal.getId());
        resultAuxiliaryMachine.setDeptId(journal.getDeptId());
        if (auxiliary.getBranchId() != null) {
            resultAuxiliaryMachine.setBranchId(auxiliary.getBranchId());
        } else {
            resultAuxiliaryMachine.setBranchId(loggedUser.getBranch_id());
        }
        resultAuxiliaryMachine.setDisplayName(auxiliary.getDisplayName());
        resultAuxiliaryMachine.setMachineId(auxiliary.getMachineId());
        resultAuxiliaryMachine.setMachineName(auxiliary.getMachineName());
        resultAuxiliaryMachine.setPrimaryWorkCenter(auxiliary.getPrimaryWorkCenter());
        resultAuxiliaryMachine.setPrimary_machine_id(auxiliary.getPrimaryMachineId());
        resultAuxiliaryMachine.setPrimary_machine_name(auxiliary.getPrimaryMachineName());
        return resultAuxiliaryMachine;
    }


    public Journal getMapDataDto(Journal journal, JournalDto journalDto) {

        journal.setJournalName(journalDto.getJournalName());
        journal.setPrimaryProductId(journalDto.getPrimaryProductId());
        journal.setDeptId(journalDto.getDeptId());
        journal.setBatchProxId(journalDto.getBatchProxId());
        journal.setProcessType(journalDto.getProcessType());
        journal.setActivity(journalDto.getActivity());
        journal.setBlending(journalDto.getBlending());
        journal.setDataCollectionType(journalDto.getDataCollectionType());
        journal.setProductionCategory(journalDto.getProductionCategory());
        journal.setProductvityType(journalDto.getProductvityType());
        journal.setDisplayUnit(journalDto.getDisplayUnit());
        journal.setAsrs(journalDto.getAsrs());
       
        if (journalDto.getCompanyId() != null) {
            journal.setCompanyId(journalDto.getCompanyId());
        }
        if (journalDto.getBatchProxId() != null) {
            journal.setBatchProxId(journalDto.getBatchProxId());
        }
        if (journalDto.getActiveStatus() != null) {
            journal.setActiveStatus(journalDto.getActiveStatus());
        }

        if (journalDto.getCalculationType() != null) {
            journal.setCalculationType(journalDto.getCalculationType());
        }
        if (journalDto.getStockType() != null) {
            journal.setStockType(journalDto.getStockType());
        }

        return journal;
    }

}

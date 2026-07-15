package com.aria.dto;

import com.aria.model.ASRSJournalBranchMap;
import com.aria.model.Branch;
import com.aria.model.JournalBranchMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BranchDto {

    private Long id;

    private String branchName;

    public BranchDto(Branch branchDto) {
        this.id = branchDto.getId();
        this.branchName = branchDto.getBranchName();
    }

    public BranchDto(JournalBranchMap branchDto) {
        this.id = branchDto.getBranchId();
        this.branchName = branchDto.getBranchName();
    }

    public BranchDto(ASRSJournalBranchMap branchDto) {
        this.id = branchDto.getBranchId();
        this.branchName = branchDto.getBranchName();
    }
}

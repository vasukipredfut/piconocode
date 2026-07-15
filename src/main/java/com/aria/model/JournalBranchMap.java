package com.aria.model;

import com.aria.dto.BranchDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "journal_branch_map")
public class JournalBranchMap {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "journal_id")
    private Long journalId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "branch_name")
    private String branchName;


    public JournalBranchMap(BranchDto branchDto) {
        this.branchId = branchDto.getId();
        this.branchName = branchDto.getBranchName();
    }
}

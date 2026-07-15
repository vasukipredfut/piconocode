package com.aria.model;

import com.aria.dto.HupMasterDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "branch_Org_Map")
public class BranchMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "branch")
    private Long branch;

    @Column(name = "company")
    private Long company;



    public BranchMapping(HupMasterDTO hupMaster) {
        this.branch = hupMaster.getBranch();
        this.company = hupMaster.getCompany();
    }
}

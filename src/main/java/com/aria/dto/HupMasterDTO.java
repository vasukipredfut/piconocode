package com.aria.dto;

import com.aria.model.BranchMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HupMasterDTO {

    private Long hubId;
    private Long branch;
    private Long company;
    private Long hoBranchId;
    private Long hoCompanyId;
    private Long hubName;


}

package com.aria.dto;

import com.aria.model.DwCluster;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class DwClusterDto {

    private Long id;

  
    private String name;

  
    private Long dept_id;
  
    private Long branch_id;

    public DwClusterDto(DwCluster clusterDto) {
        this.id = clusterDto.getId();
        this.name = clusterDto.getName();
        this.dept_id = clusterDto.getDept_id();
        this.branch_id = clusterDto.getBranch_id();
    }
}

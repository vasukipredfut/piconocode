package com.aria.model;

import com.aria.dto.DwClusterDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "dwcluster")
public class DwCluster {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long dept_id;
    @Column
    private Long branch_id;

    public DwCluster(DwClusterDto clusterDto) {
        this.id = clusterDto.getId();
        this.name = clusterDto.getName();
        this.dept_id = clusterDto.getDept_id();
        this.branch_id = clusterDto.getBranch_id();
    }
}

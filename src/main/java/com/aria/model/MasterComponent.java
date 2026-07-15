package com.aria.model;


import com.aria.dto.MasterComponentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Where;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "master_component")
public class MasterComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String componentName;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @OneToOne
    @JoinColumn(name = "dept_id", referencedColumnName = "id")
    private DepartmentDetails departmentDetails;

    @Column
    private String userLevelAccess;

    @Column
    private int active;

    @Column(updatable = false)
    private Long createdBy;

    @Column
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @OneToMany(mappedBy = "masterComponentId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Where(clause = "active=0")
    private List<MasterField> fieldList;

    public MasterComponent(MasterComponentDto masterComponentDto) {
        this.id = masterComponentDto.getId();
        this.componentName = masterComponentDto.getComponentName();
        this.userLevelAccess = masterComponentDto.getUserLevelAccess();
        this.active = masterComponentDto.getActive();
        this.createdBy = masterComponentDto.getCreatedBy();
        this.updatedBy = masterComponentDto.getUpdatedBy();
        this.created_date = masterComponentDto.getCreated_date();
        this.updated_date = masterComponentDto.getUpdated_date();
    }
}

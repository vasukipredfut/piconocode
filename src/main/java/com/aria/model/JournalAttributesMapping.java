package com.aria.model;

import com.aria.dto.JournalAttributesMappingDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "journal_attributes_mapping")
public class JournalAttributesMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long journal_id;

    @Column
    private Long dept_id;

    @Column
    private Long branch_id;

    @Column
    private Long attributes_id;

    @Column
    private String attributes_name;

    @Column
    private int disporder;

    @Column
    private String datatype;

    @Column
    private String awith;

    @Column
    private Long awithid;

    @Column
    private String awithtable;

    @Column
    private Long dispunit;

    @Column
    private String minimum_value;

    @Column
    private String maximum_value;

    @Column
    private String connect_table;

    @Column(name = "type_of_map")
    private String typeOfMap;

    @Column(name = "consider_as")
    private String considerAs;

    @Column(name = "app_cal_status")
    private Boolean appCalStatus;

    @Column
    private Boolean status;

    @OneToMany(mappedBy = "attributes_map_id", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<JournalAttributesOption> options;


    @Column(name = "bom_intake_type")
    private String bomIntakeType;

    public JournalAttributesMapping(JournalAttributesMappingDto attributesMapping) {
        this.id = attributesMapping.getId();
        this.journal_id = attributesMapping.getJournalId();
        this.dept_id = attributesMapping.getDeptId();
        this.branch_id = attributesMapping.getBranchId();
        this.attributes_id = attributesMapping.getAttributesId();
        this.attributes_name = attributesMapping.getAttributesName();
        this.disporder = attributesMapping.getDisporder();
        this.datatype = attributesMapping.getDatatype();
        this.awith = attributesMapping.getType();
        this.awithid = attributesMapping.getAttributesId();
        this.awithtable = attributesMapping.getTypeTable();
        this.dispunit = attributesMapping.getDispunit();
        this.minimum_value = attributesMapping.getMinimumValue();
        this.maximum_value = attributesMapping.getMaximumValue();
        this.connect_table = attributesMapping.getConnectTable();
        this.typeOfMap = attributesMapping.getTypeOfMap();
        this.considerAs = attributesMapping.getConsiderAs();
        this.appCalStatus = attributesMapping.getAppCalStatus();
        this.bomIntakeType = attributesMapping.getBomIntakeType();
    }
}

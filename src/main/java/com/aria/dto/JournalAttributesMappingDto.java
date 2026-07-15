package com.aria.dto;


import com.aria.model.JournalAttributesMapping;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class JournalAttributesMappingDto {

    private Long id;
    private Long journalId;
    private Long deptId;
    private Long branchId;
    private Long attributesId;
    private String attributesName;
    private int disporder;
    private String datatype;
    private String type;
    private Long typeId;
    private String typeTable;
    private Long dispunit;
    private String minimumValue;
    private String maximumValue;
    private String connectTable;
    private String typeOfMap;
    private String considerAs;
    private String bomIntakeType;
    private Boolean appCalStatus;

    List<JournalAttributesOptionDto> options;

    public JournalAttributesMappingDto(JournalAttributesMapping attributesMapping) {
        this.id = attributesMapping.getId();
        this.journalId = attributesMapping.getJournal_id();
        this.deptId = attributesMapping.getDept_id();
        this.branchId = attributesMapping.getBranch_id();
        this.attributesId = attributesMapping.getAttributes_id();
        this.attributesName = StringEscapeUtils.unescapeHtml4(attributesMapping.getAttributes_name());
        this.disporder = attributesMapping.getDisporder();
        this.datatype = attributesMapping.getDatatype();
        this.type = attributesMapping.getAwith();
        this.typeId = attributesMapping.getAwithid();
        this.typeTable = attributesMapping.getAwithtable();
        this.dispunit = attributesMapping.getDispunit();
        this.minimumValue = attributesMapping.getMinimum_value();
        this.maximumValue = attributesMapping.getMaximum_value();
        this.connectTable = attributesMapping.getConnect_table();
        this.typeOfMap = attributesMapping.getTypeOfMap();
        this.considerAs = attributesMapping.getConsiderAs();
        this.appCalStatus = attributesMapping.getAppCalStatus();
        this.options = attributesMapping.getOptions() != null
                ? attributesMapping.getOptions().stream().map(field -> new JournalAttributesOptionDto(field)).collect(Collectors.toList()) : null;

    }
}

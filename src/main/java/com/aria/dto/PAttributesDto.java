package com.aria.dto;

import com.aria.model.PAttributes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;


@Getter
@Setter
@NoArgsConstructor
public class PAttributesDto {

    private Long id;

    private String attributesName;

    private String datatype;

    private Long deptId;

    private String deptName;

    public PAttributesDto(PAttributes pAttributes) {
        this.id = pAttributes.getId();
        this.attributesName = StringEscapeUtils.unescapeHtml4(pAttributes.getName());
        this.deptId = pAttributes.getDept();
        this.datatype = pAttributes.getDatatype();
    }
}

package com.aria.dto;

import com.aria.model.JaggeryOptions;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;


@Getter
@Setter
@NoArgsConstructor
public class JaggeryOptionsDto {

    private Long id;

    private Long attributeId;
    
    private String name;

    private String range;

    public JaggeryOptionsDto(JaggeryOptions jaggeryOptions) {
        this.id = jaggeryOptions.getId();
        this.attributeId = jaggeryOptions.getAttributeId();
        this.name = StringEscapeUtils.unescapeHtml4(jaggeryOptions.getName());
        this.range = jaggeryOptions.getRange();
    }
}

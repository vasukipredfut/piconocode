package com.aria.dto;

import com.aria.model.JaggeryAttributes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@NoArgsConstructor
public class JaggeryAttributesDto {


    private Long id;

    private String name;

    private List<JaggeryOptionsDto> jaggeryOptionsList;

    public JaggeryAttributesDto(JaggeryAttributes jaggeryAttributes) {
        this.id = jaggeryAttributes.getId();
        this.name = StringEscapeUtils.unescapeHtml4(jaggeryAttributes.getName());
        this.jaggeryOptionsList = jaggeryAttributes.getJaggeryOptions() != null
                ?jaggeryAttributes.getJaggeryOptions().stream().map(field -> new JaggeryOptionsDto(field)).collect(Collectors.toList()) : null;
    }
}

package com.aria.dto;


import com.aria.model.AttributeFieldsProcessTypeValueBased;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AttributeFieldsProcessTypeValueBasedDto {


    private Long id;

    private Long attributes_id;

    private String min_value;

    private String max_value;
    private String score;

    public AttributeFieldsProcessTypeValueBasedDto(AttributeFieldsProcessTypeValueBased valueBased) {
        this.id = valueBased.getId();
        this.attributes_id = valueBased.getAttributes_id();
        this.min_value = valueBased.getMin_value();
        this.max_value = valueBased.getMax_value();
        this.score = valueBased.getScore();
    }
}

package com.aria.model;

import com.aria.dto.AttributeFieldsProcessTypeValueBasedDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "values")
public class AttributeFieldsProcessTypeValueBased {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long attributes_id;

    @Column
    private String min_value;

    @Column
    private String max_value;

    @Column
    private String score;

    public AttributeFieldsProcessTypeValueBased(AttributeFieldsProcessTypeValueBasedDto valueBased) {
        this.id = valueBased.getId();
        this.attributes_id = valueBased.getAttributes_id();
        this.min_value = valueBased.getMin_value();
        this.max_value = valueBased.getMax_value();
        this.score = valueBased.getScore();
    }

}

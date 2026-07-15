package com.aria.model;

import com.aria.dto.AttributeFieldsProcessTypeOptionBasedDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "maintenance_options")
public class MaintenanceAttributeFieldsOptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long attributes_id;

    @Column
    private String options;

    @Column
    private String score;

    public MaintenanceAttributeFieldsOptions(AttributeFieldsProcessTypeOptionBasedDto field) {
        this.id = field.getId();
        this.options = field.getOptions();
        this.score = field.getScore();
    }
}

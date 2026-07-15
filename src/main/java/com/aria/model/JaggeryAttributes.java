package com.aria.model;

import com.aria.dto.JaggeryAttributesDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

import jakarta.persistence.*;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "jattributes")
public class JaggeryAttributes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @OneToMany(mappedBy = "attributeId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<JaggeryOptions> jaggeryOptions;

    public JaggeryAttributes(JaggeryAttributesDto jaggeryAttributes) {
        this.id = jaggeryAttributes.getId();
        this.name = StringEscapeUtils.unescapeHtml4(jaggeryAttributes.getName());


    }
}

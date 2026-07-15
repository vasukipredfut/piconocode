package com.aria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.JaggeryAttributesValuesDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;


@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "jaggattrvalues")
public class JaggeryAttributesValues {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String uniqno;

    @Column
    private Long color;

    @Column
    private Long quality;

    @Column
    private Long thul;

    @Column
    private Long thulfrom;

    @Column
    private Long thulto;

    @Column
    private Long vabus;

    @Column
    private Double vabqty;

    @Column
    private Long gunny;

    @Column
    private Long comment;

    @Column(name = "vabus_Value")
    private String vabusValue;

    @Column(name = "weight_Value")
    private String weightValue;


    public JaggeryAttributesValues(JaggeryAttributesValuesDto attributesValues) {
        this.id = attributesValues.getId();
        this.uniqno = attributesValues.getUniqno();
        this.color = attributesValues.getColor();
        this.quality = attributesValues.getQuality();
        this.thul = attributesValues.getThul();
        this.thulfrom = attributesValues.getThulfrom();
        this.thulto = attributesValues.getThulto();
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.vabusValue = mapper.writeValueAsString(attributesValues.getVabusValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.gunny = attributesValues.getGunny();
        this.comment = attributesValues.getComment();

        try {
            this.weightValue = mapper.writeValueAsString(attributesValues.getWeightValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

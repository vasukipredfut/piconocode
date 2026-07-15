package com.aria.model;

import com.aria.dto.DistrictsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "districts")
public class Districts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private String state;

    @Column(name = "stateid")
    private Long stateId;

    @Column(name = "countryid")
    private Long countryId;


    public Districts(DistrictsDto districts) {
        this.id = districts.getId();
        this.name = districts.getName();
        this.state = districts.getState();
        this.stateId = districts.getStateId();
        this.countryId = districts.getCountryId();
    }
}

package com.aria.model;

import com.aria.dto.CampDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@NoArgsConstructor
@Table(name = "camp")
@Getter
@Setter
public class Camp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String camp;

    @Column
    private String place;

    @Column
    private String state;

    @Column
    private Long  state_id;

    @Column(name = "updated_on")
    private String updatedOn;

    public Camp(CampDto campmodel) {
        this.id = campmodel.getId();
        this.camp = campmodel.getCamp();
        this.place = campmodel.getPlace();
        this.state = campmodel.getState();
        this. state_id = campmodel.getStateId();
        this.updatedOn = campmodel.getUpdatedOn();
    }

}

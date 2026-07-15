package com.aria.model;

import com.aria.dto.StateDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "state")
public class State {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(name = "countryid")
    private Long countryId;

    @Column(name = "gst_state_code")
    private String gstStateCode;

    public State(StateDto state) {
        this.id = state.getId();
        this.name = state.getName();
        this.countryId = state.getCountryId();
        this.gstStateCode = state.getGstStateCode();
    }
}

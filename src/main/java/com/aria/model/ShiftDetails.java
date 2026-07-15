package com.aria.model;

import com.aria.dto.ShiftDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shift_details")
public class ShiftDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @Column(name = "shift_name")
    private String shiftName;

    public ShiftDetails(ShiftDto shiftDto) {
        this.id = shiftDto.getId();
        this.shiftName = shiftDto.getShiftName();
    }
}

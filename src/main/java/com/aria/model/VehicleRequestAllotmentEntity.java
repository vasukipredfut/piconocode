package com.aria.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vehicle_request_allot")
public class VehicleRequestAllotmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "req_id")
    private Long reqId;

    @Column(name = "distance")
    private Long distance;

    @Column(name = "vehicle_info_id")
    private Long vehicleInfoId;

    @Column(name = "status")
    private String status;

    @Column(name = "capacity")
    private String capacity;


    @Column(name = "departure_on")
    private Date departureOn;


    @Column(name = "arrival_on")
    private Date arrivalOn;


    @Column(name = "actual_departure_on")
    private Date actualDepartureOn;


    @Column(name = "actual_arrival_on")
    private Date actualArrivalOn;

    @Column(name = "shift")
    private String shift;

    @Column(name = "shift_slab")
    private String shiftSlab;

    @Column(name = "arrival_plant")
    private String arrivalPlant;

    @Column(name = "approx_date")
    private Date approxDate;

    @Column(name = "CREATED_BY")
    public Long createdBy;

    @Column(name = "departure_by")
    public Long departureBy;

    @Column(name = "arrival_by")
    public Long arrivalBy;

    @Column(name = "updated_by")
    public Long updatedBy;


    @CreatedDate
    @Column(name = "CREATED_DATE")
    public Date createdDate;

    @LastModifiedDate
    @Column(name = "UPDATED_DATE")
    public Date updatedDate;

    @PreUpdate
    public void updateDate() {
        this.setUpdatedDate(new Date());
    }

}

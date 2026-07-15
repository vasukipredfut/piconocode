package com.aria.model;

import com.aria.dto.BillInwardLorryDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "bill_lorry_details")
public class BillInwardLorryDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inward_id")
    private Long inwardId;

    @Column(name = "lorry_no")
    private String lorryNo;

    @Column(name = "payable_freight_amount")
    private String payableFreightAmount;

    @Column(name = "pan_no")
    private String panNo;

    @Column(name = "payee_name")
    private String payeeName;

    @Column(name = "contact_number")
    private String contactNumber;

    public BillInwardLorryDetails(BillInwardLorryDto lorryDetails) {
        this.id = lorryDetails.getId();
        this.inwardId = lorryDetails.getInwardId();
        this.lorryNo = lorryDetails.getLorryNo();
        this.payableFreightAmount = lorryDetails.getPayableFreightAmount();
        this.panNo = lorryDetails.getPanNo();
        this.payeeName = lorryDetails.getPayeeName();
        this.contactNumber = lorryDetails.getContactNumber();
    }
}

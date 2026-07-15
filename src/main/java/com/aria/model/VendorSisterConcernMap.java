package com.aria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.VendorSisterConcernMapDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vendor_sister_concern_map")
public class VendorSisterConcernMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name")
    private String groupName;

    @Column
    private String vendors;


    public VendorSisterConcernMap(VendorSisterConcernMapDto vendorSisterConcernMap) {
        this.id = vendorSisterConcernMap.getId();
        this.groupName = vendorSisterConcernMap.getGroupName();
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.vendors = mapper.writeValueAsString(vendorSisterConcernMap.getVendors());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

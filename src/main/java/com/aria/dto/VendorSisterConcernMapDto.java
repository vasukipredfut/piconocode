package com.aria.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.VendorSisterConcernMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
@NoArgsConstructor
public class VendorSisterConcernMapDto {

    private Long id;

    private String groupName;

    private List<Map<Long, String>> vendors;

    private List<Long> deleteVendors;

    public VendorSisterConcernMapDto(VendorSisterConcernMap vendorSisterConcernMap) {
        this.id = vendorSisterConcernMap.getId();
        this.groupName = vendorSisterConcernMap.getGroupName();
        ObjectMapper mapper = new ObjectMapper();
        try {
            this.vendors = mapper.readValue(vendorSisterConcernMap.getVendors(),new TypeReference< List<Map<Long, String>> >(){});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

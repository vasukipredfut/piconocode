package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CloseVendorSpec {

    public Long proposalId;

    public Long vendorId;

    private boolean active;

    private List<Long> proposalSpecId;

}

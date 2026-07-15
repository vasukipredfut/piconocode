package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BlendingRequest {


    public Long productId;
    public Long issueBranch;
    public Long issueDeptId;
    public double qty;


}

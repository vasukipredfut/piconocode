package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CwagesDetailsReqDTO {

    public Long cwagesId;
    public Long salStructId;

    public Double unitRate;

    public Double qty;
    public Double amt;

}

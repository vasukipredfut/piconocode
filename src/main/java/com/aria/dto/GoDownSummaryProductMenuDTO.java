package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class GoDownSummaryProductMenuDTO {

    public String menuName;
    List<LotQtyDto> lotWithQty;
}

package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
@Setter
public class RawMaterialMovementSummary {

    private Long itemId;
    private String itemName;
    List<Double> lotList;
    Map<String,Object> openingStock;
    Map<String,Object> purchases;
    Map<String,Object> receivedFromBranchWise;
    Map<String,Object> issueFrom;
    Map<String,Object> issueFromProcess;
    Map<String,Object> receiveFromProcess;
    Map<String,Object> closingStock;


}

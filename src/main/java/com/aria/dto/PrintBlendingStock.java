package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PrintBlendingStock {

    private List<Long> binList;
    private List<String> batchNoList;
    private Long productId;

}

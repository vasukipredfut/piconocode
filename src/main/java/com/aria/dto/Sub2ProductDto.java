package com.aria.dto;



import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Sub2ProductDto {
private Long productId;
private String productName;
List<LotQtyDto> lotWithQty;
}

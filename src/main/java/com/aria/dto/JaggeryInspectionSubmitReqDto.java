package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class JaggeryInspectionSubmitReqDto {

    private Long vendorId;
    private String billDate;
    private String jagDate;
    private String billNo;

}

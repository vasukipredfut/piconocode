package com.aria.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class EpayMasterAndDepayMasterDto {
	List<EpayMasterDto> staff=new ArrayList<EpayMasterDto>();
	List<DepayMasterDto> wagesEmployee=new ArrayList<DepayMasterDto>();
}

package com.aria.dto;


import com.aria.model.DailyUserDependency;
import com.aria.model.EmastDaily;
import com.aria.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContractLabourDropDownDto {
    private Long id;
    private String name;
}

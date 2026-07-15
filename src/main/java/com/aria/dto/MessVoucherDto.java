package com.aria.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MessVoucherDto {
      String date;
      Long month;
      Long year;
      Long branch;
      Long company;
      List<Long> billIds;
}

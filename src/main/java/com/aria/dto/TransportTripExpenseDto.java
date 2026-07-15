package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TransportTripExpenseDto {
    private TransportTripDto trip;
    List<ExpenseDto> expenses=new ArrayList<>();
}

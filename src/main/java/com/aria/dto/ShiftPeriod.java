package com.aria.dto;



import com.aria.model.Shifts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShiftPeriod {

    private String date;

    private String shift;
    private Shifts shift_details;

    
}

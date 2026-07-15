package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QualificationDto {

    public String qualification;
    public String marks;

    public QualificationDto(String qualification, String marks) {
        this.qualification = qualification;
        this.marks = marks;
    }
}

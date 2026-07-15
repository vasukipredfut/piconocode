package com.aria.model;

import com.aria.dto.FingerPrintDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "finger_print")
public class FingerPrint {

    @Id
    @GenericGenerator(name = "elementKey", strategy = "assigned")
    @GeneratedValue(generator = "elementKey")
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "secure_key")
    private String secureKey;

    @Column
    private String status;


    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    public FingerPrint(FingerPrintDto fingerPrintDto) {
        this.userId = fingerPrintDto.getEmployeeId();
        this.secureKey = fingerPrintDto.getSecureKey();
    }
}

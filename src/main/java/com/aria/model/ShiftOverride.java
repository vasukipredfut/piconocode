package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "shift_override")
public class ShiftOverride {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long emp_id;
    
    @Column
    private Date shift_date;
    
    @Column
    private LocalDateTime createddate;
    
    @Column
    private Long createdby;
    
    @Column
    private LocalDateTime updateddate;
    
    @Column
    private Long updatedby;
    
    @Column
    private Long shift_id;
 

}

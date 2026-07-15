package com.aria.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.WelfareEmployeeDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "welfare_employee")
public class WelfareEmployee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "emp_id")
	private Long empId;

	@Column
	private Date registrationon;

	@Column
	private String item;

	@Column
	private Long cost;
	
    @OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;
	
	public WelfareEmployee(WelfareEmployeeDto welfareEmployee) {
        this.id = welfareEmployee.getId();
        this.empId = welfareEmployee.getEmpId();
        if(welfareEmployee.getEmpId() != null){
            User user = new User();
            user.setId(welfareEmployee.getEmpId());
            this.user = user;
        }
        this.registrationon = welfareEmployee.getRegistrationon();
        this.item = welfareEmployee.getItem();
        this.cost = welfareEmployee.getCost();
        
    }
}



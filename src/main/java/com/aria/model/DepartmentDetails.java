package com.aria.model;

import com.aria.dto.DepartmentDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "department_details")
public class DepartmentDetails {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @OneToOne
    @JoinColumn(name = "org_id", referencedColumnName = "id")
    private Organisation organisation;

    @Column
    private String contract;

    @Column
    private String capacity;

    @Column
    private String resource;

    @Column
    private String reminders;

    @Column
    private String productivity;

    @Column
    private String utilisation;

    @Column
    private String purpose;

    @Column
    private String workinst;

    @Column
    private String trnprefix;

    @Column
    private String pv;

    @Column
    private String costreport;

    @Column
    private String milluse;
    

    public Long getId() {
		return id;
	}



	public void setId(Long id) {
		this.id = id;
	}


	public DepartmentDetails(DepartmentDto departmentDto) {
        this.id = departmentDto.getId();
        this.name = departmentDto.getName();
    }

}

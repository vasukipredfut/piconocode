package com.aria.model;


import com.aria.dto.UserDependencyDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "employee_dependency")
public class UserDependency {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userid;

    @Column
    private String name;

    @Column
    private String education;

    @Column(name = "dateofbirth")
    private String dependencyDOB;

    @Column(name = "weddingdate")
    private String weddingDate;

    @Column(name = "relationship")
    private String relationShip;

    @Column(name = "aadharno")
    private String aadharNo;

    public UserDependency(UserDependencyDto userDependency) {
        this.id = userDependency.getId();
        this.userid = userDependency.getUserId();
        this.name = userDependency.getName();
        this.education = userDependency.getEducation();
        this.dependencyDOB = userDependency.getDependencyDOB();
        this.weddingDate = userDependency.getWeddingDate();
        this.relationShip = userDependency.getRelationShip();
        this.aadharNo = userDependency.getAadharNo();
    }
}

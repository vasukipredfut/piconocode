package com.aria.dto;

import com.aria.model.UserDependency;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class UserDependencyDto {


    private Long id;

    private Long userId;

    private String name;

    private String education;

    private String dependencyDOB;

    private String weddingDate;

    private String relationShip;

    private String aadharNo;

    public UserDependencyDto(UserDependency userDependency) {
        this.id = userDependency.getId();
        this.userId = userDependency.getUserid();
        this.name = userDependency.getName();
        this.education = userDependency.getEducation();
        this.dependencyDOB = userDependency.getDependencyDOB();
        this.weddingDate = userDependency.getWeddingDate();
        this.relationShip = userDependency.getRelationShip();
        this.aadharNo = userDependency.getRelationShip();
    }
}

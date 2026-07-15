package com.aria.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "permissions")
public class Permissions {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    @Column(name="role_id")
    private Integer roleId;
    @Column
    private Integer module_id;
    @Column
    private Integer sub_module_id;
    @Column
    private String role_name;
    @Column
    private String module_name;
    @Column
    private String sub_module_name;
    @Column
    private Integer action_items_id;
    @Column
    private String action_items_name;

}

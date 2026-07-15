package com.aria.model;

import com.aria.dto.BankDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "bank")
public class Bank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    public Bank(BankDTO bankDTO) {
        this.id = bankDTO.getId();
        this.name = bankDTO.getName();
    }
}

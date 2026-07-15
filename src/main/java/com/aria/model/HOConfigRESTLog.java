package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Table(name = "ho_config_rest_log")
@Getter
@Setter
public class HOConfigRESTLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_id")
    private Long typeId;

    @Column
    private String type;

    @Column
    private String from_office;

    @Column
    private String status;

    @Column
    private String response;

    @Column
    private String json_data;

    @Column
    private LocalDateTime created_time;

    @Column(name = "create_type")
    private String createType;
}

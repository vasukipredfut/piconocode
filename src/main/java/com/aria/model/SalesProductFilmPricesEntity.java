package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "salesprices")
public class SalesProductFilmPricesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@Column(name = "salesproductid")
	private Long salesProductId;

    @Column(name = "saleslocationid")
    private Long salesLocationId;

    @Column(name="salesproductgroupid")
    private Long salesProductGroupId;

    @Column
    private Double price;

    @Column(name = "per_ml_price")
    private Double permlprice;

    @Column
    private String status="";

    @Column(name="ip")
    private String ip="";

    @Column(name="updatedby")
    private Long updatedBy;

    @Column(name="fromdate")
    private Date fromDate;

    @Column(name="entereddate")
    private Date enteredDate;

}

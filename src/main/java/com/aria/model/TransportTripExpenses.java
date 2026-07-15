package com.aria.model;


import com.aria.dto.ExpenseDto;
import com.aria.dto.TransportTripDto;
import com.aria.dto.TransportTripExpenseDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "tripexp")
public class TransportTripExpenses {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tripid", referencedColumnName = "id", nullable = false)
	private TransportTrip trip;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "expid", referencedColumnName = "id", nullable = false)
	private Expense expense;

	@Column
	private String slab;

	@Column
	private Long slabid;

	@Column
	private String adriver;

	@Column
	private Long disporder;

	@Column
	private String swfare;



}

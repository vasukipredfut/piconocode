
package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.DayDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "day")
public class Day {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "day_name")
	private String dayName;

	public Day(DayDto dayDto) {
		this.id = dayDto.getId();
		this.dayName = dayDto.getDayName();
	}
}

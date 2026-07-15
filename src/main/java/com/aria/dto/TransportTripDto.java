package com.aria.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.aria.model.TransportTrip;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;


@Getter
@Setter
@NoArgsConstructor
public class TransportTripDto {
	private Long id;
	private String name;
	private String shortname;
	private String coverage;
	private String startTime;
	private String jhrs;
	private String bhrs;
	private String freq;
	private String cate;
	private String distance;

	public TransportTripDto(TransportTrip transportTrip) {
		this.id = transportTrip.getId();
		this.name = transportTrip.getName();
		this.shortname = transportTrip.getShortname();
		this.coverage = transportTrip.getCoverage();
		this.startTime = new SimpleDateFormat("HH:mm:ss").format(transportTrip.getStartTime());
		this.jhrs = transportTrip.getJhrs();
		this.bhrs = transportTrip.getBhrs();
		this.freq = transportTrip.getFreq();
		this.cate = transportTrip.getCate();
		this.distance = transportTrip.getDistance();
	}
}

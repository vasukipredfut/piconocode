package com.aria.dto;

public interface TimingRawDataProjection {
	    Long getTimingid();       // maps to t.id
	    String getTimingname();   // maps to t.name
	    String getAttributename();// maps to a.name
	    Long getAttributeid();
	    Double getOpeningreading(); // maps to tr.openingReading
	    Double getClosingreading(); // maps to tr.closingReading
	    Double getDifference();     // maps to tr.difference

}

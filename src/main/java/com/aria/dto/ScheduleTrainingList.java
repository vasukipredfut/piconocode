package com.aria.dto;

import java.util.Date;

public interface ScheduleTrainingList {

     Long getId();
     String getTrainingType();
     String getTrainingTopic() ;
     Date getLastRegistrationDate() ;
     String getStatus();
     Integer getTotalSeats() ;
     Integer getRemainingSeats();

}

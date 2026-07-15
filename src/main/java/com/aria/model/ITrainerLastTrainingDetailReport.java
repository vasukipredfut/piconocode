package com.aria.model;

import java.util.Date;

public interface ITrainerLastTrainingDetailReport {

	Long getTrainerDetailId();

	Long getLastScheduleTrainingId();

	Date getLastTrainingDate();

	Double getLastTrainingScore();

}
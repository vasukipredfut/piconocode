package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleTrainingListDto {

    private Long id;
    private String trainingType;
    private String trainingTopic;
    private String lastRegistrationDate;
    private String status;
    private Integer totalSeats;
    private Integer remainingSeats;


    public ScheduleTrainingListDto(ScheduleTrainingList scheduleTrainingList) {
        DateUtil dateUtil = new DateUtil();
        this.id = scheduleTrainingList.getId();
        this.trainingType = scheduleTrainingList.getTrainingType();
        this.trainingTopic = scheduleTrainingList.getTrainingTopic();
        this.lastRegistrationDate = dateUtil.getDateTOStringWk(scheduleTrainingList.getLastRegistrationDate());
        this.status = scheduleTrainingList.getStatus();
        this.totalSeats = scheduleTrainingList.getTotalSeats();
        this.remainingSeats = scheduleTrainingList.getRemainingSeats();
    }


}

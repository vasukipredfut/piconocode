package com.aria.model;

import com.aria.dto.AttendanceDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "attendance")
public class Attendance {

	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	private User user;

	@Column
	private Date date;

	@Column
	private Date punchin;

	@Column
	private Date punchout;

	@Column
	private String production;

	@Column(name = "over_time")
	private String overTime;

	@Column
	private String Break;

	@Column
	private Date Breakin;

	@Column
	private Date Breakout;
	
	@Column(name = "over_Due_To_Break_Time_Reason")
	private String overDueToBreakTimeReason;

	@Column(name = "over_Time_Reason")
	private String overTimeReason;
	
	@Column
	private String status;

	@Column
	private int active;
	
	@OneToMany(mappedBy = "attendanceId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<AttendanceTimeline> attendanceTimelines;

	@Column(name = "created_by",updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;
	

	@Column
	private String atype;

	public Attendance(AttendanceDto attendance) {
		this.id = attendance.getId();
		if(attendance.getUserId() != null){
            User user= new User();
            user.setId(attendance.getUserId());
            this.user = user;
        }
		this.production = attendance.getProduction();
		this.Break = attendance.getBreak();
		if (attendance.getOverDueToBreakTimeReason() != null) {
            this.Break = attendance.getBreak().trim();
        }
        this.status = attendance.getStatus();
        if (attendance.getOverDueToBreakTimeReason() != null) {
            this.overDueToBreakTimeReason = attendance.getOverDueToBreakTimeReason().trim();
        }
        
        
        if (attendance.getOverTimeReason() != null) {
            this.overTime = attendance.getOverTime().trim();
        }
        this.status = attendance.getStatus();
        if (attendance.getOverTimeReason() != null) {
            this.overTimeReason = attendance.getOverTimeReason().trim();
        }
	    
        this.status =attendance.getStatus();
		this.overTime = attendance.getOverTime();
		this.active = attendance.getActive();		
		this.createdBy = attendance.getCreatedBy();
		this.updatedBy = attendance.getUpdatedBy();
		this.created_date = attendance.getCreated_date();
		if (attendance.getUpdated_date() != null) {
			this.updated_date = attendance.getUpdated_date();
		} else {
			this.updated_date = attendance.getCreated_date();
		}
	}
}

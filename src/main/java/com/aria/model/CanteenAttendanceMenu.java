
package com.aria.model;

import com.aria.dto.CanteenAttendanceMenuDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "canteen_attendance_menu")
public class CanteenAttendanceMenu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "attendance_menu_id")
	private Long attendanceMenuId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public User user;

	@Column(name = "in_time")
	private Date inTime;

	@Column(name = "out_time")
	private Date outTime;
	
	@Column
	private String type;
	
	public CanteenAttendanceMenu(CanteenAttendanceMenuDto canteenAttendanceMenu) {
		this.id = canteenAttendanceMenu.getId();
		this.attendanceMenuId = canteenAttendanceMenu.getAttendanceMenuId();
		 if(canteenAttendanceMenu.getUserId() != null){
	            User user = new User();
	            user.setId(canteenAttendanceMenu.getUserId());
	            this.user = user;
	        }
		 this.type=canteenAttendanceMenu.getType();  
		
	}

}
    
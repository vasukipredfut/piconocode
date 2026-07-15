package com.aria.model;

import com.aria.dto.CanteenMenuDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import org.hibernate.annotations.Where;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "canteen_menu")
public class CanteenMenu {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "from_date")
	@Temporal(TemporalType.DATE)
	private Date fromDate;

	@Column(name = "to_date")
	@Temporal(TemporalType.DATE)
	private Date toDate;
	
    @Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_By")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@OneToMany(mappedBy = "foodId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "menutype='dinner'")
	private List<MenuItems> dinnerItems;

	@OneToMany(mappedBy = "foodId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "menutype='breakfast'")
	private List<MenuItems> breakfastItems;
	
	@OneToMany(mappedBy = "foodId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "menutype='lunch'")
	private List<MenuItems> lunchItems;
	
	@OneToMany(mappedBy = "foodId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "menutype='beverage'")
	private List<MenuItems> beverageItems;
	
	@OneToMany(mappedBy = "foodId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Where(clause = "menutype='snacks'")
	private List<MenuItems> snacksItems;

	@OneToMany(mappedBy = "foodId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<CanteenMenuDayMapping> days;

	public CanteenMenu(CanteenMenuDto canteenMenu) {
		this.id = canteenMenu.getId();
		this.createdBy = canteenMenu.getCreatedBy();
		this.updatedBy = canteenMenu.getUpdatedBy();
		this.created_date = canteenMenu.getCreated_date();
		this.updated_date = canteenMenu.getUpdated_date();
	}

}
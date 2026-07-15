package com.aria.model;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.SalesProductFilmOrderDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "salesproductfilmorder")
public class SalesProductFilmOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "filmid", referencedColumnName = "id")
	private SalesProductFilm filmId;

	@Column(name = "locationid")
	private String location;

	@Column(name = "sublocationid")
	private Long subLocationId;

	@Column(name = "mrp")
	private Double mrp;

	@Column(name = "per_ml_price")
	private Double perMlPrice;

	@Column(name = "lot")
	private Long lot;

	@Column(name = "orderqty")
	private Double orderQty;

	@Column(name = "orderqtykg")
	private Double orderQtyKg;

	@Column(name = "stockqty")
	private Long stockInPouch;

	@Column(name = "printedqty")
	private Double printedQty;

	@Column(name = "balance")
	private Double balance;

	@Column(name = "orderdate")
	private Date orderDate; // datetime need

	@Column(name = "packedon")
	private String packedOn;

	@Column(name = "bestbefore")
	private LocalDate bestBefore;

	@Column(name = "matterstoprint")
	private String mattersToPrint;

	@Column(name = "orderedby")
	private Long orderedBy;

	@Column(name = "orderbranch")
	private Long orderBranch;

	@Column(name = "mrnstatus")
	private String mrnStatus;

	@Column(name = "completeddate")
	private Date completedDate;

	@Column(name = "printedby")
	private String printedBy;

	@Column(name = "specialinstructions")
	private String specialInstructions;

	@Column(name = "cancelledby")
	private Long cancelledBy;

	@Column(name = "ip")
	private String ip;

	@Column(name = "material_request_id")
	private Long mrnNo;

	public SalesProductFilmOrder(SalesProductFilmOrderDto dto) {
		this.id = dto.getId();
		if (dto.getFilmId() != null) {
			SalesProductFilm film = new SalesProductFilm();
			film.setId(dto.getFilmId());
			this.filmId = film;
		}

		this.location = dto.getLocation();
		this.subLocationId = dto.getSubLocationId();
		this.lot = dto.getLot();
		this.mrp = dto.getMrp();
		this.packedOn = dto.getPackedOn();
		this.orderQty = dto.getOrderQty();
		this.orderQtyKg = dto.getOrderQtyKg();
		this.printedQty = dto.getCompletedQty();
		this.stockInPouch = dto.getStockInPouch();
		this.balance = dto.getPendingQty();
		this.orderedBy = dto.getOrderedBy();
		this.orderBranch = dto.getOrderBranch();
		this.mattersToPrint = dto.getMattersToPrint();
		this.specialInstructions = dto.getSpecialInstructions();
		this.completedDate = dto.getCompletedDate();
		this.printedBy = dto.getPrintedBy();
		this.cancelledBy = dto.getCancelledBy();
		this.ip = dto.getIp();
		this.mrnStatus = dto.getMrnStatus();
		this.perMlPrice = dto.getPerMlPrice();
	}

}

package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.VoucherItemsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "vouitems")
public class VoucherItems {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	    @Column
	    private Long formid;
	    @Column
	    private Long vouid;
	    @Column
	    private String vouno;
	   
		@Column
	    private Long cid;
	    @Column
	    private Long refid;
	    @Column
	    private String reftype;
	    @Column
	    private String rtype;
	    @Column
	    private String contype;
	    @Column
	    private Long asscount;
	    @Column
	    private Double rcost;
	    @Column
	    private Double qty;
	    @Column
	    private Double ltotal;
	    @Column
	    private Long expid;
	    @Column
	    private Long branch;
	    @Column
	    private Long dept;
	    @Column
	    private String status;
	    @Column
	    private String vtype;
	    @Column
	    private Long vrefid;
	    @Column
	    private String secExpName;
	    
	    public VoucherItems(VoucherItemsDto voucherItemsDto) {
			
			this.id =voucherItemsDto.getId();		
			this.refid = voucherItemsDto.getRefid();
			this.rtype = voucherItemsDto.getRtype();
			this.contype = voucherItemsDto.getContype();
			this.asscount = voucherItemsDto.getAsscount();
			this.rcost = voucherItemsDto.getRcost();
			this.qty = voucherItemsDto.getQty();
			this.ltotal = voucherItemsDto.getLtotal();
			this.expid = voucherItemsDto.getExpid();
			this.branch = voucherItemsDto.getBranch();
			this.dept = voucherItemsDto.getDept();
			this.status = voucherItemsDto.getStatus();
			this.vtype = voucherItemsDto.getVtype();
			this.vrefid = voucherItemsDto.getVrefid();
		}
	


}

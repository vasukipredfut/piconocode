package com.aria.dto;

import java.util.List;

import jakarta.persistence.Column;

import org.apache.commons.text.StringEscapeUtils;

import com.aria.model.VoucherItems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VoucherItemsDto {
	
	private Long id;
   
    private Long formid;
   
    private Long vouid;
    
    private String vouno;
   
    private Long cid;
    
    private Long refid;

    private String secondaryExpenseName;

    private String reftype;
    
    private String rtype;
   
    private String contype;
    
    private Long asscount;
   
    private Double rcost;
   
    private Double qty;
   
    private Double ltotal;
   
    private Long expid;
   
    private Long branch;
   
    private Long dept;
    
    private String status;
   
    private String vtype;
    
    private Long vrefid;
   
    List<VoucherSharedMemberDetailsDto> sharedDetails; 

	public VoucherItemsDto(VoucherItems voucherItems) {
		this.id = voucherItems.getId();
		this.formid = voucherItems.getFormid();
		this.vouid = voucherItems.getVouid();
		this.vouno = voucherItems.getVouno();
		this.cid = voucherItems.getCid();
		this.refid = voucherItems.getRefid();
		this.reftype = voucherItems.getReftype();
		this.rtype = voucherItems.getRtype();
		this.contype = voucherItems.getContype();
		this.asscount = voucherItems.getAsscount();
		this.rcost = voucherItems.getRcost();
		this.qty = voucherItems.getQty();
		this.ltotal = voucherItems.getLtotal();
		this.expid = voucherItems.getExpid();
		this.branch = voucherItems.getBranch();
		this.dept = voucherItems.getDept();
		this.status = voucherItems.getStatus();
		this.vtype = voucherItems.getVtype();
		this.vrefid = voucherItems.getVrefid();
		this.secondaryExpenseName=StringEscapeUtils.unescapeHtml4(voucherItems.getSecExpName());
	}
    

}

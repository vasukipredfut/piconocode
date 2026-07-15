package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;


@NoArgsConstructor
@Getter
@Setter
public class PhysicalStockDto {

    private Long id;

    private Long refid;

    private String name;
    private String goDownName;
    private String dispPlaceName;
    private String binid;

    private Long binname;

    private Long gid;

    private Long bid;

    private Long  did;

    private Double qty;

    private Double lot;
   
    private String dispunit;
   
    private Double physicalStkQty;
    private Double actualStkQty;
    private Date adate;
    private Double diff;
    private String stkStatus;
    private String  stkAction;
    private Long aby;
    private String abyName;

    private Date aon;
    private String aonDate;

    private Long status;

    private Boolean check;

    public PhysicalStockDto(PhysicalStockList physicalStockList) {
        this.id = physicalStockList.getId();
        this.refid = physicalStockList.getRefid();
        this.name = physicalStockList.getProductName();
        this.binid = physicalStockList.getBinid();
        this.binname = physicalStockList.getBinname();
        if(physicalStockList.getGoDownName() != null) {
            this.goDownName = physicalStockList.getGoDownName();
            this.dispPlaceName = physicalStockList.getGoDownName() + " " + physicalStockList.getBinid();
        }
        this.gid = physicalStockList.getGid();
        this.bid = physicalStockList.getBid();
        this.did = physicalStockList.getDid();
        this.qty = physicalStockList.getQty();
        this.lot = physicalStockList.getLot();
        this.actualStkQty = physicalStockList.getQty();
        this.physicalStkQty = physicalStockList.getPhysicalStkQty();
        this.adate = physicalStockList.getAdate();
        this.check = physicalStockList.isCheck();
    }

    public PhysicalStockDto(FilterPhysicalStockList physicalStockList) {
        DateUtil dateUtil = new DateUtil();
        this.id = physicalStockList.getId();
        this.refid = physicalStockList.getRefid();
        this.name = physicalStockList.getProductName();
        this.binid = physicalStockList.getBinid();
        this.binname = physicalStockList.getBinname();
        if(physicalStockList.getGoDownName() != null) {
            this.goDownName = physicalStockList.getGoDownName();
            this.dispPlaceName = physicalStockList.getGoDownName() + " " + physicalStockList.getBinid();
        }
        this.gid = physicalStockList.getGid();
        this.bid = physicalStockList.getBid();
        this.did = physicalStockList.getDid();
        this.qty = physicalStockList.getQty();
        this.lot = physicalStockList.getLot();
        this.actualStkQty = physicalStockList.getQty();
        this.physicalStkQty = physicalStockList.getPhysicalStkQty();
        this.adate = physicalStockList.getAdate();
        this.aby = physicalStockList.getAby();
        this.aonDate = dateUtil.getDateTOStringWk(physicalStockList.getAdate());
    }
}

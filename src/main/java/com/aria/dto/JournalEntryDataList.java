package com.aria.dto;

import java.util.Date;

public interface JournalEntryDataList {

    Long getId();

    String getTrntype();

    String getTrnno();

    Date getTrndate();

    Long getPshiftid();

    Long getSjid();

    Long getWcid();

    String getWctable();

    Long getLineid();

    Long getPriid();
    Double getPriqty();
    Double getPrilot();
    Long getProid();

    String getBinid();

    Long getBinname();
    Long getBatchid();

    String getBatchno();
    String getReduce_batchno();

    String getType();
    String getCtype();
    Double getCper();
    Double getStkqty();
    Double getSstkqty();
    Double getLot();
    Double getAvgcost();
    Double getCost();
    String getAtrnno();
    Date getAtrndate();
    Long getAtrnid();
    String getUsecid();
    String getSflag();
    String getAstatus();
    Long getAby();
    String getAon();
    Long getEby();
    String getCasgrp();
    Long getCasweekid();
    Long getGodown();
    String getSrce();
    Long getDid();
    Long getBid();
    Long getVid();
    Long getPoid();
    Long getBillid();
    Date getBdt();
    Long getRowid();
    Long getInstype();
    Long getExid();
    String getTasum();
    Double getTastk();
    String getBptype();
    String getBomTakeType();
    Date getAdate();
    Double getTotal_input_qty();
    Double getTotal_output_qty();
}

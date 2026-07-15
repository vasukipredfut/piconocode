package com.aria.dto;

import java.util.Date;

public interface ViewAccountLedgerList {


     Long getId();
    
     Long getTrnType();

     Long getSerialNo();

     Date getDate();
     Date getRefDate();

     Long getPartyId();

     String getNarration();

     String getRefNo();

     Double getAmount();

}

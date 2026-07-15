package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

public interface SpecificationDetails {

    Long getId();
    
    String getName();
    
    Long getRefid();
    
    String getRefname();
    
    Long getSpectype();
    
    Long getBranch();
    
    Long getDepartment();
    
    Long getOrg();
    
    Date getCreatedDate();
    
    String getCreatedBy();
    String getStandingOrder();
    String getStandingOrderAuth();


}

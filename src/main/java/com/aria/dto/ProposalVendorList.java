package com.aria.dto;

import java.util.Date;

public interface ProposalVendorList {

     Long getVendorId();
     Long getProposalId();

     String getVendorName();
     String getPoValidityRequest();
     Date getExpiryDate();
}

package com.aria.model;

import java.time.LocalDateTime;

public interface RatehistoryGrouping {

	String getPartyName();

	Long getPartyId();

	Long getProductId();

	String getProductName();

	LocalDateTime getcreatedDate();

	Long getLot();

	Long getIncDec();

	Double getAmount();

	Double getOldAmount();
}

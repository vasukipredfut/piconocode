package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinNoWithLockType {


    public Long binNo;

    public String lockType;


    public BinNoWithLockType(BinNoWithLockTypeList binNoWithLockTypeList) {
        this.binNo = binNoWithLockTypeList.getBinNo();
        this.lockType = binNoWithLockTypeList.getLockType();
    }
}

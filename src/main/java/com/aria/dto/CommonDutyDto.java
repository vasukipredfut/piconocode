package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonDutyDto<T> {

    private Long id;
    private String name;
    private Long wcid;
    private String wctable;
    private String workCenterName;

    public CommonDutyDto(Object id, Object name, Object wcid , Object wctable) {
        String a = String.valueOf(id);
        Long b = Long.parseLong(a);
        String aa = String.valueOf(wcid);
        Long bb = Long.parseLong(aa);
        this.id = b;
        this.name = name != null ? name.toString() : null;
        this.wctable = wctable != null ? wctable.toString() : null;
        this.wcid = bb;
    }
}

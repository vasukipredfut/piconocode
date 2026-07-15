package com.aria.dto;

import lombok.Getter;
import org.apache.commons.text.StringEscapeUtils;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JournalNameListDto {


    private Long id;
    private String name;
    private Long deptId;
    private Long productId;
    private String deptName;
    private Long branchId;
    private String branchName;


    public JournalNameListDto(JournalNameDto journalList) {
        this.id = journalList.getId();
        this.name = StringEscapeUtils.unescapeHtml4(journalList.getName());
        this.deptId=journalList.getDeptId();
        this.productId=journalList.getProductId();
        this.deptName = journalList.getDeptName();
        this.branchId = journalList.getBranchId();
        this.branchName = journalList.getBranchName();
       
    }
}
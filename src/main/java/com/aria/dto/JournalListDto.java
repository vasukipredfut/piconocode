package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.text.StringEscapeUtils;

@Getter
@Setter
@NoArgsConstructor
public class JournalListDto {


    private Long id;
    private String name;

    public JournalListDto(JournalList journalList) {
        this.id = journalList.getId();
        this.name = StringEscapeUtils.unescapeHtml4(journalList.getName());
    }
}

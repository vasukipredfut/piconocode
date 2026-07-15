package com.aria.model;


import com.aria.dto.BlendingAdviceDto;
import com.aria.utils.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "blending_advice")
public class BlendingAdvice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "journal_id")
    private Long journalId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "from_date")
    @Temporal(TemporalType.DATE)
    private Date fromDate;

    @Column(name = "to_date")
    @Temporal(TemporalType.DATE)
    private Date toDate;

    @Column
    private String status;

    @Column
    private String blending_detail_type;

    @Column
    private Double per_Kalasal_Qty;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;

    @Column
    private String previous_cas;

    public BlendingAdvice(BlendingAdviceDto blendingAdvice) {
        DateUtil dateUtil = new DateUtil();
        this.id = blendingAdvice.getId();
        this.journalId = blendingAdvice.getJournalId();
        this.productId = blendingAdvice.getProductId();
        this.blending_detail_type = "P";
        if (blendingAdvice.getFromDate() != null) {
            this.fromDate = dateUtil.getStringTODateWk(blendingAdvice.getFromDate());
        }
        if (blendingAdvice.getToDate() != null) {
            this.toDate = dateUtil.getStringTODateWk(blendingAdvice.getToDate());
        }
        this.created_date = blendingAdvice.getCreated_date();
        this.updated_date = blendingAdvice.getUpdated_date();
        this.per_Kalasal_Qty = blendingAdvice.getPerKalasalQty();
        this.previous_cas = blendingAdvice.getPreviousCas();
    }
}

package com.aria.dto;


import com.aria.model.BlendingAdviceDetails;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class BlendingAdviceDetailsDto {

    private Long id;

    private Long blendingAdviceId;
    private String isoNo;

    private Long journalId;

    private Long branchId;

    public Long productId;

    private Double totalNoofKalasal;

    private Double totalNoofKalasalBag;

    private Double completedNoOfKalasal;

    private String casTransaction;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    private String kalasalWeek;

    private Long primaryProductId;

    private String primaryProductName;

    private List<BlendingAdviceStockDetailsDto> stockDetails;

    private List<BlendingAdvicePreProcessDto> preProcesses;

    private String status;

    public BlendingAdviceDetailsDto(BlendingAdviceDetails blendingAdviceDetails) {
        this.id = blendingAdviceDetails.getId();
        this.blendingAdviceId = blendingAdviceDetails.getBlendingAdviceId();
        this.journalId = blendingAdviceDetails.getJournalId();
        this.branchId = blendingAdviceDetails.getBranchId();
        this.productId = blendingAdviceDetails.getProductId();
        this.totalNoofKalasal = blendingAdviceDetails.getTotalNoOfKalasal();
        this.totalNoofKalasalBag = blendingAdviceDetails.getTotalNoOfKalasalBag();
        this.created_date = blendingAdviceDetails.getCreated_date();
        this.updated_date = blendingAdviceDetails.getUpdated_date();
        this.casTransaction = blendingAdviceDetails.getCasTransaction();
        this.status = blendingAdviceDetails.getStatus();


        this.preProcesses = blendingAdviceDetails.getPreProcesses() != null
                ? blendingAdviceDetails.getPreProcesses().stream().map(field -> new BlendingAdvicePreProcessDto(field)).collect(Collectors.toList()) : null;

    }
}

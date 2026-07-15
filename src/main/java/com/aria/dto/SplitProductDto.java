package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Setter
@Getter
@NoArgsConstructor
public class SplitProductDto {

    private Long id;

    private Long productId;

    private double productQty;

    private double selectedQty;

    private String productName;

    private String considerUs;

    private Long scrapBin;

    private double scrapQty;

    private Long stockBin;

    private double stockQty;

    private double lot;

    private String  bsk_batch_no;

    private String  palletNumber;

    private Long  bsk_batch_id;



}

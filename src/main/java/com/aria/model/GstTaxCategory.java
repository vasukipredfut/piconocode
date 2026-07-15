package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.GstTaxCategoryDTO;

import com.aria.dto.HsnMasterDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "gst_tax_category")
public class GstTaxCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "hsn_Code")
    private String hsnCode;

    @Column(name = "item_Category")
    private String itemCategory;

    @Column(name = "cgst")
    private Double cgst;

    @Column(name = "sgst")
    private Double sgst;

    @Column(name = "igst")
    private Double igst;

    @Column(name = "created_Time_Stamp")
    private LocalDateTime createdTimestamp;

    @Column(name = "emaster_id")
    private Long emasterId;

    @Column(name = "category")
    private String category;


    @Column(name = "items_values")
    private String itemListMap;

    public GstTaxCategory(GstTaxCategoryDTO gstTaxCategory) {
        this.id = gstTaxCategory.getId();
        this.hsnCode = gstTaxCategory.getHsnCode();
        this.itemCategory = gstTaxCategory.getItemCategory();
        this.cgst = gstTaxCategory.getCgst();
        this.sgst = gstTaxCategory.getSgst();
        this.igst = gstTaxCategory.getIgst();
        this.createdTimestamp = gstTaxCategory.getCreatedTimestamp();
        this.emasterId = gstTaxCategory.getEmasterId();
        this.category = gstTaxCategory.getCategory();

    }

    public GstTaxCategory(HsnMasterDto gstTaxCategory) {
        this.id = gstTaxCategory.getId();
        this.hsnCode = gstTaxCategory.getHsnCode();
        this.itemCategory = gstTaxCategory.getItemCategory();
        this.cgst = gstTaxCategory.getCgst();
        this.sgst = gstTaxCategory.getSgst();
        this.igst = gstTaxCategory.getIgst();
        this.category = gstTaxCategory.getCategory();

        ObjectMapper mapper = new ObjectMapper();
        try {
            this.itemListMap = mapper.writeValueAsString(gstTaxCategory.getMapItems());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public GstTaxCategory(GstTaxCategory gstTaxCategory) {
        this.hsnCode = gstTaxCategory.getHsnCode();
        this.itemCategory = gstTaxCategory.getItemCategory();
        this.cgst = gstTaxCategory.getCgst();
        this.sgst = gstTaxCategory.getSgst();
        this.igst = gstTaxCategory.getIgst();
        this.createdTimestamp = gstTaxCategory.getCreatedTimestamp();
        this.emasterId = gstTaxCategory.getEmasterId();
        this.category = gstTaxCategory.getCategory();

    }
}

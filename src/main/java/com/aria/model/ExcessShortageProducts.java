
package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;

import com.aria.dto.ExcessShortageProductsDto;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "excess_shortage_products")
public class ExcessShortageProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "excess_Storage_Id")
    private Long excessStorageId;

    @OneToOne
    @JoinColumn(name = "product_master_id", referencedColumnName = "id")
    public ProductMaster product;

    @Column(name = "product_Code")
    private String productCode;

    @Column(name = "product_Type")
    private String productType;

    @Column
    private String bin;

    @Column(name = "batch_No")
    private String batchNo;

    @Column
    private String lot;

    @Column(name = "trn_Type")
    private String trnType;

    @Column
    private String type;

    @Column
    private String quantity ; // bill total quantity

    @Column(name = "bin_Quantity")
    private String binQuantity ;

    @Column(name = "is_Select", columnDefinition = "char(1) default 'N'")
    private String isSelect;

    @Column(name = "excess_Or_Shortage")
    private String excessOrShortage; // excess or shortage quantity

    @OneToMany(mappedBy = "excessProductId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ExcessStoredBinDetails> binDetails;

    public ExcessShortageProducts(ExcessShortageProductsDto excessShortageProducts) {
        this.id = excessShortageProducts.getId();
        this.excessStorageId = excessShortageProducts.getExcessStorageId();
        if (excessShortageProducts.getProductId() != null) {
            ProductMaster productMaster = new ProductMaster();
            productMaster.setId(excessShortageProducts.getProductId());
            this.product = productMaster;
        }
        this.productCode = excessShortageProducts.getProductCode();
        this.productType = excessShortageProducts.getProductType();
        this.lot = excessShortageProducts.getLot();
        this.bin = excessShortageProducts.getBin();
        this.batchNo = excessShortageProducts.getBatchNo();
        this.trnType = excessShortageProducts.getTrnType();
        this.type = excessShortageProducts.getType();
        this.quantity  = excessShortageProducts.getQuantity();
        this.binQuantity  = excessShortageProducts.getBinQuantity();
        this.isSelect  = "N";
        this.excessOrShortage = excessShortageProducts.getExcessOrShortage();
    }
}

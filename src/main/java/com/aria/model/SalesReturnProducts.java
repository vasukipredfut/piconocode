package com.aria.model;

import com.aria.dto.SalesReturnProductsDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sales_return_products")
public class SalesReturnProducts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_id")
    private Long returnId;

    @OneToOne
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    public ProductMaster product;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_type")
    private String productType;

    @Column
    private Double lot;

    @Column
    private Double return_quantity;

    @OneToMany(mappedBy = "returnProductId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<SalesReturnBinDetails> binDetails;

    @Column
    private String batch_No;

    @Column
    private Long batch_Id;

    public SalesReturnProducts(SalesReturnProductsDto salesReturnProduct) {
        this.id = salesReturnProduct.getId();
        this.returnId = salesReturnProduct.getId();
        if (salesReturnProduct.getProductId() != null) {
            ProductMaster productMaster = new ProductMaster();
            productMaster.setId(salesReturnProduct.getProductId());
            this.product = productMaster;
        }

        this.productCode = salesReturnProduct.getProductCode();
        this.productType = salesReturnProduct.getProductType();
        this.lot = salesReturnProduct.getLot();
        this.return_quantity = salesReturnProduct.getReturn_quantity();

        this.batch_No = salesReturnProduct.getBatchNo();
        this.batch_Id = salesReturnProduct.getBatchId();
    }
}

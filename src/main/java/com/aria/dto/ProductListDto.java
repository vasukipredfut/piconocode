package com.aria.dto;

import com.aria.model.ProductList;
import com.aria.service.FileStorageService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
public class ProductListDto {


    private Long id;

    private String productName;

    private String productType;

    private String picture;

    private String pictureName;

    private String search;

    private String recycle;

    private String taskCategory;
    
//    private Long product_category_ref_id;

    public ProductListDto(ProductList productList, FileStorageService fileStorageService) {
        this.id = productList.getId();
        this.productName = productList.getProductName();
        this.productType = productList.getProductType();
        this.pictureName = productList.getPictureName();
        try {
            if(productList.getPicture()!=null)
            this.picture = fileStorageService.downloadFileAsBase64(productList.getPicture());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.search = productList.getSearch();
        this.recycle = productList.getRecycle();
        this.taskCategory = productList.getTaskCategory();
    }

    public ProductListDto(ProductList productList) {
        this.id = productList.getId();
        this.productName = productList.getProductName();
        this.productType = productList.getProductType();
        this.search = productList.getSearch();
        this.recycle = productList.getRecycle();
        this.taskCategory = productList.getTaskCategory();
    }
}

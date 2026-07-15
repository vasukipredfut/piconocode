package com.aria.model;

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
@Table(name = "material_flow_send_external_items")
public class SendExternalItemsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "material_flow_send_external_items_id")
    private Long materialFlowSendExternalItemsId;

    @Column(name = "send_External_Ref_Id")
    private Long sendExternalRefId;

    @Column(name = "send_External_Item_Name")
    private String sendExternalItemName;

    @Column(name = "send_External_Item_Id")
    private Long sendExternalItemId;

    @Column(name = "send_External_Item_Quantity")
    private Double sendExternalItemQuantity;

    @Column(name = "send_External_Item_Cost")
    private Double sendExternalItemCost;

    @Column(name = "send_External_Item_Unit")
    private String sendExternalItemUnit;

    @Column(name = "send_External_Item_Lot")
    private Double sendExternalItemLot;

    @Column(name = "send_External_Item_Hsncode")
    private String sendExternalItemHsncode;

    @Column(name = "packing_Type")
    private String packingType;

    @Column(name = "packing_Quantity")
    private Long packingQuantity;

    @Column(name = "material_return")
    private String materialReturn;
}

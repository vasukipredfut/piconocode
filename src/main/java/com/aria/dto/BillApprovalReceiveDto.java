package com.aria.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.BillReceive;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class BillApprovalReceiveDto {

    private Long receiverId;

    private String billNumber;

    private String status;


    
}
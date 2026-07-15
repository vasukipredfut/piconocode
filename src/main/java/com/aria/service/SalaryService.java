package com.aria.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.aria.dao.*;
import com.aria.dto.*;
import com.aria.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aria.utils.DateUtil;
import com.aria.utils.TransactionType;

@Service
public class SalaryService {



    @Autowired
    MaterialIssueRepository materialIssueRepository;

    @Autowired
    OtherBranchReceiptRepository otherBranchReceiptRepository;

    @Autowired
    BinstockAuditLogRepository binstockAuditLogRepository;

    @Autowired
    WcShiftsRepository wcShiftsRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    DateUtil dateUtil;



    @Autowired
    CwagesRepository cwagesRepository;

    @Autowired
    AssignDutyRepository assignDutyRepository;













}

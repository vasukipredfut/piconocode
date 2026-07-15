package com.aria.dto;

import com.aria.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViewAccountLedgerDto {

    private Long id;

    private Long serialNo;

    private String voucher;

    private String date;

    private String refDate;

    private Long partyId;

    private Long trnType;

    private String narration;

    private String refNo;

    private double credit;

    private double debit;

    private double balance;

    private String balanceVal;


    public ViewAccountLedgerDto(ViewAccountLedgerList viewAccountsLedger) {
        DateUtil dateUtil = new DateUtil();
        this.id = viewAccountsLedger.getId();
        this.serialNo = viewAccountsLedger.getSerialNo();

        if (viewAccountsLedger.getDate() != null) {
            this.date = dateUtil.getDateTOStringWk(viewAccountsLedger.getDate());
        }

        if (viewAccountsLedger.getRefDate() != null) {
            this.refDate = dateUtil.getDateTOStringWk(viewAccountsLedger.getRefDate());
        }else {
            this.refDate = date ;
        }

        this.trnType = viewAccountsLedger.getTrnType();
        this.partyId = viewAccountsLedger.getPartyId();

        if (viewAccountsLedger.getTrnType() == 1) {
            this.voucher = "Purchase";
            this.credit = viewAccountsLedger.getAmount();
        } else if (viewAccountsLedger.getTrnType() == 2) {
            this.voucher = "E-Transfer";
            this.debit = viewAccountsLedger.getAmount();
        } else if (viewAccountsLedger.getTrnType() == 3) {
            this.voucher = "Credit Note";
            this.debit = viewAccountsLedger.getAmount();
        } else if (viewAccountsLedger.getTrnType() == 4) {
            this.voucher = "Advance Payment";
            this.debit = viewAccountsLedger.getAmount();
        } else if (viewAccountsLedger.getTrnType() == 5) {
            this.voucher = "Debit Note";
            this.credit = viewAccountsLedger.getAmount();
        }

        this.narration = viewAccountsLedger.getNarration();
        this.refNo = viewAccountsLedger.getRefNo();


    }
}

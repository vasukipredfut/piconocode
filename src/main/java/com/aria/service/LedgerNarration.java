package com.aria.service;

public enum LedgerNarration {
    CLOSING_STOCK("CLOSING STOCK"),
    OPENING_STOCK("OPENING STOCK");

    private final String text;

    LedgerNarration(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

package com.aria.service;

public enum TrnType {
    REVERSAL("R");

    private final String code;

    TrnType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}


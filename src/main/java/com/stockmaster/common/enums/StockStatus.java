package com.stockmaster.common.enums;

public enum StockStatus {
    ACTIVE("在售"),
    INACTIVE("停售"),
    OUT_OF_STOCK("缺货");

    private final String description;

    StockStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

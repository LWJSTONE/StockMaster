package com.stockmaster.common.enums;

public enum OrderStatus {
    DRAFT("草稿"),
    PENDING("待审核"),
    APPROVED("已审核"),
    REJECTED("已拒绝"),
    CANCELLED("已取消"),
    COMPLETED("已完成");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

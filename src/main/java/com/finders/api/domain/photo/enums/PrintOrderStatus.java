package com.finders.api.domain.photo.enums;

public enum PrintOrderStatus {
    PENDING,
    CONFIRMED,
    PRINTING,
    READY,  //직접 수령시 픽업 가능 상태
    SHIPPED, //배송일 경우 배송 중
    COMPLETED //완료 상태
}

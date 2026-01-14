package com.finders.api.domain.photo.repository.projection;

public interface ScanPreviewProjection {
    Long getOrderId();
    String getImageKey();
    Integer getDisplayOrder();
}

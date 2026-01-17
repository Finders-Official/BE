package com.finders.api.domain.photo.enums.print;

public enum FrameType {

    WHITE_FRAME(0),
    NO_FRAME(0);

    private final int extraPrice;

    FrameType(int extraPrice) {
        this.extraPrice = extraPrice;
    }

    public int extraPrice() {
        return extraPrice;
    }
}

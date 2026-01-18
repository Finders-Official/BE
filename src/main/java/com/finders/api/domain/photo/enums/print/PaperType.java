package com.finders.api.domain.photo.enums.print;

public enum PaperType {
    ECO_GLOSSY_260(0),
    ECO_LUSTER_255(50),
    EPSON_SEMIGLOSSY_250(50);

    private final int extra;

    PaperType(int extra) {
        this.extra = extra;
    }

    public int extraPrice() {
        return extra;
    }
}


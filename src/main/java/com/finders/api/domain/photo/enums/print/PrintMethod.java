package com.finders.api.domain.photo.enums.print;

public enum PrintMethod {
    INKJET(1000),
    CPRINT(0);

    private final int extra;

    PrintMethod(int extra) {
        this.extra = extra;
    }

    public int extraPrice() {
        return extra;
    }
}

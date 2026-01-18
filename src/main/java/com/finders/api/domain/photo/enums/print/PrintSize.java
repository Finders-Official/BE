package com.finders.api.domain.photo.enums.print;

public enum PrintSize {
    SIZE_5x7(1400),
    SIZE_6x8(2600),
    SIZE_8x10(4400),
    SIZE_8x12(6300),
    A4(6500),
    SIZE_10x15(8000),
    SIZE_11x14(8400);

    private final int basePrice;

    PrintSize(int basePrice) {
        this.basePrice = basePrice;
    }

    public int basePrice() {
        return basePrice;
    }
}


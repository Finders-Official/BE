package com.finders.api.domain.photo.enums.print;

public enum FilmType {
    SLIDE {
        @Override
        public int extra(int basePrice) {
            return 0;
        }
    },
    COLOR_NEG {
        @Override
        public int extra(int basePrice) {
            return (basePrice * 10 / 1000) * 100; // 0.1배 + 백원단위 버림
        }
    },
    BLACK_WHITE {
        @Override
        public int extra(int basePrice) {
            return (basePrice * 10 / 1000) * 100;
        }
    };

    public abstract int extra(int basePrice);
}


package com.finders.api.domain.reservation.calculator;

public final class EstimatedTimeCalculator {

    private EstimatedTimeCalculator() {}

    /**
     * totalMinutes = avgWorkTime + ceil(waitingRollCount / N) * m
     */
    public static int totalMinutes(
            int avgWorkTime,
            int waitingRollCount,
            int N,
            int m
    ) {
        if (avgWorkTime < 0) avgWorkTime = 0;
        if (waitingRollCount <= 0) return avgWorkTime;

        // N이 0이면 장애 터지니 최소 방어 (운영 안전)
        if (N <= 0) N = 1;
        if (m < 0) m = 0;

        int loadBlocks = (int) Math.ceil((double) waitingRollCount / N);
        return avgWorkTime + (loadBlocks * m);
    }
}


package com.hlodving.mytestgold

//Список всех этапов второго прогресс бара

enum class BonusStage(val number: Int, val max: Int, val bonusTimeMillis: Long) {
    STAGE_1(1, 1000, 10 * 60_000L),
    STAGE_2(2, 1500, 10 * 60_000L),
    STAGE_3(3, 2000, 10 * 60_000L),
    STAGE_4(4, 2500, 10 * 60_000L),
    STAGE_5(5, 3000, 10 * 60_000L),
    STAGE_6(6, 3500, 5 * 60_000L),
    STAGE_7(7, 4000, 7 * 60_000L),
    STAGE_8(8, 4500, 8 * 60_000L),
    STAGE_9(9, 5000, 9 * 60_000L),
    STAGE_10(10, 6000, 10 * 60_000L),
    STAGE_11(11, 7000, 11 * 60_000L),
    STAGE_12(12, 8000, 12 * 60_000L),
    STAGE_13(13, 9000, 13 * 60_000L),
    STAGE_14(14, 10000, 14 * 60_000L),
    STAGE_15(15, 11000, 15 * 60_000L),
    STAGE_16(16, 12000, 16 * 60_000L),
    STAGE_17(17, 13000, 17 * 60_000L),
    STAGE_18(18, 14000, 18 * 60_000L),
    STAGE_19(19, 15000, 19 * 60_000L),
    STAGE_20(20, 17000, 20 * 60_000L),
    STAGE_21(21, 19000, 21 * 60_000L),
    STAGE_22(22, 21000, 22 * 60_000L),
    STAGE_23(23, 23000, 23 * 60_000L),
    STAGE_24(24, 25000, 24 * 60_000L),
    STAGE_25(25, 30000, 25 * 60_000L),
    STAGE_26(26, 35000, 26 * 60_000L),
    STAGE_27(27, 40000, 27 * 60_000L),
    STAGE_28(28, 45000, 28 * 60_000L),
    STAGE_29(29, 50000, 29 * 60_000L),
    STAGE_30(30, 100000, 30 * 60_000L);

    companion object {
        fun fromNumber(number: Int): BonusStage =
            values().find { it.number == number } ?: STAGE_1

        fun nextStage(current: BonusStage): BonusStage =
            fromNumber((current.number + 1).coerceAtMost(30))
    }
}

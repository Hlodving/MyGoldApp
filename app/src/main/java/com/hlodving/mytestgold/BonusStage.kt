package com.hlodving.mytestgold

// список всех этапов второго прогресс-бара


enum class BonusStage(val number: Int, val max: Int) {
    STAGE_1(1, 1000),
    STAGE_2(2, 1500),
    STAGE_3(3, 2000),
    STAGE_4(4, 2500),
    STAGE_5(5, 3000),
    STAGE_6(6, 3500),
    STAGE_7(7, 4000),
    STAGE_8(8, 4500),
    STAGE_9(9, 5000),
    STAGE_10(10, 6000),
    STAGE_11(11, 7000),
    STAGE_12(12, 8000),
    STAGE_13(13, 9000),
    STAGE_14(14, 10000),
    STAGE_15(15, 11000),
    STAGE_16(16, 12000),
    STAGE_17(17, 13000),
    STAGE_18(18, 14000),
    STAGE_19(19, 15000),
    STAGE_20(20, 17000),
    STAGE_21(21, 19000),
    STAGE_22(22, 21000),
    STAGE_23(23, 23000),
    STAGE_24(24, 25000),
    STAGE_25(25, 30000),
    STAGE_26(26, 35000),
    STAGE_27(27, 40000),
    STAGE_28(28, 45000),
    STAGE_29(29, 50000),
    STAGE_30(30, 100000);

    companion object {
        fun fromNumber(number: Int): BonusStage =
            values().find { it.number == number } ?: STAGE_1

        fun nextStage(current: BonusStage): BonusStage =
            fromNumber((current.number + 1).coerceAtMost(30))
    }
}

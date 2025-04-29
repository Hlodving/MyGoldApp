    package com.hlodving.mytestgold

    enum class Stage(val number: Int) {
        STAGE_1(1),
        STAGE_2(2),
        STAGE_3(3),
        STAGE_4(4),
        STAGE_5(5),
        STAGE_6(6),
        STAGE_7(7),
        STAGE_8(8),
        STAGE_9(9),
        STAGE_10(10),
        UNKNOWN(0);

        companion object {
            fun fromGold(gold: Int): Stage {
                var accumulated = 0
                for (i in 1..100) {
                    val required = i * 100
                    if (gold < accumulated + required) {
                        return values().find { it.number == i } ?: UNKNOWN
                    }
                    accumulated += required
                }
                return UNKNOWN
            }
        }
    }

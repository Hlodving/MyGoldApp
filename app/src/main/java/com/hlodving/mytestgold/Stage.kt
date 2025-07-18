    package com.hlodving.mytestgold
    // Класс с этапами
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
        STAGE_11(11),
        STAGE_12(12),
        STAGE_13(13),
        STAGE_14(14),
        STAGE_15(15),
        STAGE_16(16),
        STAGE_17(17),
        STAGE_18(18),
        STAGE_19(19),
        STAGE_20(20),
        STAGE_21(21),
        STAGE_22(22),
        STAGE_23(23),
        STAGE_24(24),
        STAGE_25(25),
        STAGE_26(26),
        STAGE_27(27),
        STAGE_28(28),
        STAGE_29(29),
        STAGE_30(30),
        STAGE_31(31),
        STAGE_32(32),
        STAGE_33(33),
        STAGE_34(34),
        STAGE_35(35),
        STAGE_36(36),
        STAGE_37(37),
        STAGE_38(38),
        STAGE_39(39),
        STAGE_40(40),
        STAGE_41(41),
        STAGE_42(42),
        STAGE_43(43),
        STAGE_44(44),
        STAGE_45(45),
        STAGE_46(46),
        STAGE_47(47),
        STAGE_48(48),
        STAGE_49(49),
        STAGE_50(50),
        STAGE_51(51),
        STAGE_52(52),
        STAGE_53(53),
        STAGE_54(54),
        STAGE_55(55),
        STAGE_56(56),
        STAGE_57(57),
        STAGE_58(58),
        STAGE_59(59),
        STAGE_60(60),
        STAGE_61(61),
        STAGE_62(62),
        STAGE_63(63),
        STAGE_64(64),
        STAGE_65(65),
        STAGE_66(66),
        STAGE_67(67),
        STAGE_68(68),
        STAGE_69(69),
        STAGE_70(70),
        STAGE_71(71),
        STAGE_72(72),
        STAGE_73(73),
        STAGE_74(74),
        STAGE_75(75),
        STAGE_76(76),
        STAGE_77(77),
        STAGE_78(78),
        STAGE_79(79),
        STAGE_80(80),
        STAGE_81(81),
        STAGE_82(82),
        STAGE_83(83),
        STAGE_84(84),
        STAGE_85(85),
        STAGE_86(86),
        STAGE_87(87),
        STAGE_88(88),
        STAGE_89(89),
        STAGE_90(90),
        STAGE_91(91),
        STAGE_92(92),
        STAGE_93(93),
        STAGE_94(94),
        STAGE_95(95),
        STAGE_96(96),
        STAGE_97(97),
        STAGE_98(98),
        STAGE_99(99),
        STAGE_100(100),
        STAGE_101(101),
        UNKNOWN(0);

        companion object {
            fun fromGold(gold: Int): Stage {  //Функция которая по колличеству кликов определяет на каком этапе
                //находится пользователь
                var accumulated = 0
                for (i in 1..101) { // Было 1..100
                    val required = i * 100
                    if (gold < accumulated + required) {
                        return values().find { it.number == i } ?: UNKNOWN
                    }
                    accumulated += required
                }

                return UNKNOWN
            }


            //Возвращает информацию о текущем прогрессе в стадии
            fun getStageData(gold: Int): Triple<Int, Int, Stage> {
                var stageNumber = 1
                var requiredGold = 100
                var accumulated = 0

                while (gold >= accumulated + requiredGold) {
                    accumulated += requiredGold
                    stageNumber++
                    requiredGold = stageNumber * 100
                }

                val stageProgress = gold - accumulated
                val stageMax = requiredGold
                val stage = fromGold(gold)

                return Triple(stageProgress, stageMax, stage)
            }
        }


    }

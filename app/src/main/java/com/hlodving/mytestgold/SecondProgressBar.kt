package com.hlodving.mytestgold

import android.content.Context

//Список всех этапов второго прогресс бара

enum class BonusStage(val number: Int, val max: Int, val bonusTimeMillis: Long) {


    STAGE_1(1, 1000,  1 * 24 * 60 * 60_000L),
    STAGE_2(2, 1500, 1 * 24 * 60 * 60_000L),
    STAGE_3(3, 2000, 2 * 24 * 60 * 60_000L),
    STAGE_4(4, 2500, 3 * 24 * 60 * 60_000L),
    STAGE_5(5, 3000, 4 * 24 * 60 * 60_000L),
    STAGE_6(6, 3500, 5 * 24 * 60 * 60_000L),
    STAGE_7(7, 4000, 5 * 24 * 60 * 60_000L),
    STAGE_8(8, 4500, 5 * 24 * 60 * 60_000L),
    STAGE_9(9, 5000, 5 * 24 * 60 * 60_000L),
    STAGE_10(10, 6000, 6 * 24 * 60 * 60_000L),
    STAGE_11(11, 7000, 7 * 24 * 60 * 60_000L),
    STAGE_12(12, 8000, 8 * 24 * 60 * 60_000L),
    STAGE_13(13, 9000, 9 * 24 * 60 * 60_000L),
    STAGE_14(14, 10000, 10 * 24 * 60 * 60_000L),
    STAGE_15(15, 11000, 10 * 24 * 60 * 60_000L),
    STAGE_16(16, 12000, 10 * 24 * 60 * 60_000L),
    STAGE_17(17, 13000, 10 * 24 * 60 * 60_000L),
    STAGE_18(18, 14000, 11 * 24 * 60 * 60_000L),
    STAGE_19(19, 15000, 12 * 24 * 60 * 60_000L),
    STAGE_20(20, 17000, 13 * 24 * 60 * 60_000L),
    STAGE_21(21, 19000, 14 * 24 * 60 * 60_000L),
    STAGE_22(22, 21000, 15 * 24 * 60 * 60_000L),
    STAGE_23(23, 23000, 15 * 24 * 60 * 60_000L),
    STAGE_24(24, 25000, 15 * 24 * 60 * 60_000L),
    STAGE_25(25, 30000, 15 * 24 * 60 * 60_000L),
    STAGE_26(26, 35000, 16 * 24 * 60 * 60_000L),
    STAGE_27(27, 40000, 17 * 24 * 60 * 60_000L),
    STAGE_28(28, 45000, 18 * 24 * 60 * 60_000L),
    STAGE_29(29, 50000, 19 * 24 * 60 * 60_000L),
    STAGE_30(30, 100000, 30 * 24 * 60 * 60_000L);

    companion object {
            fun fromNumber(number: Int): BonusStage =
            values().find { it.number == number } ?: STAGE_1

        fun nextStage(current: BonusStage): BonusStage =
            fromNumber((current.number + 1).coerceAtMost(30))
    }
}

class BonusStageManager(private val context: Context) {
    // Начальное значение номера стадии
    var stageNumber = 1
        private set

    // Основной счетчик второго прогресс бара
    var countSecondProgress = 0
        private set
    //Возвращает текущую стадию на основе stageNumber
    val currentStage: BonusStage
        get() = BonusStage.fromNumber(stageNumber)

    //Возвращает максимум нажатий
    val maxProgress: Int
        get() = currentStage.max

    //Флаг который отмечает, что стадия была заполнена
    var justFilled = false

    //Загружает текущее состояние второго прогресс бара
    fun loadState() {
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        stageNumber = prefs.getInt("bonusStageNumber", 1).coerceIn(1, 30)
        countSecondProgress = prefs.getInt("bonusProgress", 0)
    }

    //Сохраняет текущее состояние второго прогресс бара
    fun saveState() {
        val prefs = context.getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("bonusStageNumber", stageNumber)
            .putInt("bonusProgress", countSecondProgress)
            .apply()
    }

    //Увеличивает и сбрасывает второй прогресс бар
    fun increment(): Boolean {
        countSecondProgress++
        if (countSecondProgress >= maxProgress) {
            countSecondProgress = 0
            stageNumber = (stageNumber + 1).coerceAtMost(30)
            justFilled = true
            saveState()
            return true
        }
        saveState()
        return false
    }

    //Подставляет строку с цитатой в зависимости от фазы
    fun getQuote(): String {
        val quoteId = context.resources.getIdentifier(
            "bonus_stage_$stageNumber", "string", context.packageName
        )
        return if (quoteId != 0) context.getString(quoteId) else ""
    }
}


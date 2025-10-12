package com.hlodving.mytestgold

import android.widget.Toast
import com.hlodving.mytestgold.database.DbManager
import com.hlodving.mytestgold.databinding.ActivityMainBinding

class GameEngine(
    private val activity: MainActivity,
    private val binding: ActivityMainBinding, // Передаем binding для доступа к Lottie-анимациям
    private val dbManager: DbManager,
    private val goldManager: GoldProgressManager,
    private val bonusManager: BonusStageManager,
    private val tapCounter: GlobalTapCounter,
    private val timerManager: CountdownTimerManager,
    private val viewUpdater: MainViewUpdater
) {

    // Как часто сохранять прогресс
    private val SAVE_THRESHOLD = 172

    // Сколько тапов прошло с последнего сохранения
    private var tapsSinceLastSave = 0


    //Логика нажатия на оберег
    fun onHeartTapped() {
        // 1. Проверка, готовы ли данные пользователя (если он вошел в аккаунт)
        if (!activity.userDataReady && activity.mAuth.currentUser != null) {
            Toast.makeText(activity, "Загружаю профиль…", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Увеличиваем общий счетчик тапов
        tapCounter.increment()
        viewUpdater.updateTotalTaps(tapCounter.totalTaps)

        // 4. ДОБАВЛЯЕМ ЛОГИКУ ПАКЕТНОГО СОХРАНЕНИЯ
        tapsSinceLastSave++ // Увеличиваем наш новый счетчик
        if (tapsSinceLastSave >= SAVE_THRESHOLD) {
            saveProgressToFirebase()  // Вызываем сохранение
            tapsSinceLastSave = 0     // Сбрасываем счетчик
        }

        // 4. Если оберег вечный, дальнейшая логика не нужна
        if (timerManager.isOberegForever()) return

        // 5. Логика первого прогресс-бара (золото)
        val (_, _, currentStage) = goldManager.increment()
        val (progress, max, stage) = goldManager.getProgressInfo()
        viewUpdater.updateGoldProgress(progress, max, stage.number)
        GoldWidget.updateAllWidgets(activity, goldManager.count)

        // 6. Логика второго прогресс-бара (бонусный)
        if (bonusManager.increment()) {
            // Если бонусный бар заполнился, добавляем время к таймеру
            val bonusTime = bonusManager.currentStage.bonusTimeMillis
            val newEndTime = timerManager.getRemainingTimeMillis() + System.currentTimeMillis() + bonusTime
            timerManager.saveTimerEndTime(newEndTime)
            timerManager.startTimer()
            ResetScheduler.scheduleResetWorker(activity, newEndTime - System.currentTimeMillis())

            viewUpdater.updateBonusQuote(bonusManager.getQuote())
            viewUpdater.playShineAnimation()

            // Логика "прокачки" первого бара до второй фазы, если он был на первой
            if (goldManager.getStage().number == 1) {
                val (p, m, _) = goldManager.getProgressInfo()
                val needed = m - p
                repeat(needed) { goldManager.increment() }

                val (newP, newM, newS) = goldManager.getProgressInfo()
                viewUpdater.updateGoldProgress(newP, newM, newS.number)
                GoldWidget.updateAllWidgets(activity, goldManager.count)
                goldManager.markStageReached(goldManager.getStage())
            }
        }

        // Обновляем UI второго прогресс-бара при каждом нажатии
        viewUpdater.updateBonusProgress(
            bonusManager.countSecondProgress,
            bonusManager.maxProgress,
            bonusManager.stageNumber
        )

        // 7. Проверяем, не перешли ли мы на новую стадию золотого прогресса
        if (goldManager.isNextStage(currentStage)) {
            if (currentStage.number == 101) { // 101-я фаза — вечный оберег
                timerManager.saveTimerEndTime(Long.MAX_VALUE)
                timerManager.stopTimer()
                viewUpdater.showAmuletForever()
            } else { // Обычный переход — продлеваем таймер
                val now = System.currentTimeMillis()
                val safeRemaining = timerManager.getRemainingTimeMillis()
                val additionalMillis = currentStage.number * 7_200_000L // 2 часа за номер стадии
                val newTime = now + safeRemaining + additionalMillis
                timerManager.saveTimerEndTime(newTime)
                timerManager.startTimer()
                ResetScheduler.scheduleResetWorker(activity, newTime - now)
            }
            goldManager.markStageReached(currentStage)
            viewUpdater.playShineAnimation()
        }

        // 8. Обновляем анимацию нажатия (активна/неактивна)
        val isAmuletActive = timerManager.getTimerState() !is CountdownTimerManager.TimerState.Expired
        viewUpdater.updateTapAnimation(isAmuletActive)

        // 9. Запуск всех декоративных анимаций (звездочки и сердца)
        playDecorativeAnimations()
    }

    private fun playDecorativeAnimations() {
        // Анимация звездочек при каждом нажатии
        when (bonusManager.countSecondProgress % 10) {
            1 -> HeartAnimation.playLottieAnimation(binding.lottie1)
            2 -> HeartAnimation.playLottieAnimation(binding.lottie2)
            3 -> HeartAnimation.playLottieAnimation(binding.lottie5)
            4 -> HeartAnimation.playLottieAnimation(binding.lottie7)
            5 -> HeartAnimation.playLottieAnimation(binding.lottie4)
            6 -> HeartAnimation.playLottieAnimation(binding.lottie8)
            7 -> HeartAnimation.playLottieAnimation(binding.lottie9)
            8 -> HeartAnimation.playLottieAnimation(binding.lottie6)
            9 -> HeartAnimation.playLottieAnimation(binding.lottie3)
            0 -> HeartAnimation.playLottieAnimation(binding.lottie4)
        }

        // Анимация сердец каждые 27 тапов
        if (bonusManager.countSecondProgress > 0 && bonusManager.countSecondProgress % 27 == 0) {
            playHeartAnimationForStage(bonusManager.currentStage.number)
        }
    }

        //Сохраняем прогресс пользователя
    private fun saveProgressToFirebase() {
        // Сохраняем, только если пользователь вошел в аккаунт
        if (activity.mAuth.currentUser != null) {
            dbManager.saveProgress(
                tapCounter.totalTaps,
                bonusManager.countSecondProgress,
                bonusManager.stageNumber
            )
        }
    }

    private fun playHeartAnimationForStage(stageNumber: Int) {
        val Heart = HeartAnimation // Псевдоним для краткости
        val lottieHeart = binding.lottieHeartGold
        val lottieTap = binding.lottieTapGold

        when (stageNumber) {
            1 -> { // Одно сердце
                Heart.playNextHeartAnimation(lottieHeart, 0.01f, 0.027f, 0.045f, 0.025f, 0.04f, 0.06f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            2 -> { // Два сердца
                Heart.playNextHeartAnimation(lottieHeart, 0.083f, 0.1f, 0.115f, 0.095f, 0.112f, 0.132f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            3 -> { // Три сердца
                Heart.playNextHeartAnimation(lottieHeart, 0.135f, 0.151f, 0.17f, 0.148f, 0.165f, 0.185f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            4 -> { // Четыре и три сердца
                Heart.playNextHeartAnimation(lottieHeart, 0.135f, 0.207f, 0.17f, 0.148f, 0.225f, 0.185f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            5 -> { // Четыре сердца
                Heart.playNextHeartAnimation(lottieHeart, 0.19f, 0.207f, 0.226f, 0.205f, 0.225f, 0.24f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            6 -> {  // Пять и четыре сердца
                Heart.playNextHeartAnimation(lottieHeart, 0.243f, 0.207f, 0.280f, 0.258f, 0.225f, 0.295f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            7 -> {  // Пять сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.243f, 0.262f, 0.280f, 0.258f, 0.276f, 0.295f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            8 -> {  // Шесть и пять сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.298f, 0.262f, 0.352f, 0.311f, 0.276f, 0.368f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            9 -> {  // Шесть сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.298f, 0.333f, 0.352f, 0.311f, 0.351f, 0.368f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            10 -> {  // Семь и шесть сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.371f, 0.333f, 0.402f, 0.387f, 0.351f, 0.421f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            11 -> {  // Семь сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.371f, 0.388f, 0.402f, 0.387f, 0.4f, 0.421f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            12 -> {  // Восемь сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.424f, 0.442f, 0.46f, 0.439f, 0.457f, 0.475f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            13 -> {//  Девять сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.478f, 0.496f, 0.514f, 0.493f, 0.511f, 0.529f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            14 -> {//  Десять и девять сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.478f, 0.546f, 0.562f, 0.493f, 0.560f, 0.576f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            15 -> {//  Десять сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.532f, 0.546f, 0.562f, 0.545f, 0.560f, 0.576f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            16 -> {//  Одиннадцать и десять сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.578f, 0.546f, 0.612f, 0.593f, 0.560f, 0.625f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            17 -> {//  Одиннадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.578f, 0.595f, 0.612f, 0.593f, 0.610f, 0.625f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            18 -> {//  Двенадцать и одиннадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.627f, 0.595f, 0.662f, 0.640f, 0.610f, 0.678f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            19 -> {//   Двенадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.627f, 0.643f, 0.662f, 0.640f, 0.660f, 0.678f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            20 -> {//  Тринадцать и двенадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.680f, 0.643f, 0.713f, 0.695f, 0.660f, 0.727f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            21 -> {//  Тринадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.680f, 0.697f, 0.713f, 0.695f, 0.710f, 0.727f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            22 -> {// Четырнадцать и тринадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.730f, 0.697f, 0.764f, 0.745f, 0.710f, 0.780f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            23 -> {//  Четырнадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.730f, 0.748f, 0.764f, 0.745f, 0.762f, 0.780f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            24 -> {//  Пятнадцать и четырнадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.782f, 0.748f, 0.816f, 0.798f, 0.762f, 0.830f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            25 -> {//  Пятнадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.782f, 0.799f, 0.816f, 0.798f, 0.815f, 0.830f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            26 -> {  //  Шестнадцать и пятнадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.832f, 0.799f, 0.865f, 0.845f, 0.815f, 0.880f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            27 -> {  //  Шестнадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.832f, 0.848f, 0.865f, 0.845f, 0.863f, 0.880f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            28 -> {  //  Семнадцать и шестнадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.883f, 0.848f, 0.917f, 0.898f, 0.863f, 0.930f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            29 -> {  // Семнадцать сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.883f, 0.900f, 0.917f, 0.898f, 0.915f, 0.930f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
            30 -> {  //  Много сердец
                Heart.playNextHeartAnimation(lottieHeart, 0.950f, 0.967f, 0.982f, 0.966f, 0.981f, 0.999f)
                Heart.playLottieAnimation(lottieTap, 0.19f, 0.22f)
            }
        }
    }
}
    package com.hlodving.mytestgold

    import HeartAnimation
    import android.appwidget.AppWidgetManager
    import android.content.ComponentName
    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.work.WorkManager


    import com.hlodving.mytestgold.databinding.ActivityMainBinding

    class MainActivity : AppCompatActivity() {

        // Переменная с таймером
        private lateinit var countdownTimerManager: CountdownTimerManager
        //Переменная с вторым прогресс баром
        private lateinit var secondProgressManager: BonusStageManager
        //Переменная с основным счетчиком
        private lateinit var globalTapCounter: GlobalTapCounter
        //Переменная с первым прогресс баром
        private lateinit var goldProgressManager: GoldProgressManager



        private val progressColors = listOf(
            R.drawable.progress_bar_green,
            R.drawable.progress_bar_blue,
            R.drawable.progress_bar_orange,
            R.drawable.progress_bar_purple,
            R.drawable.progress_bar_yellow,
            R.drawable.progress_bar_red
        ) // Список стилей прогресс-бара по фазам





        lateinit var binding: ActivityMainBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            //Инициализация первого прогресс-бара
            goldProgressManager = GoldProgressManager(this, binding, progressColors)
            goldProgressManager.load()
            goldProgressManager.updateUI()
            GoldWidget.updateAllWidgets(this, goldProgressManager.count)

            //Инициализация второго прогресс-бара
            secondProgressManager = BonusStageManager(this)
            secondProgressManager.loadState()

            //Инициализация глобального счётчика нажатий
            globalTapCounter = GlobalTapCounter(this)
            globalTapCounter.load()


            //Инициализация и запуск таймера
            countdownTimerManager = CountdownTimerManager(
                context = this,
                onTick = { formattedTime ->
                    binding.timerText.text = formattedTime
                },
                onFinished = {
                    if (!secondProgressManager.justFilled) {
                        countdownTimerManager.resetHappened = true
                        goldProgressManager.reset()

                        countdownTimerManager.resetTimer()

                        HeartAnimation.applyResetUI(
                            context = this,
                            binding = binding,
                            progressColors = progressColors,
                            countFirstProgress = goldProgressManager.count,
                            secondStageNumber = secondProgressManager.stageNumber
                        )
                    }
                    secondProgressManager.justFilled = false
                },
                onStopped = {
                    // Останавливает анмацию в виджете
                    ResetScheduler.stopWidgetAnimationService(this)
                }
            )



            // Показываем цитату сразу при запуске
            binding.bonusQuoteText.text = secondProgressManager.getQuote()

            //Настройка второго прогресс-бара
            binding.progressBar2.max = secondProgressManager.maxProgress
            binding.progressBar2.progress = secondProgressManager.countSecondProgress
            binding.progressText2.text = "${secondProgressManager.countSecondProgress} / ${secondProgressManager.maxProgress}"
            val colorDrawableId2 = progressColors[(secondProgressManager.stageNumber - 1) % progressColors.size]
            binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this, colorDrawableId2)


            // Загрузка времени таймера
            countdownTimerManager.loadTimerEndTime()

            //Проверка состояния таймера при запуске
            when (val state = countdownTimerManager.getTimerState()) {
                is CountdownTimerManager.TimerState.Forever -> { // Вечный оберег (скрывает прогресс бары)
                    binding.timerText.text = getString(R.string.active_forever_amulet)
                    binding.progressBar.visibility = View.GONE
                    binding.progressText.visibility = View.GONE
                    binding.progressBar2.visibility = View.GONE
                    binding.progressText2.visibility = View.GONE
                }

                is CountdownTimerManager.TimerState.Running -> { //Активный таймер
                    countdownTimerManager.startTimer()
                    ResetScheduler.scheduleResetWorker(this, state.remainingMillis)
                }

                is CountdownTimerManager.TimerState.Expired -> { //Таймер истёк сброс первого прогресс бара
                    binding.timerText.text = getString(R.string.not_active_amulet)
                    HeartAnimation.applyResetUI(
                        context = this,
                        binding = binding,
                        progressColors = progressColors,
                        countFirstProgress = goldProgressManager.count,
                        secondStageNumber = secondProgressManager.stageNumber
                    )
                }
            }

            //Кнопка информации об обереге
            binding.moreInfoButton.setOnClickListener {
                val intent = Intent(this, OberegInfoActivity::class.java)
                startActivity(intent)
            }


            //Тест кнопка
            binding.buttonAdd49.setOnClickListener {
                repeat(49) {
                    binding.lottieHeartGold.performClick()
                }
            }


            val Heart = HeartAnimation

            //Устанавливает активную или не активную анимацию
            val (stageProgress, stageMax, currentStage) = goldProgressManager.getProgressInfo()
            Heart.updateTapAnimationForStage(binding.lottieTapGold, currentStage)


            //Настройка первого прогресс-бара
            binding.progressBar.max = stageMax
            binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
            binding.progressText.text = "$stageProgress / $stageMax"
            val safeStageNum = currentStage.number.coerceIn(1, 101)
            val colorDrawableId = progressColors[(safeStageNum - 1) % progressColors.size]
            binding.progressBar.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId)


            //Запуск анимаций
            binding.apply {
                Heart.animationRestart(lottieHeartGold)
                Heart.animationRestart(lottieViewShineOne)
                Heart.animationRestart(lottieViewShineTwo)
                Heart.animationRestart(lottieTapGold)

                // Оснавная кнопка оберег
                lottieHeartGold.setOnClickListener {
                    globalTapCounter.increment()

                    if (countdownTimerManager.isOberegForever()) return@setOnClickListener


                    val (stageProgress, stageMax, currentStage) = goldProgressManager.increment()
                    GoldWidget.updateAllWidgets(this@MainActivity, goldProgressManager.count)
                    goldProgressManager.updateUI()

                    //Обновление виджета
                    val intent = Intent(this@MainActivity, GoldWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                            AppWidgetManager.getInstance(this@MainActivity).getAppWidgetIds(
                                ComponentName(this@MainActivity, GoldWidget::class.java)
                            ))
                    }

                    if (secondProgressManager.increment()) { // Увеличиваем таймер на время из второго прогресс бара
                        val bonusTime = secondProgressManager.currentStage.bonusTimeMillis
                        val newBonusEndTime = countdownTimerManager.getRemainingTimeMillis() + System.currentTimeMillis() + bonusTime
                        countdownTimerManager.timerEndTime = newBonusEndTime
                        countdownTimerManager.saveTimerEndTime(newBonusEndTime)
                        countdownTimerManager.startTimer()
                        ResetScheduler.scheduleResetWorker(this@MainActivity, newBonusEndTime - System.currentTimeMillis())


                        // Обновляем цитату
                        binding.bonusQuoteText.text = secondProgressManager.getQuote()

                        // Проигрываем shine анимацию
                        Heart.playLottieAnimation(binding.lottieViewShineOne)
                        Heart.playLottieAnimation(binding.lottieViewShineTwo)

                        // Обновляем второй прогресс бар
                        binding.progressBar2.max = secondProgressManager.maxProgress
                        binding.progressBar2.progress = secondProgressManager.countSecondProgress
                        binding.progressText2.text = "${secondProgressManager.countSecondProgress} / ${secondProgressManager.maxProgress}"
                        val colorDrawableId2 = progressColors[(secondProgressManager.stageNumber - 1) % progressColors.size]
                        binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId2)

                    }

                    // Обновляем второй прогресс бар
                    binding.progressBar2.max = secondProgressManager.maxProgress
                    binding.progressBar2.progress = secondProgressManager.countSecondProgress
                    binding.progressText2.text = "${secondProgressManager.countSecondProgress} / ${secondProgressManager.maxProgress}"
                    val colorDrawableId2 = progressColors[(secondProgressManager.stageNumber - 1) % progressColors.size]
                    binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId2)


                    // Отправка обновлённого состояния в виджет
                    sendBroadcast(intent)

                    // Обновление анимации нажатий
                    Heart.updateTapAnimationForStage(binding.lottieTapGold, currentStage)


                    // Действия при заполнении первого прогресс бара
                    if (goldProgressManager.isNextStage(currentStage)) {
                        if (currentStage.number == 101) { //101 фаза вечный оберег
                            countdownTimerManager.timerEndTime = Long.MAX_VALUE
                            countdownTimerManager.saveTimerEndTime(Long.MAX_VALUE)
                            countdownTimerManager.stopTimer() // останавливаем таймер
                            binding.timerText.text = getString(R.string.active_forever_amulet)
                            // Отменяем фоновые задачи по сбросу
                            WorkManager.getInstance(this@MainActivity).cancelUniqueWork("resetGoldWorker")
                            // Скрываем оба прогресс-бара и их текст
                            binding.progressBar.visibility = View.GONE
                            binding.progressText.visibility = View.GONE
                            binding.progressBar2.visibility = View.GONE
                            binding.progressText2.visibility = View.GONE

                        } else { // При переходе на следущую стадию увеличиваем длительность таймера
                            val now = System.currentTimeMillis()
                            val safeRemaining = countdownTimerManager.getRemainingTimeMillis()



                            // Каждая следующая стадия добавляет на 1 час больше
                            //val additionalHours = countdownTimerManager.baseHoursToAdd + (currentStage.number - 2)
                            //val additionalMillis = additionalHours * 60 * 60 * 1000
                            // Это для теста
                            val additionalMillis = 15_000L // 10 секунд


                            // Рассчитываем новое время завершения таймера
                            val newTime = now + safeRemaining + additionalMillis
                            countdownTimerManager.timerEndTime = newTime
                            countdownTimerManager.saveTimerEndTime(newTime)

                            // Перезапускаем таймер и планируем сброс
                            val delayMillis = newTime - System.currentTimeMillis()
                            countdownTimerManager.startTimer()
                            ResetScheduler.scheduleResetWorker(this@MainActivity, delayMillis)

                        }
                        // Фиксируем, что стадия достигнута
                        goldProgressManager.markStageReached(currentStage)

                        // shine-анимации перехода на новую стадию
                        Heart.playLottieAnimation(binding.lottieViewShineOne)
                        Heart.playLottieAnimation(binding.lottieViewShineTwo)
                    }

                    // Обновляем отображение первого прогресс-бара после перехода на стадию
                    binding.progressBar.max = stageMax
                    binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
                    binding.progressText.text = "$stageProgress / $stageMax"
                    val colorDrawableId = progressColors[(currentStage.number - 1) % progressColors.size]
                    binding.progressBar.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId)


                    //Анимация звездочек каждый тап
                    when (secondProgressManager.countSecondProgress % 10) {
                            1 -> Heart.playLottieAnimation(lottie1)
                            2 -> Heart.playLottieAnimation(lottie2)
                            3 -> Heart.playLottieAnimation(lottie5)
                            4 -> Heart.playLottieAnimation(lottie7)
                            5 -> Heart.playLottieAnimation(lottie4)
                            6 -> Heart.playLottieAnimation(lottie8)
                            7 -> Heart.playLottieAnimation(lottie9)
                            8 -> Heart.playLottieAnimation(lottie6)
                            9 -> Heart.playLottieAnimation(lottie3)
                            0 -> Heart.playLottieAnimation(lottie4)
                        }

                    // Каждые 27 тапов на втором прогресс-баре — запускаем спец. анимацию "сердец"
                    // Анимация зависит от текущего бонусного этапа
                    if (secondProgressManager.countSecondProgress % 27 == 0) {

                        // Выполнение следующего действия
                        when (secondProgressManager.currentStage.number) {
                            1 -> { // Одно сердце
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.01f, 0.027f, 0.045f,
                                    0.025f, 0.04f, 0.06f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f, 0.22f)
                            }
                            2 -> { //Два серца
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.083f, 0.1f, 0.115f,
                                    0.095f, 0.112f, 0.132f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f, 0.22f)
                            }
                            3 -> { //Три сердца
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.135f, 0.151f, 0.17f,
                                    0.148f, 0.165f, 0.185f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            4 -> { //Четыре и три сердца
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.135f, 0.207f, 0.17f,
                                    0.148f, 0.225f, 0.185f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }

                            5 -> { //Четыре сердца
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.19f, 0.207f, 0.226f,
                                    0.205f, 0.225f, 0.24f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }

                            6 -> {  //Пять и четыре сердеца
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.243f, 0.207f, 0.280f,
                                    0.258f, 0.225f, 0.295f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }

                            7 -> {  //Пять сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.243f, 0.262f, 0.280f,
                                    0.258f, 0.276f, 0.295f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            8 -> {  // Шесть и пять сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.298f, 0.262f, 0.352f,
                                    0.311f, 0.276f, 0.368f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }

                            9 -> {  // Шесть сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.298f, 0.333f, 0.352f,
                                    0.311f, 0.351f, 0.368f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            10 -> {  // Семь и шесть сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.371f, 0.333f, 0.402f,
                                    0.387f, 0.351f, 0.421f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }

                            11 -> {  // Семь сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.371f, 0.388f, 0.402f,
                                    0.387f, 0.4f, 0.421f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            12 -> {  // Восемь сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.424f, 0.442f, 0.46f,
                                    0.439f, 0.457f, 0.475f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            13 -> {//  Девять сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.478f, 0.496f, 0.514f,
                                    0.493f, 0.511f, 0.529f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            14 -> {//  Десять и девять сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.478f, 0.546f, 0.562f,
                                    0.493f, 0.560f, 0.576f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            15 -> {//  Десять сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.532f, 0.546f, 0.562f,
                                    0.545f, 0.560f, 0.576f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            16 -> {//  Одиннадцать и десять сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.578f, 0.546f, 0.612f,
                                    0.593f, 0.560f, 0.625f
                                )

                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            17 -> {//  Одиннадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.578f, 0.595f, 0.612f,
                                    0.593f, 0.610f, 0.625f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            18 -> {//  Двенадцать и одиннадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.627f, 0.595f, 0.662f,
                                    0.640f, 0.610f, 0.678f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            19 -> {//   Двенадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.627f, 0.643f, 0.662f,
                                    0.640f, 0.660f, 0.678f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            20 -> {//  Тринадцать и двенадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.680f, 0.643f, 0.713f,
                                    0.695f, 0.660f, 0.727f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            21 -> {//  Тринадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.680f, 0.697f, 0.713f,
                                    0.695f, 0.710f, 0.727f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            22 -> {// Четырнадцать и тринадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.730f, 0.697f, 0.764f,
                                    0.745f, 0.710f, 0.780f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            23 -> {//  Четырнадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.730f, 0.748f, 0.764f,
                                    0.745f, 0.762f, 0.780f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            24 -> {//  Пятнадцать и четырнадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.782f, 0.748f, 0.816f,
                                    0.798f, 0.762f, 0.830f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            25 -> {//  Пятнадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.782f, 0.799f, 0.816f,
                                    0.798f, 0.815f, 0.830f
                                )
                               Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                                }
                            26 -> {  //  Шеснадцать и пятнадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.832f, 0.799f, 0.865f,
                                    0.845f, 0.815f, 0.880f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            27 -> {  //  Шестнадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.832f, 0.848f, 0.865f,
                                    0.845f, 0.863f, 0.880f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            28 -> {  //  Семнадцать и шеснадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.883f, 0.848f, 0.917f,
                                    0.898f, 0.863f, 0.930f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            29 -> {  // Семнадцать сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.883f, 0.900f, 0.917f,
                                    0.898f, 0.915f, 0.930f
                                )
                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                            30 -> {  //  Много сердец
                                Heart.playNextHeartAnimation(
                                    binding.lottieHeartGold,
                                    0.950f, 0.967f, 0.982f,
                                    0.966f, 0.981f, 0.999f
                                )

                                Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                        }
                    }
                }
            }
        }

        override fun onResume() {
            super.onResume()

            // При возвращении в активность — проверяем состояние таймера
            when (val state = countdownTimerManager.getTimerState()) {
                is CountdownTimerManager.TimerState.Forever -> {
                    binding.timerText.text = getString(R.string.active_forever_amulet)
                    binding.progressBar.visibility = View.GONE
                    binding.progressText.visibility = View.GONE
                    binding.progressBar2.visibility = View.GONE
                    binding.progressText2.visibility = View.GONE
                }

                is CountdownTimerManager.TimerState.Running -> {
                    // Запускаем таймер и планируем сброс
                    countdownTimerManager.startTimer()
                    ResetScheduler.scheduleResetWorker(this, state.remainingMillis)
                }

                is CountdownTimerManager.TimerState.Expired -> {
                    // Время вышло — сброс
                    binding.timerText.text = getString(R.string.not_active_amulet)
                    HeartAnimation.applyResetUI(
                        context = this,
                        binding = binding,
                        progressColors = progressColors,
                        countFirstProgress = goldProgressManager.count,
                        secondStageNumber = secondProgressManager.stageNumber
                    )
                }
            }

        }


        override fun onPause() {
            super.onPause()
        }
    }









    package com.hlodving.mytestgold

    import HeartAnimation
    import android.appwidget.AppWidgetManager
    import android.content.ComponentName
    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.work.ExistingWorkPolicy
    import androidx.work.OneTimeWorkRequestBuilder
    import androidx.work.WorkManager

    import com.hlodving.mytestgold.databinding.ActivityMainBinding

    class MainActivity : AppCompatActivity() {

        // Переменная с таймером
        private lateinit var countdownTimerManager: CountdownTimerManager
        //Переменная с вторым прогресс баром
        private lateinit var secondProgressManager: BonusStageManager
        //Переменная с основным счетчиком
        private lateinit var globalTapCounter: GlobalTapCounter


        // Переменные и настройки в начале класса
        private var lastStage = 1 // Последний достигнутый этап (нужен для проверки перехода на новый)
        private var CountFirstProgress = 0 // Общее количество нажатий для первого прогресс бара


        private val progressColors = listOf(
            R.drawable.progress_bar_green,
            R.drawable.progress_bar_blue,
            R.drawable.progress_bar_orange,
            R.drawable.progress_bar_purple,
            R.drawable.progress_bar_yellow,
            R.drawable.progress_bar_red
        ) // Список стилей прогресс-бара по фазам



        private var baseHoursToAdd = 2L // Начальная прибавка — 2 часа




        private var resetHappened = false // Флаг, отмечает факт сброса



        private var currentTapAnimation: String? = null //Текущий файл анимации для нажатий json от Lottie Это нужно, чтобы не переустанавливать один и тот же файл, если он не изменился.


        // Меняет анимацию активную и не активную
        private fun updateTapAnimationForStage(stage: Stage) {
            val newAnimation = if (stage.number == 1) "Gold_movement_cb.json" else "Gold_movement.json"
            if (currentTapAnimation != newAnimation) {
                currentTapAnimation = newAnimation
                binding.lottieTapGold.setAnimation(newAnimation)
            }
        }

        //Проверка вечного таймера
        private fun isOberegForever(): Boolean {
            return countdownTimerManager.loadTimerEndTime() == Long.MAX_VALUE
        }





        //Создаётся задание WorkManager
        private fun scheduleResetWorker(delayMillis: Long) {
            val workRequest = OneTimeWorkRequestBuilder<TimerWorker>()
                .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                "resetGoldWorker", // имя задачи
                ExistingWorkPolicy.REPLACE, // перезаписываем предыдущую, если она есть
                workRequest
            )
        }


            //Обновляет виджет при истечении времени
            private fun resetAppState() {
            //Обнуляет золото
            resetHappened = true
            CountFirstProgress = 0
            saveGoldCount()

            //Сбрасывает стадию и таймер
            lastStage = 1
            countdownTimerManager.timerEndTime = 0L
            countdownTimerManager.saveTimerEndTime(countdownTimerManager.timerEndTime)
            binding.timerText.text = getString(R.string.not_active_amulet)


            // Обновляет прогрессбар с учетом новой стадии
            val (stageProgress, stageMax, currentStage) = getStageData(CountFirstProgress)
            updateTapAnimationForStage(currentStage)

            binding.progressBar.max = stageMax
            binding.progressBar.progress = stageProgress
            binding.progressText.text = "$stageProgress / $stageMax"

            //Обновляются оба прогресс-бара
            val colorDrawableId2 = progressColors[(secondProgressManager.stageNumber - 1) % progressColors.size]
            binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this, colorDrawableId2)
            // тут мы берём номер этапа (Int), вычитаем 1
            val safeStageNum = currentStage.number.coerceIn(1, 101)
            val colorDrawableId = progressColors[(safeStageNum - 1) % progressColors.size]

            binding.progressBar.progressDrawable = ContextCompat.getDrawable(this, colorDrawableId)

            // Обновление виджета
            updateWidget()
        }


        //Этот метод обновляет виджет, подставляя нужную картинку в зависимости от goldCount.
        private fun updateWidget() {

            GoldWidget.currentGold = CountFirstProgress //Устанавливаем текущее колличество золота

            //Создаём Intent, чтобы отправить сигнал системе обновть виджет
            val intent = Intent(this, GoldWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    AppWidgetManager.getInstance(this@MainActivity)
                        .getAppWidgetIds(ComponentName(this@MainActivity, GoldWidget::class.java))
                )
            }

            sendBroadcast(intent) //Отправляем этот сигнал
        }





        // Сохраняет текущее количество кликов
        private fun saveGoldCount() {
            val sharedPref = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putInt("goldCount", CountFirstProgress)
                apply()
            }
        }

        // Загружает сохранённое количество кликов
        private fun loadGoldCount(): Int {
            val sharedPref = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
            return sharedPref.getInt("goldCount", 0)
        }

        //Этот метод считает, на каком этапе сейчас пользователь, сколько кликов нужно на следующий этап и сколько уже накоплено
        private fun getStageData(gold: Int): Triple<Int, Int, Stage> {
            var stageNumber = 1 //Номер стадии
            var requiredGold = 100 //Нужно тапов
            var accumulated = 0 //Накоплено

            while (gold >= accumulated + requiredGold) { //Цикл ищет на какой стадии находится пользователь
                accumulated += requiredGold
                stageNumber++
                requiredGold = stageNumber * 100
            }
            val stageProgress = gold - accumulated
            val stageMax = requiredGold
            val stage = Stage.fromGold(gold)

            return Triple(stageProgress, stageMax, stage)
        }


        lateinit var binding: ActivityMainBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            secondProgressManager = BonusStageManager(this)
            secondProgressManager.loadState()


            globalTapCounter = GlobalTapCounter(this)
            globalTapCounter.load()



            //инициализируем таймер
            countdownTimerManager = CountdownTimerManager(
                context = this,
                onTick = { formattedTime ->
                    binding.timerText.text = formattedTime
                },
                onFinished = {
                    if (!secondProgressManager.justFilled) {
                        resetAppState()
                    }
                    secondProgressManager.justFilled = false
                }
            )



            // Показываем цитату сразу при запуске
            binding.bonusQuoteText.text = secondProgressManager.getQuote()


            binding.progressBar2.max = secondProgressManager.maxProgress
            binding.progressBar2.progress = secondProgressManager.countSecondProgress
            binding.progressText2.text = "${secondProgressManager.countSecondProgress} / ${secondProgressManager.maxProgress}"

            val colorDrawableId2 = progressColors[(secondProgressManager.stageNumber - 1) % progressColors.size]

            binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this, colorDrawableId2)




            val endTime = countdownTimerManager.timerEndTime
            val now = System.currentTimeMillis()
            //Скрываем оба прогресс бара и их текст
            if (endTime == Long.MAX_VALUE) {
                binding.timerText.text = getString(R.string.active_forever_amulet)
                binding.progressBar.visibility = View.GONE
                binding.progressText.visibility = View.GONE
                binding.progressBar2.visibility = View.GONE
                binding.progressText2.visibility = View.GONE

            } else if (endTime > now) {
                countdownTimerManager.startTimer()
                val delayMillis = endTime - now
                scheduleResetWorker(delayMillis)

            } else {
                binding.timerText.text = getString(R.string.not_active_amulet)
            }



            binding.moreInfoButton.setOnClickListener {


                val intent = Intent(this, OberegInfoActivity::class.java)
                startActivity(intent)
            }

            // Загружаем сохранённое значение
            CountFirstProgress = loadGoldCount()

            // Обновляем виджет
            GoldWidget.currentGold = CountFirstProgress
            val widgetIntent = Intent(this@MainActivity, GoldWidget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    AppWidgetManager.getInstance(this@MainActivity)
                        .getAppWidgetIds(ComponentName(this@MainActivity, GoldWidget::class.java))
                )
            }


            sendBroadcast(widgetIntent)

    // Загружаем сохранённое время таймера
            countdownTimerManager.loadTimerEndTime()




            lastStage = getStageData(CountFirstProgress).third.number


            val (stageProgress, stageMax, currentStage) = getStageData(CountFirstProgress)
            updateTapAnimationForStage(currentStage)


            binding.progressBar.max = stageMax
            binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
            binding.progressText.text = "$stageProgress / $stageMax"

            val safeStageNum = currentStage.number.coerceIn(1, 101)
            val colorDrawableId = progressColors[(safeStageNum - 1) % progressColors.size]


            binding.progressBar.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId)


            // Анимации
            val Heart = HeartAnimation()

            binding.apply {
                Heart.animationRestart(lottieHeartGold)
                Heart.animationRestart(lottieViewShineOne)
                Heart.animationRestart(lottieViewShineTwo)
                Heart.animationRestart(lottieTapGold)


                lottieHeartGold.setOnClickListener {

                    globalTapCounter.increment()

                    if (isOberegForever()) return@setOnClickListener

                    CountFirstProgress++
                    saveGoldCount()

                    GoldWidget.currentGold = CountFirstProgress
                    val intent = Intent(this@MainActivity, GoldWidget::class.java).apply {

                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                            AppWidgetManager.getInstance(this@MainActivity).getAppWidgetIds(
                                ComponentName(this@MainActivity, GoldWidget::class.java)
                            ))
                    }

                    if (secondProgressManager.increment()) {
                        // 1. Увеличиваем таймер на бонусное время
                        val bonusTime = secondProgressManager.currentStage.bonusTimeMillis
                        val newBonusEndTime = countdownTimerManager.getRemainingTimeMillis() + System.currentTimeMillis() + bonusTime
                        countdownTimerManager.timerEndTime = newBonusEndTime
                        countdownTimerManager.saveTimerEndTime(newBonusEndTime)
                        countdownTimerManager.startTimer()
                        scheduleResetWorker(newBonusEndTime - System.currentTimeMillis())

                        // 2. Обновляем цитату
                        binding.bonusQuoteText.text = secondProgressManager.getQuote()

                        // 3. Проигрываем shine-анимации
                        Heart.playLottieAnimation(binding.lottieViewShineOne)
                        Heart.playLottieAnimation(binding.lottieViewShineTwo)

                        // 4. Обновляем UI прогресса (progressBar2)
                        binding.progressBar2.max = secondProgressManager.maxProgress
                        binding.progressBar2.progress = secondProgressManager.countSecondProgress
                        binding.progressText2.text = "${secondProgressManager.countSecondProgress} / ${secondProgressManager.maxProgress}"
                        val colorDrawableId2 = progressColors[(secondProgressManager.stageNumber - 1) % progressColors.size]
                        binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId2)

                    }




                    binding.progressBar2.max = secondProgressManager.maxProgress
                    binding.progressBar2.progress = secondProgressManager.countSecondProgress
                    binding.progressText2.text = "${secondProgressManager.countSecondProgress} / ${secondProgressManager.maxProgress}"



                    val colorDrawableId2 = progressColors[(secondProgressManager.stageNumber - 1) % progressColors.size]

                    binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId2)


                    sendBroadcast(intent)

                    val (stageProgress, stageMax, currentStage) = getStageData(CountFirstProgress)
                    updateTapAnimationForStage(currentStage)



                    // Действия при заполнении первого прогресс бара
                    if (currentStage.number > lastStage) {
                        //То что просходит по достижению 101 стадии
                        if (currentStage.number == 101) {

                            countdownTimerManager.timerEndTime = Long.MAX_VALUE
                            countdownTimerManager.saveTimerEndTime(Long.MAX_VALUE)

                            countdownTimerManager.stopTimer()
                            binding.timerText.text = getString(R.string.active_forever_amulet)
                            WorkManager.getInstance(this@MainActivity).cancelUniqueWork("resetGoldWorker")
                            // Скрываем оба прогресс-бара и их текст
                            binding.progressBar.visibility = View.GONE
                            binding.progressText.visibility = View.GONE
                            binding.progressBar2.visibility = View.GONE
                            binding.progressText2.visibility = View.GONE

                        } else { //Увеличивается время в таймере
                            val now = System.currentTimeMillis()
                            val safeRemaining = countdownTimerManager.getRemainingTimeMillis()


                            // Каждая следующая стадия добавляет на 1 час больше
                            val additionalHours = baseHoursToAdd + (currentStage.number - 2)
                            val additionalMillis = additionalHours * 60 * 60 * 1000

                            val newTime = now + safeRemaining + additionalMillis
                            countdownTimerManager.timerEndTime = newTime
                            countdownTimerManager.saveTimerEndTime(newTime)
                            val delayMillis = newTime - System.currentTimeMillis()

                            countdownTimerManager.startTimer()

                            scheduleResetWorker(delayMillis)
                        }

                        lastStage = currentStage.number

                        Heart.playLottieAnimation(binding.lottieViewShineOne)
                        Heart.playLottieAnimation(binding.lottieViewShineTwo)
                    }


                    binding.progressBar.max = stageMax
                    binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
                    binding.progressText.text = "$stageProgress / $stageMax"

                    val colorDrawableId = progressColors[(currentStage.number - 1) % progressColors.size]

                    binding.progressBar.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId)




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
        }

        override fun onPause() {
            super.onPause()
        }
    }









package com.hlodving.mytestgold

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

import com.hlodving.mytestgold.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Переменные и настройки в начале класса
    private var lastStage = 1 // Последний достигнутый этап (нужен для проверки перехода на новый)
    private val totalActions = 21 // Всего действий в специальной анимационной последовательности
    private var currentAction = 1 // Индекс текущего действия
    private val actionsSequence = mutableListOf<Int>() // Список действий для выполнения
    private var goldCount = 0 // Общее количество нажатий
    private val progressColors = listOf(
        R.drawable.progress_bar_green,
        R.drawable.progress_bar_blue,
        R.drawable.progress_bar_orange,
        R.drawable.progress_bar_purple,
        R.drawable.progress_bar_yellow,
        R.drawable.progress_bar_red
    ) // Список стилей прогресс-бара по фазам


    private var resetHappened = false

    private var bonusProgress = 0
    private var bonusMax = 1000


    private fun getBonusStage(): Int {
        return bonusMax / 100
    }


    private fun saveBonusProgress() {
        val prefs = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("bonusProgress", bonusProgress).apply()
    }

    private fun loadBonusProgress(): Int {
        val prefs = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("bonusProgress", 0)
    }

    private fun saveBonusMax() {
        val prefs = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("bonusMax", bonusMax).apply()
    }

    private fun loadBonusMax(): Int {
        val prefs = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        return prefs.getInt("bonusMax", 1000)
    }


    private var currentTapAnimation: String? = null

    private fun updateTapAnimationForStage(stage: Stage) {
        val newAnimation = if (stage.number == 1) "Gold_movement_cb.json" else "Gold_movement.json"
        if (currentTapAnimation != newAnimation) {
            currentTapAnimation = newAnimation
            binding.lottieTapGold.setAnimation(newAnimation)
        }
    }





    //Таймер обратного отсчёта
    private var timerEndTime: Long = 0L // Время окончания таймера
    private var timerHandler = android.os.Handler() // Объект для запуска таймера
    private lateinit var timerRunnable: Runnable // Код, который будет запускаться каждую секунду


    //Эта функция запускает и отображает таймер обратного отсчёта, который при завершении сбрасывает нажатия.
    private fun startCountdownTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                val remaining = timerEndTime - System.currentTimeMillis()
                if (remaining > 0) {
                    val hours = remaining / (1000 * 60 * 60)
                    val minutes = (remaining / (1000 * 60)) % 60
                    val seconds = (remaining / 1000) % 60
                    binding.timerText.text = String.format("Оберег активен: %02d:%02d:%02d", hours, minutes, seconds)
                    timerHandler.postDelayed(this, 1000)
                } else {
                    resetAppState()
                }
            }
        }
        timerHandler.post(timerRunnable)
    }

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
        resetHappened = true
        goldCount = 0
        saveGoldCount()

        lastStage = 1
        timerEndTime = 0L
        saveTimerEndTime(timerEndTime)

        binding.timerText.text = "Оберег неактивен"

        // Сброс прогресса
        val (stageProgress, stageMax, currentStage) = getStageData(goldCount)
        updateTapAnimationForStage(currentStage)

        binding.progressBar.max = stageMax
        binding.progressBar.progress = stageProgress
        binding.progressText.text = "$stageProgress / $stageMax"

        val colorDrawableId2 = progressColors[(getBonusStage() - 1) % progressColors.size]
        binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this, colorDrawableId2)



// тут мы берём номер этапа (Int), вычитаем 1
        val colorDrawableId = progressColors[(currentStage.number - 1) % progressColors.size]
        binding.progressBar.progressDrawable = ContextCompat.getDrawable(this, colorDrawableId)

        // Сброс второго прогресс-бара
        bonusProgress = 0
        bonusMax = 1000
        saveBonusProgress()
        saveBonusMax()

        binding.progressBar2.max = bonusMax
        binding.progressBar2.progress = bonusProgress
        binding.progressText2.text = "$bonusProgress / $bonusMax"



        // Обновление виджета
        updateWidget()
    }



    //Этот метод обновляет виджет, подставляя нужную картинку в зависимости от goldCount.
    private fun updateWidget() {
        GoldWidget.currentGold = goldCount
        val intent = Intent(this, GoldWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(
                AppWidgetManager.EXTRA_APPWIDGET_IDS,
                AppWidgetManager.getInstance(this@MainActivity)
                    .getAppWidgetIds(ComponentName(this@MainActivity, GoldWidget::class.java))
            )
        }
        sendBroadcast(intent)
    }



    private fun isWidgetPresent(): Boolean {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val widgetIds = appWidgetManager.getAppWidgetIds(
            ComponentName(this, GoldWidget::class.java)
        )
        return widgetIds.isNotEmpty()
    }



    // Сохраняет время окончания таймера
    private fun saveTimerEndTime(timeInMillis: Long) {
        val sharedPref = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("timerEndTime", timeInMillis)
            apply()
        }
    }
    //Загружает время окончания таймера
    private fun loadTimerEndTime(): Long {
        val sharedPref = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        return sharedPref.getLong("timerEndTime", 0L)
    }

    // Сохраняет текущее количество кликов
    private fun saveGoldCount() {
        val sharedPref = getSharedPreferences("GoldPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("goldCount", goldCount)
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
        val stage = Stage.fromGold(gold)

        return Triple(stageProgress, stageMax, stage)
    }




    lateinit var binding: ActivityMainBinding


    private val widgetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.hlodving.WIDGET_PRESENT") {
                // 🔥 При получении сигнала — тоже скрываем
                if (isWidgetPresent()) {
                    binding.widgetHintText.visibility = View.GONE
                }
            }
        }
    }






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bonusProgress = loadBonusProgress()

        bonusMax = loadBonusMax()


        binding.progressBar2.max = bonusMax
        binding.progressBar2.progress = bonusProgress
        binding.progressText2.text = "$bonusProgress / $bonusMax"

        val colorDrawableId2 = progressColors[(getBonusStage() - 1) % progressColors.size]
        binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this, colorDrawableId2)



        timerEndTime = loadTimerEndTime()



        binding.moreInfoButton.setOnClickListener {


            val intent = Intent(this, OberegInfoActivity::class.java)
            startActivity(intent)
        }



        binding.widgetHintText.visibility = if (isWidgetPresent()) View.GONE else View.VISIBLE






        // Загружаем сохранённое значение
        goldCount = loadGoldCount()



        // Обновляем виджет
        GoldWidget.currentGold = goldCount
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
        timerEndTime = loadTimerEndTime()

        if (timerEndTime > System.currentTimeMillis()) {
            startCountdownTimer()

            // <<< ДОБАВЛЕНО >>> Перезапускаем WorkManager на случай перезапуска приложения
            val delayMillis = timerEndTime - System.currentTimeMillis()
            scheduleResetWorker(delayMillis)
        } else {
            binding.timerText.text = "Оберег неактивен"
        }




        lastStage = getStageData(goldCount).third.number




        val (stageProgress, stageMax, currentStage) = getStageData(goldCount)
        updateTapAnimationForStage(currentStage)



        binding.progressBar.max = stageMax
        binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
        binding.progressText.text = "$stageProgress / $stageMax"

        val colorDrawableId = progressColors[(currentStage.number - 1) % progressColors.size]

        binding.progressBar.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId)





        // Анимации
        val Heart = HeartAnimation()

        binding.apply {
            Heart.animationRestart(lottieHeartGold)
            Heart.animationRestart(lottieViewShineOne)
            Heart.animationRestart(lottieViewShineTwo)
            Heart.animationRestart(lottieTapGold)





            lottieHeartGold.setOnClickListener {
                goldCount++
                saveGoldCount()

                GoldWidget.currentGold = goldCount
                val intent = Intent(this@MainActivity, GoldWidget::class.java).apply {

                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        AppWidgetManager.getInstance(this@MainActivity).getAppWidgetIds(
                            ComponentName(this@MainActivity, GoldWidget::class.java)
                        ))
                }

                bonusProgress++
                if (bonusProgress >= bonusMax) {
                    bonusProgress = 0
                    bonusMax += 100
                    // +24 часа к таймеру
                    timerEndTime += 24 * 60 * 60 * 1000
                    saveTimerEndTime(timerEndTime)
                    startCountdownTimer()
                    scheduleResetWorker(timerEndTime - System.currentTimeMillis())
                }
                saveBonusProgress()
                saveBonusMax()

                binding.progressBar2.max = bonusMax
                binding.progressBar2.progress = bonusProgress
                binding.progressText2.text = "$bonusProgress / $bonusMax"



                val colorDrawableId2 = progressColors[(getBonusStage() - 1) % progressColors.size]
                binding.progressBar2.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId2)








                sendBroadcast(intent)



                val (stageProgress, stageMax, currentStage) = getStageData(goldCount)
                updateTapAnimationForStage(currentStage)




                if (resetHappened) {
                    if (currentStage.number > 1) {
                        Log.d("MyLog", "После сброса — восстанавливаем таймер")
                        timerEndTime = System.currentTimeMillis() + 1 * 60 * 1000
                        saveTimerEndTime(timerEndTime)
                        startCountdownTimer()
                    } else {
                        Log.d("MyLog", "Сброс был, но этап 1 — таймер не запускаем")
                        binding.timerText.text = "Оберег неактивен"
                    }
                    resetHappened = false
                }




                if (currentStage.number > lastStage) {
                    // <<< ТУТ запуск анимаций по переходу этапа >>>
                    Heart.playLottieAnimation(lottieViewShineOne)
                    Heart.playLottieAnimation(lottieViewShineTwo)

                    // Таймер первого прогрессбара
                    val now = System.currentTimeMillis()
                    val remaining = timerEndTime - now
                    val safeRemaining = if (remaining > 0) remaining else 0
                    timerEndTime = now + safeRemaining + 6 * 60 * 60 * 1000 // Настройка таймера

                    saveTimerEndTime(timerEndTime)
                    startCountdownTimer()

                    val delayMillis = timerEndTime - System.currentTimeMillis()
                    scheduleResetWorker(delayMillis)

                    Log.d("MyLog", "Переход на этап ${currentStage.number} — запускаем таймер")
                    lastStage = currentStage.number
                }







                binding.progressBar.max = stageMax
                binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
                binding.progressText.text = "$stageProgress / $stageMax"

                val colorDrawableId = progressColors[(currentStage.number - 1) % progressColors.size]

                binding.progressBar.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId)




                //if (goldCount % 50 == 0) Heart.playLottieAnimation(lottieViewShineOne)

                //if (goldCount % 75 == 0) Heart.playLottieAnimation(lottieViewShineTwo)




                when (goldCount % 10) {
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

                if (goldCount % 27 == 0) {


                // Генерация последовательности выполнения действий
                fun generateActionsSequence() {
                    // Добавляем действия в последовательности
                    for (stage in 1..totalActions) {
                        // Первое действие
                        actionsSequence.add(1)

                        // Промежуточные действия
                        for (i in 2..stage) {
                            actionsSequence.add(i)
                        }

                        // Последнее действие
                        actionsSequence.add(totalActions)
                    }
                }

                generateActionsSequence()


                // Выполнение следующего действия
                 fun executeNextAction() {
                    // Если действия завершились
                    if (currentAction > actionsSequence.size) {
                        Log.d("MyLog", "Все действия выполнены.")
                        Heart.playLottieAnimation(lottieHeartGoldFinish)
                        Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        return
                    }


                    // Выполняем текущее действие
                    val actionNumber = actionsSequence[currentAction - 1]
                    Log.d("MyLog", "Выполняется  $actionNumber")

                    // Переходим к следующему действию
                    currentAction++

                    when (actionNumber) {
                        1 -> {
                            Log.d("MyLog", "Реально выполняется первое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.01f, 0.025f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f, 0.22f)
                        }
                        2 -> {
                            Log.d("MyLog", "Реально выполняется второе действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.027f,0.04f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        3 -> {
                            Log.d("MyLog", "Реально выполняется третье действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.045f,0.06f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        4 -> {
                            Log.d("MyLog", "Реально выполняется четвертое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.083f,0.095f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        5 -> {
                            Log.d("MyLog", "Реально выполняется пятое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.1f,0.112f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        6 -> {
                            Log.d("MyLog", "Реально выполняется шестое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.115f,0.132f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        7 -> {
                            Log.d("MyLog", "Реально выполняется седьмое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.135f,0.148f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        8 -> {
                            Log.d("MyLog", "Реально выполняется восьмое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.151f,0.165f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        9 -> {
                            Log.d("MyLog", "Реально выполняется девятое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.17f,0.185f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        10 -> {
                            Log.d("MyLog", "Реально выполняется десятое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.19f,0.205f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        11 -> {
                            Log.d("MyLog", "Реально выполняется одиннадцатое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.207f,0.225f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        12 -> {
                            Log.d("MyLog", "Реально выполняется двенадцатое действие $currentAction")
                            Heart.playLottieAnimation(lottieHeartGold, 0.226f,0.24f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        13 -> {
                            Log.d("MyLog", "Реально выполняется тринадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.243f,0.258f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        14 -> {
                            Log.d("MyLog", "Реально выполняется четырнадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.262f,0.276f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        15 -> {
                            Log.d("MyLog", "Реально выполняется пятнадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.280f,0.295f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        16 -> {
                            Log.d("MyLog", "Реально выполняется шестнадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.298f,0.311f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        17 -> {
                            Log.d("MyLog", "Реально выполняется семнадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.333f,0.351f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        18 -> {
                            Log.d("MyLog", "Реально выполняется восемнадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.352f,0.368f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        19 -> {
                            Log.d("MyLog", "Реально выполняется девятнадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.371f,0.387f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        20 -> {
                            Log.d("MyLog", "Реально выполняется двадцатое действие")
                            Heart.playLottieAnimation(lottieHeartGold, 0.388f,0.4f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                        }
                        21 -> {
                            Log.d("MyLog", "Сброс")
                            Heart.playLottieAnimation(lottieHeartGold, 0.402f,0.418f)
                            Heart.playLottieAnimation(lottieTapGold, 0.19f,0.22f)
                            }
                        }

                    }
                    executeNextAction()

                }



                }




                }


            }

    override fun onResume() {
        super.onResume()

        // 🔥 Проверка сразу: есть ли уже виджет
        if (isWidgetPresent()) {
            binding.widgetHintText.visibility = View.GONE
        }

        // 🔥 Подписка на Broadcast от GoldWidget
        registerReceiver(
            widgetReceiver,
            IntentFilter("com.hlodving.WIDGET_PRESENT"),
            Context.RECEIVER_NOT_EXPORTED
        )
    }



    override fun onPause() {
        super.onPause()
        unregisterReceiver(widgetReceiver)
    }



}









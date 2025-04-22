package com.hlodving.mytestgold

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.hlodving.mytestgold.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    // Общее количество действий
    private val totalActions = 21

    // Текущий индекс действия
    private var currentAction = 1

    // Список действий для выполнения
    private val actionsSequence = mutableListOf<Int>()

    private var goldCount = 0
    private val progressColors = listOf(
        R.drawable.progress_bar_green,
        R.drawable.progress_bar_blue,
        R.drawable.progress_bar_orange,
        R.drawable.progress_bar_purple,
        R.drawable.progress_bar_yellow,
        R.drawable.progress_bar_red
    )


    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        var Heart = HeartAnimation()



        binding.apply {


            Heart.animationRestart(lottieHeartGold)
            Heart.animationRestart(lottieViewShineOne)
            Heart.animationRestart(lottieViewShineTwo)
            Heart.animationRestart(lottieTapGold)





            lottieHeartGold.setOnClickListener {
                goldCount++

                GoldWidget.currentGold = goldCount
                val intent = Intent(this@MainActivity, GoldWidget::class.java).apply {

                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                        AppWidgetManager.getInstance(this@MainActivity).getAppWidgetIds(
                            ComponentName(this@MainActivity, GoldWidget::class.java)
                        ))
                }
                sendBroadcast(intent)



                val currentStage = (goldCount / 200) + 1
                val stageMax = currentStage * 200
                val stageStart = (currentStage - 1) * 200
                val stageProgress = goldCount - stageStart

                binding.progressBar.max = stageMax
                binding.progressBar.progress = stageProgress.coerceAtMost(stageMax)
                binding.progressText.text = "$stageProgress / $stageMax"

                val colorDrawableId = progressColors[(currentStage - 1) % progressColors.size]
                binding.progressBar.progressDrawable = ContextCompat.getDrawable(this@MainActivity, colorDrawableId)



                if (goldCount % 50 == 0) Heart.playLottieAnimation(lottieViewShineOne)

                if (goldCount % 75 == 0) Heart.playLottieAnimation(lottieViewShineTwo)




                when (goldCount % 10) {

                        1 -> {
                            Heart.playLottieAnimation(lottie1)
                        }

                        2 -> {
                            Heart.playLottieAnimation(lottie2)
                        }

                        3 -> {
                            Heart.playLottieAnimation(lottie5)
                        }

                        4 -> {
                            Heart.playLottieAnimation(lottie7)
                        }

                        5 -> {
                            Heart.playLottieAnimation(lottie4)
                        }

                        6 -> {
                            Heart.playLottieAnimation(lottie8)
                        }

                        7 -> {
                            Heart.playLottieAnimation(lottie9)
                        }

                        8 -> {
                            Heart.playLottieAnimation(lottie6)
                        }

                        9 -> {
                            Heart.playLottieAnimation(lottie3)
                        }

                        0 -> {
                            Heart.playLottieAnimation(lottie4)
                        }
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
//                            lottieHeartGold.setMinProgress(0f)
//                            lottieHeartGold.setMaxProgress(0f)
                            }
                        }

                    }
                    executeNextAction()

                }



                }




                }


            }


        }









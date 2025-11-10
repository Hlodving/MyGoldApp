package com.hlodving.mytestgold

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.hlodving.mytestgold.database.DbManager
import com.hlodving.mytestgold.databinding.ActivityMainBinding
import com.hlodving.mytestgold.dialoghelper.DialogConst
import com.hlodving.mytestgold.dialoghelper.DialogHelper
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, DbManager.DatabaseCallback {

    // --- Биндинг и Firebase ---
    lateinit var binding: ActivityMainBinding
    val mAuth = FirebaseAuth.getInstance()
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    // --- Менеджеры состояний ---
    private lateinit var dbManager: DbManager
    private lateinit var countdownTimerManager: CountdownTimerManager
    private lateinit var secondProgressManager: BonusStageManager
    private lateinit var globalTapCounter: GlobalTapCounter
    private lateinit var goldProgressManager: GoldProgressManager
    private val dialogHelper by lazy { DialogHelper(this) }

    // --- НОВЫЕ КЛАССЫ-ПОМОЩНИКИ ДЛЯ РЕФАКТОРИНГА ---
    private lateinit var mainViewUpdater: MainViewUpdater
    private lateinit var gameEngine: GameEngine
    private lateinit var authUIManager: AuthUIManager

    // --- UI переменные и состояние ---
    private lateinit var tvAccount: TextView
    private var userAlias: String = "Гость"

    private var userFaith: String? = null
    var userDataReady: Boolean = false // Публичный для доступа из GameEngine

    private val progressColors = listOf(
        R.drawable.progress_bar_green, R.drawable.progress_bar_blue, R.drawable.progress_bar_orange,
        R.drawable.progress_bar_purple, R.drawable.progress_bar_yellow, R.drawable.progress_bar_red
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Инициализация всех менеджеров данных
        initManagers()

        // 2. Инициализация новых классов-помощников
        mainViewUpdater = MainViewUpdater(this, binding, progressColors)
        authUIManager = AuthUIManager(this, binding.navView, dbManager)

        // ИСПРАВЛЕННЫЙ ВЫЗОВ КОНСТРУКТОРА:
        gameEngine = GameEngine(
            this,
            binding,
            dbManager,
            goldProgressManager,
            secondProgressManager,
            globalTapCounter,
            countdownTimerManager,
            mainViewUpdater
        )

        // 3. Настройка UI (меню, toolbar) и слушателей
        initNavigationDrawer()
        setupAuthStateListener() // Зависит от authUIManager

        // 4. Первичная загрузка и настройка UI
        loadInitialState()

        // 5. Установка слушателя на основную кнопку
        binding.lottieHeartGold.setOnClickListener {
            // Вся логика теперь в одном вызове!
            gameEngine.onHeartTapped()
        }

    }

    private fun initManagers() {
        dbManager = DbManager(this)
        // В GoldProgressManager больше не нужно передавать binding, так как UI обновляется через MainViewUpdater
        goldProgressManager = GoldProgressManager(this)
        secondProgressManager = BonusStageManager(this)
        globalTapCounter = GlobalTapCounter(this)

        countdownTimerManager = CountdownTimerManager(this,
            onTick = { formattedTime -> mainViewUpdater.updateTimer(formattedTime) },
            onFinished = {
                goldProgressManager.reset()
                countdownTimerManager.resetTimer()
                HeartAnimation.applyResetUI(this, binding, progressColors, goldProgressManager.count, secondProgressManager.stageNumber)
                secondProgressManager.justFilled = false
            },
            onStopped = { ResetScheduler.stopWidgetAnimationService(this) }
        )
    }

    private fun initNavigationDrawer() {
        setSupportActionBar(binding.actionBarInclude.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerlayout, binding.actionBarInclude.toolbar,
            R.string.open, R.string.close
        )
        binding.drawerlayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
        tvAccount = binding.navView.getHeaderView(0).findViewById(R.id.tvAccountEmail)
    }

    private fun loadInitialState() {
        // Загрузка локальных данных
        goldProgressManager.load()
        secondProgressManager.loadState()
        globalTapCounter.load()
        countdownTimerManager.loadTimerEndTime()

        // Проверка состояния таймера при запуске
        when (val state = countdownTimerManager.getTimerState()) {
            is CountdownTimerManager.TimerState.Forever -> mainViewUpdater.showAmuletForever()
            is CountdownTimerManager.TimerState.Running -> {
                countdownTimerManager.startTimer()
                ResetScheduler.scheduleResetWorker(this, state.remainingMillis)
            }
            is CountdownTimerManager.TimerState.Expired -> mainViewUpdater.showAmuletNotActive()
        }

        // Единое обновление UI при старте
        val (progress, max, stage) = goldProgressManager.getProgressInfo()
        mainViewUpdater.updateGoldProgress(progress, max, stage.number)
        mainViewUpdater.updateBonusProgress(secondProgressManager.countSecondProgress, secondProgressManager.maxProgress, secondProgressManager.stageNumber)
        mainViewUpdater.updateBonusQuote(secondProgressManager.getQuote())
        val isAmuletActive = countdownTimerManager.getTimerState() !is CountdownTimerManager.TimerState.Expired
        mainViewUpdater.updateTapAnimation(isAmuletActive)
        GoldWidget.updateAllWidgets(this, goldProgressManager.count)

        mainViewUpdater.updateTotalTaps(globalTapCounter.totalTaps)

        // Запуск анимаций Lottie
        binding.apply {
            HeartAnimation.animationRestart(lottieHeartGold)
            HeartAnimation.animationRestart(lottieViewShineOne)
            HeartAnimation.animationRestart(lottieViewShineTwo)
            HeartAnimation.animationRestart(lottieTapGold)
        }
    }

    override fun onResume() {
        super.onResume()
        when (val state = countdownTimerManager.getTimerState()) {
            is CountdownTimerManager.TimerState.Forever -> mainViewUpdater.showAmuletForever()
            is CountdownTimerManager.TimerState.Running -> {
                countdownTimerManager.startTimer()
                ResetScheduler.scheduleResetWorker(this, state.remainingMillis)
            }
            is CountdownTimerManager.TimerState.Expired -> {
                mainViewUpdater.showAmuletNotActive()
                HeartAnimation.applyResetUI(this, binding, progressColors, goldProgressManager.count, secondProgressManager.stageNumber)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (mAuth.currentUser != null && userDataReady) {
            dbManager.saveProgress(globalTapCounter.totalTaps, secondProgressManager.countSecondProgress, secondProgressManager.stageNumber)
        }
    }

    override fun onStart() {
        super.onStart()
        userDataReady = false
        mAuth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        mAuth.removeAuthStateListener(authStateListener)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_obereg_info -> startActivity(Intent(this, OberegInfoActivity::class.java))
            R.id.menu_obereg_progress -> startActivity(Intent(this, ProgressActivity::class.java))
            R.id.menu_obereg_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_sign_up -> dialogHelper.createSignDialog(DialogConst.SING_UP_STATE)
            R.id.menu_sign_in -> dialogHelper.createSignDialog(DialogConst.SING_IN_STATE)
            R.id.menu_sign_out -> {
                if (mAuth.currentUser != null) {
                    dbManager.saveProgress(globalTapCounter.totalTaps, secondProgressManager.countSecondProgress, secondProgressManager.stageNumber)
                }
                mAuth.signOut()
                performLocalDataClear()
                Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
            }
        }
        binding.drawerlayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDataLoaded(totalTaps: Int, bonusProgress: Int, bonusStage: Int, alias: String, faith: String?) {
        userAlias = alias
        tvAccount.text = alias
        globalTapCounter.updateTotalTaps(totalTaps)
        secondProgressManager.updateState(bonusProgress, bonusStage)
        userFaith = faith

        mainViewUpdater.updateBonusProgress(secondProgressManager.countSecondProgress, secondProgressManager.maxProgress, secondProgressManager.stageNumber)
        mainViewUpdater.updateBonusQuote(secondProgressManager.getQuote())

        mainViewUpdater.updateTotalTaps(globalTapCounter.totalTaps)

        userDataReady = true
    }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            authUIManager.updateUiForUser(user)
        }
    }

    fun performLocalDataClear() {
        clearLocalUserData()
        updateUIAfterReset()
    }

    private fun clearLocalUserData() {
        globalTapCounter.reset()
        secondProgressManager.resetState()
        userFaith = null
    }

    private fun updateUIAfterReset() {
        mainViewUpdater.updateBonusProgress(
            secondProgressManager.countSecondProgress,
            secondProgressManager.maxProgress,
            secondProgressManager.stageNumber
        )
        mainViewUpdater.updateBonusQuote(secondProgressManager.getQuote())

        val (progress, max, stage) = goldProgressManager.getProgressInfo()
        mainViewUpdater.updateGoldProgress(progress, max, stage.number)

        val isAmuletActive = countdownTimerManager.getTimerState() !is CountdownTimerManager.TimerState.Expired
        mainViewUpdater.updateTapAnimation(isAmuletActive)

        mainViewUpdater.updateTotalTaps(globalTapCounter.totalTaps)

        GoldWidget.updateAllWidgets(this, goldProgressManager.count)
    }
}
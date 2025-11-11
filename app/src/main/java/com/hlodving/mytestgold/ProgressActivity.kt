package com.hlodving.mytestgold

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hlodving.mytestgold.databinding.ActivityProgressBinding
import java.text.NumberFormat
import java.util.*


// Хранение данных пользователя
data class LeaderboardUser(
    val uid: String,
    val alias: String,
    val score: Int,
    val isVerified: Boolean,
    val faith: String? // ДОБАВЛЕНО ПОЛЕ ДЛЯ ВЕРЫ
)


// Таблица топ-7
class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private val auth = FirebaseAuth.getInstance()
    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    // Ссылка на запрос для топ-7 игроков
    private val topUsersRef: Query = db.orderByChild("globalTapCounter").limitToLast(7)
    private lateinit var leaderboardListener: ValueEventListener

    private var myUid: String? = null
    private var myAlias: String = "—"
    private var myScore: Int = 0
    private var myRank: Int = 0
    private var myFaith: String? = null // ДОБАВЛЕНО ПОЛЕ ДЛЯ МОЕЙ ВЕРЫ
    private val nf = NumberFormat.getIntegerInstance(Locale.getDefault())

    private var amIVerified: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Адаптация UI под системные панели (например, навигационная полоса)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val lp = binding.myBlock.layoutParams as ConstraintLayout.LayoutParams
            val base = (12 * resources.displayMetrics.density).toInt()
            lp.bottomMargin = base + bottomInset
            binding.myBlock.layoutParams = lp
            insets
        }

        myUid = auth.currentUser?.uid
    }


    override fun onStart() {
        super.onStart()
        // Проверяем наличие интернет-соединения
        if (!NetworkUtils.isNetworkAvailable(this)) {
            // Если сети нет, показываем предупреждение и не загружаем данные
            binding.noInternetWarning.visibility = View.VISIBLE
            binding.unverifiedWarning.visibility = View.GONE
            binding.resendVerificationButton.visibility = View.GONE // Прячем кнопку
            Toast.makeText(this, "Проверьте подключение к интернету", Toast.LENGTH_LONG).show()
            updateMyBlockWithOfflineStatus()
            binding.leaderboardTable.removeAllViews()
            return // Прекращаем выполнение, так как сети нет
        }

        // Если сеть есть, скрываем предупреждение
        binding.noInternetWarning.visibility = View.GONE


        // ЛОГИКА ПЕРЕНЕСЕНА С ПРЕДУПРЕЖДЕНИЯ НА НОВУЮ КНОПКУ
        binding.resendVerificationButton.setOnClickListener {
            auth.currentUser?.sendEmailVerification()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Письмо с подтверждением отправлено повторно. Проверьте почту.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Не удалось отправить письмо. Попробуйте еще раз позже.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }



        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Если пользователя нет (гость), просто скрываем плашку и кнопку
            binding.unverifiedWarning.visibility = View.VISIBLE
            binding.unverifiedWarning.text = getString(R.string.register_to_see)
            binding.resendVerificationButton.visibility = View.GONE
            loadMyInfo {
                updateMyBlock()
            }
            attachLeaderboardListener()
            return
        }

        // Принудительно перезагружаем данные пользователя из Firebase Auth
        currentUser.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // После перезагрузки user.isEmailVerified будет иметь актуальное значение
                amIVerified = currentUser.isEmailVerified

                // Синхронизируем актуальный статус с Realtime Database
                db.child(currentUser.uid).child("emailVerified").setValue(amIVerified)

                // Показываем или прячем плашку и кнопку НЕМЕДЛЕННО
                if (amIVerified) {
                    binding.unverifiedWarning.visibility = View.GONE
                    binding.resendVerificationButton.visibility = View.GONE // Прячем кнопку
                } else {
                    binding.unverifiedWarning.visibility = View.VISIBLE
                    binding.resendVerificationButton.visibility = View.VISIBLE // Показываем кнопку
                }

                // Теперь загружаем остальную информацию, как и раньше
                loadMyInfo {
                    updateMyBlock()
                    loadMyRank {
                        updateMyBlock()
                    }
                }
            } else {

                // Если перезагрузка не удалась (например, временные проблемы с сетью),
                // используем старую логику, чтобы приложение не сломалось.
                loadMyInfo {
                    if (amIVerified) {
                        binding.unverifiedWarning.visibility = View.GONE
                        binding.resendVerificationButton.visibility = View.GONE
                    } else {
                        binding.unverifiedWarning.visibility = View.VISIBLE
                        binding.resendVerificationButton.visibility = View.VISIBLE
                    }
                    updateMyBlock()
                    loadMyRank {
                        updateMyBlock()
                    }
                }
            }
        }

        // Устанавливаем слушатель для обновлений таблицы лидеров в реальном времени
        attachLeaderboardListener()
    }

    override fun onStop() {
        super.onStop()
        // ВАЖНО: Удаляем слушатель, когда активность не видна,
        // чтобы избежать утечек памяти и лишних операций.
        topUsersRef.removeEventListener(leaderboardListener)
    }

    private fun attachLeaderboardListener() {
        leaderboardListener = object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                val users = mutableListOf<LeaderboardUser>() // ИСПОЛЬЗУЕМ НОВЫЙ КЛАСС LeaderboardUser
                s.children.forEach { userSnapshot ->
                    val uid = userSnapshot.key ?: ""
                    val alias = userSnapshot.child("alias").getValue(String::class.java) ?: "Пользователь"
                    val score = userSnapshot.child("globalTapCounter").getValue(Int::class.java) ?: 0
                    val isVerified = userSnapshot.child("emailVerified").getValue(Boolean::class.java) ?: false
                    val faith = userSnapshot.child("faith").getValue(String::class.java) // ПОЛУЧАЕМ ТИП ВЕРЫ
                    users += LeaderboardUser(uid, alias, score, isVerified, faith)
                }
                val sorted = users.sortedByDescending { it.score }
                renderTable(sorted)
            }
            override fun onCancelled(e: DatabaseError) { /* ... */ }
        }
        topUsersRef.addValueEventListener(leaderboardListener)
    }

    // Загрузка моих данных: alias, score и faith из /users/<uid>
    private fun loadMyInfo(onDone: () -> Unit) {
        val uid = myUid
        if (uid == null) {
            onDone()
            return
        }
        db.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                myAlias = s.child("alias").getValue(String::class.java) ?: auth.currentUser?.email ?: "Псевдоним"
                myScore = s.child("globalTapCounter").getValue(Int::class.java) ?: 0
                myFaith = s.child("faith").getValue(String::class.java) // ПОЛУЧАЕМ МОЮ ВЕРУ

                onDone()
            }
            override fun onCancelled(e: DatabaseError) { onDone() }
        })
    }

    // Подсчёт моего места в таблице.
    private fun loadMyRank(onDone: () -> Unit) {
        // Мы ищем всех пользователей, у кого очков больше, чем у нас
        val threshold = myScore + 1.0
        db.orderByChild("globalTapCounter")
            .startAt(threshold)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    // Наше место = количество людей с большим счетом + 1
                    myRank = s.childrenCount.toInt() + 1
                    onDone()
                }
                override fun onCancelled(e: DatabaseError) {
                    onDone()
                }
            })
    }

    // Рендер таблицы (шапка + строки данных) в TableLayout.
    private fun renderTable(data: List<LeaderboardUser>) { // ТЕПЕРЬ ПРИНИМАЕМ СПИСОК LeaderboardUser
        val table = binding.leaderboardTable
        table.removeAllViews()
        // ШАПКА ТАБЛИЦЫ
        addRow(table, pos = "№", alias = "Псевдоним", score = "Счёт", isHeader = true, faith = null)

        data.forEachIndexed { index, user ->
            val isMe = (user.uid == myUid)
            addRow(
                table,
                pos = (index + 1).toString(),
                alias = user.alias,
                score = nf.format(user.score),
                isHeader = false,
                highlight = isMe,
                faith = user.faith // ПЕРЕДАЕМ ТИП ВЕРЫ В addRow
            )
        }
    }

    // Утилита добавления строки (шапка или обычная) в таблицу.
    private fun addRow(
        table: TableLayout, pos: String, alias: String, score: String,
        isHeader: Boolean = false, highlight: Boolean = false, faith: String? // ДОБАВЛЕНО faith
    ) {
        val tr = TableRow(this).apply {
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT
            )
            weightSum = 1f
            setPadding(dp(8), dp(6), dp(8), dp(6))
            if (isHeader) setBackgroundColor(0x11000000)
            if (highlight) setBackgroundColor(0x113980FF)
        }

        // ИКОНКА ВЕРЫ (ДЛЯ НЕ-ШАПКИ)
        if (!isHeader) {
            val faithIcon = ImageView(this).apply {
                layoutParams = TableRow.LayoutParams(dp(24), dp(24)).apply { // УСТАНАВЛИВАЕМ РАЗМЕР
                    gravity = Gravity.CENTER_VERTICAL
                    marginEnd = dp(8) // ОТСТУП СПРАВА
                }
                scaleType = ImageView.ScaleType.FIT_CENTER // Масштабирование
                visibility = View.GONE // СКРЫТО ПО УМОЛЧАНИЮ

                // УСТАНАВЛИВАЕМ ИКОНКУ В ЗАВИСИМОСТИ ОТ ТИПА ВЕРЫ
                faith?.let {
                    val drawableResId = when (it.toLowerCase()) {
                        "christian" -> R.drawable.ic_faith_christian
                        "islam" -> R.drawable.ic_faith_islam
                        else -> null
                    }
                    drawableResId?.let { resId ->
                        setImageDrawable(ContextCompat.getDrawable(context, resId))
                        visibility = View.VISIBLE
                    }
                }
            }
            tr.addView(faithIcon)
        } else {
            // ДЛЯ ШАПКИ МЫ ДОБАВИМ ПУСТОЕ МЕСТО, ЧТОБЫ ВЫРАВНЯТЬ СТОЛБЦЫ
            val emptySpace = View(this).apply {
                layoutParams = TableRow.LayoutParams(dp(24), dp(24)).apply {
                    marginEnd = dp(8)
                }
            }
            tr.addView(emptySpace)
        }


        fun cell(text: String, weight: Float, gravity: Int = Gravity.START): TextView {
            return TextView(this).apply {
                this.text = text
                setPadding(dp(4), dp(2), dp(4), dp(2))
                // Ширина по весу (0) и вес — для равномерного распределения
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight)
                if (isHeader) setTypeface(typeface, Typeface.BOLD) // жирный шрифт для шапки
                this.gravity = gravity
            }
        }

        tr.addView(cell(pos,    0.18f, Gravity.CENTER)) // № (центр)
        tr.addView(cell(alias,  0.52f, Gravity.START))  // Псевдоним (влево)
        tr.addView(cell(score,  0.30f, Gravity.END))    // Счёт (вправо)

        table.addView(tr)

        // Добавляем разделитель между строками
        val sep = View(this).apply {
            setBackgroundColor(0x22000000)
            layoutParams = TableLayout.LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT, dp(1)
            )
        }
        table.addView(sep)
    }

    // Конвертация dp → px
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    // Обновление нижнего блока с моими показателями.
    private fun updateMyBlock() {
        binding.myPlace.text = "Место: " + (if (myRank <= 0) "—" else myRank.toString())
        binding.myAlias.text = "Псевдоним: $myAlias"
        binding.myScore.text = "Ваш счёт: ${nf.format(myScore)}"

        // ОБНОВЛЕНИЕ ИКОНКИ ВЕРЫ В БЛОКЕ "МОИ ДАННЫЕ"
        myFaith?.let {
            val drawableResId = when (it.toLowerCase()) {
                "christian" -> R.drawable.ic_faith_christian
                "islam" -> R.drawable.ic_faith_islam
                else -> null
            }
            drawableResId?.let { resId ->
                binding.myFaithIcon.setImageDrawable(ContextCompat.getDrawable(this, resId))
                binding.myFaithIcon.visibility = View.VISIBLE
            } ?: run {
                binding.myFaithIcon.visibility = View.GONE
            }
        } ?: run {
            binding.myFaithIcon.visibility = View.GONE
        }
    }

    // Обновление блока при отсутствии интернет-соединения
    private fun updateMyBlockWithOfflineStatus() {
        binding.myPlace.text = "Место: —"
        binding.myAlias.text = "Псевдоним: (нет сети)"
        binding.myScore.text = "Ваш счёт: —"
        binding.myFaithIcon.visibility = View.GONE // СКРЫВАЕМ ИКОНКУ ПРИ ОТСУТСТВИИ СЕТИ
    }
}
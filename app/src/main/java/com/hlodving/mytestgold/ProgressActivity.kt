package com.hlodving.mytestgold

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.constraintlayout.widget.ConstraintLayout

import com.hlodving.mytestgold.databinding.ActivityProgressBinding
import java.text.NumberFormat
import java.util.Locale

// Таблица топ-7
class ProgressActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProgressBinding
    private val auth = FirebaseAuth.getInstance()
    private val db: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    private var myUid: String? = null
    private var myAlias: String = "—"
    private var myScore: Int = 0
    private var myRank: Int = 0
    private val nf = NumberFormat.getIntegerInstance(Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProgressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            val lp = binding.myBlock.layoutParams as ConstraintLayout.LayoutParams
            val base = (12 * resources.displayMetrics.density).toInt()
            lp.bottomMargin = base + bottomInset
            binding.myBlock.layoutParams = lp
            insets
        }

        myUid = auth.currentUser?.uid

        // Проверяем наличие интернет-соединения
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Если сеть есть, скрываем предупреждение и загружаем данные
            binding.noInternetWarning.visibility = View.GONE
            loadMyInfo {
                updateMyBlock()
                loadMyRank {
                    updateMyBlock()
                    loadTop7AndRender()
                }
            }
        } else {
            // Если сети нет, показываем предупреждение и не загружаем данные
            binding.noInternetWarning.visibility = View.VISIBLE
            // Можно также показать Toast для большей наглядности
            Toast.makeText(this, "Проверьте подключение к интернету", Toast.LENGTH_LONG).show()
            // Очищаем блок с данными пользователя
            updateMyBlockWithOfflineStatus()
        }
    }


     //Загрузка моих данных: alias и score из /users/<uid>
    private fun loadMyInfo(onDone: () -> Unit) {
        val uid = myUid
        if (uid == null) { onDone(); return }
        db.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(s: DataSnapshot) {
                myAlias = s.child("alias").getValue(String::class.java)
                    ?: auth.currentUser?.email ?: "Псевдоним"
                myScore = s.child("globalTapCounter").getValue(Int::class.java) ?: 0
                onDone()
            }
            override fun onCancelled(e: DatabaseError) {
                onDone()
            }
        })
    }


     // Подсчёт моего места в таблице.
    private fun loadMyRank(onDone: () -> Unit) {
        val threshold = myScore + 1.0
        db.orderByChild("globalTapCounter")
            .startAt(threshold)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    myRank = s.childrenCount.toInt() + 1
                    onDone()
                }
                override fun onCancelled(e: DatabaseError) {
                    onDone()
                }
            })
    }


     //Получение топ-7 по счёту и отрисовка таблицы.

    private fun loadTop7AndRender() {
        db.orderByChild("globalTapCounter")
            .limitToLast(7)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(s: DataSnapshot) {
                    val rows = mutableListOf<Triple<String, Int, String>>()
                    s.children.forEach { user ->
                        val alias = user.child("alias").getValue(String::class.java)
                            ?: (user.child("email").getValue(String::class.java) ?: "Пользователь")
                        val score = user.child("globalTapCounter").getValue(Int::class.java) ?: 0
                        val uid = user.key ?: ""
                        rows += Triple(alias, score, uid)
                    }
                    val sorted = rows.sortedByDescending { it.second }
                    renderTable(sorted)
                }
                override fun onCancelled(e: DatabaseError) {
                    Toast.makeText(
                        this@ProgressActivity,
                        "Ошибка загрузки топа: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    //Рендер таблицы (шапка + строки данных) в TableLayout без дополнительных адаптеров.
    private fun renderTable(data: List<Triple<String, Int, String>>) {
        val table = binding.leaderboardTable
        table.removeAllViews()
        addRow(
            table,
            pos = "№",
            alias = "Псевдоним",
            score = "Счёт",
            isHeader = true
        )

        data.forEachIndexed { index, (alias, score, uid) ->
            // Подсвечиваем мою строку
            val isMe = (uid == myUid)
            addRow(
                table,
                pos = (index + 1).toString(),
                alias = alias,
                score = nf.format(score), // форматируем счёт через NumberFormat
                isHeader = false,
                highlight = isMe
            )
        }
    }


     //Утилита добавления строки (шапка или обычная) в таблицу.
    private fun addRow(
        table: TableLayout,
        pos: String,
        alias: String,
        score: String,
        isHeader: Boolean = false,
        highlight: Boolean = false
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


     //Обновление нижнего блока с моими показателями.

    private fun updateMyBlock() {
        binding.myPlace.text = "Место: " + (if (myRank <= 0) "—" else myRank.toString())
        binding.myAlias.text = "Псевдоним: $myAlias"
        binding.myScore.text = "Счёт: ${nf.format(myScore)}"
    }

    private fun updateMyBlockWithOfflineStatus() {
        binding.myPlace.text = "Место: —"
        binding.myAlias.text = "Псевдоним: (нет сети)"
        binding.myScore.text = "Счёт: —"
    }
}

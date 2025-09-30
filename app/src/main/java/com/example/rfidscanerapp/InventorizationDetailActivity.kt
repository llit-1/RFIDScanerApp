package com.example.rfidscanner

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cf.beans.CmdData
import com.cf.ble.interfaces.IOnNotifyCallback
import com.example.rfidscanner.adapter.InventorizationDetailAdapter
import com.example.rfidscanner.model.InventarizationItemModel
import com.example.rfidscanner.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import okhttp3.ResponseBody
import retrofit2.Response

class InventorizationDetailActivity : AppCompatActivity() {
    private var currentStatus: Int = -1
    companion object {
        private const val REQUEST_PERMISSIONS = 123
        private const val HOLD_MS = 3000L
    }

    private lateinit var scanIcon: ImageView
    private lateinit var adapter: InventorizationDetailAdapter
    private lateinit var tagCounter: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var backArrow: ImageView
    private lateinit var statusIcon: ImageView
    private lateinit var statusContainer: FrameLayout

    private var mutableItems: MutableList<InventarizationItemModel> = mutableListOf()
    private var holdAnimator: ValueAnimator? = null
    private var isHoldCancelled: Boolean = false
    private var progressArcView: ProgressArcView? = null

    private val notifyCallback = object : IOnNotifyCallback {
        override fun onNotify(cmdType: Int, cmdData: CmdData?) {}
        override fun onNotify(data: ByteArray?) {
            data?.let {
                if (it.size > 23 && it[3] == 1.toByte()) {
                    val subArray = it.sliceArray(11..22)
                    val epc = subArray.joinToString("") { byte -> "%02X".format(byte) }
                    runOnUiThread { onTagReceived(epc) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventorization_detail)

        tagCounter = findViewById(R.id.tagCounter)
        recyclerView = findViewById(R.id.detailRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        backArrow = findViewById(R.id.backArrow)
        backArrow.setOnClickListener { finish() }

        scanIcon = findViewById(R.id.scanIcon)
        statusIcon = findViewById(R.id.statusIcon)
        val statusContainer = findViewById<FrameLayout>(R.id.statusIconContainer)
        statusIcon = findViewById(R.id.statusIcon)

        // Создаем ProgressArcView
        progressArcView = ProgressArcView(this)
        progressArcView?.visibility = View.GONE

// Добавляем дугу после того, как статусная иконка уже измерена
        statusIcon.post {
            val iconWidth = statusIcon.width
            val iconHeight = statusIcon.height


            val size = (maxOf(iconWidth, iconHeight) * 1.3).toInt()

            val lp = FrameLayout.LayoutParams(size, size, Gravity.CENTER)
            progressArcView?.layoutParams = lp

            // Добавляем дугу в контейнер поверх иконки
            statusContainer.addView(progressArcView)
        }

        BleManager.init(this)
        BleManager.registerNotifyCallback(notifyCallback)
        BleManager.setOnDisconnectedListener {
            runOnUiThread { scanIcon.setColorFilter(Color.RED) }
        }

        val id = intent.getIntExtra("item_id", -1)
        if (id == -1) {
            Toast.makeText(this, "Неверный ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        adapter = InventorizationDetailAdapter(this, mutableItems)
        recyclerView.adapter = adapter

        checkPermissionsAndConnect()
        fetchDetailItems(id)
        updateTagCounter()

        // Инициализация статуса: ожидаем, что в интент придёт extra "status" (int).
        // Если его нет — будет отображаться finish.png (как до этого).
        val initStatus = intent.getIntExtra("status", -1)
        updateStatusIcon(initStatus)

        initStatusTouch()
    }

    private fun initStatusTouch() {
        statusIcon.setOnTouchListener { _, event ->
            // Разрешаем hold только если текущий статус finish
            if (currentStatus != -1 && currentStatus != 2 && currentStatus != 3) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startHold()
                        true
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        cancelHold()
                        true
                    }
                    else -> false
                }
            }
                true
        }
    }

    private fun startHold() {
        isHoldCancelled = false
        progressArcView?.apply {
            progress = 0f
            visibility = View.VISIBLE
            invalidate()
        }

        holdAnimator?.cancel()
        holdAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = HOLD_MS
            addUpdateListener { anim ->
                val v = anim.animatedValue as Float
                progressArcView?.progress = v
                progressArcView?.invalidate()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    if (!isHoldCancelled) {
                        progressArcView?.visibility = View.GONE
                        completeInventorization()
                    } else {
                        progressArcView?.visibility = View.GONE
                    }
                }
                override fun onAnimationCancel(animation: Animator) {
                    isHoldCancelled = true
                    progressArcView?.visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
            start()
        }
    }

    private fun cancelHold() {
        isHoldCancelled = true
        holdAnimator?.cancel()
        progressArcView?.visibility = View.GONE
    }

    private fun fetchDetailItems(id: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.create(this@InventorizationDetailActivity)
                    .getInventorizationItems(id)
                if (response.isSuccessful) {
                    val json = response.body()?.string()
                    val listType = object : TypeToken<List<InventarizationItemModel>>() {}.type
                    val items: List<InventarizationItemModel> = Gson().fromJson(json, listType)

                    mutableItems.clear()
                    mutableItems.addAll(items)
                    sortItems()
                    adapter.notifyDataSetChanged()
                    updateTagCounter()
                } else {
                    Toast.makeText(this@InventorizationDetailActivity, "Ошибка: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@InventorizationDetailActivity, "Ошибка запроса: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkPermissionsAndConnect() {
        val perms = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
        val needed = perms.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), REQUEST_PERMISSIONS)
        } else {
            connectBle()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                connectBle()
            } else {
                Toast.makeText(this, "Нет разрешений для Bluetooth", Toast.LENGTH_SHORT).show()
                scanIcon.setColorFilter(Color.RED)
            }
        }
    }

    private fun connectBle() {
        if (!BleManager.isConnected()) {
            BleManager.connect(this) { success ->
                runOnUiThread {
                    if (success) {
                        scanIcon.setColorFilter(Color.BLACK)
                    } else {
                        scanIcon.setColorFilter(Color.RED)
                        Toast.makeText(this, "Ошибка подключения к сканеру", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            scanIcon.setColorFilter(Color.BLACK)
        }
    }

    private fun onTagReceived(epc: String) {
        val pos = mutableItems.indexOfFirst { it.obj == epc }
        if (pos == -1 || mutableItems[pos].detected) return

        val itemId = mutableItems[pos].id
        mutableItems[pos] = mutableItems[pos].copy(detected = true)
        sortItems()
        adapter.notifyDataSetChanged()
        updateTagCounter()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.create(this@InventorizationDetailActivity).detected(itemId)
                withContext(Dispatchers.Main) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@InventorizationDetailActivity, "Ошибка сервера: ${response.code()}", Toast.LENGTH_SHORT).show()
                        val revertPos = mutableItems.indexOfFirst { it.id == itemId }
                        if (revertPos != -1) {
                            mutableItems[revertPos] = mutableItems[revertPos].copy(detected = false)
                            sortItems()
                            adapter.notifyDataSetChanged()
                            updateTagCounter()
                        }
                    } else {
                        val allDetected = mutableItems.all { it.detected }
                        if (allDetected) {
                            completeInventorization()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InventorizationDetailActivity, "Ошибка запроса: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateTagCounter() {
        val total = mutableItems.size
        val detected = mutableItems.count { it.detected }
        tagCounter.text = "$total($detected)"
    }

    private fun sortItems() {
        // detected = false (ненайденные) сверху, затем найденные; внутри группы — по category
        mutableItems.sortWith(compareBy<InventarizationItemModel> { it.detected }.thenBy { it.objectCategory ?: "" })
    }

    private fun updateStatusIcon(status: Int) {
        currentStatus = status
        when (status) {
            2 -> statusIcon.setImageResource(R.drawable.done)
            3 -> statusIcon.setImageResource(R.drawable.shortage)
            else -> statusIcon.setImageResource(R.drawable.finish)
        }
    }

    /**
     * Унифицированная функция завершения инвентаризации:
     * отправляет запрос done(id) и по ответу меняет иконку:
     *  - 2 -> done.png
     *  - 3 -> short.png
     */
    private fun completeInventorization() {
        val id = intent.getIntExtra("item_id", -1)
        if (id == -1) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<ResponseBody> = RetrofitClient.create(this@InventorizationDetailActivity).done(id)
                withContext(Dispatchers.Main) {
                    if (!response.isSuccessful) {
                        Toast.makeText(this@InventorizationDetailActivity, "Ошибка завершения: ${response.code()}", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }
                    val body = response.body()?.string()?.trim()
                    val statusInt = body?.toIntOrNull()
                    if (statusInt == null) {
                        Toast.makeText(this@InventorizationDetailActivity, "Неверный ответ сервера: $body", Toast.LENGTH_SHORT).show()
                        return@withContext
                    }
                    updateStatusIcon(statusInt)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InventorizationDetailActivity, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.unregisterNotifyCallback(notifyCallback)
        holdAnimator?.cancel()
    }

    /**
     * View, рисующий тонкую дугу вокруг иконки (progress 0..1)
     */
    private class ProgressArcView(context: Context) : View(context) {
        var progress: Float = 0f
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4f * context.resources.displayMetrics.density // ≈2px
            color = context.resources.getColor(R.color.primary)
        }
        private val rect = RectF()

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val desired = (70 * context.resources.displayMetrics.density).toInt()
            val w = resolveSize(desired, widthMeasureSpec)
            val h = resolveSize(desired, heightMeasureSpec)
            setMeasuredDimension(w, h)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val padding = paint.strokeWidth / 2f + 2f
            rect.set(padding, padding, width.toFloat() - padding, height.toFloat() - padding)
            val sweep = 360f * progress
            canvas.drawArc(rect, -90f, sweep, false, paint)
        }
    }
}

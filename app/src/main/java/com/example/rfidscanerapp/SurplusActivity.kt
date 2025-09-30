package com.example.rfidscanner

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cf.beans.CmdData
import com.cf.ble.interfaces.IOnNotifyCallback
import com.example.rfidscanner.adapter.SurplusAdapter
import com.example.rfidscanner.model.SurplusItemModel
import com.example.rfidscanner.network.RetrofitClient
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response

class SurplusActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        private const val REQUEST_PERMISSIONS = 123
    }

    private lateinit var scanIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var autoCompleteLocation: AutoCompleteTextView
    private lateinit var recyclerViewSurplus: RecyclerView
    private lateinit var surplusAdapter: SurplusAdapter

    private val scannedMarks: MutableSet<String> = mutableSetOf()
    private val surplusItems: MutableList<SurplusItemModel> = mutableListOf()
    private val locationsMap = mutableMapOf<String, String>()
    private var selectedLocationGuid: String? = null

    private val notifyCallback = object : IOnNotifyCallback {
        override fun onNotify(cmdType: Int, cmdData: CmdData?) {}

        override fun onNotify(data: ByteArray?) {
            data?.let {
                if (it.size > 23 && it[3] == 1.toByte()) {
                    val subArray = it.sliceArray(11..22)
                    val epc = subArray.joinToString("") { byte -> "%02X".format(byte) }
                    runOnUiThread { handleEpc(epc) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surplus)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Излишки"

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        scanIcon = findViewById(R.id.connectionStatusIcon)
        autoCompleteLocation = findViewById(R.id.autoCompleteLocation)
        recyclerViewSurplus = findViewById(R.id.recyclerViewSurplus)

        surplusAdapter = SurplusAdapter(surplusItems)
        recyclerViewSurplus.layoutManager = LinearLayoutManager(this)
        recyclerViewSurplus.adapter = surplusAdapter

        BleManager.init(this)
        BleManager.registerNotifyCallback(notifyCallback)
        BleManager.setOnDisconnectedListener {
            runOnUiThread {
                scanIcon.setColorFilter(Color.RED)
            }
        }
        loadLocations()
        checkPermissionsAndConnect()

        autoCompleteLocation.setOnItemClickListener { parent, _, position, _ ->
            val selectedName = parent.getItemAtPosition(position) as String
            selectedLocationGuid = locationsMap[selectedName]
            clearScannedMarksAndList()

            Toast.makeText(this@SurplusActivity, "Локация выбрана: $selectedName", Toast.LENGTH_SHORT).show()

            autoCompleteLocation.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(autoCompleteLocation.windowToken, 0)
        }
    }

    private fun loadLocations() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.create(this@SurplusActivity).getAllLocations()
                if (response.isSuccessful) {
                    val locations = response.body()
                    locations?.let {
                        val names = it.map { loc -> loc.name }
                        locationsMap.clear()
                        locationsMap.putAll(it.associate { loc -> loc.name to loc.guid })

                        val adapter = ArrayAdapter(this@SurplusActivity, android.R.layout.simple_dropdown_item_1line, names)
                        autoCompleteLocation.setAdapter(adapter)
                        autoCompleteLocation.threshold = 1
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun checkPermissionsAndConnect() {
        val perms = arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
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

    private fun handleEpc(epc: String) {
        if (epc.isEmpty() || !epc.startsWith("0") || scannedMarks.contains(epc)) return

        val location = selectedLocationGuid
        if (location.isNullOrBlank()) {
            runOnUiThread {
                Toast.makeText(this, "Выберите локацию для проверки", Toast.LENGTH_SHORT).show()
            }
            return
        }

        scannedMarks.add(epc)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response: Response<ResponseBody> = RetrofitClient.create(this@SurplusActivity).standingCheck(epc, location)
                if (response.isSuccessful) {
                    val body = response.body()?.string()
                    if (!body.isNullOrBlank()) {
                        val model = Gson().fromJson(body, SurplusItemModel::class.java)
                        if (!model.onPlace) {
                            withContext(Dispatchers.Main) {
                                surplusItems.add(0, model)
                                surplusAdapter.updateData(surplusItems)
                                recyclerViewSurplus.scrollToPosition(0)
                            }
                        }
                    } else {
                        scannedMarks.remove(epc)
                    }
                } else {
                    scannedMarks.remove(epc)
                }
            } catch (e: Exception) {
                scannedMarks.remove(epc)
            }
        }
    }

    private fun clearScannedMarksAndList() {
        runOnUiThread {
            scannedMarks.clear()
            surplusItems.clear()
            surplusAdapter.updateData(surplusItems)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BleManager.unregisterNotifyCallback(notifyCallback)
        // НЕ отключаем соединение, чтобы не рвать его при переходах между активити
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        drawerLayout.closeDrawers()
        when (item.itemId) {
            R.id.nav_inventory -> startActivity(android.content.Intent(this, MainActivity::class.java))
            R.id.nav_surplus -> { /* Уже здесь */ }
        }
        return true
    }

}
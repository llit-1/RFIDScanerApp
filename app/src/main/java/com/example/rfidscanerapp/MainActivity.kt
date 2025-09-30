package com.example.rfidscanner

import com.example.rfidscanner.SurplusActivity
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rfidscaner.model.CreateInventorizationRequest
import com.example.rfidscaner.model.LocationModel
import com.example.rfidscanner.adapter.InventorizationAdapter
import com.example.rfidscanner.api.ApiService
import com.example.rfidscanner.api.LoginRequest
import com.example.rfidscanner.model.InventorizationItem
import com.example.rfidscanner.network.RetrofitClient
import com.example.rfidscanner.storage.TokenStorage
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*

@RequiresApi(Build.VERSION_CODES.S)
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var apiService: ApiService
    private lateinit var tokenStorage: TokenStorage
    private val macAddress = "E0:4E:7A:F3:77:BB"

    private val requiredPermissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private var selectedLocationGuid: String? = null

    // Для Navigation Drawer
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Настройка Toolbar и Drawer
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Инвентаризация"


        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        recyclerView.layoutManager = LinearLayoutManager(this)

        tokenStorage = TokenStorage(this)
        apiService = RetrofitClient.create(this)

        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val fabAdd = findViewById<AppCompatImageButton>(R.id.fab_add)
        fabAdd.setOnClickListener {
            showCreateInventorizationDialog()
        }

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, requiredPermissions, 1)
        } else {
            startProcess()
        }
    }

    override fun onResume() {
        super.onResume()
        refreshInventorization()
    }

    private fun hasPermissions(): Boolean =
        requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && hasPermissions()) {
            startProcess()
        } else {
            showToast("Необходимы разрешения для Bluetooth")
        }
    }

    private fun startProcess() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            val authorized = loginUser("assassin", "nothing is true everything is permitted")
            if (authorized) {
                showToast("Токен получен")
                getInventorization()
            } else {
                showToast("Ошибка авторизации")
            }
            progressBar.visibility = View.GONE
        }
    }

    private suspend fun loginUser(login: String, password: String): Boolean {
        return try {
            val response = apiService.login(LoginRequest(login, password))
            if (response.isSuccessful) {
                val token = response.body()?.string()
                if (!token.isNullOrEmpty()) {
                    tokenStorage.saveToken(token)
                    true
                } else {
                    showToast("Пустой токен")
                    false
                }
            } else {
                showToast("Ошибка: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            showToast("Ошибка: ${e.message}")
            false
        }
    }

    private suspend fun getInventorization() {
        try {
            val response = apiService.getInventorization()
            if (response.isSuccessful) {
                val json = response.body()?.string()
                if (!json.isNullOrEmpty()) {
                    val gson = Gson()
                    val type = TypeToken.getParameterized(
                        List::class.java,
                        InventorizationItem::class.java
                    ).type
                    val items: List<InventorizationItem> =
                        gson.fromJson(json, type)
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter =
                        InventorizationAdapter(this, items)
                } else {
                    showToast("Пустой ответ от сервера")
                }
            } else {
                showToast("Ошибка загрузки: ${response.code()}")
            }
        } catch (e: Exception) {
            showToast("Ошибка: ${e.message}")
        }
    }

    private fun refreshInventorization() {
        progressBar.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.Main).launch {
            getInventorization()
            progressBar.visibility = View.GONE
        }
    }

    private fun showCreateInventorizationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_inventorization, null)
        val responsibleField = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteResponsible)
        val spinnerLocation = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteLocation)
        val buttonCreate = dialogView.findViewById<Button>(R.id.btn_create)
        val categoryField = dialogView.findViewById<AutoCompleteTextView>(R.id.autoCompleteCategory)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val locationsMap = mutableMapOf<String, String>()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.getAllLocations()
                if (response.isSuccessful) {
                    val locations = response.body()
                    if (locations != null) {
                        val locationNames = locations.map { it.name }
                        locationsMap.putAll(locations.associate { it.name to it.guid })

                        val adapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            locationNames
                        )
                        spinnerLocation.setAdapter(adapter)
                        spinnerLocation.threshold = 1
                        spinnerLocation.setOnItemClickListener { parent, _, position, _ ->
                            val selectedName = parent.getItemAtPosition(position) as String
                            selectedLocationGuid = locationsMap[selectedName]
                        }
                    } else {
                        showToast("Пустой ответ с локациями")
                    }
                } else {
                    showToast("Ошибка получения локаций: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("Ошибка сети: ${e.message}")
            }
        }

        val responsibleMap = mutableMapOf<String, String>()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.getAllHolders()
                if (response.isSuccessful) {
                    val holders = response.body()
                    if (!holders.isNullOrEmpty()) {
                        val holderNames = holders.map { "${it.surname} ${it.name}" }

                        holders.forEach {
                            responsibleMap["${it.surname} ${it.name}"] = it.id.toString()
                        }

                        val adapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            holderNames
                        )
                        responsibleField.setAdapter(adapter)
                        responsibleField.threshold = 1
                    } else {
                        showToast("Список ответственных пуст")
                    }
                } else {
                    showToast("Ошибка загрузки ответственных: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("Ошибка сети: ${e.message}")
            }
        }

        val categoryMap = mutableMapOf<String, Int>()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = apiService.getMainCategories()
                if (response.isSuccessful) {
                    val categories = response.body()
                    if (!categories.isNullOrEmpty()) {
                        val categoryNames = categories.map { it.name }
                        categories.forEach {
                            categoryMap[it.name] = it.id
                        }

                        val adapter = ArrayAdapter(
                            this@MainActivity,
                            android.R.layout.simple_dropdown_item_1line,
                            categoryNames
                        )
                        categoryField.setAdapter(adapter)
                        categoryField.threshold = 1
                    } else {
                        showToast("Список категорий пуст")
                    }
                } else {
                    showToast("Ошибка загрузки категорий: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("Ошибка сети: ${e.message}")
            }
        }

        buttonCreate.setOnClickListener {
            val responsible = responsibleField.text.toString()
            val locationName = spinnerLocation.text.toString()
            val categoryName = categoryField.text.toString().trim()
            val categoryId = categoryMap[categoryName]

            val guid = locationsMap[locationName]

            if (responsible.isBlank() || guid.isNullOrEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            val request = CreateInventorizationRequest(
                location = guid,
                person = responsible,
                warehouseCategoriesId = categoryId
            )

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = apiService.createInventorization(request)
                    if (response.isSuccessful) {
                        showToast("Инвентаризация создана")
                        dialog.dismiss()
                        refreshInventorization()
                    } else {
                        showToast("Ошибка создания: ${response.code()}")
                    }
                } catch (e: Exception) {
                    showToast("Ошибка запроса: ${e.message}")
                }
            }
        }

        dialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Обработка выбора в Navigation Drawer
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_inventory -> {
                drawerLayout.closeDrawers()
                // Уже здесь - ничего не делать
                return true
            }
            R.id.nav_surplus -> {
                drawerLayout.closeDrawers()
                startActivity(Intent(this, SurplusActivity::class.java))
                return true
            }
        }
        return false
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }
}
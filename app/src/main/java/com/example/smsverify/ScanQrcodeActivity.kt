package com.example.smsverify

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.eclipsesource.json.Json
import com.example.smsverify.database.Bank
import com.example.smsverify.database.BankDao
import com.example.smsverify.database.BankDatabase
import com.example.smsverify.databinding.ActivityScanQrcodeBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ninja.sakib.jsonq.JSONQ
import org.json.JSONException
import org.json.JSONObject

private const val CAMERA_REQUEST_CODE = 101

class ScanQrcodeActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private lateinit var binding: ActivityScanQrcodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanQrcodeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermission()
        codeScanner()

        getSettings()
    }

    private fun codeScanner() {
        codeScanner = CodeScanner(this, binding.scannerView)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                if (it.text.isNotEmpty()) {
                    setSettings(it.text)
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "Camera initialization error: ${it.message}")
                }
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermission() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    private fun setSettings(settingString: String) {
        try {
            val settings = JSONObject(settingString)
            GlobalScope.launch {
                DataStoreManager.setValue(
                    this@ScanQrcodeActivity,
                    DataStoreManager.DataKey.SETTINGSTRING.getKey(),
                    settingString
                )
                runOnUiThread {
                    binding.settingTextView.text = settingString
                }
            }

            if (settings.has("to_url")) {
                val toUrl = settings.getString("to_url")
                GlobalScope.launch {
                    DataStoreManager.setValue(
                        this@ScanQrcodeActivity,
                        DataStoreManager.DataKey.TOURL.getKey(),
                        toUrl
                    )
                    runOnUiThread {
                        binding.urlTextView.text = toUrl
                    }
                }
            }

            if (settings.has("banks")) {
                val setting = JSONQ(Json.parse(settingString).asObject())
                val banks = setting.from("banks")
                var bankData = mutableListOf<Bank>()
                banks.forEach {
                    val bankObject = it.asObject()
                    bankObject.get("from").asArray().forEach { fromNumber ->
                        bankData.add(
                            Bank(
                                bankObject.getString("slug", ""),
                                bankObject.getString("name", ""),
                                fromNumber.asString()
                            )
                        )
                    }
                }
                GlobalScope.launch {
                    val bankDao = BankDatabase.getDatabase(this@ScanQrcodeActivity, this).bankDao()
                    bankDao.insertAll(bankData)
                }
            }
        } catch (ex: JSONException) {
            runOnUiThread {
                Toast.makeText(this, "json格式解析失敗", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getSettings() {
        GlobalScope.launch {
            val settingString = DataStoreManager.getStringValue(
                this@ScanQrcodeActivity,
                DataStoreManager.DataKey.SETTINGSTRING.getKey(),
                default = "scanning something..."
            )
            val url = DataStoreManager.getStringValue(
                this@ScanQrcodeActivity,
                DataStoreManager.DataKey.TOURL.getKey()
            )

            runOnUiThread {
                binding.settingTextView.text = settingString
                binding.urlTextView.text = url
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "You need the camera permission to be able to use this app!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
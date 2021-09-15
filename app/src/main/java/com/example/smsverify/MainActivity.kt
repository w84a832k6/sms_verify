package com.example.smsverify

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smsverify.adapter.BankListAdapter
import com.example.smsverify.databinding.ActivityMainBinding
import com.example.smsverify.viewModels.BankViewModel
import com.example.smsverify.viewModels.BankViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

private const val SMS_REQUEST_CODE = 101
private const val PHONE_NUMBER_REQUEST_CODE = 102
private const val INTERNET_REQUEST_CODE = 104
private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val bankViewModel: BankViewModel by viewModels {
        BankViewModelFactory((application as MessageApplication).bankRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermission()

        binding.resetNumberButton.setOnClickListener {
            setPhoneNumber()
        }
        binding.appVersionTextView.text = BuildConfig.VERSION_NAME
        binding.connectStatusTextView.text = getString(R.string.connect_offline)
        binding.connectStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.red))

        val bankAdapter = BankListAdapter()
        binding.bankSettingRecyclerView.adapter = bankAdapter

        bankViewModel.allBank.observe(this) { banks ->
            banks.let {
                bankAdapter.submitList(it)
            }
        }

        binding.connectTestButton.setOnClickListener {
            //test connect
            GlobalScope.launch {
                DataStoreManager.setValue(
                    this@MainActivity,
                    DataStoreManager.DataKey.CONNECT_STATUS.getKey(),
                    true
                )
                DataStoreManager.setValue(
                    this@MainActivity,
                    DataStoreManager.DataKey.CONNECT_TIMESTAMP.getKey(),
                    System.currentTimeMillis()
                )
            }
            binding.connectStatusTextView.text = getString(R.string.connect_online)
            binding.connectStatusTextView.setTextColor(ContextCompat.getColor(this, R.color.green))
        }

        //定時判斷連線狀態
        Timer().schedule(object : TimerTask() {
            override fun run() {
                GlobalScope.launch {
                    val connectStatus = DataStoreManager.getBooleanValue(
                        this@MainActivity,
                        DataStoreManager.DataKey.CONNECT_STATUS.getKey(), false
                    )
                    val connectTimestamp = DataStoreManager.getLongValue(
                        this@MainActivity,
                        DataStoreManager.DataKey.CONNECT_TIMESTAMP.getKey(), 0
                    )
                    val difference = (System.currentTimeMillis() - connectTimestamp) / (1000)
                    if (difference < 600 && connectStatus) {
                        runOnUiThread {
                            binding.connectStatusTextView.text = getString(R.string.connect_online)
                            binding.connectStatusTextView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.green
                                )
                            )
                        }
                    } else {
                        runOnUiThread {
                            binding.connectStatusTextView.text = getString(R.string.connect_offline)
                            binding.connectStatusTextView.setTextColor(
                                ContextCompat.getColor(
                                    this@MainActivity,
                                    R.color.red
                                )
                            )
                        }
                    }
                }
            }
        }, Date(), 60000)
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch {
            val phoneNumber = DataStoreManager.getStringValue(
                this@MainActivity,
                DataStoreManager.DataKey.PHONE_NUMBER.getKey()
            )
            GlobalScope.launch(Dispatchers.Main) {
                binding.phoneNumberTextView.text = phoneNumber
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.scanQrButton -> {
                val intent = Intent(this, ScanQrcodeActivity::class.java)
                this.startActivity(intent)
                return true
            }
            R.id.messageList -> {
                val intent = Intent(this, MessageListActivity::class.java)
                this.startActivity(intent)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setPhoneNumber() {
        when {
            (checkPermission(android.Manifest.permission.READ_PHONE_NUMBERS)) -> {
                makeRequest(android.Manifest.permission.READ_PHONE_NUMBERS)
            }
            else -> {
                val tMgr = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                val phoneNumber = tMgr.line1Number
                GlobalScope.launch {
                    DataStoreManager.setValue(
                        this@MainActivity,
                        DataStoreManager.DataKey.PHONE_NUMBER.getKey(),
                        phoneNumber
                    )
                }
                binding.phoneNumberTextView.text = phoneNumber
            }
        }
    }

    private fun setupPermission() {
        if (checkPermission(android.Manifest.permission.READ_SMS)) {
            makeRequest(android.Manifest.permission.READ_SMS)
        }
        if (checkPermission(android.Manifest.permission.RECEIVE_SMS)) {
            makeRequest(android.Manifest.permission.RECEIVE_SMS)
        }
        if (checkPermission(android.Manifest.permission.READ_PHONE_NUMBERS)) {
            makeRequest(android.Manifest.permission.READ_PHONE_NUMBERS)
        }
        if (checkPermission(android.Manifest.permission.INTERNET)) {
            makeRequest(android.Manifest.permission.INTERNET)
        }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun makeRequest(permission: String) {
        when (permission) {
            android.Manifest.permission.READ_SMS -> {
                ActivityCompat.requestPermissions(this, arrayOf(permission), SMS_REQUEST_CODE)
            }
            android.Manifest.permission.RECEIVE_SMS -> {
                ActivityCompat.requestPermissions(this, arrayOf(permission), SMS_REQUEST_CODE)
            }
            android.Manifest.permission.READ_PHONE_NUMBERS -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    PHONE_NUMBER_REQUEST_CODE
                )
            }
            android.Manifest.permission.INTERNET -> {
                ActivityCompat.requestPermissions(this, arrayOf(permission), INTERNET_REQUEST_CODE)
            }
            android.Manifest.permission.INTERACT_ACROSS_PROFILES -> {
                ActivityCompat.requestPermissions(this, arrayOf(permission), INTERNET_REQUEST_CODE)
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
            SMS_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "You need the sms permission to be able to use this app!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            PHONE_NUMBER_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "You need the phone number permission to be able to use this app!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            INTERNET_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "You need the internet permission to be able to use this app!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
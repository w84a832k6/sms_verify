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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recyclerview_item.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val SMS_REQUEST_CODE = 101
private const val PHONE_NUMBER_REQUEST_CODE = 102

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermission()

        resetNumber_button.setOnClickListener {
            setPhoneNumber()
        }
    }

    override fun onStart() {
        super.onStart()

        GlobalScope.launch {
            val phoneNumber = DataStoreManager.getStringValue(this@MainActivity, "phoneNumber")
            GlobalScope.launch(Dispatchers.Main) {
                phoneNumber_textView.text = phoneNumber
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
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_NUMBERS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_PHONE_NUMBERS),
                PHONE_NUMBER_REQUEST_CODE
            )
        } else {
            val tMgr = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val phoneNumber = tMgr.line1Number
            GlobalScope.launch {
                DataStoreManager.setValue(this@MainActivity, "phoneNumber", phoneNumber)
            }
            phoneNumber_textView.text = phoneNumber
        }
    }

    private fun setupPermission() {
        val readPermission =
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)
        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.RECEIVE_SMS,
                android.Manifest.permission.READ_PHONE_NUMBERS
            ),
            SMS_REQUEST_CODE
        )
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
        }
    }
}
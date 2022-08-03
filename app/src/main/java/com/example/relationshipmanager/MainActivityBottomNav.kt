package com.example.relationshipmanager

import android.app.*
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Color.*
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.widget.RelativeLayout.LayoutParams
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.marginBottom
import androidx.preference.PreferenceManager
import com.example.relationshipmanager.activities.WelcomeActivity
import com.example.relationshipmanager.adapters.EventAdapter
import com.example.relationshipmanager.databinding.ActivityMainBinding
import com.example.relationshipmanager.databinding.DialogInsertEventBinding
import com.example.relationshipmanager.fragments.SettingsFragment
import com.example.relationshipmanager.viewmodels.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.apache.poi.ss.usermodel.*
import org.openxmlformats.schemas.drawingml.x2006.main.STPresetColorVal.BLACK
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException


@ExperimentalStdlibApi
class MainActivityBottomNav : AppCompatActivity() {

    val WRITE_RQ = 101
    val READ_RQ = 102

    //lateinit var mainViewModel: MainViewModel
    public val mainViewModel: MainViewModel by viewModels()
    private lateinit var adapter: EventAdapter
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var binding: ActivityMainBinding
    private var _dialogInsertEventBinding: DialogInsertEventBinding? = null
    val dialogInsertEventBinding get() = _dialogInsertEventBinding!!
    private lateinit var resultLauncher: ActivityResultLauncher<String>
    public var imageChosen = false
    companion object {
        var rowNr = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        adapter = EventAdapter(null)
        // Initialize the result launcher to pick the image
        resultLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                // Handle the returned Uri
                if (uri != null) {
                    imageChosen = true
                    ////setImage(uri)
                }
            }

        // Retrieve the shared preferences
        val theme = sharedPrefs.getString("theme_color", "system")
        val accent = sharedPrefs.getString("accent_color", "aqua")

        // Show the introduction for the first launch
        if (sharedPrefs.getBoolean("first", true)) {
            val editor = sharedPrefs.edit()
            editor.putBoolean("first", false)
            editor.apply()
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        // Set the task appearance in recent apps
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setTaskDescription(
                ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    R.mipmap.ic_launcher,
                    ContextCompat.getColor(this, R.color.deepGray)
                )
            )
        } else setTaskDescription(
            ActivityManager.TaskDescription(
                getString(R.string.app_name),
                ContextCompat.getDrawable(this, R.mipmap.ic_launcher)?.toBitmap(),
                ContextCompat.getColor(this, R.color.deepGray)
            )
        )
        // Create the notification channel and check the permission (note: appIntro 6.0 is still buggy, better avoid to use it for asking permissions)
        ////askContactsPermission()
        createNotificationChannel()

        //call super function and create View
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test)

        //create BottomNavigationView object, open Start Fragment
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, StartFragment()).commit()

        //switch Fragment Views based on clicking Bottom Navigation Items
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_start -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, StartFragment()).commit()
                }
                R.id.nav_contacts -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ContactsFragment()).commit()
                }
                R.id.nav_add_person -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, AddPersonFragment()).commit()
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SettingsFragment()).commit()
                }
            }
            return@setOnItemSelectedListener true
        }

        mainViewModel.scheduleNextCheck()
    }


    // Create the NotificationChannel. This code does nothing when it already exists
    private fun createNotificationChannel() {
        val soundUri =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + applicationContext.packageName + "/" + R.raw.birday_notification)
        val attributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()
        val name = getString(R.string.events_notification_channel)
        val descriptionText = getString(R.string.events_channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("events_channel", name, importance).apply {
            description = descriptionText
        }
        // Additional tuning over sound, vibration and notification light
        channel.setSound(soundUri, attributes)
        channel.enableLights(true)
        channel.lightColor = Color.GREEN
        channel.enableVibration(true)
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    //
    //Permission Stuff
    //
    private fun buttonTaps() {
        val btnWritePerm = findViewById<Button>(R.id.buttonWritePerm)
        val btnReadPerm = findViewById<Button>(R.id.buttonReadPerm)

        btnWritePerm.setOnClickListener() {
            checkForPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, "write", WRITE_RQ)
        }
        btnReadPerm.setOnClickListener() {
            checkForPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE, "read", READ_RQ)
        }
    }

    private fun checkForPermissions(permission: String, name: String, requestCode: Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            when{
                ContextCompat.checkSelfPermission(applicationContext, permission) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(applicationContext, "$name permission granetd", Toast.LENGTH_SHORT).show()
                }
                shouldShowRequestPermissionRationale(permission) -> showDialog(permission, name, requestCode)

                else -> ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        fun innerCheck(name: String) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "$name permission refused", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "$name permission granted", Toast.LENGTH_SHORT).show()
            }
        }

        when (requestCode) {
            WRITE_RQ -> innerCheck("write")
            READ_RQ -> innerCheck("read")
        }
    }

    private fun showDialog(permission: String, name: String, requestCode: Int){
        val builder = AlertDialog.Builder(this)

        builder.apply {
            setMessage("Permission to access your $name is required to use this app")
            setTitle("Permission required")
            setPositiveButton("OK") { dialog, which ->
                ActivityCompat.requestPermissions(this@MainActivityBottomNav, arrayOf(permission), requestCode)
            }
        }
        val dialog = builder.create()
        dialog.show()
    }
}
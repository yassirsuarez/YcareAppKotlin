package com.example.navigation

import android.os.Bundle
import android.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.Manifest
import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private val viewModel: AppuntamentiViewModel by viewModels()
    val CHANNEL_ID="ch1"
    private lateinit var navController: NavController
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//logout
         lateinit var auth: FirebaseAuth
         lateinit var user: FirebaseUser

        auth=FirebaseAuth.getInstance()
        if(auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
//tollbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
//fragment
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navController = navHostFragment.navController

        navController.navigate(R.id.home)
//bottomNavigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.navigation_home
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem->
            when(menuItem.itemId){
                R.id.navigation_home->{
                    navController.navigate(R.id.home)
                    true
                }
                R.id.navigation_medicine->{
                    navController.navigate(R.id.medicine)
                    true
                }
                R.id.navigation_storico->{
                    navController.navigate(R.id.storico)
                    true
                }
                R.id.navigation_appuntamenti->{
                    navController.navigate(R.id.appuntamenti)
                    true
                }
                else ->false
            }
        }
//menu scorrevole
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener {menuItem ->
            when( menuItem.itemId){
                R.id.prima->{ val intent=Intent(this,Profilo::class.java)
                    startActivity(intent)
                    true
                }
                R.id.seconda2->{
                   FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                    true
                }
                else ->false
            }

        }

        scheduleDailyWork()

        val userId = auth.currentUser?.uid.toString()
        viewModel.numeroAppuntamentiOggi(userId)

        createNotificationChannel()
        if(ActivityCompat.checkSelfPermission(
            applicationContext,Manifest.permission.POST_NOTIFICATIONS
        )!= PackageManager.PERMISSION_GRANTED
            ){
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }else {
            viewModel.numeroAppuntamentiOggi.observe(this, Observer { numero ->
                showNotification(numero)
            })

        }
    }

    //menu sopra
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.phone -> {
                val dialIntent = Intent(Intent.ACTION_DIAL)
                dialIntent.data = Uri.parse("tel:118")
                startActivity(dialIntent)
                true
            }

            R.id.info -> {
                showInfoDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }
    private fun showInfoDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Informazioni sull'app")
        alertDialogBuilder.setMessage("Y-Care è l'app ideale per gestire la tua salute in modo semplice e organizzato. Con Ycare, puoi monitorare le tue visite mediche, tenere traccia dei tuoi farmaci e gestire i tuoi dati sanitari. Inoltre, facilita il contatto diretto con il tuo medico curante, rendendo la comunicazione più facile e veloce.")

        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private val notificationPermissionLauncher=registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){result:Boolean ->
        if(result){
            viewModel.numeroAppuntamentiOggi.observe(this, Observer { numero ->
                showNotification(numero)
            })
        }
        else {
            Toast.makeText(
                this@MainActivity,
                "Permesso notifiche non garantito" ,
                        Toast.LENGTH_SHORT
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification(numero: Int){
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Benvenuto")
            .setContentText("Oggi hai ${numero} visite mediche")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        with(NotificationManagerCompat.from(this)) {
            notify(1, builder.build())
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "My Chanel"
            val descriptionText = "descrizione canale"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun scheduleDailyWork() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyFirebaseUpdateWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyFirebaseUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )

        Log.d("MainActivity", "Daily work scheduled")
    }

}




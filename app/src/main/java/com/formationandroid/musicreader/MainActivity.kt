package com.formationandroid.musicreader

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {
    companion object {
        var isChecked: Boolean = false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        if (requestCode == 123)
        {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Initialize the listview with data
                this.setAudioModels(false)
            }
            else
            {
                // Tell the user he need to change autorisation if he want to use the application
                Toast.makeText(this, getString(R.string.toast_no_permission), Toast.LENGTH_LONG).show()

                // Initialize the listview with data
                this.setAudioModels(false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // For perf :
        list_audio.setHasFixedSize(true)

        // Ask the permission to some folder
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Initialize the listview with data
            this.setAudioModels(false)
        }
        else
        {
            // affichage de la popup de demande de permission :
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                123)
        }

        // Set up group managment for notifications
        val manager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(
                "music_group"
                ,
                "Music",
                NotificationManager.IMPORTANCE_HIGH)
            channel.description = "A group of notifications for music"
            channel.enableLights(true)
            channel.lightColor = Color.GREEN
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(300, 200, 300)
            manager.createNotificationChannel(channel)
        }
    }

    fun start(view: View) {
        val intent = Intent(this, LecteurService::class.java)
        intent.putExtra(LecteurService.EXTRA_COMMANDE, LecteurService.COMMANDE_PLAY)
        intent.putExtra("needDb", isChecked)
        startService(intent)
        Toast.makeText(this@MainActivity, this.getString(R.string.toast_play), Toast.LENGTH_LONG).show()
    }

    fun pause(view: View)
    {
        // lancement commande pause :
        val intent = Intent(this, LecteurService::class.java)
        intent.putExtra(LecteurService.EXTRA_COMMANDE, LecteurService.COMMANDE_PAUSE)
        intent.putExtra("needDb", isChecked)
        startService(intent)
        Toast.makeText(this@MainActivity, this.getString(R.string.toast_pause), Toast.LENGTH_LONG).show()
    }

    fun stop(view: View) {
        val intent = Intent(this, LecteurService::class.java)
        intent.putExtra(LecteurService.EXTRA_COMMANDE, LecteurService.COMMANDE_STOP)
        intent.putExtra("needDb", isChecked)
        startService(intent)
        Toast.makeText(this@MainActivity, this.getString(R.string.toast_stop), Toast.LENGTH_LONG).show()
    }

    fun switchFav(view: View) {
        // OnClick method called when you click on the switch button
        isChecked = view.favoris.isChecked
        this.setAudioModels(isChecked)
    }

    private fun setAudioModels(needDb: Boolean) {
        val layoutManager = LinearLayoutManager(this)
        list_audio.layoutManager = layoutManager

        val listAudioModel = if (needDb) {
            LecteurService.getAllAudioFromDb(this);
        }
        else {
            LecteurService.getAllAudioFromDevice(this);
        }

        // Setting the adapter
        val audioModelsAdapter = AudioModelsAdapter(listAudioModel)
        list_audio.adapter = audioModelsAdapter

        // Notify the view that the list changed
        audioModelsAdapter.notifyDataSetChanged()
    }
}
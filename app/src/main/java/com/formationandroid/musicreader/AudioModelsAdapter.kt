package com.formationandroid.musicreader

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.formationandroid.musicreader.LecteurService.Companion.COMMANDE_PAUSE
import com.formationandroid.musicreader.LecteurService.Companion.COMMANDE_PLAY
import com.formationandroid.musicreader.LecteurService.Companion.EXTRA_COMMANDE
import com.formationandroid.musicreader.db.AppDatabaseHelper
import com.formationandroid.musicreader.db.AudioModelDTO

class AudioModelsAdapter(private var listAudioModel: MutableList<AudioModelDTO>) : RecyclerView.Adapter<AudioModelsAdapter.AudioModelViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioModelsAdapter.AudioModelViewHolder {
        val viewAudioModel = LayoutInflater.from(parent.context).inflate(R.layout.item_audiomodel, parent, false)
        return AudioModelViewHolder(viewAudioModel)
    }

    override fun getItemCount(): Int {
        return listAudioModel.size
    }

    override fun onBindViewHolder(holder: AudioModelsAdapter.AudioModelViewHolder, position: Int) {
        holder.audioModelName.text = listAudioModel[position].name
        holder.audioModelSize.text = "${listAudioModel[position].size} Mo"
        holder.audioModelTime.text = listAudioModel[position].duration
        holder.audioModelUri.text = listAudioModel[position].uri
        holder.audioModelId.text = listAudioModel[position].id.toString()
    }

    inner class AudioModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        // Getting reference of what we need to display informations correctly
        var audioModelName: TextView = itemView.findViewById(R.id.audio_model_name)
        var audioModelSize: TextView = itemView.findViewById(R.id.audio_model_size)
        var audioModelTime: TextView = itemView.findViewById(R.id.audio_model_time)
        var audioModelUri: TextView = itemView.findViewById(R.id.audio_model_uri)
        var audioModelId: TextView = itemView.findViewById(R.id.audio_model_id)
        var button: ImageButton = itemView.findViewById(R.id.button)

        init
        {
            // Set up what happen when we click on the item
            itemView.setOnClickListener {
                val intent = Intent(it.context, LecteurService::class.java)
                intent.putExtra(LecteurService.EXTRA_COMMANDE, LecteurService.COMMANDE_PLAY)
                intent.putExtra("position", adapterPosition)
                intent.putExtra("needDb", MainActivity.isChecked)
                it.context.startService(intent)
                Toast.makeText(it.context, "Musique en cours de lecture", Toast.LENGTH_LONG).show()

                val manager = ContextCompat.getSystemService(
                    it.context,
                    NotificationManager::class.java
                ) as NotificationManager

                var remoteView: RemoteViews = RemoteViews(it.context.packageName, R.layout.custom_notification)
                val intentPause = Intent(it.context, LecteurService::class.java)
                intentPause.putExtra(EXTRA_COMMANDE, COMMANDE_PAUSE)
                intentPause.putExtra("needDb", MainActivity.isChecked)
                val pendingIntentPause = PendingIntent.getService(it.context, 1, intentPause, PendingIntent.FLAG_UPDATE_CURRENT)
                remoteView.setOnClickPendingIntent(R.id.pause_notification, pendingIntentPause)

                val intentStart = Intent(it.context, LecteurService::class.java)
                intentStart.putExtra(EXTRA_COMMANDE, COMMANDE_PLAY)
                intentStart.putExtra("needDb", MainActivity.isChecked)
                val pendingIntentStart = PendingIntent.getService(it.context, 0, intentStart, PendingIntent.FLAG_UPDATE_CURRENT)
                remoteView.setOnClickPendingIntent(R.id.play_notification, pendingIntentStart)

                // construction notification :
                val builder = NotificationCompat.Builder(it.context,
                    "music_group")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("Ma notification")
                    .setAutoCancel(false)
                    .setContentText("Bienvenue !")
                    .setCustomContentView(remoteView)
                    .setCustomBigContentView(remoteView)

                // lancer la notif
                manager.notify(1234, builder.build())
            }

            // Set up what happen when we click on the fav button
            button.setOnClickListener {
                // Check if the object is not already added to your fav
                if (AppDatabaseHelper.getDatabase(it.context).audioModelsDAO().countAudioModelByURI(audioModelUri.text.toString()) == 0.toLong()) {
                    // Insert into database
                    AppDatabaseHelper.getDatabase(it.context).audioModelsDAO().insert(
                        AudioModelDTO(
                            0,
                            audioModelName.text.toString(),
                            audioModelSize.text.toString().split(" ")[0].toFloat(),
                            audioModelTime.text.toString(),
                            audioModelUri.text.toString()
                        )
                    )

                    // Tell the user what happened
                    Toast.makeText(it.context, itemView.context.getString(R.string.toast_add_fav), Toast.LENGTH_LONG).show()
                }
                else {
                    // Tell the user what happened
                    Toast.makeText(it.context, itemView.context.getString(R.string.toast_add_fav_already_exist), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
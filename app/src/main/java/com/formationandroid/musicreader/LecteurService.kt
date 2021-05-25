package com.formationandroid.musicreader

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import com.formationandroid.musicreader.db.AppDatabaseHelper
import com.formationandroid.musicreader.db.AudioModelDTO

class LecteurService : Service()
{
    // Static block :
    companion object
    {
        // Constant :
        const val EXTRA_COMMANDE = "EXTRA_COMMANDE"
        const val COMMANDE_PLAY = "COMMANDE_PLAY"
        const val COMMANDE_PAUSE = "COMMANDE_PAUSE"
        const val COMMANDE_STOP = "COMMANDE_STOP"

        // Used to track the last position
        private var currentPosition: Int = -1

        // Used to know if the last button we pressed is stop
        private var hasPressedStop: Boolean = false;

        private fun formatDate(duration: Float?): String {
            if (duration != null && duration != 0.0f) {
                val minutes : Int = ((duration % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                val seconds = (duration % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
                return "$minutes:$seconds"
            }

            return "";
        }

        fun getAllAudioFromDevice(context: Context): MutableList<AudioModelDTO> {
            val tempAudioList: MutableList<AudioModelDTO> = ArrayList()

            // Define where we want to search
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            // Set up which information we want
            val projection = arrayOf(
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.DATA
            )

            // Get what we want where we want
            val c = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                null
            )

            if (c != null) {
                // Since it's a cursor we need to move it when we read it
                while (c.moveToNext()) {
                    val title: String? = c.getString(0)
                    val size: Float? = c.getFloat(1)
                    val duration: Float? = c.getFloat(2)
                    val uri: String? = c.getString(3)

                    var finalDuration = formatDate(duration);

                    // Get the extension
                    val extension = uri?.substring(uri?.lastIndexOf("."))

                    if (extension == ".mp3" && title != null && size != null && uri != null) {
                        val audioModel = AudioModelDTO(0, title, size / 1000000, finalDuration, uri)
                        tempAudioList.add(audioModel)
                    }
                }
                c.close()
            }
            return tempAudioList
        }

        fun getAllAudioFromDb(context: Context): MutableList<AudioModelDTO> {
            return AppDatabaseHelper.getDatabase(context).audioModelsDAO().getListAudioModels().toMutableList()
        }
    }

    // MediaPlayer :
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate()
    {
        // init :
        super.onCreate()
        this.initialiseMediaPlayer();
    }

    private fun initialiseMediaPlayer() {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener {
            // When the music end we start the next one
            val intent = Intent(this, LecteurService::class.java)
            intent.putExtra(LecteurService.EXTRA_COMMANDE, LecteurService.COMMANDE_PLAY)
            intent.putExtra("position", currentPosition + 1)
            intent.putExtra("needDb", MainActivity.isChecked)
            startService(intent)
        }
    }

    private fun prepareMediaPlayer(needDb: Boolean, position: Int) {
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
        this.initialiseMediaPlayer();
        var uri: Uri = Uri.EMPTY;
        var audioModels : MutableList<AudioModelDTO> = ArrayList()

        audioModels = if(needDb) {
            getAllAudioFromDb(this)
        } else {
            getAllAudioFromDevice(this)
        }

        uri = if (position == audioModels.size) {
            Uri.parse(audioModels[0].uri);
        } else {
            Uri.parse(audioModels[position].uri);
        }

        mediaPlayer.setDataSource(this, uri);
        mediaPlayer.prepare();
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        if (intent?.hasExtra(EXTRA_COMMANDE) == true)
        {
            var needDb: Boolean = intent.getBooleanExtra("needDb", false);
            var position : Int = intent.getIntExtra("position", -1);
            if (position != -1) {
                this.prepareMediaPlayer(needDb, position)
                currentPosition = position;
            }

            when (intent.getStringExtra(EXTRA_COMMANDE))
            {
                COMMANDE_PLAY -> {
                    if (currentPosition != -1 && hasPressedStop){
                        this.prepareMediaPlayer(needDb, currentPosition)
                    }

                    hasPressedStop = false;
                    mediaPlayer.start()
                    // notification
                }
                COMMANDE_PAUSE -> {
                    // pause :
                    hasPressedStop = false;
                    mediaPlayer.pause()
                }
                COMMANDE_STOP -> {
                    // stop :
                    hasPressedStop = true;
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy()
    {
        super.onDestroy()

        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
    }
}
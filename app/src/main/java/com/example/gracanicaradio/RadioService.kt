import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.gracanicaradio.MainActivity
import com.example.gracanicaradio.R

class RadioService : Service() {

    private lateinit var mediaPlayer: MediaPlayer

    companion object {
        const val CHANNEL_ID = "RadioServiceChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        // Load your radio stream or media here
        mediaPlayer.setDataSource("YOUR_STREAM_URL")
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle the actions for pause and stop
        when (intent?.action) {
            ACTION_PAUSE -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    updateNotification("Paused") // Update notification to reflect paused state
                }
            }
            ACTION_STOP -> {
                stopForeground(true)
                stopSelf() // Stop the service and remove the notification
            }
        }

        // If no action is specified, continue showing the notification and media
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing Radio")
            .setContentText("Gracanica Radio Station")
            .setSmallIcon(R.drawable.radio_gracanica_logo) // Use your radio icon here
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.pause_button, "Pause", getActionIntent(ACTION_PAUSE)) // Use pause icon
            .addAction(R.drawable.play_img, "Stop", getActionIntent(ACTION_STOP)) // Use stop icon (play_img here)
            .setOngoing(true) // Makes the notification non-dismissible
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_NOT_STICKY
    }

    // Helper function to handle actions
    private fun getActionIntent(action: String): PendingIntent {
        val intent = Intent(this, RadioService::class.java)
        intent.action = action
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    // Helper function to update the notification state (e.g., when paused)
    private fun updateNotification(status: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playing Radio")
            .setContentText(status)
            .setSmallIcon(R.drawable.radio_gracanica_logo) // Still use the logo
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.play_img, "Play", getActionIntent(ACTION_PAUSE)) // Use play icon for resume
            .addAction(R.drawable.pause_button, "Stop", getActionIntent(ACTION_STOP)) // Use stop icon for pause
            .setOngoing(true)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification) // Update the existing notification
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Radio Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
        super.onDestroy()
    }
}

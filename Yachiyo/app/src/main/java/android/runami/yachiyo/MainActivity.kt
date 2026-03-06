package android.runami.yachiyo

import android.media.MediaPlayer
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import java.util.Random

class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private var mediaPlayer: MediaPlayer? = null
    private val random = Random()
    private var isPlayingRandom = false
    private var isSurfaceReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        hideSystemUI()

        surfaceView = findViewById(R.id.videoView)
        surfaceView.holder.addCallback(this)

        findViewById<View>(R.id.main_container).setOnClickListener {
            if (!isPlayingRandom) {
                playRandomVideo()
            }
        }
    }

    private fun playAssetVideo(fileName: String, isLooping: Boolean) {
        if (!isSurfaceReady) return
        
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
            } else {
                mediaPlayer?.reset()
            }

            val afd = assets.openFd(fileName)
            mediaPlayer?.apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                setDisplay(surfaceView.holder)
                setLooping(isLooping)
                
                setOnPreparedListener { mp ->
                    mp.start()
                }
                
                setOnCompletionListener {
                    if (isPlayingRandom) {
                        isPlayingRandom = false
                        playAssetVideo("0.mp4", true)
                    } else if (fileName == "Null.mp4") {
                        playAssetVideo("0.mp4", true)
                    }
                }
                
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (fileName != "0.mp4") {
                playAssetVideo("0.mp4", true)
            }
        }
    }

    private fun playRandomVideo() {
        isPlayingRandom = true
        val randomIndex = random.nextInt(22) + 1 
        playAssetVideo("$randomIndex.mp4", false)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        isSurfaceReady = true
        playAssetVideo("Null.mp4", false)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        isSurfaceReady = false
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

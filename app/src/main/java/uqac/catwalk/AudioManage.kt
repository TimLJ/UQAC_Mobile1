package uqac.catwalk

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes

object SoundPlayer {
    private const val TAG = "SoundPlayer"
    private var player: MediaPlayer? = null
    private var currentResId: Int = 0

    /**
     * Démarre le son par resId (préférable).
     */
    fun start(context: Context, @RawRes resId: Int, loop: Boolean = false) {
        val appCtx = context.applicationContext
        if (resId == 0) {
            Log.w(TAG, "start: invalid resId=0")
            return
        }

        synchronized(this) {
            if (player?.isPlaying == true && currentResId == resId) {
                Log.d(TAG, "start: same sound already playing resId=$resId")
                return
            }

            // Libère l'ancien player
            player?.let {
                try { if (it.isPlaying) it.stop() } catch (_: Exception) { }
                try { it.release() } catch (_: Exception) { }
            }
            player = null
            currentResId = 0

            try {
                player = MediaPlayer.create(appCtx, resId)?.apply {
                    isLooping = loop
                    setVolume(1f, 1f)
                    if (!loop) {
                        setOnCompletionListener {
                            try { it.release() } catch (_: Exception) { }
                            synchronized(this@SoundPlayer) {
                                player = null
                                currentResId = 0
                            }
                        }
                    }
                    start()
                }

                if (player != null) {
                    currentResId = resId
                    Log.d(TAG, "start: started resId=$resId loop=$loop")
                } else {
                    Log.w(TAG, "start: MediaPlayer.create returned null for resId=$resId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "start: failed to create/start player", e)
                player = null
                currentResId = 0
            }
        }
    }

    /**
     * Démarre le son par nom de res/raw (ex: "ronronnement").
     */
    @SuppressLint("DiscouragedApi")
    fun start(context: Context, resName: String, loop: Boolean = false) {
        val appCtx = context.applicationContext
        val resId = appCtx.resources.getIdentifier(resName, "raw", appCtx.packageName)
        if (resId == 0) {
            Log.w(TAG, "start: resource not found: $resName")
            return
        }
        start(appCtx, resId, loop)
    }

    /**
     * Arrête et libère le player s'il existe.
     */
    fun stop() {
        synchronized(this) {
            player?.let {
                try { if (it.isPlaying) it.stop() } catch (_: Exception) { }
                try { it.release() } catch (_: Exception) { }
            }
            player = null
            currentResId = 0
            Log.d(TAG, "stop: player released")
        }
    }
}

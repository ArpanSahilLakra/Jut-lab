package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import com.example.R
import com.example.data.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppAudioManager(private val context: Context, private val preferences: AppPreferences) {

    private var soundPool: SoundPool? = null
    private var isLoaded = false
    private val soundMap = mutableMapOf<Int, Int>()

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) isLoaded = true
        }

        // Example resource loading - gracefully handles missing files
        // loadSoundSafely(SoundType.CLICK, R.raw.ui_click)
        // loadSoundSafely(SoundType.SUCCESS, R.raw.ui_success)
        // etc.
    }

    private fun loadSoundSafely(type: SoundType, resId: Int) {
        try {
            soundMap[type.ordinal] = soundPool?.load(context, resId, 1) ?: 0
        } catch (e: Exception) {
            // File not found, ignore
        }
    }

    enum class SoundType {
        CLICK, NAVIGATION, SUCCESS, ERROR, WARNING, BOOKMARK_ADD, BOOKMARK_REMOVE, EXPERIMENT_START, EXPERIMENT_COMPLETE, ANSWER_CORRECT, ANSWER_INCORRECT
    }

    fun playSound(type: SoundType, view: android.view.View? = null) {
        CoroutineScope(Dispatchers.Main).launch {
            if (!preferences.soundEffectsEnabled.first()) return@launch
            
            // Respect device silent mode
            when (audioManager.ringerMode) {
                AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE -> return@launch
            }

            val volume = preferences.soundVolume.first()
            val soundId = soundMap[type.ordinal] ?: 0
            
            if (isLoaded && soundId != 0) {
                soundPool?.play(soundId, volume, volume, 1, 0, 1f)
            } else if (view != null) {
                // Fallback to default system sounds
                when (type) {
                    SoundType.CLICK -> view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                    SoundType.NAVIGATION -> view.playSoundEffect(android.view.SoundEffectConstants.NAVIGATION_DOWN)
                    else -> view.playSoundEffect(android.view.SoundEffectConstants.CLICK)
                }
            }
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }
}

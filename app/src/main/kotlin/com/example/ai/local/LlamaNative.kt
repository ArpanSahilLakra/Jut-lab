package com.example.ai.local

import android.util.Log

/**
 * JNI wrapper for native llama.cpp backend via libjut_llama.so.
 */
object LlamaNative {
    private const val TAG = "LlamaNative"

    var isNativeLibraryLoaded: Boolean = false
        private set

    var nativeLoadError: String? = null
        private set

    init {
        val libraries = listOf(
            "omp",
            "ggml-base",
            "ggml",
            "ggml-cpu",
            "llama",
            "jut_llama"
        )
        var allOk = true
        for (lib in libraries) {
            try {
                System.loadLibrary(lib)
                Log.d(TAG, "Successfully loaded native library: $lib")
            } catch (e: UnsatisfiedLinkError) {
                // If secondary helper fails, record but proceed if jut_llama succeeds
                Log.w(TAG, "Library $lib load notice: ${e.message}")
                if (lib == "jut_llama") {
                    allOk = false
                    nativeLoadError = e.message
                }
            }
        }
        isNativeLibraryLoaded = allOk
    }

    interface TokenCallback {
        fun onToken(token: String)
        fun onComplete()
        fun onError(error: String)
    }

    /**
     * Loads a GGUF model from the given absolute filesystem path.
     * @return non-zero pointer handle on success, 0 on failure.
     */
    external fun loadModel(modelPath: String): Long

    /**
     * Frees native model and context resources.
     */
    external fun unloadModel(modelPtr: Long)

    /**
     * Executes generation loop with token callback.
     */
    external fun generate(
        modelPtr: Long,
        prompt: String,
        systemPrompt: String,
        callback: TokenCallback
    )

    /**
     * Signals native generation loop to stop cleanly.
     */
    external fun cancel(modelPtr: Long)
}

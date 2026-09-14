# Room Database Entities & DAOs Keep Rules
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
    @androidx.room.* <fields>;
}

# Kotlin Coroutines Keep Rules
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keep class kotlinx.coroutines.** { *; }

# Gemini API Client & Firebase AI Keep Rules
-keep class com.google.firebase.ai.** { *; }
-keep class com.google.ai.client.generativeai.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# OkHttp TLS platforms keep rules / ignore warnings
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn okhttp3.internal.platform.**

# General Serialization & Moshi Keep Rules
-keepattributes *Annotation*,InnerClasses,Signature
-keepclassmembers,allowobfuscation class * {
    @com.squareup.moshi.* <fields>;
    @com.squareup.moshi.* <methods>;
}

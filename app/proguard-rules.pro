# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

-dontwarn io.ktor.**
-keep class io.ktor.** { *; }

-dontwarn com.google.ai.client.generativeai.**
-keep class com.google.ai.client.generativeai.** { *; }

-dontwarn io.github.jan.supabase.**
-keep class io.github.jan.supabase.** { *; }

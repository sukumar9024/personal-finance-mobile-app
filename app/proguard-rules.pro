# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Compose classes
-keepclassmembers class androidx.compose.** { *; }
-keep class androidx.compose.** { *; }

# Keep Google API classes
-keep class com.google.api.services.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.client.json.** { *; }

# Keep Kotlin metadata
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Keep data classes (Expense, Category) for JSON serialization/deserialization
-keep class com.financetracker.data.model.** { *; }

# Navigation Compose
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**

# Material 3
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.android.AndroidMainDispatcher { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
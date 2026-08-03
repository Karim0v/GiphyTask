# Shared attributes (declared once; duplicating -keepattributes silently overrides earlier lines).
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, RuntimeVisibleAnnotations

# --- Retrofit ---------------------------------------------------------------
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
# R8 full mode strips generic signatures of return types used by Retrofit.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
# Keep the API declarations themselves (annotations drive the proxy).
-keep,allowobfuscation interface com.nursulton.giphytask.data.remote.api.** { *; }

# --- OkHttp / Okio --------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Kotlinx Serialization -------------------------------------------------
# Keep the generated serializers of the DTOs; without these, release builds
# fail at runtime with "Serializer for class ... not found".
-keepclassmembers class com.nursulton.giphytask.data.remote.dto.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep class com.nursulton.giphytask.data.remote.dto.**$$serializer { *; }
-keep,includedescriptorclasses class com.nursulton.giphytask.data.remote.dto.** { *; }
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Coil -----------------------------------------------------------------
-dontwarn coil.**

# --- Timber ---------------------------------------------------------------
-dontwarn com.jakewharton.timber.**

# --- Coroutines -----------------------------------------------------------
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

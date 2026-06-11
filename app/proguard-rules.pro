# Dvide — ProGuard rules

# Keep Room entities and DAOs
-keep class com.dvide.app.data.** { *; }

# Keep Hilt generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.** { *; }

# Keep data classes used in serialisation
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# DataStore
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }

# Keep Kotlin metadata for reflection
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

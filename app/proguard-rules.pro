# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.busterminal.app.data.model.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase

# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to the flags specified
# in /Users/.../Library/Android/sdk/tools/proguard/proguard-android.txt
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.jetbrains.annotations.* <fields>;
    @org.jetbrains.annotations.* <methods>;
}

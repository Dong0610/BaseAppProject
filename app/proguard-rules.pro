
#check keep enable ads ex: com.azg.as042_phonetrack9.remoteconfig.model.AdNativeConfig.enable
-keepclassmembers class com.as069.iptv.remoteconfig.model.** {
    boolean enable;
}

# If class has fields with `@SerializedName` annotation keep its constructors
-if class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep class <1> {
  <init>(...);
}
-keepclasseswithmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclasseswithmembers class * {
  <init>(...);
  @com.google.gson.annotations.SerializedName <fields>;
}



-keep,allowoptimization class com.google.android.libraries.maps.** { *; }

-keepclasseswithmembernames class * {
   native <methods>;
}

-keep class androidx.renderscript.** { *; }
-keep class com.google.android.gms.location.** { *; }
-keep class com.google.android.gms.internal.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.** { *; }
-keep class com.google.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-dontwarn com.google.android
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
# Example for keeping specific annotated members (if needed)
-keepclassmembers class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

# Example for keeping Parcelable implementations (if needed)
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep class com.google.firebase.** { *; }
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn java.lang.reflect.AnnotatedType
-dontwarn java.lang.reflect.AnnotatedTypeVariable
-dontwarn java.lang.reflect.AnnotatedParameterizedType
-dontwarn java.lang.reflect.AnnotatedArrayType
-dontwarn java.lang.reflect.AnnotatedWildcardType

# Tránh R8 inline/strip sâu vào Guava reflect:
-keep class com.google.common.reflect.** { *; }
-keepclassmembers class com.google.common.reflect.** { *; }

# Giữ nguyên các model để Firestore phản chiếu được
-keep class com.as069.iptv.model.** { *; }
-keepclassmembers class com.as069.iptv.model.** { *; }
-keep class com.locationtracker.gpstracker.phonelocator.model.** { *; }
-keepclassmembers class com.locationtracker.gpstracker.phonelocator.model.** { *; }
-keepattributes Signature,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,AnnotationDefault

# (Tùy chọn) nếu dùng @PropertyName/@ServerTimestamp, giữ annotation attributes
-keepattributes *Annotation*
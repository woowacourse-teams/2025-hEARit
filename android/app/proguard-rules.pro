# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ==========================================
# ProGuard / R8 Rules for Hearit App
# ==========================================

# -------------------------
# kakao SDK
# -------------------------
# Kakao SDK 모델 클래스 유지
-keep class com.kakao.sdk.**.model.* { <fields>; }

# 외부 보안 라이브러리 관련 경고 무시
# https://github.com/square/okhttp/pull/6792
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**

# -------------------------
# Retrofit
# -------------------------
# Retrofit이 reflection으로 사용하는 어노테이션 및 시그니처 유지
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault

# Retrofit 서비스 인터페이스 유지
-keepclassmembers,allowshrinking,allowobfuscation interface * { @retrofit2.http.* <methods>; }

# -------------------------
# Kotlin & Coroutine
# -------------------------
# 코루틴 Continuation 클래스 유지
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepattributes *Annotation*

# -------------------------
# kotlinx-serialization
# -------------------------
# 직렬화용 클래스와 필드 유지
-keepclassmembers class kotlinx.serialization.** { *; }
-keepclassmembers class * { @kotlinx.serialization.SerialName <fields>; }

# -------------------------
# Timber 로그 제거 (릴리즈)
# -------------------------
-assumenosideeffects class timber.log.Timber {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# -------------------------
# Firebase Crashlytics
# -------------------------
# 소스파일, 라인번호 유지
-keepattributes SourceFile,LineNumberTable
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#自定义混淆字典
-obfuscationdictionary dic.txt
-classobfuscationdictionary dic.txt
-packageobfuscationdictionary dic.txt
#-keepattributes EnclosingMethod
    ############### okhttp ############
        -dontwarn com.squareup.okhttp3.**
        -keep class com.squareup.okhttp3.** { *;}
        -dontwarn okio.**
    ###############retrofit2.0############
        # Platform calls Class.forName on types which do not exist on Android to determine platform.
        -dontnote retrofit2.Platform
        # Platform used when running on Java 8 VMs. Will not be used at runtime.
        -dontwarn retrofit2.Platform$Java8
        # Retain generic type information for use by reflection by converters and adapters.
        -keepattributes Signature
        # Retain declared checked exceptions for use by a Proxy instance.
        -keepattributes Exceptions

 -optimizationpasses 5 #表示proguard对代码进行迭代优化的次数，Android一般为5
-dontwarn net.sourceforge.jtds.**
-dontwarn com.ashokvarma.bottomnavigation.behaviour.**
#反射类不被混肴
-keep class com.shu.messystem.plantime.*
#反射用到的方法不混肴
#-keepclassmembers class com.shu.messystem.plantime.PlanTimeForStopFragementChildAdd{
#    public void queryThread();
#}
#-keepclassmembers class com.shu.messystem.plantime.PlanTimeForStopFragementMain{
#    public void queryThread();
#}
#-keepclassmembers class com.shu.messystem.plantime.PlanTimeForStopFragementModi{
 #   public void queryThread();
#}
#-keepclassmembers class com.shu.messystem.plantime.PlanTimeForStopFragementDel{
#    public void queryThread();
#}
#-keepclassmembers class com.shu.messystem.downtime.DownTimeFragementMain{
   # public void queryThread();
#}
-keepclassmembers class com.shu.messystem..update_component.InstallApkBroadCastReceiver{
     private void open(*);
 }
-keepclassmembers class com.shu.messystem.plantime.*{
     public void queryThread();
 }
 -keepclassmembers class com.shu.messystem.downtime.*{
     public void queryThread();
 }
  -keepclassmembers class com.shu.messystem.downtime_edit.*{
      public void queryThread();
  }
 -keepclassmembers class com.shu.messystem.outputperhour.*{
     public void queryThread();
 }
#Gson解析的bean类 要与服务器返回字段一致。不能混肴
-keep class com.shu.messystem.result_bean.**{*;}
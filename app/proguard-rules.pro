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

# JavaMail/Android Mail - Optimized Configuration
# Only keep what's necessary for SMTP to work, allow R8 to shrink the rest

# Keep core JavaMail API classes and their public methods
-keep class javax.mail.Session {
    public static javax.mail.Session getInstance(java.util.Properties, javax.mail.Authenticator);
    public javax.mail.Transport getTransport(java.lang.String);
}
-keep class javax.mail.Transport {
    public static void send(javax.mail.Message);
}
-keep class javax.mail.Message { *; }
-keep class javax.mail.internet.MimeMessage { *; }
-keep class javax.mail.internet.InternetAddress { *; }
-keep class javax.mail.Authenticator { *; }
-keep class javax.mail.PasswordAuthentication { *; }

# Keep SMTP provider classes (loaded via reflection/SPI)
-keep class com.sun.mail.smtp.SMTPTransport { *; }
-keep class com.sun.mail.smtp.SMTPSSLTransport { *; }

# Keep provider configuration
-keep class com.sun.mail.smtp.SMTPProvider { *; }

# Keep classes used by JavaMail via reflection
-keepclassmembers class * extends javax.mail.Provider {
    <init>(...);
}
-keepclassmembers class * extends javax.mail.Transport {
    <init>(javax.mail.Session, javax.mail.URLName);
}

# Minimal activation framework support
-keep class javax.activation.DataHandler { *; }
-keep class javax.activation.DataSource { *; }
-keep class javax.activation.CommandMap { *; }
-keep class javax.activation.MailcapCommandMap { *; }

# Preserve attributes needed for reflection
-keepattributes Signature,InnerClasses

# Suppress warnings about unused mail providers
-dontwarn javax.mail.**
-dontwarn com.sun.mail.**
-dontwarn javax.activation.**
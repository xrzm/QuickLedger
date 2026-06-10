# QuickLedger ProGuard Rules

# Keep Room entities
-keep class com.quickledger.app.data.local.entity.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

java -jar ./signapk.jar ./platform.x509.pem ./platform.pk8 app-debug.apk app_signed_rd.apk 

adb install -t -r app_signed_rd.apk

pause

# del FFF.apk
# adb shell am start -n com.fanfull.fff_primary/com.fanfull.handheld.activity.StartActivity
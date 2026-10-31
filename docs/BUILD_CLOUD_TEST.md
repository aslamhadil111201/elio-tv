# Build prarilis Cloudstream

Main mengikuti test7 (versionCode 32). Unduh ELIO_TV_1.9.3-cloud-test7_Source.zip dari Assets prarilis untuk dependensi lengkap dan sumber GPL. Arsip berisi project/ dan smoke-libs/ sebagai folder sejajar. Gunakan JDK 17, Gradle 8.9, Android SDK platform 35 dan build-tools 35.0.0, lalu gradle -p project :app:assembleRelease. APK hasil build belum ditandatangani.

Jika membangun checkout main langsung, salin smoke-libs/ dari arsip Source ke root checkout; app/build.gradle pada main memakai ../smoke-libs. Jangan sertakan android.jar dari SDK sebagai dependency runtime. Katalog source publik adalah katalog tersanitasi; APK distribusi memakai katalog aplikasinya.

Tidak ada kunci signing atau password dalam repository maupun arsip Source publik. Kompatibilitas ekstensi dan sumber siaran perlu diuji pada perangkat.

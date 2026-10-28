# Membangun ELIO TV 1.9.0

## Kebutuhan

- JDK 17
- Gradle 8.9 (belum ada Gradle Wrapper di repository)
- Android SDK Platform 35 dan Build Tools 35.0.0
- Akses internet untuk mengunduh dependensi Google Maven dan Maven Central

## Android Studio

Buka folder root repository sebagai proyek Gradle. Pilih Gradle JDK 17 dan Gradle 8.9 pada pengaturan IDE, lalu sinkronkan proyek. Android Studio membuat local.properties sesuai SDK di komputer masing-masing.

## Terminal

Set ANDROID_HOME ke folder SDK lokal, atau buat local.properties berisi sdk.dir sesuai lokasi SDK. Jalankan:

```bash
gradle :app:assembleDebug
```

APK debug berada di app/build/outputs/apk/debug/. Untuk release unsigned:

```bash
gradle :app:assembleRelease
```

APK unsigned berada di app/build/outputs/apk/release/. Repository ini tidak memuat kunci penandatanganan atau password. Untuk memperbarui APK resmi yang sudah terpasang, gunakan kunci asli yang disimpan terpisah. APK debug memakai kunci berbeda.

## Struktur

- app/src/main/java/id/matin/nusatv/: kode Java UI, pemutar, failover, EPG, dan navigasi sentuh.
- app/src/main/assets/channels.json: katalog sumber.
- app/src/main/assets/logos/: logo channel.
- app/src/main/assets/epg-*.xmlz: cache awal XMLTV terkompresi.
- app/src/main/res/: ikon launcher, banner Android TV, dan resource Android.

Ketersediaan sumber dan jadwal EPG dapat berubah. Kredensial penandatanganan, konfigurasi mesin, cache, serta output build tidak disertakan.

## Versi dan katalog online

Branch main memakai versionName 1.9.0 dan versionCode 21. Katalog online berada di [catalog/channels.json](../catalog/channels.json); petunjuk pengelolaan ada di [CATALOG_UPDATES.md](CATALOG_UPDATES.md). Perubahan katalog tidak memerlukan build APK, sedangkan perubahan kode aplikasi memerlukan build dan penandatanganan baru.

APK yang dibangun dari salinan publik memakai konfigurasi sumber publik yang telah disanitasi. Gunakan aset APK pada rilis jika membutuhkan paket distribusi yang sama persis.

## Uji logika Java

Sumber uji ada di folder tests. Dengan JDK 17 dan dependency org.json:json:20240303 pada classpath, kompilasi CatalogStore, CatalogHttp, SeekWindow, CatalogStoreTest, dan SeekWindowTest. Jalankan CatalogStoreTest dengan dua argumen: path app/src/main/assets/channels.json dan catalog/channels.json. SeekWindowTest tidak memerlukan argumen. Kedua uji tidak menjalankan UI Android.

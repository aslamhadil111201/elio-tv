# ELIO TV

Aplikasi IPTV untuk **Android TV, STB Android, dan HP Android**, dengan preview siaran, favorit, jadwal acara dan pemutar film melalui ekstensi Cloudstream.

## Unduh aplikasi

**Prarilis terbaru: [v1.9.3-cloud-test8](https://github.com/aslamhadil111201/elio-tv/releases/tag/v1.9.3-cloud-test8)** · [Unduh APK](https://github.com/aslamhadil111201/elio-tv/releases/download/v1.9.3-cloud-test8/ELIO_TV_1.9.3-cloud-test8_Mobile_TV.apk)

- Tampilan TV + Film diperbarui; keduanya memakai header dan skala yang sama.
- Katalog dan pencarian Film menggabungkan provider yang sudah terpasang.
- Sumber kosong/gagal mencoba provider cadangan yang cocok. Metadata tahun yang belum lengkap memerlukan konfirmasi.
- Filter remote kini dapat dijangkau: dari baris channel pertama tekan Atas, pilih Internasional dengan Kiri/Kanan, lalu OK.
- Preview tidak dihentikan saat berpindah fokus menu/filter; server terakhir yang berhasil diprioritaskan, dengan waktu tunggu awal 9 detik per server.
- Ikon informasi pada kartu channel dihapus; Info siaran tetap tersedia.
- **Provider pihak ketiga belum dibundel.** Instalasi baru masih memerlukan pengaturan ekstensi melalui Setelan Film.
- APK Android 6.0+; fitur Film/Cloudstream Android 8.0+.
- Build dan 158 pemeriksaan logika lulus. **Tampilan, navigasi dan playback di perangkat belum diverifikasi langsung oleh pengembang.** Ketersediaan provider tidak dijamin.

[Catatan prarilis](docs/RELEASE_1.9.3-cloud-test8.md) · [Kode lengkap dan dependensi](https://github.com/aslamhadil111201/elio-tv/releases/download/v1.9.3-cloud-test8/ELIO_TV_1.9.3-cloud-test8_Source.zip)

Kode Android di **main** sekarang mengikuti test8. Untuk membangun lengkap gunakan arsip Source yang menyertakan dependensi SDK/API dan sumber GPL; lihat [panduan build](docs/BUILD_CLOUD_TEST.md). Katalog publik tetap terpisah dari sumber siaran privat.

Versi stabil tetap [v1.9.2](https://github.com/aslamhadil111201/elio-tv/releases/tag/v1.9.2). Test7 diterbitkan sebagai **pre-release**, bukan pengganti klaim stabil.

## Pembaruan v1.9.2

- **Pengaturan layar** saat menonton: Pas layar, Penuhi layar, dan Regangkan.
- Buka **Pilihan → Pengaturan layar** di HP atau **MENU → Pengaturan layar** lewat remote TV. Pilihan disimpan setelah pindah channel dan membuka ulang aplikasi.
- **Penuhi layar** memotong tepi gambar; **Regangkan** dapat mengubah proporsi. Default **Pas layar** mempertahankan gambar utuh.
- **46 uji Java ukuran video lolos**. Belum diuji langsung di perangkat fisik. [Catatan rilis](docs/RELEASE_1.9.2.md).
- Fitur ini membutuhkan pemasangan APK v1.9.2; pembaruan katalog saja tidak menambah menu player.

## Pembaruan v1.9.1

- **39 logo nasional bersih** dengan ukuran/padding konsisten; logo Tawaf TV dan Berita Satu diperbaiki. Katalog online revisi 2 membawa logo pengganti.
- **Timeline / seek bar** di player: HP tarik titik menit/detik lalu lepas; remote kiri membuka timeline, kiri/kanan menggeser 15 detik, OK memutar; Back/atas/bawah membatalkan. Tombol LIVE kembali ke siaran langsung.
- **Acara sebelumnya / arsip** melalui Pilihan / MENU. Hanya awal acara yang masih berada dalam DVR dan memiliki timestamp dapat diputar. Acara di luar DVR memerlukan arsip resmi dari penyedia.

- **Katalog online:** pemeriksaan otomatis setiap 4 jam saat aplikasi aktif; pemeriksaan manual melalui **Pengaturan → Perbarui channel → Perbarui sekarang**.
- **Katalog cadangan:** pembaruan divalidasi, disimpan secara atomik, dan memakai katalog terakhir jika koneksi gagal. Favorit serta playlist pribadi dipertahankan.
- **Putar ulang pada sumber yang mendukung:** melalui **Pilihan / MENU → Putar ulang / posisi siaran**. Mundur/maju 30 detik, pilih menit atau mm:ss, dan kembali ke siaran langsung.
- Perbaikan fokus remote dari 1.8.6: tombol **Tonton sekarang**, **Favorit**, dan **Info siaran** dapat dipilih.
- Katalog bawaan tetap **131 entri channel**. Rilis ini tidak menambah atau menguji ulang sumber siaran.

Putar ulang bergantung pada rentang video yang disediakan server. Jadwal EPG bukan arsip video; fitur ini tidak menyediakan rekaman acara kemarin pada semua channel.

## Cara instalasi

1. Buka [rilis v1.9.2](https://github.com/aslamhadil111201/elio-tv/releases/tag/v1.9.2) dan unduh `ELIO_TV_1.9.2_Mobile_TV.apk`.
2. Buka APK di perangkat Android. Jika diminta, izinkan pemasangan dari sumber yang dipilih.
3. Pengguna versi lama dapat memasangnya sebagai pembaruan.
4. TV/STB: gunakan tombol arah, OK, Back, dan MENU. HP: gunakan sentuhan dan geser daftar channel.

## Katalog dan jadwal acara

**Katalog online revisi 6: 136 entri**, termasuk **RCTV Cirebon, Banten TV, Jogja TV, Banyumas TV, dan Radar Tasikmalaya TV**. Di versi 1.9.0 ke atas buka **Pengaturan → Perbarui channel → Perbarui sekarang**, lalu cari nama channel. Tidak perlu memasang APK baru untuk tambahan katalog ini. Jadwal EPG kelima channel lokal tersebut belum dipetakan.


**Perbaikan logo katalog revisi 6 (untuk APK v1.9.1 ke atas):** 19 gambar logo disimpan di repositori, mencakup lima TV lokal dan 14 channel olahraga/internasional. Logo yang sudah tersedia di APK memakai aset bawaan agar tidak menghabiskan kuota pemrosesan 64 logo online. Total cakupan 135 dari 136 channel; TVS Sports Bureau masih memakai teks karena gambar yang ditemukan sudah tidak tersedia. Gunakan pembaruan katalog manual, lalu buka ulang aplikasi bila gambar belum muncul.

[Katalog online](catalog/channels.json) disimpan di branch **main**. Pengelola dapat mengganti metadata, sumber, server cadangan, dan logo tanpa membangun APK baru. Setiap perubahan katalog harus menaikkan nomor revisi. Baca **[panduan pembaruan katalog](docs/CATALOG_UPDATES.md)**.

Pembaruan katalog bukan pencarian otomatis IPTV aktif. Ketersediaan siaran bergantung pada penyedia, wilayah, dan jaringan. EPG hanya tampil jika data jadwal tersedia.

## Kode sumber dan build

Kode versi **1.9.2** tersedia di branch **main**. Pilih **Code → Download ZIP** untuk mengunduh proyek, atau gunakan Source code pada rilis **v1.9.2**.

Kebutuhan build: **JDK 17, Gradle 8.9, Android SDK 35, Build Tools 35.0.0**. Baca [panduan build](docs/BUILD.md).

Salinan publik tidak menyertakan kunci penandatanganan, password, konfigurasi komputer, atau konfigurasi akses bertoken/DRM dan header rahasia. Katalog publik dapat berbeda dari konfigurasi APK distribusi.

## Pengujian

Build release dan lintVital lolos. Sebanyak **38 uji katalog/HTTP, 18 uji parsing posisi, dan 18 uji timeline/riwayat acara** lolos. Pengunduh Java berhasil mengambil katalog GitHub melalui HTTPS, memvalidasi, menggabungkan 131 channel, dan membaca ulang salinan lokal. Sertifikat APK cocok dengan versi 1.8.6.

**Belum diuji langsung di TV/STB/HP fisik.** Uji logika bukan jaminan semua channel aktif atau semua sumber mendukung DVR. Lihat [catatan pengujian v1.9.1](docs/TESTING_1.9.1.md).

## Riwayat versi

| Versi | Pembaruan |
| --- | --- |
| **1.9.2** | Pilihan Pas layar, Penuhi layar dan Regangkan untuk ukuran video di HP/TV |
| 1.9.1 | Logo nasional bersih, timeline yang dapat ditarik, dan daftar acara sebelumnya dalam DVR |
| 1.9.0 | Katalog online, cache cadangan, dan kontrol putar ulang sesuai dukungan sumber |
| 1.8.6 | Perbaikan fokus remote untuk Tonton, Favorit, dan Info siaran |
| 1.8.5 | 29 varian keluarga Nickelodeon; total 131 entri |
| 1.8.4 | Tambahan Saudi, USA, serta olahraga/gaming; total 102 entri |

Rincian: [CHANGELOG](CHANGELOG.md). Rilis lama tetap tersedia sebagai arsip.

## Struktur proyek

- `app/src/main/java/`: UI, player, failover, navigasi, EPG, katalog online, dan posisi putar ulang.
- `app/src/main/assets/`: katalog bawaan, cache EPG, dan logo.
- `app/src/main/res/`: ikon launcher, banner Android TV, dan resource.
- `catalog/channels.json`: snapshot katalog online.
- `tests/`: uji Java katalog/HTTP dan posisi putar ulang.
- `docs/`: panduan build, pengelolaan katalog, dan catatan pengujian.
- `app/build.gradle`: versi 1.9.2, versionCode 24.

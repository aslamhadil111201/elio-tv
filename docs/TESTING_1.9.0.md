# Pengujian ELIO TV 1.9.0

- Package: `id.matin.nusatv`
- VersionName: `1.9.0`; versionCode: `21`
- APK: `ELIO_TV_1.9.0_Mobile_TV.apk`
- SHA-256: `085d55a79fee8740c8716f8eb7e946a72caf06619c30209da8223b342890595b`

## Hasil pemeriksaan

- Build Android release bersih dan lintVital berhasil.
- Tanda tangan APK valid; sertifikat SHA-256 sama dengan APK 1.8.6.
- 38 uji Java katalog/HTTP: penggabungan metadata dan sumber, penambahan/penghapusan channel, penolakan duplikat/format rusak/header tidak valid, pembatasan ukuran, status HTTP, dan redirect HTTPS.
- 18 uji Java posisi: parsing menit/mm:ss, penolakan input tidak valid, batas seek, dan format waktu.
- Uji jaringan menggunakan kode pengunduh Java berhasil mengambil katalog publik dari GitHub lewat HTTPS, memvalidasi revisi 1, menggabungkan 131 entri, dan membaca ulang salinan di disk.
- Semua 125 aset katalog/logo/EPG dibanding versi 1.8.6 identik. Profil optimasi Dex dapat berubah mengikuti build.
- APK berisi kelas katalog, kontrol posisi, dan perbaikan fokus remote.

## Batas pengujian

Tidak ada uji langsung di TV/STB/HP fisik atau emulator Android yang berhasil berjalan. Uji logika dan jaringan Java bukan uji UI Android maupun bukti pemutaran siaran.

Sumber siaran tidak diuji ulang pada versi ini. Angka 131 adalah jumlah entri katalog, bukan jaminan jumlah channel aktif. Kontrol putar ulang memerlukan rentang video seekable dengan durasi yang tersedia dari server; tidak membangun server DVR atau arsip tayangan.

Android AtomicFile dipakai oleh integrasi aplikasi. Uji jaringan Java memeriksa penyimpanan dan pembacaan ulang data, bukan simulasi crash proses Android.

## Riwayat

[Pengujian 1.8.5](TESTING_1.8.5.md) adalah catatan historis pengujian sumber pada versi tersebut.

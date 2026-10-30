# ELIO TV 1.9.3-cloud-test1 — prarilis

Versi percobaan, versionCode 26. Versi stabil tetap v1.9.2.

## Yang ditambahkan

- Pengaturan layar global di Setelan beranda; pilihan tetap dapat diubah dari player.
- Menu Setelan → Film & Cloudstream · Percobaan untuk menambah repository HTTPS, memilih dan memasang ekstensi secara manual, membuka provider, mencari judul, melihat detail/episode dan meneruskan tautan video ke player ELIO.
- Demo offline Big Buck Bunny berdurasi 10 detik untuk mencoba alur API.
- Tidak ada pemasangan atau pembaruan ekstensi otomatis.

## Cara mencoba

Unduh APK dari [prarilis](https://github.com/aslamhadil111201/elio-tv/releases/tag/v1.9.3-cloud-test1), lalu pasang sebagai pembaruan. Buka Setelan → Film & Cloudstream → Tambahkan repo → pilih ekstensi → Pasang → Buka provider terpasang. Pencarian mengikuti dukungan provider. Link repository contoh yang diminta pengguna telah diisi; sumber tersebut tidak dijamin kompatibel.

Fitur ekstensi memerlukan Android 8/API 26 ke atas. IPTV tetap minSDK 23/Android 6. Dialog dibuat untuk sentuhan dan D-pad; belum diuji langsung di perangkat HP/TV.

## Batasan dan pengujian

Build Android dan lintVital lolos. 23 pemeriksaan JVM dan 72 pencocokan referensi metode DEX statis lolos. Cuplikan demo berhasil didekode menggunakan FFmpeg. Ini bukan pengujian pemutaran di Android: provider pihak ketiga dan stream filmnya belum dijalankan di HP/TV atau emulator.

Ekstensi dengan resources tambahan, torrent/magnet, DRM, login khusus/CAPTCHA belum didukung. Subtitle eksternal provider belum diteruskan. Plugin menjalankan kode dengan izin aplikasi; pasang hanya dari sumber yang dipercaya. SHA-256 file lokal tidak membuktikan kepercayaan penerbit. Hak siar/film mengikuti pemilik konten, bukan lisensi kode aplikasi.

## Kode sumber percobaan

Branch main tetap memuat kode stabil v1.9.2. Untuk membangun versi percobaan, unduh **ELIO_TV_1.9.3-cloud-test1_Source.zip** pada Assets prarilis, bukan ZIP otomatis GitHub. Arsip tersebut memuat proyek percobaan, dependensi runtime yang tepat, fixture demo, sumber upstream Cloudstream dan lisensi GPL-3.0. Konfigurasi katalog publik disanitasi dan dapat berbeda dari APK distribusi. Kunci signing dan password tidak disertakan.

Panduan build dan atribusi lengkap terdapat dalam README_CLOUD_TEST.md di arsip sumber. SDK upstream: recloudstream/cloudstream commit 5024ace2922a148d38df2a816b483b529789cf58.

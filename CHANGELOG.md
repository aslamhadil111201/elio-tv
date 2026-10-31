# v1.9.3-cloud-test8 · Prarilis

- Perbaiki jalur fokus remote ke filter, termasuk Internasional dan kategori kosong.
- Pertahankan preview saat fokus berpindah ke header/filter; prioritaskan server terakhir yang berhasil dan beri 9 detik untuk frame pertama.
- 158 pemeriksaan logika dan build lulus. Navigasi/playback langsung di perangkat belum diuji pengembang.

[Catatan rilis](docs/RELEASE_1.9.3-cloud-test8.md)

# v1.9.3-cloud-test7 · Prarilis

Header/skala TV dan Film disatukan, katalog gabungan provider terpasang, fallback sumber dan tab Film diperbarui. Build dan 117 uji logika lulus; pengujian perangkat belum dilakukan. [Catatan rilis](docs/RELEASE_1.9.3-cloud-test7.md).

# Riwayat ELIO TV

## 1.9.3-cloud-test1 — 30 Oktober 2026 (prarilis)

- Pengaturan layar global pada beranda.
- Dukungan percobaan repository/ekstensi Cloudstream, pencarian judul, detail/episode dan pemutaran melalui player ELIO.
- Demo API offline 10 detik; fitur ekstensi Android 8+.
- Build/lint dan uji JVM/statis lolos; belum diuji langsung di HP/TV atau provider pihak ketiga.
- Versi stabil tetap 1.9.2. Kode lengkap percobaan tersedia sebagai arsip sumber khusus di Assets prarilis, bukan kode stabil main.
- [Panduan dan batasan](docs/RELEASE_1.9.3-cloud-test1.md).

## 1.9.2 — 29 Oktober 2026

- Pengaturan layar video: Pas layar, Penuhi layar, dan Regangkan.
- Pilihan tersimpan lintas channel dan sesi; menu mendukung sentuhan dan remote TV.
- Penghitungan ulang saat format video/ukuran layar berubah, termasuk rasio piksel video.
- 46 uji Java geometri layar lolos; belum diuji langsung pada perangkat fisik.
- VersionCode 24. [Catatan rilis](docs/RELEASE_1.9.2.md).

## 1.9.1 — 28 Oktober 2026

- 39 logo nasional dinormalisasi/diganti dengan aset bersih; Tawaf dan Berita Satu dikoreksi.
- Katalog online revisi 2 membawa logo baru tanpa mengganti sumber siaran.
- Timeline menit/detik di player: drag pada HP dan navigasi remote, dengan penyesuaian pergeseran DVR.
- Daftar hingga 24 acara sebelumnya dalam 24 jam terakhir. Pemutaran hanya jika awal acara masih berada dalam DVR yang tersedia; akses layanan resmi bila URL tersedia.
- 18 uji baru timeline/riwayat; build release dan sertifikat lolos. Belum diuji di perangkat fisik.
- VersionCode 22. [Catatan rilis](docs/RELEASE_1.9.1.md).

## 1.9.0 — 28 Oktober 2026

- Katalog kumulatif dari GitHub, pemeriksaan otomatis 4 jam dan manual.
- Validasi sebelum cache atomik; fallback katalog terakhir dan bawaan.
- Favorit, playlist pribadi, pilihan channel, dan pemutaran aktif dipertahankan.
- Metadata, server, logo HTTPS, dan ID EPG dapat dikelola melalui katalog.
- Putar ulang sesuai dukungan sumber: ±30 detik, menit/mm:ss, kembali live, serta penanganan posisi tertinggal di luar jendela live.
- Katalog bawaan 131 entri dipertahankan. Tidak ada pengujian ulang sumber.
- VersionCode 21. [Pengujian](docs/TESTING_1.9.0.md).

## 1.8.6 — 27 Oktober 2026

- Fokus remote untuk Tonton sekarang, Favorit, dan Info siaran.
- Atas dari baris channel pertama menuju tombol; kiri/kanan memilih aksi; bawah kembali ke daftar.
- 20 kasus logika fokus Java lolos; belum diuji pada TV fisik.
- VersionCode 20.

## 1.8.5 — 27 Oktober 2026

- Tambahan 29 varian keluarga Nickelodeon; total 131 entri.
- Kandidat Nickelodeon Indonesia belum lolos uji dan tidak dimasukkan.
- VersionCode 19. [Pengujian historis](docs/TESTING_1.8.5.md).

## 1.8.4

- Tambahan Saudi, USA, dan olahraga/gaming; total 102 entri.

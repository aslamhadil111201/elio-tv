# Pengujian ELIO TV 1.9.1

VersionName 1.9.1; versionCode 22; package id.matin.nusatv.

APK SHA-256: `b90d5342733a3cfd2e89c3371bcda6a13b3dd3d60e0e4f2dbbec0c00ba919f6b`

- Build release bersih dan lintVital lolos.
- Sertifikat APK cocok dengan versi 1.9.0.
- 38 uji katalog/HTTP dan 18 uji parsing posisi lolos.
- 18 uji baru: koordinat timeline, batas kiri/kanan, input koordinat invalid, pergeseran jendela DVR, target yang sudah kedaluwarsa, urutan riwayat EPG, pembatasan 24 jam dan 24 acara.
- 39 aset logo nasional diperiksa secara visual dan dicocokkan dengan PNG dalam APK serta blob GitHub.
- Semua 131 daftar server pada katalog APK sama dengan 1.9.0; sumber siaran tidak diuji ulang.
- Tidak ada uji langsung di TV/STB/HP fisik atau emulator yang berhasil berjalan. Uji logika bukan bukti gesture Android, playback DVR atau arsip tiap penyedia sudah teruji di perangkat.
- Acara sebelumnya hanya dapat diputar jika awal acara tersedia dalam DVR dan sumber memberi timestamp awal jendela. EPG bukan rekaman video; arsip luar DVR harus disediakan penyedia.

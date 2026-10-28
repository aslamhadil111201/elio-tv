# Katalog online ELIO TV 1.9.0

APK mengambil `catalog/channels.json` dari branch `main` repositori ini melalui HTTPS. Untuk pengguna: Pengaturan → Perbarui channel → Perbarui sekarang. Pemeriksaan otomatis saat aplikasi aktif dibatasi setiap 4 jam; kegagalan dicoba kembali setelah 15 menit. Pemeriksaan manual melewati interval tersebut.

## Mengubah katalog

1. Edit `catalog/channels.json` di GitHub.
2. Naikkan `revision` setiap ada perubahan (1 → 2 → 3).
3. Pertahankan `schema: 1`. Simpan seluruh override sebelumnya; file merupakan snapshot kumulatif, bukan hanya perubahan paling baru.
4. Commit, lalu buka Pengaturan → Perbarui channel → Perbarui sekarang di ELIO TV.

Contoh format (gunakan URL siaran publik yang memang tersedia):

```json
{
  "schema": 1,
  "revision": 2,
  "updatedAt": "2026-10-28",
  "channels": [
    {
      "id": "contoh-channel",
      "name": "Contoh Channel",
      "group": "Internasional",
      "section": "Olahraga",
      "owner": "Penyedia",
      "servers": [
        {"name": "Server 1", "url": "https://example.org/live.m3u8"}
      ],
      "logoUrl": "https://example.org/logo.png"
    }
  ],
  "removed": []
}
```

- ID harus tetap sama agar favorit tetap terhubung. Untuk channel lama, cukup tulis ID dan field yang akan diganti. Field lain memakai nilai katalog bawaan APK.
- Channel baru wajib mempunyai `id`, `name`, dan `servers` (boleh kosong untuk entri informasi).
- `servers` mengganti seluruh daftar server channel tersebut. Hilangkan field ini jika sumber bawaan ingin dipertahankan.
- `removed` menghapus ID dari katalog bawaan/online; tidak menghapus ID favorit dari penyimpanan. Playlist M3U pribadi dipertahankan.
- `logoUrl` opsional, harus HTTPS, gambar ≤256 KiB dan ≤2048×2048. Kegagalan logo tidak menggagalkan katalog. Logo bawaan atau nama channel menjadi cadangan. Unduhan logo dibatasi 64 per pemeriksaan dan anggaran sekitar 30 detik.
- `epgIds`, `logoDark`, `availabilityNote`, `officialUrl`, `liveLabel`, `group`, `section`, `owner` dapat diperbarui. URL jadwal EPG tetap memakai sumber EPG aplikasi.
- Katalog maksimal 2 MiB / 2000 channel / 20 server per channel. Duplikat ID, field rusak, protokol tidak sesuai, dan revisi lama ditolak.
- Katalog awal revisi 1 hanya memuat metadata 131 channel. Sumber bawaan APK dipertahankan; tidak ada penambahan atau pengujian ulang sumber siaran pada rilis ini.

Repositori ini publik: gunakan hanya URL dan informasi yang memang boleh dipublikasikan. Jangan masukkan password, token akses, kunci DRM, atau konfigurasi privat.

## Penyimpanan dan pemutaran

Katalog divalidasi sebelum ditulis melalui Android AtomicFile di penyimpanan internal aplikasi. Jika koneksi atau data gagal, katalog terakhir tetap dipakai. Jika cache rusak, aplikasi kembali ke katalog bawaan. Pembaruan saat menonton diterapkan setelah keluar dari player agar siaran tidak terputus. Favorit, tema, dan playlist impor tidak dihapus. Pilihan channel dipertahankan berdasarkan ID jika masih tersedia.

Tidak ada pencarian otomatis IPTV aktif. Status pembaruan katalog bukan jaminan siaran dapat diputar. Perubahan kode/fitur tetap memerlukan APK baru.

## Putar ulang

Saat menonton: Pilihan / MENU → Putar ulang / posisi siaran. Mendukung mundur/maju 30 detik, posisi menit atau mm:ss, serta kembali ke live/default position. Tombol remote rewind/fast-forward juga memakai langkah 30 detik.

Kontrol hanya aktif saat player melaporkan media seekable dan durasi tersedia. Posisi dihitung dari awal rentang video yang saat ini disediakan server. Rentang live terus bergeser; bukan jam siaran. Acara kemarin tidak otomatis tersedia. EPG bukan arsip video; rilis ini tidak merekam atau membuat server catch-up. Jika posisi tertinggal di luar rentang live, player kembali ke posisi live.

## Validasi

- 38 uji Java penggabungan/validasi katalog dan unduhan HTTP.
- 18 uji Java parsing posisi dan batas seek.
- Build release, pemeriksaan APK dan sertifikat dilakukan untuk rilis.
- Belum diuji langsung pada TV/STB/HP fisik. Uji unit bukan bukti channel aktif atau fitur DVR tersedia pada semua sumber.

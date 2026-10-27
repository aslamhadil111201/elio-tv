# ELIO TV

Aplikasi pemutar IPTV untuk **Android TV, STB Android, dan HP Android**. Mendukung navigasi remote maupun layar sentuh, preview siaran, favorit, pilihan server, dan jadwal acara pada channel yang didukung.

Repository publik ini menyimpan dokumentasi, APK, dan arsip kode sumber ELIO TV.

## Unduh aplikasi

APK tersedia pada halaman [Releases](../../releases). Versi saat ini: **v1.8.5**.

## Pembaruan v1.8.5

- Total katalog **131 entri channel**.
- Tambahan **29 varian Nickelodeon dan Nick Jr.**, termasuk Classics, Teen, serta Toons, yang videonya berhasil didekode saat uji ulang.
- 102 entri dari versi sebelumnya tetap dipertahankan.
- Kandidat Nickelodeon berbahasa Indonesia belum lolos uji sehingga belum dimasukkan.

## Cara instalasi

1. Buka halaman Releases dan unduh `ELIO_TV_1.8.5_Mobile_TV.apk`.
2. Buka APK di perangkat Android. Jika diminta, izinkan pemasangan dari sumber yang dipilih.
3. Untuk pengguna versi sebelumnya, pasang sebagai pembaruan.
4. Pada TV/STB gunakan tombol arah, OK, dan Back. Pada HP gunakan sentuhan serta geser untuk menjelajahi channel.

## Catatan pengujian

Keberhasilan uji sumber berlaku saat pengujian dan tidak menjamin ketersediaan seterusnya atau akses dari semua jaringan. Jumlah katalog bukan jumlah channel yang dijamin aktif. APK versi ini belum diuji langsung pada perangkat Android fisik. Jadwal acara hanya tampil jika data EPG tersedia.

## Riwayat versi

| Versi | Pembaruan |
| --- | --- |
| 1.8.5 | 29 varian keluarga Nickelodeon; total 131 entri |
| 1.8.4 | Tambahan Saudi, USA, serta olahraga/gaming; total 102 entri |

---

ELIO TV merupakan aplikasi pemutar. Ketersediaan siaran bergantung pada penyedia sumber dan jaringan yang digunakan.

## Kode sumber lengkap

Unduh [ELIO_TV_1.8.5_Source.zip](https://github.com/aslamhadil111201/elio-tv/releases/download/v1.8.5/ELIO_TV_1.8.5_Source.zip) pada halaman rilis. Arsip berisi proyek Android: kode Java, aset, ikon, logo, katalog sumber, cache EPG, konfigurasi Gradle, panduan build dan catatan pengujian.

Gunakan arsip bernama **ELIO_TV_1.8.5_Source.zip**, bukan ZIP otomatis dari tag awal yang hanya memuat dokumentasi repository. Kode lengkap disimpan sebagai aset rilis, bukan sebagai folder proyek pada branch main.

Salinan publik tidak memuat kunci penandatanganan, password, konfigurasi mesin, atau entri sumber dengan kunci dekripsi tertanam. Karena itu katalog kode publik berbeda dari sebagian konfigurasi distribusi APK. Gunakan sumber resmi atau playlist yang Anda miliki aksesnya.

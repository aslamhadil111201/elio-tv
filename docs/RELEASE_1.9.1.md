# ELIO TV 1.9.1

## Logo nasional

39 entri nasional memakai aset PNG yang telah diperiksa secara visual, dengan padding konsisten. Logo Tawaf TV dan Berita Satu juga dikoreksi. iNews, tvOne, JakTV dan Garuda memakai latar gelap agar bagian putih terbaca. Katalog online revisi 2 menunjuk aset versi v191; aset sama dibundel ke APK agar tidak bergantung pada unduhan logo pertama.

Berkas logo lama pada backup yang diperiksa berbeda dari screenshot pengguna dan sebagian sudah bersih. Rilis ini mengganti/menormalkan seluruh 39 entri, bukan menghapus watermark dari gambar lama. Sumber aset tercatat di LOGO_SOURCES_1.9.1.json; merek/logo tetap milik pemiliknya.

## Timeline / seek bar

Saat kontrol player tampil, sumber seekable menyediakan timeline menit/detik. HP: ketuk video lalu tarik titik pada timeline; lepas untuk memutar. TV: kiri membuka fokus timeline, kiri/kanan memilih posisi 15 detik, OK memutar; Back/atas/bawah membatalkan fokus. Tombol LIVE kembali ke posisi live/default server. Pilihan → Putar ulang / posisi siaran → Geser timeline juga membuka fokus.

Drag menyimpan posisi saat mulai dan memperhitungkan pergeseran jendela DVR saat dilepas. Berganti server/channel membatalkan drag. Timeline tidak mengirim seek pada setiap gerakan jari. Sumber tanpa dukungan DVR menampilkan penjelasan, bukan timeline yang seolah aktif.

## Acara sebelumnya

Pilihan → Acara sebelumnya / arsip menampilkan hingga 24 acara yang telah berakhir dalam 24 jam terakhir dari EPG. Acara hanya dapat diputar jika awalnya berada dalam DVR yang tersedia dan sumber memberi timestamp awal jendela. Bagian di luar DVR dijelaskan sebagai tidak tersedia. Layanan resmi dapat dibuka jika URL resmi tersedia; arsip resmi bisa memerlukan login/paket penyedia. Tidak ada server rekaman atau pengambilan arsip berbayar yang ditambahkan.

## Pengujian

18 uji baru untuk koordinat timeline, batas posisi, pergeseran DVR, dan riwayat EPG; 18 uji parsing posisi dan 38 uji katalog/HTTP lolos. Build Android dan tanda tangan APK diperiksa saat rilis. Belum diuji pada TV/STB/HP fisik; ketersediaan DVR dan arsip tiap channel belum diuji langsung.

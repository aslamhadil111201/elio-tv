# Pemeriksaan logo katalog revisi 6

Tanggal: 28 Oktober 2026. APK tetap v1.9.1; tidak ada build atau uji perangkat baru.

## Perubahan

- Lima logo lokal disalin dari sumber stasiun/aplikasi resminya: RCTV Cirebon, Banten TV, Jogja TV, Banyumas TV, Radar Tasikmalaya TV.
- 14 logo olahraga/internasional yang belum memiliki aset bawaan ditambahkan dari daftar logo iptv-org dan situs HTSpor.
- PNG dinormalisasi ke 512 × 256, ruang logo maksimum 448 × 190. Logo FTF putih memakai latar gelap.
- Alamat gambar Banten TV sempat mengembalikan HTTP 503; salinan gambar yang sudah diperiksa dipakai di repositori ini.
- Pemuat logo v1.9.1 memproses maksimal 64 entri beralamat gambar dan memiliki batas waktu 30 detik. Sebelumnya logo lokal berada setelah batas tersebut. Revisi 6 mengosongkan logoUrl pada 116 channel yang sudah memiliki PNG bawaan. applyCatalog memuat aset logos/<id>.png ketika logoUrl kosong. Ini membuat hanya 19 logo online perlu diproses, tanpa APK baru.
- Tidak mengubah sumber siaran, ID, jumlah channel, atau jadwal.

## Pemeriksaan

- 136 ID tetap; 116 memiliki aset PNG bawaan, 19 memiliki gambar tambahan, satu masih fallback teks.
- 19 PNG berhasil dibuka dengan Pillow, diperiksa lewat contact sheet, masing-masing di bawah 256 KiB dan dimensi di bawah 2048 px.
- Logo TVS Sports Bureau belum ditambahkan: tautan yang ditemukan berisi gambar pemberitahuan Imgur bahwa gambar sudah tidak tersedia. Tidak diganti dengan logo stasiun lain.
- Belum diuji langsung di TV/STB/HP; koneksi perangkat dan cache dapat memengaruhi waktu tampil logo.

## Sumber gambar

- **KTV Sport**: https://i.imgur.com/R1hGX1d.png
- **KTV Sport Plus**: https://i.imgur.com/l4oX0gf.png
- **Oman Sports TV**: https://i.imgur.com/1omi7p8.png
- **FTF Sports**: https://i.imgur.com/yvUjOI3.png
- **TVS Sports**: https://i.imgur.com/Lwwq62E.png
- **Bahrain Sports 1**: https://i.imgur.com/fBpLsbC.png
- **Bahrain Sports 2**: https://i.imgur.com/ZkuZmIo.png
- **HTSpor TV**: https://www.htspor.com/images/manifest/social-share-logo.png
- **More Than Sports TV**: https://i.imgur.com/SLrjImc.png
- **Motorvision TV**: https://i.imgur.com/v4kj8pI.png
- **Jordan Sport**: https://i.imgur.com/2EmrZPQ.png
- **Al Iraqia Sport**: https://i.imgur.com/DrrlxTO.png
- **Señal Colombia**: https://i.imgur.com/Y9x16vl.png
- **Bolivia TV**: https://i.imgur.com/tKLYYys.png
- **RCTV Cirebon**: https://play-lh.googleusercontent.com/2IbcLcLYOu2NvgYc98F15YcMQQfcUeH98fgpDfuThqihzOQomqy6VHRzlUefIO50rpY%3Dw240-h480
- **Banten TV**: https://app.bantentv.com/wp-content/uploads/2025/05/BANTEN_TV-768x92.png
- **Jogja TV**: https://jogjatv.co.id/templates/frontend/jtv/images/logo-atas.png
- **Banyumas TV**: https://bmstv.net/wp-content/uploads/2024/02/BMS-LOGO-2024-kecil.png
- **Radar Tasikmalaya TV**: https://radartasikmalaya.tv/wp-content/uploads/2022/12/cropped-logo-radar-tv-baru-november-20172-1-e1684336104958.png

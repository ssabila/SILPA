# SILPA - Sistem Informasi Layanan Perizinan Akademik

Aplikasi backend untuk mengelola pengajuan perizinan mahasiswa di institusi pendidikan. Sistem ini memfasilitasi proses pengajuan izin sakit, dispensasi institusi, dan izin alasan penting secara digital.

## Fitur Utama

### Untuk Mahasiswa
- Mengajukan perizinan dengan detail sesi kuliah dan berkas pendukung
- Melihat riwayat dan status pengajuan
- Melakukan revisi perizinan yang ditolak
- Dashboard personal dengan statistik perizinan
- Notifikasi perubahan status

### Untuk Admin
- Mengelola semua pengajuan perizinan
- Menyetujui, menolak, atau meminta revisi
- Dashboard dengan statistik pengajuan
- Melihat detail mahasiswa dan riwayat perizinannya
- Filter dan pencarian perizinan

## Teknologi

- Java 17
- Spring Boot 3.5.6
- Spring Security dengan JWT Authentication
- Spring Data JPA
- MySQL
- Lombok
- SpringDoc OpenAPI (Swagger)

## Prasyarat

- JDK 17 atau lebih tinggi
- MySQL 8.0 atau lebih tinggi
- Maven 3.6 atau lebih tinggi

## Instalasi

1. Clone repository

2. Konfigurasi database di `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/silpa_db
spring.datasource.username=root
spring.datasource.password=your_password
```

3. Build aplikasi:
```bash
mvn clean install
```

4. Jalankan aplikasi:
```bash
mvn spring-boot:run
```

Aplikasi akan berjalan di `http://localhost:8080`

## API Documentation

Dokumentasi API tersedia di Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

## Struktur Endpoint

### Authentication
- `POST /api/auth/register` - Registrasi pengguna baru
- `POST /api/auth/login` - Login dan mendapatkan JWT token

### Perizinan (Mahasiswa)
- `POST /api/perizinan` - Ajukan izin baru
- `GET /api/perizinan/saya` - Lihat perizinan sendiri
- `PUT /api/perizinan/{id}/revisi` - Revisi perizinan
- `DELETE /api/perizinan/{id}` - Hapus perizinan
- `PATCH /api/perizinan/{id}/deskripsi` - Update deskripsi

### Perizinan (Admin)
- `GET /api/perizinan` - Lihat semua perizinan
- `PUT /api/perizinan/{id}/status` - Update status perizinan
- `GET /api/perizinan/filter` - Filter perizinan

### Profil Pengguna
- `GET /api/pengguna/saya` - Lihat profil
- `PUT /api/pengguna/saya` - Update profil lengkap
- `PATCH /api/pengguna/saya/nama` - Update nama saja
- `PATCH /api/pengguna/saya/email` - Update email saja
- `PUT /api/pengguna/saya/kata-sandi` - Ganti password
- `DELETE /api/pengguna/saya` - Hapus akun

### Admin
- `GET /api/admin/mahasiswa` - Lihat daftar mahasiswa
- `GET /api/admin/mahasiswa/{id}` - Detail mahasiswa
- `GET /api/admin/dashboard` - Dashboard admin

### Statistik
- `GET /api/statistik/per-bulan` - Statistik per bulan
- `GET /api/statistik/per-jenis-izin` - Statistik per jenis izin
- `GET /api/statistik/trend` - Trend pengajuan

## Autentikasi

Sistem menggunakan JWT Bearer Token. Setelah login, sertakan token di header:
```
Authorization: Bearer {your_token}
```

## Jenis Perizinan

1. **Sakit**
   - Rawat Jalan (bobot: 60)
   - Rawat Inap (bobot: 100)

2. **Dispensasi Institusi** (bobot: 100)

3. **Izin Alasan Penting** (bobot: 100)
   - Keluarga Inti Meninggal
   - Bencana Alam
   - Pasangan Melahirkan

## Aturan Pengajuan

- Izin sakit dan alasan penting: maksimal 7 hari setelah kejadian
- Dispensasi institusi: harus sebelum hari pelaksanaan
- Tidak boleh ada perizinan yang overlap dengan tanggal yang sama
- Berkas pendukung wajib diunggah
- Ukuran file maksimal 2MB per file

## Status Perizinan

- `DIAJUKAN` - Baru diajukan, menunggu review
- `PERLU_REVISI` - Admin meminta revisi
- `DISETUJUI` - Perizinan disetujui
- `DITOLAK` - Perizinan ditolak

## File Upload

File yang diunggah disimpan di direktori `./uploads/{perizinan_id}/`. Dapat diakses melalui endpoint:
```
GET /files/{perizinan_id}/{nama_file}
```

## Kontributor

Dikembangkan sebagai bagian dari tugas PPK STIS 2025.

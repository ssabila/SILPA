package ppk.silpa.entity;

import lombok.Getter;

@Getter
public enum DetailIzin {
    // Sakit
    RAWAT_JALAN(
            JenisIzin.SAKIT,
            "Rawat Jalan",
            "Izin karena sakit namun tidak perlu menginap di fasilitas kesehatan.",
            "Wajib: Surat Keterangan Dokter, Resep Obat, SS-an mobile JKN (jika memakai BPJS)."
    ),
    RAWAT_INAP(
            JenisIzin.SAKIT,
            "Rawat Inap",
            "Izin karena sakit dan perlu menginap di fasilitas kesehatan (klinik/RS).",
            "Wajib: Surat Keterangan Rawat Inap dari Faskes, SS-an mobile JKN (jika memakai BPJS)."
    ),

    // Dispensasi
    DISPENSASI(
            JenisIzin.DISPENSASI_INSTITUSI,
            "Dispensasi Institusi",
            "Penugasan resmi dari institusi (Polstat STIS) untuk mengikuti kegiatan, lomba, atau tugas lainnya.",
            "Wajib: Surat Tugas atau Surat Keterangan Dispensasi dari unit terkait (misal: BEM, Bagian Akademik)."
    ),

    // Alasan Penting
    KELUARGA_INTI_MENINGGAL(
            JenisIzin.IZIN_ALASAN_PENTING,
            "Keluarga Inti Meninggal",
            "Izin karena anggota keluarga inti (orang tua, saudara kandung) meninggal dunia.",
            "Wajib: Surat Keterangan Kematian."
    ),
    BENCANA(
            JenisIzin.IZIN_ALASAN_PENTING,
            "Bencana Alam",
            "Izin karena terdampak langsung bencana alam di daerah tempat tinggal.",
            "Wajib: Bukti foto atau surat keterangan dari RT/RW setempat."
    ),
    PASANGAN_MELAHIRKAN(
            JenisIzin.IZIN_ALASAN_PENTING,
            "Pasangan Melahirkan",
            "Izin untuk mendampingi istri/pasangan yang melahirkan.",
            "Wajib: Surat Keterangan Lahir dari Faskes."
    );

    private final JenisIzin jenis;
    private final String namaTampilan;
    private final String deskripsi;
    private final String syarat;

    DetailIzin(JenisIzin jenis, String namaTampilan, String deskripsi, String syarat) {
        this.jenis = jenis;
        this.namaTampilan = namaTampilan;
        this.deskripsi = deskripsi;
        this.syarat = syarat;
    }
}


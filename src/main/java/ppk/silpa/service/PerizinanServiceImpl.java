package ppk.silpa.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ppk.silpa.dto.AjukanIzinDto;
import ppk.silpa.dto.PerizinanDto;
import ppk.silpa.dto.UpdateStatusDto;
import ppk.silpa.exception.ResourceNotFoundException;
import ppk.silpa.exception.SilpaAPIException;
import ppk.silpa.entity.*;
import ppk.silpa.repository.BerkasRepository;
import ppk.silpa.repository.PenggunaRepository;
import ppk.silpa.repository.PerizinanRepository;
import ppk.silpa.util.PerizinanMapper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerizinanServiceImpl implements PerizinanService {

    private final PerizinanRepository perizinanRepository;
    private final PenggunaRepository penggunaRepository;
    private final BerkasRepository berkasRepository;
    private final FileStorageService fileStorageService;

    // Konstanta untuk validasi
    private static final int MAX_HARI_PENGAJUAN = 7;

    public PerizinanServiceImpl(
            PerizinanRepository perizinanRepository,
            PenggunaRepository penggunaRepository,
            BerkasRepository berkasRepository,
            FileStorageService fileStorageService) {
        this.perizinanRepository = perizinanRepository;
        this.penggunaRepository = penggunaRepository;
        this.berkasRepository = berkasRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public PerizinanDto ajukanPerizinan(AjukanIzinDto ajukanIzinDto, List<MultipartFile> daftarBerkas) {
        Pengguna mahasiswa = getCurrentUser();

        // VALIDASI 1: Tanggal pengajuan sesuai tipe izin
        validateTanggalPengajuan(ajukanIzinDto.getJenisIzin(),
                ajukanIzinDto.getDetailIzin(),
                ajukanIzinDto.getTanggalMulai());

        // VALIDASI 2: Range tanggal valid
        validateRangeTanggal(ajukanIzinDto.getTanggalMulai(),
                ajukanIzinDto.getTanggalSelesai());

        // VALIDASI 3: Tidak ada perizinan yang overlap
        validateNoDuplicatePerizinan(mahasiswa.getId(),
                ajukanIzinDto.getTanggalMulai(),
                ajukanIzinDto.getTanggalSelesai());

        // Buat perizinan baru
        Perizinan perizinan = new Perizinan();
        perizinan.setMahasiswa(mahasiswa);
        perizinan.setJenisIzin(ajukanIzinDto.getJenisIzin());
        perizinan.setDetailIzin(ajukanIzinDto.getDetailIzin());
        perizinan.setTanggalMulai(ajukanIzinDto.getTanggalMulai());
        perizinan.setTanggalSelesai(ajukanIzinDto.getTanggalSelesai());
        perizinan.setDeskripsi(ajukanIzinDto.getDeskripsi());
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setBobotKehadiran(hitungBobotKehadiran(perizinan.getJenisIzin(), perizinan.getDetailIzin()));

        Perizinan perizinanTersimpan = perizinanRepository.save(perizinan);

        // Simpan berkas
        for (MultipartFile file : daftarBerkas) {
            String namaFile = fileStorageService.simpanFile(file, perizinanTersimpan.getId());
            Berkas berkas = new Berkas();
            berkas.setNamaFile(namaFile);
            berkas.setUrlAksesFile("/files/" + perizinanTersimpan.getId() + "/" + namaFile);
            berkas.setPerizinan(perizinanTersimpan);
            berkasRepository.save(berkas);
        }

        return PerizinanMapper.mapToPerizinanDto(
                perizinanRepository.findById(perizinanTersimpan.getId()).get());
    }

    @Override
    public PerizinanDto perbaruiPerizinan(Long perizinanId, AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkasBaru) {
        Pengguna mahasiswa = getCurrentUser();

        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        // Validasi hak akses
        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN,
                    "Anda tidak memiliki hak untuk mengubah perizinan ini.");
        }

        // Validasi status - hanya PERLU_REVISI yang boleh direvisi
        if (perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Hanya perizinan dengan status PERLU_REVISI yang dapat diubah.");
        }

        // VALIDASI: Range tanggal valid
        validateRangeTanggal(ajukanIzinDto.getTanggalMulai(),
                ajukanIzinDto.getTanggalSelesai());

        // VALIDASI: Tidak ada duplikat (exclude perizinan ini sendiri)
        validateNoDuplicatePerizinan(mahasiswa.getId(),
                ajukanIzinDto.getTanggalMulai(),
                ajukanIzinDto.getTanggalSelesai(),
                perizinanId);

        // Update data perizinan
        perizinan.setJenisIzin(ajukanIzinDto.getJenisIzin());
        perizinan.setDetailIzin(ajukanIzinDto.getDetailIzin());
        perizinan.setTanggalMulai(ajukanIzinDto.getTanggalMulai());
        perizinan.setTanggalSelesai(ajukanIzinDto.getTanggalSelesai());
        perizinan.setDeskripsi(ajukanIzinDto.getDeskripsi());
        perizinan.setBobotKehadiran(hitungBobotKehadiran(perizinan.getJenisIzin(), perizinan.getDetailIzin()));
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setCatatanAdmin(null);

        // Hapus berkas lama
        perizinan.getDaftarBerkas().clear();

        // Tambahkan berkas baru
        for (MultipartFile file : berkasBaru) {
            String namaFile = fileStorageService.simpanFile(file, perizinan.getId());
            Berkas berkas = new Berkas();
            berkas.setNamaFile(namaFile);
            berkas.setUrlAksesFile("/files/" + perizinan.getId() + "/" + namaFile);
            berkas.setPerizinan(perizinan);
            perizinan.getDaftarBerkas().add(berkas);
        }

        Perizinan perizinanDiperbarui = perizinanRepository.save(perizinan);
        return PerizinanMapper.mapToPerizinanDto(perizinanDiperbarui);
    }

    @Override
    public List<PerizinanDto> getPerizinanByMahasiswa() {
        Pengguna mahasiswa = getCurrentUser();
        List<Perizinan> daftarPerizinan = perizinanRepository.findByMahasiswaId(mahasiswa.getId());
        return daftarPerizinan.stream()
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());
    }

    @Override
    public void hapusPerizinan(Long perizinanId) {
        Pengguna mahasiswa = getCurrentUser();
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        // Validasi hak akses
        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN,
                    "Anda tidak memiliki hak untuk menghapus perizinan ini.");
        }

        // Validasi status - hanya DIAJUKAN dan PERLU_REVISI yang boleh dihapus
        if (perizinan.getStatus() != StatusPengajuan.DIAJUKAN &&
                perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Hanya perizinan yang belum diproses yang dapat dihapus.");
        }

        perizinanRepository.delete(perizinan);
    }

    @Override
    public List<PerizinanDto> getSemuaPerizinan() {
        return perizinanRepository.findAll().stream()
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());
    }

    @Override
    public PerizinanDto getPerizinanById(Long perizinanId) {
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));
        return PerizinanMapper.mapToPerizinanDto(perizinan);
    }

    @Override
    public PerizinanDto perbaruiStatusPerizinan(Long perizinanId, UpdateStatusDto updateStatusDto) {
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        // Validasi: Status DISETUJUI dan DITOLAK tidak boleh diubah
        if (perizinan.getStatus() == StatusPengajuan.DISETUJUI ||
                perizinan.getStatus() == StatusPengajuan.DITOLAK) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Perizinan yang sudah disetujui atau ditolak tidak dapat diubah!");
        }

        perizinan.setStatus(updateStatusDto.getStatus());
        perizinan.setCatatanAdmin(updateStatusDto.getCatatanAdmin());

        Perizinan perizinanDiperbarui = perizinanRepository.save(perizinan);
        return PerizinanMapper.mapToPerizinanDto(perizinanDiperbarui);
    }

    // ============================================
    // PRIVATE VALIDATION METHODS
    // ============================================

    /**
     * Validasi tanggal pengajuan sesuai dengan jenis izin
     */
    private void validateTanggalPengajuan(JenisIzin jenisIzin, DetailIzin detailIzin, LocalDate tanggalMulai) {
        LocalDate hariIni = LocalDate.now();

        // Untuk SAKIT dan IZIN_ALASAN_PENTING: Maksimal 7 hari setelah kejadian
        if (jenisIzin == JenisIzin.SAKIT || jenisIzin == JenisIzin.IZIN_ALASAN_PENTING) {
            long hariSelisih = ChronoUnit.DAYS.between(tanggalMulai, hariIni);

            if (hariSelisih > MAX_HARI_PENGAJUAN) {
                throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                        "Pengajuan " + jenisIzin + " hanya boleh dilakukan maksimal 7 hari setelah kejadian!");
            }

            if (hariSelisih < 0) {
                throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                        "Tanggal mulai tidak boleh di masa depan!");
            }
        }

        // Untuk DISPENSASI_INSTITUSI: Harus sebelum hari H
        if (jenisIzin == JenisIzin.DISPENSASI_INSTITUSI) {
            if (tanggalMulai.isBefore(hariIni)) {
                throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                        "Pengajuan dispensasi institusi harus dilakukan sebelum hari pelaksanaan!");
            }
        }
    }

    /**
     * Validasi range tanggal (mulai < selesai)
     */
    private void validateRangeTanggal(LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        if (tanggalMulai.isAfter(tanggalSelesai)) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Tanggal mulai tidak boleh lebih besar dari tanggal selesai!");
        }

        if (tanggalMulai.isEqual(tanggalSelesai)) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Tanggal mulai dan tanggal selesai tidak boleh sama!");
        }
    }

    /**
     * Validasi tidak ada perizinan yang overlap
     */
    private void validateNoDuplicatePerizinan(Long mahasiswaId, LocalDate tanggalMulai, LocalDate tanggalSelesai) {
        validateNoDuplicatePerizinan(mahasiswaId, tanggalMulai, tanggalSelesai, null);
    }

    /**
     * Validasi tidak ada perizinan yang overlap (exclude perizinan tertentu)
     */
    private void validateNoDuplicatePerizinan(Long mahasiswaId, LocalDate tanggalMulai,
                                              LocalDate tanggalSelesai, Long excludePerizinanId) {
        boolean adaDuplikat = perizinanRepository.findByMahasiswaId(mahasiswaId)
                .stream()
                // Exclude perizinan yang sedang di-revisi (jika ada)
                .filter(p -> excludePerizinanId == null || !p.getId().equals(excludePerizinanId))
                // Exclude perizinan yang sudah ditolak
                .filter(p -> p.getStatus() != StatusPengajuan.DITOLAK)
                // Check overlap
                .anyMatch(p -> isDateRangeOverlap(tanggalMulai, tanggalSelesai,
                        p.getTanggalMulai(), p.getTanggalSelesai()));

        if (adaDuplikat) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST,
                    "Terdapat perizinan lain yang overlap dengan tanggal ini!");
        }
    }

    /**
     * Check apakah dua date range overlap
     * Overlap jika: (mulai1 <= selesai2) AND (selesai1 >= mulai2)
     */
    private boolean isDateRangeOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    /**
     * Hitung bobot kehadiran berdasarkan jenis dan detail izin
     */
    private int hitungBobotKehadiran(JenisIzin jenisIzin, DetailIzin detailIzin) {
        if (jenisIzin == JenisIzin.SAKIT) {
            // Rawat inap: 100%, Rawat jalan: 60%
            return detailIzin == DetailIzin.RAWAT_INAP ? 100 : 60;
        }
        // Dispensasi Institusi dan Izin Alasan Penting: 100%
        return 100;
    }

    /**
     * Ambil pengguna yang sedang login
     */
    private Pengguna getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan"));
    }
}
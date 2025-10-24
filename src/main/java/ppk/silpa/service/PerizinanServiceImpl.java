package ppk.silpa.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ppk.silpa.dto.AjukanIzinDto;
import ppk.silpa.dto.DetailSesiIzinDto;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PerizinanServiceImpl implements PerizinanService {

    private final PerizinanRepository perizinanRepository;
    private final PenggunaRepository penggunaRepository;
    private final BerkasRepository berkasRepository;
    private final FileStorageService fileStorageService;
    private final ValidasiPerizinanService validasiPerizinanService; // Tambahkan ini

    public PerizinanServiceImpl(
            PerizinanRepository perizinanRepository,
            PenggunaRepository penggunaRepository,
            BerkasRepository berkasRepository,
            FileStorageService fileStorageService,
            ValidasiPerizinanService validasiPerizinanService) { // Perbarui konstruktor
        this.perizinanRepository = perizinanRepository;
        this.penggunaRepository = penggunaRepository;
        this.berkasRepository = berkasRepository;
        this.fileStorageService = fileStorageService;
        this.validasiPerizinanService = validasiPerizinanService; // Tambahkan ini
    }

    @Override
    public PerizinanDto ajukanPerizinan(AjukanIzinDto ajukanIzinDto, List<MultipartFile> daftarBerkas) {
        Pengguna mahasiswa = getCurrentUser();

        // 1. Tentukan Tanggal Mulai dan Selesai secara otomatis
        LocalDate tanggalMulai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid."));

        LocalDate tanggalSelesai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid."));

        // 2. Gunakan service validasi
        validasiPerizinanService.validasiTanggalPengajuan(ajukanIzinDto.getJenisIzin(), ajukanIzinDto.getDetailIzin(), tanggalMulai);
        // Kita asumsikan validasi 'range' tidak lagi relevan, atau kita ubah
        // validasiPerizinanService.validasiRangeTanggal(tanggalMulai, tanggalSelesai);
        validasiPerizinanService.validasiTidakAdaDuplikat(mahasiswa.getId(), tanggalMulai, tanggalSelesai);

        // 3. Buat Perizinan (Induk)
        Perizinan perizinan = new Perizinan();
        perizinan.setMahasiswa(mahasiswa);
        perizinan.setJenisIzin(ajukanIzinDto.getJenisIzin());
        perizinan.setDetailIzin(ajukanIzinDto.getDetailIzin());
        perizinan.setTanggalMulai(tanggalMulai); // Diisi dari langkah 1
        perizinan.setTanggalSelesai(tanggalSelesai); // Diisi dari langkah 1
        perizinan.setDeskripsi(ajukanIzinDto.getDeskripsi());
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setBobotKehadiran(hitungBobotKehadiran(perizinan.getJenisIzin(), perizinan.getDetailIzin()));

        // 4. Ubah DTO Sesi (anak) menjadi Entity Sesi (anak)
        for (DetailSesiIzinDto sesiDto : ajukanIzinDto.getDaftarSesi()) {
            DetailSesiIzin sesiEntity = new DetailSesiIzin();
            sesiEntity.setTanggal(sesiDto.getTanggal());
            sesiEntity.setNamaMataKuliah(sesiDto.getNamaMataKuliah());
            sesiEntity.setNamaDosen(sesiDto.getNamaDosen());
            sesiEntity.setSesi1(sesiDto.isSesi1());
            sesiEntity.setSesi2(sesiDto.isSesi2());
            sesiEntity.setSesi3(sesiDto.isSesi3());

            // Set relasi
            sesiEntity.setPerizinan(perizinan);

            // Tambahkan ke daftar di induk
            perizinan.getDaftarSesiIzin().add(sesiEntity);
        }

        // 5. Simpan Induk (Cascade.ALL akan menyimpan anak-anaknya)
        Perizinan perizinanTersimpan = perizinanRepository.save(perizinan);

        // 6. Simpan Berkas (Logika ini tetap sama)
        for (MultipartFile file : daftarBerkas) {
            String namaFile = fileStorageService.simpanFile(file, perizinanTersimpan.getId());
            Berkas berkas = new Berkas();
            berkas.setNamaFile(namaFile);
            berkas.setUrlAksesFile("/files/" + perizinanTersimpan.getId() + "/" + namaFile);
            berkas.setPerizinan(perizinanTersimpan);
            berkasRepository.save(berkas);
        }

        // 7. Ambil kembali dari DB untuk memastikan semua relasi ter-load
        return PerizinanMapper.mapToPerizinanDto(
                perizinanRepository.findById(perizinanTersimpan.getId()).get());
    }

    @Override
    public PerizinanDto perbaruiPerizinan(Long perizinanId, AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkasBaru) {
        Pengguna mahasiswa = getCurrentUser();

        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN, "Anda tidak memiliki hak untuk mengubah perizinan ini.");
        }
        if (perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Hanya perizinan dengan status PERLU_REVISI yang dapat diubah.");
        }

        // 1. Tentukan Tanggal Mulai dan Selesai baru
        LocalDate tanggalMulai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid."));

        LocalDate tanggalSelesai = ajukanIzinDto.getDaftarSesi().stream()
                .map(DetailSesiIzinDto::getTanggal)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new SilpaAPIException(HttpStatus.BAD_REQUEST, "Daftar sesi tidak valid."));

        // 2. Update data perizinan (Induk)
        perizinan.setJenisIzin(ajukanIzinDto.getJenisIzin());
        perizinan.setDetailIzin(ajukanIzinDto.getDetailIzin());
        perizinan.setTanggalMulai(tanggalMulai);
        perizinan.setTanggalSelesai(tanggalSelesai);
        perizinan.setDeskripsi(ajukanIzinDto.getDeskripsi());
        perizinan.setBobotKehadiran(hitungBobotKehadiran(perizinan.getJenisIzin(), perizinan.getDetailIzin()));
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setCatatanAdmin(null);

        // 3. Hapus berkas lama dan Sesi lama (orphanRemoval=true akan menghapusnya dari DB)
        perizinan.getDaftarBerkas().clear();
        perizinan.getDaftarSesiIzin().clear();

        // 4. Tambahkan berkas baru
        for (MultipartFile file : berkasBaru) {
            String namaFile = fileStorageService.simpanFile(file, perizinan.getId());
            Berkas berkas = new Berkas();
            berkas.setNamaFile(namaFile);
            berkas.setUrlAksesFile("/files/" + perizinan.getId() + "/" + namaFile);
            berkas.setPerizinan(perizinan);
            perizinan.getDaftarBerkas().add(berkas);
        }

        // 5. Tambahkan Sesi baru
        for (DetailSesiIzinDto sesiDto : ajukanIzinDto.getDaftarSesi()) {
            DetailSesiIzin sesiEntity = new DetailSesiIzin();
            sesiEntity.setTanggal(sesiDto.getTanggal());
            sesiEntity.setNamaMataKuliah(sesiDto.getNamaMataKuliah());
            sesiEntity.setNamaDosen(sesiDto.getNamaDosen());
            sesiEntity.setSesi1(sesiDto.isSesi1());
            sesiEntity.setSesi2(sesiDto.isSesi2());
            sesiEntity.setSesi3(sesiDto.isSesi3());
            sesiEntity.setPerizinan(perizinan);
            perizinan.getDaftarSesiIzin().add(sesiEntity);
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

    private int hitungBobotKehadiran(JenisIzin jenisIzin, DetailIzin detailIzin) {
        if (jenisIzin == JenisIzin.SAKIT) {
            return detailIzin == DetailIzin.RAWAT_INAP ? 100 : 60;
        }
        return 100;
    }

    private Pengguna getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan"));
    }
}
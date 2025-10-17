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

        // Gunakan service validasi
        validasiPerizinanService.validasiTanggalPengajuan(ajukanIzinDto.getJenisIzin(), ajukanIzinDto.getDetailIzin(), ajukanIzinDto.getTanggalMulai());
        validasiPerizinanService.validasiRangeTanggal(ajukanIzinDto.getTanggalMulai(), ajukanIzinDto.getTanggalSelesai());
        validasiPerizinanService.validasiTidakAdaDuplikat(mahasiswa.getId(), ajukanIzinDto.getTanggalMulai(), ajukanIzinDto.getTanggalSelesai());

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

    // ... (sisa metode lainnya tetap sama, tidak perlu diubah) ...

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
        validasiPerizinanService.validasiRangeTanggal(ajukanIzinDto.getTanggalMulai(),
                ajukanIzinDto.getTanggalSelesai());


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
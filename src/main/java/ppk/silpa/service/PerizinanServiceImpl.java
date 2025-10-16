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

    public PerizinanServiceImpl(PerizinanRepository perizinanRepository, PenggunaRepository penggunaRepository, BerkasRepository berkasRepository, FileStorageService fileStorageService) {
        this.perizinanRepository = perizinanRepository;
        this.penggunaRepository = penggunaRepository;
        this.berkasRepository = berkasRepository;
        this.fileStorageService = fileStorageService;
    }

    // ===================================
    // == FUNGSI UNTUK MAHASISWA ==
    // ===================================

    @Override
    public PerizinanDto ajukanPerizinan(AjukanIzinDto ajukanIzinDto, List<MultipartFile> daftarBerkas) {
        Pengguna mahasiswa = getCurrentPengguna();

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

        for (MultipartFile file : daftarBerkas) {
            String namaFile = fileStorageService.simpanFile(file, perizinanTersimpan.getId());
            Berkas berkas = new Berkas();
            berkas.setNamaFile(namaFile);
            berkas.setUrlAksesFile("/files/" + perizinanTersimpan.getId() + "/" + namaFile);
            berkas.setPerizinan(perizinanTersimpan);
            berkasRepository.save(berkas);
        }

        return PerizinanMapper.mapToPerizinanDto(perizinanRepository.findById(perizinanTersimpan.getId()).get());
    }

    @Override
    public PerizinanDto perbaruiPerizinan(Long perizinanId, AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkasBaru) {
        Pengguna mahasiswa = getCurrentPengguna();

        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN, "Anda tidak memiliki hak untuk mengubah perizinan ini.");
        }
        if (perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Hanya perizinan dengan status PERLU_REVISI yang dapat diubah.");
        }

        perizinan.setJenisIzin(ajukanIzinDto.getJenisIzin());
        perizinan.setDetailIzin(ajukanIzinDto.getDetailIzin());
        perizinan.setTanggalMulai(ajukanIzinDto.getTanggalMulai());
        perizinan.setTanggalSelesai(ajukanIzinDto.getTanggalSelesai());
        perizinan.setDeskripsi(ajukanIzinDto.getDeskripsi());
        perizinan.setBobotKehadiran(hitungBobotKehadiran(perizinan.getJenisIzin(), perizinan.getDetailIzin()));
        perizinan.setStatus(StatusPengajuan.DIAJUKAN);
        perizinan.setCatatanAdmin(null);

        // Hapus berkas lama (orphanRemoval=true akan otomatis menghapus dari DB)
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
        Pengguna mahasiswa = getCurrentPengguna();
        List<Perizinan> daftarPerizinan = perizinanRepository.findByMahasiswaId(mahasiswa.getId());
        return daftarPerizinan.stream()
                .map(PerizinanMapper::mapToPerizinanDto)
                .collect(Collectors.toList());
    }

    @Override
    public void hapusPerizinan(Long perizinanId) {
        Pengguna mahasiswa = getCurrentPengguna();
        Perizinan perizinan = perizinanRepository.findById(perizinanId)
                .orElseThrow(() -> new ResourceNotFoundException("Perizinan", "id", perizinanId));

        if (!perizinan.getMahasiswa().getId().equals(mahasiswa.getId())) {
            throw new SilpaAPIException(HttpStatus.FORBIDDEN, "Anda tidak memiliki hak untuk menghapus perizinan ini.");
        }
        if (perizinan.getStatus() != StatusPengajuan.DIAJUKAN && perizinan.getStatus() != StatusPengajuan.PERLU_REVISI) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Hanya perizinan yang belum diproses yang dapat dihapus.");
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

        perizinan.setStatus(updateStatusDto.getStatus());
        perizinan.setCatatanAdmin(updateStatusDto.getCatatanAdmin());

        Perizinan perizinanDiperbarui = perizinanRepository.save(perizinan);
        return PerizinanMapper.mapToPerizinanDto(perizinanDiperbarui);
    }

    private Pengguna getCurrentPengguna() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return penggunaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan"));
    }

    private int hitungBobotKehadiran(JenisIzin jenisIzin, DetailIzin detailIzin) {
        if (jenisIzin == JenisIzin.SAKIT) {
            return detailIzin == DetailIzin.RAWAT_INAP ? 100 : 60;
        }
        return 100;
    }
}


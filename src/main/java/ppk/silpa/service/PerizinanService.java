package ppk.silpa.service;

import ppk.silpa.dto.*;
import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import ppk.silpa.entity.StatusPengajuan;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface PerizinanService {
    PerizinanDto ajukanPerizinan(AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkas);
    List<PerizinanDto> getPerizinanByMahasiswa();
    PerizinanDto getPerizinanById(Long perizinanId);
    PerizinanDto perbaruiPerizinan(Long perizinanId, AjukanIzinDto ajukanIzinDto, List<MultipartFile> berkas);
    void hapusPerizinan(Long perizinanId);

    List<PerizinanDto> getSemuaPerizinan();
    PerizinanDto perbaruiStatusPerizinan(Long perizinanId, UpdateStatusDto updateStatusDto); // Hanya ini untuk update status

    AdminDashboardDto getAdminDashboardData();
    MahasiswaDashboardDto getMahasiswaDashboardData();

    List<PerizinanDto> filterPerizinan(
            StatusPengajuan status,
            JenisIzin jenisIzin,
            DetailIzin detailIzin,
            LocalDate tanggalMulaiDari,
            LocalDate tanggalMulaiSampai,
            Long mahasiswaId,
            String namaMahasiswa,
            Integer bulan,
            Integer tahun
    );

    PerizinanDto updateDeskripsi(Long perizinanId, UpdateDeskripsiDto updateDeskripsiDto); // Mahasiswa only

    List<StatistikPerBulanDto> getStatistikPerBulan();
    List<StatistikPerJenisDto> getStatistikPerJenisIzin();
    StatistikTrendDto getStatistikTrend();

}


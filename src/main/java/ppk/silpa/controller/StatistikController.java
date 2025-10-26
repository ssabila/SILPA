package ppk.silpa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppk.silpa.dto.ApiResponse;
import ppk.silpa.dto.StatistikPerBulanDto;
import ppk.silpa.dto.StatistikPerJenisDto;
import ppk.silpa.dto.StatistikTrendDto;
import ppk.silpa.service.PerizinanService;

import java.util.List;

@RestController
@RequestMapping("/api/statistik")
@PreAuthorize("hasAuthority('ADMIN')")
public class StatistikController {

    private final PerizinanService perizinanService;

    public StatistikController(PerizinanService perizinanService) {
        this.perizinanService = perizinanService;
    }

    @GetMapping("/per-bulan")
    public ResponseEntity<ApiResponse<List<StatistikPerBulanDto>>> getStatistikPerBulan() {
        List<StatistikPerBulanDto> statistik = perizinanService.getStatistikPerBulan();
        return ResponseEntity.ok(ApiResponse.sukses("Statistik per bulan berhasil diambil", statistik));
    }

    @GetMapping("/per-jenis-izin")
    public ResponseEntity<ApiResponse<List<StatistikPerJenisDto>>> getStatistikPerJenisIzin() {
        List<StatistikPerJenisDto> statistik = perizinanService.getStatistikPerJenisIzin();
        return ResponseEntity.ok(ApiResponse.sukses("Statistik per jenis izin berhasil diambil", statistik));
    }

    @GetMapping("/trend")
    public ResponseEntity<ApiResponse<StatistikTrendDto>> getStatistikTrend() {
        StatistikTrendDto trend = perizinanService.getStatistikTrend();
        return ResponseEntity.ok(ApiResponse.sukses("Statistik tren pengajuan berhasil diambil", trend));
    }
}

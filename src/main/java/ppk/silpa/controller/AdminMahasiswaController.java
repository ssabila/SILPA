package ppk.silpa.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ppk.silpa.dto.*;
import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import ppk.silpa.entity.StatusPengajuan;
import ppk.silpa.service.LayananPengguna;
import ppk.silpa.service.PerizinanService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/mahasiswa")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminMahasiswaController {

    private final LayananPengguna layananPengguna;
    private final PerizinanService perizinanService;

    public AdminMahasiswaController(LayananPengguna layananPengguna, PerizinanService perizinanService) {
        this.layananPengguna = layananPengguna;
        this.perizinanService = perizinanService;
    }

    @GetMapping
    public ResponseEntity<List<ProfilPenggunaDto>> getAllMahasiswa() {
        // Mengambil semua pengguna dengan peran MAHASISWA
        List<ProfilPenggunaDto> mahasiswaList = layananPengguna.getAllMahasiswaForAdmin();
        return ResponseEntity.ok(mahasiswaList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MahasiswaDetailAdminDto> getMahasiswaDetail(@PathVariable("id") Long mahasiswaId) {
        MahasiswaDetailAdminDto detailMahasiswa = layananPengguna.getMahasiswaDetailForAdmin(mahasiswaId);
        return ResponseEntity.ok(detailMahasiswa);
    }

    @GetMapping("/{id}/perizinan")
    public ResponseEntity<List<PerizinanDto>> getPerizinanMahasiswaById(
            @PathVariable("id") Long mahasiswaId,
            @RequestParam(required = false) StatusPengajuan status,
            @RequestParam(required = false) JenisIzin jenisIzin,
            @RequestParam(required = false) DetailIzin detailIzin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggalMulaiDari,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggalMulaiSampai,
            @RequestParam(required = false) Integer bulan,
            @RequestParam(required = false) Integer tahun
    ) {
        List<PerizinanDto> perizinanList = perizinanService.filterPerizinan(
                status, jenisIzin, detailIzin, tanggalMulaiDari, tanggalMulaiSampai,
                mahasiswaId, null, bulan, tahun);
        return ResponseEntity.ok(perizinanList);
    }
}


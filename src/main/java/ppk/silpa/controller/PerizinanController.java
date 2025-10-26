package ppk.silpa.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ppk.silpa.dto.*;
import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import ppk.silpa.entity.StatusPengajuan;
import ppk.silpa.service.PerizinanService;
import ppk.silpa.service.ValidasiBerkasService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/perizinan")
public class PerizinanController {

    private final PerizinanService perizinanService;
    private final ValidasiBerkasService validasiBerkas;

    public PerizinanController(PerizinanService perizinanService, ValidasiBerkasService validasiBerkas) {
        this.perizinanService = perizinanService;
        this.validasiBerkas = validasiBerkas;
    }

    @PreAuthorize("hasAuthority('MAHASISWA')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PerizinanDto> ajukanIzin(@Valid @RequestPart("izin") AjukanIzinDto ajukanIzinDto,
                                                   @RequestPart(value = "berkas", required = false) List<MultipartFile> berkas) {
        validasiBerkas.validasiBerkas( ajukanIzinDto.getJenisIzin(), ajukanIzinDto.getDetailIzin(), berkas);
        PerizinanDto perizinanBaru = perizinanService.ajukanPerizinan(ajukanIzinDto, berkas);
        return new ResponseEntity<>(perizinanBaru, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('MAHASISWA')")
    @PutMapping(value = "/{id}/revisi", consumes = {"multipart/form-data"})
    public ResponseEntity<PerizinanDto> revisiIzin(
            @PathVariable("id") Long perizinanId,
            @Valid @RequestPart("izin") AjukanIzinDto ajukanIzinDto,
            @RequestPart(value = "berkas", required = false) List<MultipartFile> berkasBaru) {
        validasiBerkas.validasiBerkas( ajukanIzinDto.getJenisIzin(), ajukanIzinDto.getDetailIzin(), berkasBaru);
        PerizinanDto perizinanDirevisi = perizinanService.perbaruiPerizinan(perizinanId, ajukanIzinDto, berkasBaru);
        return ResponseEntity.ok(perizinanDirevisi);
    }

    @PreAuthorize("hasAuthority('MAHASISWA')")
    @GetMapping("/saya")
    public ResponseEntity<List<PerizinanDto>> getPerizinanSaya() {
        return ResponseEntity.ok(perizinanService.getPerizinanByMahasiswa());
    }

    @PreAuthorize("hasAuthority('MAHASISWA')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> hapusIzin(@PathVariable("id") Long perizinanId) {
        perizinanService.hapusPerizinan(perizinanId);
        return ResponseEntity.ok(ApiResponse.sukses("Perizinan berhasil dihapus", null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/filter")
    public ResponseEntity<List<PerizinanDto>> filterPerizinan(
            @RequestParam(required = false) StatusPengajuan status,
            @RequestParam(required = false) JenisIzin jenisIzin,
            @RequestParam(required = false) DetailIzin detailIzin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggalMulaiDari,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tanggalMulaiSampai,
            @RequestParam(required = false) Long mahasiswaId,
            @RequestParam(required = false) String namaMahasiswa,
            @RequestParam(required = false) Integer bulan,
            @RequestParam(required = false) Integer tahun
    ) {
        List<PerizinanDto> hasil = perizinanService.filterPerizinan(status, jenisIzin, detailIzin, tanggalMulaiDari, tanggalMulaiSampai, mahasiswaId, namaMahasiswa, bulan, tahun);
        return ResponseEntity.ok(hasil);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PerizinanDto>> getSemuaPerizinan() {
        return ResponseEntity.ok(perizinanService.getSemuaPerizinan());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<PerizinanDto> getPerizinanById(@PathVariable("id") Long perizinanId) {
        return ResponseEntity.ok(perizinanService.getPerizinanById(perizinanId));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<PerizinanDto> perbaruiStatus(
            @PathVariable("id") Long perizinanId,
            @Valid @RequestBody UpdateStatusDto updateStatusDto) {

        PerizinanDto perizinanDiperbarui = perizinanService.perbaruiStatusPerizinan(perizinanId, updateStatusDto);
        return ResponseEntity.ok(perizinanDiperbarui);
    }

    @PreAuthorize("hasAuthority('MAHASISWA')")
    @PatchMapping("/{id}/deskripsi")
    public ResponseEntity<ApiResponse<PerizinanDto>> updateDeskripsi(
            @PathVariable("id") Long perizinanId,
            @Valid @RequestBody UpdateDeskripsiDto updateDeskripsiDto) {
        PerizinanDto updated = perizinanService.updateDeskripsi(perizinanId, updateDeskripsiDto);
        return ResponseEntity.ok(ApiResponse.sukses("Deskripsi perizinan berhasil diperbarui dan status diubah kembali ke DIAJUKAN", updated));
    }
}


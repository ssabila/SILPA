package ppk.silpa.controller;

import ppk.silpa.dto.AjukanIzinDto;
import ppk.silpa.dto.PerizinanDto;
import ppk.silpa.dto.UpdateStatusDto;
import ppk.silpa.service.PerizinanService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/perizinan")
public class PerizinanController {

    private final PerizinanService perizinanService;

    public PerizinanController(PerizinanService perizinanService) {
        this.perizinanService = perizinanService;
    }

    // == ENDPOINTS MAHASISWA ==
    @PreAuthorize("hasAuthority('MAHASISWA')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<PerizinanDto> ajukanIzin(@Valid @RequestPart("izin") AjukanIzinDto ajukanIzinDto,
                                                   @RequestPart("berkas") List<MultipartFile> berkas) {
        PerizinanDto perizinanBaru = perizinanService.ajukanPerizinan(ajukanIzinDto, berkas);
        return new ResponseEntity<>(perizinanBaru, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('MAHASISWA')")
    @GetMapping("/saya")
    public ResponseEntity<List<PerizinanDto>> getPerizinanSaya() {
        return ResponseEntity.ok(perizinanService.getPerizinanByMahasiswa());
    }

    // == ENDPOINTS ADMIN ==
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PerizinanDto>> getSemuaPerizinan() {
        return ResponseEntity.ok(perizinanService.getSemuaPerizinan());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PerizinanDto> getPerizinanById(@PathVariable("id") Long perizinanId){
        return ResponseEntity.ok(perizinanService.getPerizinanById(perizinanId));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<PerizinanDto> perbaruiStatus(@PathVariable("id") Long perizinanId,
                                                       @Valid @RequestBody UpdateStatusDto updateStatusDto) {
        PerizinanDto perizinanDiperbarui = perizinanService.perbaruiStatusPerizinan(perizinanId, updateStatusDto);
        return ResponseEntity.ok(perizinanDiperbarui);
    }
}

package ppk.silpa.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ppk.silpa.dto.ApiResponse;
import ppk.silpa.dto.NotifikasiDto;
import ppk.silpa.service.NotifikasiService;

import java.util.List;

@RestController
@RequestMapping("/api/notifikasi")
public class NotifikasiController {

    private final NotifikasiService notifikasiService;

    public NotifikasiController(NotifikasiService notifikasiService) {
        this.notifikasiService = notifikasiService;
    }

    @GetMapping("/saya")
    @PreAuthorize("isAuthenticated()") // Mahasiswa dan Admin bisa lihat notifikasi masing-masing
    public ResponseEntity<ApiResponse<List<NotifikasiDto>>> getNotifikasiSaya() {
        List<NotifikasiDto> notifikasiList = notifikasiService.getNotifikasiSaya();
        return ResponseEntity.ok(ApiResponse.sukses("Notifikasi berhasil diambil", notifikasiList));
    }
}

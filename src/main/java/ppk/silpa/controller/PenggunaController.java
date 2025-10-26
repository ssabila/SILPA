package ppk.silpa.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ppk.silpa.dto.*;
import ppk.silpa.service.LayananPengguna;

@RestController
@RequestMapping("/api/pengguna")
public class PenggunaController {

    private final LayananPengguna layananPengguna;

    public PenggunaController(LayananPengguna layananPengguna) {
        this.layananPengguna = layananPengguna;
    }

    @GetMapping("/saya")
    @PreAuthorize("isAuthenticated()") // Semua pengguna terotentikasi bisa akses
    public ResponseEntity<ApiResponse<ProfilPenggunaDto>> ambilProfil() {
        ProfilPenggunaDto profil = layananPengguna.ambilProfil();
        return ResponseEntity.ok(ApiResponse.sukses("Profil berhasil diambil", profil));
    }

    @PutMapping("/saya")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfilPenggunaDto>> perbaruiProfil(
            @Valid @RequestBody PerbaruiProfilDto perbaruiProfilDto) {
        ProfilPenggunaDto diperbarui = layananPengguna.perbaruiProfil(perbaruiProfilDto);
        return ResponseEntity.ok(ApiResponse.sukses("Profil berhasil diperbarui", diperbarui));
    }

    @PutMapping("/saya/kata-sandi")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> gantiKataSandi(
            @Valid @RequestBody GantiKataSandiDto gantiKataSandiDto) {
        layananPengguna.gantiKataSandi(gantiKataSandiDto);
        return ResponseEntity.ok(ApiResponse.sukses("Kata sandi berhasil diubah", null));
    }

    @DeleteMapping("/saya")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> hapusAkun() {
        layananPengguna.hapusAkun();
        return ResponseEntity.ok(ApiResponse.sukses("Akun berhasil dihapus", null));
    }

    @PatchMapping("/saya/nama")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfilPenggunaDto>> updateNama(
            @Valid @RequestBody UpdateNamaDto updateNamaDto) {
        ProfilPenggunaDto updatedProfil = layananPengguna.updateNama(updateNamaDto);
        return ResponseEntity.ok(ApiResponse.sukses("Nama lengkap berhasil diperbarui", updatedProfil));
    }

    @PatchMapping("/saya/email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProfilPenggunaDto>> updateEmail(
            @Valid @RequestBody UpdateEmailDto updateEmailDto) {
        ProfilPenggunaDto updatedProfil = layananPengguna.updateEmail(updateEmailDto);
        return ResponseEntity.ok(ApiResponse.sukses("Email berhasil diperbarui", updatedProfil));
    }

}

package ppk.silpa.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppk.silpa.dto.ApiResponse;
import ppk.silpa.dto.GantiKataSandiDto;
import ppk.silpa.dto.PerbaruiProfilDto;
import ppk.silpa.dto.ProfilPenggunaDto;
import ppk.silpa.service.LayananPengguna;

@RestController
@RequestMapping("/api/pengguna")
public class PenggunaController {

    private final LayananPengguna layananPengguna;

    public PenggunaController(LayananPengguna layananPengguna) {
        this.layananPengguna = layananPengguna;
    }

    @GetMapping("/saya")
    public ResponseEntity<ApiResponse<ProfilPenggunaDto>> ambilProfil() {
        ProfilPenggunaDto profil = layananPengguna.ambilProfil();
        return ResponseEntity.ok(ApiResponse.sukses("Profil berhasil diambil", profil));
    }

    @PutMapping("/saya")
    public ResponseEntity<ApiResponse<ProfilPenggunaDto>> perbaruiProfil(
            @Valid @RequestBody PerbaruiProfilDto perbaruiProfilDto) {
        ProfilPenggunaDto diperbarui = layananPengguna.perbaruiProfil(perbaruiProfilDto);
        return ResponseEntity.ok(ApiResponse.sukses("Profil berhasil diperbarui", diperbarui));
    }

    @PutMapping("/saya/kata-sandi")
    public ResponseEntity<ApiResponse<String>> gantiKataSandi(
            @Valid @RequestBody GantiKataSandiDto gantiKataSandiDto) {
        layananPengguna.gantiKataSandi(gantiKataSandiDto);
        return ResponseEntity.ok(ApiResponse.sukses("Kata sandi berhasil diubah", null));
    }

    @DeleteMapping("/saya")
    public ResponseEntity<ApiResponse<String>> hapusAkun() {
        layananPengguna.hapusAkun();
        return ResponseEntity.ok(ApiResponse.sukses("Akun berhasil dihapus", null));
    }
}
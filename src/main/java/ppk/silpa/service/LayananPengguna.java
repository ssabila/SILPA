package ppk.silpa.service;

import ppk.silpa.dto.*;

import java.util.List;

public interface LayananPengguna {
    // Metode Profil Pengguna
    ProfilPenggunaDto ambilProfil();
    ProfilPenggunaDto perbaruiProfil(PerbaruiProfilDto perbaruiProfilDto);
    void gantiKataSandi(GantiKataSandiDto gantiKataSandiDto);
    void hapusAkun();
    ProfilPenggunaDto updateNama(UpdateNamaDto updateNamaDto);
    ProfilPenggunaDto updateEmail(UpdateEmailDto updateEmailDto);

    // Metode Admin
    List<ProfilPenggunaDto> getAllMahasiswaForAdmin();
    MahasiswaDetailAdminDto getMahasiswaDetailForAdmin(Long mahasiswaId);
}


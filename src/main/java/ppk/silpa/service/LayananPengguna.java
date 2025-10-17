package ppk.silpa.service;

import ppk.silpa.dto.ProfilPenggunaDto;
import ppk.silpa.dto.GantiKataSandiDto;
import ppk.silpa.dto.PerbaruiProfilDto;

public interface LayananPengguna {
    ProfilPenggunaDto ambilProfil();
    ProfilPenggunaDto perbaruiProfil(PerbaruiProfilDto perbaruiProfilDto);
    void gantiKataSandi(GantiKataSandiDto gantiKataSandiDto);
    void hapusAkun();
}
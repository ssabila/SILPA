package ppk.silpa.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppk.silpa.dto.ProfilPenggunaDto;
import ppk.silpa.dto.GantiKataSandiDto;
import ppk.silpa.dto.PerbaruiProfilDto;
import ppk.silpa.entity.Pengguna;
import ppk.silpa.exception.SilpaAPIException;
import ppk.silpa.repository.PenggunaRepository;

@Service
@Transactional
public class LayananPenggunaImpl implements LayananPengguna {

    private final PenggunaRepository repositoriPengguna;
    private final PasswordEncoder penyandiBentuk;

    public LayananPenggunaImpl(PenggunaRepository repositoriPengguna, PasswordEncoder penyandiBentuk) {
        this.repositoriPengguna = repositoriPengguna;
        this.penyandiBentuk = penyandiBentuk;
    }

    @Override
    public ProfilPenggunaDto ambilProfil() {
        Pengguna pengguna = ambilPenggunaSekarang();
        return new ProfilPenggunaDto(
                pengguna.getId(),
                pengguna.getNamaLengkap(),
                pengguna.getEmail(),
                pengguna.getPeran().toString()
        );
    }

    @Override
    public ProfilPenggunaDto perbaruiProfil(PerbaruiProfilDto perbaruiProfilDto) {
        Pengguna pengguna = ambilPenggunaSekarang();

        // Cek apakah email baru sudah digunakan oleh pengguna lain
        if (!pengguna.getEmail().equals(perbaruiProfilDto.getEmail()) &&
                repositoriPengguna.existsByEmail(perbaruiProfilDto.getEmail())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Email sudah digunakan!");
        }

        pengguna.setNamaLengkap(perbaruiProfilDto.getNamaLengkap());
        pengguna.setEmail(perbaruiProfilDto.getEmail());

        Pengguna diperbarui = repositoriPengguna.save(pengguna);
        return new ProfilPenggunaDto(
                diperbarui.getId(),
                diperbarui.getNamaLengkap(),
                diperbarui.getEmail(),
                diperbarui.getPeran().toString()
        );
    }

    @Override
    public void gantiKataSandi(GantiKataSandiDto gantiKataSandiDto) {
        Pengguna pengguna = ambilPenggunaSekarang();

        // Validasi kata sandi lama
        if (!penyandiBentuk.matches(gantiKataSandiDto.getKataSandiLama(), pengguna.getKataSandi())) {
            throw new SilpaAPIException(HttpStatus.UNAUTHORIZED, "Kata sandi lama tidak sesuai!");
        }

        // Validasi kata sandi baru dan konfirmasi
        if (!gantiKataSandiDto.getKataSandiBaru().equals(gantiKataSandiDto.getKonfirmasiKataSandi())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Kata sandi baru dan konfirmasi tidak sesuai!");
        }

        // Jangan boleh sama dengan kata sandi lama
        if (penyandiBentuk.matches(gantiKataSandiDto.getKataSandiBaru(), pengguna.getKataSandi())) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Kata sandi baru tidak boleh sama dengan kata sandi lama!");
        }

        pengguna.setKataSandi(penyandiBentuk.encode(gantiKataSandiDto.getKataSandiBaru()));
        repositoriPengguna.save(pengguna);
    }

    @Override
    public void hapusAkun() {
        Pengguna pengguna = ambilPenggunaSekarang();
        repositoriPengguna.deleteById(pengguna.getId());
    }

    private Pengguna ambilPenggunaSekarang() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repositoriPengguna.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Pengguna tidak ditemukan"));
    }
}

package ppk.silpa.service;


import ppk.silpa.dto.LoginDto;
import ppk.silpa.dto.RegisterDto;
import ppk.silpa.exception.SilpaAPIException;
import ppk.silpa.entity.Pengguna;
import ppk.silpa.repository.PenggunaRepository;
import ppk.silpa.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final PenggunaRepository penggunaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(AuthenticationManager authenticationManager, PenggunaRepository penggunaRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.penggunaRepository = penggunaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public String login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getKataSandi()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    public String register(RegisterDto registerDto) {
        // Cek apakah email sudah ada
        if(penggunaRepository.existsByEmail(registerDto.getEmail())){
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Email sudah terdaftar!");
        }

        Pengguna pengguna = new Pengguna();
        pengguna.setNamaLengkap(registerDto.getNamaLengkap());
        pengguna.setEmail(registerDto.getEmail());
        pengguna.setKataSandi(passwordEncoder.encode(registerDto.getKataSandi()));
        pengguna.setPeran(registerDto.getPeran());

        penggunaRepository.save(pengguna);

        return "Pengguna berhasil diregistrasi!";
    }
}

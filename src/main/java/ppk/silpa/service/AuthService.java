package ppk.silpa.service;

import ppk.silpa.dto.LoginDto;
import ppk.silpa.dto.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);
    String register(RegisterDto registerDto);
}


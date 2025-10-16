package ppk.silpa.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String simpanFile(MultipartFile file, Long perizinanId);
    Resource muatFileSebagaiResource(String namaFile, Long perizinanId);
}


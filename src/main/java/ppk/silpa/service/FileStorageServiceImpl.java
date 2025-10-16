package ppk.silpa.service;

import ppk.silpa.config.FileStorageProperties;
import ppk.silpa.exception.FileStorageException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Tidak dapat membuat direktori untuk menyimpan file.", ex);
        }
    }

    @Override
    public String simpanFile(MultipartFile file, Long perizinanId) {
        String namaFileAsli = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        try {
            if (namaFileAsli.contains("..")) {
                throw new FileStorageException("Nama file mengandung karakter yang tidak valid " + namaFileAsli);
            }
            // Buat sub-direktori untuk setiap perizinan
            Path targetLocationPerizinan = this.fileStorageLocation.resolve(String.valueOf(perizinanId));
            Files.createDirectories(targetLocationPerizinan);

            Path targetLocation = targetLocationPerizinan.resolve(namaFileAsli);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return namaFileAsli;
        } catch (IOException ex) {
            throw new FileStorageException("Tidak dapat menyimpan file " + namaFileAsli + ". Silakan coba lagi!", ex);
        }
    }

    @Override
    public Resource muatFileSebagaiResource(String namaFile, Long perizinanId) {
        try {
            Path filePath = this.fileStorageLocation.resolve(String.valueOf(perizinanId)).resolve(namaFile).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File tidak ditemukan " + namaFile);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File tidak ditemukan " + namaFile, ex);
        }
    }
}


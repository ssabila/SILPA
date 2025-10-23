package ppk.silpa.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;
import ppk.silpa.exception.SilpaAPIException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class ValidasiBerkasServiceImpl implements ValidasiBerkasService {

    @Override
    public void validasiBerkas(JenisIzin jenisIzin, DetailIzin detailIzin, List<MultipartFile> berkas) {
        if (berkas == null || berkas.isEmpty()) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "Berkas tidak boleh kosong.");
        }

        if (berkas.stream().anyMatch(MultipartFile::isEmpty)) {
            throw new SilpaAPIException(HttpStatus.BAD_REQUEST, "File yang diunggah tidak boleh kosong.");
        }
    }
}
package ppk.silpa.service;

import org.springframework.web.multipart.MultipartFile;
import ppk.silpa.entity.DetailIzin;
import ppk.silpa.entity.JenisIzin;

import java.util.List;

public interface ValidasiBerkasService {
    void validasiBerkas(JenisIzin jenisIzin, DetailIzin detailIzin, List<MultipartFile> berkas);
}
package com.suhai.filetransformationb3.controller;

import com.suhai.filetransformationb3.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


@Slf4j
@Controller
@RequestMapping(value = "b3/file")
public class FileController {

    // TODO melhorar a aparência da página
    // TODO colocar autenticação
    // TODO subir na AWS
    // TODO colocar tratamento para exceções
    // TODO refatorar o código


    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/transformation")
    public String transformFile(@RequestParam("files") MultipartFile[] files, Model model) {
        Arrays.stream(files).forEach(file -> log.info("File name: " + file.getOriginalFilename()));
        model.addAttribute("fileNames", Arrays.stream(files).map(MultipartFile::getOriginalFilename).toArray(String[]::new));
        List<String> paths = fileService.transformFile(files);
        model.addAttribute("paths", paths);
        return "file-list";
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("path") String path) {
        try {
            File file = new File(path);
            FileInputStream inputStream = new FileInputStream(file);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + file.getName() );

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/downloads")
    public ResponseEntity<InputStreamResource> downloadFiles(@RequestParam("paths") String[] paths) {
        try {
            // Crie um arquivo ZIP temporário
            File zipFile = File.createTempFile("arquivos_b3", ".zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            // Adicione os arquivos do array de strings ao arquivo ZIP
            for (String path : paths) {
                path = path.replace("[", "").replace("]","");
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    FileInputStream fis = new FileInputStream(file);
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                    IOUtils.copy(fis, zos);
                    fis.close();
                }
            }

            zos.close();

            // Crie um InputStream a partir do arquivo ZIP
            FileInputStream zipInputStream = new FileInputStream(zipFile);

            // Configure o cabeçalho de resposta para o download do arquivo ZIP
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=download.zip");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(zipInputStream));
        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}

package com.suhai.filetransformationb3.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FileService {

    public List<String> transformFile(MultipartFile[] files) {
        List<String> paths = new ArrayList<>();

        for (MultipartFile file : files) {
            log.info("File name: " + file.getOriginalFilename());
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.toLowerCase().contains("sinistro")) {
                paths.add(createModifiedFile(file, "claim", 4));
            } else if (fileName != null) {
                paths.add(createModifiedFile(file, null, 2));
            }
        }
        return paths;
    }

    public String createModifiedFile(MultipartFile file, String tipo, int position) {
        try {
            InputStream inputStream = file.getInputStream();

            Path tempDir = Files.createTempDirectory("tempDir");

            String destinationFileName = tempDir.toString() + File.separator + file.getOriginalFilename();
            OutputStream outputStream = new FileOutputStream(destinationFileName);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

            String line;
            String claimNumber = "";
            String claimDate = "";
            String origin = "";

            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(";");
                if (Objects.equals(parts[0], "1")) {
                    claimNumber = parts[position];
                    if ("claim".equalsIgnoreCase(tipo)) {
                        origin = parts[1];
                        claimDate = parts[3];
                    }
                }
                if ("claim".equalsIgnoreCase(tipo)) {
                    line = parts[0] + ";" + origin + ";" + claimNumber + ";" + claimDate + ";" + line.substring(line.indexOf(";") + 1);
                } else {
                    line = parts[0] + ";" + claimNumber + ";" + line.substring(line.indexOf(";") + 1);
                }

                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
            bufferedReader.close();
            return destinationFileName;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

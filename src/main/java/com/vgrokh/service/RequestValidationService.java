package com.vgrokh.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


@Service
public class RequestValidationService {

    File directoryToUnzip;

    public void unZipFile(MultipartFile incomeFile){
        directoryToUnzip = new File("src/main/resources/unzip" + incomeFile.getName());
        try {
            ZipInputStream zipInputStream = new ZipInputStream(incomeFile.getInputStream());
            byte[] buffer = new byte[1024];
            ZipEntry zipEntry = zipInputStream.getNextEntry();
            while (zipEntry != null){
                File newFile = createNewFile(directoryToUnzip, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zipInputStream.read(buffer)) > 0) { fos.write(buffer, 0, len); }
                    fos.close();
                }
                zipEntry = zipInputStream.getNextEntry();
            }
            zipInputStream.closeEntry();
            zipInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            directoryToUnzip.delete();
        }
    }

    private static File createNewFile(File directoryToUnzip, ZipEntry zipEntry) throws IOException {
        File destinationFile = new File(directoryToUnzip, zipEntry.getName());
        String destDirPath  = directoryToUnzip.getCanonicalPath();
        String destFilePath = destinationFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());

        return destinationFile;
    }


    public boolean isIncomingFileValid(MultipartFile incomeFile){
        String fileName = StringUtils.removeEnd( incomeFile.getOriginalFilename(), ".zip");
        unZipFile(incomeFile);
        Path pathToVerify = Paths.get(directoryToUnzip + "/" + fileName + "/Dockerfile");
        if (Files.exists(pathToVerify)) {
            System.out.println("Dockerfile exists");
            return true;
        }
        return false;
    }
}

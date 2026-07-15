
package com.aria.service;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aria.config.Constants;
import com.aria.model.Status;
import com.aria.utils.SFTPConfig;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

@Service
public class FileStorageService {

    @Autowired
    SFTPConfig sftpConfig;

    @Autowired
    ServletContext servletContext;

    Logger logger = LoggerFactory.getLogger(FileStorageService.class);


    ChannelSftp sftp;
    LocalDate today;
    String curr_month;
    int curr_year;


    public ResponseEntity<?> storeDocuments(MultipartFile multiPartFile, String attachmentType)
            throws IOException, JSchException {

        String folderName;
        String fileName = System.currentTimeMillis() + "_" + multiPartFile.getOriginalFilename();
        System.out.println(fileName);

        if (attachmentType.equalsIgnoreCase("machineimage")) {
            folderName = Constants.FTP_MACHINE_IMAGE_LOCATION;
        } else if (attachmentType.equalsIgnoreCase("billcopy")) {
            folderName = Constants.FTP_BILL_COPY_LOCATION;
        } else if (attachmentType.equalsIgnoreCase("rentaldocument")) {
            folderName = Constants.FTP_RENTAL_DOCUMENT_LOCATION;
        } else if (attachmentType.equalsIgnoreCase("employee")) {
            folderName = Constants.FTP_EMPLOYEE_IMAGE_LOCATION;
        } else if (attachmentType.equalsIgnoreCase("labreport")) {
            folderName = Constants.FTP_LAB_REPORT_LOCATION;
        } else if (attachmentType.equalsIgnoreCase("aadhar")) {
            folderName = Constants.FTP_EMP_AADHAR_LOCATION;
        } else if (attachmentType.equalsIgnoreCase("quotation")) {
            folderName = Constants.FTP_PROPOSAL_QUOTATION;
        } else if (attachmentType.equalsIgnoreCase("qualityInspection")) {
            folderName = Constants.FTP_QUALITY_INSPECTION;
        } else {
            folderName = Constants.FTP_OTHERS_FILE_LOCATION;
        }
        String result = uploadTemplateFile(multiPartFile, folderName);

        if (result != null && !result.equalsIgnoreCase("")) {
            return new ResponseEntity<Status>(new Status("200", result), HttpStatus.OK);
        } else {
            return new ResponseEntity<Status>(new Status("500", "Not able to save a file"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadTemplateFile(MultipartFile templateFile, String path) {
        FileOutputStream fileOutputStream = null;
        String resultFilePath = null;
        try {

            System.out.println("== path == ");
            System.out.println("path == " + path);
            System.out.println("== path == ");
            File filePath = new File(path);
            if (!filePath.exists()) {
                filePath.mkdirs();
            }

           /* File filesList[] = filePath.listFiles();
            for (File file : filesList) {
                if (file.isFile()) {
                    file.delete();
                }
            }*/
            String fileName = templateFile.getOriginalFilename();
            System.out.println("fileName == " + fileName);
            resultFilePath = String.join("/", path, fileName);
            System.out.println("resultFilePath == " + resultFilePath);
            File uploadFile = new File(resultFilePath);

            uploadFile.setWritable(true);
            uploadFile.setReadable(true);
            uploadFile.setExecutable(true);
            fileOutputStream = new FileOutputStream(uploadFile);
            fileOutputStream.write(templateFile.getBytes());
        } catch (Exception e) {
            logger.error("Exception while uploading etl file", e);
            throw new RuntimeException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    logger.error("Exception while uploading etl file", e);
                    throw new RuntimeException(e);
                }
            }
        }
        return resultFilePath;
    }


    public String downloadFileAsBase64(String filePath) throws IOException {
        String base64File = "";
        File file = new File(filePath);
        try (FileInputStream imageInFile = new FileInputStream(file)) {
            // Reading a file from file system
            byte fileData[] = new byte[(int) file.length()];
            imageInFile.read(fileData);
            base64File = Base64.getEncoder().encodeToString(fileData);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
        } catch (IOException ioe) {
            System.out.println("Exception while reading the file " + ioe);
        }
        return base64File;
    }

    public File downloadFile(String fileName, HttpServletResponse response) throws IOException {
        String path = fileName;
        System.out.println("path == " + path);
        File findFile = new File(path);
        if (findFile.exists()) {
            return findFile;
        } else {
            return null;
        }
    }

    public Map<String, String> downloadFileAsBase64AsList(Map<String, String> fileNames) throws IOException {
        String base64String = null;
        byte[] temp = new byte[1024];
        Map<String, String> result = new HashMap<>();

        String path = Constants.FTP_MACHINE_IMAGE_LOCATION;

        try {
            File filePath = new File(path);

            File filesList[] = filePath.listFiles();
            if (filesList != null) {

                List<String> remoteFileNames = new ArrayList<>();

                int counterTemp = 0;
                for (File remoteFile : filesList) {
                    String fileName = Constants.FTP_MACHINE_IMAGE_LOCATION + "/" + remoteFile.getName();
                    System.out.println("====== fileName ======");
                    System.out.println("====== fileName ======" + fileName);
                    System.out.println("====== fileName ======");
                    if (counterTemp == fileNames.size())
                        break;
                    if (fileName.equals(fileNames.get(fileName))) {
                        counterTemp++;
                        remoteFileNames.add(fileName);
                    }
                }

                if (!remoteFileNames.isEmpty()) {

                    for (String fileNameTemp : remoteFileNames) {
                        System.out.println("====== fileNameTemp ======");
                        System.out.println("====== fileNameTemp ======" + fileNameTemp);
                        System.out.println("====== fileNameTemp ======");
                        try {
                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(fileNameTemp)));
                            System.out.println("####### = "+fileNameTemp);
                            temp = IOUtils.toByteArray(bis);
                            base64String = Base64.getEncoder().encodeToString(temp);
                            System.out.println("====== base64String ======");
                           // System.out.println("====== base64String ======" + base64String);
                            System.out.println("====== base64String ======");
                            result.put(fileNameTemp, base64String);
                        } catch (Exception e) {
                            System.out.println("Could not download the file " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }


        return result;


    }

/*
    //@PostConstruct
    public void init() {
        today = LocalDate.now();
        curr_month = today.getMonth().toString().substring(0, 3).toLowerCase();
        curr_year = today.getYear();
        try {
            sftp = sftpConfig.setupSFTPSession();
        } catch (JSchException e) {
            System.out.println("not get session from sftp " + e.getLocalizedMessage());
        }
        try {
            sftp.connect();
        } catch (JSchException e) {
            System.out.println("not able to connect sftp " + e.getLocalizedMessage());
        }
    }

    public ResponseEntity<?> storeFile(MultipartFile multiPartFile, String attachmentType)
            throws IOException, JSchException {

        String folderName = null;
//		String fileName = null;
        if (attachmentType.equalsIgnoreCase("machine")) {
            folderName = Constants.FTP_MEDIA_BASE_PATH + Constants.MACHINE_FOLDER;
        } else if (attachmentType.equalsIgnoreCase("employee")) {
            folderName = Constants.FTP_MEDIA_BASE_PATH + Constants.EMPLOYEE_FOLDER;
        } else {
            folderName = Constants.FTP_MEDIA_BASE_PATH + Constants.OTHERS_FOLDER;
        }

        String fileName = System.currentTimeMillis() + "_" + multiPartFile.getOriginalFilename();

        String uploadPath = folderName + "/" + Integer.toString(curr_year) + "/" + curr_month;

        String fileToSavePath = checkDirectoryExistOrNot(uploadPath, folderName);

        try {
            sftp.put(multiPartFile.getInputStream(), fileToSavePath + "/" + fileName);
            return new ResponseEntity<Status>(new Status("200", fileToSavePath + "/" + fileName), HttpStatus.OK);
        } catch (SftpException e) {
            System.out.println("not able to upload a file " + e.getLocalizedMessage());
        }
//		sftp.exit();
        return new ResponseEntity<Status>(new Status("500", "Not able to save a file"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public ResponseEntity<?> storeFileInMachineImagesFolder(MultipartFile multiPartFile, String attachmentType)
            throws IOException, JSchException {

        String folderName = Constants.FTP_MACHINE_IMAGE_LOCATION;
        String fileName = System.currentTimeMillis() + "_" + multiPartFile.getOriginalFilename();

        try {
            sftp.put(multiPartFile.getInputStream(), folderName + "/" + fileName);
            return new ResponseEntity<Status>(new Status("200", folderName + "/" + fileName), HttpStatus.OK);
        } catch (SftpException e) {
            System.out.println("not able to upload a file " + e.getLocalizedMessage());
        }
        return new ResponseEntity<Status>(new Status("500", "Not able to save a file"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private String checkDirectoryExistOrNot(String uploadPath, String folderName) throws IOException {
        SftpATTRS attrs = null;
        String year = Integer.toString(curr_year);
        try {
            attrs = sftp.stat(uploadPath);
        } catch (Exception e) {
            System.out.println("directory not found " + e.getLocalizedMessage());
        }
        if (attrs == null) {
            System.out.println("creating directory");

            try {
                SftpATTRS isYearAlreadyExist = null;
                try {
                    isYearAlreadyExist = sftp.stat(folderName + "/" + year);
                    System.out.println("109 " + folderName + "/" + year);
                } catch (Exception e) {

                    System.out.println("directory not found at year level " + e.getLocalizedMessage());
                    sftp.cd(folderName);
                    String path = folderName + "/" + year;
                    System.out.println("109 " + path);
                    sftp.mkdir(year);
                }
                SftpATTRS isMonthAlreadyExist = null;
                try {
                    isMonthAlreadyExist = sftp.stat(folderName + "/" + year + "/" + curr_month);
                    System.out.println("109 " + folderName + "/" + year + "/" + curr_month);
                } catch (Exception e) {
                    System.out.println("directory not found at month level" + e.getLocalizedMessage());
                    String path = folderName + "/" + year;

                    sftp.cd(path);
                    sftp.mkdir(curr_month);
                }
            } catch (Exception e) {
                System.out.println("error while creating directory " + e.getLocalizedMessage());
            }

        } else {
            System.out.println("directory exists");

        }

        return folderName + "/" + year + "/" + curr_month;

    }

    public boolean deleteFile(String fileName, String attachmentType) {
        boolean isDeleted = false;

        try {
            sftp.rm(fileName);
        } catch (SftpException e) {
            isDeleted = false;
            System.out.println("not able to delete a file" + e.getLocalizedMessage());
        }
//			sftp.exit();

        return isDeleted;
    }

    *//*public String downloadFileAsBase64(String fileName) throws IOException {
        boolean status = false;
        String base64String = null;
        byte[] temp = new byte[1024];
        SftpATTRS isYearAlreadyExist;
        try {
            sftp = sftpConfig.setupSFTPSession();
        } catch (JSchException e) {
            System.out.println("not get session from sftp " + e.getLocalizedMessage());
        }
        try {
            sftp.connect();
        } catch (JSchException e) {
            System.out.println("not able to connect sftp " + e.getLocalizedMessage());
        }
        try {
            isYearAlreadyExist = sftp.lstat(fileName);

        } catch (Exception e) {
            System.out.println("location not fond for " + fileName);
        }
        try (BufferedInputStream bis = new BufferedInputStream(sftp.get(fileName));) {
            System.out.println(fileName);
//			BufferedInputStream bis = new BufferedInputStream(sftp.get(fileName));
            temp = IOUtils.toByteArray(bis);
            base64String = Base64.getEncoder().encodeToString(temp);
            status = true;
        } catch (SftpException e) {
            status = false;
            System.out.println("Could not download the file " + e.getLocalizedMessage());
        }

        if (status) {
            System.out.println("Downloaded the file");
        } else {
            System.out.println("Could not download the file");
        }
        return base64String;
//		sftp.exit();

    }*//*


    *//*public Map<String, String> downloadFileAsBase64AsList(Map<String, String> fileNames) throws IOException {
        String base64String = null;
        byte[] temp = new byte[1024];
        Map<String, String> result = new HashMap<>();
        try {
            sftp = sftpConfig.setupSFTPSession();
        } catch (JSchException e) {
            System.out.println("not get session from sftp " + e.getLocalizedMessage());
        }
        try {
            sftp.connect();
        } catch (JSchException e) {
            System.out.println("not able to connect sftp " + e.getLocalizedMessage());
        }
        try {
            sftp.cd(Constants.FTP_MACHINE_IMAGE_LOCATION);
        } catch (SftpException e) {
            System.out.println("no directory " + e.getLocalizedMessage());
        }
        try {
            Vector<ChannelSftp.LsEntry> fileList = sftp.ls("*");
            if (fileList != null) {
                List<String> remoteFileNames = new ArrayList<>();

                int counterTemp = 0;
                for (ChannelSftp.LsEntry remoteFile : fileList) {
                    String fileName = Constants.FTP_MACHINE_IMAGE_LOCATION + "/" + remoteFile.getFilename();
                    if (counterTemp == fileNames.size())
                        break;
                    if (fileName.equals(fileNames.get(fileName))) {
                        counterTemp++;
                        remoteFileNames.add(remoteFile.getFilename());
                    }
                }

                for (String fileNameTemp : remoteFileNames) {
                    try {
                        BufferedInputStream bis = new BufferedInputStream(sftp.get(fileNameTemp));
                        System.out.println(fileNameTemp);
                        temp = IOUtils.toByteArray(bis);
                        base64String = Base64.getEncoder().encodeToString(temp);
                        result.put(Constants.FTP_MACHINE_IMAGE_LOCATION + "/" + fileNameTemp, base64String);
                    } catch (SftpException e) {
                        System.out.println("Could not download the file " + e.getLocalizedMessage());
                    }
                }


            }
        } catch (SftpException e) {
            e.printStackTrace();
        }


        return result;


    }*//*

   *//* public File downloadFile(String fileName, HttpServletResponse response) throws IOException {

        byte[] buffer = new byte[1024];
        String fn = fileName.split("/")[5];
        String ff = fn.split("\\.")[1];
        File newFile = File.createTempFile("downloaded_" + fn.split("\\.")[0], ff);


        try {
            sftp = sftpConfig.setupSFTPSession();
        } catch (JSchException e) {
            System.out.println("not get session from sftp " + e.getLocalizedMessage());
        }
        try {
            sftp.connect();
        } catch (JSchException e) {
            System.out.println("not able to connect sftp " + e.getLocalizedMessage());
        }
        try {
//			sftp.cd(Constants.FTP_BILL_COPY_LOCATION);
            sftp.cd(Constants.FTP_MEDIA_BASE_PATH + fileName.split("/")[4]);
        } catch (SftpException e) {
            System.out.println("not able to connect sftp " + e.getLocalizedMessage());
        }
        try {
            System.out.println(" file " + fileName);


            BufferedInputStream bis = new BufferedInputStream(sftp.get(fn));
//			File newFile = new File("C:\\Users\\TAMIL\\Desktop\\downloaded_" + fn);
            newFile = File.createTempFile(fn.split("\\.")[0], "." + fn.split("\\.")[1]);
            FileUtils.copyInputStreamToFile(bis, newFile);
//			}
            bis.close();
//			bos.close();

        } catch (SftpException e) {
            System.out.println("Could not download the file " + e.getLocalizedMessage());
        }
//		sftp.exit();
        return newFile;

    }
*//*


    public File downloadFile(String fileName, HttpServletResponse response) throws IOException {
        String path = fileName;
        System.out.println("path == " + path);
        File findFile = new File(path);
        if (findFile.exists()) {
            return findFile;
        } else {
            return null;
        }
    }


    public Map<String, String> downloadEmpFileAsBase64AsList(String filePath, Map<String, String> fileNames) throws IOException {
        String base64String = null;
        byte[] temp = new byte[1024];
        Map<String, String> result = new HashMap<>();
        try {
            sftp = sftpConfig.setupSFTPSession();
        } catch (JSchException e) {
            System.out.println("not get session from sftp " + e.getLocalizedMessage());
        }
        try {
            sftp.connect();
        } catch (JSchException e) {
            System.out.println("not able to connect sftp " + e.getLocalizedMessage());
        }
        try {
            sftp.cd(filePath);
        } catch (SftpException e) {
            System.out.println("no directory " + e.getLocalizedMessage());
        }
        try {
            Vector<ChannelSftp.LsEntry> fileList = sftp.ls("*");
            if (fileList != null) {
                List<String> remoteFileNames = new ArrayList<>();

                int counterTemp = 0;
                for (ChannelSftp.LsEntry remoteFile : fileList) {
                    String fileName = filePath + "/" + remoteFile.getFilename();

                    System.out.println("=====");
                    System.out.println("remote fileNme  = " + fileName);
                    System.out.println("=====");
                    System.out.println("curren fileNme  = " + fileNames.get(fileName));
                    System.out.println("=====");
                    if (counterTemp == fileNames.size())
                        break;
                    if (fileName.equals(fileNames.get(fileName))) {
                        counterTemp++;
                        remoteFileNames.add(remoteFile.getFilename());
                    }
                }

                for (String fileNameTemp : remoteFileNames) {
                    try {
                        BufferedInputStream bis = new BufferedInputStream(sftp.get(fileNameTemp));
                        System.out.println(fileNameTemp);
                        temp = IOUtils.toByteArray(bis);
                        base64String = Base64.getEncoder().encodeToString(temp);
                        result.put(filePath + "/" + fileNameTemp, base64String);
                    } catch (SftpException e) {
                        System.out.println("Could not download the file " + e.getLocalizedMessage());
                    }
                }
            }
        } catch (SftpException e) {

            e.printStackTrace();
        }

        return result;


    }

*//*
    private ByteArrayInputStream loadFileAsResource(String fileName) throws FileNotFoundException {
        System.out.println("#########");
        System.out.println("#########");
        System.out.println("----------");
        System.out.println("----------");
        try {
            System.out.println("fileName ==  "+fileName);
            System.out.println("----------");
            System.out.println("----------");

            Path filePath = Paths.get(fileName);
            System.out.println("filePath uri== "+filePath.toUri());
            System.out.println("filePath fileName== "+filePath.getFileName());
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return new ByteArrayInputStream(resource.getInputStream().);
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            throw new FileNotFoundException("File not found " + fileName);
        }
    }*/
}

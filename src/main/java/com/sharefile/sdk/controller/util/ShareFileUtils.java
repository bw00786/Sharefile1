package com.sharefile.sdk.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.*;
import java.util.List;

import static org.slf4j.LoggerFactory.*;

public class ShareFileUtils {

    private  static final Logger log = getLogger(ShareFileUtils.class);
    @Autowired
    private  ShareFileV3SDK shareFileV3SDK;
    @Autowired
    private  CreateZip2 createZip2;

    @Autowired
    private ZipDirectory zipDirectory;

    private static volatile OAuth2Token shareFileToken2;

    private ShareFileUtils shareFileUtils;
    public static String itemID = null;
    // get info from properties file


    /**
     * fileWatcher - this method watches for a zip file which will be dumped in the Drop folder.
     *
     * @param myDir
     * @param hostname
     * @param clientId
     * @param clientSecret
     * @param username
     * @param password
     * @return
     */
    public  String fileWatcher(Path myDir,
                               String hostname,
                               String clientId,
                               String clientSecret,
                               String username,
                               String password) {
        String zipFile = null;
        OAuth2Token token = null;
        try {
            log.info("Waiting for a zipfile.....");

            WatchService watcher = myDir.getFileSystem().newWatchService();
            myDir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey watckKey = watcher.take();
            List<WatchEvent<?>> events = watckKey.pollEvents();
            for (WatchEvent event : events) {
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    log.info("Created: {} ", event.context().toString());
                    zipFile = event.context().toString();
                    //now that we have the zip file, attempt to Reports it

                    token = ShareFileV3SDK.authenticate(hostname, clientId, clientSecret, username, password);
                    String accessToken = token.getAccessToken();

                    log.info("access token is {}", accessToken);
                    if (token != null) {
                        log.info("Successfully connected to sharefile");
                    } else {
                        log.error("invalid token generated for ShareFile Administration");
                        throw new ShareFileException("invalid login to sharefile admininistrative account for add");
                    }
                    //** comment this out for now as we are putting deletion of directories on hold
                    if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        log.info("Delete: " + event.context().toString());
                    }
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        log.info("Modify: " + event.context().toString());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error: {}", e.toString());
        }
        return zipFile;
    }

    /**
     * verifyUpload - this method verifies upload of files from /data/sharefile/extract/ to sharefile
     *
     * @param hostname       - token to access sharefile
     * @param directoryToZip - this is the /data/sharefile/extract folder
     */

    public  Boolean verifyUpload(File directoryToZip,
                                 String hostname,
                                 String clientID,
                                 String clientSecret,
                                 String userName,
                                 String passWord,
                                 String exceptionDirectory,
                                 String shareFileRootDirectory,
                                 String desFolder,
                                 String desFolderSharefile,
                                 String exceptionFile,
                                 int threadCount
    ) throws IOException, ParseException {


        try {
            List<String> listofFilesToReUploadToShareFile = new ArrayList<String>();

            OAuth2Token token = ShareFileV3SDK.authenticate(hostname, clientID, clientSecret, userName, passWord);
            String accessToken = token.getAccessToken();
            log.info("access token = " + accessToken);
            log.info("Successfully re-connected to sharefile");
            // now take exception file an(d
            try {

                File newDir = new File(exceptionDirectory);
                if (!newDir.exists()) {
                    if (!newDir.mkdirs()) {

                        throw new ShareFileException("Problem creating Folder " + newDir);

                    }
                }
                log.info("Getting file listings...");
                List<File> fileList = new ArrayList<File>();
                createZip2.getAllFiles(directoryToZip, fileList);
                //If this pathname does not denote a directory, then listFiles() returns null.
                for (File file : fileList) {
                    if (file.isFile()) {
                        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                                file.getCanonicalPath().length());

                        StringBuilder thisFile = new StringBuilder();
                        thisFile.append(shareFileRootDirectory);
                        thisFile.append(File.separator);
                        thisFile.append(zipFilePath);
                        log.info("Verifying " + thisFile.toString() + "...");
                        String changeThisFile = thisFile.toString();
                        changeThisFile = changeThisFile.replace(" ", "%20");
                        String folderGUID = shareFileV3SDK.getDirByPath(token, changeThisFile);
                        if (folderGUID == null || folderGUID.contains("404")) {
                            log.info("Missing file " + thisFile.toString() + " from ShareFile upload");
                            log.info("Adding " + zipFilePath + " to missing files list");
                            listofFilesToReUploadToShareFile.add(zipFilePath);
                        } else if (folderGUID.contains("401") || folderGUID.contains("402") || folderGUID.contains("500")) {
                            token = shareFileV3SDK.authenticate(hostname, clientID, clientSecret, userName, passWord);
                            String accessToken2 = token.getAccessToken();
                            log.info("access token = " + accessToken2);
                            log.info("Successfully re-connected to sharefile");
                            folderGUID = shareFileV3SDK.getDirByPath(token, changeThisFile);
                            if (folderGUID == null || folderGUID.contains("404")) {
                                log.info("Missing file " + thisFile.toString() + " from ShareFile upload");
                                log.info("Adding " + zipFilePath + " to missing files list");
                                listofFilesToReUploadToShareFile.add(zipFilePath);
                            } else {
                                log.info("File " + thisFile.toString() + "uploaded sucessfully ");
                                log.info("File {} uploaded successfully and verified", thisFile.toString());
                            }
                        }
                    }
                }
            } catch (IOException | ShareFileException e) {
                log.error(e.getMessage());
                throw new ShareFileException(e.getMessage());

            }
            // now take exception file and
            if (listofFilesToReUploadToShareFile.size() > 0) {
                File newDir = new File(exceptionDirectory);
                if (!newDir.exists()) {
                    if (!newDir.mkdirs()) {
                        throw new ShareFileException("Unable to create folder " + newDir);
                    }
                }
                log.info("Creating exception list");
                zipDirectory.writeZipFile(new File(desFolder), listofFilesToReUploadToShareFile, exceptionFile, desFolderSharefile);
                shareFileUtils.uploadZipFilestoShareFile(new File(exceptionDirectory), hostname, clientID, clientSecret, userName, passWord, shareFileRootDirectory,threadCount);
            } else {
                log.info("All Files were successfully uploaded and verified");
            }

        } catch (Exception e) {
            log.error(e.getMessage());
            return false;

        }
        return true;
    }



    /**
     * copyFile - copy file from one location to another
     *
     * @param oldLocation - source file
     * @param newLocation - destination
     * @throws IOException
     */

    private  void copyFile(File oldLocation, File newLocation) throws IOException, ShareFileException {
        if (oldLocation.exists()) {
            StringBuilder tempFile = new StringBuilder();
            tempFile.append(newLocation.getName());
            tempFile.append(File.separator);
            tempFile.append(oldLocation.getName());
            BufferedInputStream reader = new BufferedInputStream(new FileInputStream(oldLocation));
            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(newLocation, false));

            try {
                byte[] buff = new byte[8192];
                int numChars;
                while ((numChars = reader.read(buff, 0, buff.length)) != -1) {
                    writer.write(buff, 0, numChars);
                }
            } catch (IOException ex) {
                log.error("IOException when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
            } finally {
                try {
                    writer.close();
                    reader.close();
                } catch (IOException ex) {
                    log.error("Error closing files when transferring to disk" + oldLocation.getPath() + " to " + newLocation.getPath() + "  " + ex.getMessage());

                }
            }
        } else {
            throw new IOException("Old location does not exist when transferring " + oldLocation.getPath() + " to " + newLocation.getPath());
        }
    }
    public  void copyFile(String sourcePath, String targetDir, String fileName, String reportType)throws ShareFileException {
        File source = new File(sourcePath);

        // nw rename zip file
        StringBuilder newFile = new StringBuilder();
        newFile.append(fileName);
        fileName = newFile.toString();
        StringBuilder newDes = new StringBuilder();
        newDes.append(targetDir);
        if (reportType !=null && reportType.equalsIgnoreCase("PHYSPR")) {
            newDes.append(File.separator);
            newDes.append("Physician Performance Reports");
            File physicianReportsDir = new File(newDes.toString());
            if (!physicianReportsDir.exists()) {
                if (!physicianReportsDir.mkdirs()) {
                    log.error("Unable to create directory {}",newDes);
                    throw new ShareFileException("unable to create directory "+newDes);
                }
            }

        }
        if (reportType !=null && reportType.equalsIgnoreCase("PHYSSR")) {
            newDes.append(File.separator);
            newDes.append("Physician Spending Reports");
            File physicianReportsDir = new File(newDes.toString());
            if (!physicianReportsDir.exists()) {
                if (!physicianReportsDir.mkdirs()) {
                    log.error("Unable to create directory {}",newDes);
                    throw new ShareFileException("unable to create directory "+newDes);
                }
            }

        }
        newDes.append(File.separator);
        newDes.append(fileName);
        String targetPath = newDes.toString();
        File target = new File(targetPath);
        try {

            copyFile(source, target);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * method - cleanUpFolders - this method cleans up all sharefile related folders after all processing of files and
     * directories is complete
     * @param zipFolder  - this folder holds all the files needed
     * @param uploadFolder
     * @param shareFileUpload
     * @param exceptionDir
     * @param zipFile2
     * @param shareFileArchive - this
     * @param folder
     * @return
     * @throws IOException
     * @throws ShareFileException
     */
    public String cleanUpFolders(File zipFolder, File uploadFolder, File shareFileUpload, File exceptionDir, String zipFile2,
                                 String shareFileArchive,
                                 String folder) throws IOException, ShareFileException {


        ArrayList<File> listOfFoldersToDelete = new ArrayList<File>();
        listOfFoldersToDelete.add(zipFolder);
        listOfFoldersToDelete.add(uploadFolder);
        listOfFoldersToDelete.add(shareFileUpload);
        listOfFoldersToDelete.add(exceptionDir);

        try {
            for (File files: listOfFoldersToDelete) {

                if (files.exists()) {
                    FileDeleteStrategy.FORCE.delete(files);
                }
            }
            Date myDate = new Date();
            String archiveFileDate = new SimpleDateFormat("MM-dd-yyyy").format(myDate);
            // now rename zip file
            StringBuilder toZipNew = new StringBuilder();
            toZipNew.append(shareFileArchive);
            toZipNew.append(File.separator);
            toZipNew.append(zipFile2);
            toZipNew.append(".");
            toZipNew.append(archiveFileDate);


            // now buid structure for new file

            StringBuilder toZipOld= new StringBuilder();
            toZipOld.append(folder);
            toZipOld.append(File.separator);
            toZipOld.append(zipFile2);
            log.info("old file is {}",toZipOld.toString());
            log.info("new zip file is {}",toZipNew.toString());

            // first create an archive folder
            File archiveFolder = new File(shareFileArchive);
            if(!archiveFolder.exists()) {
                if (!archiveFolder.mkdirs()) {
                    log.error("Unable to create archive folder {} ", shareFileArchive);
                    throw new ShareFileException("Exception. Unable to create directory " + archiveFolder.toString());

                }
            }
            // now move the old analytics zip file to the new archive folder
            Files.move(Paths.get(toZipOld.toString()), Paths.get(toZipNew.toString()));

        } catch (IOException | ShareFileException e) {
            return (e.getMessage());
        }
        return "OK";
    }
    /**
     * deleteShareFileFolders - deletes folders from shareFile - this method is not being used in this release, bu perhaps in future releases
     * @param token
     */

    /**
     * UploadZipFilestoShareFile - this method uploads
     *
     *
     *
     * @param zipFilesDir - holds the directory where all the zip files will be kept
     * @param hostname - name of server host
     * @param clientId - client id needed to form an access token into sahrefile
     * @param clientSecret - client secret needed for the deployment
     * @param username - sharefile cloud instance admin user
     * @param password - sharefile cloud instance admin user password
     * @throws IOException
     * @throws ParseException
     */
    public  boolean uploadZipFilestoShareFile(File zipFilesDir, String hostname, String clientId,
                                              String clientSecret, String username,
                                              String password,
                                              String shareFileRootDir,
                                              int threadCount) throws IOException, ParseException, ShareFileException {

        OAuth2Token shareFileToken = ShareFileV3SDK.authenticate(hostname, clientId, clientSecret, username, password);
        String accessToken = shareFileToken.getAccessToken();
        log.info("access token = " + accessToken);
        log.info("Successfully re-connected to sharefile");

        String folderGUID = shareFileV3SDK.getDirByPath(shareFileToken, shareFileRootDir);
        if (folderGUID.contains("401") || folderGUID==null) {
            log.error("Failire to obtain folder GUID value for uploading files");
            throw new ShareFileException("Failed to obtain folder GUID");
        }
        File[] files2 = zipFilesDir.listFiles();
        final CountDownLatch latch = new CountDownLatch(files2.length);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        //If this pathname does not denote a directory, then listFiles() returns null.

        if (files2 != null) {
            for (File file : files2)
                if (file.isFile()) {

                    log.debug("Uploading all zip files {}", file.getAbsolutePath());
                    try {
                        String fileToUpload = file.getAbsolutePath();
                        String errorCode = shareFileV3SDK.uploadFile(shareFileToken, folderGUID, fileToUpload, threadCount);
                        /** now check if upload is successful, if not, re-authenticate and get
                         * and get a new token
                         *
                         **/
                        if (errorCode.contains(String.valueOf(HttpURLConnection.HTTP_BAD_REQUEST)) || errorCode.contains(String.valueOf(HttpURLConnection.HTTP_UNAVAILABLE)) || errorCode.contains(String.valueOf(HttpURLConnection.HTTP_UNAUTHORIZED))) {
                            shareFileToken2 = ShareFileV3SDK.authenticate(hostname, clientId, clientSecret, username, password);
                            long shaFileTokenExpireStartTime = shareFileToken2.getExpiresIn();

                            if (shareFileToken == null) {
                                throw new ShareFileException("Exception. Unable to authenticate to Sharefile");
                            } else {
                                accessToken = shareFileToken.getAccessToken();
                                log.info("access token = " + accessToken);
                                log.info("Successfully re-connected to sharefile");
                                pool.execute(() -> {
                                    try {

                                        String returnMessage = shareFileV3SDK.uploadFile(shareFileToken2, folderGUID, fileToUpload, threadCount);
                                        long shareFileTokenExpireEndTime = System.currentTimeMillis();
                                        float totalTokenTime=(shareFileTokenExpireEndTime - shaFileTokenExpireStartTime)/1000L;
                                        if ((totalTokenTime > (shareFileToken2.getExpiresIn() - 10)/1000L ) || returnMessage.contains("401")) {
                                            log.error("Error in uploading zip files to Sharefile");
                                            log.error("Re-Authenticating...");
                                            shareFileToken2 = ShareFileV3SDK.authenticate(hostname, clientId, clientSecret, username, password);
                                            shareFileV3SDK.uploadFile(shareFileToken2, folderGUID, fileToUpload, threadCount);

                                        }
                                    } catch (InterruptedIOException | ParseException e) {
                                        try {
                                            throw new ShareFileException(e.getMessage());
                                        } catch (ShareFileException e1) {
                                            log.error(e1.getMessage());
                                        }
                                        finally {
                                            latch.countDown();
                                        }


                                    } catch (ShareFileException e) {
                                        e.printStackTrace();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                            }
                        }

                    } catch (IOException e) {
                        log.error(e.getMessage());
                        throw new ShareFileException(e.getMessage());
                    }
                } //if file
        }
        return true;
    }
}
}

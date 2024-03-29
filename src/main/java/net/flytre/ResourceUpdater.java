package net.flytre;

import net.flytre.config.ConfigInstance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceUpdater {


    public static Logger LOGGER = Logger.getLogger("Resource Updater");


    public static void update() throws IOException {

        deleteDir(new File("resources"));
        new File("resources/assets").mkdirs();
        new File("resources/data").mkdirs();

        String folder = ConfigInstance.CONFIG.getMinecraftDirectory() + "/versions/" + ConfigInstance.CONFIG.getVersion() + "/" + ConfigInstance.CONFIG.getVersion();
        String jar = folder + ".jar";
        String zip = folder + ".zip";

        LOGGER.log(Level.INFO, "Deleting old files...");
        deleteDir(new File(zip));
        deleteDir(new File(folder));


        LOGGER.log(Level.INFO, "Copying Jar contents to Zip file...");

        copyDirectory(new File(jar), new File(zip));

        LOGGER.log(Level.INFO, "Unzipping...");
        unzip(zip, folder);


        LOGGER.log(Level.INFO, "Deleting Zip File...");
        deleteDir(new File(zip));


        LOGGER.log(Level.INFO, "Updating /resources...");

        String assets = folder + "/assets";
        String data = folder + "/data";


        copyDirectory(new File(assets), new File("resources/assets"));
        copyDirectory(new File(data), new File("resources/data"));


        LOGGER.log(Level.INFO, "Cleaning up...");
        deleteDir(new File(folder));


        LOGGER.log(Level.INFO, "Finished task.");


    }

    private static void copyDirectory(File sourceDir, File targetDir) throws IOException {
        if (!sourceDir.exists())
            return;
        if (sourceDir.isDirectory()) {
            copyDirectoryRecursively(sourceDir, targetDir);
        } else {
            Files.copy(sourceDir.toPath(), targetDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void copyDirectoryRecursively(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdir();
        }
        for (String child : source.list()) {
            copyDirectory(new File(source, child), new File(target, child));
        }
    }

    private static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        // create output directory if it doesn't exist
        if (!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                if (newFile.getAbsolutePath().contains(".class")) {
                    zis.closeEntry();
                    ze = zis.getNextEntry();
                    continue;
                }
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void deleteDir(File file) {

        if (!file.exists())
            return;

        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }
}

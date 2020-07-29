package net.flytre;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateResources {

    
    public static void update() {

        String jar = Constants.DIRECTORY + "versions/" + Constants.VERSION + "/" + Constants.VERSION + ".jar";
        String zip = Constants.DIRECTORY + "versions/" + Constants.VERSION + "/" + Constants.VERSION + ".zip";
        String folder = Constants.DIRECTORY + "versions/" + Constants.VERSION + "/" + Constants.VERSION;

        System.out.println("Deleting blocking files...");
        deleteDir(new File(zip));
        deleteDir(new File(folder));

        System.out.println("Copying Jar contents to Zip file...");
        try {
            copyDirectory(new File(jar),new File(zip));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Unzipping...");
        unzip(zip,folder);


        System.out.println("Deleting Zip File...");
        deleteDir(new File(zip));


        System.out.println("Updating /resources...");

        String assets = folder + "/assets";
        String data = folder + "/data";


        try {
            copyDirectory(new File(assets), new File("resources/assets"));
            copyDirectory(new File(data), new File("resources/data"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


        System.out.println("Deleting folder...");
        deleteDir(new File(folder));


        System.out.println("Finalizing...");

    }



    private static void copyDirectory(File sourceDir, File targetDir) throws IOException {
        if (sourceDir.isDirectory()) {
            copyDirectoryRecursively(sourceDir, targetDir);
        } else {
            Files.copy(sourceDir.toPath(), targetDir.toPath());
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
        if(!dir.exists()) dir.mkdirs();
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                if(newFile.getAbsolutePath().contains(".class")) {
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
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDir(f);
            }
        }
        file.delete();
    }

}

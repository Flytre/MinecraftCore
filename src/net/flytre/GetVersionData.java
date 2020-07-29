package net.flytre;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;

public class GetVersionData {

    private static void getData() {
        FileHandler.deleteDirectory("resources");
        new File("resources/assets").mkdirs();
        new File("resources/data").mkdirs();

        UpdateResources.update();


        System.out.println("Downloading server.jar...");
        downloadJAR(getServerURL());

        System.out.println("Running debug scripts...");
        runDebug();

        System.out.println("Parsing Debug Output...");
        getRegistries(new String[]{"item","block","enchantment","entity_type","mob_effect","biome","particle_type"});
        System.out.println("Finishing...");
        deleteDebug();
    }

    public static void main(String[] args) {
        getData();
    }

    private static String getServerURL() {
        String json = Constants.DIRECTORY + "versions/" + Constants.VERSION + "/" + Constants.VERSION + ".json";
        String content = FileHandler.readFile(json);
        JSONObject meta = (JSONObject) JSONValue.parse(content);
        JSONObject downloads = (JSONObject) meta.get("downloads");
        JSONObject server = (JSONObject) downloads.get("server");
        String url = (String) server.get("url");
        return url;
    }

    private static void downloadJAR(String url) {
        try {
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream("resources/server.jar");
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runDebug() {

        try {
            String command = "java -cp server.jar net.minecraft.data.Main --server --reports\n";
            Process proc = Runtime.getRuntime().exec(command, null, new File("resources"));
            proc.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void getRegistries(String[] keys) {
        String registries = "resources/generated/reports/registries.json";
        String output = "resources/lists/";
        String content = FileHandler.readFile(registries);

        JSONObject meta = (JSONObject) JSONValue.parse(content);

        for(String s : keys) {
            JSONObject registry  = (JSONObject) meta.get("minecraft:" + s);
            JSONObject entries = (JSONObject) registry.get("entries");
            FileHandler.setOutput(output + s + ".txt");
            ArrayList<String> unsorted = new ArrayList<>();
            for(Object entry : entries.keySet()) {
               unsorted.add(entry.toString().replace("minecraft:",""));
            }
            Collections.sort(unsorted);
            for(String entry : unsorted)
                FileHandler.print(entry);
        }
    }

    private static void deleteDebug() {
        FileHandler.deleteDirectory("resources/generated");
        FileHandler.deleteDirectory("resources/logs");
        FileHandler.deleteDirectory("resources/server.jar");

    }
}

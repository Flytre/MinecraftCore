package net.flytre;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.flytre.config.Config;
import net.flytre.config.ConfigInstance;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class VersionAnalyzer {

    private static final Gson GSON = new Gson();
    public static Logger LOGGER = Logger.getLogger("Version Analyzer");

    public static void analyze() throws IOException, InterruptedException {
        ResourceUpdater.update();


        LOGGER.log(Level.INFO, "Downloading server.jar...");
        downloadJAR(getServerURL());

        LOGGER.log(Level.INFO, "Running debug scripts...");
        runDebug();

        LOGGER.log(Level.INFO, "Parsing Debug Output...");

        if (ConfigInstance.CONFIG.getVersionType() == Config.VersionType.PRE_1_14)
            getRegistries();
        else
            getRegistries2(new String[]{"item", "block", "enchantment", "entity_type", "mob_effect", "particle_type"});

        LOGGER.log(Level.INFO, "Finishing...");
        deleteDebug();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        analyze();
    }

    private static String getServerURL() throws FileNotFoundException {
        String json = ConfigInstance.CONFIG.getMinecraftDirectory() + "/versions/" + ConfigInstance.CONFIG.getVersion() + "/" + ConfigInstance.CONFIG.getVersion() + ".json";

        JsonObject object = GSON.fromJson(new FileReader(json), JsonObject.class);
        return object
                .getAsJsonObject("downloads")
                .getAsJsonObject("server")
                .get("url")
                .getAsString();
    }

    private static void downloadJAR(String url) throws IOException {
        URL website = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream("resources/server.jar");
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

    private static void runDebug() throws InterruptedException, IOException {

        String command = "java -cp server.jar net.minecraft.data.Main --server --reports\n";

        if (ConfigInstance.CONFIG.getVersionType() == Config.VersionType.RECENT)
            command = "java -DbundlerMainClass=net.minecraft.data.Main -jar server.jar --reports\n";


        Process proc = Runtime.getRuntime().exec(command, null, new File("resources"));
        proc.waitFor();
    }

    private static void getRegistries() throws FileNotFoundException {
        String reports = "resources/generated/reports/";
        String output = "resources/lists/";
        List<String> targets = List.of("blocks", "items");

        for (String target : targets) {
            JsonObject object = GSON.fromJson(new FileReader(reports + target + ".json"), JsonObject.class);
            List<String> entries = object.keySet().stream().map(i -> i.replace("minecraft:", "")).sorted().collect(Collectors.toList());

            for (String entry : entries)
                print(output + target + ".txt", entry);
        }

    }


    private static void getRegistries2(String[] keys) throws FileNotFoundException {
        String registries = "resources/generated/reports/registries.json";
        String output = "resources/lists/";

        JsonObject object = GSON.fromJson(new FileReader(registries), JsonObject.class);


        for (String s : keys) {


            JsonObject registry = object.getAsJsonObject("minecraft:" + s);
            JsonObject entries = registry.getAsJsonObject("entries");
            List<String> entryList = entries.keySet().stream().map(i -> i.replace("minecraft:", "")).sorted().collect(Collectors.toList());

            for (String entry : entryList)
                print(output + s + ".txt", entry);
        }
    }

    private static void print(String loc, String msg) {
        File out = new File(loc);
        try {
            out.getParentFile().mkdirs();
            out.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter toFile = new PrintWriter(new FileWriter(loc, true))) {
            toFile.println(msg);
            toFile.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void deleteDebug() {
        ResourceUpdater.deleteDir(new File("resources/generated"));
        ResourceUpdater.deleteDir(new File("resources/logs"));
        ResourceUpdater.deleteDir(new File("resources/libraries"));
        ResourceUpdater.deleteDir(new File("resources/versions"));
        ResourceUpdater.deleteDir(new File("resources/server.jar"));

    }
}

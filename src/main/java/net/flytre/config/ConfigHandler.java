package net.flytre.config;

import com.google.gson.*;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public final class ConfigHandler<T> {
    private final Gson gson;
    private final T assumed;
    private final String name;
    private T config;

    public ConfigHandler(T assumed, String name) {
        this(assumed, name, new GsonBuilder().setPrettyPrinting().create());
    }

    public ConfigHandler(T assumed, String name, Gson gson) {
        this.assumed = assumed;
        this.name = name;
        this.gson = gson;
    }


    public String getName() {
        return name;
    }

    /**
     * Save a config to the config file location
     */
    public void save(T config) {
        Path path = Paths.get(name + ".json");
        Writer writer;
        try {
            writer = new FileWriter(path.toFile());
            gson.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load the config, or if none is found save the default to a file and load that
     * Return false if an error was found, or true if none was
     */
    public boolean handle() {
        boolean error = false;
        Path location = Paths.get(name + ".json");
        File file = location.toFile();

        if (file.length() == 0) {
            save(assumed);
            this.config = assumed;
        } else {

            try (Reader reader = new FileReader(file)) {
                try {
                    JsonObject json = gson.fromJson(reader, JsonObject.class);
                    this.config = gson.fromJson(json, (Type) assumed.getClass());
                } catch (JsonParseException | NumberFormatException e) {
                    this.config = assumed;
                    error = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                error = true;
            }
        }
        return !error;
    }

    public T getConfig() {
        return config;
    }
}

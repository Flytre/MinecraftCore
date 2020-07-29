package net.flytre;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Constants {

    public static final String DIRECTORY;
    public static final String VERSION;

    static {
        String content = FileHandler.readFile("minecraft_core_config.json");
        JSONObject meta = (JSONObject) JSONValue.parse(content);
        DIRECTORY = (String) meta.get("directory");
        VERSION = (String) meta.get("version");

    }
}

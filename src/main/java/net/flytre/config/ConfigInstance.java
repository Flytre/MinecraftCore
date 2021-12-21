package net.flytre.config;

public class ConfigInstance {

    public static final Config CONFIG;

    static {
        ConfigHandler<Config> handler = new ConfigHandler<>(new Config(), "minecraft_core_config");
        handler.handle();
        CONFIG = handler.getConfig();
        System.out.println(CONFIG);
    }


    public static void main(String[] args) {

    }
}

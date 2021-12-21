package net.flytre.config;

import com.google.gson.annotations.SerializedName;

public class Config {

    private String version = "1.18.1";
    private @SerializedName("version_type")
    VersionType versionType = VersionType.RECENT;
    private @SerializedName("minecraft_direction")
    String minecraftDirectory = "/Users/user/Library/Application Support/minecraft";


    public String getVersion() {
        return version;
    }

    public VersionType getVersionType() {
        return versionType;
    }

    public String getMinecraftDirectory() {
        return minecraftDirectory;
    }


    public enum VersionType {
        @SerializedName("pre-1.14") PRE_1_14,
        @SerializedName("pre-1.18") PRE_1_18,
        @SerializedName("recent") RECENT;
    }

    @Override
    public String toString() {
        return "Config{" +
                "version='" + version + '\'' +
                ", versionType=" + versionType +
                ", minecraftDirectory='" + minecraftDirectory + '\'' +
                '}';
    }
}

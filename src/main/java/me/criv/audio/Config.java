package me.criv.audio;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;

public class Config {
    static Main instance = Main.instance;
    static FileConfiguration config = Main.instance.getConfig();
    public static void addRegionObject(String region, String sound, Integer max) {
        ConfigurationSection section = config.getConfigurationSection("region-sound-mappings");
        section.set(region + ".sound", sound);
        section.set(region + ".max", max);
        instance.saveConfig();
    }
    public static void removeRegionObject(String region) {
        config.getConfigurationSection("region-sound-mappings").set(region, null);
        instance.saveConfig();
    }
    public static void createDefaults() {
        if(!config.isConfigurationSection("region-sound-mappings")) {
            config.createSection("region-sound-mappings");
        }
        if(config.get("sync-interval-seconds") == null) {
            config.set("sync-interval-seconds", 5);
        }
        if(config.get("fade-time-seconds") == null) {
            config.set("fade-time-seconds", 2.5);
        }
        if(config.get("fade-height-blocks") == null) {
            config.set("fade-height-blocks", 18);
        }
        if(config.get("pitch-double") == null) {
            config.set("pitch-double", 1.0);
        }
        instance.saveConfig();
    }
    public static ArrayList<String> listRegionObjects() {
        ArrayList<String> regions = new ArrayList<>();
        for(String section : config.getConfigurationSection("region-sound-mappings").getKeys(false)) {
            regions.add("\n" + section);
            for (String value : config.getConfigurationSection("region-sound-mappings").getConfigurationSection(section).getKeys(true)) {
                regions.add("\n     " + value + ": " + config.getConfigurationSection("region-sound-mappings").getConfigurationSection(section).get(value));
            }
        }
        return regions;
    }
    public static int getMax(String region) {
        return config.getInt("region-sound-mappings." + region+".max");
    }
    public static String getSound(String region) {
        return config.getString("region-sound-mappings." + region +".sound");
    }
    public static double getFadeTime() {
        return config.getDouble("fade-time-seconds")*20;
    }
    public static double getSyncInterval() {
        return config.getDouble("sync-interval-seconds");
    }
    public static double getFadeHeight() {
        return config.getDouble("fade-height-blocks");
    }
    public static double getPitch() {
        return config.getDouble("pitch-double");
    }
    public static void setFadeTime(double seconds) {
        config.set("fade-time-seconds", seconds);
        instance.saveConfig();
    }
    public static void setSyncInterval(double seconds) {
        config.set("sync-interval-seconds", seconds);
        instance.saveConfig();
    }
    public static void setFadeHeight(double blocks) {
        config.set("fade-height-blocks", blocks);
        instance.saveConfig();
    }
    public static void setPitch(double pitch) {
        config.set("pitch-double", pitch);
        instance.saveConfig();
    }
}

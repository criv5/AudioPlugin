package me.criv.audio;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
        if(config.get("transition-time-seconds") == null) {
            config.set("transition-time-seconds", 4);
        }
        if(config.get("transition-maximum-height-blocks") == null) {
            config.set("transition-maximum-height-blocks", 32);
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
    public static double getTransitionTime() {
        return config.getDouble("transition-time-seconds")*20;
    }
    public static int getSyncInterval() {
        return config.getInt("sync-interval-seconds");
    }
    public static double getTransitionHeight() {
        return config.getDouble("transition-maximum-height-blocks");
    }
    public static void setTransitionTime(int seconds) {
        config.set("transition-time-seconds", seconds);
        instance.saveConfig();
    }
    public static void setSyncInterval(int seconds) {
        config.set("sync-interval-seconds", seconds);
        instance.saveConfig();
    }
    public static void setTransitionHeight(double blocks) {
        config.set("transition-maximum-height-blocks", blocks);
        instance.saveConfig();
    }
}

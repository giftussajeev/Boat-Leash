package com.boatleash.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("boatleash.json");

    public static boolean enabled = true;
    public static double maxDistance = 30.0;
    public static double pullForce = 0.4;
    public static double breakDistance = 45.0;

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Data data = GSON.fromJson(reader, Data.class);

            if (data != null) {
                enabled = data.enabled;
                maxDistance = data.maxDistance;
                pullForce = data.pullForce;
                breakDistance = data.breakDistance;
            }

        } catch (IOException | JsonSyntaxException e) {
            System.out.println("[BoatLeash] Failed to load config, using defaults");
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                Data data = new Data();
                data.enabled = enabled;
                data.maxDistance = maxDistance;
                data.pullForce = pullForce;
                data.breakDistance = breakDistance;

                GSON.toJson(data, writer);
            }

        } catch (IOException e) {
            System.out.println("[BoatLeash] Failed to save config");
            e.printStackTrace();
        }
    }

    private static class Data {
        boolean enabled = true;
        double maxDistance = 30.0;
        double pullForce = 0.4;
        double breakDistance = 45.0;
    }
}
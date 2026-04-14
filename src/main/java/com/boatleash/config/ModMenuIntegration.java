package com.boatleash.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createScreen;
    }

    private Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(new LiteralText("Boat Leash Config"))
                .setSavingRunnable(ModConfig::save);

        ConfigCategory general = builder.getOrCreateCategory(new LiteralText("General"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startBooleanToggle(new LiteralText("Enabled"), ModConfig.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> ModConfig.enabled = val)
                .build());

        general.addEntry(entryBuilder.startDoubleField(new LiteralText("Max Rope Length"), ModConfig.maxDistance)
                .setDefaultValue(30.0)
                .setMin(1.0)
                .setSaveConsumer(val -> ModConfig.maxDistance = val)
                .build());

        general.addEntry(entryBuilder.startDoubleField(new LiteralText("Pull Strength"), ModConfig.pullForce)
                .setDefaultValue(0.4)
                .setMin(0.01)
                .setMax(2.0)
                .setSaveConsumer(val -> ModConfig.pullForce = val)
                .build());

        general.addEntry(entryBuilder.startDoubleField(new LiteralText("Break Distance"), ModConfig.breakDistance)
                .setDefaultValue(45.0)
                .setMin(2.0)
                .setSaveConsumer(val -> ModConfig.breakDistance = val)
                .build());

        return builder.build();
    }
}
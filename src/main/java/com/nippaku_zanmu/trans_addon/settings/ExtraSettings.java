package com.nippaku_zanmu.trans_addon.settings;


import meteordevelopment.meteorclient.gui.DefaultSettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.screens.settings.EntityTypeListSettingScreen;
import meteordevelopment.meteorclient.gui.themes.meteor.widgets.WMeteorLabel;
import meteordevelopment.meteorclient.gui.utils.SettingsWidgetFactory;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.Settings;

import java.util.Collection;
import java.util.Map;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ExtraSettings {
    private final Map<Class<?>, SettingsWidgetFactory.Factory> factories;

    private final GuiTheme theme;

    public ExtraSettings(Map<Class<?>, SettingsWidgetFactory.Factory> factories, GuiTheme theme) {
        this.factories = factories;
        this.theme = theme;
    }

    public void addSettings() {
        //factories.put(StringMapSetting.class, (table, setting) -> stringMapW(table, (StringMapSetting) setting));

        factories.put(StringSelectSetting.class, (table, setting) -> stringSelectW(table, (StringSelectSetting) setting));    }

    private void stringSelectW(WTable table, StringSelectSetting setting) {
        selectW(table, setting, () -> mc.gui.setScreen(new StringSelectScreen(theme, setting)));

    }private void selectW(WContainer c, Setting<?> setting, Runnable action) {
        boolean addCount = WSelectedCountLabel.getSize(setting) != -1;

        WContainer c2 = c;
        if (addCount) {
            c2 = c.add(theme.horizontalList()).expandCellX().widget();
            ((WHorizontalList) c2).spacing *= 2;
        }

        WButton button = c2.add(theme.button("Select")).expandCellX().widget();
        button.action = action;

        if (addCount) c2.add(new WSelectedCountLabel(setting).color(theme.textSecondaryColor()));

        reset(c, setting, null);
    }

    private void reset(WContainer c, Setting<?> setting, Runnable action) {
        WButton reset = c.add(theme.button(GuiRenderer.RESET)).widget();
        reset.action = () -> {
            setting.reset();
            if (action != null) action.run();
        };
    }

//    private void stringMapW(WTable table, StringMapSetting setting) {
//        WTable wtable = table.add(theme.table()).expandX().widget();
//        StringMapSetting.fillTable(theme, wtable, setting);
//    }





    private static class WSelectedCountLabel extends WMeteorLabel {
        private final Setting<?> setting;
        private int lastSize = -1;

        public WSelectedCountLabel(Setting<?> setting) {
            super("", false);

            this.setting = setting;
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            int size = getSize(setting);

            if (size != lastSize) {
                if (setting.get()instanceof Settings){
                    set("(" + size + " Group)");
                }else
                    set("(" + size + " selected)");
                lastSize = size;
            }

            super.onRender(renderer, mouseX, mouseY, delta);
        }

        public static int getSize(Setting<?> setting) {
            if (setting.get() instanceof Collection<?> collection) return collection.size();
            if (setting.get() instanceof Map<?, ?> map) return map.size();
            if (setting.get() instanceof Settings) return ((Settings) setting.get()).groups.size();
            return -1;
        }
    }
}

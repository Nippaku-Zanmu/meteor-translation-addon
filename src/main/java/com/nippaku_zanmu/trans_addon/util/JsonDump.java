package com.nippaku_zanmu.trans_addon.util;

import com.nippaku_zanmu.trans_addon.mixin.SettingGroupAccessor;
import com.nippaku_zanmu.trans_addon.modules.Translation;
import com.nippaku_zanmu.trans_addon.util.trans_engine.AbstractTransEngine;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class JsonDump {
    public static JsonDump getINSTANCE() {
        return INSTANCE;
    }

    private static final JsonDump INSTANCE = new JsonDump();

    //    private LinkedHashSet<String> keySet = new LinkedHashSet<>();
    private LinkedHashMap<String, String> entMap = new LinkedHashMap<>();
    private BufferedWriter dumpBW;

    private Translation getTran() {
        return Modules.get().get(Translation.class);
    }


    public void write(AbstractTransEngine engine, AbstractTransEngine engine2) {

        dump2Set(engine, engine2);

        try {
            File path = new File(getTran().sSetDumpPath.get());
            if (!path.exists() & !(path.createNewFile())) {
                ChatUtils.warning("DumpError Can't Create Dump File");
                entMap.clear();
                return;
            }
            dumpBW = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path, false), StandardCharsets.UTF_8));

            for (Map.Entry<String, String> entry : entMap.entrySet()) {
                dumpBW.write("\"" + entry.getKey() + "\"" + ":" + "\"" + TransUtil.formatValue(entry.getValue()) + "\"" + ",");
                dumpBW.newLine();
            }

            dumpBW.flush();
            dumpBW.close();

        } catch (IOException e) {
            ChatUtils.error(e.getMessage());
            entMap.clear();
            return;
        } finally {
            try {
                if (dumpBW != null)
                    dumpBW.close();
            } catch (IOException ignore) {
            }
        }


        entMap.clear();
    }

    private void dump2Set(AbstractTransEngine engine, AbstractTransEngine engine2) {
        boolean dumpText = getTran().bSetDumpText.get();
        for (Module module : Modules.get().getAll()) {
            String addonName = TransUtil.getAddonName(module);
            if (!getTran().translationModules.get().contains(addonName)) continue;
            //插件过滤


            String nameKey = engine.getModuleNameKey(module);
            addEntry(nameKey, dumpText ? engine2.transModuleName(module) : module.name);

            String desKey = engine.getModuleDescriptionKey(module);
            addEntry(desKey, dumpText ? engine2.transModuleDescription(module) : module.description);

            for (SettingGroup group : module.settings.groups) {
                for (Setting<?> setting : ((SettingGroupAccessor) group).getSettings()) {

                    String settingNameKey = engine.getSettingNameKey(module, group, setting);
                    addEntry(settingNameKey, dumpText ? engine2.transSettingName(module, group, setting) : setting.name);

                    String settDescKey = engine.getSettingDesKey(module, group, setting);
                    addEntry(settDescKey, dumpText ? engine2.transSettingDes(module, group, setting) : setting.description);


                }
            }

        }
    }

    private void addEntry(String key, String value) {
        entMap.putIfAbsent(key, value);
    }
}

/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.nippaku_zanmu.trans_addon.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class StringSelectScreen extends WindowScreen {
    private final StringSelectSetting setting;

    private WVerticalList list;
    private final WTextBox filter;

    private String filterText = "";
//
//    private WSection animals;
//    private WTable animalsT;


    private WSection strings;
    private WTable stringsT;

    public StringSelectScreen(GuiTheme theme, StringSelectSetting setting) {
        super(theme, setting.title);
        this.setting = setting;

        // Filter
        filter = super.add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            list.clear();
            initWidgets();
        };

        list = super.add(theme.verticalList()).expandX().widget();

    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return list.add(widget);
    }

    int strsSize;

    @Override
    public void initWidgets() {
//        strsSize = setting.get().size();
        strsSize = 0;
        for (String s:setting.get()){
            if (setting.filter == null || setting.filter.test(s)) strsSize++;
        }

        List<String> stringE = new ArrayList<>();
        WCheckbox stringC = theme.checkbox(strsSize > 0);

        strings = theme.section("Strings", strings != null && strings.isExpanded(), stringC);
        stringC.action = () -> tableChecked(stringE, stringC.checked);

        Cell<WSection> stringsCell = add(strings).expandX();
        stringsT = strings.add(theme.table()).expandX().widget();


        Consumer<String> stringForeach = str -> {
            stringE.add(str);
            addString(stringsT,stringC,str);
        };

        strings.setExpanded(true);


        // Sort all entities
        if (filterText.isEmpty()) {
            setting.validValues.forEach(stringForeach);
        } else {
            record DiffByType<S, I extends Number>(String type, int diff) {}
            List<DiffByType<String, Integer>> strings = new ArrayList<>();
            setting.validValues.forEach(str -> {
                int words = Utils.searchInWords(str, filterText);
                int diff = Utils.searchLevenshteinDefault(str, filterText, false);

                if (words > 0 || diff < str.length() / 2) strings.add(new DiffByType<>(str, -diff));
            });

            strings.sort(Comparator.comparingInt(DiffByType::diff));
            for (var pair : strings) stringForeach.accept(pair.type);
        }

        if (stringsT.cells.isEmpty()) list.cells.remove(stringsCell);
    }

    private void tableChecked(List<String> strings, boolean checked) {
        boolean changed = false;

        for (String string : strings) {
            if (checked) {
                setting.get().add(string);
                changed = true;
            } else {
                if (setting.get().remove(string)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            list.clear();
            initWidgets();
            setting.onChanged();
        }
    }


    private void addString(WTable table, WCheckbox tableCheckbox, String str) {
        table.add(theme.label(str));
        WCheckbox a = table.add(theme.checkbox(setting.get().contains(str))).expandCellX().right().widget();
        a.action=()->{
            if (a.checked) {
                setting.get().add(str);
                if (strsSize == 0) tableCheckbox.checked = true;
                strsSize++;
            }else {
                if (setting.get().remove(str)) {
                    strsSize--;
                    if (strsSize == 0) tableCheckbox.checked = false;
                }
            }
        };
        table.row();
    }

//    private void addEntityType(WTable table, WCheckbox tableCheckbox, EntityType<?> entityType) {
//        table.add(theme.label(Names.get(entityType)));
//
//
//      WCheckbox a = table.add(theme.checkbox(setting.get().contains(entityType))).expandCellX().right().widget();
//        a.action = () -> {
//            if (a.checked) {
//                setting.get().add(entityType);
//                switch (entityType.getSpawnGroup()) {
//                    case CREATURE -> {
//                        if (hasAnimal == 0) tableCheckbox.checked = true;
//                        hasAnimal++;
//                    }
//                    case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> {
//                        if (hasWaterAnimal == 0) tableCheckbox.checked = true;
//                        hasWaterAnimal++;
//                    }
//                    case MONSTER -> {
//                        if (hasMonster == 0) tableCheckbox.checked = true;
//                        hasMonster++;
//                    }
//                    case AMBIENT -> {
//                        if (hasAmbient == 0) tableCheckbox.checked = true;
//                        hasAmbient++;
//                    }
//                    case MISC -> {
//                        if (hasMisc == 0) tableCheckbox.checked = true;
//                        hasMisc++;
//                    }
//                }
//            } else {
//                if (setting.get().remove(entityType)) {
//                    switch (entityType.getSpawnGroup()) {
//                        case CREATURE -> {
//                            hasAnimal--;
//                            if (hasAnimal == 0) tableCheckbox.checked = false;
//                        }
//                        case WATER_AMBIENT, WATER_CREATURE, UNDERGROUND_WATER_CREATURE, AXOLOTLS -> {
//                            hasWaterAnimal--;
//                            if (hasWaterAnimal == 0) tableCheckbox.checked = false;
//                        }
//                        case MONSTER -> {
//                            hasMonster--;
//                            if (hasMonster == 0) tableCheckbox.checked = false;
//                        }
//                        case AMBIENT -> {
//                            hasAmbient--;
//                            if (hasAmbient == 0) tableCheckbox.checked = false;
//                        }
//                        case MISC -> {
//                            hasMisc--;
//                            if (hasMisc == 0) tableCheckbox.checked = false;
//                        }
//                    }
//                }
//            }
//
//            setting.onChanged();
//        };
//
//        table.row();
//    }
}

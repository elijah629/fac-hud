package com.elijah629.fachud;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "fac-hud")
public class FacHudConfig implements ConfigData {
    public enum HudLocation {
        Left,
        Right
    }

    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    HudLocation HUDLocation = HudLocation.Left;

    boolean ShowAngles = false;
}
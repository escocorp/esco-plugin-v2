package plugin.utils;

import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.gen.Groups;
import plugin.Bundle;
import plugin.ai.DumbAI;

import static plugin.PVars.gamemode;

public class Patches {
    public static void load() {
        if(gamemode.optimized) {
            patchUnits();
            Timer.schedule(()->{
                despawnUnits();
            }, 360, 360);
        }
    }

    private static void patchUnits() {
        Vars.content.units().each(u->{
            u.controller = (un) -> new DumbAI();
        });
    }

    public static void despawnUnits() {
        Log.info("Time to despawn unused units!");
        Bundle.sendMessage("unitdespawn");
        Groups.unit.each(u->{
            if(!u.controller().toString().toLowerCase().startsWith("player"))
                u.kill();
        });
    }
}

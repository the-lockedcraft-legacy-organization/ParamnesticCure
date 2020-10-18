package com.mc_smp;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin{
    public static Logger log;
    FileConfiguration pcConfig = this.getConfig();
    @Override
    public void onEnable() {
        log = Logger.getLogger("Minecraft");
        this.saveDefaultConfig();
        byte givenVersion = (byte) pcConfig.getInt("configVersion");
        byte currentVersion = 1;
        if (givenVersion == currentVersion) {
            log.info("[Paramnestic] [Debug] Your config is up to date!.");
        } else {
            log.log(Level.INFO, "[Paramnestic] [Debug] Your config was outdated. It has been updated to v{0}", currentVersion);
            pcConfig.set("configVersion", currentVersion);
        }
        log.warning("[Paramnestic] [Dev] You have enabled an early development version of this plugin.");
        log.warning("[Paramnestic] [Dev] It will probably be unstable");
    }
    @Override
    public void onDisable() {
        saveDefaultConfig();
    }
    public void debugBroadcast(String debugMsg) {
        
    }
}


package me.brkstyl.pss;

import me.brkstyl.pss.commands.PSSCommand;
import me.brkstyl.pss.commands.ReportCommand;
import me.brkstyl.pss.config.LanguageManager;
import me.brkstyl.pss.listeners.ChatLockListener;
import me.brkstyl.pss.listeners.ConnectionListener;
import me.brkstyl.pss.listeners.FreezeListener;
import me.brkstyl.pss.listeners.MenuListener;
import me.brkstyl.pss.listeners.SpectateListener;
import me.brkstyl.pss.menus.MenuHandler;
import me.brkstyl.pss.reports.ReportService;
import me.brkstyl.pss.reports.ReportStore;
import me.brkstyl.pss.spectate.SpectateManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginMain extends JavaPlugin {

    private static PluginMain instance;
    private LanguageManager languageManager;
    private MenuHandler menuHandler;
    private ReportService reportService;
    private SpectateManager spectateManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Initializing basic components
        saveDefaultConfig();
        this.languageManager = new LanguageManager(this);
        this.menuHandler = new MenuHandler(this);
        this.reportService = new ReportService(new ReportStore(getDataFolder()));
        this.spectateManager = new SpectateManager(this);

        // 2. Registering the Command
        PSSCommand pssCommand = new PSSCommand(this);
        if (getCommand("pss") != null) {
            getCommand("pss").setExecutor(pssCommand);
            getCommand("pss").setTabCompleter(pssCommand);
        } else {
            getLogger().severe("Command 'pss' NOT found in plugin.yml! Commands will not work.");
        }

        ReportCommand reportCommand = new ReportCommand(this);
        if (getCommand("report") != null) {
            getCommand("report").setExecutor(reportCommand);
            getCommand("report").setTabCompleter(reportCommand);
        } else {
            getLogger().severe("Command 'report' NOT found in plugin.yml! Reports will not work.");
        }

        // 3. Registering Listeners
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new MenuListener(this), this);
        getServer().getPluginManager().registerEvents(new SpectateListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatLockListener(this), this);
        getServer().getPluginManager().registerEvents(new FreezeListener(this), this);

        getLogger().info("PSS (Player and Server Security) has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("PSS has been safely disabled.");
    }

    public static PluginMain getInstance() { return instance; }
    public LanguageManager getLanguageManager() { return languageManager; }
    public MenuHandler getMenuHandler() { return menuHandler; }
    public ReportService getReportService() { return reportService; }
    public SpectateManager getSpectateManager() { return spectateManager; }
}
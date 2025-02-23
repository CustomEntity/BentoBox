package world.bentobox.bentobox;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import world.bentobox.bentobox.api.configuration.Config;
import world.bentobox.bentobox.api.events.BentoBoxReadyEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.Notifier;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.commands.BentoBoxCommand;
import world.bentobox.bentobox.hooks.DynmapHook;
import world.bentobox.bentobox.hooks.placeholders.MVdWPlaceholderAPIHook;
import world.bentobox.bentobox.hooks.MultiverseCoreHook;
import world.bentobox.bentobox.hooks.placeholders.PlaceholderAPIHook;
import world.bentobox.bentobox.hooks.VaultHook;
import world.bentobox.bentobox.listeners.BannedVisitorCommands;
import world.bentobox.bentobox.listeners.BlockEndDragon;
import world.bentobox.bentobox.listeners.DeathListener;
import world.bentobox.bentobox.listeners.JoinLeaveListener;
import world.bentobox.bentobox.listeners.NetherTreesListener;
import world.bentobox.bentobox.listeners.PanelListenerManager;
import world.bentobox.bentobox.listeners.PortalTeleportationListener;
import world.bentobox.bentobox.listeners.StandardSpawnProtectionListener;
import world.bentobox.bentobox.managers.AddonsManager;
import world.bentobox.bentobox.managers.BlueprintsManager;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.FlagsManager;
import world.bentobox.bentobox.managers.HooksManager;
import world.bentobox.bentobox.managers.IslandDeletionManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlaceholdersManager;
import world.bentobox.bentobox.managers.PlayersManager;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.managers.WebManager;
import world.bentobox.bentobox.util.heads.HeadGetter;
import world.bentobox.bentobox.versions.ServerCompatibility;

/**
 * Main BentoBox class
 * @author tastybento, Poslovitch
 */
public class BentoBox extends JavaPlugin {
    private static BentoBox instance;

    // Databases
    private PlayersManager playersManager;
    private IslandsManager islandsManager;

    // Managers
    private CommandsManager commandsManager;
    private LocalesManager localesManager;
    private AddonsManager addonsManager;
    private FlagsManager flagsManager;
    private IslandWorldManager islandWorldManager;
    private RanksManager ranksManager;
    private BlueprintsManager blueprintsManager;
    private HooksManager hooksManager;
    private PlaceholdersManager placeholdersManager;
    private IslandDeletionManager islandDeletionManager;
    private WebManager webManager;

    // Settings
    private Settings settings;

    // Notifier
    private Notifier notifier;

    private HeadGetter headGetter;

    private boolean isLoaded;

    // Metrics
    @Nullable
    private BStats metrics;

    @Override
    public void onEnable(){
        if (!ServerCompatibility.getInstance().checkCompatibility().isCanLaunch()) {
            // The server's most likely incompatible.
            // Show a warning
            logWarning("************ Disclaimer **************");
            logWarning("BentoBox may not be compatible with this server!");
            logWarning("BentoBox is tested only on the following Spigot versions:");

            List<String> versions = ServerCompatibility.ServerVersion.getVersions(ServerCompatibility.Compatibility.COMPATIBLE, ServerCompatibility.Compatibility.SUPPORTED)
                    .stream().map(ServerCompatibility.ServerVersion::toString).collect(Collectors.toList());

            logWarning(String.join(", ", versions));
            logWarning("**************************************");
        }

        // Not loaded
        isLoaded = false;
        // Store the current millis time so we can tell how many ms it took for BSB to fully load.
        final long loadStart = System.currentTimeMillis();

        // Save the default config from config.yml
        saveDefaultConfig();
        setInstance(this);
        // Load Flags
        flagsManager = new FlagsManager(this);

        if (!loadSettings()) {
            // We're aborting the load.
            return;
        }
        // Saving the config now.
        saveConfig();

        // Start Database managers
        playersManager = new PlayersManager(this);
        // Check if this plugin is now disabled (due to bad database handling)
        if (!this.isEnabled()) {
            return;
        }
        islandsManager = new IslandsManager(this);
        ranksManager = new RanksManager();

        // Start head getter
        headGetter = new HeadGetter(this);

        // Load Notifier
        notifier = new Notifier();

        // Set up command manager
        commandsManager = new CommandsManager();

        // Load BentoBox commands
        new BentoBoxCommand();

        // Start Island Worlds Manager
        islandWorldManager = new IslandWorldManager(this);

        // Load blueprints manager
        blueprintsManager = new BlueprintsManager(this);

        // Locales manager must be loaded before addons
        localesManager = new LocalesManager(this);

        // Load hooks
        hooksManager = new HooksManager(this);
        hooksManager.registerHook(new VaultHook());

        // Load addons. Addons may load worlds, so they must go before islands are loaded.
        addonsManager = new AddonsManager(this);
        addonsManager.loadAddons();

        final long loadTime = System.currentTimeMillis() - loadStart;

        getServer().getScheduler().runTask(instance, () -> {
            final long enableStart = System.currentTimeMillis();
            hooksManager.registerHook(new PlaceholderAPIHook());
            hooksManager.registerHook(new MVdWPlaceholderAPIHook());
            // Setup the Placeholders manager
            placeholdersManager = new PlaceholdersManager(this);

            // Enable addons
            addonsManager.enableAddons();

            // Register default gamemode placeholders
            addonsManager.getGameModeAddons().forEach(placeholdersManager::registerDefaultPlaceholders);

            // Register Listeners
            registerListeners();

            // Load islands from database - need to wait until all the worlds are loaded
            islandsManager.load();

            // Save islands & players data every X minutes
            instance.getServer().getScheduler().runTaskTimer(instance, () -> {
                playersManager.saveAll();
                islandsManager.saveAll();
            }, getSettings().getDatabaseBackupPeriod() * 20 * 60L, getSettings().getDatabaseBackupPeriod() * 20 * 60L);

            // Make sure all flag listeners are registered.
            flagsManager.registerListeners();

            // Load metrics
            if (settings.isMetrics()) {
                metrics = new BStats(this);
                metrics.registerMetrics();
            }

            // Register Multiverse hook - MV loads AFTER BentoBox
            // Make sure all worlds are already registered to Multiverse.
            hooksManager.registerHook(new MultiverseCoreHook());
            islandWorldManager.registerWorldsToMultiverse();

            // Register additional hooks
            hooksManager.registerHook(new DynmapHook());

            webManager = new WebManager(this);

            final long enableTime = System.currentTimeMillis() - enableStart;

            // Show banner
            User.getInstance(Bukkit.getConsoleSender()).sendMessage("successfully-loaded",
                    TextVariables.VERSION, instance.getDescription().getVersion(),
                    "[time]", String.valueOf(loadTime + enableTime));

            // Fire plugin ready event - this should go last after everything else
            isLoaded = true;
            Bukkit.getServer().getPluginManager().callEvent(new BentoBoxReadyEvent());
        });
    }

    /**
     * Registers listeners.
     */
    private void registerListeners() {
        PluginManager manager = getServer().getPluginManager();
        // Player join events
        manager.registerEvents(new JoinLeaveListener(this), this);
        // Panel listener manager
        manager.registerEvents(new PanelListenerManager(), this);
        // Standard Nether/End spawns protection
        manager.registerEvents(new StandardSpawnProtectionListener(this), this);
        // Nether portals
        manager.registerEvents(new PortalTeleportationListener(this), this);
        // Nether trees conversion
        manager.registerEvents(new NetherTreesListener(this), this);
        // End dragon blocking
        manager.registerEvents(new BlockEndDragon(this), this);
        // Banned visitor commands
        manager.registerEvents(new BannedVisitorCommands(this), this);
        // Death counter
        manager.registerEvents(new DeathListener(this), this);
        // Island Delete Manager
        islandDeletionManager = new IslandDeletionManager(this);
        manager.registerEvents(islandDeletionManager, this);
    }

    @Override
    public void onDisable() {
        if (addonsManager != null) {
            addonsManager.disableAddons();
        }
        // Save data
        if (playersManager != null) {
            playersManager.shutdown();
        }
        if (islandsManager != null) {
            islandsManager.shutdown();
        }
    }

    /**
     * Returns the player database
     * @return the player database
     */
    public PlayersManager getPlayers(){
        return playersManager;
    }

    /**
     * Returns the island database
     * @return the island database
     */
    public IslandsManager getIslands(){
        return islandsManager;
    }

    private static void setInstance(BentoBox plugin) {
        BentoBox.instance = plugin;
    }

    public static BentoBox getInstance() {
        return instance;
    }

    /**
     * @return the Commands manager
     */
    public CommandsManager getCommandsManager() {
        return commandsManager;
    }

    /**
     * @return the Locales manager
     */
    public LocalesManager getLocalesManager() {
        return localesManager;
    }

    /**
     * @return the Addons manager
     */
    public AddonsManager getAddonsManager() {
        return addonsManager;
    }

    /**
     * @return the Flags manager
     */
    public FlagsManager getFlagsManager() {
        return flagsManager;
    }

    /**
     * @return the ranksManager
     */
    public RanksManager getRanksManager() {
        return ranksManager;
    }

    /**
     * @return the Island World Manager
     */
    public IslandWorldManager getIWM() {
        return islandWorldManager;
    }

    /**
     * @return the settings
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * Loads the settings from the config file.
     * If it fails, it can shut the plugin down.
     * @return {@code true} if it loaded successfully.
     * @since 1.3.0
     */
    public boolean loadSettings() {
        log("Loading Settings from config.yml...");
        // Load settings from config.yml. This will check if there are any issues with it too.
        settings = new Config<>(this, Settings.class).loadConfigObject();
        if (settings == null) {
            // Settings did not load correctly. Disable plugin.
            logError("Settings did not load correctly - disabling plugin - please check config.yml");
            getPluginLoader().disablePlugin(this);
            return false;
        }
        return true;
    }

    @Override
    public void saveConfig() {
        if (settings != null) new Config<>(this, Settings.class).saveConfigObject(settings);
    }

    /**
     * @return the notifier
     */
    public Notifier getNotifier() {
        return notifier;
    }

    /**
     * @return the headGetter
     */
    public HeadGetter getHeadGetter() {
        return headGetter;
    }

    public void log(String string) {
        getLogger().info(() -> string);
    }

    public void logDebug(Object object) {
        getLogger().info(() -> "DEBUG: " + object);
    }

    public void logError(String error) {
        getLogger().severe(() -> error);
    }

    /**
     * Logs the stacktrace of a Throwable that was thrown by an error.
     * It should be used preferably instead of {@link Throwable#printStackTrace()} as it does not risk exposing sensitive information.
     * @param throwable the Throwable that was thrown by an error.
     * @since 1.3.0
     */
    public void logStacktrace(@NonNull Throwable throwable) {
        logError(ExceptionUtils.getStackTrace(throwable));
    }

    public void logWarning(String warning) {
        getLogger().warning(() -> warning);
    }

    /**
     * Returns the instance of the {@link BlueprintsManager}.
     * @return the {@link BlueprintsManager}.
     * @since 1.5.0
     */
    public BlueprintsManager getBlueprintsManager() {
        return blueprintsManager;
    }

    /**
     * Returns whether BentoBox is fully loaded or not.
     * This basically means that all managers are instantiated and can therefore be safely accessed.
     * @return whether BentoBox is fully loaded or not.
     */
    public boolean isLoaded() {
        return isLoaded;
    }

    /**
     * @return the HooksManager
     */
    public HooksManager getHooks() {
        return hooksManager;
    }

    /**
     * Convenience method to get the VaultHook.
     * @return the Vault hook
     */
    public Optional<VaultHook> getVault() {
        return Optional.ofNullable((VaultHook) hooksManager.getHook("Vault").orElse(null));
    }

    /**
     * @return the PlaceholdersManager.
     */
    public PlaceholdersManager getPlaceholdersManager() {
        return placeholdersManager;
    }

    /**
     * @return the islandDeletionManager
     * @since 1.1
     */
    public IslandDeletionManager getIslandDeletionManager() {
        return islandDeletionManager;
    }

    /**
     * @return an optional of the Bstats instance
     * @since 1.1
     */
    @NonNull
    public Optional<BStats> getMetrics() {
        return Optional.ofNullable(metrics);
    }

    /**
     * @return the {@link WebManager}.
     * @since 1.5.0
     */
    public WebManager getWebManager() {
        return webManager;
    }

    // Overriding default JavaPlugin methods

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#getDefaultWorldGenerator(java.lang.String, java.lang.String)
     */
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NonNull String worldName, String id) {
        return addonsManager.getDefaultWorldGenerator(worldName, id);
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#reloadConfig()
     */
    @Override
    public void reloadConfig() {
        loadSettings();
    }
}

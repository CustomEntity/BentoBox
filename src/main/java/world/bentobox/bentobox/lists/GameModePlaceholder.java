package world.bentobox.bentobox.lists;

import world.bentobox.bentobox.api.placeholders.GameModePlaceholderReplacer;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.RanksManager;
import world.bentobox.bentobox.util.Util;

import java.text.DateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public enum GameModePlaceholder {

    /* World-related */
    WORLD_FRIENDLY_NAME("world_friendly_name", (addon, user, island) -> addon.getWorldSettings().getFriendlyName()),
    /**
     * Returns the amount of islands in the world.
     * @since 1.5.0
     */
    WORLD_ISLANDS("world_islands", (addon, user, island) -> String.valueOf(addon.getIslands().getIslandCount(addon.getOverWorld()))),

    /* Island-related */

    // TODO: change the two next placeholders as they're related to the worlds instead of the islands
    ISLAND_DISTANCE("island_distance", (addon, user, island) -> String.valueOf(addon.getWorldSettings().getIslandDistance())),
    /**
     * Returns the island distance as a diameter (it is therefore equivalent to twice the island distance).
     * @since 1.5.0
     */
    ISLAND_DISTANCE_DIAMETER("island_distance_diameter", (addon, user, island) -> String.valueOf(2 * addon.getWorldSettings().getIslandDistance())),
    // ----------------------------------
    /**
     * Returns the island's protection range.
     * @since 1.4.0
     */
    ISLAND_PROTECTION_RANGE("island_protection_range", (addon, user, island) -> island == null ? "" : String.valueOf(island.getProtectionRange())),
    /**
     * Returns the island's protection range as a diameter (it is therefore equivalent to twice the island protection range).
     * @since 1.5.0
     */
    ISLAND_PROTECTION_RANGE_DIAMETER("island_protection_range_diameter", (addon, user, island) -> island == null ? "" : String.valueOf(2 * island.getProtectionRange())),
    ISLAND_OWNER("island_owner", (addon, user, island) -> island == null ? "" : addon.getPlayers().getName(island.getOwner())),
    ISLAND_CREATION_DATE("island_creation_date", (addon, user, island) -> island == null ? "" : DateFormat.getInstance().format(Date.from(Instant.ofEpochMilli(island.getCreatedDate())))),
    /**
     * @deprecated As of 1.5.2, for removal; use {@link #ISLAND_CENTER} instead.
     */
    @Deprecated
    ISLAND_SPAWNPOINT("island_spawnpoint", (addon, user, island) -> island == null ? "" : Util.xyz(island.getCenter().toVector())),
    ISLAND_NAME("island_name", (addon, user, island) -> {
        if (island == null || island.getName() == null) {
            return "";
        }
        return island.getName();
    }),
    /**
     * Returns the coordinates of the island's center.
     * @since 1.5.0
     */
    ISLAND_CENTER("island_center", (addon, user, island) -> island == null ? "" : Util.xyz(island.getCenter().toVector())),
    /**
     * Returns the X coordinate of the island's center.
     * @since 1.5.0
     */
    ISLAND_CENTER_X("island_center_x", (addon, user, island) -> island == null ? "" : String.valueOf(island.getCenter().getBlockX())),
    /**
     * Returns the Y coordinate of the island's center.
     * @since 1.5.0
     */
    ISLAND_CENTER_Y("island_center_y", (addon, user, island) -> island == null ? "" : String.valueOf(island.getCenter().getBlockY())),
    /**
     * Returns the Z coordinate of the island's center.
     * @since 1.5.0
     */
    ISLAND_CENTER_Z("island_center_z", (addon, user, island) -> island == null ? "" : String.valueOf(island.getCenter().getBlockZ())),
    /**
     * Returns the maximum number of members the island can have
     * @since 1.5.0
     */
    ISLAND_MEMBERS_MAX("island_members_max", (addon, user, island) -> island == null ? "" : String.valueOf(user.getPermissionValue(addon.getPermissionPrefix() + "team.maxsize", addon.getPlugin().getIWM().getMaxTeamSize(addon.getOverWorld())))),
    /**
     * Returns the amount of players that are at least MEMBER on this island.
     * @since 1.5.0
     */
    ISLAND_MEMBERS_COUNT("island_members_count", (addon, user, island) -> island == null ? "" : String.valueOf(island.getMemberSet().size())),
    /**
     * Returns the amount of players that are TRUSTED on this island.
     * @since 1.5.0
     */
    ISLAND_TRUSTEES_COUNT("island_trustees_count", (addon, user, island) -> island == null ? "" : String.valueOf(island.getMemberSet(RanksManager.TRUSTED_RANK, false).size())),
    /**
     * Returns the amount of players that are TRUSTED on this island.
     * @since 1.5.0
     */
    ISLAND_COOPS_COUNT("island_coops_count", (addon, user, island) -> island == null ? "" : String.valueOf(island.getMemberSet(RanksManager.COOP_RANK, false).size())),
    /**
     * Returns the amount of players that are currently visiting the island.
     * @since 1.5.0
     */
    ISLAND_VISITORS_COUNT("island_visitors_count", (addon, user, island) -> island == null ? "" : String.valueOf(island.getVisitors().size())),
    /**
     * Returns the amount of players banned from the island.
     * @since 1.5.0
     */
    ISLAND_BANS_COUNT("island_bans_count", (addon, user, island) -> island == null ? "" : String.valueOf(island.getBanned().size())),

    /* Visited island-related (= island the user is standing on) */
    /**
     * Returns the protection range of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_PROTECTION_RANGE("visited_island_protection_range", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getProtectionRange())).orElse("");
    }),
    /**
     * Returns the protection range of the island the player is standing on as a diameter.
     * @since 1.5.2
     */
    VISITED_ISLAND_PROTECTION_RANGE_DIAMETER("visited_island_protection_range_diameter", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(2*value.getProtectionRange())).orElse("");
    }),
    /**
     * Returns the name of the owner of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_OWNER("visited_island_owner", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> addon.getPlayers().getName(value.getOwner())).orElse("");
    }),
    /**
     * Returns the formatted creation date of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_CREATION_DATE("visited_island_creation_date", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> DateFormat.getInstance().format(Date.from(Instant.ofEpochMilli(value.getCreatedDate())))).orElse("");
    }),
    /**
     * Returns the name of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_NAME("visited_island_name", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(Island::getName).orElse("");
    }),
    /**
     * Returns the coordinates of the center of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_CENTER("visited_island_center", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> Util.xyz(value.getCenter().toVector())).orElse("");
    }),
    /**
     * Returns the X coordinate of the center of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_CENTER_X("visited_island_center_x", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getCenter().getBlockX())).orElse("");
    }),
    /**
     * Returns the Y coordinate of the center of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_CENTER_Y("visited_island_center_y", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getCenter().getBlockY())).orElse("");
    }),
    /**
     * Returns the Z coordinate of the center of the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_CENTER_Z("visited_island_center_z", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getCenter().getBlockZ())).orElse("");
    }),
    /**
     * Returns the maximum number of members the island the player is standing on can have.
     * @since 1.5.2
     */
    VISITED_ISLAND_MEMBERS_MAX("visited_island_members_max", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(user.getPermissionValue(addon.getPermissionPrefix() + "team.maxsize", addon.getPlugin().getIWM().getMaxTeamSize(addon.getOverWorld())))).orElse("");
    }),
    /**
     * Returns the amount of players that are at least MEMBER on the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_MEMBERS_COUNT("visited_island_members_count", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getMemberSet().size())).orElse("");
    }),
    /**
     * Returns the amount of players that are TRUSTED on the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_TRUSTEES_COUNT("visited_island_trustees_count", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getMemberSet(RanksManager.TRUSTED_RANK, false).size())).orElse("");
    }),
    /**
     * Returns the amount of players that are TRUSTED on the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_COOPS_COUNT("visited_island_coops_count", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getMemberSet(RanksManager.COOP_RANK, false).size())).orElse("");
    }),
    /**
     * Returns the amount of players that are currently visiting the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_VISITORS_COUNT("visited_island_visitors_count", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getVisitors().size())).orElse("");
    }),
    /**
     * Returns the amount of players banned from the island the player is standing on.
     * @since 1.5.2
     */
    VISITED_ISLAND_BANS_COUNT("visited_island_bans_count", (addon, user, island) -> {
        if (user == null || !user.isPlayer() || user.getLocation() == null) {
            return "";
        }
        Optional<Island> visitedIsland = addon.getIslands().getIslandAt(user.getLocation());
        return visitedIsland.map(value -> String.valueOf(value.getBanned().size())).orElse("");
    }),

    /* Player-related */
    /**
     * Returns whether this player has an island or not.
     * @since 1.5.0
     */
    HAS_ISLAND("has_island", (addon, user, island) -> String.valueOf(island != null)),
    /**
     * Returns the rank this player has on his island.
     * @since 1.5.0
     */
    RANK("rank", (addon, user, island) -> (island == null || user == null) ? "" : user.getTranslation(addon.getPlugin().getRanksManager().getRank(island.getRank(user)))),
    /**
     * Returns how many times this player reset his island.
     * @since 1.5.0
     */
    RESETS("resets", (addon, user, island) -> String.valueOf(addon.getPlayers().getResets(addon.getOverWorld(), user.getUniqueId()))),
    /**
     * Returns how many times this player can reset his island.
     * {@code -1} is unlimited.
     * @since 1.5.0
     */
    RESETS_LEFT("resets_left", (addon, user, island) -> String.valueOf(addon.getPlayers().getResetsLeft(addon.getOverWorld(), user.getUniqueId())));

    private String placeholder;
    /**
     * @since 1.5.0
     */
    private GameModePlaceholderReplacer replacer;

    GameModePlaceholder(String placeholder, GameModePlaceholderReplacer replacer) {
        this.placeholder = placeholder;
        this.replacer = replacer;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    /**
     * @since 1.5.0
     */
    public GameModePlaceholderReplacer getReplacer() {
        return replacer;
    }
}

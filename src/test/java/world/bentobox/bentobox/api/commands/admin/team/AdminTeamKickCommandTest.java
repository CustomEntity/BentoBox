package world.bentobox.bentobox.api.commands.admin.team;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.CommandsManager;
import world.bentobox.bentobox.managers.IslandWorldManager;
import world.bentobox.bentobox.managers.IslandsManager;
import world.bentobox.bentobox.managers.LocalesManager;
import world.bentobox.bentobox.managers.PlayersManager;

/**
 * @author tastybento
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Bukkit.class, BentoBox.class, User.class })
public class AdminTeamKickCommandTest {

    private CompositeCommand ac;
    private UUID uuid;
    private User user;
    private IslandsManager im;
    private PlayersManager pm;
    private UUID notUUID;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        // Set up plugin
        BentoBox plugin = mock(BentoBox.class);
        Whitebox.setInternalState(BentoBox.class, "instance", plugin);

        // Command manager
        CommandsManager cm = mock(CommandsManager.class);
        when(plugin.getCommandsManager()).thenReturn(cm);

        // Player
        Player p = mock(Player.class);
        // Sometimes use Mockito.withSettings().verboseLogging()
        user = mock(User.class);
        when(user.isOp()).thenReturn(false);
        uuid = UUID.randomUUID();
        notUUID = UUID.randomUUID();
        while(notUUID.equals(uuid)) {
            notUUID = UUID.randomUUID();
        }
        when(user.getUniqueId()).thenReturn(uuid);
        when(user.getPlayer()).thenReturn(p);
        when(user.getName()).thenReturn("tastybento");
        User.setPlugin(plugin);

        // Parent command has no aliases
        ac = mock(CompositeCommand.class);
        when(ac.getSubCommandAliases()).thenReturn(new HashMap<>());

        // Island World Manager
        IslandWorldManager iwm = mock(IslandWorldManager.class);
        when(plugin.getIWM()).thenReturn(iwm);


        // Player has island to begin with
        im = mock(IslandsManager.class);
        when(im.hasIsland(Mockito.any(), Mockito.any(UUID.class))).thenReturn(true);
        when(im.hasIsland(Mockito.any(), Mockito.any(User.class))).thenReturn(true);
        when(im.isOwner(Mockito.any(),Mockito.any())).thenReturn(true);
        when(im.getOwner(Mockito.any(),Mockito.any())).thenReturn(uuid);
        when(plugin.getIslands()).thenReturn(im);

        // Has team
        pm = mock(PlayersManager.class);
        when(im.inTeam(Mockito.any(), Mockito.eq(uuid))).thenReturn(true);

        when(plugin.getPlayers()).thenReturn(pm);

        // Server & Scheduler
        BukkitScheduler sch = mock(BukkitScheduler.class);
        PowerMockito.mockStatic(Bukkit.class);
        when(Bukkit.getScheduler()).thenReturn(sch);
        when(Bukkit.getPluginManager()).thenReturn(mock(PluginManager.class));

        // Locales
        LocalesManager lm = mock(LocalesManager.class);
        when(lm.get(Mockito.any(), Mockito.any())).thenReturn("mock translation");
        when(plugin.getLocalesManager()).thenReturn(lm);

        // Addon
        when(iwm.getAddon(Mockito.any())).thenReturn(Optional.empty());

        // Plugin Manager
        Server server = mock(Server.class);
        PluginManager pim = mock(PluginManager.class);
        when(server.getPluginManager()).thenReturn(pim);
        when(Bukkit.getServer()).thenReturn(server);
    }


    /**
     * Test method for {@link AdminTeamKickCommand#canExecute(User, String, List)}.
     */
    @Test
    public void testCanExecuteNoTarget() {
        AdminTeamKickCommand itl = new AdminTeamKickCommand(ac);
        assertFalse(itl.canExecute(user, itl.getLabel(), new ArrayList<>()));
        // Show help
    }

    /**
     * Test method for {@link AdminTeamKickCommand#canExecute(User, String, List)}.
     */
    @Test
    public void testCanExecuteUnknownPlayer() {
        AdminTeamKickCommand itl = new AdminTeamKickCommand(ac);
        String[] name = {"tastybento"};
        when(pm.getUUID(Mockito.any())).thenReturn(null);
        assertFalse(itl.canExecute(user, itl.getLabel(), Arrays.asList(name)));
        Mockito.verify(user).sendMessage("general.errors.unknown-player", "[name]", name[0]);
    }

    /**
     * Test method for {@link AdminTeamKickCommand#canExecute(User, String, List)}.
     */
    @Test
    public void testCanExecutePlayerNotInTeam() {
        AdminTeamKickCommand itl = new AdminTeamKickCommand(ac);
        String[] name = {"tastybento"};
        when(pm.getUUID(Mockito.any())).thenReturn(notUUID);
        when(im.getMembers(Mockito.any(), Mockito.any())).thenReturn(new HashSet<>());
        assertFalse(itl.canExecute(user, itl.getLabel(), Arrays.asList(name)));
        Mockito.verify(user).sendMessage(Mockito.eq("commands.admin.team.kick.not-in-team"));
    }

    /**
     * Test method for {@link AdminTeamKickCommand#execute(User, String, List)} .
     */
    @Test
    public void testExecuteKickOwner() {
        when(im.inTeam(Mockito.any(), Mockito.any())).thenReturn(true);
        Island is = mock(Island.class);
        when(im.getIsland(Mockito.any(), Mockito.any(UUID.class))).thenReturn(is);
        String[] name = {"tastybento"};
        when(pm.getUUID(Mockito.any())).thenReturn(notUUID);

        when(is.getOwner()).thenReturn(notUUID);

        AdminTeamKickCommand itl = new AdminTeamKickCommand(ac);
        assertFalse(itl.execute(user, itl.getLabel(), Arrays.asList(name)));
        Mockito.verify(user).sendMessage(Mockito.eq("commands.admin.team.kick.cannot-kick-owner"));
        Mockito.verify(is).showMembers(Mockito.any());
    }

    /**
     * Test method for {@link world.bentobox.bentobox.api.commands.admin.team.AdminTeamKickCommand#execute(User, String, List)}.
     */
    @Test
    public void testExecute() {
        when(im.inTeam(Mockito.any(), Mockito.any())).thenReturn(true);
        Island is = mock(Island.class);
        when(im.getIsland(Mockito.any(), Mockito.any(UUID.class))).thenReturn(is);
        String name = "tastybento";
        when(pm.getUUID(Mockito.any())).thenReturn(notUUID);
        when(pm.getName(Mockito.any())).thenReturn(name);

        when(is.getOwner()).thenReturn(uuid);

        AdminTeamKickCommand itl = new AdminTeamKickCommand(ac);
        assertTrue(itl.execute(user, itl.getLabel(), Collections.singletonList(name)));
        Mockito.verify(im).removePlayer(Mockito.any(), Mockito.eq(notUUID));
        Mockito.verify(user).sendMessage(Mockito.eq("commands.admin.team.kick.success"), Mockito.eq(TextVariables.NAME), Mockito.eq(name), Mockito.eq("[owner]"), Mockito.anyString());
    }

}

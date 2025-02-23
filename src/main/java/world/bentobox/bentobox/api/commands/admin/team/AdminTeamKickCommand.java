package world.bentobox.bentobox.api.commands.admin.team;

import org.bukkit.Bukkit;
import org.eclipse.jdt.annotation.NonNull;
import world.bentobox.bentobox.api.commands.CompositeCommand;
import world.bentobox.bentobox.api.events.IslandBaseEvent;
import world.bentobox.bentobox.api.events.team.TeamEvent;
import world.bentobox.bentobox.api.localization.TextVariables;
import world.bentobox.bentobox.api.user.User;
import world.bentobox.bentobox.database.objects.Island;

import java.util.List;
import java.util.UUID;

/**
 * Kicks the specified player from the island team.
 * @author tastybento
 */
public class AdminTeamKickCommand extends CompositeCommand {

    public AdminTeamKickCommand(CompositeCommand parent) {
        super(parent, "kick");
    }

    @Override
    public void setup() {
        setPermission("mod.team");
        setParametersHelp("commands.admin.team.kick.parameters");
        setDescription("commands.admin.team.kick.description");
    }

    @Override
    public boolean canExecute(User user, String label, List<String> args) {
        // If args are not right, show help
        if (args.size() != 1) {
            showHelp(this, user);
            return false;
        }

        // Get target
        UUID targetUUID = getPlayers().getUUID(args.get(0));
        if (targetUUID == null) {
            user.sendMessage("general.errors.unknown-player", TextVariables.NAME, args.get(0));
            return false;
        }
        if (!getIslands().inTeam(getWorld(), targetUUID)) {
            user.sendMessage("commands.admin.team.kick.not-in-team");
            return false;
        }

        return true;
    }

    @Override
    public boolean execute(User user, String label, @NonNull List<String> args) {
        UUID targetUUID = getPlayers().getUUID(args.get(0));

        Island island = getIslands().getIsland(getWorld(), targetUUID);

        if (targetUUID.equals(island.getOwner())) {
            user.sendMessage("commands.admin.team.kick.cannot-kick-owner");
            island.showMembers(user);
            return false;
        }
        User target = User.getInstance(targetUUID);
        target.sendMessage("commands.admin.team.kick.admin-kicked");

        getIslands().removePlayer(getWorld(), targetUUID);
        user.sendMessage("commands.admin.team.kick.success", TextVariables.NAME, target.getName(), "[owner]", getPlayers().getName(island.getOwner()));

        // Fire event so add-ons know
        IslandBaseEvent event = TeamEvent.builder()
                .island(island)
                .reason(TeamEvent.Reason.KICK)
                .involvedPlayer(targetUUID)
                .admin(true)
                .build();
        Bukkit.getServer().getPluginManager().callEvent(event);
        return true;
    }
}

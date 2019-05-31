package world.bentobox.bentobox.hooks;

import org.bukkit.Material;
import world.bentobox.bentobox.api.hooks.Hook;

/**
 * @author Poslovitch
 * @since 1.5.0
 */
public class WDLCompanionHook extends Hook {

    public WDLCompanionHook() {
        super("WDLCompanion", Material.ARROW);
    }

    @Override
    public boolean hook() {
        return false;
    }

    @Override
    public String getFailureCause() {
        return "the version of WDLCompanion you're using is incompatible with this hook";
    }
}

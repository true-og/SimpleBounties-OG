package net.trueog.simplebountiesog;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BountyEvents implements Listener {

	public BountyCommands  bountyCommands = null;

	@EventHandler
	public void onKill(PlayerDeathEvent event) {

		try {

			String killed = event.getEntity().getName();
			String killer = event.getEntity().getKiller().getName();
			if (killed != killer) {

				// event.setDeathMessage(ChatColor.RED + killed + " has been murdered by " + killer);

				bountyCheck(killed, killer);

			}

		}
		catch (Exception error) {}

		/*(catch (Exception exception) {
            String killed = e.getEntity().getName();
            String killer = "God";
            // error.setDeathMessage(ChatColor.RED + killed + " has been murdered by " + killer);
            bountyCheck(killed, killer);
        }*/

	}

	// Check if killed player has a bounty on them. If it was a valid bounty, complete the bounty.
	private void bountyCheck(String killed, String killer) {

		bountyCommands = SimpleBountiesOG.getBountyCommands();
		if (bountyCommands == null) {

			Bukkit.getLogger().warning("BountyEvents has no reference to BountyCommands");

		}
		else {

			if (bountyCommands.isValidBounty(killed)) {

				bountyCommands.completeBounty(killed, killer);

			}
			else {

				Bukkit.getLogger().info("bountyCheck: isValidBounty is false");

			}

		}

	}

}
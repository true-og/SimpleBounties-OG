package net.trueog.simplebountiesog;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger; // Vault

import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class SimpleBountiesOG extends JavaPlugin {

	FileConfiguration config = getConfig();
	BountyEvents bountyEvents = new BountyEvents();
	TabCompleter tabCompleter = new TabCompletion();
	private static final Logger log = Logger.getLogger("Minecraft");
	private static Permission perms = null;
	static BountyCommands bountyCommands;

	// Plugin startup logic.
	@Override
	public void onEnable() {

		config.options().copyDefaults(true);
		saveConfig();

		// Initialize bounty commands.
		bountyCommands = new BountyCommands(this);

		getServer().getPluginCommand("bounty").setExecutor(bountyCommands);
		getServer().getPluginManager().registerEvents(bountyEvents, this);

		getCommand("bounty").setTabCompleter(tabCompleter);

		// So we have an empty bounty buffer and it all comes from saved data.
		bountyCommands.clearBounties();

		loadBounties();

		setupPermissions();

		log.info("SimpleBounties-OG has loaded correctly.");

	}

	// Plugin shutdown logic.
	@Override
	public void onDisable() {

		saveBounties();

		config.options().copyDefaults(true);
		saveConfig();

		log.info(String.format("[%s] Disabled Version %s", getPluginMeta().getName(), getPluginMeta().getVersion()));

	}

	// DATA STRUCTURE: SENDER, TARGET, REWARD.
	private void loadBounties() {

		log.info("[Bounties] Loading bounties...");

		List<String> bountiesToLoad = new ArrayList<String>();
		List<String> tempBountyInfo = new ArrayList<String>();
		bountiesToLoad = config.getStringList("bounties");

		// 0, 3, 6, 9, 12, etc.
		int i = 0;
		// Temp counter, for use inside the sets of three.
		int t = 0;
		for (String s : bountiesToLoad) {

			if (i % 3 <= 0) {

				t = 0;

			}
			if (t == 0 || t == 1) {

				tempBountyInfo.add(s);

				t++;

			}
			else if (t == 2) {

				tempBountyInfo.add(s);

				// Tells bountycommands to load a bounty with the tempinfo data.
				bountyCommands.loadBounty(tempBountyInfo);

				tempBountyInfo.clear();

			}

			i++;

		}

	}

	private void saveBounties() {

		log.info("[Bounties] Saving bounties...");

		List<String> tempBountyInfo = new ArrayList<String>();
		for (Bounty b: bountyCommands.bounties) {

			tempBountyInfo.addAll(bountyCommands.seperateBounty(b));

		}

		config.set("bounties", tempBountyInfo);

	}

	private boolean setupPermissions() {

		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		perms = rsp.getProvider();

		return perms != null;

	}

	public static BountyCommands getBountyCommands() {

		return bountyCommands;

	}

}
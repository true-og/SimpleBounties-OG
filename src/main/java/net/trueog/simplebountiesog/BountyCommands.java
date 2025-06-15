package net.trueog.simplebountiesog;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.trueog.utilitiesog.UtilitiesOG;

public class BountyCommands implements CommandExecutor {

	private final SimpleBountiesOG main;
	private final EconomyHandler diamondbank;
	public List<Bounty> bounties = new ArrayList<Bounty>();

	public BountyCommands(SimpleBountiesOG main, EconomyHandler economyHandler) {

		this.main = main;
		this.diamondbank = economyHandler;

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 0) {

			if(sender instanceof Player) {	

				UtilitiesOG.trueogMessage((Player) sender, "<red>You are missing arguments.</red>");

			}

			return true;

		}

		// Check if the sender is a player.
		if(sender instanceof Player) {

			Player player = (Player) sender;

			// Check permissions for the player.
			if (! player.isOp() || ! player.hasPermission("bounties.*")) {

				UtilitiesOG.trueogMessage(player, "<red>ERROR: You do not have permission to use that command!</red>");
				return true;

			}

		}
		else {

			// Check permissions for the console.
			if (! sender.isOp() || ! sender.hasPermission("bounties.*")) {

				main.getLogger().info("ERROR: You do not have permission to use that command!");
				return true;

			}

		}

		// Handle help command.
		if (args[0].equalsIgnoreCase("help")) {

			sendHelpMessage(sender);
			return true;

		}

		// Handle commands based on player or console.
		if (sender instanceof Player) {

			Player player = (Player) sender;

			return handlePlayerCommands(player, args);

		}
		else {

			return handleConsoleCommands(sender, args);

		}

	}

	// Sends help message based on sender type.
	private void sendHelpMessage(CommandSender sender) {

		// Check if the sender is a player.
		if (sender instanceof Player) {

			Player player = (Player) sender;
			if (player.hasPermission("bounties.admin")) {

				UtilitiesOG.trueogMessage(player, """
						    <gold>How to use the /bounty command:</gold>
						    <newline><gold>/bounty place <target> <reward></gold> - Place a bounty on a target for a reward.
						    <newline><gold>/bounty edit <target> (optional - placer) <new_reward></gold> - Edit an existing bounty's reward.
						    <newline><gold>/bounty remove <target> (optional - placer)</gold> - Remove an existing bounty.
						    <newline><gold>/bounty clearall</gold> - Clear all bounties.
						    <newline><gray>You can also use /bn instead of /bounty.</gray>
						""");

			}
			else {

				UtilitiesOG.trueogMessage(player, """
						    <gold>How to use the /bounty command:</gold>
						    <newline><gold>/bounty place <target> <reward></gold> - Place a bounty on a target for a reward.
						    <newline><gold>/bounty edit <target> <new_reward></gold> - Edit an existing bounty's reward.
						    <newline><gold>/bounty remove <target></gold> - Remove an existing bounty.
						    <newline><gray>You can also use /bn instead of /bounty.</gray>
						""");

			}

		}
		else if (sender instanceof ConsoleCommandSender) {

			// Console variant: No color, plain text.
			main.getLogger().info("How to use the /bounty command:");
			main.getLogger().info("/bounty place <target> <reward> - Place a bounty on a target for a reward.");
			main.getLogger().info("/bounty edit <target> (optional - placer) <new_reward> - Edit an existing bounty's reward.");
			main.getLogger().info("/bounty remove <target> (optional - placer) - Remove an existing bounty.");
			main.getLogger().info("/bounty clearall - Clear all bounties.");
			main.getLogger().info("You can also use /bn instead of /bounty.");

		}

	}

	// Handles player-specific commands.
	private boolean handlePlayerCommands(Player player, String[] args) {

		String command = args[0].toLowerCase();

		switch (command) {
		case "place":
			if (args.length < 3) {

				UtilitiesOG.trueogMessage(player, "<red>Usage: /bounty place <target> <reward></red>");

				return true;

			}

			try {

				Double.parseDouble(args[2]);

			}
			catch (NumberFormatException error) {

				UtilitiesOG.trueogMessage(player, "<red>Invalid reward amount. Must be a number.</red>");

				return true;

			}

			placeBounty(player, args[1], args[2]);

			return true;
		case "remove":
			if (args.length < 2) {

				UtilitiesOG.trueogMessage(player, "<red>Usage: /bounty remove <target></red>");

				return true;

			}

			cancelBounty(player, args[1], args.length >= 3 ? args[2] : "null");

			return true;
		case "edit":
			if (args.length < 3) {

				UtilitiesOG.trueogMessage(player, "<red>Usage: /bounty edit <target> <new_reward></red>");

				return true;

			}

			try {

				Double.parseDouble(args[2]);

			}
			catch (NumberFormatException error) {

				UtilitiesOG.trueogMessage(player, "<red>Invalid reward amount. Must be a number.</red>");

				return true;

			}

			editBounty(player, args[1], "null", args[2]);

			return true;
		case "list":
			if (bounties.isEmpty()) {

				UtilitiesOG.trueogMessage(player, "<red>No bounties!</red>");

			}
			else {

				for (Bounty bounty : bounties) {

					UtilitiesOG.trueogMessage(player, "<gold>BOUNTY: " + bounty.TARGET + " PLACER: " + bounty.SENDER + " REWARD: <red>$" + bounty.REWARD + "</red></gold>");

				}

				UtilitiesOG.trueogMessage(player, "<green>Bounties shown.</green>");

			}

			return true;
		case "clearall":
			if (player.hasPermission("bounties.admin")) {

				bounties.clear();

				UtilitiesOG.trueogMessage(player, "<green>All bounties cleared.</green>");

			}
			else {

				UtilitiesOG.trueogMessage(player, "<red>You don't have permission to clear all bounties.</red>");

			}

			return true;
		default:
			UtilitiesOG.trueogMessage(player, "<red>Unrecognized command.</red>");

			return true;
		}

	}

	// Handles console-specific commands (no colors).
	private boolean handleConsoleCommands(CommandSender sender, String[] args) {

		String command = args[0].toLowerCase();
		switch (command) {
		case "place":
			if (args.length < 3) {

				sender.sendMessage("Usage: /bounty place <target> <reward>");

				return true;

			}

			try {

				Double.parseDouble(args[2]);

			}
			catch (NumberFormatException error) {

				sender.sendMessage("Invalid reward amount. Must be a number.");

				return true;

			}

			placeBounty(sender, args[1], args[2]);

			return true;
		case "remove":
			if (args.length < 2) {

				sender.sendMessage("Usage: /bounty remove <target>");

				return true;

			}

			cancelBounty(sender, args[1], args.length >= 3 ? args[2] : "God");

			return true;
		case "edit":
			if (args.length < 3) {

				sender.sendMessage("Usage: /bounty edit <target> <new_reward>");

				return true;

			}
			try {

				Double.parseDouble(args[2]);

			}
			catch (NumberFormatException error) {

				sender.sendMessage("Invalid reward amount. Must be a number.");

				return true;

			}

			editBounty(sender, args[1], "God", args[2]);

			return true;
		case "list":
			if (bounties.isEmpty()) {

				sender.sendMessage("No bounties!");

			}
			else {

				for (Bounty bounty : bounties) {

					sender.sendMessage("BOUNTY: " + bounty.TARGET + " PLACER: " + bounty.SENDER + " REWARD: $" + bounty.REWARD);

				}

				sender.sendMessage("Bounties shown.");

			}

			return true;
		case "clearall":
			bounties.clear();

			sender.sendMessage("All bounties cleared.");

			return true;
		default:
			sender.sendMessage("Unrecognized command.");

			return true;
		}

	}

	private void placeBounty(CommandSender sender, String target, String reward) {

		Bounty bounty = new Bounty();
		// Check if player exists with this name, only works for online players.
		if (isValidTarget(target)) {

			bounty.TARGET = target;
			bounty.REWARD = reward;
			if (sender instanceof Player) {

				Player pSender = (Player) sender;
				if (! hasPlacedBounty(pSender.getName(), target)) {

					bounty.SENDER = pSender.getName();

					diamondbank.withdraw(pSender, reward);

					// Place the bounty.
					bounties.add(bounty);

					UtilitiesOG.trueogMessage(pSender, "<green>Bounty placed on: </green>" + "<yellow>" + target + "<green>.</green>");

					// Notify all players.
					for (Player player : Bukkit.getOnlinePlayers()) {

						UtilitiesOG.trueogMessage(player, "<gold>" + pSender.getName() + " has placed a BOUNTY on " + target + " for <red>$" + reward + "</red></gold>");

					}

				}
				else {

					UtilitiesOG.trueogMessage(pSender, "<red>Bounty not placed: insufficient funds or invalid reward amount.</red>");

				}

			}
			else {

				if (sender instanceof Player) {

					Player pSender = (Player) sender;

					UtilitiesOG.trueogMessage(pSender, "<red>You have already placed a bounty on " + target + ".</red>");

				}
				else {

					UtilitiesOG.logToConsole("[SimpleBounties-OG]", "<red>You have already placed a bounty on " + target + ".</red>");

				}

			}

		}
		else {

			// Console placing a bounty.
			bounty.SENDER = "God";
			if (! hasPlacedBounty("God", target)) {

				bounties.add(bounty);

				main.getLogger().info("Bounty placed on: " + target + ".");

			}
			else {

				main.getLogger().info("The server has already placed a bounty on " + target + ".");

			}

		}

	}

	// Cancel a bounty on a target that the sender has placed.
	private void cancelBounty(CommandSender sender, String target, String placer) {

		boolean found = false;
		Bounty toCancel = new Bounty();
		if (isValidTarget(target)) {

			if (sender instanceof Player && placer.equals("null")) {

				Player p = (Player) sender;

				// Player is canceling their own bounty.
				for (Bounty bounty : bounties) {

					if (bounty.SENDER.equalsIgnoreCase(sender.getName()) && bounty.TARGET.equalsIgnoreCase(target)) {

						found = true;

						toCancel = bounty;

						diamondbank.deposit(p, bounty.REWARD);

						UtilitiesOG.trueogMessage(p, "<green>Bounty on " + target + " removed</green>");

						break;

					}

				}

				if (! found) {

					UtilitiesOG.trueogMessage(p, "<red>You have not placed a bounty on " + target + "</red>");

				}

			}
			else {

				if (sender instanceof Player) {

					Player p = (Player) sender;

					for (Bounty bounty : bounties) {

						if (bounty.SENDER.equalsIgnoreCase(placer) && bounty.TARGET.equalsIgnoreCase(target)) {

							found = true;
							toCancel = bounty;

							UtilitiesOG.trueogMessage(p, "<green>Bounty on " + target + " removed</green>");

							break;

						}

					}

					if (! found) {

						UtilitiesOG.trueogMessage(p, "<red>Bounty on " + target + " not found</red>");

					}

				}

			}

			bounties.remove(toCancel);

		}

	}

	// Only can change the reward.
	private void editBounty(CommandSender sender, String target, String placer, String reward) {

		boolean found = false;
		if (isValidTarget(target)) {

			if (sender instanceof Player && placer.equals("null")) {

				Player p = (Player) sender;
				for (Bounty bounty : bounties) {

					if (bounty.SENDER.equalsIgnoreCase(sender.getName()) && bounty.TARGET.equalsIgnoreCase(target)) {

						found = true;

						diamondbank.deposit(p, bounty.REWARD);

						diamondbank.withdraw(p, reward);

						bounty.REWARD = reward;

						UtilitiesOG.trueogMessage(p, "<green>Bounty on " + target + ": reward edited to <red>$" + reward + "</red></green>");

						break;

					}

				}
				if (! found) {

					UtilitiesOG.trueogMessage(p, "<red>You have not placed a bounty on " + target + "</red>");

				}

			}
			else {

				if (sender instanceof Player) {

					Player p = (Player) sender;
					for (Bounty bounty : bounties) {

						if (bounty.SENDER.equalsIgnoreCase(placer) && bounty.TARGET.equalsIgnoreCase(target)) {

							found = true;
							bounty.REWARD = reward;

							UtilitiesOG.trueogMessage(p, "<green>Bounty on " + target + " edited to <red>$" + reward + "</red></green>");

							break;

						}

					}
					if (! found) {

						UtilitiesOG.trueogMessage(p, "<red>Bounty on " + target + " not found</red>");

					}

				}

			}

		}

	}

	// Loads bounties.
	public void loadBounty(List<String> bounty) {

		Bounty loadBounty = new Bounty();
		if (bounty.toArray().length != 3) {

			// main.getLogger().info("loadBounty - Bounty to be added has improper length");

		}
		else {

			loadBounty.SENDER = bounty.get(0);
			loadBounty.TARGET = bounty.get(1);
			loadBounty.REWARD = bounty.get(2);

			// main.getLogger().info("loadBounty - bounty loaded!");

		}

		bounties.add(loadBounty);

	}

	// Splits bounty data into a string so Main can save it.
	public List<String> seperateBounty(Bounty b) {

		List<String> tmpBounty = new ArrayList<String>();

		tmpBounty.add(b.SENDER);
		tmpBounty.add(b.TARGET);
		tmpBounty.add(b.REWARD);

		return tmpBounty;

	}

	public void clearBounties() {

		bounties.clear();

	}

	private boolean isValidTarget(String target) {

		for (Player player : Bukkit.getOnlinePlayers()) {

			if (player.getName().equalsIgnoreCase(target)) {

				// main.getLogger().info("Online Target - Target is valid");

				return true;

			}

		}
		for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {

			if (op.getName().equalsIgnoreCase(target)) {

				// main.getLogger().info("Offline Target - Target is valid");

				return true;

			}

		}
		main.getLogger().info("Target is invalid");

		return false;

	}

	public boolean isValidBounty(String target) {

		// Bukkit.getLogger().info("isValidBounty: Checking for " + target);

		for (Bounty b : bounties) {

			if (b.TARGET.equalsIgnoreCase(target)) {

				return true;

			}

		}

		return false;

	}

	public boolean hasPlacedBounty(String placer, String target) {

		for (Bounty b : bounties) {

			if (b.SENDER.equalsIgnoreCase(placer)) {

				if (b.TARGET.equalsIgnoreCase(target)) {

					return true;

				}

			}

		}

		return false;

	}

	public boolean checkIfNegative(String reward) {

		double d = Double.parseDouble(reward);
		if (d < 0) {

			return true;

		}
		else {

			return false;

		}

	}

	// If there is a valid bounty, remove it as completed.
	public void completeBounty(String killed, String killer) {

		Bukkit.getLogger().info("A Bounty on " + killed + " has been completed by " + killer);

		List<Bounty> toCancel = new ArrayList<>();
		for (Bounty b : bounties) {

			if (b.TARGET.equalsIgnoreCase(killed)) {

				// Add bounty to cancellation list.
				toCancel.add(b);

			}

		}

		String amt = null;
		for (Bounty b : toCancel) {

			amt = b.REWARD;

			Player p = Bukkit.getPlayer(killer);

			// Deposit the reward to the killer's balance.
			diamondbank.deposit(p, amt);

			// Remove the bounty.
			bounties.remove(b);

		}

		// Notify all online players about the completed bounty.
		for (Player player : Bukkit.getOnlinePlayers()) {

			UtilitiesOG.trueogMessage(player, "<gold>" + killer + " has completed a bounty on " + killed + " for</gold> <aqua>" + amt + "Diamonds </aqua>");

		}

	}

}
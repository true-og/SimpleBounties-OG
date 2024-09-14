package net.trueog.simplebountiesog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.trueog.diamondbankog.DiamondBankAPI;
import net.trueog.diamondbankog.DiamondBankOG;
import net.trueog.diamondbankog.PostgreSQL;

public class BountyCommands implements CommandExecutor {

	private final SimpleBountiesOG main;
	public List<Bounty> bounties = new ArrayList<Bounty>();

	public BountyCommands (SimpleBountiesOG main) {

		this.main = main;

	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (args.length == 0) {

			sendMessage(sender, "<red>You are missing arguments.</red>");

			return true;

		}

		// Check if the sender is a player.
		if(sender instanceof Player) {

			Player player = (Player) sender;

			// Check permissions for the player.
			if (! player.isOp() || ! player.hasPermission("bounties.*")) {

				sendMessage(sender, "<red>ERROR: You do not have permission to use that command!</red>");

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
		else if (sender instanceof ConsoleCommandSender) {

			return handleConsoleCommands(sender, args);

		}
		else {

			sendMessage(sender, "<red>This command can only be run by a player or the console.</red>");

			return true;

		}

	}

	// Sends help message based on sender type.
	private void sendHelpMessage(CommandSender sender) {

		// Check if the sender is a player.
		if (sender instanceof Player) {

			Player player = (Player) sender;
			if (player.hasPermission("bounties.admin")) {

				sendMessage(sender, """
						    <gold>How to use the /bounty command:</gold>
						    <newline><gold>/bounty place <target> <reward></gold> - Place a bounty on a target for a reward.
						    <newline><gold>/bounty edit <target> (optional - placer) <new_reward></gold> - Edit an existing bounty's reward.
						    <newline><gold>/bounty remove <target> (optional - placer)</gold> - Remove an existing bounty.
						    <newline><gold>/bounty clearall</gold> - Clear all bounties.
						    <newline><gray>You can also use /bn instead of /bounty.</gray>
						""");

			}
			else {

				sendMessage(sender, """
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

				sendMessage(player, "<red>Usage: /bounty place <target> <reward></red>");

				return true;

			}

			try {

				Double.parseDouble(args[2]);

			}
			catch (NumberFormatException error) {

				sendMessage(player, "<red>Invalid reward amount. Must be a number.</red>");

				return true;

			}

			placeBounty(player, args[1], args[2]);

			return true;
		case "remove":
			if (args.length < 2) {

				sendMessage(player, "<red>Usage: /bounty remove <target></red>");

				return true;

			}

			cancelBounty(player, args[1], args.length >= 3 ? args[2] : "null");

			return true;
		case "edit":
			if (args.length < 3) {

				sendMessage(player, "<red>Usage: /bounty edit <target> <new_reward></red>");

				return true;

			}

			try {

				Double.parseDouble(args[2]);

			}
			catch (NumberFormatException error) {

				sendMessage(player, "<red>Invalid reward amount. Must be a number.</red>");

				return true;

			}

			editBounty(player, args[1], "null", args[2]);

			return true;
		case "list":
			if (bounties.isEmpty()) {

				sendMessage(player, "<red>No bounties!</red>");

			}
			else {

				for (Bounty bounty : bounties) {

					sendMessage(player, "<gold>BOUNTY: " + bounty.TARGET + " PLACER: " + bounty.SENDER + " REWARD: <red>$" + bounty.REWARD + "</red></gold>");

				}

				sendMessage(player, "<green>Bounties shown.</green>");

			}

			return true;
		case "clearall":
			if (player.hasPermission("bounties.admin")) {

				bounties.clear();

				sendMessage(player, "<green>All bounties cleared.</green>");

			}
			else {

				sendMessage(player, "<red>You don't have permission to clear all bounties.</red>");

			}

			return true;
		default:
			sendMessage(player, "<red>Unrecognized command.</red>");

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

	// Helper function to send messages with MiniMessage for players, plain text for console.
	private void sendMessage(CommandSender sender, String message) {

		if (sender instanceof Player) {

			Player player = (Player) sender;

			Component component = MiniMessage.miniMessage().deserialize(message);

			player.sendMessage(component);

		}
		else {

			// For console, strip MiniMessage tags and send plain text.
			String plainMessage = LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(message));

			sender.sendMessage(plainMessage);

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

					if (withdraw(pSender, reward)) {

						// Place the bounty.
						bounties.add(bounty);

						sendMessage(sender, "<green>Bounty placed on: </green>" + "<yellow>" + target + "<green>.</green>");

						// Notify all players.
						for (Player player : Bukkit.getOnlinePlayers()) {

							sendMessage(player, "<gold>" + pSender.getName() + " has placed a BOUNTY on " + target + " for <red>$" + reward + "</red></gold>");

						}
					}
					else {

						sendMessage(sender, "<red>Bounty not placed: insufficient funds or invalid reward amount.</red>");

					}

				}
				else {

					sendMessage(sender, "<red>You have already placed a bounty on " + target + ".</red>");

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
		else {

			sendMessage(sender, "<red>ERROR: Unknown player.</red>");

		}

	}

	// Cancel a bounty on a target that the sender has placed.
	private void cancelBounty(CommandSender sender, String target, String placer) {

		boolean found = false;
		Bounty toCancel = new Bounty();
		if (isValidTarget(target)) {

			if (sender instanceof Player && placer.equals("null")) {

				// Player is canceling their own bounty.
				for (Bounty bounty : bounties) {

					if (bounty.SENDER.equalsIgnoreCase(sender.getName()) && bounty.TARGET.equalsIgnoreCase(target)) {

						found = true;

						toCancel = bounty;

						Player p = (Player) sender;
						deposit(p, bounty.REWARD);

						sendMessage(sender, "<green>Bounty on " + target + " removed</green>");

						break;

					}

				}

				if (! found) {

					sendMessage(sender, "<red>You have not placed a bounty on " + target + "</red>");

				}

			}
			else {

				// Console or admin canceling a bounty.
				for (Bounty bounty : bounties) {

					if (bounty.SENDER.equalsIgnoreCase(placer) && bounty.TARGET.equalsIgnoreCase(target)) {

						found = true;
						toCancel = bounty;

						sendMessage(sender, "<green>Bounty on " + target + " removed</green>");

						break;

					}

				}

				if (! found) {

					sendMessage(sender, "<red>Bounty on " + target + " not found</red>");

				}

			}

			bounties.remove(toCancel);

		}
		else {

			sendMessage(sender, "<red>Unknown player</red>");

		}

	}

	// Only can change the reward.
	private void editBounty(CommandSender sender, String target, String placer, String reward) {

		boolean found = false;
		if (isValidTarget(target)) {

			if (sender instanceof Player && placer.equals("null")) {

				for (Bounty bounty : bounties) {

					if (bounty.SENDER.equalsIgnoreCase(sender.getName()) && bounty.TARGET.equalsIgnoreCase(target)) {

						found = true;

						Player p = (Player) sender;
						deposit(p, bounty.REWARD);

						if (withdraw(p, reward)) {

							bounty.REWARD = reward;

							sendMessage(sender, "<green>Bounty on " + target + ": reward edited to <red>$" + reward + "</red></green>");

						}
						else {

							sendMessage(sender, "<red>Bounty on " + target + ": unable to edit reward</red>");

							// Revert transaction.
							withdraw(p, bounty.REWARD);

						}

						break;

					}

				}
				if (! found) {

					sendMessage(sender, "<red>You have not placed a bounty on " + target + "</red>");

				}

			}
			else {

				// Admin or console editing the bounty.
				for (Bounty bounty : bounties) {

					if (bounty.SENDER.equalsIgnoreCase(placer) && bounty.TARGET.equalsIgnoreCase(target)) {

						found = true;
						bounty.REWARD = reward;

						sendMessage(sender, "<green>Bounty on " + target + " edited to <red>$" + reward + "</red></green>");

						break;

					}

				}
				if (! found) {

					sendMessage(sender, "<red>Bounty on " + target + " not found</red>");

				}

			}

		}
		else {

			sendMessage(sender, "<red>Unknown player</red>");

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
			deposit(p, amt);

			// Remove the bounty.
			bounties.remove(b);

		}

		// Notify all online players about the completed bounty.
		for (Player player : Bukkit.getOnlinePlayers()) {

			sendMessage(player, "<gold>" + killer + " has completed a bounty on " + killed + " for</gold> <aqua>" + amt + "Diamonds </aqua>");

		}

	}

	public boolean withdraw(Player p, String amt) {

		int amountToWithdraw = parseAmount(p, amt);
		if (amountToWithdraw <= 0) {

			return false;

		}

		DiamondBankAPI diamondBankAPI = getDiamondBankAPI(p, amt);
		if (diamondBankAPI == null) {

			return false;

		}

		CompletableFuture<PostgreSQL.PlayerBalance> asyncPlayerBalance = diamondBankAPI.getPlayerBalance(p.getUniqueId(), PostgreSQL.BalanceType.ALL);
		PostgreSQL.PlayerBalance playerBalance = null;
		try {

			playerBalance = asyncPlayerBalance.get();

		}
		catch (InterruptedException | ExecutionException errors) {

			errors.printStackTrace();

			sendMessage(p, "<red>ERROR: Internal error with DiamondBank-OG. Contact a Developer!</red>");

			return false;

		}

		double balance = playerBalance.getBankBalance() + playerBalance.getEnderChestBalance() + playerBalance.getInventoryBalance();

		// Check if player can afford the withdrawal.
		if (balance < amountToWithdraw) {

			sendMessage(p, "<red>ERROR: Insufficient balance.</red>");

			return false;

		}

		diamondBankAPI.withdrawFromPlayer(p.getUniqueId(), amountToWithdraw);

		main.getLogger().info("Transaction success!");

		sendMessage(p, "<aqua>" + amt + "Diamonds</aqua> <yellow>have been withdrawn. Your new balance is: <aqua>" + balance + "Diamonds</aqua><yellow>.</yellow>");

		return true;

	}

	public void deposit(Player p, String amt) {

		int amountToWithdraw = parseAmount(p, amt);
		if (amountToWithdraw <= 0) {

			return;

		}

		DiamondBankAPI diamondBankAPI = getDiamondBankAPI(p, amt);
		if (diamondBankAPI == null) {

			return;

		}

		diamondBankAPI.addToPlayerBankBalance(p.getUniqueId(), amountToWithdraw);

		CompletableFuture<PostgreSQL.PlayerBalance> asyncPlayerBalance = diamondBankAPI.getPlayerBalance(p.getUniqueId(), PostgreSQL.BalanceType.ALL);
		PostgreSQL.PlayerBalance playerBalance = null;
		try {

			playerBalance = asyncPlayerBalance.get();

		}
		catch (InterruptedException | ExecutionException errors) {

			errors.printStackTrace();

			sendMessage(p, "<red>ERROR: Internal error with DiamondBank-OG. Contact a Developer!</red>");

		}

		double balance = playerBalance.getBankBalance() + playerBalance.getEnderChestBalance() + playerBalance.getInventoryBalance();

		sendMessage(p, "<aqua>" + amt + "Diamonds <yellow>have been deposited. Your new balance is:</yellow> <aqua>" + balance + "Diamonds</aqua><yellow>.</yellow>");

		main.getLogger().info("Transaction success! Player: " + p.getName() + " Amount: " + amt + ".");

	}

	// Common function to parse the amount and handle errors.
	private int parseAmount(Player p, String amt) {

		try {

			return Integer.parseInt(amt);

		}
		catch (Exception error) {

			main.getLogger().info("ERROR: Invalid number of Diamonds specified by: " + p + ". Amount: " + amt + ".");

			sendMessage(p, "<red>ERROR: Invalid number of Diamonds.</red>");

			return -1;

		}

	}

	// Common method to initialize the DiamondBank-OG API and handle errors.
	private DiamondBankAPI getDiamondBankAPI(Player p, String amt) {

		DiamondBankAPI diamondBankAPI = DiamondBankOG.getApi();
		if (diamondBankAPI == null) {

			main.getLogger().info("ERROR: Could not initialize the DiamondBank-OG API! Do you have it installed and configured properly?" + amt);

			sendMessage(p, "<red>ERROR: Could not initialize the DiamondBank-OG API! Do you have it installed and configured properly?</red>");

		}

		return diamondBankAPI;

	}

}
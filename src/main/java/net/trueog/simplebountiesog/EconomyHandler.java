package net.trueog.simplebountiesog;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.trueog.diamondbankog.DiamondBankAPIJava;
import net.trueog.diamondbankog.DiamondBankException;
import net.trueog.diamondbankog.PostgreSQL.PlayerShards;
import net.trueog.diamondbankog.PostgreSQL.ShardType;
import net.trueog.utilitiesog.UtilitiesOG;

public class EconomyHandler {

	private final DiamondBankAPIJava api;

	public EconomyHandler(DiamondBankAPIJava api) {

		this.api = api;

	}

	public void withdraw(Player player, String amt) {

		Bukkit.getScheduler().runTaskAsynchronously(SimpleBountiesOG.getPlugin(), () -> {

			int diamonds;
			try {

				diamonds = Integer.parseInt(amt);

			}
			catch (NumberFormatException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Invalid amount.");
				return;

			}

			int shards = diamonds * 9;

			try {

				api.subtractFromPlayerBankShards(player.getUniqueId(), shards, "Withdraw", "Player withdrawal via bounty plugin.").get();

				UtilitiesOG.trueogMessage(player, "&aWithdrawal successful.");

			}
			catch (InterruptedException | ExecutionException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: An error occurred during withdrawal.");
				error.printStackTrace();

			}
			catch (DiamondBankException.EconomyDisabledException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Economy is disabled.");
				error.printStackTrace();

			}
			catch (DiamondBankException.TransactionsLockedException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Transactions locked. Please try later.");
				error.printStackTrace();

			}
			catch (DiamondBankException.OtherException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: An unexpected error occurred.");
				error.printStackTrace();

			}

		});

	}

	public void deposit(Player player, String amt) {

		Bukkit.getScheduler().runTaskAsynchronously(SimpleBountiesOG.getPlugin(), () -> {

			int diamonds;
			try {

				diamonds = Integer.parseInt(amt);

			}
			catch (NumberFormatException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Invalid amount.");
				return;

			}

			int shards = diamonds * 9;

			try {

				api.addToPlayerBankShards(player.getUniqueId(), shards, "Deposit", "Player deposit via bounty plugin.").get();

				UtilitiesOG.trueogMessage(player, "&aDeposit successful.");

			}
			catch (InterruptedException | ExecutionException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: An error occurred during deposit.");
				error.printStackTrace();

			}
			catch (DiamondBankException.EconomyDisabledException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Economy is disabled.");
				error.printStackTrace();

			}
			catch (DiamondBankException.TransactionsLockedException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Transactions locked. Please try later.");
				error.printStackTrace();

			}
			catch (DiamondBankException.OtherException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: An unexpected error occurred.");
				error.printStackTrace();

			}

		});

	}

	public void balance(Player player) {

		Bukkit.getScheduler().runTaskAsynchronously(SimpleBountiesOG.getPlugin(), () -> {

			CompletableFuture<PlayerShards> future;
			try {

				future = api.getPlayerShards(player.getUniqueId(), ShardType.ALL);

			}
			catch (DiamondBankException.EconomyDisabledException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Economy is disabled.");
				error.printStackTrace();
				return;

			}
			catch (DiamondBankException.TransactionsLockedException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Transactions locked. Please try later.");
				error.printStackTrace();
				return;

			}
			catch (DiamondBankException.OtherException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Something went wrong.");
				error.printStackTrace();
				return;

			}

			try {

				PlayerShards shards = future.get();
				if (shards.getShardsInBank() == null || shards.getShardsInInventory() == null) {

					UtilitiesOG.trueogMessage(player, "&cERROR: No shards found.");
					return;

				}

				int totalShards = shards.getShardsInBank() + shards.getShardsInInventory();
				int diamonds = totalShards / 9;
				int remainingShards = totalShards % 9;

				UtilitiesOG.trueogMessage(player, "&aBalance: " + diamonds + " diamonds and " + remainingShards + " shards.");

			}
			catch (InterruptedException | ExecutionException error) {

				UtilitiesOG.trueogMessage(player, "&cERROR: Error retrieving balance.");

			}

		});

	}

}
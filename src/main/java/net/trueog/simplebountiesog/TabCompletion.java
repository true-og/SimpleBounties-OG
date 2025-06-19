package net.trueog.simplebountiesog;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TabCompletion implements TabCompleter {

    public BountyCommands bountyCommands = null;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {

        List<String> completions = new ArrayList<String>();
        if (cmd.getName().equalsIgnoreCase("bounty") && args.length >= 0) {

            if (sender instanceof Player) {

                Player p = (Player) sender;
                if (args.length <= 1) {

                    completions.clear();
                    completions.add("place");
                    completions.add("help");
                    completions.add("edit");
                    completions.add("remove");
                    completions.add("list");

                    if (p.isOp() || p.hasPermission("bounties.admin")) {

                        completions.add("clearall");
                    }

                } else if (args.length == 2
                        && !args[0].equalsIgnoreCase("list")
                        && !args[0].equalsIgnoreCase("clearall")) {

                    completions.clear();

                    if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("edit")) {

                        bountyCommands = SimpleBountiesOG.getBountyCommands();
                        if (p.hasPermission("bounties.admin") || sender.isOp()) {

                            for (Bounty b : bountyCommands.bounties) {

                                completions.add(b.TARGET);
                            }

                        } else {
                            for (Bounty b : bountyCommands.bounties) {

                                // Servers don't have auto-completion, so I don't need to add "God" here.
                                if (b.SENDER.equalsIgnoreCase(sender.getName())) {

                                    // Only adds players the player has already placed a bounty on, the only ones they
                                    // can edit or remove.
                                    completions.add(b.TARGET);
                                }
                            }
                        }

                    } else {

                        for (Player player : Bukkit.getOnlinePlayers()) {

                            completions.add(player.getName());
                        }
                    }

                } else if (args.length == 3
                        && !args[0].equalsIgnoreCase("list")
                        && !args[0].equalsIgnoreCase("remove")
                        && !args[0].equalsIgnoreCase("clearall")) {

                    completions.clear();
                    completions.add("10");
                    completions.add("100");
                    completions.add("1000");

                } else {

                    completions.clear();
                }

                return completions;
            }
        }

        return null;
    }
}

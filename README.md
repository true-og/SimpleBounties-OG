# SimpleBounties-OG
A simple, straight-forward Spigot plugin for servers using Minecraft 1.19 to allow players to place and complete bounties on each other. Maintained by [TrueOG Network](https://true-og.net).

# Dependencies
EssentialsX and [DiamondBank-OG](https://github.com:true-og/DiamondBank-OG).

# Building

Run

```./gradlew build```

The output .jar will be under build/libs/

# Permissions
bounties.* - Allows basic access to the plugin. 

bounties.admin - Allows operator permissions, such as the abilities to edit and remove other people's bounties. When an operator does that, it won't send a refund to the original bounty placer. Also allows the use of the /bounty clearall command, which removes all active bounties. Operators also have these permissions.

# Aliases
/bn is equivalent to /bounty

# Bounty Rules
You cannot place bounties as another person, and you cannot claim bounties on yourself. You cannot place multiple bounties on a person, and you have to be directly responsible for the kill. As of right now, you can only enter Diamonds as a reward. 

# Guide

### For Regular Users
To place a bounty do /bounty place [target player] [reward amount].

To edit a bounty do /bounty edit [bounty target] [new reward amount].

To remove a bounty do /bounty remove [bounty target].

To list all current bounties do /bounty list.

Do /bounty help for instructions on how to use the plugin.

### For Those With bounties.admin Permissions
Operators can use the plugin the same way regular users can, or they can utilize a couple more options.

They can do /bounty clearall to remove all active bounties.

They can do /bounty edit [bounty target] [bounty placer] [new reward] to change the reward of another player's bounty without refunding the original bounty placers.

They can do /bounty remove [bounty target] [bounty placer] to remove another player's bounty without refunding the original bounty placers.

Enjoy!

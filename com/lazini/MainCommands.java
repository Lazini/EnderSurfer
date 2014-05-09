package com.lazini;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MainCommands implements CommandExecutor {
	EnderSurfer plugin = EnderSurfer.getInstance();
	int health = plugin.getConfig().getInt("half-hearts");
	String strHealth = Integer.toString(health);
	int velMult = plugin.getConfig().getInt("vel-mult");
	String strVelMult = Integer.toString(velMult);
	boolean dmgOnAir = plugin.getConfig().getBoolean("dmg-on-air");
	String strDmgOnAir = Boolean.toString(dmgOnAir);

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("sethearts")) {
			if (args.length == 0) {
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.GREEN
						+ strHealth
						+ " half hearts will be lost when someone hits the ground!");
				return true;
			} else if (args.length > 1) {
				sender.sendMessage(ChatColor.RED
						+ "Too many arguments, don't you think?");
				return false;
			} else {
				plugin.reloadConfig();
				health = Integer.parseInt(args[0]);
				plugin.getConfig().set("half-hearts", health);
				strHealth = Integer.toString(health);
				plugin.saveConfig();
				sender.sendMessage(ChatColor.GREEN
						+ " Configuration file updated!" + ChatColor.GOLD
						+ " (half-hearts: " + strHealth + ")");
				return true;
			}
		} else if (command.getName().equalsIgnoreCase("setVelocityMultiplier")) {
			if (args.length == 0) {
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.GREEN
						+ "The current velocity multiplier is " + strVelMult);
				return true;
			} else if (args.length > 1) {
				sender.sendMessage(ChatColor.RED
						+ "Too many arguments, don't you think?");
				return false;
			} else {
				plugin.reloadConfig();
				velMult = Integer.parseInt(args[0]);
				plugin.getConfig().set("vel-mult", velMult);
				strVelMult = Integer.toString(velMult);
				plugin.saveConfig();
				sender.sendMessage(ChatColor.GREEN
						+ " Configuration file updated!" + ChatColor.GOLD
						+ " (vel-mult: " + strVelMult + ")");
				return true;
			}
		} else if (command.getName().equalsIgnoreCase("setDamageOnAir")) {
			if (args.length == 0) {
				plugin.reloadConfig();
				sender.sendMessage(ChatColor.GREEN
						+ "Currently damaging the player while in the air is set to "
						+ strDmgOnAir);
				return true;
			} else if (args.length > 1) {
				sender.sendMessage(ChatColor.RED
						+ "Too many arguments, don't you think?");
				return false;
			} else {
				plugin.reloadConfig();
				dmgOnAir = Boolean.parseBoolean(args[0]);
				plugin.getConfig().set("dmg-on-air", dmgOnAir);
				strDmgOnAir = Boolean.toString(dmgOnAir);
				plugin.saveConfig();
				sender.sendMessage(ChatColor.GREEN
						+ " Configuration file updated!" + ChatColor.GOLD
						+ " (dmg-on-air: " + strDmgOnAir + ")");
				return true;
			}
		}

		return false;
	}

}

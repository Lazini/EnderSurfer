package com.lazini;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class EnderSurfer extends JavaPlugin implements Listener {

	EnderPearl enderpearl;
	Player shooter;
	UUID shooterUUID;

	int health = getConfig().getInt("half-hearts");
	String strHealth = Integer.toString(health);
	int velMult = getConfig().getInt("vel-mult");
	String strVelMult = Integer.toString(velMult);
	boolean dmgOnAir = getConfig().getBoolean("dmg-on-air");
	String strDmgOnAir = Boolean.toString(dmgOnAir);

	ArrayList<UUID> list = new ArrayList<UUID>(100);

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	@Override
	public void onDisable() {
		super.onDisable();
		HandlerList.unregisterAll();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (command.getName().equalsIgnoreCase("sethearts")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.GREEN
						+ strHealth
						+ " half hearts will be lost when someone hits the ground!");
				return true;
			} else if (args.length > 1) {
				sender.sendMessage(ChatColor.RED
						+ "Too many arguments, don't you think?");
				return false;
			} else {
				reloadConfig();
				health = Integer.parseInt(args[0]);
				getConfig().set("half-hearts", health);
				strHealth = Integer.toString(health);
				saveConfig();
				sender.sendMessage(ChatColor.GREEN
						+ " Configuration file updated!" + ChatColor.GOLD
						+ " (half-hearts: " + strHealth + ")");
				return true;
			}
		} else if (command.getName().equalsIgnoreCase("setVelocityMultiplier")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.GREEN
						+ "The current velocity multiplier is " + strVelMult);
				return true;
			} else if (args.length > 1) {
				sender.sendMessage(ChatColor.RED
						+ "Too many arguments, don't you think?");
				return false;
			} else {
				reloadConfig();
				velMult = Integer.parseInt(args[0]);
				getConfig().set("vel-mult", velMult);
				strVelMult = Integer.toString(velMult);
				saveConfig();
				sender.sendMessage(ChatColor.GREEN
						+ " Configuration file updated!" + ChatColor.GOLD
						+ " (vel-mult: " + strVelMult + ")");
				return true;
			}
		} else if (command.getName().equalsIgnoreCase("setDamageOnAir")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.GREEN
						+ "Currently damaging the player while in the air is set to "
						+ strDmgOnAir);
				return true;
			} else if (args.length > 1) {
				sender.sendMessage(ChatColor.RED
						+ "Too many arguments, don't you think?");
				return false;
			} else {
				reloadConfig();
				dmgOnAir = Boolean.parseBoolean(args[0]);
				getConfig().set("dmg-on-air", dmgOnAir);
				strDmgOnAir = Boolean.toString(dmgOnAir);
				saveConfig();
				sender.sendMessage(ChatColor.GREEN
						+ " Configuration file updated!" + ChatColor.GOLD
						+ " (dmg-on-air: " + strDmgOnAir + ")");
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onThrowEnderPearl(ProjectileLaunchEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player)
				|| !(event.getEntity() instanceof EnderPearl))
			return;
		shooter = (Player) event.getEntity().getShooter();
		if (shooter.isSneaking() == true)
			return;
		Vector velocity = event.getEntity().getVelocity();

		if (!shooter.isOnGround() && dmgOnAir) {
			shooter.setHealth(shooter.getHealth() - health);
		}

		velocity = velocity.multiply(velMult);
		shooter.setVelocity(velocity);
		event.getEntity().remove();
		shooterUUID = shooter.getUniqueId();

		if (!(list.contains(shooterUUID))) {
			list.add(shooterUUID);
		}
	}

	@EventHandler
	public void onGetDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)
				|| event.getEntity() != shooter)
			return;
		if (event.getCause() == DamageCause.FALL && list.contains(shooterUUID)) {
			shooter.setHealth(shooter.getHealth() - health);
			list.remove(shooterUUID);
			event.setCancelled(true);
		}
	}
}

package com.lazini;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

	private static EnderSurfer instance;
	int health = getConfig().getInt("half-hearts");
	int velMult = getConfig().getInt("vel-mult");
	boolean dmgOnAir = getConfig().getBoolean("dmg-on-air");

	public Map<UUID, Boolean> list = new HashMap<>();

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		instance = this;
		getCommand("setHearts").setExecutor(new MainCommands());
		getCommand("setVelocityMultiplier").setExecutor(new MainCommands());
		getCommand("setDamageOnAir").setExecutor(new MainCommands());
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
		health = getConfig().getInt("half-hearts");
		velMult = getConfig().getInt("vel-mult");
		dmgOnAir = getConfig().getBoolean("dmg-on-air");
		Vector velocity = event.getEntity().getVelocity();

		if (!shooter.isOnGround() && dmgOnAir)
			shooter.setHealth(shooter.getHealth() - health);

		velocity = velocity.multiply(velMult);
		shooter.setVelocity(velocity);
		event.getEntity().remove();
		shooterUUID = shooter.getUniqueId();

		if (!list.containsKey(shooterUUID) || !list.get(shooterUUID))
			list.put(shooterUUID, true);

	}

	@EventHandler
	public void onGetDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)
				|| event.getEntity() != shooter)
			return;
		health = getConfig().getInt("half-hearts");
		dmgOnAir = getConfig().getBoolean("dmg-on-air");
		if (event.getCause() == DamageCause.FALL
				&& list.containsKey(shooterUUID)) {
			if (!dmgOnAir)
				shooter.setHealth(shooter.getHealth() - health);
			if (list.get(shooterUUID))
				list.put(shooterUUID, false);

			event.setCancelled(true);
		}
	}

	public static EnderSurfer getInstance() {
		return instance;
	}
}

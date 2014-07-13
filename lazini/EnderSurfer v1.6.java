package lazini;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
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
	public Map<UUID, Boolean> threwE = new HashMap<>();

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
				|| !(event.getEntity() instanceof EnderPearl)
				|| !((Player) event.getEntity().getShooter())
						.hasPermission("throw"))
			return;
		shooter = (Player) event.getEntity().getShooter();
		if (shooter.isSneaking())
			return;

		health = getConfig().getInt("half-hearts");
		velMult = getConfig().getInt("vel-mult");
		dmgOnAir = getConfig().getBoolean("dmg-on-air");
		Vector velocity = event.getEntity().getVelocity();

		if (dmgOnAir)
			shooter.damage(health);

		if (shooter.isOnGround()) {
			Location loc = shooter.getLocation();
			loc.setY(loc.getY() + .1);
			shooter.teleport(loc);
		}

		velocity = velocity.multiply(velMult);
		shooter.setVelocity(velocity);
		event.getEntity().remove();
		shooterUUID = shooter.getUniqueId();
		threwE.put(shooterUUID, false);
		list.put(shooterUUID, false);

		if (dmgOnAir)
			list.put(shooterUUID, true);
		else
			threwE.put(shooterUUID, true);

	}

	@EventHandler
	public void onGetDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)
				|| event.getEntity() != shooter
				|| !((Player) event.getEntity())
						.hasPermission("defy-fall-damage"))
			return;
		shooter = (Player) event.getEntity();
		shooterUUID = shooter.getUniqueId();
		health = getConfig().getInt("half-hearts");
		dmgOnAir = getConfig().getBoolean("dmg-on-air");

		if (event.getCause() == DamageCause.FALL
				&& (list.containsKey(shooterUUID) || threwE
						.containsKey(shooterUUID))) {
			if (list.get(shooterUUID)) {
				list.put(shooterUUID, false);
				event.setCancelled(true);
			} else if (threwE.get(shooterUUID)) {
				shooter.damage(health);
				threwE.put(shooterUUID, false);
				event.setCancelled(true);
			}
		}
	}

	public static EnderSurfer getInstance() {
		return instance;
	}
}

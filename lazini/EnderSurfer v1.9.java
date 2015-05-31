package lazini;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class EnderSurfer extends JavaPlugin implements Listener {
	// Variables
	private static EnderSurfer instance;
	int health;
	int velMult;
	boolean dmgOnAir;
	boolean showParticles;
	private Random random;
	// Sets go here.
	public HashSet<UUID> list;
	public HashSet<UUID> threwE;
	public HashSet<UUID> particles;

	@Override
	public void onEnable() {
		super.onEnable();
		getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		instance = this;
		Commands cmds = new Commands(); // Memory saver
		getCommand("endersurfer").setExecutor(cmds);
		getCommand("es").setExecutor(cmds);
		health = getConfig().getInt("half-hearts");
		velMult = getConfig().getInt("vel-mult");
		dmgOnAir = getConfig().getBoolean("dmg-on-air");
		showParticles = getConfig().getBoolean("show-particles");
		random = new Random();
		list = new HashSet<>();
		threwE = new HashSet<>();
		particles = new HashSet<>();
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onThrowEnderPearl(ProjectileLaunchEvent event) {
		if (!(event.getEntity().getShooter() instanceof Player)
				|| !(event.getEntity() instanceof EnderPearl)
				|| !((Player) event.getEntity().getShooter())
						.hasPermission("endersurfer.utilize")) {
			return;
		}
		Player shooter = (Player) event.getEntity().getShooter();
		UUID shooterUUID;

		if (shooter.isSneaking())
			return;
		if (this.getConfig().contains("disabled-for-worlds") && this.getConfig().isList("disabled-for-worlds")) { 
			List<String> disabledWorlds = this.getConfig().getStringList("disabled-for-worlds");
			for (String str : disabledWorlds) { 
				if (shooter.getWorld().getName().equalsIgnoreCase(str)) {
					return;
				}
			}

		}

		if (shooter.isOnGround()) {
			Location endermiteLoc = event.getEntity().getLocation();
			if (random.nextInt(100) < 5) {
				event.getEntity().getWorld()
						.spawnEntity(endermiteLoc, EntityType.ENDERMITE);
			}
		}

		this.reloadConfig();
		health = getConfig().getInt("half-hearts");
		velMult = getConfig().getInt("vel-mult");
		dmgOnAir = getConfig().getBoolean("dmg-on-air");
		showParticles = getConfig().getBoolean("show-particles");
		Vector velocity = event.getEntity().getVelocity();

		if (shooter.isOnGround() && (shooter.getLocation().getPitch() < 15)) {
			Location loc = shooter.getLocation();
			loc.setY(loc.getY() + .2);
			shooter.teleport(loc);
		}

		velocity = velocity.multiply(velMult);
		shooter.setVelocity(velocity);
		event.getEntity().remove();
		shooterUUID = shooter.getUniqueId();

		if (dmgOnAir) {
			if (!shooter.hasPermission("endersurfer.defydamageonair"))
				handlePlayerDamage(shooter, health);
			if (!list.contains(shooterUUID))
				list.add(shooterUUID);
		} else {
			if (!threwE.contains(shooterUUID))
				threwE.add(shooterUUID);
		}

		if (!particles.contains(shooterUUID)
				&& (showParticles && shooter
						.hasPermission("endersurfer.showparticles"))) {
			particles.add(shooterUUID);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onGetDamage(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player)
				|| !((Player) event.getEntity())
						.hasPermission("endersurfer.utilize"))
			return;
		Player shooter = (Player) event.getEntity();
		UUID shooterUUID = shooter.getUniqueId();

		health = getConfig().getInt("half-hearts");
		if (event.getCause() == DamageCause.FALL) {
			if (list.contains(shooterUUID)) {
				list.remove(shooterUUID);
				event.setCancelled(true);
			}

			if (threwE.contains(shooterUUID)) {
				if (!shooter.hasPermission("endersurfer.defyfalldamage"))
					handlePlayerDamage(shooter, health);
				threwE.remove(shooterUUID);
				event.setCancelled(true);
			}
			if (particles.contains(shooterUUID)) {
				particles.remove(shooterUUID);
				if (shooter.isOnGround()) {
					Location endermiteLoc = event.getEntity().getLocation();
					if (random.nextInt(100) < 5) {
						event.getEntity()
								.getWorld()
								.spawnEntity(endermiteLoc, EntityType.ENDERMITE);

					}
				}
			}
		}
	}

	/** Handles particles */
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerFlyWithEnderPearl(PlayerMoveEvent event) {
		if (!(event.getPlayer() instanceof Player)
				|| !(event.getPlayer()).hasPermission("endersurfer.utilize"))
			return;
		Player shooter;
		UUID shooterUUID;
		shooter = event.getPlayer();
		shooterUUID = shooter.getUniqueId();
		reloadConfig();
		showParticles = getConfig().getBoolean("show-particles");

		if (particles.contains(shooterUUID)) {
			if (shooter.isOnGround()) {
				Vector tempVel = shooter.getVelocity().normalize();
				if (tempVel.getX() < 0.1 && tempVel.getX() > -0.1) {
					if (tempVel.getZ() < 0.1 && tempVel.getZ() > -0.1) {
						// if (tempVel.getY())
						if (shooter.getLocation().getPitch() > -10
								&& shooter.getLocation().getPitch() <= 90) {
							particles.remove(shooterUUID);
							return;
						}
					}
				}
			}
			if (shooter.hasPermission("endersurfer.showparticles")
					&& showParticles) {
				showEnderParticles(shooter);
			}
		}
	}

	public static EnderSurfer getInstance() {
		return instance;
	}

	private void handlePlayerDamage(Player shooter, double health) {
		if (health == 0)
			return;
		shooter.damage(health);
	}

	private void showEnderParticles(Player shooter) {
		Location loc = shooter.getLocation();
		loc.setY(loc.getY() + 1);
		ParticleEffect.PORTAL.display(.5f, .5f, .5f, .5f, 20, loc, 40);
	}
}

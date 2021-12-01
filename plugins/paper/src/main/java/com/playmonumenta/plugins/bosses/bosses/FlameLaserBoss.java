package com.playmonumenta.plugins.bosses.bosses;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.bosses.SpellManager;
import com.playmonumenta.plugins.bosses.spells.SpellBaseLaser;
import com.playmonumenta.plugins.utils.BossUtils;

/**
 * @deprecated use boss_laser instead, like this:
 * <blockquote><pre>
 * /bos var Tags add boss_laser
 * /bos var Tags add boss_laser[damage=19,cooldown=160,singletarget=true,effects=[(fire,80)]]
 * /bos var Tags add boss_laser[soundTicks=[(UI_TOAST_IN,0.5)],soundEnd=[(ENTITY_DRAGON_FIREBALL_EXPLODE,1,1.5)]]
 * /bos var Tags add boss_laser[ParticleLaser=[(CLOUD,1,0.02,0.02,0.02,0),(FLAME,1,0.04,0.04,0.04,1)],ParticleEnd=[(FIREWORKS_SPARK,300,0.8,0.8,0.8,0)]]
 * </pre></blockquote>
 * @author G3m1n1Boy
 */
public class FlameLaserBoss extends BossAbilityGroup {
	public static final String identityTag = "boss_flamelaser";

	public static class Parameters extends BossParameters {
		public int DAMAGE = 19;
		public int DELAY = 100;
		public int DETECTION = 30;
		public int COOLDOWN = 8 * 20;
		public int FUSE_TIME = 5 * 20;
		public int FIRE_DURATION = 4 * 20;
		public boolean SINGLE_TARGET = true;
	}

	public static BossAbilityGroup deserialize(Plugin plugin, LivingEntity boss) throws Exception {
		return new FlameLaserBoss(plugin, boss);
	}

	public FlameLaserBoss(Plugin plugin, LivingEntity boss) {
		super(plugin, identityTag, boss);

		Parameters p = BossParameters.getParameters(boss, identityTag, new Parameters());

		SpellManager activeSpells = new SpellManager(Arrays.asList(
			new SpellBaseLaser(plugin, boss, p.DETECTION, p.FUSE_TIME, false, p.SINGLE_TARGET, p.COOLDOWN,
					// Tick action per player
					(LivingEntity target, int ticks, boolean blocked) -> {
						target.getWorld().playSound(target.getLocation(), Sound.UI_TOAST_IN, 0.5f, 0.5f + (ticks / 80f) * 1.5f);
						boss.getLocation().getWorld().playSound(boss.getLocation(), Sound.UI_TOAST_IN, 1f, 0.5f + (ticks / 80f) * 1.5f);
						if (ticks == 0) {
							boss.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 110, 4));
						}
					},
					// Particles generated by the laser
					(Location loc) -> {
						loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.02, 0.02, 0.02, 0);
						loc.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0.04, 0.04, 0.04, 1);
					},
					// Damage generated at the end of the attack
					(LivingEntity target, Location loc, boolean blocked) -> {
						loc.getWorld().playSound(loc, Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 1f, 1.5f);
						loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 300, 0.8, 0.8, 0.8, 0);
						if (!blocked) {
							BossUtils.bossDamage(boss, target, p.DAMAGE);
							// Shields don't stop fire!
							target.setFireTicks(p.FIRE_DURATION);
						}
					})
		));

		super.constructBoss(activeSpells, null, p.DETECTION, null, p.DELAY);
	}
}

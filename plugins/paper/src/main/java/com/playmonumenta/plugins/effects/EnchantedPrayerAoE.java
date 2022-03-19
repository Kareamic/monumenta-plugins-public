package com.playmonumenta.plugins.effects;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.particle.PartialParticle;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import java.util.EnumSet;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class EnchantedPrayerAoE extends Effect {

	private final Plugin mPlugin;
	private final int mDamageAmount;
	private final double mHealAmount;
	private final Player mPlayer;
	private final EnumSet<DamageType> mAffectedDamageTypes;

	public EnchantedPrayerAoE(Plugin plugin, int duration, int damageAmount, double healAmount, Player player, EnumSet<DamageType> affectedDamageTypes) {
		super(duration);
		mPlugin = plugin;
		mDamageAmount = damageAmount;
		mHealAmount = healAmount;
		mPlayer = player;
		mAffectedDamageTypes = affectedDamageTypes;
	}

	// This needs to trigger after any percent damage
	@Override
	public EffectPriority getPriority() {
		return EffectPriority.LATE;
	}

	@Override
	public double getMagnitude() {
		return Math.abs(mDamageAmount);
	}

	@Override
	public void onDamage(LivingEntity entity, DamageEvent event, LivingEntity enemy) {
		if (mAffectedDamageTypes == null || mAffectedDamageTypes.contains(event.getType())) {
			World world = entity.getWorld();
			world.playSound(enemy.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.9f);
			world.playSound(enemy.getLocation(), Sound.ENTITY_BLAZE_DEATH, 1, 1.75f);
			new PartialParticle(Particle.SPELL_INSTANT, enemy.getLocation().add(0, enemy.getHeight() / 2, 0), 100, 0.25f, 0.3f, 0.25f, 1).spawnAsPlayerActive(mPlayer);
			new PartialParticle(Particle.FIREWORKS_SPARK, enemy.getLocation().add(0, enemy.getHeight() / 2, 0), 75, 0, 0, 0, 0.3).spawnAsPlayerActive(mPlayer);
			for (LivingEntity le : EntityUtils.getNearbyMobs(enemy.getLocation(), 3.5)) {
				DamageUtils.damage(mPlayer, le, DamageType.MAGIC, mDamageAmount, ClassAbility.ENCHANTED_PRAYER, true, true);
			}
			double maxHealth = EntityUtils.getMaxHealth(mPlayer);
			PlayerUtils.healPlayer(mPlugin, mPlayer, maxHealth * mHealAmount, mPlayer);
			setDuration(0);
		}
	}

	@Override
	public void entityTickEffect(Entity entity, boolean fourHertz, boolean twoHertz, boolean oneHertz) {
		Player p = (Player) entity;
		if (fourHertz) {
			Location rightHand = PlayerUtils.getRightSide(p.getEyeLocation(), 0.45).subtract(0, .8, 0);
			Location leftHand = PlayerUtils.getRightSide(p.getEyeLocation(), -0.45).subtract(0, .8, 0);
			new PartialParticle(Particle.SPELL_INSTANT, leftHand, 2, 0.05f, 0.05f, 0.05f, 0).spawnAsPlayerActive(p);
			new PartialParticle(Particle.SPELL_INSTANT, rightHand, 2, 0.05f, 0.05f, 0.05f, 0).spawnAsPlayerActive(p);
		}
	}

	@Override
	public String toString() {
		return String.format("EnchantedPrayerAoE duration:%d player:%s amount:%d", this.getDuration(), mPlayer.getName(), mDamageAmount);
	}
}

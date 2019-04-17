package com.playmonumenta.plugins.abilities.alchemist.harbinger;

import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.utils.EntityUtils;

/*
 * When you kill an enemy they give off a noxious cloud, dealing 4/8 damage and
 * 6/12s of Weakness to all targets within 5 blocks. Mobs dying from this cloud
 * can also trigger Purple Haze.
 */

public class PurpleHaze extends Ability {
	private static final int PURPLE_HAZE_1_DAMAGE = 4;
	private static final int PURPLE_HAZE_2_DAMAGE = 8;
	private static final int PURPLE_HAZE_1_WEAKNESS_DURATION = 6 * 20;
	private static final int PURPLE_HAZE_2_WEAKNESS_DURATION = 12 * 20;
	private static final double PURPLE_HAZE_RADIUS = 5;

	public PurpleHaze(Plugin plugin, World world, Random random, Player player) {
		super(plugin, world, random, player);
		mInfo.scoreboardId = "PurpleHaze";
	}

	@Override
	public void EntityDeathEvent(EntityDeathEvent event, boolean shouldGenDrops) {
		mWorld.spawnParticle(Particle.SPELL_WITCH, event.getEntity().getLocation(), 150, PURPLE_HAZE_RADIUS, PURPLE_HAZE_RADIUS, PURPLE_HAZE_RADIUS, 0.5f); //Rudimentary effects
		mWorld.spawnParticle(Particle.SPELL_MOB, event.getEntity().getLocation(), 150, PURPLE_HAZE_RADIUS, PURPLE_HAZE_RADIUS, PURPLE_HAZE_RADIUS, 0f); //Rudimentary effects
		int purpleHaze = getAbilityScore();
		int damage = purpleHaze == 1 ? PURPLE_HAZE_1_DAMAGE : PURPLE_HAZE_2_DAMAGE;
		int duration = purpleHaze == 1 ? PURPLE_HAZE_1_WEAKNESS_DURATION : PURPLE_HAZE_2_WEAKNESS_DURATION;
		for (LivingEntity mob : EntityUtils.getNearbyMobs(event.getEntity().getLocation(), PURPLE_HAZE_RADIUS)) {
			EntityUtils.damageEntity(mPlugin, mob, damage, mPlayer);
			mob.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 0, true, false));
		}
	}
}

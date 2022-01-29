package com.playmonumenta.plugins.abilities.rogue;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.classes.ClassAbility;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.potion.PotionManager.PotionID;
import com.playmonumenta.plugins.utils.DamageUtils;
import com.playmonumenta.plugins.utils.EntityUtils;
import com.playmonumenta.plugins.utils.InventoryUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;



public class ByMyBlade extends Ability {

	private static final int BY_MY_BLADE_1_HASTE_AMPLIFIER = 1;
	private static final int BY_MY_BLADE_2_HASTE_AMPLIFIER = 3;
	private static final int BY_MY_BLADE_HASTE_DURATION = 4 * 20;
	private static final int BY_MY_BLADE_1_DAMAGE = 10;
	private static final int BY_MY_BLADE_2_DAMAGE = 20;
	private static final int BY_MY_BLADE_COOLDOWN = 10 * 20;

	private final int mDamageBonus;

	public ByMyBlade(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "By My Blade");
		mInfo.mLinkedSpell = ClassAbility.BY_MY_BLADE;
		mInfo.mScoreboardId = "ByMyBlade";
		mInfo.mShorthandName = "BmB";
		mInfo.mDescriptions.add("While holding two swords, attacking an enemy with a critical attack deals an extra 10 melee damage to that enemy, and grants you Haste 2 for 4s. Cooldown: 10s.");
		mInfo.mDescriptions.add("Damage is increased from 10 to 20. Haste level is increased from 2 to 4.");
		mInfo.mCooldown = BY_MY_BLADE_COOLDOWN;
		mDisplayItem = new ItemStack(Material.SKELETON_SKULL, 1);
		mDamageBonus = getAbilityScore() == 1 ? BY_MY_BLADE_1_DAMAGE : BY_MY_BLADE_2_DAMAGE;
	}

	@Override
	public void onDamage(DamageEvent event, LivingEntity enemy) {
		if (event.getType() == DamageType.MELEE || event.getType() == DamageType.MELEE_ENCH || event.getType() == DamageType.MELEE_SKILL) {
			int hasteAmplifier = getAbilityScore() == 1 ? BY_MY_BLADE_1_HASTE_AMPLIFIER : BY_MY_BLADE_2_HASTE_AMPLIFIER;
			double extraDamage = mDamageBonus;

			mPlugin.mPotionManager.addPotion(mPlayer, PotionID.ABILITY_SELF,
			                                 new PotionEffect(PotionEffectType.FAST_DIGGING, BY_MY_BLADE_HASTE_DURATION, hasteAmplifier, false, true));

			if (EntityUtils.isElite(enemy)) {
				extraDamage *= RoguePassive.PASSIVE_DAMAGE_ELITE_MODIFIER;
			} else if (EntityUtils.isBoss(enemy)) {
				extraDamage *= RoguePassive.PASSIVE_DAMAGE_BOSS_MODIFIER;
			}

			DamageUtils.damage(mPlayer, enemy, DamageType.MELEE_SKILL, extraDamage, mInfo.mLinkedSpell, true);

			Location loc = enemy.getLocation();
			World world = mPlayer.getWorld();
			loc.add(0, 1, 0);
			int count = 15;
			if (getAbilityScore() > 1) {
				world.spawnParticle(Particle.SPELL_WITCH, loc, 45, 0.2, 0.65, 0.2, 1.0);
				count = 30;
			}
			world.spawnParticle(Particle.SPELL_MOB, loc, count, 0.25, 0.5, 0.5, 0.001);
			world.spawnParticle(Particle.CRIT, loc, 30, 0.25, 0.5, 0.5, 0.001);
			world.playSound(loc, Sound.ITEM_SHIELD_BREAK, 2.0f, 0.5f);
			putOnCooldown();
		}
	}

	@Override
	public boolean runCheck() {
		return mPlayer != null && PlayerUtils.isFallingAttack(mPlayer) && InventoryUtils.rogueTriggerCheck(mPlugin, mPlayer);
	}

}

package com.playmonumenta.plugins.abilities.cleric;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.PlayerUtils;
import javax.annotation.Nullable;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;

public class NonClericProvisionsPassive extends Ability {

	public NonClericProvisionsPassive(Plugin plugin, @Nullable Player player) {
		super(plugin, player, null);
	}

	@Override
	public boolean canUse(Player player) {
		return true;
	}

	public static boolean testRandomChance(Player player) {
		double bestChance = 0;

		for (Player p : PlayerUtils.playersInRange(player.getLocation(), 100, true)) {
			SacredProvisions provisions = AbilityManager.getManager().getPlayerAbility(p, SacredProvisions.class);
			if (provisions != null && provisions.isInRange(player)) {
				double chance = provisions.getChance();
				if (chance > bestChance) {
					bestChance = chance;
				}
			}
		}

		return bestChance > 0 && FastUtils.RANDOM.nextDouble() < bestChance;
	}

	public static boolean testEnhanced(Player player) {
		for (Player p : PlayerUtils.playersInRange(player.getLocation(), 100, true)) {
			SacredProvisions provisions = AbilityManager.getManager().getPlayerAbility(p, SacredProvisions.class);
			if (provisions != null && provisions.isEnhanced() && provisions.isInRange(player)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void playerItemConsumeEvent(PlayerItemConsumeEvent event) {
		if (mPlayer != null && testRandomChance(mPlayer)) {
			event.setReplacement(event.getItem());
			sacredProvisionsSound(mPlayer);
		}
	}

	@Override
	public void playerItemDamageEvent(PlayerItemDamageEvent event) {
		if (mPlayer != null && testRandomChance(mPlayer)) {
			event.setDamage(0);
		}
	}

	@Override
	public boolean playerShotArrowEvent(AbstractArrow arrow) {
		if (mPlayer != null && testRandomChance(mPlayer)) {
			AbilityUtils.refundArrow(mPlayer, arrow);
		}
		return true;
	}

	@Override
	public boolean playerThrewSplashPotionEvent(ThrownPotion potion) {
		if (mPlayer != null && testRandomChance(mPlayer)) {
			if (AbilityUtils.refundPotion(mPlayer, potion)) {
				sacredProvisionsSound(mPlayer);
			}
		}
		return true;
	}

	@Override
	public boolean playerThrewLingeringPotionEvent(ThrownPotion potion) {
		if (mPlayer != null && testRandomChance(mPlayer)) {
			if (AbilityUtils.refundPotion(mPlayer, potion)) {
				sacredProvisionsSound(mPlayer);
			}
		}
		return true;
	}

	public static void sacredProvisionsSound(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 0.65f, 2f);
	}

	@Override
	public void playerRegainHealthEvent(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player player) {
			if (player.getFoodLevel() >= SacredProvisions.ENHANCEMENT_HEALING_HUNGER_REQUIREMENT && testEnhanced(player)) {
				event.setAmount(event.getAmount() * (1 + SacredProvisions.ENHANCEMENT_HEALING_BONUS));
			}
		}
	}
}

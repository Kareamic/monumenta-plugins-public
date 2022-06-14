package com.playmonumenta.plugins.abilities.scout;

import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.abilities.Ability;
import com.playmonumenta.plugins.abilities.AbilityManager;
import com.playmonumenta.plugins.abilities.AbilityWithChargesOrStacks;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.events.DamageEvent.DamageType;
import com.playmonumenta.plugins.itemstats.abilities.CharmManager;
import com.playmonumenta.plugins.network.ClientModHandler;
import com.playmonumenta.plugins.utils.AbilityUtils;
import com.playmonumenta.plugins.utils.FastUtils;
import com.playmonumenta.plugins.utils.MessagingUtils;
import javax.annotation.Nullable;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

public class Sharpshooter extends Ability implements AbilityWithChargesOrStacks {
	private static final double PERCENT_BASE_DAMAGE = 0.2;
	private static final int SHARPSHOOTER_DECAY_TIMER = 20 * 4;
	private static final int MAX_STACKS = 8;
	private static final double PERCENT_DAMAGE_PER_STACK = 0.035;
	private static final double DAMAGE_PER_BLOCK = 0.02;
	private static final double MAX_DISTANCE = 16;
	private static final double ARROW_SAVE_CHANCE = 0.2;

	public static final String CHARM_STACK_DAMAGE = "Sharpshooter Stack Damage";
	public static final String CHARM_STACKS = "Sharpshooter Max Stacks";
	public static final String CHARM_RETRIEVAL = "Sharpshooter Retrieval Chance";
	public static final String CHARM_DECAY = "Sharpshooter Stack Decay Time";

	private final int mMaxStacks;
	private final int mDecayTime;

	public Sharpshooter(Plugin plugin, @Nullable Player player) {
		super(plugin, player, "Sharpshooter");
		mInfo.mScoreboardId = "Sharpshooter";
		mInfo.mShorthandName = "Ss";
		mInfo.mDescriptions.add("Your arrows deal 20% more damage.");
		mInfo.mDescriptions.add("Each enemy hit with a critical arrow or trident gives you a stack of Sharpshooter, up to 8. Stacks decay after 4 seconds of not gaining a stack. Each stack makes your arrows and tridents deal +3.5% damage. Additionally, passively gain a 20% chance to not consume arrows when shot.");
		mInfo.mDescriptions.add("Your arrows and tridents deal an extra 2% per block of distance between you and the target, up to 16 blocks.");
		mDisplayItem = new ItemStack(Material.TARGET, 1);

		mMaxStacks = MAX_STACKS + (int) CharmManager.getLevel(mPlayer, CHARM_STACKS);
		mDecayTime = SHARPSHOOTER_DECAY_TIMER + CharmManager.getExtraDuration(mPlayer, CHARM_DECAY);
	}

	private int mStacks = 0;
	private int mTicksToStackDecay = 0;

	@Override
	public boolean onDamage(DamageEvent event, LivingEntity enemy) {
		if (event.getType() == DamageType.PROJECTILE && event.getDamager() instanceof AbstractArrow arrow) {

			event.setDamage(event.getDamage() * (1 + PERCENT_BASE_DAMAGE + mStacks * (PERCENT_DAMAGE_PER_STACK + CharmManager.getLevelPercentDecimal(mPlayer, CHARM_STACK_DAMAGE))));
			if (isEnhanced()) {
				event.setDamage(event.getDamage() * (1 + Math.min(enemy.getLocation().distance(mPlayer.getLocation()), MAX_DISTANCE) * DAMAGE_PER_BLOCK));
			}

			// Critical arrow and mob is actually going to take damage
			if (isLevelTwo() && (arrow.isCritical() || arrow instanceof Trident) && (enemy.getNoDamageTicks() <= enemy.getMaximumNoDamageTicks() / 2f || enemy.getLastDamage() < event.getDamage())) {
				mTicksToStackDecay = mDecayTime;

				if (mStacks < mMaxStacks) {
					mStacks++;
					MessagingUtils.sendActionBarMessage(mPlayer, "Sharpshooter Stacks: " + mStacks);
					ClientModHandler.updateAbility(mPlayer, this);
				}
			}
		}
		return false; // only changes event damage
	}

	@Override
	public void periodicTrigger(boolean twoHertz, boolean oneSecond, int ticks) {
		if (mPlayer == null) {
			return;
		}
		if (mStacks > 0) {
			mTicksToStackDecay -= 5;

			if (mTicksToStackDecay <= 0) {
				mTicksToStackDecay = mDecayTime;
				mStacks--;
				MessagingUtils.sendActionBarMessage(mPlayer, "Sharpshooter Stacks: " + mStacks);
				ClientModHandler.updateAbility(mPlayer, this);
			}
		}
	}

	@Override
	public boolean playerShotArrowEvent(AbstractArrow arrow) {
		if (getAbilityScore() > 1 && mPlayer != null && FastUtils.RANDOM.nextDouble() < ARROW_SAVE_CHANCE + CharmManager.getLevelPercentDecimal(mPlayer, CHARM_RETRIEVAL)) {
			boolean refunded = AbilityUtils.refundArrow(mPlayer, arrow);
			if (refunded) {
				mPlayer.playSound(mPlayer.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.3f, 1.0f);
			}
		}
		return true;
	}

	public static void addStacks(Player player, int stacks) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		if (ss != null) {
			ss.mStacks = Math.min(MAX_STACKS + (int) CharmManager.getLevel(player, CHARM_STACKS), ss.mStacks + stacks);
			MessagingUtils.sendActionBarMessage(player, "Sharpshooter Stacks: " + ss.mStacks);
			ClientModHandler.updateAbility(player, ss);
		}
	}

	public static double getDamageMultiplier(Player player) {
		Sharpshooter ss = AbilityManager.getManager().getPlayerAbility(player, Sharpshooter.class);
		return ss == null ? 1 : (1 + PERCENT_BASE_DAMAGE + ss.mStacks * PERCENT_DAMAGE_PER_STACK);
	}

	@Override
	public int getCharges() {
		return mStacks;
	}

	@Override
	public int getMaxCharges() {
		return mMaxStacks;
	}

}

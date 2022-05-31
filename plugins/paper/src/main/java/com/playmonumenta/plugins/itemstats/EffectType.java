package com.playmonumenta.plugins.itemstats;

import com.playmonumenta.plugins.CustomLogger;
import com.playmonumenta.plugins.Plugin;
import com.playmonumenta.plugins.effects.AbilityCooldownDecrease;
import com.playmonumenta.plugins.effects.AbilityCooldownIncrease;
import com.playmonumenta.plugins.effects.ArrowSaving;
import com.playmonumenta.plugins.effects.Bleed;
import com.playmonumenta.plugins.effects.BonusSoulThreads;
import com.playmonumenta.plugins.effects.BoonOfThePit;
import com.playmonumenta.plugins.effects.CrystalineBlessing;
import com.playmonumenta.plugins.effects.DeepGodsEndowment;
import com.playmonumenta.plugins.effects.DurabilitySaving;
import com.playmonumenta.plugins.effects.NegateDamage;
import com.playmonumenta.plugins.effects.PercentAttackSpeed;
import com.playmonumenta.plugins.effects.PercentDamageDealt;
import com.playmonumenta.plugins.effects.PercentDamageReceived;
import com.playmonumenta.plugins.effects.PercentExperience;
import com.playmonumenta.plugins.effects.PercentHeal;
import com.playmonumenta.plugins.effects.PercentKnockbackResist;
import com.playmonumenta.plugins.effects.PercentSpeed;
import com.playmonumenta.plugins.effects.SilverPrayer;
import com.playmonumenta.plugins.effects.StarCommunion;
import com.playmonumenta.plugins.effects.Stasis;
import com.playmonumenta.plugins.effects.TuathanBlessing;
import com.playmonumenta.plugins.events.DamageEvent;
import com.playmonumenta.plugins.utils.ItemStatUtils;
import com.playmonumenta.plugins.utils.PotionUtils;
import java.util.EnumSet;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public enum EffectType {
	//when ever a new EffectType get added here remember to add it also in the switch inside applyEffect(...)

	// type: is the unique key save inside the nbt of the item
	// name: is the name that the player will see on the item -> format:  +/-dd% name (x:yy)
	// isPositive: if the display should be blue (positive) or red (negative)
	// isFlat: not a percentage (true) or percentage (false)
	// isConstant: does it have a number associated?
	// isVanilla: is it a vanilla effect?
	VANILLA_SPEED("VanillaSpeed", "Speed", true, true, false, true),
	VANILLA_SLOW("VanillaSlow", "Slowness", false, true, false, false),
	VANILLA_HASTE("Haste", "Haste", true, true, false, true),
	VANILLA_FATIGUE("MiningFatigue", "Mining Fatigue", false, true, false, true),
	VANILLA_JUMP("JumpBoost", "Jump Boost", true, true, false, true),
	VANILLA_FIRE_RESISTANCE("VanillaFireRes", "Fire Immunity", true, true, false, true),
	VANILLA_WATER_BREATH("WaterBreath", "Water Breathing", true, true, false, true),
	VANILLA_BLINDNESS("Blindness", "Blindness", false, true, false, true),
	VANILLA_NIGHT_VISION("NightVision", "Night Vision", true, true, false, true),
	VANILLA_POISON("Poison", "Poison", false, true, false, true),
	VANILLA_WITHER("Wither", "Wither", false, true, false, true),
	VANILLA_REGEN("Regeneration", "Regeneration", true, true, false, true),
	VANILLA_HEALTH_BOOST("HealthBoost", "Health Boost", true, true, false, true),
	VANILLA_HEAL("InstantHealth", "Instant Health", true, true, false, true),
	VANILLA_DAMAGE("InstantDamage", "Instant Damage", false, true, false, true),
	VANILLA_ABSORPTION("Absorption", "Absorption", true, true, false, true),
	VANILLA_SATURATION("Saturation", "Saturation", true, true, false, true),
	VANILLA_GLOW("Glowing", "Glowing", true, true, false, true),
	VANILLA_SLOWFALL("SlowFalling", "Slow Falling", true, true, false, true),
	VANILLA_CONDUIT("ConduitPower", "ConduitPower", true, true, false, true),

	SPEED("Speed", "Speed", true, false, false, false),
	SLOW("Slow", "Speed", false, false, false, false),

	ATTACK_SPEED("AttackSpeed", "Attack Speed", true, false, false, false),
	NEGATIVE_ATTACK_SPEED("NegativeAttackSpeed", "Attack Speed", false, false, false, false),

	KNOCKBACK_RESIST("KnockbackResist", "Knockback Resistance", true, false, false, false),
	NEGATIVE_KNOCKBACK_RESIST("NegativeKnockbackResist", "Knockback Resistance", false, false, false, false),

	//Resistance type of effects
	RESISTANCE("Resistance", "Resistance", true, false, false, false),
	MELEE_RESISTANCE("MeleeResistance", "Melee Resistance", true, false, false, false),
	PROJECTILE_RESISTANCE("ProjectileResistance", "Projectile Resistance", true, false, false, false),
	MAGIC_RESISTANCE("MagicResistance", "Magic Resistance", true, false, false, false),
	BLAST_RESISTANCE("BlastResistance", "Blast Resistance", true, false, false, false),
	FIRE_RESISTANCE("FireResistance", "Fire Resistance", true, false, false, false),
	FALL_RESISTANCE("FallResistance", "Fall Resistance", true, false, false, false),

	//Damage Negation
	DAMAGE_NEGATE("DamageNegate", "Hits Blocked", true, true, false, false),
	MELEE_DAMAGE_NEGATE("MeleeDamageNegate", "Melee Hits Blocked", true, true, false, false),
	PROJECTILE_DAMAGE_NEGATE("ProjectileDamageNegate", "Projectile Hits Blocked", true, true, false, false),
	MAGIC_DAMAGE_NEGATE("MagicDamageNegate", "Magic Hits Blocked", true, true, false, false),
	BLAST_DAMAGE_NEGATE("BlastDamageNegate", "Blast Hits Blocked", true, true, false, false),
	FIRE_DAMAGE_NEGATE("FireDamageNegate", "Fire Hits Blocked", true, true, false, false),
	FALL_DAMAGE_NEGATE("FallDamageNegate", "Falling Hits Blocked", true, true, false, false),

	//Vulnerability type of effects
	VULNERABILITY("Vulnerability", "Resistance", false, false, false, false),
	MELEE_VULNERABILITY("MeleeVulnerability", "Melee Resistance", false, false, false, false),
	PROJECTILE_VULNERABILITY("ProjectileVulnerability", "Projectile Resistance", false, false, false, false),
	MAGIC_VULNERABILITY("MagicVulnerability", "Magic Resistance", false, false, false, false),
	BLAST_VULNERABILITY("BlastVulnerability", "Blast Resistance", false, false, false, false),
	FIRE_VULNERABILITY("FireVulnerability", "Fire Resistance", false, false, false, false),
	FALL_VULNERABILITY("FallVulnerability", "Fall Resistance", false, false, false, false),

	//Damage type of effects
	DAMAGE("damage", "Strength", true, false, false, false),
	MAGIC_DAMAGE("MagicDamage", "Magic Damage", true, false, false, false),
	MELEE_DAMAGE("MeleeDamage", "Melee Damage", true, false, false, false),
	PROJECTILE_DAMAGE("ProjectileDamage", "Projectile Damage", true, false, false, false),

	//Weakness type of effects
	WEAKNESS("Weakness", "Weakness", false, false, false, false),
	MAGIC_WEAKNESS("MagicWeakness", "Magic Damage", false, false, false, false),
	MELEE_WEAKNESS("MeleeWeakness", "Melee Damage", false, false, false, false),
	PROJECTILE_WEAKNESS("ProjectileWeakness", "Projectile Damage", false, false, false, false),

	HEAL("Heal", "Healing", true, false, false, false),
	ANTI_HEAL("AntiHeal", "Healing", false, false, false, false),

	ARROW_SAVING("ArrowSaving", "Arrow Save Chance", true, false, false, false),
	ARROW_LOSS("ArrowSaving", "Arrow Save Chance", false, false, false, false),

	SOUL_THREAD_BONUS("SoulThreadBonus", "Soul Thread Chance", true, false, false, false),
	SOUL_THREAD_REDUCTION("SoulThreadReduction", "Soul Thread Chance", false, false, false, false),

	DURABILITY_SAVE("DurabilitySave", "Durability", true, false, false, false),
	DURABILITY_LOSS("DurabilityLoss", "Durability", false, false, false, false),

	EXP_BONUS("ExpBonus", "Experience", true, false, false, false),
	EXP_LOSS("ExpLoss", "Experience", false, false, false, false),

	COOLDOWN_DECREASE("AbilityCooldownDecrease", "Cooldown Reduction", true, false, false, false),
	COOLDOWN_INCREASE("AbilityCooldownIncrease", "Cooldown Reduction", false, false, false, false),

	BLEED("Bleed", "Bleed", false, false, false, false),

	STASIS("Stasis", "Stasis", true, false, true, false),

	BOON_OF_THE_PIT("BoonOfThePit", "Boon of the Pit", true, false, true, false),
	BOON_OF_SILVER_SCALES("BoonOfSilverScales", "Boon of Silver Scales", true, false, true, false),
	CRYSTALLINE_BLESSING("CrystallineBlessing", "Crystalline Blessing", true, false, true, false),
	CURSE_OF_THE_DARK_SOUL("DarkSoul", "Curse of the Dark Soul", false, false, true, false),
	DEEP_GODS_ENDOWMENT("DeepGodsEndowment", "Deep God's Endowment", true, false, true, false),
	SILVER_PRAYER("SilverPrayer", "Silver Prayer", true, false, true, false),
	STAR_COMMUNION("StarCommunion", "Star Communion", true, false, true, false),
	TUATHAN_BLESSING("TuathanBlessing", "Tuathan Blessing", true, false, true, false);

	public static final String KEY = "Effects";

	private final String mType;
	private final String mName;
	private final Boolean mIsPositive;
	private final Boolean mIsFlat;
	private final Boolean mIsConstant;
	private final Boolean mIsVanilla;

	EffectType(String type, String name, Boolean isPositive, Boolean isFlat, Boolean isConstant, Boolean isVanilla) {
		mType = type;
		mName = name;
		mIsPositive = isPositive;
		mIsFlat = isFlat;
		mIsConstant = isConstant;
		mIsVanilla = isVanilla;
	}

	public String getType() {
		return mType;
	}

	public Boolean isPositive() {
		return mIsPositive;
	}

	public Boolean isFlat() {
		return mIsFlat;
	}

	public Boolean isConstant() {
		return mIsConstant;
	}

	public Boolean isVanilla() {
		return mIsVanilla;
	}

	public static EffectType fromType(String type) {
		for (EffectType effectType : values()) {
			if (effectType.mType.equals(type)) {
				return effectType;
			}
		}
		return null;
	}

	public static Component getComponent(@Nullable EffectType effectType, double strength, int duration) {
		if (effectType == null) {
			return Component.empty();
		}

		int minutes = duration / 1200;
		int seconds = (duration / 20) % 60;
		String timeString = "(" + minutes + ":" + (seconds > 9 ? seconds : "0" + seconds) + ")";
		if (minutes > 999) {
			timeString = "(∞)";
		}
		String color;
		String add;

		if (effectType.isPositive()) {
			color = "#4AC2E5";
			add = "+";
		} else {
			color = "#D02E28";
			add = "-";
		}

		if (effectType.getType().contains("Cooldown")) {
			if (effectType.isPositive()) {
				add = "-";
			} else {
				add = "+";
			}
		}

		if (effectType.isVanilla()) {
			if (effectType.getType().contains("Instant")) {
				return Component.text(effectType.mName + " " + ItemStatUtils.toRomanNumerals((int) strength), TextColor.fromHexString(color)).decoration(TextDecoration.ITALIC, false);
			}
			return Component.text(effectType.mName + " " + ItemStatUtils.toRomanNumerals((int) strength) + " ", TextColor.fromHexString(color)).decoration(TextDecoration.ITALIC, false).append(
				Component.text(timeString, TextColor.fromHexString("#555555")).decoration(TextDecoration.ITALIC, false)
			);
		}
		if (effectType.isConstant()) {
			return Component.text(effectType.mName + " ", TextColor.fromHexString(color)).decoration(TextDecoration.ITALIC, false).append(
				Component.text(timeString, TextColor.fromHexString("#555555")).decoration(TextDecoration.ITALIC, false)
			);
		}
		if (effectType.isFlat()) {
			return Component.text(add + ((int) strength) + " " + effectType.mName + " ", TextColor.fromHexString(color)).decoration(TextDecoration.ITALIC, false).append(
				Component.text(timeString, TextColor.fromHexString("#555555")).decoration(TextDecoration.ITALIC, false)
			);
		}
		return Component.text(add + (int) (strength * 100) + "% " + effectType.mName + " ", TextColor.fromHexString(color)).decoration(TextDecoration.ITALIC, false).append(
			Component.text(timeString, TextColor.fromHexString("#555555")).decoration(TextDecoration.ITALIC, false)
		);
	}

	public static void applyEffect(@Nullable EffectType effectType, Entity entity, int duration, double strength, @Nullable String source) {
		if (effectType == null) {
			return;
		}

		String sourceString = (source != null ? effectType.mName + source : effectType.mName);

		switch (effectType) {
			case VANILLA_SPEED -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.SPEED, duration, (int) (strength - 1), true));
			case VANILLA_SLOW -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.SLOW, duration, (int) (strength - 1), true));
			case VANILLA_FATIGUE -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.SLOW_DIGGING, duration, (int) (strength - 1), true));
			case VANILLA_JUMP -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.FAST_DIGGING, duration, (int) (strength - 1), true));
			case VANILLA_FIRE_RESISTANCE -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, (int) (strength - 1), true));
			case VANILLA_WATER_BREATH -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.WATER_BREATHING, duration, (int) (strength - 1), true));
			case VANILLA_BLINDNESS -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.BLINDNESS, duration, (int) (strength - 1), true));
			case VANILLA_NIGHT_VISION -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.NIGHT_VISION, duration, (int) (strength - 1), true));
			case VANILLA_POISON -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.POISON, duration, (int) (strength - 1), true));
			case VANILLA_WITHER -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.WITHER, duration, (int) (strength - 1), true));
			case VANILLA_REGEN -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.REGENERATION, duration, (int) (strength - 1), true));
			case VANILLA_HEALTH_BOOST -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.HEALTH_BOOST, duration, (int) (strength - 1), true));
			case VANILLA_HEAL -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.HEAL, 1, (int) (strength - 1), true));
			case VANILLA_DAMAGE -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.HARM, 1, (int) (strength - 1), true));
			case VANILLA_ABSORPTION -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.ABSORPTION, duration, (int) (strength - 1), true));
			case VANILLA_SATURATION -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.SATURATION, duration, (int) (strength - 1), true));
			case VANILLA_GLOW -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.GLOWING, duration, (int) (strength - 1), true));
			case VANILLA_SLOWFALL -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.SLOW_FALLING, duration, (int) (strength - 1), true));
			case VANILLA_CONDUIT -> PotionUtils.applyPotion(Plugin.getInstance(), (Player) entity, new PotionEffect(PotionEffectType.CONDUIT_POWER, duration, (int) (strength - 1), true));

			case SPEED -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentSpeed(duration, strength, sourceString));
			case SLOW -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentSpeed(duration, -strength, sourceString));

			case ATTACK_SPEED -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentAttackSpeed(duration, strength, sourceString));
			case NEGATIVE_ATTACK_SPEED -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentAttackSpeed(duration, -strength, sourceString));

			case KNOCKBACK_RESIST -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentKnockbackResist(duration, strength, sourceString));
			case NEGATIVE_KNOCKBACK_RESIST -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentKnockbackResist(duration, -strength, sourceString));

			case RESISTANCE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, -strength));
			case MELEE_RESISTANCE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, -strength, EnumSet.of(DamageEvent.DamageType.MELEE)));
			case PROJECTILE_RESISTANCE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, -strength, EnumSet.of(DamageEvent.DamageType.PROJECTILE)));
			case MAGIC_RESISTANCE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, -strength, EnumSet.of(DamageEvent.DamageType.MAGIC)));
			case BLAST_RESISTANCE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, -strength, EnumSet.of(DamageEvent.DamageType.BLAST)));
			case FIRE_RESISTANCE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, -strength, EnumSet.of(DamageEvent.DamageType.FIRE)));
			case FALL_RESISTANCE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, -strength, EnumSet.of(DamageEvent.DamageType.FALL)));

			case DAMAGE_NEGATE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new NegateDamage(duration, (int) strength));
			case MELEE_DAMAGE_NEGATE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new NegateDamage(duration, (int) strength, EnumSet.of(DamageEvent.DamageType.MELEE)));
			case PROJECTILE_DAMAGE_NEGATE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new NegateDamage(duration, (int) strength, EnumSet.of(DamageEvent.DamageType.PROJECTILE)));
			case MAGIC_DAMAGE_NEGATE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new NegateDamage(duration, (int) strength, EnumSet.of(DamageEvent.DamageType.MAGIC)));
			case BLAST_DAMAGE_NEGATE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new NegateDamage(duration, (int) strength, EnumSet.of(DamageEvent.DamageType.BLAST)));
			case FIRE_DAMAGE_NEGATE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new NegateDamage(duration, (int) strength, EnumSet.of(DamageEvent.DamageType.FIRE)));
			case FALL_DAMAGE_NEGATE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new NegateDamage(duration, (int) strength, EnumSet.of(DamageEvent.DamageType.FALL)));

			case VULNERABILITY -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, strength));
			case MELEE_VULNERABILITY -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, strength, EnumSet.of(DamageEvent.DamageType.MELEE)));
			case PROJECTILE_VULNERABILITY -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, strength, EnumSet.of(DamageEvent.DamageType.PROJECTILE)));
			case MAGIC_VULNERABILITY -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, strength, EnumSet.of(DamageEvent.DamageType.MAGIC)));
			case BLAST_VULNERABILITY -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, strength, EnumSet.of(DamageEvent.DamageType.BLAST)));
			case FIRE_VULNERABILITY -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, strength, EnumSet.of(DamageEvent.DamageType.FIRE)));
			case FALL_VULNERABILITY -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, strength, EnumSet.of(DamageEvent.DamageType.FALL)));

			case DAMAGE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, strength));
			case PROJECTILE_DAMAGE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, strength, EnumSet.of(DamageEvent.DamageType.PROJECTILE)));
			case MAGIC_DAMAGE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, strength, EnumSet.of(DamageEvent.DamageType.MAGIC)));
			case MELEE_DAMAGE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, strength, EnumSet.of(DamageEvent.DamageType.MELEE)));

			case WEAKNESS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, -strength));
			case PROJECTILE_WEAKNESS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, -strength, EnumSet.of(DamageEvent.DamageType.PROJECTILE)));
			case MAGIC_WEAKNESS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, -strength, EnumSet.of(DamageEvent.DamageType.MAGIC)));
			case MELEE_WEAKNESS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageDealt(duration, -strength, EnumSet.of(DamageEvent.DamageType.MELEE)));

			case HEAL -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentHeal(duration, strength));
			case ANTI_HEAL -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentHeal(duration, -strength));

			case ARROW_SAVING -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new ArrowSaving(duration, strength));
			case ARROW_LOSS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new ArrowSaving(duration, -strength));

			case SOUL_THREAD_BONUS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new BonusSoulThreads(duration, strength));
			case SOUL_THREAD_REDUCTION -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new BonusSoulThreads(duration, -strength));

			case DURABILITY_SAVE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new DurabilitySaving(duration, strength));
			case DURABILITY_LOSS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new DurabilitySaving(duration, -strength));

			case COOLDOWN_DECREASE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new AbilityCooldownDecrease(duration, strength));
			case COOLDOWN_INCREASE -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new AbilityCooldownIncrease(duration, strength));

			case EXP_BONUS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentExperience(duration, strength));
			case EXP_LOSS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentExperience(duration, -strength));

			case BLEED -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new Bleed(duration, strength, Plugin.getInstance()));

			case STASIS -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new Stasis(duration));

			case BOON_OF_THE_PIT -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new BoonOfThePit(duration));
			case BOON_OF_SILVER_SCALES -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new AbilityCooldownDecrease(duration, 0.05));
			case CRYSTALLINE_BLESSING -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new CrystalineBlessing(duration));
			case CURSE_OF_THE_DARK_SOUL -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new PercentDamageReceived(duration, 1));
			case DEEP_GODS_ENDOWMENT -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new DeepGodsEndowment(duration));
			case SILVER_PRAYER -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new SilverPrayer(duration));
			case STAR_COMMUNION -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new StarCommunion(duration));
			case TUATHAN_BLESSING -> Plugin.getInstance().mEffectManager.addEffect(entity, sourceString, new TuathanBlessing(duration));

			default -> CustomLogger.getInstance().warning("No EffectType implemented in applyEffect(..) for: " + effectType.mType);

		}
	}
}

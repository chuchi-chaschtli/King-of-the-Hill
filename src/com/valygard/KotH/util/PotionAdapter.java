/**
 * PotionAdapter.java is a part of King of the Hill. 
 */
package com.valygard.KotH.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import com.valygard.KotH.messenger.KotHLogger;

/**
 * Matches all creative inventory potions to their attributes. Each potion is
 * unique in its type, its level and its duration. Used for reading config
 * files, which is designed to be "kind" towards different user input for potion
 * building.
 * 
 * @author Anand
 * 
 */
public enum PotionAdapter {
	// normal potions
	EMPTY(PotionType.UNCRAFTABLE, "empty", "uncraftable"),
	WATER(PotionType.WATER, "water"),
	MUNDANE(PotionType.MUNDANE, "mundane", "normal"),
	THICK(PotionType.THICK, "thick"),
	AWKWARD(PotionType.AWKWARD, "awkward"),
	NIGHT_VISION(PotionType.NIGHT_VISION, "night-vision", "vision", "night"),
	INVISIBILITY(PotionType.INVISIBILITY, "invisibility"),
	LEAPING(PotionType.JUMP, "jump", "leaping"),
	SWIFTNESS(PotionType.SPEED, "speed", "swift", "swiftness"),
	FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, "fire-resistance", "fire-resist"),
	SLOWNESS(PotionType.SLOWNESS, "slow", "slowness"),
	WATER_BREATHING(PotionType.WATER_BREATHING, "water-breathing"),
	INSTANT_HEALTH(PotionType.INSTANT_HEAL, "heal", "instant-heal", "instant-health", "health"),
	HARMING(PotionType.INSTANT_DAMAGE, "instant-damage", "damage", "harming"),
	POISON(PotionType.POISON, "poison"),
	REGEN(PotionType.REGEN, "regen", "regeneration"),
	STRENGTH(PotionType.STRENGTH, "strength", "str"),
	WEAKNESS(PotionType.WEAKNESS, "weakness", "weak"),
	LUCK(PotionType.LUCK, "luck", "fortune"),

	// extended potions
	LONG_NIGHT_VISION(PotionType.NIGHT_VISION, true, false, "long-night-vision", "long-vision", "long-night"),
	LONG_INVISIBILITY(PotionType.INVISIBILITY, true, false, "long-invisibility"),
	LONG_LEAPING(PotionType.JUMP, true, false, "long-jump", "long-leaping"),
	LONG_SWIFTNESS(PotionType.SPEED, true, false, "long-speed", "long-swift", "long-swiftness"),
	LONG_FIRE_RESISTANCE(PotionType.FIRE_RESISTANCE, true, false, "long-fire-resist", "long-fire-resistance"),
	LONG_SLOWNESS(PotionType.SLOWNESS, true, false, "long-slow", "long-slowness"),
	LONG_WATER_BREATHING(PotionType.WATER_BREATHING, true, false, "long-water-breathing"),
	LONG_POISON(PotionType.POISON, true, false, "long-poison"),
	LONG_REGEN(PotionType.REGEN, true, false, "long-regeneration", "long-regen"),
	LONG_STRENGTH(PotionType.STRENGTH, true, false, "long-strength", "long-str"),
	LONG_WEAKNESS(PotionType.WEAKNESS, true, false, "long-weakness", "long-weak"),
	LONG_LUCK(PotionType.LUCK, true, false, "long-luck", "long-fortune"),

	// upgraded potions, mutually exclusive from extended potions
	STRONG_LEAPING(PotionType.JUMP, false, true, "strong-jump", "strong-leaping"),
	STRONG_SWIFTNESS(PotionType.SPEED, false, true, "strong-speed", "strong-swift", "strong-swiftness"),
	STRONG_INSTANT_HEALTH(PotionType.INSTANT_HEAL, false, true, "strong-instant-heal", "strong-instant-health", "strong-heal", "strong-health"),
	STRONG_HARMING(PotionType.INSTANT_DAMAGE, false, true, "strong-instant-damage", "strong-damage", "strong-harming"),
	STRONG_POISON(PotionType.POISON, false, true, "strong-poison"),
	STRONG_REGEN(PotionType.REGEN, false, true, "strong-regeneration", "strong-regen"),
	STRONG_STRENGTH(PotionType.STRENGTH, false, true, "strong-strength", "strong-str");

	// attributes
	private PotionType type;
	private boolean extended, upgraded;
	private List<String> handles;

	/**
	 * Constructor for default potions assumes the potion is neither extended
	 * nor upgraded.
	 * 
	 * @param type
	 *            the PotionType
	 * @param handles
	 *            the String identifiers
	 * @see #PotionMatcher(PotionType, boolean, boolean, String...)
	 */
	PotionAdapter(PotionType type, String... handles) {
		this(type, false, false, handles);
	}

	/**
	 * Constructor for potions requires input for duration and level.
	 * 
	 * @param type
	 *            the PotionType
	 * @param extended
	 *            boolean flag; whether or not potion is extended
	 * @param upgraded
	 *            boolean flag; whether or not potion is upgraded
	 * @param handles
	 *            the String identifiers
	 */
	PotionAdapter(PotionType type, boolean extended, boolean upgraded,
			String... handles) {
		this.type = type;
		this.extended = extended;
		this.upgraded = upgraded;
		this.handles = Arrays.asList(handles);
	}

	/**
	 * Grabs the {@code type} value
	 * 
	 * @return a PotionType enum value
	 */
	PotionType getType() {
		return type;
	}

	/**
	 * Grabs the {@code extended} value
	 * 
	 * @return a boolean flag, true if the potion is extended
	 */
	boolean isExtended() {
		return extended;
	}

	/**
	 * Grabs the {@code upgraded} value
	 * 
	 * @return a boolean flag, true if the potion is upgraded
	 */
	boolean isUpgraded() {
		return upgraded;
	}

	/**
	 * Grabs the String identifiers for the potion. Any one of these values is
	 * usable to detect a different type of potion
	 * 
	 * @return a String list, all of a PotionAdapter's identifiers
	 */
	List<String> getIdentifiers() {
		return Collections.unmodifiableList(handles);
	}

	/**
	 * Grabs a PotionAdapter enumeration from a given PotionData. Iterates
	 * through all values to determine if {@code type}, {@code extended} and
	 * {@code upgraded} are all equivalent and returns the corresponding
	 * Potionmatcher value.
	 * 
	 * @param data
	 *            the PotionData to analyze.
	 * @return a PotionAdapter value.
	 */
	public static PotionAdapter matchData(PotionData data) {
		for (PotionAdapter pm : PotionAdapter.values()) {
			if (pm.getType() == data.getType()
					&& pm.isExtended() == data.isExtended()
					&& pm.isUpgraded() == data.isUpgraded())
				return pm;
		}
		// error here would mean there is missing PotionData values
		KotHLogger.getLogger().error();
		return WATER;
	}

	/**
	 * Grabs a PotionAdapter value from a String identifier. Iterates through
	 * all values to determine if the given {@code handle} is a valid
	 * identifier. If not, a Water Bottle is created in its place.
	 * 
	 * @param handle
	 *            the String identifier to parse
	 * @return a PotionAdapter value
	 */
	public static PotionAdapter matchHandle(String handle) {
		for (PotionAdapter pm : PotionAdapter.values()) {
			if (pm.getIdentifiers().contains(
					handle.toLowerCase().replace("_", "-")))
				return pm;
		}
		KotHLogger.getLogger().warn(
				handle + " could not be parsed as a potion!"
						+ " Water Bottle created ...");
		return WATER;
	}

	/**
	 * Builds a new PotionData object from {@code type}, {@code extended} and
	 * {@code upgraded}
	 * 
	 * @return a PotionData object
	 */
	public PotionData buildPotionData() {
		return new PotionData(type, extended, upgraded);
	}
}

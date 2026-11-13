package com.housebuilder;

/** Every builder block: its registry ID, name, family and size tier (0 = smallest). */
public enum HouseStyle {
	WOODEN_SMALL("wooden_house_small", Kind.WOODEN, 0),
	WOODEN_MEDIUM("wooden_house_builder", Kind.WOODEN, 1), // legacy ID kept for the original wooden block
	WOODEN_LARGE("wooden_house_large", Kind.WOODEN, 2),
	WOODEN_VILLA("wooden_house_villa", Kind.WOODEN, 3),

	MODERN_SMALL("modern_house_small", Kind.MODERN, 0),
	MODERN_MEDIUM("modern_house_builder", Kind.MODERN, 1), // legacy ID kept for the original modern block
	MODERN_LARGE("modern_house_large", Kind.MODERN, 2),
	MODERN_VILLA("modern_house_villa", Kind.MODERN, 3),

	MILITARY("military_barracks_builder", Kind.MILITARY, 0),

	CASTLE_SMALL("castle_small", Kind.CASTLE, 0),
	CASTLE_MEDIUM("castle_medium", Kind.CASTLE, 1),
	CASTLE_LARGE("castle_large", Kind.CASTLE, 2),
	CASTLE_GRAND("castle_grand", Kind.CASTLE, 3);

	public enum Kind { WOODEN, MODERN, MILITARY, CASTLE }

	private final String blockId;
	private final Kind kind;
	private final int tier;

	HouseStyle(String blockId, Kind kind, int tier) {
		this.blockId = blockId;
		this.kind = kind;
		this.tier = tier;
	}

	public String blockId() { return blockId; }
	public Kind kind() { return kind; }
	public int tier() { return tier; }
}

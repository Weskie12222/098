package com.housebuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * Entry point. Each style family has its own builder; all of them use the same
 * local coordinates (u = sideways, f = forward, y = up) with the builder block at 0,0,0.
 */
public final class HouseGenerator {
	private HouseGenerator() {}

	public static void build(Level level, BlockPos origin, Direction forward, HouseStyle style) {
		Ctx c = new Ctx(level, origin, forward);
		switch (style.kind()) {
			case WOODEN -> ResidentialBuilder.build(c, false, style.tier());
			case MODERN -> ResidentialBuilder.build(c, true, style.tier());
			case MILITARY -> BarracksBuilder.build(c);
			case CASTLE -> CastleBuilder.build(c, style.tier());
		}
		c.settleAll();
	}
}

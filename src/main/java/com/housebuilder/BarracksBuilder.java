package com.housebuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Haunted blackstone fortress, 15 wide x 13 deep: skull stakes, cobwebs, soul lanterns,
 * blood-stained floors, a barracks hall, prison cells and a grim armory.
 * Footprint matches the old barracks (u -7..7, f 0..12).
 */
final class BarracksBuilder {
	private BarracksBuilder() {}

	private static String wall(Ctx c) {
		int r = c.rnd.nextInt(10);
		return r < 6 ? "polished_blackstone_bricks" : r < 8 ? "cracked_polished_blackstone_bricks" : "blackstone";
	}

	private static String floor(Ctx c) {
		int r = c.rnd.nextInt(20);
		if (r < 11) return "polished_blackstone";
		if (r < 15) return "cracked_polished_blackstone_bricks";
		if (r < 17) return "nether_wart_block";   // dried blood
		if (r < 19) return "soul_soil";
		return "blackstone";
	}

	private static void hangingSoul(Ctx c, int u, int f, int y) {
		c.place(u, f, y, Ctx.with(Ctx.state("soul_lantern"), BlockStateProperties.HANGING, true));
	}

	private static void skullStake(Ctx c, int u, int f) {
		c.fillId(u, u, f, f, 0, 2, "nether_brick_fence");
		c.put(u, f, 3, c.rnd.nextBoolean() ? "wither_skeleton_skull" : "skeleton_skull");
	}

	static void build(Ctx c) {
		Direction back = c.back();
		Direction left = c.left();

		// Clear, foundation, bloody floor, and a dark road to the gate.
		c.fillId(-9, 9, -6, 14, 0, 14, "air");
		c.foundation(-7, 7, 0, 12, Ctx.state("blackstone"));
		for (int u = -7; u <= 7; u++) {
			for (int f = 0; f <= 12; f++) c.put(u, f, -1, floor(c));
		}
		c.fillId(-1, 1, -5, -1, -1, -1, "polished_blackstone_bricks");

		// Outer walls with a blackstone plinth.
		for (int u = -7; u <= 7; u++) {
			for (int f = 0; f <= 12; f++) {
				if (!(Math.abs(u) == 7 || f == 0 || f == 12)) continue;
				c.put(u, f, 0, "blackstone");
				for (int y = 1; y <= 4; y++) c.put(u, f, y, wall(c));
			}
		}

		// Roof and jagged battlements (every cell, uneven heights = broken teeth).
		c.fillId(-7, 7, 0, 12, 5, 5, "deepslate_tiles");
		for (int u = -7; u <= 7; u++) {
			for (int f = 0; f <= 12; f++) {
				boolean edge = Math.abs(u) == 7 || f == 0 || f == 12;
				if (!edge || Math.floorMod(u + f, 2) != 0) continue;
				c.put(u, f, 6, "polished_blackstone_bricks");
				if (c.rnd.nextInt(3) == 0) c.put(u, f, 7, "polished_blackstone_brick_wall");
			}
		}

		// Four watchtowers topped with skulls and soul fire.
		for (int su : new int[] {-1, 1}) {
			for (int tf : new int[] {0, 10}) {
				int uMin = su < 0 ? -7 : 5, uMax = su < 0 ? -5 : 7;
				c.fillId(uMin, uMax, tf, tf + 2, 0, 8, "polished_blackstone_bricks");
				for (int u = uMin; u <= uMax; u++) {
					for (int f = tf; f <= tf + 2; f++) {
						boolean center = u == (uMin + uMax) / 2 && f == tf + 1;
						if (center) c.put(u, f, 9, "soul_campfire");
						else if (Math.floorMod(u + f, 2) == 0) {
							c.put(u, f, 9, "blackstone_wall");
							c.put(u, f, 10, c.rnd.nextBoolean() ? "wither_skeleton_skull" : "skeleton_skull");
						}
					}
				}
			}
		}

		// Interior dividers with archways: entry hall | barracks | armory.
		for (int f : new int[] {3, 8}) {
			c.fillId(-6, 6, f, f, 0, 4, "polished_blackstone_bricks");
			c.fillId(-1, 1, f, f, 0, 2, "air");
		}
		c.fillId(-6, 6, 4, 7, 0, 4, "air");

		// Arrow slits and broken windows (bars with cobwebs behind them).
		int[][] slits = {{-3, 0}, {3, 0}, {-7, 5}, {-7, 6}, {7, 5}, {7, 6}, {-7, 9}, {7, 9}, {-3, 12}, {0, 12}, {3, 12}};
		for (int[] s : slits) {
			c.put(s[0], s[1], 2, "iron_bars");
			c.put(s[0], s[1], 3, "iron_bars");
		}

		// Gate: iron door with buttons, red banners and a skull on each side.
		c.doorId(0, 0, 0, "iron_door", c.fwd);
		c.wallButton(1, -1, 1, back);
		c.wallButton(1, 1, 1, c.fwd);
		for (int s : new int[] {-3, 3}) c.putFaced(s, -1, 3, "red_wall_banner", back);
		for (int s : new int[] {-2, 2}) {
			c.fillId(s, s, -1, -1, 0, 1, "blackstone_wall");
			c.put(s, -1, 2, "wither_skeleton_skull");
		}

		// Dim lighting: a few soul lanterns only.
		int[][] lights = {{-3, 2}, {3, 2}, {-3, 6}, {3, 6}, {0, 10}};
		for (int[] l : lights) hangingSoul(c, l[0], l[1], 4);

		// Entry hall: skull pillars, a war bell, a guard's lectern.
		c.putFaced(-3, 1, 0, "lectern", c.fwd);
		c.put(3, 1, 0, "bell");
		for (int s : new int[] {-5, 5}) {
			c.fillId(s, s, 1, 1, 0, 2, "polished_blackstone_brick_wall");
			c.put(s, 1, 3, "wither_skeleton_skull");
		}

		// Barracks hall: grim beds with footlockers, a blood-red carpet down the middle.
		for (int f = 4; f <= 7; f++) {
			c.bedId(-5, f, 0, "black_bed", left);
			c.putFaced(-4, f, 0, "chest", c.right);
			c.bedId(5, f, 0, "gray_bed", c.right);
			c.putFaced(4, f, 0, "chest", left);
			c.put(0, f, 0, "red_carpet");
		}

		// Armory: forge and weapon racks along the back wall.
		String[] kit = {"smithing_table", "anvil", "grindstone", "blast_furnace", "crafting_table", "chest", "chest", "chest", "barrel"};
		for (int i = 0; i < kit.length; i++) c.putFaced(-4 + i, 11, 0, kit[i], back);
		for (int u = -6; u <= 6; u += 2) {
			c.put(u, 11, 2, "iron_bars");
			c.put(u, 11, 3, "iron_bars");
		}

		// Prison cells in the armory: iron-bar cages with a cauldron "bucket" and a chained skull.
		for (int s : new int[] {-1, 1}) {
			c.fillId(5 * s, 5 * s, 9, 10, 0, 2, "iron_bars");
			c.fillId(4 * s, 6 * s, 9, 9, 0, 2, "iron_bars");
			c.put(6 * s, 10, 0, "cauldron");
			c.put(6 * s, 10, 1, "wither_skeleton_skull");
		}

		// Cobwebs in the rafters and corners.
		for (int u = -6; u <= 6; u++) {
			for (int f = 1; f <= 11; f++) {
				if (c.rnd.nextInt(14) == 0) c.put(u, f, 4, "cobweb");
			}
		}

		// Forecourt: skull stakes, a spiked iron barricade and soul fires.
		for (int u = -6; u <= 6; u += 3) {
			if (Math.abs(u) > 1) skullStake(c, u, -3);
		}
		for (int u = -6; u <= 6; u++) {
			if (Math.abs(u) <= 1) continue;
			c.put(u, -4, 0, "iron_bars");
			c.put(u, -4, 1, "iron_bars");
			if (Math.floorMod(u, 2) == 0) c.put(u, -4, 2, "blackstone_wall");
		}
		for (int s : new int[] {-1, 1}) {
			c.put(2 * s, -2, 0, "soul_campfire");
			c.put(5 * s, -1, 0, "cobweb");
		}
	}
}

package com.housebuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Castles in four sizes. Layout (local coords): gate at u=0,f=0, walls form a square
 * u=-hw..hw, f=0..2*hw, corner towers, optional mid/gate towers, keep at the back.
 */
final class CastleBuilder {
	private CastleBuilder() {}

	/** hw: half width; cTw/cH corner tower width/height; mTw/mH mid towers; gTw/gH gate towers. */
	private record Cfg(int hw, int wallH, int cTw, int cH, int mTw, int mH, int gTw, int gH,
					   int keepW, int keepH, boolean moat, boolean roofed, int extras) {}

	private static final Cfg[] CFG = {
		new Cfg(8, 6, 5, 10, 0, 0, 0, 0, 5, 9, false, false, 0),       // small
		new Cfg(12, 8, 7, 13, 5, 11, 5, 12, 9, 13, true, true, 1),      // medium
		new Cfg(17, 10, 9, 16, 5, 13, 7, 15, 13, 18, true, true, 2),    // large
		new Cfg(23, 12, 11, 20, 7, 16, 9, 18, 17, 23, true, true, 3)    // grand
	};

	static void build(Ctx c, int tier) {
		Cfg g = CFG[Math.min(tier, CFG.length - 1)];
		int hw = g.hw(), D = 2 * hw, hh = g.cTw() / 2;
		int m = hh + (g.moat() ? 7 : 3);

		// Clear the site and lay a foundation.
		c.fillId(-hw - m, hw + m, -m, D + m, 0, Math.max(g.cH(), g.keepH()) + 12, "air");
		c.foundation(-hw - hh, hw + hh, -hh, D + hh, Ctx.state("cobblestone"));
		courtyardFloor(c, hw, D);
		if (g.moat()) moat(c, hw, D, hh);

		walls(c, g, hw, D);
		gate(c);
		wallStairs(c, g, hw, hh);
		towers(c, g, hw, D, hh);
		keep(c, g, D);
		extras(c, g, hw, D, hh);

		// Path lamps along the road to the keep.
		int kf = D - g.keepW() / 2 - 4, keepFront = kf - g.keepW() / 2;
		for (int f = 4; f < keepFront - 1; f += 6) {
			for (int s : new int[] {-2, 2}) {
				c.fillId(s, s, f, f, 0, 1, "stone_brick_wall");
				c.put(s, f, 2, "lantern");
			}
		}
	}

	private static String mix(Ctx c) {
		int r = c.rnd.nextInt(10);
		return r < 7 ? "stone_bricks" : r < 9 ? "mossy_stone_bricks" : "cracked_stone_bricks";
	}

	private static void courtyardFloor(Ctx c, int hw, int D) {
		for (int u = -hw + 2; u <= hw - 2; u++) {
			for (int f = 2; f <= D - 2; f++) {
				int r = c.rnd.nextInt(10);
				c.put(u, f, -1, r < 4 ? "cobblestone" : r < 6 ? "stone_bricks" : r < 8 ? "mossy_cobblestone" : "gravel");
			}
		}
		c.fillId(-1, 1, -6, D - 2, -1, -1, "stone_bricks");   // road from the gate
	}

	private static void moat(Ctx c, int hw, int D, int hh) {
		int g1 = hh + 1, g2 = g1 + 3;
		for (int u = -hw - g2; u <= hw + g2; u++) {
			for (int f = -g2; f <= D + g2; f++) {
				boolean inner = Math.abs(u) <= hw + g1 && f >= -g1 && f <= D + g1;
				if (inner) continue;
				c.putLiquid(u, f, -1, "water");
				c.putLiquid(u, f, -2, "water");
			}
		}
		c.fillId(-1, 1, -g2, -1, -1, -1, "oak_planks");           // drawbridge
		for (int s : new int[] {-2, 2}) c.fillId(s, s, -g2, -g1 - 1, 0, 0, "oak_fence");
	}

	private static void walls(Ctx c, Cfg g, int hw, int D) {
		int wh = g.wallH();
		for (int u = -hw; u <= hw; u++) {
			for (int f = 0; f <= D; f++) {
				boolean band = Math.abs(u) >= hw - 1 || f <= 1 || f >= D - 1;
				if (!band) continue;
				for (int y = 0; y < wh; y++) c.put(u, f, y, y == 0 ? "stone_bricks" : mix(c));
				boolean outer = Math.abs(u) == hw || f == 0 || f == D;
				if (!outer) continue;
				if (Math.floorMod(u + f, 2) == 0) {
					c.put(u, f, wh, "stone_bricks");
					c.put(u, f, wh + 1, "stone_bricks");
				} else {
					c.put(u, f, wh, "stone_brick_wall");
				}
			}
		}
	}

	private static void gate(Ctx c) {
		c.fillId(-1, 1, 0, 1, 0, 3, "air");
		c.fillId(-1, 1, 0, 0, 3, 3, "iron_bars");   // raised portcullis
		c.place(0, 1, 3, Ctx.with(Ctx.state("lantern"), BlockStateProperties.HANGING, true));
	}

	/** Stairs from the courtyard up to the wall walk, on both sides. */
	private static void wallStairs(Ctx c, Cfg g, int hw, int hh) {
		int wh = g.wallH(), row = hh + 2;
		for (int su : new int[] {-1, 1}) {
			Direction up = su < 0 ? c.left() : c.right;
			for (int i = 0; i < wh; i++) {
				int u = su * (hw - 2 - (wh - 1 - i));
				if (i > 0) c.fillId(u, u, row, row, 0, i - 1, "stone_bricks");
				c.putFaced(u, row, i, "stone_brick_stairs", up);
			}
		}
	}

	private static void towers(Ctx c, Cfg g, int hw, int D, int hh) {
		int wh = g.wallH();
		for (int su : new int[] {-1, 1}) {
			int sideU1 = su * hw, sideU2 = su * (hw - 1);
			// Corner towers: doorways where the wall walk arrives.
			for (int back = 0; back < 2; back++) {
				int cf = back == 0 ? 0 : D;
				int sf = back == 0 ? 1 : -1;
				int bandA = back == 0 ? 0 : D - 1, bandB = back == 0 ? 1 : D;
				int faceU = su * hw - su * hh, faceF = cf + sf * hh;
				int[][] doors = {{faceU, bandA}, {faceU, bandB}, {sideU1, faceF}, {sideU2, faceF}};
				tower(c, su * hw, cf, g.cTw(), g.cH(), wh, g.roofed(), doors, back == 0 ? 1 : -1);
			}
			// Mid-wall towers.
			if (g.mTw() > 0) {
				int mh = g.mTw() / 2, mf = D / 2;
				int[][] doors = {{sideU1, mf - mh}, {sideU2, mf - mh}, {sideU1, mf + mh}, {sideU2, mf + mh}};
				tower(c, su * hw, mf, g.mTw(), g.mH(), wh, g.roofed(), doors, 1);
			}
			// Gate towers.
			if (g.gTw() > 0) {
				int gh = g.gTw() / 2, cu = su * (1 + (g.gTw() + 1) / 2), outer = cu + su * gh;
				int[][] doors = {{outer, 0}, {outer, 1}};
				tower(c, cu, gh - 1, g.gTw(), g.gH(), wh, g.roofed(), doors, 1);
			}
		}
		if (g.mTw() > 0) {   // back-centre tower
			int mh = g.mTw() / 2;
			int[][] doors = {{-mh, D - 1}, {-mh, D}, {mh, D - 1}, {mh, D}};
			tower(c, 0, D, g.mTw(), g.mH(), wh, g.roofed(), doors, -1);
		}
	}

	/**
	 * Solid base up to {@code baseH}, hollow shell above it with a ladder up to a deck.
	 * Open-top towers get battlements; roofed ones get a pointed roof.
	 * ladderDir: +1 puts the ladder on the front-side wall, -1 on the back-side wall.
	 */
	private static void tower(Ctx c, int cu, int cf, int tw, int h, int baseH, boolean roofed, int[][] doors, int ladderDir) {
		int hh = tw / 2;
		int ringTop = roofed ? h + 3 : h - 1;
		c.fillId(cu - hh, cu + hh, cf - hh, cf + hh, 0, baseH - 1, "stone_bricks");
		c.ring(cu - hh, cu + hh, cf - hh, cf + hh, baseH, ringTop, Ctx.state("stone_bricks"));
		c.fillId(cu - hh + 1, cu + hh - 1, cf - hh + 1, cf + hh - 1, baseH, ringTop, "air");
		for (int[] d : doors) c.fillId(d[0], d[0], d[1], d[1], baseH, baseH + 1, "air");

		c.fillId(cu - hh + 1, cu + hh - 1, cf - hh + 1, cf + hh - 1, h - 1, h - 1, "spruce_planks");   // deck
		int lf = ladderDir > 0 ? cf - (hh - 1) : cf + (hh - 1);
		Direction lookDir = ladderDir > 0 ? c.fwd : c.back();
		c.fillId(cu, cu, lf, lf, h - 1, h - 1, "air");
		for (int y = baseH; y <= h - 1; y++) c.putFaced(cu, lf, y, "ladder", lookDir);

		if (!roofed) {
			for (int u = cu - hh; u <= cu + hh; u++) {
				for (int f = cf - hh; f <= cf + hh; f++) {
					boolean edge = u == cu - hh || u == cu + hh || f == cf - hh || f == cf + hh;
					if (edge && Math.floorMod(u + f, 2) == 0) c.put(u, f, h, "stone_bricks");
				}
			}
			return;
		}
		for (int[] w : new int[][] {{cu + hh, cf}, {cu - hh, cf}, {cu, cf + hh}, {cu, cf - hh}}) {
			c.put(w[0], w[1], h + 1, "iron_bars");   // arrow slits in the upper room
		}
		Furniture.light(c, cu, cf, h + 3, false);
		int half = hh + 1, y = ringTop + 1;
		while (half >= 0) {
			c.fillId(cu - half, cu + half, cf - half, cf + half, y, y, "deepslate_tiles");
			half--;
			y++;
		}
		c.put(cu, cf, y, "lightning_rod");
	}

	private static void keep(Ctx c, Cfg g, int D) {
		int kw = g.keepW(), kh = g.keepH(), hh = kw / 2;
		int kf = D - hh - 4;
		int u1 = -hh + 1, u2 = hh - 1, f1 = kf - hh + 1, f2 = kf + hh - 1;
		boolean dungeon = g.extras() >= 3;
		int bottom = dungeon ? -7 : 0;

		c.ring(-hh, hh, kf - hh, kf + hh, bottom, kh - 1, Ctx.state("stone_bricks"));
		c.fillId(u1, u2, f1, f2, 0, kh - 2, "air");
		c.fillId(u1, u2, f1, f2, -1, -1, "polished_andesite");
		for (int y = 7; kh - 1 - y >= 4; y += 5) c.fillId(u1, u2, f1, f2, y, y, "spruce_planks");
		c.fillId(u1, u2, f1, f2, kh - 1, kh - 1, "stone_bricks");
		for (int u = -hh; u <= hh; u++) {                            // roof battlements
			for (int f = kf - hh; f <= kf + hh; f++) {
				boolean edge = u == -hh || u == hh || f == kf - hh || f == kf + hh;
				if (edge && Math.floorMod(u + f, 2) == 0) c.put(u, f, kh, "stone_bricks");
			}
		}

		// Ladder up the back-right corner through every floor.
		int lu = u2, lf = f2;
		for (int y = 7; kh - 1 - y >= 4; y += 5) c.put(lu, lf, y, "air");
		c.put(lu, lf, kh - 1, "air");
		for (int y = dungeon ? -6 : 0; y <= kh - 1; y++) c.putFaced(lu, lf, y, "ladder", c.back());

		c.doorId(0, kf - hh, 0, "dark_oak_door", c.fwd);
		for (int s : new int[] {-2, 2}) c.putFaced(s, kf - hh - 1, 4, "red_wall_banner", c.back());
		for (int y0 : new int[] {2, 8, 13, 18}) {                    // arrow slits
			if (y0 + 2 >= kh - 1) continue;
			for (int y = y0; y <= y0 + 1; y++) {
				c.put(hh, kf, y, "iron_bars");
				c.put(-hh, kf, y, "iron_bars");
				c.put(0, kf + hh, y, "iron_bars");
			}
		}

		// Great hall: red carpet, throne, banners, long tables.
		int hallTop = kh - 1 >= 12 ? 7 : kh - 1;
		for (int f = f1; f <= f2; f++) c.put(0, f, 0, "red_carpet");
		c.put(0, f2, 0, "gold_block");
		for (int s : new int[] {-2, 0, 2}) {
			if (Math.abs(s) <= u2) c.putFaced(s, f2, 3, "red_wall_banner", c.back());
		}
		if (kw >= 9) {
			for (int s : new int[] {-2, 2}) {
				for (int f = f1 + 2; f <= f2 - 2; f++) {
					c.put(s, f, 0, "dark_oak_fence");
					c.put(s, f, 1, "dark_oak_pressure_plate");
					c.putFaced(s + (s > 0 ? 1 : -1), f, 0, "dark_oak_stairs", s > 0 ? c.right : c.left());
				}
			}
		}
		for (int u = u1 + 1; u <= u2 - 1; u += 4) {
			for (int f = f1 + 1; f <= f2 - 1; f += 4) Furniture.light(c, u, f, hallTop - 1, false);
		}

		// Upper storeys: bedrooms with chests, bookshelves and lanterns.
		int idx = 0;
		for (int ys = 7; kh - 1 - ys >= 4; ys += 5) {
			c.bedId(u1 + 1, f2 - 1, ys + 1, idx++ % 2 == 0 ? "red_bed" : "blue_bed", c.fwd);
			c.putFaced(u1, f1, ys + 1, "chest", c.fwd);
			c.fillId(u2, u2, kf, kf, ys + 1, ys + 2, "bookshelf");
			Furniture.light(c, 0, kf, ys + 4, false);
		}

		// Keep turrets on the four corners of the roof for the big castles.
		if (g.extras() >= 2) {
			for (int su : new int[] {-1, 1}) {
				for (int sf : new int[] {-1, 1}) {
					int cu = su * (hh + 1), cf = kf + sf * (hh + 1);
					tower(c, cu, cf, 3, kh + 6, kh, true, new int[][] {{cu - su, cf}}, 1);
				}
			}
		}
		if (dungeon) dungeon(c, hh, kf);
	}

	/** Grand castle only: a cellar below the keep with iron-bar cells. */
	private static void dungeon(Ctx c, int hh, int kf) {
		int u1 = -hh + 1, u2 = hh - 1, f1 = kf - hh + 1, f2 = kf + hh - 1;
		c.fillId(u1, u2, f1, f2, -6, -2, "air");
		c.fillId(u1, u2, f1, f2, -7, -7, "deepslate_bricks");
		c.fillId(0, 0, f1, f2 - 2, -6, -2, "iron_bars");
		for (int u = u1; u <= u2; u++) {
			for (int f = f1; f <= f2; f++) {
				if (c.rnd.nextInt(9) == 0) c.put(u, f, -6, "cobweb");
			}
		}
		for (int s : new int[] {-2, 2}) {
			c.place(s, kf, -2, Ctx.with(Ctx.state("soul_lantern"), BlockStateProperties.HANGING, true));
			c.putFaced(s * 2, f1, -6, "chest", c.fwd);
		}
		c.put(u1 + 1, f2 - 1, -6, "cauldron");
	}

	private static void extras(Ctx c, Cfg g, int hw, int D, int hh) {
		well(c, 3, hh + 4);
		if (g.extras() < 1) return;
		int fa = hh + 6;
		stable(c, -(hw - 2), fa);
		forge(c, hw - 7, fa);
		if (g.extras() >= 2) {
			int kf = D - g.keepW() / 2 - 4;
			for (int su : new int[] {-1, 1}) {
				stall(c, su * (hw - 5), kf - 4, su < 0 ? "red_wool" : "blue_wool");
				stall(c, su * (hw - 5), kf + 3, su < 0 ? "white_wool" : "yellow_wool");
			}
		}
	}

	private static void well(Ctx c, int cu, int cf) {
		c.fillId(cu - 1, cu + 1, cf - 1, cf + 1, 0, 0, "stone_bricks");
		c.putLiquid(cu, cf, 0, "water");
		for (int du : new int[] {-1, 1}) {
			for (int df : new int[] {-1, 1}) c.fillId(cu + du, cu + du, cf + df, cf + df, 1, 3, "oak_fence");
		}
		c.fillId(cu - 1, cu + 1, cf - 1, cf + 1, 4, 4, "spruce_slab");
	}

	private static void stable(Ctx c, int a, int b) {
		c.ring(a, a + 5, b, b + 5, 0, 0, Ctx.state("oak_fence"));
		c.fillId(a + 2, a + 3, b, b, 0, 0, "air");
		for (int[] p : new int[][] {{a, b}, {a + 5, b}, {a, b + 5}, {a + 5, b + 5}}) c.fillId(p[0], p[0], p[1], p[1], 0, 3, "oak_log");
		c.fillId(a, a + 5, b, b + 5, 4, 4, "spruce_slab");
		c.put(a + 1, b + 4, 0, "hay_block");
		c.put(a + 2, b + 4, 0, "hay_block");
		c.put(a + 4, b + 4, 0, "water_cauldron");
		c.place(a + 3, b + 2, 3, Ctx.with(Ctx.state("lantern"), BlockStateProperties.HANGING, true));
	}

	private static void forge(Ctx c, int a, int b) {
		for (int[] p : new int[][] {{a, b}, {a + 5, b}, {a, b + 4}, {a + 5, b + 4}}) c.fillId(p[0], p[0], p[1], p[1], 0, 3, "stone_bricks");
		c.fillId(a, a + 5, b, b + 4, 4, 4, "stone_brick_slab");
		c.fillId(a, a + 5, b + 4, b + 4, 0, 3, "stone_bricks");   // back wall
		String[] kit = {"blast_furnace", "anvil", "grindstone", "smithing_table", "barrel", "cauldron"};
		for (int i = 0; i < 6; i++) c.putFaced(a + i, b + 3, 0, kit[i], c.back());
		c.place(a + 2, b + 2, 3, Ctx.with(Ctx.state("lantern"), BlockStateProperties.HANGING, true));
	}

	private static void stall(Ctx c, int cu, int cf, String wool) {
		for (int du : new int[] {-1, 1}) {
			for (int df : new int[] {-1, 1}) c.fillId(cu + du, cu + du, cf + df, cf + df, 0, 2, "oak_fence");
		}
		for (int u = cu - 1; u <= cu + 1; u++) {
			for (int f = cf - 1; f <= cf + 1; f++) c.put(u, f, 3, Math.floorMod(u + f, 2) == 0 ? wool : "white_wool");
		}
		for (int u = cu - 1; u <= cu + 1; u++) c.put(u, cf - 1, 0, u == cu ? "hay_block" : "barrel");
	}
}

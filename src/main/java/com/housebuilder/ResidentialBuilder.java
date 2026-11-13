package com.housebuilder;

import java.util.List;

import com.housebuilder.HouseLayouts.Layout;
import com.housebuilder.HouseLayouts.Room;

/** Wooden and modern houses in four sizes: shell, roof, stairs, rooms and furniture. */
final class ResidentialBuilder {
	private ResidentialBuilder() {}

	private record Pal(String wall, String corner, String trim, String floor, String part, String ceil,
					   String found, String path, String glass, String stair, String roofStair, String roof, String door) {}

	private static final Pal WOOD = new Pal("oak_planks", "oak_log", "dark_oak_planks", "spruce_planks", "oak_planks",
		"spruce_planks", "cobblestone", "stone_bricks", "glass_pane", "oak_stairs", "dark_oak_stairs", "dark_oak_planks", "spruce_door");
	private static final Pal MOD = new Pal("white_concrete", "black_concrete", "black_concrete", "light_gray_concrete",
		"white_concrete", "white_concrete", "gray_concrete", "smooth_stone", "glass", "quartz_stairs", "quartz_stairs",
		"black_concrete", "dark_oak_door");

	static void build(Ctx c, boolean modern, int tier) {
		Layout L = HouseLayouts.of(tier);
		Pal p = modern ? MOD : WOOD;
		int hw = L.hw(), d = L.d(), n = L.floors(), top = 5 * n;
		int rise = modern ? 4 : hw + 2;

		// Clear the site, lay the foundation, ground slab and the path to the door.
		c.fillId(-hw - 4, hw + 4, L.pool() ? -13 : -5, d + 4, 0, top + rise + 2, "air");
		c.foundation(-hw, hw, 0, d, Ctx.state(p.found()));
		c.fillId(-hw, hw, 0, d, -1, -1, p.found());
		c.fillId(-1, 1, -5, -1, -1, -1, p.path());

		walls(c, p, modern, hw, d, n);

		// Interiors: solid fill, then carve rooms and doorways, then furnish.
		for (int k = 0; k < n; k++) {
			int y0 = 5 * k;
			List<Room> rooms = k == 0 ? L.ground() : L.upper();
			int[][] doors = k == 0 ? L.gDoors() : L.uDoors();
			c.fillId(-hw + 1, hw - 1, 1, d - 1, y0, y0 + 3, p.part());
			c.fillId(-hw + 1, hw - 1, 1, d - 1, y0 + 4, y0 + 4, k == n - 1 ? p.ceil() : p.floor());
			for (Room r : rooms) {
				c.fillId(r.u1(), r.u2(), r.f1(), r.f2(), y0, y0 + 3, "air");
				c.fillId(r.u1(), r.u2(), r.f1(), r.f2(), y0 - 1, y0 - 1, floorFor(r.type(), p));
			}
			for (int[] dr : doors) c.fillId(dr[0], dr[0], dr[1], dr[1], y0, y0 + 1, "air");
			int i = 0;
			for (Room r : rooms) Furniture.furnish(c, r, y0, modern, doorFar(r, doors), i++);
		}

		if (n == 2) stairs(c, p, L);

		// Front door (replaces the builder block) and porch.
		c.doorId(0, 0, 0, p.door(), c.fwd);
		if (tier >= 1) {
			c.fillId(-2, 2, -2, -1, -1, -1, p.found());
			for (int s : new int[] {-2, 2}) c.fillId(s, s, -2, -2, 0, 2, p.corner());
			c.fillId(-2, 2, -2, -1, 3, 3, p.trim());
			Furniture.light(c, 0, -1, 2, modern);
		}

		if (modern) modernRoof(c, p, tier, hw, d, top);
		else woodenRoof(c, p, tier, hw, d, top);

		if (L.pool()) pool(c);
	}

	private static String floorFor(String type, Pal p) {
		return switch (type) {
			case "bath" -> "smooth_quartz";
			case "kitchen" -> "polished_andesite";
			default -> p.floor();
		};
	}

	private static boolean doorFar(Room r, int[][] doors) {
		for (int[] d : doors) {
			if (d[1] == r.f2() + 1 && d[0] >= r.u1() && d[0] <= r.u2()) return true;
		}
		return false;
	}

	private static void walls(Ctx c, Pal p, boolean modern, int hw, int d, int n) {
		for (int k = 0; k < n; k++) {
			for (int u = -hw; u <= hw; u++) {
				for (int f = 0; f <= d; f++) {
					boolean edge = Math.abs(u) == hw || f == 0 || f == d;
					if (!edge) continue;
					boolean corner = Math.abs(u) == hw && (f == 0 || f == d);
					int s = (f == 0 || f == d) ? u : f;
					boolean doorZone = k == 0 && f == 0 && Math.abs(u) <= (modern ? 2 : 1);
					boolean windowCol = modern ? Math.floorMod(s, 4) != 0 : Math.floorMod(Math.abs(s), 4) == 2;
					for (int yl = 0; yl <= 4; yl++) {
						String id;
						if (corner) id = p.corner();
						else if (yl == 4) id = p.trim();
						else if ((yl == 1 || yl == 2) && windowCol && !doorZone) id = p.glass();
						else id = p.wall();
						c.put(u, f, 5 * k + yl, id);
					}
				}
			}
		}
	}

	private static void stairs(Ctx c, Pal p, Layout L) {
		int su = L.stairU(), f0 = L.stairF0();
		c.fillId(su, su, f0, f0 + 4, 0, 3, "air");
		c.fillId(su, su, f0, f0 + 4, -1, -1, p.floor());
		c.fillId(su, su, f0 + 2, f0 + 5, 5, 8, "air");   // stairwell on the upper floor
		c.fillId(su, su, f0 + 2, f0 + 3, 4, 4, "air");   // hole in the upper floor slab
		for (int i = 0; i < 5; i++) {
			if (i > 0) c.fillId(su, su, f0 + i, f0 + i, 0, i - 1, p.part());
			c.putFaced(su, f0 + i, i, p.stair(), c.fwd);
		}
	}

	private static void woodenRoof(Ctx c, Pal p, int tier, int hw, int d, int top) {
		for (int k = 0; k < hw; k++) {
			int y = top + k, ul = hw - k;
			for (int f = -1; f <= d + 1; f++) {
				c.putFaced(-ul, f, y, p.roofStair(), c.right);
				c.putFaced(ul, f, y, p.roofStair(), c.left());
			}
			c.fillId(-(ul - 1), ul - 1, 0, 0, y, y, p.wall());   // gable ends
			c.fillId(-(ul - 1), ul - 1, d, d, y, y, p.wall());
		}
		c.fillId(0, 0, -1, d + 1, top + hw, top + hw, p.roof());   // ridge
		if (tier >= 1) c.fillId(hw - 2, hw - 2, d - 2, d - 2, top, top + 4, "bricks");   // chimney
	}

	private static void modernRoof(Ctx c, Pal p, int tier, int hw, int d, int top) {
		c.fillId(-hw - 1, hw + 1, -1, d + 1, top, top, p.roof());
		if (tier < 1) return;
		c.ring(-hw - 1, hw + 1, -1, d + 1, top + 1, top + 1, Ctx.state("glass_pane"));
		for (int u = -2; u <= 2; u++) c.put(u, d / 2, top + 1, "daylight_detector");
		if (tier >= 2) {
			for (int u = -4; u <= 4; u += 2) c.putFaced(u, 3, top + 1, "quartz_stairs", c.fwd);
			c.put(hw, d, top + 1, "sea_lantern");
			c.put(-hw, d, top + 1, "sea_lantern");
		}
	}

	/** Villa pool: quartz rim, 2-deep water, corner lamp posts. */
	private static void pool(Ctx c) {
		c.fillId(2, 9, -10, -3, -1, -1, "smooth_quartz");
		c.fillId(3, 8, -9, -4, -3, -3, "light_blue_concrete");
		for (int u = 3; u <= 8; u++) {
			for (int f = -9; f <= -4; f++) {
				c.putLiquid(u, f, -2, "water");
				c.putLiquid(u, f, -1, "water");
			}
		}
		for (int[] q : new int[][] {{2, -3}, {9, -3}, {2, -10}, {9, -10}}) {
			c.fillId(q[0], q[0], q[1], q[1], 0, 1, "stone_brick_wall");
			c.put(q[0], q[1], 2, "lantern");
		}
	}
}

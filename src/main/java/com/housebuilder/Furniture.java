package com.housebuilder;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Fills one room with furniture. Placement is relative to the wall opposite the doorway. */
final class Furniture {
	private Furniture() {}

	static void light(Ctx c, int u, int f, int y, boolean modern) {
		if (modern) {
			c.put(u, f, y, "sea_lantern");
		} else {
			c.place(u, f, y, Ctx.with(Ctx.state("lantern"), BlockStateProperties.HANGING, true));
		}
	}

	static void furnish(Ctx c, HouseLayouts.Room r, int y0, boolean modern, boolean doorFar, int idx) {
		int u1 = r.u1(), u2 = r.u2(), f1 = r.f1(), f2 = r.f2();
		int w = u2 - u1 + 1, dep = f2 - f1 + 1;
		int um = (u1 + u2) / 2, fm = (f1 + f2) / 2;

		int wf = doorFar ? f1 : f2;                       // wall opposite the door
		int in = doorFar ? 1 : -1;                        // one step from that wall into the room
		Direction toWall = doorFar ? c.back() : c.fwd;    // direction pointing at that wall
		Direction inward = doorFar ? c.fwd : c.back();    // direction pointing into the room

		String carpet = modern ? "gray_carpet" : "red_carpet";
		String fence = modern ? "dark_oak_fence" : "oak_fence";
		String plate = modern ? "dark_oak_pressure_plate" : "oak_pressure_plate";
		String chair = modern ? "quartz_stairs" : "oak_stairs";
		String sofa = modern ? "polished_blackstone_stairs" : "crimson_stairs";
		String bed = modern ? (idx % 2 == 0 ? "white_bed" : "gray_bed") : (idx % 2 == 0 ? "red_bed" : "blue_bed");

		// Ceiling lights on a grid (halls get a row of their own).
		if (r.type().equals("hall")) {
			for (int u = u1 + 1; u <= u2; u += 4) light(c, u, fm, y0 + 3, modern);
		} else {
			boolean any = false;
			for (int u = u1 + 1; u <= u2 - 1; u += 4) {
				for (int f = f1 + 1; f <= f2 - 1; f += 4) {
					light(c, u, f, y0 + 3, modern);
					any = true;
				}
			}
			if (!any) light(c, um, fm, y0 + 3, modern);
		}

		switch (r.type()) {
			case "living" -> {
				for (int u = u2 - 4; u <= u2 - 1; u++) {
					for (int f = fm - 2; f <= fm + 2; f++) {
						if (u >= u1 && f >= f1 && f <= f2) c.put(u, f, y0, carpet);
					}
				}
				if (w >= 6 && dep >= 3) {
					c.fillId(u2, u2, fm - 1, fm + 1, y0, y0 + 2, "bricks"); // fireplace on the right wall
					c.put(u2, fm, y0, "campfire");
					for (int f = fm - 1; f <= fm + 1; f++) c.putFaced(u2 - 3, f, y0, sofa, c.left());
					c.put(u2 - 2, fm, y0, fence);                              // coffee table
					c.put(u2 - 2, fm, y0 + 1, plate);
					c.put(u2, f1, y0, "potted_fern");
				}
			}
			case "kitchen" -> {
				String[] kit = {"furnace", "smoker", "crafting_table", "cauldron", "barrel", "blast_furnace"};
				for (int u = u1; u <= u2; u++) {
					String id = kit[(u - u1) % kit.length];
					if (id.endsWith("furnace") || id.equals("smoker")) c.putFaced(u, wf, y0, id, inward);
					else c.put(u, wf, y0, id);
					if ((u - u1) % 2 == 0) c.put(u, wf, y0 + 2, "barrel"); // wall cabinets
				}
				if (w >= 5 && dep >= 4) {                                  // island with a cake on it
					for (int u = um - 1; u <= um + 1; u++) c.put(u, fm, y0, modern ? "smooth_stone_slab" : "spruce_slab");
					c.put(um, fm, y0 + 1, "cake");
				}
			}
			case "dining" -> {
				if (dep >= w) {
					int fa = dep >= 5 ? fm - 1 : fm;
					for (int f = fa; f <= fm + 1; f++) {
						c.put(um, f, y0, fence);
						c.put(um, f, y0 + 1, plate);
						c.putFaced(um - 1, f, y0, chair, c.left());
						c.putFaced(um + 1, f, y0, chair, c.right);
					}
				} else {
					for (int u = um - 1; u <= um + 1; u++) {
						c.put(u, fm, y0, fence);
						c.put(u, fm, y0 + 1, plate);
						c.putFaced(u, fm - 1, y0, chair, c.back());
						c.putFaced(u, fm + 1, y0, chair, c.fwd);
					}
				}
				c.put(u2, wf, y0, "potted_fern");
			}
			case "bedroom", "master" -> {
				boolean master = r.type().equals("master");
				int footF = wf + in;
				c.bedId(um, footF, y0, bed, toWall);
				if (master) c.bedId(um + 1, footF, y0, bed, toWall);
				int left = um - 1, right = um + (master ? 2 : 1);
				if (left >= u1) { c.put(left, wf, y0, "barrel"); c.put(left, wf, y0 + 1, "lantern"); }
				if (right <= u2) { c.put(right, wf, y0, "barrel"); c.put(right, wf, y0 + 1, "lantern"); }
				if (w >= 5 && dep >= 4) {
					c.putFaced(u1, wf + 3 * in, y0, "chest", c.right);          // wardrobe
					for (int k = 2; k <= 3; k++) {
						int f = footF + in * k;
						if (f < f1 || f > f2) continue;
						for (int u = Math.max(u1, left); u <= Math.min(u2, right); u++) c.put(u, f, y0, carpet);
					}
				}
			}
			case "bath" -> {
				for (int u = u1; u <= u2; u++) {
					int i = u - u1;
					if (i == 0) c.putFaced(u, wf, y0, "quartz_stairs", toWall);   // toilet
					else if (i % 3 == 1) c.put(u, wf, y0, "cauldron");           // sink
					else c.put(u, wf, y0, "water_cauldron");                     // tub
				}
				int mf = wf + 2 * in;
				if (mf >= f1 && mf <= f2) c.put(um, mf, y0, "white_carpet");
			}
			case "study" -> {
				for (int u = u1; u <= u2; u++) c.fillId(u, u, wf, wf, y0, y0 + 2, "bookshelf");
				c.put(um, fm, y0, "enchanting_table");
				c.putFaced(u2, fm, y0, "lectern", c.left());
			}
			case "hall" -> {
				for (int u = u1; u <= u2; u++) {
					for (int f = f1; f <= f2; f++) c.put(u, f, y0, carpet);
				}
				c.put(u2, f1, y0, "potted_fern");
			}
			default -> { }
		}
	}
}

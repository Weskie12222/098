package com.housebuilder;

import java.util.List;

/**
 * Room layouts for the four house sizes (shared by wooden and modern styles).
 * Coordinates are interior cells: u = sideways, f = forward. Anything that is not a
 * room is solid partition wall; "doors" are doorway cells cut through partitions.
 * The staircase (2-storey tiers) climbs forward from (stairU, stairF0).
 */
final class HouseLayouts {
	private HouseLayouts() {}

	record Room(String type, int u1, int u2, int f1, int f2) {}

	record Layout(int hw, int d, int floors, int stairU, int stairF0, boolean pool,
				  List<Room> ground, List<Room> upper, int[][] gDoors, int[][] uDoors) {}

	private static Room r(String type, int u1, int u2, int f1, int f2) {
		return new Room(type, u1, u2, f1, f2);
	}

	private static int[] p(int u, int f) {
		return new int[] {u, f};
	}

	static Layout of(int tier) {
		return switch (tier) {
			// Small: 9x10, one floor - living room, kitchen, bedroom.
			case 0 -> new Layout(4, 9, 1, 0, 0, false,
				List.of(r("living", -3, 3, 1, 4), r("kitchen", -3, -1, 6, 8), r("bedroom", 1, 3, 6, 8)),
				List.of(),
				new int[][] {p(-2, 5), p(2, 5)},
				new int[][] {});

			// Medium: 11x12, two floors - living, kitchen, dining / hall, master, bath, bedroom.
			case 1 -> new Layout(5, 11, 2, -4, 1, false,
				List.of(r("living", -3, 4, 1, 5), r("kitchen", -4, 0, 7, 10), r("dining", 2, 4, 7, 10)),
				List.of(r("hall", -3, 4, 6, 6), r("master", -2, 4, 1, 4), r("bath", -4, -2, 8, 10), r("bedroom", 0, 4, 8, 10)),
				new int[][] {p(-2, 6), p(3, 6), p(1, 8)},
				new int[][] {p(0, 5), p(-3, 7), p(2, 7)});

			// Large: 15x14, two floors - adds a study, more bedrooms.
			case 2 -> new Layout(7, 13, 2, -6, 1, false,
				List.of(r("living", -5, 6, 1, 6), r("kitchen", -6, -3, 8, 12), r("dining", -1, 2, 8, 12), r("study", 4, 6, 8, 12)),
				List.of(r("hall", -5, 6, 6, 6), r("bedroom", -4, -1, 1, 4), r("master", 1, 6, 1, 4),
					r("bath", -5, -3, 8, 12), r("bedroom", -1, 2, 8, 12), r("study", 4, 6, 8, 12)),
				new int[][] {p(-4, 7), p(0, 7), p(5, 7), p(-2, 10), p(3, 10)},
				new int[][] {p(-2, 5), p(3, 5), p(-4, 7), p(0, 7), p(5, 7)});

			// Villa: 19x16, two floors, plus a pool out front.
			default -> new Layout(9, 15, 2, -8, 1, true,
				List.of(r("living", -7, 3, 1, 6), r("study", 5, 8, 1, 6), r("kitchen", -8, -2, 8, 14),
					r("dining", 0, 4, 8, 14), r("study", 6, 8, 8, 14)),
				List.of(r("hall", -7, 8, 6, 6), r("bedroom", -6, -2, 1, 4), r("master", 0, 8, 1, 4),
					r("bedroom", -7, -3, 8, 14), r("bath", -1, 2, 8, 14), r("living", 4, 8, 8, 14)),
				new int[][] {p(4, 5), p(-5, 7), p(2, 7), p(7, 7), p(-1, 11), p(5, 11)},
				new int[][] {p(-4, 5), p(4, 5), p(-5, 7), p(0, 7), p(6, 7)});
		};
	}
}

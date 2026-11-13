package com.housebuilder;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

/** Local-coordinate helper: u = sideways (right +), f = forward, y = up. */
	// ------------------------------------------------------------------
	final class Ctx {
		// setBlock flags: 2 = send to clients, no neighbour updates (fast, avoids pops)
		private static final int FLAGS = 2;

		final Level level;
		final BlockPos origin;
		final Direction fwd;
		final Direction right;
		private final List<BlockPos> toSettle = new ArrayList<>();

		Ctx(Level level, BlockPos origin, Direction fwd) {
			this.level = level;
			this.origin = origin;
			this.fwd = fwd;
			this.right = fwd.getClockWise();
		}

		BlockPos at(int u, int f, int y) {
			return origin.relative(right, u).relative(fwd, f).above(y);
		}

		void set(int u, int f, int y, BlockState state) {
			level.setBlock(at(u, f, y), state, FLAGS);
		}

		/** Places a block that must later re-connect to its neighbours (glass panes). */
		void setSettled(int u, int f, int y, BlockState state) {
			BlockPos pos = at(u, f, y);
			level.setBlock(pos, state, FLAGS);
			toSettle.add(pos);
		}

		void settleAll() {
			for (BlockPos pos : toSettle) {
				BlockState current = level.getBlockState(pos);
				BlockState updated = Block.updateFromNeighbourShapes(current, level, pos);
				if (updated != current) {
					level.setBlock(pos, updated, FLAGS);
				}
			}
		}

		void fill(int u1, int u2, int f1, int f2, int y1, int y2, BlockState state) {
			for (int u = Math.min(u1, u2); u <= Math.max(u1, u2); u++) {
				for (int f = Math.min(f1, f2); f <= Math.max(f1, f2); f++) {
					for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
						set(u, f, y, state);
					}
				}
			}
		}

		/** Fills empty/replaceable/water space under the floor (y=-2 downward) so houses never float. */
		void foundation(int u1, int u2, int f1, int f2, BlockState state) {
			for (int u = u1; u <= u2; u++) {
				for (int f = f1; f <= f2; f++) {
					for (int y = -2; y >= -16; y--) {
						BlockPos pos = at(u, f, y);
						BlockState existing = level.getBlockState(pos);
						boolean gap = existing.isAir() || existing.canBeReplaced() || !existing.getFluidState().isEmpty();
						if (!gap) break;
						level.setBlock(pos, state, FLAGS);
					}
				}
			}
		}

		BlockState facing(Block block, Direction direction) {
			return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
		}

		void door(int u, int f, Block door) {
			BlockState base = facing(door, fwd);
			set(u, f, 0, base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
			set(u, f, 1, base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
		}

		/** Foot at (u, f, y), head one block further forward. */
		void bed(int u, int f, int y, Block bed) {
			bed(u, f, y, bed, fwd);
		}

		/** Foot at (u, f, y), head one block further in the given direction. */
		void bed(int u, int f, int y, Block bed, Direction dir) {
			int du = 0;
			int df = 0;
			if (dir == fwd) {
				df = 1;
			} else if (dir == fwd.getOpposite()) {
				df = -1;
			} else if (dir == right) {
				du = 1;
			} else {
				du = -1;
			}
			BlockState base = facing(bed, dir);
			set(u, f, y, base.setValue(BlockStateProperties.BED_PART, BedPart.FOOT));
			set(u + du, f + df, y, base.setValue(BlockStateProperties.BED_PART, BedPart.HEAD));
		}

		/** Stone button stuck to the wall face; {@code outward} is the direction the button faces. */
		void wallButton(int u, int f, int y, Direction outward) {
			set(u, f, y, Blocks.STONE_BUTTON.defaultBlockState()
				.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
				.setValue(BlockStateProperties.HORIZONTAL_FACING, outward));
		}

		// ---------------- ID-based helpers (safe across Minecraft renames) ----------------
		final java.util.Random rnd = new java.util.Random();

		/** Resolves a vanilla block by ID. An unknown ID gives air instead of a compile error. */
		static Block block(String id) {
			return BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", id));
		}

		static BlockState state(String id) {
			return block(id).defaultBlockState();
		}

		/** Sets a property only if the block has it, so a wrong guess never crashes. */
		static <T extends Comparable<T>> BlockState with(BlockState s, Property<T> p, T v) {
			return s.hasProperty(p) ? s.setValue(p, v) : s;
		}

		static BlockState faced(String id, Direction d) {
			BlockState s = with(state(id), BlockStateProperties.HORIZONTAL_FACING, d);
			return with(s, BlockStateProperties.FACING, d);
		}

		Direction back() { return fwd.getOpposite(); }
		Direction left() { return right.getOpposite(); }

		/** Places a block and queues connecting blocks (panes, fences, walls, bars) for a shape refresh. */
		void place(int u, int f, int y, BlockState s) {
			BlockPos pos = at(u, f, y);
			level.setBlock(pos, s, FLAGS);
			String n = BuiltInRegistries.BLOCK.getKey(s.getBlock()).getPath();
			if (n.endsWith("_pane") || n.endsWith("fence") || n.equals("iron_bars") || n.endsWith("_wall")) {
				toSettle.add(pos);
			}
		}

		void put(int u, int f, int y, String id) { place(u, f, y, state(id)); }

		void putFaced(int u, int f, int y, String id, Direction d) { place(u, f, y, faced(id, d)); }

		void fillId(int u1, int u2, int f1, int f2, int y1, int y2, String id) {
			fill(u1, u2, f1, f2, y1, y2, state(id));
		}

		/** Like put, but with neighbour updates so water and lava settle. */
		void putLiquid(int u, int f, int y, String id) {
			level.setBlock(at(u, f, y), state(id), 3);
		}

		void doorId(int u, int f, int y, String id, Direction facing) {
			BlockState base = faced(id, facing);
			set(u, f, y, base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
			set(u, f, y + 1, base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
		}

		void bedId(int u, int f, int y, String id, Direction toHead) {
			bed(u, f, y, block(id), toHead);
		}

		/** Hollow box outline (walls only) from y1..y2. */
		void ring(int u1, int u2, int f1, int f2, int y1, int y2, BlockState s) {
			for (int u = u1; u <= u2; u++) {
				for (int f = f1; f <= f2; f++) {
					if (u == u1 || u == u2 || f == f1 || f == f2) {
						for (int y = y1; y <= y2; y++) place(u, f, y, s);
					}
				}
			}
		}
	}


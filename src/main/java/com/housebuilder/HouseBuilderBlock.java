package com.housebuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Right-click this block and a house is built in front of you.
 * The block itself becomes the front door; the house extends away from the
 * direction you are facing when you click.
 */
public class HouseBuilderBlock extends Block {
	private final HouseStyle style;

	public HouseBuilderBlock(Properties properties, HouseStyle style) {
		super(properties);
		this.style = style;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		// The house grows away from the player, with the door facing them.
		Direction forward = player.getDirection();
		HouseGenerator.build(level, pos, forward, style);

		return InteractionResult.SUCCESS;
	}
}

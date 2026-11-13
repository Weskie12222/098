package com.housebuilder;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
	private ModBlocks() {}

	/** One builder block per style, registered in a loop. */
	public static final Map<HouseStyle, Block> BUILDERS = new EnumMap<>(HouseStyle.class);

	static {
		for (HouseStyle style : HouseStyle.values()) {
			BUILDERS.put(style, register(style));
		}
	}

	private static Block register(HouseStyle style) {
		BlockBehaviour.Properties props = switch (style.kind()) {
			case WOODEN -> BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOD);
			case MODERN -> BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.STONE);
			case MILITARY -> BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL);
			case CASTLE -> BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE);
		};

		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, HouseBuilderMod.id(style.blockId()));
		Block block = new HouseBuilderBlock(props.setId(blockKey), style);
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, HouseBuilderMod.id(style.blockId()));
		BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		return block;
	}

	/** Called from the mod initializer to trigger static initialisation. */
	public static void initialize() {}
}

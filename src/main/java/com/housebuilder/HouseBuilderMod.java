package com.housebuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;

public class HouseBuilderMod implements ModInitializer {
	public static final String MOD_ID = "housebuilder";

	@Override
	public void onInitialize() {
		ModBlocks.initialize();

		// Show every builder block in the "Building Blocks" creative tab.
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
			for (Block block : ModBlocks.BUILDERS.values()) {
				output.accept(block);
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}

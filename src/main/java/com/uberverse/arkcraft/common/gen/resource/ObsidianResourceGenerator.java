package com.uberverse.arkcraft.common.gen.resource;

import com.uberverse.arkcraft.init.ARKCraftBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public class ObsidianResourceGenerator extends SurfaceResourceGenerator
{
	public ObsidianResourceGenerator()
	{
		super(1, 1, 1, 2, 0.8);
	}

	@Override
	public boolean isValidPosition(World world, BlockPos pos)
	{
		return BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(pos), BiomeDictionary.Type.MOUNTAIN)
				&& super.isValidPosition(world, pos) && pos.getY() > 100;
	}

	@Override
	public IBlockState getGeneratedState()
	{
		return ARKCraftBlocks.obsidianResource.getDefaultState();
	}
}

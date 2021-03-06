package com.uberverse.arkcraft.common.gen.resource;

import com.uberverse.arkcraft.init.ARKCraftBlocks;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public class MetalResourceGenerator extends SurfaceResourceGenerator
{
	public MetalResourceGenerator()
	{
		super(1, 2, 2, 4, 0.8);
	}

	@Override
	public boolean isValidPosition(World world, BlockPos pos)
	{
		return BiomeDictionary.isBiomeOfType(world.getBiomeGenForCoords(pos), BiomeDictionary.Type.MOUNTAIN)
				&& super.isValidPosition(world, pos);
	}

	@Override
	public IBlockState getGeneratedState()
	{
		return ARKCraftBlocks.metalResource.getDefaultState();
	}
}

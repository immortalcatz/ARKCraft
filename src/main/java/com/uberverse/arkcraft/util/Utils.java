package com.uberverse.arkcraft.util;

import java.lang.reflect.Field;

import com.uberverse.arkcraft.common.item.IDecayable;
import com.uberverse.arkcraft.common.tileentity.IDecayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class Utils
{
	public static EnumFacing getDirectionFacing(EntityLivingBase entity, boolean includeUpAndDown)
	{
		double yaw = entity.rotationYaw;
		while (yaw < 0)
			yaw += 360;
		yaw = yaw % 360;
		if (includeUpAndDown)
		{
			if (entity.rotationPitch > 45) return EnumFacing.DOWN;
			else if (entity.rotationPitch < -45) return EnumFacing.UP;
		}
		if (yaw < 45) return EnumFacing.SOUTH;
		else if (yaw < 135) return EnumFacing.WEST;
		else if (yaw < 225) return EnumFacing.NORTH;
		else if (yaw < 315) return EnumFacing.EAST;

		else return EnumFacing.SOUTH;
	}

	public static boolean isUseable(BlockPos pos, EntityPlayer player, World worldObj, TileEntity thisT)
	{
		return worldObj.getTileEntity(pos) != thisT ? false : player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D,
				pos.getZ() + 0.5D) <= 64.0D;
	}

	static Field equippedProgress, prevEquippedProgress;

	// Function that handles the hack for the Item Swap Animation
	public static void setItemRendererEquippProgress(float From0To1, boolean isSmooth)
	{
		ItemRenderer IR = Minecraft.getMinecraft().entityRenderer.itemRenderer;
		if (IR != null) try
		{
			if (!isSmooth)
			{
				if (prevEquippedProgress == null) prevEquippedProgress = ItemRenderer.class.getDeclaredField(
						"prevEquippedProgress");
				prevEquippedProgress.setAccessible(true);
				prevEquippedProgress.setFloat(IR, From0To1);
			}
			if (equippedProgress == null) equippedProgress = ItemRenderer.class.getDeclaredField("equippedProgress");
			equippedProgress.setAccessible(true);
			equippedProgress.setFloat(IR, From0To1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void checkInventoryForDecayable(IInventory inventory)
	{
		if (inventory instanceof IDecayer) IDecayer.updateDecayer((IDecayer) inventory);
		else for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof IDecayable)
			{
				System.out.println("tick");
				IDecayable decayable = (IDecayable) stack.getItem();
				decayable.decayTick(inventory, i, 1, stack);
			}
		}
	}

	public static double calculateDistance(BlockPos pos1, BlockPos pos2)
	{
		double x1 = pos1.getX();
		double x2 = pos2.getX();
		double y1 = pos1.getY();
		double y2 = pos2.getY();
		double z1 = pos1.getZ();
		double z2 = pos2.getZ();

		return calculateDistance(x1, y1, z1, x2, y2, z2);
	}

	public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2)
	{
		return Math.sqrt(Math.pow(Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(z1 - z2, 2)), 2) + Math.pow(y1 - y2, 2));
	}

}

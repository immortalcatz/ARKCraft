package com.uberverse.arkcraft.common.item.explosives;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.uberverse.arkcraft.ARKCraft;
import com.uberverse.arkcraft.common.config.ModuleItemBalance;
import com.uberverse.arkcraft.common.item.ranged.ItemRangedWeapon;

public class ItemRocketLauncher extends ItemRangedWeapon
{
	public ItemRocketLauncher()
	{
		super("rocket_launcher", 250, 1, "rocket_propelled_grenade", 1, 4, 5F, 3F, 30, 300);
	}

	@Override
	public void soundCharge(ItemStack stack, World world, EntityPlayer player)
	{
		world.playSoundAtEntity(player, ARKCraft.MODID + ":" + "rocket_launcher_reload", 0.7F, 0.9F / (getItemRand()
				.nextFloat() * 0.2F + 0.0F));
	}

	@Override
	public int getReloadDuration()
	{
		return (int) (ModuleItemBalance.WEAPONS.ROCKET_LAUNCHER_RELOAD * 20.0);
	}

	@Override
	public void effectPlayer(ItemStack itemstack, EntityPlayer entityplayer, World world)
	{
		float f = entityplayer.isSneaking() ? -0.01F : -0.02F;
		double d = -MathHelper.sin((entityplayer.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((0 / 180F)
				* 3.141593F) * f;
		double d1 = MathHelper.cos((entityplayer.rotationYaw / 180F) * 3.141593F) * MathHelper.cos((0 / 180F)
				* 3.141593F) * f;
		entityplayer.rotationPitch -= entityplayer.isSneaking() ? 2.5F : 5F;
		entityplayer.addVelocity(d, 0, d1);
	}

	@Override
	public void effectShoot(ItemStack stack, World world, double x, double y, double z, float yaw, float pitch)
	{
		world.playSoundEffect(x, y, z, "random.explode", 3F, 1F / (this.getItemRand().nextFloat() * 0.4F + 0.7F));
		world.playSoundEffect(x, y, z, "ambient.weather.thunder", 3F, 1F / (this.getItemRand().nextFloat() * 0.4F
				+ 0.4F));

		float particleX = -MathHelper.sin(((yaw + 23) / 180F) * 3.141593F) * MathHelper.cos((pitch / 180F) * 3.141593F);
		float particleY = -MathHelper.sin((pitch / 180F) * 3.141593F) - 0.1F;
		float particleZ = MathHelper.cos(((yaw + 23) / 180F) * 3.141593F) * MathHelper.cos((pitch / 180F) * 3.141593F);

		for (int i = 0; i < 3; i++)
		{
			world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + particleX, y + particleY, z + particleZ, 0.0D, 0.0D,
					0.0D);
		}
		world.spawnParticle(EnumParticleTypes.FLAME, x + particleX, y + particleY, z + particleZ, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public void effectReloadDone(ItemStack stack, World world, EntityPlayer player)
	{
		world.playSoundAtEntity(player, "random.door_close", 1.2F, 1.0F / (this.getItemRand().nextFloat() * 0.2F
				+ 0.0F));
	}
}

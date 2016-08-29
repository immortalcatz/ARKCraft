package com.uberverse.arkcraft.rework;

import java.util.Queue;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public abstract class TileEntityEngramCrafter extends TileEntity
		implements IInventory, IUpdatePlayerListBox, IEngramCrafter
{
	private ItemStack[] inventory;

	private Queue<CraftingOrder> craftingQueue;

	private int progress;

	private String name;

	public TileEntityEngramCrafter(int size, String name)
	{
		inventory = new ItemStack[size];
		this.progress = 0;
		this.name = name;
		craftingQueue = new FixedSizeQueue<>(5);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTagCompound = new NBTTagCompound();
		writeToNBT(nbtTagCompound);
		return new S35PacketUpdateTileEntity(this.pos, 0, nbtTagCompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public boolean receiveClientEvent(int id, int type)
	{
		if (id == 0)
		{
			progress = type;
			return true;
		}
		else return super.receiveClientEvent(id, type);
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean hasCustomName()
	{
		return true;
	}

	@Override
	public IChatComponent getDisplayName()
	{
		return new ChatComponentText(name);
	}

	@Override
	public int getSizeInventory()
	{
		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int index)
	{
		return inventory[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		return inventory[index].splitStack(count);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int index)
	{
		return inventory[index];
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack)
	{
		inventory[index] = stack;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack)
	{
		return true;
	}

	@Override
	public int getField(int id)
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
	}

	@Override
	public int getFieldCount()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		for (int i = 0; i < inventory.length; i++)
			inventory[i] = null;
	}

	@Override
	public void update()
	{
		IEngramCrafter.super.update();
	}

	@Override
	public void syncProgress()
	{
		worldObj.addBlockEvent(pos, blockType, 0, progress);
		markDirty();
	}

	@Override
	public void sync()
	{
		worldObj.markBlockForUpdate(pos);
		markDirty();
	}

	@Override
	public IInventory getIInventory()
	{
		return this;
	}

	@Override
	public ItemStack[] getInventory()
	{
		return inventory;
	}

	@Override
	public int getProgress()
	{
		return progress;
	}

	@Override
	public void setProgress(int progress)
	{
		this.progress = progress;
	}

	@Override
	public BlockPos getPosition()
	{
		return pos;
	}

	@Override
	public Queue<CraftingOrder> getCraftingQueue()
	{
		return craftingQueue;
	}
}

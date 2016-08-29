package com.uberverse.arkcraft.rework;

import java.util.Collections;
import java.util.List;

import com.uberverse.arkcraft.ARKCraft;
import com.uberverse.arkcraft.common.container.scrollable.IContainerScrollable;
import com.uberverse.arkcraft.common.container.scrollable.SlotScrolling;
import com.uberverse.arkcraft.common.network.CraftMessage;
import com.uberverse.arkcraft.rework.EngramManager.Engram;
import com.uberverse.arkcraft.rework.EngramManager.EngramType;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.FMLCommonHandler;

public abstract class ContainerEngramCrafting extends ContainerScrollable
{
	private static final int SLOT_SIZE = 18;

	private EntityPlayer player;
	private EngramInventory engramInventory;
	private TileEntityEngramCrafter inventory;

	protected int playerInvBoundLeft, playerInvBoundRight, invBoundLeft, invBoundRight,
			scrollInvBoundLeft, scrollInvBoundRight;

	private int counter = 0;

	public ContainerEngramCrafting(EngramType type, EntityPlayer player, TileEntityEngramCrafter inventory)
	{
		super();
		this.player = player;
		this.inventory = inventory;
		this.engramInventory = new EngramInventory(
				EngramManager.instance().getUnlockedEngramsOfType(player, type));
		initPlayerSlots();
		initInventorySlots();
		initScrollableSlots();
		System.out.println(counter);
	}

	private void initInventorySlots()
	{
		invBoundLeft = counter;
		for (int row = 0; row < getInventorySlotsHeight(); row++)
		{
			for (int col = 0; col < getInventorySlotsWidth(); col++)
			{
				int index = col + row * getInventorySlotsWidth();
				this.addSlotToContainer(
						new Slot(getIInventory(), index, getInventorySlotsX() + col * getSlotSize(),
								getInventorySlotsY() + row * getSlotSize()));
				counter++;
			}
		}
		invBoundRight = counter;
	}

	private void initPlayerSlots()
	{
		playerInvBoundLeft = counter;
		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				int slotIndex = col + row * 9 + 9;
				addSlotToContainer(new Slot(player.inventory, slotIndex,
						getPlayerInventorySlotsX() + col * getSlotSize(),
						getPlayerInventorySlotsY() + row * getSlotSize()));
				counter++;
			}
		}

		for (int col = 0; col < 9; col++)
		{
			addSlotToContainer(new Slot(player.inventory, col,
					getPlayerHotbarSlotsX() + col * getSlotSize(), getPlayerHotbarSlotsY()));
			counter++;
		}

		playerInvBoundRight = counter;
	}

	@Override
	public void initScrollableSlots()
	{
		scrollInvBoundLeft = counter;
		for (int i = 0; i < getVisibleSlotsAmounts(); i++)
		{
			this.addSlotToContainer(new EngramSlot(getScrollableInventory(), i,
					getScrollableSlotsX() + i % getScrollableSlotsWidth() * getSlotSize(),
					getScrollableSlotsY() + i / getScrollableSlotsWidth() * getSlotSize(), this));
			counter++;
		}
		scrollInvBoundRight = counter;
	}

	public abstract int getPlayerInventorySlotsX();

	public abstract int getPlayerInventorySlotsY();

	public abstract int getPlayerHotbarSlotsX();

	public abstract int getPlayerHotbarSlotsY();

	public abstract int getInventorySlotsX();

	public abstract int getInventorySlotsY();

	public abstract int getInventorySlotsWidth();

	public abstract int getInventorySlotsHeight();

	public IInventory getIInventory()
	{
		return inventory;
	}

	@Override
	public int getSlotSize()
	{
		return SLOT_SIZE;
	}

	@Override
	public IInventory getScrollableInventory()
	{
		return engramInventory;
	}

	public IInventory getPlayerInventory()
	{
		return player.inventory;
	}

	private short selectedEngramId = -1;

	public void selectEngram(short engramId)
	{
		this.selectedEngramId = engramId;
		System.out.println(engramId);
	}

	public void craftOne()
	{
		if (FMLCommonHandler.instance().getSide()
				.isClient()) ARKCraft.modChannel.sendToServer(new CraftMessage(false));
		else if (FMLCommonHandler.instance().getSide()
				.isServer() && selectedEngramId >= 0) inventory.startCraft(selectedEngramId);
	}

	public void craftAll()
	{
		if (FMLCommonHandler.instance().getSide()
				.isClient()) ARKCraft.modChannel.sendToServer(new CraftMessage(true));
		else if (FMLCommonHandler.instance().getSide()
				.isServer() && selectedEngramId >= 0) inventory.startCraftAll(selectedEngramId);
	}

	public static class EngramSlot extends SlotScrolling
	{
		public EngramSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, IContainerScrollable container)
		{
			super(inventoryIn, index, xPosition, yPosition, container);
		}

		@Override
		public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
		{
			((ContainerEngramCrafting) getContainer()).selectEngram((short) getSlotIndex());
		}
	}

	public static class EngramInventory implements IInventory
	{
		private List<Engram> engrams;

		public EngramInventory(List<Engram> engrams)
		{
			this.engrams = engrams;
			Collections.sort(this.engrams);
		}

		@Override
		public String getName()
		{
			return "";
		}

		@Override
		public boolean hasCustomName()
		{
			return false;
		}

		@Override
		public IChatComponent getDisplayName()
		{
			return null;
		}

		@Override
		public int getSizeInventory()
		{
			return engrams.size();
		}

		@Override
		public ItemStack getStackInSlot(int index)
		{
			return new ItemStack(engrams.get(index).getItem());
		}

		@Override
		public ItemStack decrStackSize(int index, int count)
		{
			return null;
		}

		@Override
		public ItemStack getStackInSlotOnClosing(int index)
		{
			return null;
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack)
		{
		}

		@Override
		public int getInventoryStackLimit()
		{
			return Integer.MAX_VALUE;
		}

		@Override
		public void markDirty()
		{
		}

		@Override
		public boolean isUseableByPlayer(EntityPlayer player)
		{
			return false;
		}

		@Override
		public void openInventory(EntityPlayer player)
		{
		}

		@Override
		public void closeInventory(EntityPlayer player)
		{
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack)
		{
			return false;
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
		}
	}
}
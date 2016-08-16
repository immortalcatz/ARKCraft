package com.uberverse.arkcraft.common.block.container;

import com.uberverse.arkcraft.client.gui.GUIEngram;
import com.uberverse.arkcraft.common.container.scrollable.IContainerScrollable;
import com.uberverse.arkcraft.common.container.scrollable.SlotScrolling;
import com.uberverse.arkcraft.common.inventory.InventoryPlayerEngram;
import com.uberverse.arkcraft.common.item.engram.ARKCraftEngrams;
import com.uberverse.arkcraft.common.item.engram.Engram;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * @author ERBF
 *
 */
public class ContainerEngram extends Container implements IContainerScrollable
{

	public static final int SLOT_SPACING = 20;
	
	private static InventoryPlayerEngram invEngram;
	
	private int maxSlots;
	private int scrollingOffset = 0;
	
	public ContainerEngram(InventoryPlayerEngram inventory, EntityPlayer player)
	{
		invEngram = inventory;
		maxSlots = inventory.getSizeInventory();
		
		int index = 0;
		for (int y = 0; y < 8; ++y) {
	        for (int x = 0; x < 8; ++x) {
	            this.addSlotToContainer(new EngramSlot(inventory, index, 1 + x * 20, 44 + y * 20, this));
	            try {
	            	inventory.setInventorySlotContents(index, new ItemStack(ARKCraftEngrams.engramList.get(index)));
	            } catch (IndexOutOfBoundsException e) {}
	            index++;
	        }
	    }
	}
	
	public static InventoryPlayerEngram getEngramInventory()
	{
		return invEngram;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) 
	{
		return false;
	}

	@Override
	public int getScrollingOffset() {
		return this.scrollingOffset;
	}

	@Override
	public void scroll(int offset) {
		int newScrollingOffset = scrollingOffset + offset;
		if(isValidOffset(newScrollingOffset)) {
			scrollingOffset = newScrollingOffset;
			refreshLists();
		}
	}

	@Override
	public int getScrollableSlotsWidth() {
		return 18;
	}

	@Override
	public int getScrollableSlotsHeight() {
		return 18;
	}

	@Override
	public int getScrollableSlotsCount() {
		return 18 * 18;
	}

	@Override
	public int getRequiredSlotsCount() {
		return 32;
	}

	@Override
	public int getMaxOffset() {
		return 32 / getScrollableSlotsWidth() - getScrollableSlotsHeight() + 1;
	}

	@Override
	public double getRelativeScrollingOffset() {
		return (double) this.scrollingOffset / (double) getMaxOffset();
	}
	
	@SuppressWarnings("unchecked")
	private void refreshLists()
	{
		for(int i = 0; i < inventoryItemStacks.size(); i++) {
			Slot slot = (Slot) inventorySlots.get(i);
			if(slot instanceof EngramSlot) {
				inventoryItemStacks.set(i, slot.getStack());
			}
		}
	}
	
	private boolean isValidOffset(int offset)
	{
		int maxOffset = getMaxOffset();
		return canScroll() && offset >= 0 && offset <= maxOffset;
	}
	
	public boolean canScroll()
	{
		return this.maxSlots < 32;
	}
	
	public class EngramSlot extends SlotScrolling {

		public EngramSlot(InventoryPlayerEngram inventoryIn, int index, int xPosition, int yPosition, IContainerScrollable container) {
			super(inventoryIn, index, xPosition, yPosition, container);
		}
		
		@Override
		public boolean canTakeStack(EntityPlayer playerIn)
	    {
	        return true;
	    }
		
		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
		
		@Override
		public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack)
	    {
	        if(stack.getItem() instanceof Engram) {
	        	Engram engram = (Engram) stack.getItem();
				GUIEngram.setInformation(engram);
				ContainerEngram.getEngramInventory().setInventorySlotContents(this.getSlotIndex(), playerIn.inventory.getItemStack());
				playerIn.inventory.setItemStack(null);
	        }
	    }
		
	}
	
}

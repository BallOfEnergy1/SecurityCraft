package net.geforcemods.securitycraft.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemRestrictedSlot extends Slot {

	private final IInventory inventory;
	private final Item[] prohibitedItems;

	public ItemRestrictedSlot(IInventory inventory, int index, int xPos, int yPos, Item... prohibitedItems) {
		super(inventory, index, xPos, yPos);
		this.inventory = inventory;
		this.prohibitedItems = prohibitedItems;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		if(stack.getItem() == null) return false;

		// Only allows items not in prohibitedItems[] to be placed in the slot.
		for(Item prohibitedItem : prohibitedItems)
			if(stack.getItem() == prohibitedItem)
				return false;

		return true;
	}

	@Override
	public void set(ItemStack stack) {
		inventory.setItem(getSlotIndex(), stack);
		setChanged();
	}
}

package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.items.ModuleItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class ModuleItemContainer implements IInventory {
	public final int size = 1;
	private final ItemStack module;
	public NonNullList<ItemStack> moduleInventory;

	public ModuleItemContainer(ItemStack moduleItem) {
		module = moduleItem;

		if (!(moduleItem.getItem() instanceof ModuleItem))
			return;

		moduleInventory = NonNullList.withSize(size, ItemStack.EMPTY);

		if (!module.hasTag())
			module.setTag(new CompoundNBT());

		readFromNBT(module.getTag());
	}

	@Override
	public int getContainerSize() {
		return size;
	}

	@Override
	public ItemStack getItem(int index) {
		return moduleInventory.get(index);
	}

	public void readFromNBT(CompoundNBT tag) {
		ListNBT items = tag.getList("ItemInventory", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < items.size(); i++) {
			CompoundNBT item = items.getCompound(i);
			int slot = item.getInt("Slot");

			if (slot < getContainerSize())
				moduleInventory.set(slot, ItemStack.of(item));
		}
	}

	public void writeToNBT(CompoundNBT tag) {
		ListNBT items = new ListNBT();

		for (int i = 0; i < getContainerSize(); i++) {
			if (!getItem(i).isEmpty()) {
				CompoundNBT item = new CompoundNBT();
				item.putInt("Slot", i);
				getItem(i).save(item);

				items.add(item);
			}
		}

		tag.put("ItemInventory", items);
	}

	@Override
	public ItemStack removeItem(int index, int size) {
		ItemStack stack = getItem(index);

		if (!stack.isEmpty())  {
			if (stack.getCount() > size) {
				stack = stack.split(size);
				setChanged();
			}
			else
				setItem(index, ItemStack.EMPTY);
		}

		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		ItemStack stack = getItem(index);
		setItem(index, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		moduleInventory.set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > getMaxStackSize())
			stack.setCount(getMaxStackSize());

		setChanged();
	}

	@Override
	public int getMaxStackSize() {
		return 64;
	}

	@Override
	public void setChanged() {
		for (int i = 0; i < getContainerSize(); i++) {
			if (!getItem(i).isEmpty() && getItem(i).getCount() == 0)
				moduleInventory.set(i, ItemStack.EMPTY);
		}

		writeToNBT(module.getTag());
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}

	@Override
	public void startOpen(PlayerEntity player) {}

	@Override
	public void stopOpen(PlayerEntity player) {}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return true;
	}

	@Override
	public void clearContent() {}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : moduleInventory) {
			if (!stack.isEmpty())
				return false;
		}

		return true;
	}
}

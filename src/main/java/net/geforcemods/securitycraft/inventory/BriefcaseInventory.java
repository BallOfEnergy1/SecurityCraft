package net.geforcemods.securitycraft.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

public class BriefcaseInventory implements IInventory {
	public static final int SIZE = 12;
	private final ItemStack briefcase;
	private NonNullList<ItemStack> briefcaseInventory = NonNullList.<ItemStack> withSize(SIZE, ItemStack.EMPTY);

	public BriefcaseInventory(ItemStack briefcaseItem) {
		briefcase = briefcaseItem;

		if (!briefcase.hasTag())
			briefcase.setTag(new CompoundNBT());

		readFromNBT(briefcase.getTag());
	}

	@Override
	public int getContainerSize() {
		return SIZE;
	}

	@Override
	public ItemStack getItem(int index) {
		return briefcaseInventory.get(index);
	}

	public void readFromNBT(CompoundNBT tag) {
		ListNBT items = tag.getList("ItemInventory", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < items.size(); i++) {
			CompoundNBT item = items.getCompound(i);
			int slot = item.getInt("Slot");

			if (slot < getContainerSize())
				briefcaseInventory.set(slot, ItemStack.of(item));
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

		if (!stack.isEmpty()) {
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
	public void setItem(int index, ItemStack itemStack) {
		briefcaseInventory.set(index, itemStack);

		if (!itemStack.isEmpty() && itemStack.getCount() > getMaxStackSize())
			itemStack.setCount(getMaxStackSize());

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
				briefcaseInventory.set(i, ItemStack.EMPTY);
		}

		writeToNBT(briefcase.getTag());
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
	public boolean canPlaceItem(int index, ItemStack itemStack) {
		return true;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < SIZE; i++) {
			briefcaseInventory.set(i, ItemStack.EMPTY);
		}
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : briefcaseInventory) {
			if (!stack.isEmpty())
				return false;
		}

		return true;
	}
}

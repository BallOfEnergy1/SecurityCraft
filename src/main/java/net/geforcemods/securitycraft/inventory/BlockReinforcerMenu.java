package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IReinforcedBlock;
import net.geforcemods.securitycraft.items.UniversalBlockReinforcerItem;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class BlockReinforcerMenu extends Container {
	private final ItemStack blockReinforcer;
	private final Inventory itemInventory = new Inventory(2);
	public final SlotBlockReinforcer reinforcingSlot;
	public final SlotBlockReinforcer unreinforcingSlot;
	public final boolean isLvl1, isReinforcing;

	public BlockReinforcerMenu(int windowId, PlayerInventory inventory, boolean isLvl1) {
		super(SCContent.BLOCK_REINFORCER_MENU.get(), windowId);

		blockReinforcer = inventory.getSelected().getItem() instanceof UniversalBlockReinforcerItem ? inventory.getSelected() : inventory.offhand.get(0);
		this.isLvl1 = isLvl1;
		this.isReinforcing = UniversalBlockReinforcerItem.isReinforcing(blockReinforcer);

		//main player inventory
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new Slot(inventory, 9 + j + i * 9, 8 + j * 18, 104 + i * 18));
			}
		}

		//player hotbar
		for (int i = 0; i < 9; i++) {
			addSlot(new Slot(inventory, i, 8 + i * 18, 162));
		}

		addSlot(reinforcingSlot = new SlotBlockReinforcer(itemInventory, 0, 26, 20, true));

		if (!isLvl1)
			addSlot(unreinforcingSlot = new SlotBlockReinforcer(itemInventory, 1, 26, 45, false));
		else
			unreinforcingSlot = null;
	}

	@Override
	public boolean stillValid(PlayerEntity player) {
		return true;
	}

	@Override
	public void removed(PlayerEntity player) {
		super.removed(player);

		if (!player.isAlive() || player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).hasDisconnected()) {
			for (int slot = 0; slot < itemInventory.getContainerSize(); ++slot) {
				player.drop(itemInventory.removeItemNoUpdate(slot), false);
			}

			return;
		}

		if (!itemInventory.getItem(0).isEmpty()) {
			if (itemInventory.getItem(0).getCount() > reinforcingSlot.output.getCount()) { //if there's more in the slot than the reinforcer can reinforce (due to durability)
				ItemStack overflowStack = itemInventory.getItem(0).copy();

				overflowStack.setCount(itemInventory.getItem(0).getCount() - reinforcingSlot.output.getCount());
				player.drop(overflowStack, false);
			}

			player.drop(reinforcingSlot.output, false);
			blockReinforcer.hurtAndBreak(reinforcingSlot.output.getCount(), player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
		}

		if (!isLvl1 && !itemInventory.getItem(1).isEmpty()) {
			if (itemInventory.getItem(1).getCount() > unreinforcingSlot.output.getCount()) {
				ItemStack overflowStack = itemInventory.getItem(1).copy();

				overflowStack.setCount(itemInventory.getItem(1).getCount() - unreinforcingSlot.output.getCount());
				player.drop(overflowStack, false);
			}

			player.drop(unreinforcingSlot.output, false);
			blockReinforcer.hurtAndBreak(unreinforcingSlot.output.getCount(), player, p -> p.broadcastBreakEvent(p.getUsedItemHand()));
		}
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int id) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = slots.get(id);

		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();

			slotStackCopy = slotStack.copy();

			if (id >= 36) {
				if (!moveItemStackTo(slotStack, 0, 36, true))
					return ItemStack.EMPTY;

				slot.onQuickCraft(slotStack, slotStackCopy);
			}
			else if (!moveItemStackTo(slotStack, 36, fixSlot(38), false))
				return ItemStack.EMPTY;

			if (slotStack.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();

			if (slotStack.getCount() == slotStackCopy.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, slotStack);
		}

		return slotStackCopy;
	}

	private int fixSlot(int slot) {
		return isLvl1 ? slot - 1 : slot;
	}

	//edited to check if the item to be merged is valid in that slot
	@Override
	protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
		boolean merged = false;
		int currentIndex = startIndex;

		if (useEndIndex)
			currentIndex = endIndex - 1;

		Slot slot;
		ItemStack slotStack;

		if (stack.isStackable()) {
			while (stack.getCount() > 0 && (!useEndIndex && currentIndex < endIndex || useEndIndex && currentIndex >= startIndex)) {
				slot = slots.get(currentIndex);
				slotStack = slot.getItem();

				if (!slotStack.isEmpty() && consideredTheSameItem(stack, slotStack) && slot.mayPlace(stack)) {
					int combinedCount = slotStack.getCount() + stack.getCount();

					if (combinedCount <= stack.getMaxStackSize()) {
						stack.setCount(0);
						slotStack.setCount(combinedCount);
						slot.setChanged();
						merged = true;
					}
					else if (slotStack.getCount() < stack.getMaxStackSize()) {
						stack.shrink(stack.getMaxStackSize() - slotStack.getCount());
						slotStack.setCount(stack.getMaxStackSize());
						slot.setChanged();
						merged = true;
					}
				}

				if (useEndIndex)
					--currentIndex;
				else
					++currentIndex;
			}
		}

		if (stack.getCount() > 0) {
			if (useEndIndex) {
				currentIndex = endIndex - 1;
			}
			else {
				currentIndex = startIndex;
			}

			while (!useEndIndex && currentIndex < endIndex || useEndIndex && currentIndex >= startIndex) {
				slot = slots.get(currentIndex);
				slotStack = slot.getItem();

				if (slotStack.isEmpty() && slot.mayPlace(stack)) {
					slot.set(stack.copy());
					slot.setChanged();
					stack.setCount(0);
					merged = true;
					break;
				}

				if (useEndIndex)
					--currentIndex;
				else
					++currentIndex;
			}
		}

		return merged;
	}

	@Override
	public ItemStack clicked(int slot, int dragType, ClickType clickType, PlayerEntity player) {
		if (slot >= 0 && getSlot(slot) != null && getSlot(slot).getItem().getItem() instanceof UniversalBlockReinforcerItem)
			return ItemStack.EMPTY;

		return super.clicked(slot, dragType, clickType, player);
	}

	public class SlotBlockReinforcer extends Slot {
		private final boolean reinforce;
		private ItemStack output = ItemStack.EMPTY;

		public SlotBlockReinforcer(IInventory inventory, int index, int x, int y, boolean reinforce) {
			super(inventory, index, x, y);

			this.reinforce = reinforce;
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			//can only reinforce OR unreinforce at once
			if (!itemInventory.getItem((index + 1) % 2).isEmpty())
				return false;

			return (reinforce ? IReinforcedBlock.VANILLA_TO_SECURITYCRAFT : IReinforcedBlock.SECURITYCRAFT_TO_VANILLA).containsKey(Block.byItem(stack.getItem()));
		}

		@Override
		public void setChanged() {
			ItemStack stack = itemInventory.getItem(index % 2);

			if (!stack.isEmpty()) {
				Block block = (reinforce ? IReinforcedBlock.VANILLA_TO_SECURITYCRAFT : IReinforcedBlock.SECURITYCRAFT_TO_VANILLA).get(Block.byItem(stack.getItem()));

				if (block != null) {
					boolean isLvl3 = blockReinforcer.getItem() == SCContent.UNIVERSAL_BLOCK_REINFORCER_LVL_3.get();

					output = new ItemStack(block);
					output.setCount(isLvl3 ? stack.getCount() : Math.min(stack.getCount(), blockReinforcer.getMaxDamage() - blockReinforcer.getDamageValue()));
				}
			}
		}

		public ItemStack getOutput() {
			return output;
		}
	}
}

package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.ProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ProjectorMenu extends AbstractContainerMenu {
	public static final int SIZE = 1;
	public ProjectorBlockEntity be;
	private ContainerLevelAccess worldPosCallable;

	public ProjectorMenu(int windowId, Level level, BlockPos pos, Inventory inventory) {
		super(SCContent.mTypeProjector, windowId);

		if (level.getBlockEntity(pos) instanceof ProjectorBlockEntity be)
			this.be = be;

		worldPosCallable = ContainerLevelAccess.create(level, pos);

		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; ++x) {
				addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 + 59));
			}
		}

		for (int x = 0; x < 9; x++) {
			addSlot(new Slot(inventory, x, 8 + x * 18, 142 + 59));
		}

		// A custom slot that prevents non-Block items from being inserted into the projector
		addSlot(new Slot(be, 36, 79, 23) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return stack.getItem() instanceof BlockItem;
			}
		});
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack slotStackCopy = ItemStack.EMPTY;
		Slot slot = slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			slotStackCopy = slotStack.copy();

			if (index == 36) {
				if (!moveItemStackTo(slotStack, 0, 36, false))
					return ItemStack.EMPTY;
			}
			else {
				if (!moveItemStackTo(slotStack, 36, 37, false))
					return ItemStack.EMPTY;
			}

			if (slotStack.getCount() == 0)
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();

			if (slotStack.getCount() == slotStack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(player, slotStack);
		}

		return slotStackCopy;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(worldPosCallable, player, SCContent.PROJECTOR.get());
	}
}

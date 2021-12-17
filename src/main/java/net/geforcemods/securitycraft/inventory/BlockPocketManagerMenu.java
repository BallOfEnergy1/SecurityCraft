package net.geforcemods.securitycraft.inventory;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.blockentities.BlockPocketManagerBlockEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.SlotItemHandler;

public class BlockPocketManagerMenu extends AbstractContainerMenu {
	public BlockPocketManagerBlockEntity be;
	private ContainerLevelAccess worldPosCallable;
	public final boolean storage;
	public final boolean isOwner;

	public BlockPocketManagerMenu(int windowId, Level level, BlockPos pos, Inventory inventory) {
		super(SCContent.mTypeBlockPocketManager, windowId);

		if (level.getBlockEntity(pos) instanceof BlockPocketManagerBlockEntity be) {
			this.be = be;
		}

		worldPosCallable = ContainerLevelAccess.create(level, pos);
		isOwner = be.getOwner().isOwner(inventory.player);
		storage = be != null && be.hasModule(ModuleType.STORAGE) && isOwner;

		if (storage) {
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; ++x) {
					addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18 + 74));
				}
			}

			for (int x = 0; x < 9; x++) {
				addSlot(new Slot(inventory, x, 8 + x * 18, 142 + 74));
			}

			be.getStorageHandler().ifPresent(storage -> {
				int slotId = 0;

				for (int y = 0; y < 8; y++) {
					for (int x = 0; x < 7; x++) {
						addSlot(new SlotItemHandler(storage, slotId++, 124 + x * 18, 8 + y * 18));
					}
				}
			});
		}
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();

			copy = slotStack.copy();

			if (index >= 36) { //block pocket manager slots
				if (!moveItemStackTo(slotStack, 0, 36, true))
					return ItemStack.EMPTY;
			}
			else if (index >= 0 && index <= 35) { //main inventory and hotbar
				if (!moveItemStackTo(slotStack, 36, slots.size(), false))
					return ItemStack.EMPTY;
			}

			if (slotStack.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
		}

		return copy;
	}

	@Override
	public boolean stillValid(Player player) {
		return stillValid(worldPosCallable, player, SCContent.BLOCK_POCKET_MANAGER.get());
	}
}

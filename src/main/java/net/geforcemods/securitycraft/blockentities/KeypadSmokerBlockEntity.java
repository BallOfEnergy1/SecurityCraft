package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.inventory.KeypadSmokerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

public class KeypadSmokerBlockEntity extends AbstractKeypadFurnaceBlockEntity {
	public KeypadSmokerBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.beTypeKeypadSmoker, pos, state, RecipeType.SMOKING);
	}

	@Override
	protected int getBurnDuration(ItemStack fuel) {
		return super.getBurnDuration(fuel) / 2;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inv) {
		return new KeypadSmokerMenu(windowId, level, worldPosition, inv, this, dataAccess);
	}
}

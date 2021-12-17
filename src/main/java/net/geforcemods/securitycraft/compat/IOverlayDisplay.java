package net.geforcemods.securitycraft.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IOverlayDisplay {
	public ItemStack getDisplayStack(Level level, BlockState state, BlockPos pos);

	public boolean shouldShowSCInfo(Level level, BlockState state, BlockPos pos);
}

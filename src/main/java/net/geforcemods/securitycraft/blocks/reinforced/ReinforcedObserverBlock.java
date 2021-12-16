package net.geforcemods.securitycraft.blocks.reinforced;

import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;

public class ReinforcedObserverBlock extends ObserverBlock implements IReinforcedBlock, EntityBlock
{
	public ReinforcedObserverBlock(Block.Properties properties)
	{
		super(properties);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction side)
	{
		return side == state.getValue(ObserverBlock.FACING);
	}

	@Override
	public Block getVanillaBlock()
	{
		return Blocks.OBSERVER;
	}

	@Override
	public BlockState getConvertedState(BlockState vanillaState)
	{
		return defaultBlockState().setValue(FACING, vanillaState.getValue(FACING)).setValue(POWERED, vanillaState.getValue(POWERED));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if(placer instanceof Player player)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(level, pos, player));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new OwnableBlockEntity(pos, state);
	}
}

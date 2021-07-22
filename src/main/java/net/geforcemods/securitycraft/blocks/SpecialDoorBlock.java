package net.geforcemods.securitycraft.blocks;

import java.util.Random;

import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

public abstract class SpecialDoorBlock extends DoorBlock
{
	public SpecialDoorBlock(Block.Properties properties)
	{
		super(properties);
	}

	@Override
	public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean flag)
	{
		onNeighborChanged(world, pos, fromPos);
	}

	@Override
	public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if(placer instanceof PlayerEntity)
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (PlayerEntity)placer));

		super.setPlacedBy(world, pos, state, placer, stack);
	}

	/**
	 * Old method, renamed because I am lazy. Called by neighborChanged
	 * @param world The world the change occured in
	 * @param pos The position of this block
	 * @param neighbor The position of the changed block
	 */
	public void onNeighborChanged(World world, BlockPos pos, BlockPos neighbor)
	{
		BlockState state = world.getBlockState(pos);
		Block neighborBlock = world.getBlockState(neighbor).getBlock();

		if(state.getValue(HALF) == DoubleBlockHalf.UPPER)
		{
			BlockPos blockBelow = pos.below();
			BlockState stateBelow = world.getBlockState(blockBelow);

			if(stateBelow.getBlock() != this)
				world.destroyBlock(pos, false);
			else if (neighborBlock != this)
				onNeighborChanged(world, blockBelow, neighbor);
		}
		else
		{
			boolean drop = false;
			BlockPos blockBelow = pos.above();
			BlockState stateBelow = world.getBlockState(blockBelow);

			if(stateBelow.getBlock() != this)
			{
				world.destroyBlock(pos, false);
				drop = true;
			}

			if(!world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP))
			{
				world.destroyBlock(pos, false);
				drop = true;

				if(stateBelow.getBlock() == this)
					world.destroyBlock(blockBelow, false);
			}

			if(drop)
			{
				if(!world.isClientSide)
				{
					world.destroyBlock(pos, false);
					Block.popResource(world, pos, new ItemStack(getDoorItem()));
				}
			}
		}
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random rand)
	{
		BlockState upperState = world.getBlockState(pos);
		BlockState lowerState;

		if(upperState.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
		{
			lowerState = upperState;
			pos = pos.above();
			upperState = world.getBlockState(pos);
		}
		else
			lowerState = world.getBlockState(pos.below());

		world.setBlock(pos, upperState.setValue(DoorBlock.OPEN, false), 3);
		world.setBlock(pos.below(), lowerState.setValue(DoorBlock.OPEN, false), 3);
		world.levelEvent(null, 1011, pos, 0);
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		super.onRemove(state, world, pos, newState, isMoving);

		if(state.getBlock() != newState.getBlock())
			world.removeBlockEntity(pos);
	}

	@Override
	public boolean triggerEvent(BlockState state, World world, BlockPos pos, int id, int param)
	{
		super.triggerEvent(state, world, pos, id, param);

		TileEntity tileentity = world.getBlockEntity(pos);

		return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		return new ItemStack(getDoorItem());
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state)
	{
		return PushReaction.BLOCK;
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

	public abstract Item getDoorItem();
}

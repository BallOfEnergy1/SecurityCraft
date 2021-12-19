package net.geforcemods.securitycraft.blocks;

import java.util.Random;

import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockSpecialDoor extends BlockDoor implements ITileEntityProvider {
	public BlockSpecialDoor(Material material) {
		super(material);
		setSoundType(SoundType.METAL);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (placer instanceof EntityPlayer) {
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (EntityPlayer) placer));
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
		onNeighborChanged(world, pos, fromPos);
	}

	/**
	 * Old method, renamed because I am lazy. Called by neighborChanged
	 *
	 * @param world The world the change occured in
	 * @param pos The position of this block
	 * @param neighbor The position of the changed block
	 */
	public void onNeighborChanged(IBlockAccess access, BlockPos pos, BlockPos neighbor) {
		World world = (World) access;
		IBlockState state = world.getBlockState(pos);
		Block neighborBlock = world.getBlockState(neighbor).getBlock();

		if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) {
			BlockPos blockBelow = pos.down();
			IBlockState stateBelow = world.getBlockState(blockBelow);

			if (stateBelow.getBlock() != this)
				world.setBlockToAir(pos);
			else if (neighborBlock != this)
				onNeighborChanged(world, blockBelow, neighbor);
		}
		else {
			boolean drop = false;
			BlockPos blockBelow = pos.up();
			IBlockState stateBelow = world.getBlockState(blockBelow);

			if (stateBelow.getBlock() != this) {
				world.setBlockToAir(pos);
				drop = true;
			}

			if (!world.isSideSolid(pos.down(), EnumFacing.UP)) {
				world.setBlockToAir(pos);
				drop = true;

				if (stateBelow.getBlock() == this)
					world.setBlockToAir(blockBelow);
			}

			if (drop && !world.isRemote)
				dropBlockAsItem(world, pos, state, 0);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
		IBlockState upperState = world.getBlockState(pos);

		if (!upperState.getValue(BlockDoor.OPEN))
			return;

		IBlockState lowerState;

		if (upperState.getValue(BlockDoor.HALF) == EnumDoorHalf.LOWER) {
			lowerState = upperState;
			pos = pos.up();
			upperState = world.getBlockState(pos);
		}
		else
			lowerState = world.getBlockState(pos.down());

		world.setBlockState(pos, upperState.withProperty(BlockDoor.OPEN, false), 3);
		world.setBlockState(pos.down(), lowerState.withProperty(BlockDoor.OPEN, false), 3);
		world.playEvent(null, 1011, pos, 0);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}

	@Override
	public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int param) {
		super.eventReceived(state, world, pos, id, param);

		TileEntity tileentity = world.getTileEntity(pos);

		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(getDoorItem());
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER ? null : getDoorItem();
	}

	@Override
	public EnumPushReaction getPushReaction(IBlockState state) {
		return EnumPushReaction.BLOCK;
	}

	@Override
	public abstract TileEntity createNewTileEntity(World world, int meta);

	public abstract Item getDoorItem();
}

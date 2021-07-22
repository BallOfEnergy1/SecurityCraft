package net.geforcemods.securitycraft.blocks;

import java.util.Random;

import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.tileentity.RetinalScannerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;

public class RetinalScannerBlock extends DisguisableBlock {

	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public RetinalScannerBlock(Block.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
	}

	/**
	 * Called when the block is placed in the world.
	 */
	@Override
	public void setPlacedBy(World world, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack)
	{
		if (entity instanceof PlayerEntity)
		{
			TileEntity tileentity = world.getBlockEntity(pos);
			if (!world.isClientSide && tileentity instanceof RetinalScannerTileEntity)
			{
				((RetinalScannerTileEntity)tileentity).setPlayerProfile(((PlayerEntity)entity).getGameProfile());
			}
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (PlayerEntity)entity));
		}
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random)
	{
		if (!world.isClientSide && state.getValue(POWERED))
			world.setBlockAndUpdate(pos, state.setValue(POWERED, false));
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change based on its state.
	 */
	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	@Override
	public boolean shouldCheckWeakPower(BlockState state, IWorldReader world, BlockPos pos, Direction side)
	{
		return false;
	}

	/**
	 * Returns true if the block is emitting indirect/weak redstone power on the specified side. If isBlockNormalCube
	 * returns true, standard redstone propagation rules will apply instead and this will not be called. Args: World, X,
	 * Y, Z, side. Note that the side is reversed - eg it is 1 (up) when checking the bottom of the block.
	 */
	@Override
	public int getSignal(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		if(blockState.getValue(POWERED))
			return 15;
		else
			return 0;
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx)
	{
		return getStateForPlacement(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace(), ctx.getClickLocation().x, ctx.getClickLocation().y, ctx.getClickLocation().z, ctx.getPlayer());
	}

	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, double hitX, double hitY, double hitZ, PlayerEntity placer)
	{
		return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite()).setValue(POWERED, false);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
		builder.add(POWERED);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new RetinalScannerTileEntity().activatedByView();
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror)
	{
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}

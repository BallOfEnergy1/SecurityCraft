package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.TileEntityOwnable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockReinforcedMycelium extends BlockMycelium implements IReinforcedBlock
{
	public BlockReinforcedMycelium()
	{
		setSoundType(SoundType.GROUND);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		Block block = world.getBlockState(pos.up()).getBlock();

		state = state.withProperty(SNOWY, block == Blocks.SNOW || block == Blocks.SNOW_LAYER || block == SCContent.reinforcedSnowBlock);

		return state;
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return new TileEntityOwnable();
	}

	@Override
	public List<Block> getVanillaBlocks()
	{
		return Arrays.asList(Blocks.MYCELIUM);
	}

	@Override
	public int getAmount()
	{
		return 1;
	}
}
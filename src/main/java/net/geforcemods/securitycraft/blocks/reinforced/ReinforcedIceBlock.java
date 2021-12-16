package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedIceBlock extends BaseReinforcedBlock
{
	public ReinforcedIceBlock(Properties properties, Block vB)
	{
		super(properties, vB);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state)
	{
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, Random rand)
	{
		if(level.getBrightness(LightLayer.BLOCK, pos) > 11 - state.getLightBlock(level, pos))
		{
			if(level.dimensionType().ultraWarm())
				level.removeBlock(pos, false);
			else
			{
				level.setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
				level.neighborChanged(pos, Blocks.WATER, pos);
			}
		}
	}
}

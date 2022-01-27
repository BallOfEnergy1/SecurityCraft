package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Random;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IGrowable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.Features;
import net.minecraft.world.gen.feature.NetherVegetationFeature;
import net.minecraft.world.gen.feature.TwistingVineFeature;
import net.minecraft.world.server.ServerWorld;

public class ReinforcedNyliumBlock extends BaseReinforcedBlock implements IGrowable {
	public ReinforcedNyliumBlock(Block.Properties properties, Block vB) {
		super(properties, vB);
	}

	@Override
	public boolean isValidBonemealTarget(IBlockReader world, BlockPos pos, BlockState state, boolean flag) {
		return world.getBlockState(pos.above()).isAir(world, pos);
	}

	@Override
	public boolean isBonemealSuccess(World world, Random random, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(ServerWorld world, Random random, BlockPos pos, BlockState blockState) {
		BlockState state = world.getBlockState(pos);
		BlockPos upperPos = pos.above();

		if (state.is(SCContent.REINFORCED_CRIMSON_NYLIUM.get()))
			NetherVegetationFeature.place(world, random, upperPos, Features.Configs.CRIMSON_FOREST_CONFIG, 3, 1);
		else if (state.is(SCContent.REINFORCED_WARPED_NYLIUM.get())) {
			NetherVegetationFeature.place(world, random, upperPos, Features.Configs.WARPED_FOREST_CONFIG, 3, 1);
			NetherVegetationFeature.place(world, random, upperPos, Features.Configs.NETHER_SPROUTS_CONFIG, 3, 1);

			if (random.nextInt(8) == 0)
				TwistingVineFeature.place(world, random, upperPos, 3, 1, 2);
		}
	}
}

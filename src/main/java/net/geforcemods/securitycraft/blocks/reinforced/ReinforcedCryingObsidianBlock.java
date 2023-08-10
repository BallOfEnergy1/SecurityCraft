package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Random;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReinforcedCryingObsidianBlock extends BaseReinforcedBlock {
	public ReinforcedCryingObsidianBlock(AbstractBlock.Properties properties, Block vB) {
		super(properties, vB);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random rand) {
		if (rand.nextInt(5) == 0) {
			Direction direction = Direction.getRandom(rand);

			if (direction != Direction.UP) {
				BlockPos offsetPos = pos.relative(direction);
				BlockState offsetState = world.getBlockState(offsetPos);

				if (!state.canOcclude() || !offsetState.isFaceSturdy(world, offsetPos, direction.getOpposite())) {
					double xOffset = direction.getStepX() == 0 ? rand.nextDouble() : 0.5D + direction.getStepX() * 0.6D;
					double yOffset = direction.getStepY() == 0 ? rand.nextDouble() : 0.5D + direction.getStepY() * 0.6D;
					double zOffset = direction.getStepZ() == 0 ? rand.nextDouble() : 0.5D + direction.getStepZ() * 0.6D;

					world.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, 0.0D, 0.0D, 0.0D);
				}
			}
		}
	}
}

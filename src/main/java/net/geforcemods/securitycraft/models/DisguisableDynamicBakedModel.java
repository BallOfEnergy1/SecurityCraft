package net.geforcemods.securitycraft.models;

import java.util.List;
import java.util.Random;

import javax.annotation.Nonnull;

import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

public class DisguisableDynamicBakedModel implements IDynamicBakedModel {
	public static final ModelProperty<ResourceLocation> DISGUISED_BLOCK_RL = new ModelProperty<>();
	private final ResourceLocation defaultStateRl;
	private final BakedModel oldModel;

	public DisguisableDynamicBakedModel(ResourceLocation defaultStateRl, BakedModel oldModel) {
		this.defaultStateRl = defaultStateRl;
		this.oldModel = oldModel;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData modelData) {
		ResourceLocation rl = modelData.getData(DISGUISED_BLOCK_RL);

		if (rl != defaultStateRl) {
			Block block = ForgeRegistries.BLOCKS.getValue(rl);

			if (block != null) {
				final BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState());

				if (model != null && model != this)
					return model.getQuads(block.defaultBlockState(), side, rand, modelData);
			}
		}

		return oldModel.getQuads(state, side, rand, modelData);
	}

	@Override
	public TextureAtlasSprite getParticleIcon(IModelData modelData) {
		ResourceLocation rl = modelData.getData(DISGUISED_BLOCK_RL);

		if (rl != defaultStateRl) {
			Block block = ForgeRegistries.BLOCKS.getValue(rl);

			if (block != null && !(block instanceof DisguisableBlock))
				return Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState()).getParticleIcon(modelData);
		}

		return oldModel.getParticleIcon(modelData);
	}

	@Override
	@Nonnull
	public IModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, IModelData tileData) {
		Block block = level.getBlockEntity(pos).getBlockState().getBlock();

		if (block instanceof DisguisableBlock disguisedBlock) {
			BlockState disguisedState = disguisedBlock.getDisguisedBlockState(level, pos);

			if (disguisedState != null) {
				tileData.setData(DISGUISED_BLOCK_RL, disguisedState.getBlock().getRegistryName());
				return tileData;
			}
		}

		tileData.setData(DISGUISED_BLOCK_RL, defaultStateRl);
		return tileData;
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return oldModel.getParticleIcon();
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return false;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public ItemOverrides getOverrides() {
		return null;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}
}

package net.geforcemods.securitycraft.models;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.annotation.Nonnull;

import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.registries.ForgeRegistries;

public class DisguisableDynamicBakedModel implements IDynamicBakedModel {
	public static final ModelProperty<ResourceLocation> DISGUISED_BLOCK_RL = new ModelProperty<>();
	private final ResourceLocation defaultStateRl;
	private final IBakedModel oldModel;

	public DisguisableDynamicBakedModel(ResourceLocation defaultStateRl, IBakedModel oldModel) {
		this.defaultStateRl = defaultStateRl;
		this.oldModel = oldModel;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, Random rand, IModelData modelData) {
		ResourceLocation rl = modelData.getData(DISGUISED_BLOCK_RL);

		if (rl != null && rl != defaultStateRl) {
			Block block = ForgeRegistries.BLOCKS.getValue(rl);

			if (block != null) {
				final IBakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState());

				if (model != null && model != this)
					return model.getQuads(block.defaultBlockState(), side, rand, modelData);
			}
		}

		return oldModel.getQuads(state, side, rand, modelData);
	}

	@Override
	public TextureAtlasSprite getParticleTexture(IModelData modelData) {
		ResourceLocation rl = modelData.getData(DISGUISED_BLOCK_RL);

		if (rl != null && rl != defaultStateRl) {
			Block block = ForgeRegistries.BLOCKS.getValue(rl);

			if (block != null && !(block instanceof DisguisableBlock))
				return Minecraft.getInstance().getBlockRenderer().getBlockModel(block.defaultBlockState()).getParticleTexture(modelData);
		}

		return oldModel.getParticleTexture(modelData);
	}

	@Override
	@Nonnull
	public IModelData getModelData(IBlockDisplayReader world, BlockPos pos, BlockState state, IModelData tileData) {
		TileEntity te = world.getBlockEntity(pos);

		if (te != null) {
			Block block = te.getBlockState().getBlock();

			if (block instanceof DisguisableBlock) {
				Optional<BlockState> disguisedState = ((DisguisableBlock) block).getDisguisedBlockState(world, pos);

				if (disguisedState.isPresent()) {
					tileData.setData(DISGUISED_BLOCK_RL, disguisedState.get().getBlock().getRegistryName());
					return tileData;
				}
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
	public ItemOverrideList getOverrides() {
		return null;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}
}

package net.geforcemods.securitycraft.datagen;

import java.lang.reflect.Field;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blocks.mines.BaseFullMineBlock;
import net.geforcemods.securitycraft.blocks.mines.RedstoneOreMineBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedCarpetBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPaneBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedSlabBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedStainedGlassBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedStainedGlassPaneBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedStairsBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedWallBlock;
import net.geforcemods.securitycraft.util.RegisterItemBlock;
import net.geforcemods.securitycraft.util.RegisterItemBlock.SCItemGroup;
import net.geforcemods.securitycraft.util.Reinforced;
import net.minecraft.block.Block;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ExistingFileHelper;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.fml.RegistryObject;

public class BlockModelAndStateGenerator extends BlockStateProvider
{
	public BlockModelAndStateGenerator(DataGenerator gen, ExistingFileHelper exFileHelper)
	{
		super(gen, SecurityCraft.MODID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels()
	{
		for(Field field : SCContent.class.getFields())
		{
			try
			{
				if(field.isAnnotationPresent(Reinforced.class))
				{
					Block block = ((RegistryObject<Block>)field.get(null)).get();

					if(block instanceof ReinforcedSlabBlock)
						reinforcedSlabBlock(block);
					else if(block instanceof ReinforcedStainedGlassBlock)
						simpleBlock(block);
					else if(block instanceof ReinforcedStainedGlassPaneBlock)
						reinforcedPaneBlock((ReinforcedPaneBlock)block);
					else if(block instanceof ReinforcedStairsBlock)
						reinforcedStairsBlock(block);
					else if(block instanceof ReinforcedWallBlock)
						reinforcedWallBlock(block);
					else if(block instanceof ReinforcedCarpetBlock)
						reinforcedCarpetBlock(block);
				}
				else if(field.isAnnotationPresent(RegisterItemBlock.class) && field.getAnnotation(RegisterItemBlock.class).value() == SCItemGroup.EXPLOSIVES)
				{
					Block block = ((RegistryObject<Block>)field.get(null)).get();

					if(block instanceof BaseFullMineBlock)
						blockMine(((BaseFullMineBlock)block).getBlockDisguisedAs(), block);
				}
			}
			catch(IllegalArgumentException | IllegalAccessException e)
			{
				e.printStackTrace();
			}
		}

		horizontalBlock(SCContent.FURNACE_MINE.get(), new ResourceLocation(ModelProvider.BLOCK_FOLDER + "/furnace_side"), new ResourceLocation(ModelProvider.BLOCK_FOLDER + "/furnace_front"), new ResourceLocation(ModelProvider.BLOCK_FOLDER + "/furnace_top"));
		getVariantBuilder(SCContent.REDSTONE_ORE_MINE.get()).forAllStates(state -> {
			if(state.get(RedstoneOreMineBlock.LIT))
				return new ConfiguredModel[] {new ConfiguredModel(getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/redstone_ore_on")))};
			else
				return new ConfiguredModel[] {new ConfiguredModel(getExistingFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/redstone_ore")))};
		});

		simpleBlock(SCContent.REINFORCED_GLASS.get());
		reinforcedPaneBlock((ReinforcedPaneBlock)SCContent.REINFORCED_GLASS_PANE.get());

		reinforcedColumn("reinforced_smooth_stone_slab_double", "smooth_stone_slab_side", "smooth_stone");

		reinforcedSlabBlock(SCContent.REINFORCED_OAK_SLAB.get(), "reinforced_planks_oak", "oak_planks");
		reinforcedSlabBlock(SCContent.REINFORCED_SPRUCE_SLAB.get(), "reinforced_planks_spruce", "spruce_planks");
		reinforcedSlabBlock(SCContent.REINFORCED_BIRCH_SLAB.get(), "reinforced_planks_birch", "birch_planks");
		reinforcedSlabBlock(SCContent.REINFORCED_JUNGLE_SLAB.get(), "reinforced_planks_jungle", "jungle_planks");
		reinforcedSlabBlock(SCContent.REINFORCED_ACACIA_SLAB.get(), "reinforced_planks_acacia", "acacia_planks");
		reinforcedSlabBlock(SCContent.REINFORCED_DARK_OAK_SLAB.get(), "reinforced_planks_dark_oak", "dark_oak_planks");
		reinforcedSlabBlock(SCContent.REINFORCED_NORMAL_STONE_SLAB.get(), "reinforced_stone", "stone");
		reinforcedSlabBlock(SCContent.REINFORCED_SMOOTH_STONE_SLAB.get(), "reinforced_smooth_stone_slab_double", "smooth_stone_slab_side", "smooth_stone");
		reinforcedSlabBlock(SCContent.REINFORCED_SANDSTONE_SLAB.get(), "reinforced_sandstone_normal", "sandstone", "sandstone_bottom", "sandstone_top");
		reinforcedSlabBlock(SCContent.REINFORCED_CUT_SANDSTONE_SLAB.get(), "reinforced_sandstone_smooth", "cut_sandstone", "sandstone_top");
		reinforcedSlabBlock(SCContent.REINFORCED_BRICK_SLAB.get(), "reinforced_brick", "bricks");
		reinforcedSlabBlock(SCContent.REINFORCED_STONE_BRICK_SLAB.get(), "reinforced_stone_brick_default", "stone_bricks");
		reinforcedSlabBlock(SCContent.REINFORCED_NETHER_BRICK_SLAB.get(), "reinforced_nether_brick", "nether_bricks");
		reinforcedSlabBlock(SCContent.REINFORCED_QUARTZ_SLAB.get(), "reinforced_quartz_default", "quartz_block_side", "quartz_block_top");
		reinforcedSlabBlock(SCContent.REINFORCED_RED_SANDSTONE_SLAB.get(), "reinforced_red_sandstone_default", "red_sandstone", "red_sandstone_bottom", "red_sandstone_top");
		reinforcedSlabBlock(SCContent.REINFORCED_CUT_RED_SANDSTONE_SLAB.get(), "reinforced_red_sandstone_smooth", "cut_red_sandstone", "red_sandstone_top");
		reinforcedSlabBlock(SCContent.REINFORCED_PURPUR_SLAB.get(), "reinforced_purpur_default", "purpur_block");
		reinforcedSlabBlock(SCContent.REINFORCED_PRISMARINE_SLAB.get(), "reinforced_prismarine_default", "prismarine");
		reinforcedSlabBlock(SCContent.REINFORCED_PRISMARINE_BRICK_SLAB.get(), "reinforced_prismarine_bricks", "prismarine_bricks");
		reinforcedSlabBlock(SCContent.REINFORCED_DARK_PRISMARINE_SLAB.get(), "reinforced_prismarine_dark", "dark_prismarine");
		reinforcedSlabBlock(SCContent.REINFORCED_POLISHED_GRANITE_SLAB.get(), "reinforced_stone_smooth_granite", "polished_granite");
		reinforcedSlabBlock(SCContent.REINFORCED_SMOOTH_RED_SANDSTONE_SLAB.get(), "reinforced_smooth_red_sandstone", "red_sandstone_top");
		reinforcedSlabBlock(SCContent.REINFORCED_MOSSY_STONE_BRICK_SLAB.get(), "reinforced_stone_brick_mossy", "mossy_stone_bricks");
		reinforcedSlabBlock(SCContent.REINFORCED_POLISHED_DIORITE_SLAB.get(), "reinforced_stone_smooth_diorite", "polished_diorite");
		reinforcedSlabBlock(SCContent.REINFORCED_END_STONE_BRICK_SLAB.get(), "reinforced_end_stone_bricks", "end_stone_bricks");
		reinforcedSlabBlock(SCContent.REINFORCED_SMOOTH_SANDSTONE_SLAB.get(), "reinforced_smooth_sandstone", "sandstone_top");
		reinforcedSlabBlock(SCContent.REINFORCED_SMOOTH_QUARTZ_SLAB.get(), "reinforced_smooth_quartz", "quartz_block_bottom");
		reinforcedSlabBlock(SCContent.REINFORCED_GRANITE_SLAB.get(), "reinforced_stone_granite", "granite");
		reinforcedSlabBlock(SCContent.REINFORCED_ANDESITE_SLAB.get(), "reinforced_stone_andesite", "andesite");
		reinforcedSlabBlock(SCContent.REINFORCED_RED_NETHER_BRICK_SLAB.get(), "reinforced_red_nether_brick", "red_nether_bricks");
		reinforcedSlabBlock(SCContent.REINFORCED_POLISHED_ANDESITE_SLAB.get(), "reinforced_stone_smooth_andesite", "polished_andesite");
		reinforcedSlabBlock(SCContent.REINFORCED_DIORITE_SLAB.get(), "reinforced_stone_diorite", "diorite");
		reinforcedSlabBlock(SCContent.CRYSTAL_QUARTZ_SLAB.get(), "reinforced_quartz_default", "quartz_block_side", "quartz_block_top");
		reinforcedSlabBlock(SCContent.REINFORCED_CRYSTAL_QUARTZ_SLAB.get(), "reinforced_quartz_default", "quartz_block_side", "quartz_block_top");

		reinforcedStairsBlock(SCContent.REINFORCED_PURPUR_STAIRS.get(), "purpur_block");
		reinforcedStairsBlock(SCContent.REINFORCED_OAK_STAIRS.get(), "oak_planks");
		reinforcedStairsBlock(SCContent.REINFORCED_BRICK_STAIRS.get(), "bricks");
		reinforcedStairsBlock(SCContent.REINFORCED_STONE_BRICK_STAIRS.get(), "stone_bricks");
		reinforcedStairsBlock(SCContent.REINFORCED_NETHER_BRICK_STAIRS.get(), "nether_bricks");
		reinforcedStairsBlock(SCContent.REINFORCED_SANDSTONE_STAIRS.get(), "sandstone", "sandstone_bottom", "sandstone_top");
		reinforcedStairsBlock(SCContent.REINFORCED_SPRUCE_STAIRS.get(), "spruce_planks");
		reinforcedStairsBlock(SCContent.REINFORCED_BIRCH_STAIRS.get(), "birch_planks");
		reinforcedStairsBlock(SCContent.REINFORCED_JUNGLE_STAIRS.get(), "jungle_planks");
		reinforcedStairsBlock(SCContent.REINFORCED_QUARTZ_STAIRS.get(), "quartz_block_side", "quartz_block_top");
		reinforcedStairsBlock(SCContent.REINFORCED_ACACIA_STAIRS.get(), "acacia_planks");
		reinforcedStairsBlock(SCContent.REINFORCED_DARK_OAK_STAIRS.get(), "dark_oak_planks");
		reinforcedStairsBlock(SCContent.REINFORCED_PRISMARINE_BRICK_STAIRS.get(), "prismarine_bricks");
		reinforcedStairsBlock(SCContent.REINFORCED_RED_SANDSTONE_STAIRS.get(), "red_sandstone", "red_sandstone_bottom", "red_sandstone_top");
		reinforcedStairsBlock(SCContent.REINFORCED_SMOOTH_RED_SANDSTONE_STAIRS.get(), "red_sandstone_top");
		reinforcedStairsBlock(SCContent.REINFORCED_MOSSY_STONE_BRICK_STAIRS.get(), "mossy_stone_bricks");
		reinforcedStairsBlock(SCContent.REINFORCED_END_STONE_BRICK_STAIRS.get(), "end_stone_bricks");
		reinforcedStairsBlock(SCContent.REINFORCED_SMOOTH_SANDSTONE_STAIRS.get(), "sandstone_top");
		reinforcedStairsBlock(SCContent.REINFORCED_SMOOTH_QUARTZ_STAIRS.get(), "quartz_block_bottom");
		reinforcedStairsBlock(SCContent.REINFORCED_RED_NETHER_BRICK_STAIRS.get(), "red_nether_bricks");
		reinforcedStairsBlock(SCContent.REINFORCED_CRYSTAL_QUARTZ_STAIRS.get(), "quartz_block_side", "quartz_block_top");
		reinforcedStairsBlock(SCContent.STAIRS_CRYSTAL_QUARTZ.get(), "quartz_block_side", "quartz_block_top");

		reinforcedWallBlock(SCContent.REINFORCED_BRICK_WALL.get(), "bricks");
		reinforcedWallBlock(SCContent.REINFORCED_MOSSY_STONE_BRICK_WALL.get(), "mossy_stone_bricks");
		reinforcedWallBlock(SCContent.REINFORCED_STONE_BRICK_WALL.get(), "stone_bricks");
		reinforcedWallBlock(SCContent.REINFORCED_NETHER_BRICK_WALL.get(), "nether_bricks");
		reinforcedWallBlock(SCContent.REINFORCED_RED_NETHER_BRICK_WALL.get(), "red_nether_bricks");
		reinforcedWallBlock(SCContent.REINFORCED_END_STONE_BRICK_WALL.get(), "end_stone_bricks");
	}

	public void blockMine(Block vanillaBlock, Block block)
	{
		getVariantBuilder(block).forAllStates(state -> new ConfiguredModel[] {new ConfiguredModel(new UncheckedModelFile(mcLoc(ModelProvider.BLOCK_FOLDER + "/" + vanillaBlock.getRegistryName().getPath())))});
	}

	public void reinforcedCarpetBlock(Block block)
	{
		String name = name(block);
		ModelFile model = uncheckedSingleTexture(name, modLoc(BLOCK_FOLDER + "/reinforced_carpet"), "wool", mcLoc(ModelProvider.BLOCK_FOLDER + "/" + name.replace("reinforced_", "").replace("carpet", "wool")));

		getVariantBuilder(block).forAllStates(state -> new ConfiguredModel[]{new ConfiguredModel(model)});
	}

	public BlockModelBuilder reinforcedColumn(String name, String side, String end)
	{
		return withExistingParent(name, modLoc(BLOCK_FOLDER + "/reinforced_column"))
				.texture("side", mcLoc(BLOCK_FOLDER + "/" + side))
				.texture("end", mcLoc(BLOCK_FOLDER + "/" + end));
	}

	public void reinforcedPaneBlock(ReinforcedPaneBlock block)
	{
		String name = name(block);
		ResourceLocation noPane = modLoc(ModelProvider.BLOCK_FOLDER + "/" + name.replace("_pane", ""));

		reinforcedPaneBlock(block, block.getRegistryName().toString(), noPane, modLoc(ModelProvider.BLOCK_FOLDER + "/" + name + "_top"));
	}

	public void reinforcedPaneBlock(ReinforcedPaneBlock block, String name, ResourceLocation pane, ResourceLocation edge)
	{
		ModelFile post = panePost(name + "_post", pane, edge);
		ModelFile side = paneSide(name + "_side", pane, edge);
		ModelFile sideAlt = paneSideAlt(name + "_side_alt", pane, edge);
		ModelFile noSide = paneNoSide(name + "_noside", pane);
		ModelFile noSideAlt = paneNoSideAlt(name + "_noside_alt", pane);
		MultiPartBlockStateBuilder builder = getMultipartBuilder(block).part().modelFile(post).addModel().end();

		SixWayBlock.FACING_TO_PROPERTY_MAP.entrySet().forEach(e -> {
			Direction dir = e.getKey();

			if(dir.getAxis().isHorizontal())
			{
				builder.part().modelFile(dir == Direction.SOUTH || dir == Direction.WEST ? sideAlt : side).rotationY(dir.getAxis() == Axis.X ? 90 : 0).addModel()
				.condition(e.getValue(), true).end()
				.part().modelFile(dir == Direction.SOUTH || dir == Direction.EAST ? noSideAlt : noSide).rotationY(dir == Direction.WEST ? 270 : dir == Direction.SOUTH ? 90 : 0).addModel()
				.condition(e.getValue(), false);
			}
		});
	}

	public BlockModelBuilder reinforcedSlab(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		return sideBottomTop(name, modLoc(BLOCK_FOLDER + "/reinforced_slab"), side, bottom, top);
	}

	public BlockModelBuilder reinforcedSlabTop(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		return sideBottomTop(name, modLoc(BLOCK_FOLDER + "/reinforced_slab_top"), side, bottom, top);
	}

	protected BlockModelBuilder reinforcedStairs(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		return sideBottomTop(name, modLoc(BLOCK_FOLDER + "/reinforced_stairs"), side, bottom, top);
	}

	protected BlockModelBuilder reinforcedStairsOuter(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		return sideBottomTop(name, modLoc(BLOCK_FOLDER + "/reinforced_outer_stairs"), side, bottom, top);
	}

	protected BlockModelBuilder reinforcedStairsInner(String name, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		return sideBottomTop(name, modLoc(BLOCK_FOLDER + "/reinforced_inner_stairs"), side, bottom, top);
	}

	public void reinforcedSlabBlock(Block block)
	{
		String name = name(block).replace("_slab", "");

		reinforcedSlabBlock(block, name, name.replace("reinforced_", ""));
	}

	public void reinforcedSlabBlock(Block block, String doubleSlabModel, String texture)
	{
		ResourceLocation textureLocation = mcLoc(ModelProvider.BLOCK_FOLDER + "/" + texture);

		reinforcedSlabBlock(block, name(block), modLoc(ModelProvider.BLOCK_FOLDER + "/" + doubleSlabModel), textureLocation, textureLocation, textureLocation);
	}

	public void reinforcedSlabBlock(Block block, String doubleSlabModel, String side, String end)
	{
		ResourceLocation endTextureLocation = mcLoc(ModelProvider.BLOCK_FOLDER + "/" + end);

		reinforcedSlabBlock(block, name(block),  modLoc(ModelProvider.BLOCK_FOLDER + "/" + doubleSlabModel), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + side), endTextureLocation, endTextureLocation);
	}

	public void reinforcedSlabBlock(Block block, String doubleSlabModel, String side, String bottom, String top)
	{
		reinforcedSlabBlock(block, name(block), modLoc(ModelProvider.BLOCK_FOLDER + "/" + doubleSlabModel), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + side), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + bottom), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + top));
	}

	public void reinforcedSlabBlock(Block block, String baseName, ResourceLocation doubleSlab, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		ModelFile bottomModel = reinforcedSlab(baseName, side, bottom, top);
		ModelFile topModel = reinforcedSlabTop(baseName + "_top", side, bottom, top);
		ModelFile doubleSlabModel = getExistingFile(doubleSlab);

		getVariantBuilder(block)
		.partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(new ConfiguredModel(bottomModel))
		.partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(new ConfiguredModel(topModel))
		.partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(new ConfiguredModel(doubleSlabModel));
	}

	public void reinforcedStairsBlock(Block block)
	{
		reinforcedStairsBlock(block, name(block).replace("reinforced_", "").replace("_stairs", ""));
	}

	public void reinforcedStairsBlock(Block block, String texture)
	{
		ResourceLocation textureLocation = mcLoc(ModelProvider.BLOCK_FOLDER + "/" + texture);

		reinforcedStairsBlock(block, block.getRegistryName().toString(), textureLocation, textureLocation, textureLocation);
	}

	public void reinforcedStairsBlock(Block block, String side, String end)
	{
		ResourceLocation textureLocationEnd = mcLoc(ModelProvider.BLOCK_FOLDER + "/" + end);

		reinforcedStairsBlock(block, block.getRegistryName().toString(), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + side), textureLocationEnd, textureLocationEnd);
	}

	public void reinforcedStairsBlock(Block block, String side, String bottom, String top)
	{
		reinforcedStairsBlock(block, block.getRegistryName().toString(), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + side), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + bottom), mcLoc(ModelProvider.BLOCK_FOLDER + "/" + top));
	}

	public void reinforcedStairsBlock(Block block, String baseName, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		ModelFile stairs = reinforcedStairs(baseName, side, bottom, top);
		ModelFile stairsInner = reinforcedStairsInner(baseName + "_inner", side, bottom, top);
		ModelFile stairsOuter = reinforcedStairsOuter(baseName + "_outer", side, bottom, top);

		getVariantBuilder(block).forAllStatesExcept(state -> {
			Direction facing = state.get(StairsBlock.FACING);
			Half half = state.get(StairsBlock.HALF);
			StairsShape shape = state.get(StairsBlock.SHAPE);
			int yRot = (int)facing.rotateY().getHorizontalAngle();

			if(shape == StairsShape.INNER_LEFT || shape == StairsShape.OUTER_LEFT)
				yRot += 270;

			if(shape != StairsShape.STRAIGHT && half == Half.TOP)
				yRot += 90;

			yRot %= 360;

			return ConfiguredModel.builder()
					.modelFile(shape == StairsShape.STRAIGHT ? stairs : shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT ? stairsInner : stairsOuter)
					.rotationX(half == Half.BOTTOM ? 0 : 180)
					.rotationY(yRot)
					.uvLock(yRot != 0 || half == Half.TOP)
					.build();
		}, StairsBlock.WATERLOGGED);
	}

	public void reinforcedWallBlock(Block block)
	{
		reinforcedWallBlock(block, name(block).replace("reinforced_", "").replace("_wall", ""));
	}

	public void reinforcedWallBlock(Block block, String textureName)
	{
		ResourceLocation texture = new ResourceLocation(ModelProvider.BLOCK_FOLDER + "/" + textureName);
		String baseName = block.getRegistryName().toString();
		ModelFile post = reinforcedWallPost(baseName + "_post", texture);
		ModelFile side = reinforcedWallSide(baseName + "_side", texture);
		MultiPartBlockStateBuilder builder = getMultipartBuilder(block)
				.part().modelFile(post).addModel()
				.condition(WallBlock.UP, true).end();

		fourWayMultipart(builder, side);
		reinforcedWallInventory(baseName + "_inventory", texture);
	}

	public BlockModelBuilder reinforcedWallPost(String name, ResourceLocation wall)
	{
		return uncheckedSingleTexture(name, modLoc(ModelProvider.BLOCK_FOLDER + "/reinforced_wall_post"), "wall", wall);
	}

	public BlockModelBuilder reinforcedWallSide(String name, ResourceLocation wall)
	{
		return uncheckedSingleTexture(name, modLoc(ModelProvider.BLOCK_FOLDER + "/reinforced_wall_side"), "wall", wall);
	}

	public BlockModelBuilder reinforcedWallInventory(String name, ResourceLocation wall)
	{
		return uncheckedSingleTexture(name, modLoc(ModelProvider.BLOCK_FOLDER + "/reinforced_wall_inventory"), "wall", wall);
	}

	private BlockModelBuilder sideBottomTop(String name, ResourceLocation parent, ResourceLocation side, ResourceLocation bottom, ResourceLocation top)
	{
		return withExistingParent(name, parent)
				.texture("side", side)
				.texture("bottom", bottom)
				.texture("top", top);
	}

	public BlockModelBuilder uncheckedSingleTexture(String name, ResourceLocation parent, String textureKey, ResourceLocation texture)
	{
		return getBuilder(name).parent(new UncheckedModelFile(parent)).texture(textureKey, texture);
	}

	@Override
	public MultiPartBlockStateBuilder getMultipartBuilder(Block b)
	{
		//because some blocks' states get set twice (once during scanning SCContent, then actually generating the proper model),
		//and the super method checking if the state has already been generated, and erroring if so, the key has to be removed so it can be remade
		if(registeredBlocks.containsKey(b))
			registeredBlocks.remove(b);

		return super.getMultipartBuilder(b);
	}

	@Override
	public VariantBlockStateBuilder getVariantBuilder(Block b)
	{
		//because some blocks' states get set twice (once during scanning SCContent, then actually generating the proper model),
		//and the super method checking if the state has already been generated, and erroring if so, the key has to be removed so it can be remade
		if(registeredBlocks.containsKey(b))
			registeredBlocks.remove(b);

		return super.getVariantBuilder(b);
	}

	@Override
	public String getName()
	{
		return "SecurityCraft Block States/Models";
	}

	private String name(Block block)
	{
		return block.getRegistryName().getPath();
	}
}

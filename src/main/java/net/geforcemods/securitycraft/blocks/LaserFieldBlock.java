package net.geforcemods.securitycraft.blocks;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.OwnableBlockEntity;
import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.geforcemods.securitycraft.misc.CustomDamageSources;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.EntityUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class LaserFieldBlock extends OwnableBlock implements IOverlayDisplay {
	public static final IntegerProperty BOUNDTYPE = IntegerProperty.create("boundtype", 1, 3);
	private static final VoxelShape SHAPE_X = Block.box(0, 6.75, 6.75, 16, 9.25, 9.25);
	private static final VoxelShape SHAPE_Y = Block.box(6.75, 0, 6.75, 9.25, 16, 9.25);
	private static final VoxelShape SHAPE_Z = Block.box(6.75, 6.75, 0, 9.25, 9.25, 16);

	public LaserFieldBlock(Block.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(BOUNDTYPE, 1));
	}

	@Override
	public VoxelShape getCollisionShape(BlockState blockState, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
		return VoxelShapes.empty();
	}

	@Override
	public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!world.isClientSide && entity instanceof LivingEntity && !EntityUtils.isInvisible((LivingEntity) entity)) {
			if (!getShape(state, world, pos, ISelectionContext.of(entity)).bounds().move(pos).intersects(entity.getBoundingBox()))
				return;

			for (Direction facing : Direction.values()) {
				for (int i = 0; i < ConfigHandler.SERVER.laserBlockRange.get(); i++) {
					BlockPos offsetPos = pos.relative(facing, i);
					BlockState offsetState = world.getBlockState(offsetPos);
					Block offsetBlock = offsetState.getBlock();

					if (offsetBlock == SCContent.LASER_BLOCK.get()) {
						TileEntity te = world.getBlockEntity(offsetPos);

						if (te instanceof IModuleInventory) {
							IModuleInventory moduleInv = (IModuleInventory) te;

							if (moduleInv.isAllowed(entity))
								return;

							if (moduleInv.isModuleEnabled(ModuleType.REDSTONE) && !offsetState.getValue(LaserBlock.POWERED)) {
								world.setBlockAndUpdate(offsetPos, offsetState.setValue(LaserBlock.POWERED, true));
								BlockUtils.updateIndirectNeighbors(world, offsetPos, SCContent.LASER_BLOCK.get());
								world.getBlockTicks().scheduleTick(offsetPos, SCContent.LASER_BLOCK.get(), 50);
							}

							if (moduleInv.isModuleEnabled(ModuleType.HARMING)) {
								if (!(entity instanceof PlayerEntity && ((IOwnable) moduleInv).isOwner((PlayerEntity) entity))) {
									double damage = ConfigHandler.SERVER.laserDamage.get();

									((LivingEntity) entity).hurt(CustomDamageSources.LASER, (float) damage);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void destroy(IWorld world, BlockPos pos, BlockState state) {
		if (!world.isClientSide()) {
			Direction[] facingArray = {
					Direction.from3DDataValue((state.getValue(LaserFieldBlock.BOUNDTYPE) - 1) * 2), Direction.from3DDataValue((state.getValue(LaserFieldBlock.BOUNDTYPE) - 1) * 2).getOpposite()
			};

			for (Direction facing : facingArray) {
				for (int i = 0; i < ConfigHandler.SERVER.laserBlockRange.get(); i++) {
					if (world.getBlockState(pos.relative(facing, i)).getBlock() == SCContent.LASER_BLOCK.get()) {
						for (int j = 1; j < i; j++) {
							world.destroyBlock(pos.relative(facing, j), false);
						}

						break;
					}
				}
			}
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader source, BlockPos pos, ISelectionContext ctx) {
		if (source.getBlockState(pos).getBlock() instanceof LaserFieldBlock) {
			int boundType = source.getBlockState(pos).getValue(BOUNDTYPE);

			if (boundType == 1)
				return SHAPE_Y;
			else if (boundType == 2)
				return SHAPE_Z;
			else if (boundType == 3)
				return SHAPE_X;
		}

		return VoxelShapes.empty();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		return getStateForPlacement(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace(), ctx.getClickLocation().x, ctx.getClickLocation().y, ctx.getClickLocation().z, ctx.getPlayer());
	}

	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, double hitX, double hitY, double hitZ, PlayerEntity placer) {
		return defaultBlockState().setValue(BOUNDTYPE, 1);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(BOUNDTYPE);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
		return ItemStack.EMPTY;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new OwnableBlockEntity(SCContent.ABSTRACT_BLOCK_ENTITY.get());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		int boundType = state.getValue(BOUNDTYPE);

		return rot == Rotation.CLOCKWISE_180 ? state : state.setValue(BOUNDTYPE, boundType == 2 ? 3 : (boundType == 3 ? 2 : 1));
	}

	@Override
	public ItemStack getDisplayStack(World level, BlockState state, BlockPos pos) {
		return null;
	}

	@Override
	public boolean shouldShowSCInfo(World level, BlockState state, BlockPos pos) {
		return false;
	}
}

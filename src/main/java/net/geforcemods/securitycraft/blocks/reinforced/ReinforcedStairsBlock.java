package net.geforcemods.securitycraft.blocks.reinforced;

import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ReinforcedStairsBlock extends BaseReinforcedBlock implements IWaterLoggable {
	public static final DirectionProperty FACING = HorizontalBlock.FACING;
	public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
	public static final EnumProperty<StairsShape> SHAPE = BlockStateProperties.STAIRS_SHAPE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	protected static final VoxelShape AABB_SLAB_TOP = Block.box(0.0D, 8.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	protected static final VoxelShape AABB_SLAB_BOTTOM = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
	protected static final VoxelShape NWD_CORNER = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 8.0D, 8.0D);
	protected static final VoxelShape SWD_CORNER = Block.box(0.0D, 0.0D, 8.0D, 8.0D, 8.0D, 16.0D);
	protected static final VoxelShape NWU_CORNER = Block.box(0.0D, 8.0D, 0.0D, 8.0D, 16.0D, 8.0D);
	protected static final VoxelShape SWU_CORNER = Block.box(0.0D, 8.0D, 8.0D, 8.0D, 16.0D, 16.0D);
	protected static final VoxelShape NED_CORNER = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 8.0D, 8.0D);
	protected static final VoxelShape SED_CORNER = Block.box(8.0D, 0.0D, 8.0D, 16.0D, 8.0D, 16.0D);
	protected static final VoxelShape NEU_CORNER = Block.box(8.0D, 8.0D, 0.0D, 16.0D, 16.0D, 8.0D);
	protected static final VoxelShape SEU_CORNER = Block.box(8.0D, 8.0D, 8.0D, 16.0D, 16.0D, 16.0D);
	protected static final VoxelShape[] SLAB_TOP_SHAPES = makeShapes(AABB_SLAB_TOP, NWD_CORNER, NED_CORNER, SWD_CORNER, SED_CORNER);
	protected static final VoxelShape[] SLAB_BOTTOM_SHAPES = makeShapes(AABB_SLAB_BOTTOM, NWU_CORNER, NEU_CORNER, SWU_CORNER, SEU_CORNER);
	private static final int[] SHAPE_BY_STATE = {
			12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8
	};
	private final Block modelBlock;
	private final BlockState modelState;

	public ReinforcedStairsBlock(Block.Properties properties, Block vB) {
		this(properties, () -> vB);
	}

	public ReinforcedStairsBlock(Block.Properties properties, Supplier<Block> vB) {
		super(properties, vB);

		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM).setValue(SHAPE, StairsShape.STRAIGHT).setValue(WATERLOGGED, false));
		modelBlock = getVanillaBlock();
		modelState = modelBlock.defaultBlockState();
	}

	private static VoxelShape[] makeShapes(VoxelShape slabShape, VoxelShape nwCorner, VoxelShape neCorner, VoxelShape swCorner, VoxelShape seCorner) {
		return IntStream.range(0, 16).mapToObj(shape -> combineShapes(shape, slabShape, nwCorner, neCorner, swCorner, seCorner)).toArray(size -> new VoxelShape[size]);
	}

	private static VoxelShape combineShapes(int bitfield, VoxelShape slabShape, VoxelShape nwCorner, VoxelShape neCorner, VoxelShape swCorner, VoxelShape seCorner) {
		VoxelShape shape = slabShape;

		if ((bitfield & 1) != 0)
			shape = VoxelShapes.or(slabShape, nwCorner);

		if ((bitfield & 2) != 0)
			shape = VoxelShapes.or(shape, neCorner);

		if ((bitfield & 4) != 0)
			shape = VoxelShapes.or(shape, swCorner);

		if ((bitfield & 8) != 0)
			shape = VoxelShapes.or(shape, seCorner);

		return shape;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return (state.getValue(HALF) == Half.TOP ? SLAB_TOP_SHAPES : SLAB_BOTTOM_SHAPES)[SHAPE_BY_STATE[getShapeIndex(state)]];
	}

	private int getShapeIndex(BlockState state) {
		return state.getValue(SHAPE).ordinal() * 4 + state.getValue(FACING).get2DDataValue();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
		modelBlock.animateTick(stateIn, worldIn, pos, rand);
	}

	@Override
	public void attack(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		modelState.attack(worldIn, pos, player);
	}

	@Override
	public void destroy(IWorld worldIn, BlockPos pos, BlockState state) {
		modelBlock.destroy(worldIn, pos, state);
	}

	@Override
	public void onPlace(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (state.getBlock() != oldState.getBlock()) {
			modelState.neighborChanged(world, pos, Blocks.AIR, pos, false);
			modelBlock.onPlace(modelState, world, pos, oldState, false);
		}
	}

	@Override
	public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock())
			modelState.onRemove(world, pos, newState, isMoving);

		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public void stepOn(World worldIn, BlockPos pos, Entity entity) {
		modelBlock.stepOn(worldIn, pos, entity);
	}

	@Override
	public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		modelState.tick(world, pos, random);
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		return modelState.use(world, player, hand, hit);
	}

	@Override
	public void wasExploded(World world, BlockPos pos, Explosion explosion) {
		modelBlock.wasExploded(world, pos, explosion);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext ctx) {
		Direction dir = ctx.getClickedFace();
		BlockPos pos = ctx.getClickedPos();
		FluidState fluidState = ctx.getLevel().getFluidState(pos);
		BlockState state = this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection()).setValue(HALF, dir != Direction.DOWN && (dir == Direction.UP || !(ctx.getClickLocation().y - pos.getY() > 0.5D)) ? Half.BOTTOM : Half.TOP).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);

		return state.setValue(SHAPE, getShapeProperty(state, ctx.getLevel(), pos));
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld world, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED))
			world.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));

		return facing.getAxis().isHorizontal() ? state.setValue(SHAPE, getShapeProperty(state, world, currentPos)) : super.updateShape(state, facing, facingState, world, currentPos, facingPos);
	}

	private static StairsShape getShapeProperty(BlockState state, IBlockReader world, BlockPos pos) {
		Direction dir = state.getValue(FACING);
		BlockState offsetState = world.getBlockState(pos.relative(dir));

		if (isBlockStairs(offsetState) && state.getValue(HALF) == offsetState.getValue(HALF)) {
			Direction offsetDir = offsetState.getValue(FACING);

			if (offsetDir.getAxis() != state.getValue(FACING).getAxis() && isDifferentStairs(state, world, pos, offsetDir.getOpposite())) {
				if (offsetDir == dir.getCounterClockWise())
					return StairsShape.OUTER_LEFT;
				else
					return StairsShape.OUTER_RIGHT;
			}
		}

		BlockState offsetOppositeState = world.getBlockState(pos.relative(dir.getOpposite()));

		if (isBlockStairs(offsetOppositeState) && state.getValue(HALF) == offsetOppositeState.getValue(HALF)) {
			Direction offsetOppositeDir = offsetOppositeState.getValue(FACING);

			if (offsetOppositeDir.getAxis() != state.getValue(FACING).getAxis() && isDifferentStairs(state, world, pos, offsetOppositeDir)) {
				if (offsetOppositeDir == dir.getCounterClockWise())
					return StairsShape.INNER_LEFT;
				else
					return StairsShape.INNER_RIGHT;
			}
		}

		return StairsShape.STRAIGHT;
	}

	private static boolean isDifferentStairs(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
		BlockState offsetState = world.getBlockState(pos.relative(face));

		return !isBlockStairs(offsetState) || offsetState.getValue(FACING) != state.getValue(FACING) || offsetState.getValue(HALF) != state.getValue(HALF);
	}

	public static boolean isBlockStairs(BlockState state) {
		return state.getBlock() instanceof ReinforcedStairsBlock || state.getBlock() instanceof StairsBlock;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		Direction direction = state.getValue(FACING);
		StairsShape shape = state.getValue(SHAPE);

		switch (mirror) {
			case LEFT_RIGHT:
				if (direction.getAxis() == Direction.Axis.Z) {
					switch (shape) {
						case INNER_LEFT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
						case INNER_RIGHT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
						case OUTER_LEFT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
						case OUTER_RIGHT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
						default:
							return state.rotate(Rotation.CLOCKWISE_180);
					}
				}
				break;
			case FRONT_BACK:
				if (direction.getAxis() == Direction.Axis.X) {
					switch (shape) {
						case INNER_LEFT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_LEFT);
						case INNER_RIGHT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.INNER_RIGHT);
						case OUTER_LEFT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_RIGHT);
						case OUTER_RIGHT:
							return state.rotate(Rotation.CLOCKWISE_180).setValue(SHAPE, StairsShape.OUTER_LEFT);
						case STRAIGHT:
							return state.rotate(Rotation.CLOCKWISE_180);
					}
				}
				break;
			default:
				break;
		}

		return super.mirror(state, mirror);
	}

	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF, SHAPE, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader world, BlockPos pos, PathType type) {
		return false;
	}
}
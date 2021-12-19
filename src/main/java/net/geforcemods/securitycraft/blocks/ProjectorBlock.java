package net.geforcemods.securitycraft.blocks;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import net.geforcemods.securitycraft.blockentities.ProjectorBlockEntity;
import net.geforcemods.securitycraft.util.LevelUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class ProjectorBlock extends DisguisableBlock {
	private static final MutableComponent TOOLTIP = new TranslatableComponent("tooltip.securitycraft:projector").setStyle(Utils.GRAY_STYLE);
	public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
	private static final VoxelShape NORTH = Stream.of(Block.box(3, 5, 0.9, 6, 8, 1.9), Block.box(0, 3, 1, 16, 10, 16), Block.box(2, 8, 0.5, 7, 9, 1), Block.box(2, 4, 0.5, 7, 5, 1), Block.box(6, 5, 0.5, 7, 8, 1), Block.box(2, 5, 0.5, 3, 8, 1), Block.box(0, 0, 1, 2, 3, 3), Block.box(14, 0, 1, 16, 3, 3), Block.box(14, 0, 14, 16, 3, 16), Block.box(0, 0, 14, 2, 3, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).orElse(Shapes.block());
	private static final VoxelShape SOUTH = Stream.of(Block.box(0, 3, 0, 16, 10, 15), Block.box(10, 5, 14.1, 13, 8, 15.100000000000001), Block.box(9, 8, 15, 14, 9, 15.5), Block.box(9, 4, 15, 14, 5, 15.5), Block.box(9, 5, 15, 10, 8, 15.5), Block.box(13, 5, 15, 14, 8, 15.5), Block.box(14, 0, 13, 16, 3, 15), Block.box(0, 0, 13, 2, 3, 15), Block.box(0, 0, 0, 2, 3, 2), Block.box(14, 0, 0, 16, 3, 2)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).orElse(Shapes.block());
	private static final VoxelShape WEST = Stream.of(Block.box(0.5, 5, 13, 1, 8, 14), Block.box(0.5, 5, 9, 1, 8, 10), Block.box(0.5, 4, 9, 1, 5, 14), Block.box(0.5, 8, 9, 1, 9, 14), Block.box(0.75, 5, 10, 1.75, 8, 13), Block.box(1, 0, 14, 3, 3, 16), Block.box(14, 0, 14, 16, 3, 16), Block.box(14, 0, 0, 16, 3, 2), Block.box(1, 0, 0, 3, 3, 2), Block.box(1, 3, 0, 16, 10, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).orElse(Shapes.block());
	private static final VoxelShape EAST = Stream.of(Block.box(15, 5, 2, 15.5, 8, 3), Block.box(15, 5, 6, 15.5, 8, 7), Block.box(15, 4, 2, 15.5, 5, 7), Block.box(15, 8, 2, 15.5, 9, 7), Block.box(14.25, 5, 3, 15.25, 8, 6), Block.box(13, 0, 0, 15, 3, 2), Block.box(0, 0, 0, 2, 3, 2), Block.box(0, 0, 14, 2, 3, 16), Block.box(13, 0, 14, 15, 3, 16), Block.box(0, 3, 0, 15, 10, 16)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).orElse(Shapes.block());

	public ProjectorBlock(Block.Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		BlockState disguisedState = getDisguisedStateOrDefault(state, level, pos);

		if (disguisedState.getBlock() != this)
			return disguisedState.getShape(level, pos, ctx);
		else
			return switch (state.getValue(FACING)) {
				case NORTH -> SOUTH;
				case EAST -> WEST;
				case SOUTH -> NORTH;
				case WEST -> EAST;
				default -> Shapes.block();
			};
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!(level.getBlockEntity(pos) instanceof ProjectorBlockEntity be))
			return InteractionResult.FAIL;

		boolean isOwner = be.getOwner().isOwner(player);

		if (!level.isClientSide && isOwner)
			NetworkHooks.openGui((ServerPlayer) player, be, pos);

		return isOwner ? InteractionResult.SUCCESS : InteractionResult.FAIL;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (level.getBlockEntity(pos) instanceof ProjectorBlockEntity be) {
			// Drop the block being projected
			ItemEntity item = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), be.getStackInSlot(36));
			LevelUtils.addScheduledTask(level, () -> level.addFreshEntity(item));
		}

		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		if (!level.isClientSide && level.getBlockEntity(pos) instanceof ProjectorBlockEntity be) {
			if (be.isActivatedByRedstone()) {
				be.setActive(level.hasNeighborSignal(pos));
				level.sendBlockUpdated(pos, state, state, 3);
			}
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, Random rand) {
		if (!level.hasNeighborSignal(pos) && level.getBlockEntity(pos) instanceof ProjectorBlockEntity be) {
			if (be.isActivatedByRedstone())
				be.setActive(false);
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return getStateForPlacement(ctx.getLevel(), ctx.getClickedPos(), ctx.getClickedFace(), ctx.getClickLocation().x, ctx.getClickLocation().y, ctx.getClickLocation().z, ctx.getPlayer());
	}

	public BlockState getStateForPlacement(Level level, BlockPos pos, Direction facing, double hitX, double hitY, double hitZ, Player placer) {
		return defaultBlockState().setValue(FACING, placer.getDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ProjectorBlockEntity(pos, state);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
		tooltip.add(TOOLTIP);
	}
}

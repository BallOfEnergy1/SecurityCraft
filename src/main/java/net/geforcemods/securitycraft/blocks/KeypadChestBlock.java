package net.geforcemods.securitycraft.blocks;

import java.util.function.Function;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasscodeConvertible;
import net.geforcemods.securitycraft.api.IPasscodeProtected;
import net.geforcemods.securitycraft.blockentities.KeypadChestBlockEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.misc.OwnershipEvent;
import net.geforcemods.securitycraft.misc.SaltData;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

public class KeypadChestBlock extends OwnableBlock {
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	protected static final AxisAlignedBB NORTH_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0D, 0.9375D, 0.875D, 0.9375D);
	protected static final AxisAlignedBB SOUTH_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 1.0D);
	protected static final AxisAlignedBB WEST_CHEST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);
	protected static final AxisAlignedBB EAST_CHEST_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 1.0D, 0.875D, 0.9375D);
	protected static final AxisAlignedBB NOT_CONNECTED_AABB = new AxisAlignedBB(0.0625D, 0.0D, 0.0625D, 0.9375D, 0.875D, 0.9375D);

	public KeypadChestBlock() {
		super(Material.IRON);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setSoundType(SoundType.METAL);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean hasCustomBreakingProgress(IBlockState state) {
		return true;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		if (world.getBlockState(pos.north()).getBlock() == this)
			return NORTH_CHEST_AABB;
		else if (world.getBlockState(pos.south()).getBlock() == this)
			return SOUTH_CHEST_AABB;
		else if (world.getBlockState(pos.west()).getBlock() == this)
			return WEST_CHEST_AABB;
		else
			return world.getBlockState(pos.east()).getBlock() == this ? EAST_CHEST_AABB : NOT_CONNECTED_AABB;
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		checkForSurroundingChests(world, pos, state);

		for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
			BlockPos offsetPos = pos.offset(facing);
			IBlockState offsetState = world.getBlockState(offsetPos);

			if (offsetState.getBlock() == this)
				checkForSurroundingChests(world, offsetPos, offsetState);
		}
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			KeypadChestBlockEntity te = (KeypadChestBlockEntity) world.getTileEntity(pos);

			if (te.verifyPasscodeSet(world, pos, te, player)) {
				if (te.isDenied(player)) {
					if (te.sendsMessages())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getTranslationKey() + ".name"), Utils.localize("messages.securitycraft:module.onDenylist"), TextFormatting.RED);

					return true;
				}
				else if (te.isAllowed(player)) {
					if (te.sendsMessages())
						PlayerUtils.sendMessageToPlayer(player, Utils.localize(getTranslationKey() + ".name"), Utils.localize("messages.securitycraft:module.onAllowlist"), TextFormatting.GREEN);

					activate(world, pos, player);
				}
				else if (!PlayerUtils.isHoldingItem(player, SCContent.codebreaker, hand))
					te.openPasscodeGUI(world, pos, player);
			}
		}

		return true;
	}

	public void activate(World world, BlockPos pos, EntityPlayer player) {
		if (!isBlocked(world, pos))
			player.displayGUIChest(getLockableContainer(world, pos));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		EnumFacing facing = EnumFacing.byHorizontalIndex(MathHelper.floor(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3).getOpposite();
		state = state.withProperty(FACING, facing);
		BlockPos northPos = pos.north();
		BlockPos southPos = pos.south();
		BlockPos westPos = pos.west();
		BlockPos eastPos = pos.east();
		boolean isNorthPPC = this == world.getBlockState(northPos).getBlock();
		boolean isSouthPPC = this == world.getBlockState(southPos).getBlock();
		boolean isWestPPC = this == world.getBlockState(westPos).getBlock();
		boolean isEastPPC = this == world.getBlockState(eastPos).getBlock();
		BlockPos otherChestPos = null;

		if (!isNorthPPC && !isSouthPPC && !isWestPPC && !isEastPPC)
			world.setBlockState(pos, state, 3);
		else if (facing.getAxis() != EnumFacing.Axis.X || !isNorthPPC && !isSouthPPC) {
			if (facing.getAxis() == EnumFacing.Axis.Z && (isWestPPC || isEastPPC)) {
				if (isWestPPC) {
					world.setBlockState(westPos, state, 3);
					otherChestPos = westPos;
				}
				else {
					world.setBlockState(eastPos, state, 3);
					otherChestPos = eastPos;
				}

				world.setBlockState(pos, state, 3);
			}
		}
		else {
			if (isNorthPPC) {
				world.setBlockState(northPos, state, 3);
				otherChestPos = northPos;
			}
			else {
				world.setBlockState(southPos, state, 3);
				otherChestPos = southPos;
			}

			world.setBlockState(pos, state, 3);
		}

		KeypadChestBlockEntity thisTe = (KeypadChestBlockEntity) world.getTileEntity(pos);

		if (stack.hasDisplayName())
			thisTe.setCustomName(stack.getDisplayName());

		if (entity instanceof EntityPlayer) {
			MinecraftForge.EVENT_BUS.post(new OwnershipEvent(world, pos, (EntityPlayer) entity));

			if (otherChestPos != null) {
				TileEntity otherTe = world.getTileEntity(otherChestPos);

				if (otherTe instanceof KeypadChestBlockEntity && thisTe.getOwner().owns((KeypadChestBlockEntity) otherTe)) {
					KeypadChestBlockEntity te = (KeypadChestBlockEntity) otherTe;

					for (ModuleType type : te.getInsertedModules()) {
						thisTe.insertModule(te.getModule(type), false);
					}

					thisTe.setSendsMessages(te.sendsMessages());

					if (te.getSaltKey() != null)
						thisTe.setSaltKey(SaltData.putSalt(te.getSalt()));

					thisTe.setPasscode(te.getPasscode());
				}
			}
		}
	}

	public IBlockState checkForSurroundingChests(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			IBlockState northState = world.getBlockState(pos.north());
			IBlockState southState = world.getBlockState(pos.south());
			IBlockState westState = world.getBlockState(pos.west());
			IBlockState eastState = world.getBlockState(pos.east());
			EnumFacing facing = state.getValue(FACING);

			if (northState.getBlock() != this && southState.getBlock() != this) {
				boolean isNorthFullBlock = northState.isFullBlock();
				boolean isSouthFullBlock = southState.isFullBlock();

				if (westState.getBlock() == this || eastState.getBlock() == this) {
					BlockPos otherPos = westState.getBlock() == this ? pos.west() : pos.east();
					IBlockState otherNorthState = world.getBlockState(otherPos.north());
					IBlockState otherSouthState = world.getBlockState(otherPos.south());
					EnumFacing otherFacing;

					facing = EnumFacing.SOUTH;

					if (westState.getBlock() == this)
						otherFacing = westState.getValue(FACING);
					else
						otherFacing = eastState.getValue(FACING);

					if (otherFacing == EnumFacing.NORTH)
						facing = EnumFacing.NORTH;

					if ((isNorthFullBlock || otherNorthState.isFullBlock()) && !isSouthFullBlock && !otherSouthState.isFullBlock())
						facing = EnumFacing.SOUTH;

					if ((isSouthFullBlock || otherSouthState.isFullBlock()) && !isNorthFullBlock && !otherNorthState.isFullBlock())
						facing = EnumFacing.NORTH;
				}
			}
			else {
				BlockPos otherPos = northState.getBlock() == this ? pos.north() : pos.south();
				IBlockState otherWestState = world.getBlockState(otherPos.west());
				IBlockState otherEastState = world.getBlockState(otherPos.east());
				EnumFacing otherFacing;

				facing = EnumFacing.EAST;

				if (northState.getBlock() == this)
					otherFacing = northState.getValue(FACING);
				else
					otherFacing = southState.getValue(FACING);

				if (otherFacing == EnumFacing.WEST)
					facing = EnumFacing.WEST;

				if ((westState.isFullBlock() || otherWestState.isFullBlock()) && !eastState.isFullBlock() && !otherEastState.isFullBlock())
					facing = EnumFacing.EAST;

				if ((eastState.isFullBlock() || otherEastState.isFullBlock()) && !westState.isFullBlock() && !otherWestState.isFullBlock())
					facing = EnumFacing.WEST;
			}

			state = state.withProperty(FACING, facing);
			world.setBlockState(pos, state, 3);
		}

		return state;
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos) {
		int surroundingChests = 0;
		BlockPos westPos = pos.west();
		BlockPos eastPos = pos.east();
		BlockPos northPos = pos.north();
		BlockPos southPos = pos.south();

		if (world.getBlockState(westPos).getBlock() == this) {
			if (isDoubleChest(world, westPos))
				return false;

			surroundingChests++;
		}

		if (world.getBlockState(eastPos).getBlock() == this && (isDoubleChest(world, eastPos) || ++surroundingChests > 1))
			return false;

		if (world.getBlockState(northPos).getBlock() == this && (isDoubleChest(world, northPos) || ++surroundingChests > 1))
			return false;

		if (world.getBlockState(southPos).getBlock() == this && (isDoubleChest(world, southPos) || ++surroundingChests > 1))
			return false;

		return surroundingChests <= 1;
	}

	public boolean isDoubleChest(World world, BlockPos pos) {
		if (world.getBlockState(pos).getBlock() == this) {
			for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
				if (world.getBlockState(pos.offset(facing)).getBlock() == this)
					return true;
			}
		}

		return false;
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);

		TileEntity tileEntity = world.getTileEntity(pos);

		if (tileEntity instanceof KeypadChestBlockEntity)
			((KeypadChestBlockEntity) tileEntity).updateContainingBlockInfo();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, world, pos, blockIn, fromPos);
		TileEntity te = world.getTileEntity(pos);

		if (te instanceof KeypadChestBlockEntity)
			te.updateContainingBlockInfo();
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		TileEntity te = world.getTileEntity(pos);

		if (te instanceof IInventory) {
			InventoryHelper.dropInventoryItems(world, pos, (IInventory) te);
			world.updateComparatorOutputLevel(pos, this);
		}

		if (te instanceof IPasscodeProtected)
			SaltData.removeSalt(((IPasscodeProtected) te).getSaltKey());

		super.breakBlock(world, pos, state);
	}

	public ILockableContainer getLockableContainer(World worldIn, BlockPos pos) {
		return getContainer(worldIn, pos, false);
	}

	public ILockableContainer getContainer(World world, BlockPos pos, boolean allowBlocking) {
		TileEntity te = world.getTileEntity(pos);

		if (!(te instanceof KeypadChestBlockEntity))
			return null;
		else {
			ILockableContainer container = (KeypadChestBlockEntity) te;

			if (!allowBlocking && isBlocked(world, pos))
				return null;
			else {
				for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
					BlockPos offsetPos = pos.offset(facing);
					Block offsetBlock = world.getBlockState(offsetPos).getBlock();

					if (offsetBlock == this) {
						if (!allowBlocking && isBlocked(world, offsetPos)) // Forge: fix MC-99321
							return null;

						TileEntity otherTE = world.getTileEntity(offsetPos);

						if (otherTE instanceof KeypadChestBlockEntity) {
							if (facing != EnumFacing.WEST && facing != EnumFacing.NORTH)
								container = new InventoryLargeChest("gui.securitycraft:keypadChestDouble", container, (TileEntityChest) otherTE);
							else
								container = new InventoryLargeChest("gui.securitycraft:keypadChestDouble", (TileEntityChest) otherTE, container);
						}
					}
				}

				return container;
			}
		}
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (!state.canProvidePower())
			return 0;
		else {
			TileEntity te = world.getTileEntity(pos);
			int numPlayersUsing = 0;

			if (te instanceof KeypadChestBlockEntity && ((KeypadChestBlockEntity) te).isModuleEnabled(ModuleType.REDSTONE))
				numPlayersUsing = ((KeypadChestBlockEntity) te).numPlayersUsing;

			return MathHelper.clamp(numPlayersUsing, 0, 15);
		}
	}

	@Override
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return side == EnumFacing.UP ? state.getWeakPower(world, pos, side) : 0;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new KeypadChestBlockEntity();
	}

	public static boolean isBlocked(World world, BlockPos pos) {
		return isBelowSolidBlock(world, pos) || isOcelotSittingOnChest(world, pos);
	}

	private static boolean isBelowSolidBlock(World world, BlockPos pos) {
		return world.getBlockState(pos.up()).doesSideBlockChestOpening(world, pos.up(), EnumFacing.DOWN);
	}

	private static boolean isOcelotSittingOnChest(World world, BlockPos pos) {
		for (EntityOcelot ocelot : world.getEntitiesWithinAABB(EntityOcelot.class, new AxisAlignedBB(pos.getX(), pos.getY() + 1, pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1))) {
			if (ocelot.isSitting())
				return true;
		}

		return false;
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		return Container.calcRedstoneFromInventory(this.getLockableContainer(worldIn, pos));
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing facing = EnumFacing.byIndex(meta);

		if (facing.getAxis() == EnumFacing.Axis.Y)
			facing = EnumFacing.NORTH;

		return this.getDefaultState().withProperty(FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirror) {
		return state.withRotation(mirror.toRotation(state.getValue(FACING)));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Override
	public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
		return !isDoubleChest(world, pos) && super.rotateBlock(world, pos, axis);
	}

	public static class Convertible implements Function<Object, IPasscodeConvertible>, IPasscodeConvertible {
		@Override
		public IPasscodeConvertible apply(Object o) {
			return this;
		}

		@Override
		public boolean isValidStateForConversion(IBlockState state) {
			return OreDictionary.getOres("chestWood").stream().anyMatch(stack -> stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() == state.getBlock());
		}

		@Override
		public boolean convert(EntityPlayer player, World world, BlockPos pos) {
			EnumFacing facing = world.getBlockState(pos).getValue(FACING);
			EnumFacing doubleFacing = getDoubleChestFacing(world, pos);

			convertChest(player, world, pos, facing);

			if (doubleFacing != EnumFacing.UP) {
				BlockPos newPos = pos.offset(doubleFacing);

				convertChest(player, world, newPos, world.getBlockState(newPos).getValue(FACING));
			}

			return true;
		}

		private void convertChest(EntityPlayer player, World world, BlockPos pos, EnumFacing facing) {
			TileEntityChest chest = (TileEntityChest) world.getTileEntity(pos);
			NBTTagCompound tag;
			TileEntity newTe;

			chest.fillWithLoot(player); //generate loot (if any), so items don't spill out when converting and no additional loot table is generated
			tag = chest.writeToNBT(new NBTTagCompound());
			chest.clear();
			world.setBlockState(pos, SCContent.keypadChest.getDefaultState().withProperty(FACING, facing));
			newTe = world.getTileEntity(pos);
			((TileEntityChest) newTe).readFromNBT(tag);
			((IOwnable) newTe).setOwner(player.getUniqueID().toString(), player.getName());
		}

		private EnumFacing getDoubleChestFacing(World world, BlockPos pos) {
			if (world.getBlockState(pos).getBlock() instanceof BlockChest) {
				for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL) {
					if (world.getBlockState(pos.offset(facing)).getBlock() instanceof BlockChest)
						return facing;
				}
			}

			return EnumFacing.UP;
		}
	}
}

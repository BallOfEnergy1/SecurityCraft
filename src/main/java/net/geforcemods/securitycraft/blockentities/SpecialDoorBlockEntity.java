package net.geforcemods.securitycraft.blockentities;

import java.util.ArrayList;
import java.util.function.Predicate;

import net.geforcemods.securitycraft.api.LinkableBlockEntity;
import net.geforcemods.securitycraft.api.LinkedAction;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class SpecialDoorBlockEntity extends LinkableBlockEntity {
	private BooleanOption sendMessage = new BooleanOption("sendMessage", true);
	private IntOption signalLength = new IntOption(this::getBlockPos, "signalLength", defaultSignalLength(), 0, 400, 5, true); //20 seconds max

	public SpecialDoorBlockEntity(TileEntityType<?> type) {
		super(type);
	}

	@Override
	public void onOwnerChanged(BlockState state, World world, BlockPos pos, PlayerEntity player) {
		TileEntity te;

		pos = state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos.above();
		te = world.getBlockEntity(pos);

		if (te instanceof SpecialDoorBlockEntity && isLinkedWith(this, (SpecialDoorBlockEntity) te)) {
			((SpecialDoorBlockEntity) te).setOwner(getOwner().getUUID(), getOwner().getName());

			if (!world.isClientSide)
				world.getServer().getPlayerList().broadcastAll(te.getUpdatePacket());
		}
	}

	@Override
	public void onModuleInserted(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleInserted(stack, module, toggled);
		handleModule(stack, module, false, toggled);
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module, boolean toggled) {
		super.onModuleRemoved(stack, module, toggled);
		handleModule(stack, module, true, toggled);
	}

	private void handleModule(ItemStack stack, ModuleType module, boolean removed, boolean toggled) {
		DoubleBlockHalf myHalf = getBlockState().getValue(DoorBlock.HALF);
		BlockPos otherPos;

		if (myHalf == DoubleBlockHalf.UPPER)
			otherPos = getBlockPos().below();
		else
			otherPos = getBlockPos().above();

		BlockState other = level.getBlockState(otherPos);

		if (other.getValue(DoorBlock.HALF) != myHalf) {
			TileEntity otherTe = level.getBlockEntity(otherPos);

			if (otherTe instanceof SpecialDoorBlockEntity) {
				SpecialDoorBlockEntity otherDoorTe = (SpecialDoorBlockEntity) otherTe;
				Predicate<ModuleType> test = toggled ? otherDoorTe::isModuleEnabled : otherDoorTe::hasModule;
				boolean result = test.test(module);

				if (!removed && !result)
					otherDoorTe.insertModule(stack, toggled);
				else if (removed && result)
					otherDoorTe.removeModule(module, toggled);
			}
		}
	}

	@Override
	protected void onLinkedBlockAction(LinkedAction action, Object[] parameters, ArrayList<LinkableBlockEntity> excludedTEs) {
		if (action == LinkedAction.OPTION_CHANGED) {
			Option<?> option = (Option<?>) parameters[0];

			if (option.getName().equals(sendMessage.getName()))
				sendMessage.copy(option);
			else if (option.getName().equals(signalLength.getName()))
				signalLength.copy(option);

			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2);
		}
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				sendMessage, signalLength
		};
	}

	public boolean sendsMessages() {
		return sendMessage.get();
	}

	public int getSignalLength() {
		return signalLength.get();
	}

	public abstract int defaultSignalLength();
}

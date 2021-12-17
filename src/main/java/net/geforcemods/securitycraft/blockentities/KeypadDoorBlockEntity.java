package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blocks.KeypadDoorBlock;
import net.geforcemods.securitycraft.inventory.GenericTEMenu;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class KeypadDoorBlockEntity extends SpecialDoorBlockEntity implements IPasswordProtected, ILockable {
	private String passcode;

	public KeypadDoorBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.beTypeKeypadDoor, pos, state);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		super.save(tag);

		if (passcode != null && !passcode.isEmpty())
			tag.putString("passcode", passcode);

		return tag;
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);

		passcode = tag.getString("passcode");
	}

	@Override
	public void activate(Player player) {
		if (!level.isClientSide && getBlockState().getBlock() instanceof KeypadDoorBlock block)
			block.activate(getBlockState(), level, worldPosition, getSignalLength());
	}

	@Override
	public void openPasswordGUI(Player player) {
		if (getPassword() != null) {
			if (player instanceof ServerPlayer serverPlayer) {
				NetworkHooks.openGui(serverPlayer, new MenuProvider() {
					@Override
					public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
						return new GenericTEMenu(SCContent.mTypeCheckPassword, windowId, level, worldPosition);
					}

					@Override
					public Component getDisplayName() {
						return KeypadDoorBlockEntity.super.getDisplayName();
					}
				}, worldPosition);
			}
		}
		else {
			if (getOwner().isOwner(player)) {
				if (player instanceof ServerPlayer serverPlayer) {
					NetworkHooks.openGui(serverPlayer, new MenuProvider() {
						@Override
						public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player player) {
							return new GenericTEMenu(SCContent.mTypeSetPassword, windowId, level, worldPosition);
						}

						@Override
						public Component getDisplayName() {
							return KeypadDoorBlockEntity.super.getDisplayName();
						}
					}, worldPosition);
				}
			}
			else {
				PlayerUtils.sendMessageToPlayer(player, new TextComponent("SecurityCraft"), Utils.localize("messages.securitycraft:passwordProtected.notSetUp"), ChatFormatting.DARK_RED);
			}
		}
	}

	@Override
	public boolean onCodebreakerUsed(BlockState state, Player player) {
		if (!state.getValue(DoorBlock.OPEN)) {
			activate(player);
			return true;
		}

		return false;
	}

	@Override
	public String getPassword() {
		return (passcode != null && !passcode.isEmpty()) ? passcode : null;
	}

	@Override
	public void setPassword(String password) {
		BlockEntity be = null;

		passcode = password;

		if (getBlockState().getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER)
			be = level.getBlockEntity(worldPosition.above());
		else if (getBlockState().getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
			be = level.getBlockEntity(worldPosition.below());

		if (be instanceof KeypadDoorBlockEntity doorTe)
			doorTe.setPasswordExclusively(password);
	}

	//only set the password for this door half
	public void setPasswordExclusively(String password) {
		passcode = password;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST
		};
	}

	@Override
	public int defaultSignalLength() {
		return 60;
	}
}

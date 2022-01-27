package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.api.Option.IntOption;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.containers.GenericTEContainer;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.network.NetworkHooks;

public class KeypadTileEntity extends DisguisableTileEntity implements IPasswordProtected, ILockable {
	private String passcode;
	private BooleanOption isAlwaysActive = new BooleanOption("isAlwaysActive", false) {
		@Override
		public void toggle() {
			super.toggle();

			level.setBlockAndUpdate(worldPosition, getBlockState().setValue(KeypadBlock.POWERED, get()));
			level.updateNeighborsAt(worldPosition, SCContent.KEYPAD.get());
		}
	};
	private BooleanOption sendMessage = new BooleanOption("sendMessage", true);
	private IntOption signalLength = new IntOption(this::getBlockPos, "signalLength", 60, 5, 400, 5, true); //20 seconds max

	public KeypadTileEntity() {
		super(SCContent.teTypeKeypad);
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		super.save(tag);

		if (passcode != null && !passcode.isEmpty())
			tag.putString("passcode", passcode);

		return tag;
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);

		passcode = tag.getString("passcode");
	}

	@Override
	public void activate(PlayerEntity player) {
		if (!level.isClientSide && getBlockState().getBlock() instanceof KeypadBlock)
			((KeypadBlock) getBlockState().getBlock()).activate(getBlockState(), level, worldPosition, signalLength.get());
	}

	@Override
	public void openPasswordGUI(PlayerEntity player) {
		if (getPassword() != null) {
			if (player instanceof ServerPlayerEntity) {
				NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
					@Override
					public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
						return new GenericTEContainer(SCContent.cTypeCheckPassword, windowId, level, worldPosition);
					}

					@Override
					public ITextComponent getDisplayName() {
						return KeypadTileEntity.super.getDisplayName();
					}
				}, worldPosition);
			}
		}
		else {
			if (getOwner().isOwner(player)) {
				if (player instanceof ServerPlayerEntity) {
					NetworkHooks.openGui((ServerPlayerEntity) player, new INamedContainerProvider() {
						@Override
						public Container createMenu(int windowId, PlayerInventory inv, PlayerEntity player) {
							return new GenericTEContainer(SCContent.cTypeSetPassword, windowId, level, worldPosition);
						}

						@Override
						public ITextComponent getDisplayName() {
							return KeypadTileEntity.super.getDisplayName();
						}
					}, worldPosition);
				}
			}
			else
				PlayerUtils.sendMessageToPlayer(player, new StringTextComponent("SecurityCraft"), Utils.localize("messages.securitycraft:passwordProtected.notSetUp"), TextFormatting.DARK_RED);
		}
	}

	@Override
	public boolean onCodebreakerUsed(BlockState blockState, PlayerEntity player) {
		if (!blockState.getValue(KeypadBlock.POWERED)) {
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
		passcode = password;
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST, ModuleType.DENYLIST, ModuleType.DISGUISE
		};
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				isAlwaysActive, sendMessage, signalLength
		};
	}

	public boolean sendsMessages() {
		return sendMessage.get();
	}

	public int getSignalLength() {
		return signalLength.get();
	}
}

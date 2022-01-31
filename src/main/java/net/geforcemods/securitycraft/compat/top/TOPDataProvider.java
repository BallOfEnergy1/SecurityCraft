package net.geforcemods.securitycraft.compat.top;

import java.util.function.Function;

import javax.annotation.Nullable;

import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeHitEntityData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoEntityProvider;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.api.IPasswordProtected;
import net.geforcemods.securitycraft.blockentities.KeycardReaderBlockEntity;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.geforcemods.securitycraft.entity.Sentry;
import net.geforcemods.securitycraft.entity.Sentry.SentryMode;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.INameable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;

public class TOPDataProvider implements Function<ITheOneProbe, Void> {
	private final String formatting = TextFormatting.BLUE.toString() + TextFormatting.ITALIC.toString();

	@Nullable
	@Override
	public Void apply(ITheOneProbe theOneProbe) {
		theOneProbe.registerBlockDisplayOverride((mode, probeInfo, player, world, blockState, data) -> {
			ItemStack disguisedAs = ItemStack.EMPTY;

			if (blockState.getBlock() instanceof DisguisableBlock)
				disguisedAs = ((DisguisableBlock) blockState.getBlock()).getDisguisedStack(world, data.getPos());
			else if (blockState.getBlock() instanceof IOverlayDisplay)
				disguisedAs = ((IOverlayDisplay) blockState.getBlock()).getDisplayStack(world, blockState, data.getPos());

			if (!disguisedAs.isEmpty()) {
				//@formatter:off
				probeInfo.horizontal()
				.item(disguisedAs)
				.vertical()
				.itemLabel(disguisedAs)
				.text(formatting + ModList.get().getModContainerById(disguisedAs.getItem().getRegistryName().getNamespace()).get().getModInfo().getDisplayName());
				return true;
				//@formatter:on
			}

			return false;
		});
		theOneProbe.registerProvider(new IProbeInfoProvider() {
			@Override
			public String getID() {
				return SecurityCraft.MODID + ":" + SecurityCraft.MODID;
			}

			@Override
			public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
				Block block = blockState.getBlock();

				if (block instanceof IOverlayDisplay && !((IOverlayDisplay) block).shouldShowSCInfo(world, blockState, data.getPos()))
					return;

				TileEntity te = world.getBlockEntity(data.getPos());

				if (te instanceof IOwnable) {
					String ownerName = ((IOwnable) te).getOwner().getName();

					if (ConfigHandler.SERVER.enableTeamOwnership.get()) {
						ScorePlayerTeam team = PlayerUtils.getPlayersTeam(ownerName);

						if (team != null)
							ownerName = Utils.localize("messages.securitycraft:teamOwner", team.getColor() + team.getDisplayName().getColoredString() + TextFormatting.GRAY).getColoredString(); //TOP does not work with normal component formatting
					}

					probeInfo.vertical().text(new StringTextComponent(TextFormatting.GRAY + Utils.localize("waila.securitycraft:owner", ownerName).getColoredString()).getColoredString());
				}

				//if the te is ownable, show modules only when it's owned, otherwise always show
				if (te instanceof IModuleInventory && (!(te instanceof IOwnable) || ((IOwnable) te).getOwner().isOwner(player))) {
					if (!((IModuleInventory) te).getInsertedModules().isEmpty()) {
						probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:equipped").getColoredString());

						for (ModuleType module : ((IModuleInventory) te).getInsertedModules()) {
							probeInfo.text(TextFormatting.GRAY + "- " + new TranslationTextComponent(module.getTranslationKey()).getColoredString());
						}
					}
				}

				if (te instanceof IPasswordProtected && !(te instanceof KeycardReaderBlockEntity) && ((IOwnable) te).getOwner().isOwner(player)) {
					String password = ((IPasswordProtected) te).getPassword();

					probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:password").getColoredString() + " " + (password != null && !password.isEmpty() ? password : Utils.localize("waila.securitycraft:password.notSet").getColoredString()));
				}

				if (te instanceof INameable && ((INameable) te).hasCustomName()) {
					ITextComponent text = ((INameable) te).getCustomName();
					String name = text == null ? "" : text.getColoredString();

					probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:customName", name).getColoredString() + " " + name);
				}
			}
		});
		theOneProbe.registerEntityProvider(new IProbeInfoEntityProvider() {
			@Override
			public String getID() {
				return SecurityCraft.MODID + ":" + SecurityCraft.MODID;
			}

			@Override
			public void addProbeEntityInfo(ProbeMode probeMode, IProbeInfo probeInfo, PlayerEntity player, World world, Entity entity, IProbeHitEntityData data) {
				if (entity instanceof Sentry) {
					Sentry sentry = (Sentry) entity;
					SentryMode mode = sentry.getMode();
					String ownerName = sentry.getOwner().getName();

					if (ConfigHandler.SERVER.enableTeamOwnership.get()) {
						ScorePlayerTeam team = PlayerUtils.getPlayersTeam(ownerName);

						if (team != null)
							ownerName = Utils.localize("messages.securitycraft:teamOwner", team.getColor() + team.getDisplayName().getColoredString() + TextFormatting.GRAY).getColoredString(); //TOP does not work with normal component formatting
					}

					probeInfo.text(new StringTextComponent(TextFormatting.GRAY + Utils.localize("waila.securitycraft:owner", ownerName).getColoredString()).getColoredString());

					if (!sentry.getAllowlistModule().isEmpty() || !sentry.getDisguiseModule().isEmpty() || sentry.hasSpeedModule()) {
						probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:equipped").getColoredString());

						if (!sentry.getAllowlistModule().isEmpty())
							probeInfo.text(TextFormatting.GRAY + "- " + new TranslationTextComponent(ModuleType.ALLOWLIST.getTranslationKey()).getColoredString());

						if (!sentry.getDisguiseModule().isEmpty())
							probeInfo.text(TextFormatting.GRAY + "- " + new TranslationTextComponent(ModuleType.DISGUISE.getTranslationKey()).getColoredString());

						if (sentry.hasSpeedModule())
							probeInfo.text(TextFormatting.GRAY + "- " + new TranslationTextComponent(ModuleType.SPEED.getTranslationKey()).getColoredString());
					}

					String modeDescription = Utils.localize(mode.getModeKey()).getColoredString();

					if (mode != SentryMode.IDLE)
						modeDescription += "- " + Utils.localize(mode.getTargetKey()).getColoredString();

					probeInfo.text(TextFormatting.GRAY + modeDescription);
				}
			}
		});
		return null;
	}
}
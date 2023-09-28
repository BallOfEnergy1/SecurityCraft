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
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.DisguisableBlock;
import net.geforcemods.securitycraft.blocks.FakeLavaBaseBlock;
import net.geforcemods.securitycraft.blocks.FakeWaterBaseBlock;
import net.geforcemods.securitycraft.compat.IOverlayDisplay;
import net.geforcemods.securitycraft.entity.sentry.Sentry;
import net.geforcemods.securitycraft.entity.sentry.Sentry.SentryMode;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

public class TOPDataProvider implements Function<ITheOneProbe, Void> {
	private final String formatting = TextFormatting.BLUE.toString() + TextFormatting.ITALIC.toString();

	@Nullable
	@Override
	public Void apply(ITheOneProbe theOneProbe) {
		theOneProbe.registerBlockDisplayOverride((mode, probeInfo, player, world, blockState, data) -> {
			boolean edited = false;
			ItemStack item = ItemStack.EMPTY;
			ItemStack itemLabel = ItemStack.EMPTY;
			String labelText = "";
			String text = formatting + "Minecraft";

			//split up so the display override does not work for every block
			if (blockState.getBlock() instanceof DisguisableBlock) {
				item = ((DisguisableBlock) blockState.getBlock()).getDisguisedStack(world, data.getPos());
				itemLabel = item;
				text = formatting + Loader.instance().getIndexedModList().get(item.getItem().getRegistryName().getNamespace()).getName();
				edited = true;
			}
			else if (blockState.getBlock() instanceof FakeLavaBaseBlock) {
				item = new ItemStack(Items.LAVA_BUCKET);
				labelText = Utils.localize("tile.lava.name").getFormattedText();
				edited = true;
			}
			else if (blockState.getBlock() instanceof FakeWaterBaseBlock) {
				item = new ItemStack(Items.WATER_BUCKET);
				labelText = Utils.localize("tile.water.name").getFormattedText();
				edited = true;
			}
			else if (blockState.getBlock() instanceof IOverlayDisplay) {
				ItemStack displayStack = ((IOverlayDisplay) blockState.getBlock()).getDisplayStack(world, blockState, data.getPos());

				if (displayStack != null) {
					item = itemLabel = displayStack;
					edited = true;
				}
			}

			if (edited) {
				IProbeInfo info = probeInfo.horizontal().item(item).vertical();

				if (itemLabel.isEmpty())
					info.text(labelText);
				else
					info.itemLabel(itemLabel);

				info.text(text);
				return true;
			}

			return false;
		});
		theOneProbe.registerProvider(new IProbeInfoProvider() {
			@Override
			public String getID() {
				return SecurityCraft.MODID + ":" + SecurityCraft.MODID;
			}

			@Override
			public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
				Block block = blockState.getBlock();

				if (block instanceof IOverlayDisplay && !((IOverlayDisplay) block).shouldShowSCInfo(world, blockState, data.getPos()))
					return;

				TileEntity te = world.getTileEntity(data.getPos());

				if (te instanceof IOwnable)
					probeInfo.vertical().text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:owner", PlayerUtils.getOwnerComponent(((IOwnable) te).getOwner())).getFormattedText());

				//if the te is ownable, show modules only when it's owned, otherwise always show
				if (te instanceof IModuleInventory && (!(te instanceof IOwnable) || ((IOwnable) te).isOwnedBy(player)) && !((IModuleInventory) te).getInsertedModules().isEmpty()) {
					probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:equipped").getFormattedText());

					for (ModuleType module : ((IModuleInventory) te).getInsertedModules()) {
						probeInfo.text(TextFormatting.GRAY + "- " + Utils.localize(module.getTranslationKey()).getFormattedText());
					}
				}

				if (te instanceof IWorldNameable && ((IWorldNameable) te).hasCustomName()) {
					String name = ((IWorldNameable) te).getName();

					probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:customName").getFormattedText() + " " + name);
				}
			}
		});
		theOneProbe.registerEntityProvider(new IProbeInfoEntityProvider() {
			@Override
			public String getID() {
				return SecurityCraft.MODID + ":" + SecurityCraft.MODID;
			}

			@Override
			public void addProbeEntityInfo(ProbeMode probeMode, IProbeInfo probeInfo, EntityPlayer player, World world, Entity entity, IProbeHitEntityData data) {
				if (entity instanceof Sentry) {
					Sentry sentry = (Sentry) entity;
					SentryMode mode = sentry.getMode();

					probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:owner", PlayerUtils.getOwnerComponent(sentry.getOwner())).getFormattedText());

					if (!sentry.getAllowlistModule().isEmpty() || !sentry.getDisguiseModule().isEmpty() || sentry.hasSpeedModule()) {
						probeInfo.text(TextFormatting.GRAY + Utils.localize("waila.securitycraft:equipped").getFormattedText());

						if (!sentry.getAllowlistModule().isEmpty())
							probeInfo.text(TextFormatting.GRAY + "- " + Utils.localize(ModuleType.ALLOWLIST.getTranslationKey()).getFormattedText());

						if (!sentry.getDisguiseModule().isEmpty())
							probeInfo.text(TextFormatting.GRAY + "- " + Utils.localize(ModuleType.DISGUISE.getTranslationKey()).getFormattedText());

						if (sentry.hasSpeedModule())
							probeInfo.text(TextFormatting.GRAY + "- " + Utils.localize(ModuleType.SPEED.getTranslationKey()).getFormattedText());
					}

					String modeDescription = Utils.localize(mode.getModeKey()).getFormattedText();

					if (mode != SentryMode.IDLE)
						modeDescription += " - " + Utils.localize(mode.getTargetKey()).getFormattedText();

					probeInfo.text(TextFormatting.GRAY + modeDescription);
				}
			}
		});
		return null;
	}
}
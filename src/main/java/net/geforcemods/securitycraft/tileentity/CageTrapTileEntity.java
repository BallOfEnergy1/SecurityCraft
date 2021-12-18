package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.misc.ModuleType;

public class CageTrapTileEntity extends DisguisableTileEntity {
	private BooleanOption shouldCaptureMobsOption = new BooleanOption("captureMobs", false);

	public CageTrapTileEntity() {
		super(SCContent.teTypeCageTrap);
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.DISGUISE, ModuleType.ALLOWLIST
		};
	}

	public boolean capturesMobs() {
		return shouldCaptureMobsOption.get();
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				shouldCaptureMobsOption
		};
	}
}

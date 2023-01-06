package net.geforcemods.securitycraft.blockentities;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.api.CustomizableBlockEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.api.Option.BooleanOption;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedCauldronBlockEntity extends CustomizableBlockEntity {
	private final BooleanOption isPublic = new BooleanOption("isPublic", false);

	public ReinforcedCauldronBlockEntity(BlockPos pos, BlockState state) {
		super(SCContent.REINFORCED_CAULDRON_BLOCK_ENTITY.get(), pos, state);
	}

	public boolean isAllowedToInteract(Player player) {
		return isPublic.get() || isOwnedBy(player) || isAllowed(player);
	}

	@Override
	public Option<?>[] customOptions() {
		return new Option[] {
				isPublic
		};
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[] {
				ModuleType.ALLOWLIST
		};
	}
}

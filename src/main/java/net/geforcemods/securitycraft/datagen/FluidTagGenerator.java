package net.geforcemods.securitycraft.datagen;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FluidTagGenerator extends FluidTagsProvider {
	public FluidTagGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, SecurityCraft.MODID, existingFileHelper);
	}

	@Override
	protected void registerTags() {
		getOrCreateBuilder(FluidTags.LAVA).add(SCContent.FAKE_LAVA.get(), SCContent.FLOWING_FAKE_LAVA.get());
		getOrCreateBuilder(FluidTags.WATER).add(SCContent.FAKE_WATER.get(), SCContent.FLOWING_FAKE_WATER.get());
	}

	@Override
	public String getName() {
		return "SecurityCraft Fluid Tags";
	}
}

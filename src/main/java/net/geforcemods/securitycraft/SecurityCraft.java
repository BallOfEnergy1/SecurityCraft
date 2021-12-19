package net.geforcemods.securitycraft;

import java.lang.reflect.Field;

import net.geforcemods.securitycraft.api.SecurityCraftAPI;
import net.geforcemods.securitycraft.blocks.InventoryScannerBlock;
import net.geforcemods.securitycraft.blocks.KeypadBlock;
import net.geforcemods.securitycraft.blocks.KeypadChestBlock;
import net.geforcemods.securitycraft.blocks.KeypadFurnaceBlock;
import net.geforcemods.securitycraft.blocks.reinforced.IReinforcedBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedCauldronBlock.IReinforcedCauldronInteraction;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedHopperBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedPressurePlateBlock;
import net.geforcemods.securitycraft.blocks.reinforced.ReinforcedRedstoneBlock;
import net.geforcemods.securitycraft.commands.SCCommand;
import net.geforcemods.securitycraft.compat.lycanitesmobs.LycanitesMobsCompat;
import net.geforcemods.securitycraft.compat.quark.QuarkCompat;
import net.geforcemods.securitycraft.compat.top.TOPDataProvider;
import net.geforcemods.securitycraft.itemgroups.SCDecorationTab;
import net.geforcemods.securitycraft.itemgroups.SCExplosivesTab;
import net.geforcemods.securitycraft.itemgroups.SCTechnicalTab;
import net.geforcemods.securitycraft.items.SCManualItem;
import net.geforcemods.securitycraft.misc.CommonDoorActivator;
import net.geforcemods.securitycraft.misc.SCManualPage;
import net.geforcemods.securitycraft.misc.conditions.BlockEntityNBTCondition;
import net.geforcemods.securitycraft.util.HasManualPage;
import net.geforcemods.securitycraft.util.Reinforced;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;

@Mod(SecurityCraft.MODID)
@EventBusSubscriber(modid = SecurityCraft.MODID, bus = Bus.MOD)
public class SecurityCraft {
	public static final String MODID = "securitycraft";
	public static final String PROTOCOL_VERSION = "4";
	public static SimpleChannel channel = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static CreativeModeTab technicalTab = new SCTechnicalTab();
	public static CreativeModeTab mineTab = new SCExplosivesTab();
	public static CreativeModeTab decorationTab = new SCDecorationTab();
	public static final LootItemConditionType BLOCK_ENTITY_NBT_LOOT_CONDITION = LootItemConditions.register(SecurityCraft.MODID + ":tile_entity_nbt", new BlockEntityNBTCondition.ConditionSerializer());

	public SecurityCraft() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.SERVER_SPEC);
		SCContent.BLOCKS.register(modEventBus);
		SCContent.FLUIDS.register(modEventBus);
		SCContent.ITEMS.register(modEventBus);
	}

	@SubscribeEvent
	public static void onFMLCommonSetup(FMLCommonSetupEvent event) { //stage 1
		RegistrationHandler.registerPackets();
	}

	@SubscribeEvent
	public static void onInterModEnqueue(InterModEnqueueEvent event) { //stage 3
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_EXTRACTION_BLOCK_MSG, ReinforcedHopperBlock.ExtractionBlock::new);
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_PASSWORD_CONVERTIBLE_MSG, KeypadBlock.Convertible::new);
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_PASSWORD_CONVERTIBLE_MSG, KeypadChestBlock.Convertible::new);
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_PASSWORD_CONVERTIBLE_MSG, KeypadFurnaceBlock.Convertible::new);
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, CommonDoorActivator::new);
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, InventoryScannerBlock.DoorActivator::new);
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, ReinforcedPressurePlateBlock.DoorActivator::new);
		InterModComms.sendTo(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, ReinforcedRedstoneBlock.DoorActivator::new);

		if (ModList.get().isLoaded("theoneprobe"))
			InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPDataProvider::new);

		if (ModList.get().isLoaded("lycanitesmobs"))
			InterModComms.sendTo(MODID, SecurityCraftAPI.IMC_SENTRY_ATTACK_TARGET_MSG, LycanitesMobsCompat::new);

		if (ModList.get().isLoaded("quark"))
			QuarkCompat.registerChestConversions();
	}

	@SubscribeEvent
	public static void onInterModProcess(InterModProcessEvent event) { //stage 4
		for (Field field : SCContent.class.getFields()) {
			try {
				if (field.isAnnotationPresent(Reinforced.class)) {
					Block block = ((RegistryObject<Block>) field.get(null)).get();
					IReinforcedBlock rb = (IReinforcedBlock) block;

					IReinforcedBlock.VANILLA_TO_SECURITYCRAFT.put(rb.getVanillaBlock(), block);
					IReinforcedBlock.SECURITYCRAFT_TO_VANILLA.put(block, rb.getVanillaBlock());
				}

				if (field.isAnnotationPresent(HasManualPage.class)) {
					Object o = ((RegistryObject<?>) field.get(null)).get();
					HasManualPage hmp = field.getAnnotation(HasManualPage.class);
					Item item = ((ItemLike) o).asItem();
					String key = "help.";

					if (hmp.specialInfoKey().isEmpty())
						key += item.getDescriptionId().substring(5) + ".info";
					else
						key += hmp.specialInfoKey();

					SCManualItem.PAGES.add(new SCManualPage(item, new TranslatableComponent(key.replace("..", ".")), hmp.designedBy(), hmp.hasRecipeDescription()));
				}
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		ForgeChunkManager.setForcedChunkLoadingCallback(SecurityCraft.MODID, (level, ticketHelper) -> { //this will only check against SecurityCraft's camera chunks, so no need to add an (instanceof SecurityCamera) somewhere
			ticketHelper.getEntityTickets().forEach(((uuid, chunk) -> {
				if (level.getEntity(uuid) == null)
					ticketHelper.removeAllTickets(uuid);
			}));
		});

		IReinforcedCauldronInteraction.bootStrap();
	}

	public void registerCommands(RegisterCommandsEvent event) {
		SCCommand.register(event.getDispatcher());
	}

	public static String getVersion() {
		return "v" + ModList.get().getModContainerById(MODID).get().getModInfo().getVersion().toString();
	}
}

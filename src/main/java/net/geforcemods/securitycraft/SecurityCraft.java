package net.geforcemods.securitycraft;

import java.lang.reflect.Field;

import net.geforcemods.securitycraft.api.SecurityCraftAPI;
import net.geforcemods.securitycraft.blocks.BlockInventoryScanner;
import net.geforcemods.securitycraft.blocks.BlockKeypad;
import net.geforcemods.securitycraft.blocks.BlockKeypadChest;
import net.geforcemods.securitycraft.blocks.BlockKeypadFurnace;
import net.geforcemods.securitycraft.blocks.reinforced.BlockReinforcedHopper;
import net.geforcemods.securitycraft.blocks.reinforced.BlockReinforcedMetals;
import net.geforcemods.securitycraft.blocks.reinforced.BlockReinforcedPressurePlate;
import net.geforcemods.securitycraft.blocks.reinforced.IReinforcedBlock;
import net.geforcemods.securitycraft.commands.CommandSC;
import net.geforcemods.securitycraft.compat.cyclic.CyclicCompat;
import net.geforcemods.securitycraft.compat.icbmclassic.ICBMClassicEMPCompat;
import net.geforcemods.securitycraft.compat.lycanitesmobs.LycanitesMobsCompat;
import net.geforcemods.securitycraft.compat.projecte.ProjectECompat;
import net.geforcemods.securitycraft.compat.quark.QuarkCompat;
import net.geforcemods.securitycraft.compat.versionchecker.VersionUpdateChecker;
import net.geforcemods.securitycraft.gui.GuiHandler;
import net.geforcemods.securitycraft.misc.CommonDoorActivator;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.network.IProxy;
import net.geforcemods.securitycraft.tabs.CreativeTabSCDecoration;
import net.geforcemods.securitycraft.tabs.CreativeTabSCExplosives;
import net.geforcemods.securitycraft.tabs.CreativeTabSCTechnical;
import net.geforcemods.securitycraft.util.Reinforced;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Type;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = SecurityCraft.MODID, name = "SecurityCraft", dependencies = "required-after:forge@[14.23.5.2826,)", updateJSON = "https://www.github.com/Geforce132/SecurityCraft/raw/master/Updates/Forge.json", acceptedMinecraftVersions = "[1.12.2]")
public class SecurityCraft {
	public static final String MODID = "securitycraft";
	@SidedProxy(clientSide = "net.geforcemods.securitycraft.network.ClientProxy", serverSide = "net.geforcemods.securitycraft.network.ServerProxy")
	public static IProxy proxy;
	@Instance(MODID)
	public static SecurityCraft instance = new SecurityCraft();
	public static SimpleNetworkWrapper network;
	private GuiHandler guiHandler = new GuiHandler();
	public static CreativeTabs tabSCTechnical = new CreativeTabSCTechnical();
	public static CreativeTabs tabSCMine = new CreativeTabSCExplosives();
	public static CreativeTabs tabSCDecoration = new CreativeTabSCDecoration();

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandSC());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		SecurityCraft.network = NetworkRegistry.INSTANCE.newSimpleChannel(SecurityCraft.MODID);
		RegistrationHandler.registerPackets(SecurityCraft.network);
		SetupHandler.setupBlocks();
		SetupHandler.setupMines();
		SetupHandler.setupItems();
		proxy.registerEntityRenderingHandlers();

		if (Loader.isModLoaded("icbmclassic"))
			MinecraftForge.EVENT_BUS.register(new ICBMClassicEMPCompat());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_EXTRACTION_BLOCK_MSG, BlockReinforcedHopper.ExtractionBlock.class.getName());
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_PASSWORD_CONVERTIBLE_MSG, BlockKeypad.Convertible.class.getName());
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_PASSWORD_CONVERTIBLE_MSG, BlockKeypadChest.Convertible.class.getName());
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_PASSWORD_CONVERTIBLE_MSG, BlockKeypadFurnace.Convertible.class.getName());
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, CommonDoorActivator.class.getName());
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, BlockInventoryScanner.DoorActivator.class.getName());
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, BlockReinforcedPressurePlate.DoorActivator.class.getName());
		FMLInterModComms.sendFunctionMessage(SecurityCraft.MODID, SecurityCraftAPI.IMC_DOOR_ACTIVATOR_MSG, BlockReinforcedMetals.DoorActivator.class.getName());
		FMLInterModComms.sendMessage("waila", "register", "net.geforcemods.securitycraft.compat.waila.WailaDataProvider.callbackRegister");
		FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "net.geforcemods.securitycraft.compat.top.TOPDataProvider");

		if (Loader.isModLoaded("lycanitesmobs"))
			FMLInterModComms.sendFunctionMessage(MODID, SecurityCraftAPI.IMC_SENTRY_ATTACK_TARGET_MSG, LycanitesMobsCompat.class.getName());

		if (Loader.isModLoaded("quark"))
			QuarkCompat.registerChestConversion();

		if (ConfigHandler.checkForUpdates) {
			NBTTagCompound vcUpdateTag = VersionUpdateChecker.getNBTTagCompound();

			if (vcUpdateTag != null)
				FMLInterModComms.sendRuntimeMessage(MODID, "VersionChecker", "addUpdate", vcUpdateTag);
		}

		NetworkRegistry.INSTANCE.registerGuiHandler(this, guiHandler);
		EnumModuleType.refresh();
		proxy.registerRenderThings();
		FMLCommonHandler.instance().getDataFixer().init(SecurityCraft.MODID, TileEntityIDDataFixer.VERSION).registerFix(FixTypes.BLOCK_ENTITY, new TileEntityIDDataFixer());
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedCobblestone), new ItemStack(SCContent.reinforcedStone, 1, 0), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedSand, 1, 0), new ItemStack(SCContent.reinforcedGlass, 1, 0), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedSand, 1, 1), new ItemStack(SCContent.reinforcedGlass, 1, 0), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStoneBrick, 1, 0), new ItemStack(SCContent.reinforcedStoneBrick, 1, 2), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedClay, 1, 0), new ItemStack(SCContent.reinforcedHardenedClay, 1, 0), 0.35F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.WHITE.getMetadata()), new ItemStack(SCContent.reinforcedWhiteGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.ORANGE.getMetadata()), new ItemStack(SCContent.reinforcedOrangeGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.MAGENTA.getMetadata()), new ItemStack(SCContent.reinforcedMagentaGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.LIGHT_BLUE.getMetadata()), new ItemStack(SCContent.reinforcedLightBlueGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.YELLOW.getMetadata()), new ItemStack(SCContent.reinforcedYellowGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.LIME.getMetadata()), new ItemStack(SCContent.reinforcedLimeGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.PINK.getMetadata()), new ItemStack(SCContent.reinforcedPinkGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.GRAY.getMetadata()), new ItemStack(SCContent.reinforcedGrayGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.SILVER.getMetadata()), new ItemStack(SCContent.reinforcedSilverGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.CYAN.getMetadata()), new ItemStack(SCContent.reinforcedCyanGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.PURPLE.getMetadata()), new ItemStack(SCContent.reinforcedPurpleGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.BLUE.getMetadata()), new ItemStack(SCContent.reinforcedBlueGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.BROWN.getMetadata()), new ItemStack(SCContent.reinforcedBrownGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.GREEN.getMetadata()), new ItemStack(SCContent.reinforcedGreenGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.RED.getMetadata()), new ItemStack(SCContent.reinforcedRedGlazedTerracotta), 0.1F);
		GameRegistry.addSmelting(new ItemStack(SCContent.reinforcedStainedHardenedClay, 1, EnumDyeColor.BLACK.getMetadata()), new ItemStack(SCContent.reinforcedBlackGlazedTerracotta), 0.1F);
	}

	@EventHandler
	public void onIMC(IMCEvent event) {
		SecurityCraftAPI.onIMC(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Loader.isModLoaded("cyclicmagic"))
			MinecraftForge.EVENT_BUS.register(new CyclicCompat());

		for (Field field : SCContent.class.getFields()) {
			try {
				if (field.isAnnotationPresent(Reinforced.class))
					IReinforcedBlock.BLOCKS.add((Block) field.get(null));
			}
			catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		if (Loader.isModLoaded("projecte") && !Loader.isModLoaded("projecteintegration")) //ProjectE Integration already adds support for SecurityCraft, so no need to add our own
			ProjectECompat.registerConversions();

		ForgeChunkManager.setForcedChunkLoadingCallback(instance, (tickets, world) -> { //this will only check against SecurityCraft's camera chunks, so no need to add an (instanceof SecurityCameraEntity) somewhere
			tickets.forEach(ticket -> {
				if (ticket.getType() == Type.ENTITY && ((WorldServer) ticket.world).getEntityFromUuid(ticket.getEntity().getPersistentID()) == null)
					ForgeChunkManager.releaseTicket(ticket);
			});
		});
		ConfigHandler.loadEffects();
	}

	public static String getVersion() {
		return Loader.instance().activeModContainer().getVersion();
	}
}

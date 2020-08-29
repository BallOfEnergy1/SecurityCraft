package net.geforcemods.securitycraft.entity;

import java.util.ArrayList;

import org.lwjgl.input.Mouse;

import net.geforcemods.securitycraft.ConfigHandler;
import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.IModuleInventory;
import net.geforcemods.securitycraft.blocks.BlockSecurityCamera;
import net.geforcemods.securitycraft.items.ItemCameraMonitor;
import net.geforcemods.securitycraft.misc.CameraView;
import net.geforcemods.securitycraft.misc.EnumModuleType;
import net.geforcemods.securitycraft.misc.KeyBindings;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.packets.PacketCSetPlayerPositionAndRotation;
import net.geforcemods.securitycraft.network.packets.PacketGivePotionEffect;
import net.geforcemods.securitycraft.network.packets.PacketSSetCameraRotation;
import net.geforcemods.securitycraft.network.packets.PacketSetCameraPowered;
import net.geforcemods.securitycraft.tileentity.TileEntitySecurityCamera;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntitySecurityCamera extends Entity{

	private final float CAMERA_SPEED = ConfigHandler.cameraSpeed;

	public int blockPosX;
	public int blockPosY;
	public int blockPosZ;

	private double cameraUseX;
	private double cameraUseY;
	private double cameraUseZ;
	private float cameraUseYaw;
	private float cameraUsePitch;

	private int id;
	private int screenshotCooldown = 0;
	private int redstoneCooldown = 0;
	private int toggleNightVisionCooldown = 0;
	private int toggleLightCooldown = 0;
	private boolean shouldProvideNightVision = false;
	private float zoomAmount = 1F;

	private String playerViewingName = null;
	private boolean zooming = false;

	public EntitySecurityCamera(World world){
		super(world);
		noClip = true;
		height = 0.0001F;
		width = 0.0001F;
	}

	public EntitySecurityCamera(World world, double x, double y, double z, int id, EntityPlayer player){
		this(world);
		blockPosX = (int) x;
		blockPosY = (int) y;
		blockPosZ = (int) z;
		cameraUseX = player.posX;
		cameraUseY = player.posY;
		cameraUseZ = player.posZ;
		cameraUseYaw = player.rotationYaw;
		cameraUsePitch = player.rotationPitch;
		this.id = id;
		playerViewingName = player.getName();
		setPosition(x + 0.5D, y, z + 0.5D);

		TileEntity te = world.getTileEntity(getPosition());

		if(te instanceof TileEntitySecurityCamera)
			setInitialPitchYaw((TileEntitySecurityCamera)te);
	}

	public EntitySecurityCamera(World world, double x, double y, double z, int id, EntitySecurityCamera camera){
		this(world);
		blockPosX = (int) x;
		blockPosY = (int) y;
		blockPosZ = (int) z;
		cameraUseX = camera.cameraUseX;
		cameraUseY = camera.cameraUseY;
		cameraUseZ = camera.cameraUseZ;
		cameraUseYaw = camera.cameraUseYaw;
		cameraUsePitch = camera.cameraUsePitch;
		this.id = id;
		playerViewingName = camera.playerViewingName;
		setPosition(x + 0.5D, y, z + 0.5D);

		TileEntity te = world.getTileEntity(getPosition());

		if(te instanceof TileEntitySecurityCamera)
			setInitialPitchYaw((TileEntitySecurityCamera)te);
	}

	private void setInitialPitchYaw(TileEntitySecurityCamera te)
	{
		if(te != null && te.hasModule(EnumModuleType.SMART) && te.lastPitch != Float.MAX_VALUE && te.lastYaw != Float.MAX_VALUE)
		{
			rotationPitch = te.lastPitch;
			rotationYaw = te.lastYaw;
		}
		else
		{
			rotationPitch = 30F;

			EnumFacing facing = BlockUtils.getBlockProperty(world, BlockUtils.toPos((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)), BlockSecurityCamera.FACING);

			if(facing == EnumFacing.NORTH)
				rotationYaw = 180F;
			else if(facing == EnumFacing.WEST)
				rotationYaw = 90F;
			else if(facing == EnumFacing.SOUTH)
				rotationYaw = 0F;
			else if(facing == EnumFacing.EAST)
				rotationYaw = 270F;
			else if(facing == EnumFacing.DOWN)
				rotationPitch = 75;
		}
	}

	@Override
	public double getMountedYOffset(){
		return height * -7500D;
	}

	@Override
	protected boolean shouldSetPosAfterLoading(){
		return false;
	}

	@Override
	public boolean shouldDismountInWater(Entity rider){
		return true;
	}

	@Override
	public void onUpdate(){
		if(world.isRemote && isBeingRidden()){
			EntityPlayer lowestEntity = (EntityPlayer)getPassengers().get(0);

			if(lowestEntity != Minecraft.getMinecraft().player)
				return;

			if(screenshotCooldown > 0)
				screenshotCooldown -= 1;

			if(redstoneCooldown > 0)
				redstoneCooldown -= 1;

			if(toggleNightVisionCooldown > 0)
				toggleNightVisionCooldown -= 1;

			if(toggleLightCooldown > 0)
				toggleLightCooldown -= 1;

			if(lowestEntity.rotationYaw != rotationYaw){
				lowestEntity.setPositionAndRotation(lowestEntity.posX, lowestEntity.posY, lowestEntity.posZ, rotationYaw, rotationPitch);
				lowestEntity.rotationYaw = rotationYaw;
			}

			if(lowestEntity.rotationPitch != rotationPitch)
				lowestEntity.setPositionAndRotation(lowestEntity.posX, lowestEntity.posY, lowestEntity.posZ, rotationYaw, rotationPitch);

			checkKeysPressed();

			if(Mouse.hasWheel() && Mouse.isButtonDown(2) && screenshotCooldown == 0){
				screenshotCooldown = 30;
				ClientUtils.takeScreenshot();
				Minecraft.getMinecraft().world.playSound(new BlockPos(posX, posY, posZ), SoundEvent.REGISTRY.getObject(SCSounds.CAMERASNAP.location), SoundCategory.BLOCKS, 1.0F, 1.0F, true);
			}

			if(getPassengers().size() != 0 && shouldProvideNightVision)
				SecurityCraft.network.sendToServer(new PacketGivePotionEffect(Potion.getIdFromPotion(Potion.getPotionFromResourceLocation("night_vision")), 3, -1));
		}

		if(!world.isRemote)
			if(getPassengers().size() == 0 || BlockUtils.getBlock(world, blockPosX, blockPosY, blockPosZ) != SCContent.securityCamera){
				setDead();
				return;
			}
	}

	@SideOnly(Side.CLIENT)
	private void checkKeysPressed() {
		if(Minecraft.getMinecraft().gameSettings.keyBindForward.isKeyDown())
			moveViewUp();

		if(Minecraft.getMinecraft().gameSettings.keyBindBack.isKeyDown())
			moveViewDown();

		if(Minecraft.getMinecraft().gameSettings.keyBindLeft.isKeyDown())
			moveViewLeft();

		if(Minecraft.getMinecraft().gameSettings.keyBindRight.isKeyDown())
			moveViewRight();

		if(KeyBindings.cameraEmitRedstone.isPressed() && redstoneCooldown == 0){
			setRedstonePower();
			redstoneCooldown = 30;
		}

		if(KeyBindings.cameraActivateNightVision.isPressed() && toggleNightVisionCooldown == 0)
			enableNightVision();

		if(KeyBindings.cameraZoomIn.isPressed())
		{
			zoomIn();
			zooming = true;
		}
		else if(KeyBindings.cameraZoomOut.isPressed())
		{
			zoomOut();
			zooming = true;
		}
		else
			zooming = false;

		if(KeyBindings.cameraPrevious.isPressed())
		{
			for(Entity e : getPassengers())
			{
				if(e instanceof EntityPlayer)
				{
					EntityPlayer player = ((EntityPlayer)e);

					if(player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemCameraMonitor)
					{
						ItemCameraMonitor monitor = (ItemCameraMonitor)player.getHeldItem(EnumHand.MAIN_HAND).getItem();
						ArrayList<CameraView> views = monitor.getCameraPositions(player.getHeldItem(EnumHand.MAIN_HAND).getTagCompound());
						ArrayList<CameraView> nonNull = new ArrayList<>();
						int newIndex = -1;
						BlockPos newPos;

						for(CameraView view : views)
						{
							if(view != null)
							{
								nonNull.add(view);

								if(view.getLocation() == getPosition().subtract(new Vec3i(0.5D, 1.0D, 0.5D)))
									newIndex = nonNull.indexOf(view) - 1;
							}
						}

						if(newIndex == -1)
							newIndex = nonNull.size() - 1;

						newPos = views.get(newIndex).getLocation();

						if(!(BlockUtils.getBlock(Minecraft.getMinecraft().world, newPos) instanceof BlockSecurityCamera))
							return;

						player.dismountRidingEntity();
						((BlockSecurityCamera) BlockUtils.getBlock(Minecraft.getMinecraft().world, newPos)).mountCamera(world, newPos.getX(), newPos.getY(), newPos.getZ(), -1 /*does not get used*/, player);
					}

					break;
				}
			}
		}

		//		if(KeyBindings.cameraNext.isPressed())
		//		{
		//			if(getRidingEntity() instanceof EntityPlayer)
		//			{
		//				EntityPlayer player = ((EntityPlayer)getRidingEntity());
		//
		//				if(player.getActiveItemStack().getItem() == SCContent.cameraMonitor)
		//				{
		//					ItemCameraMonitor monitor = (ItemCameraMonitor)player.getActiveItemStack().getItem();
		//					ArrayList<CameraView> views = monitor.getCameraPositions(player.getActiveItemStack().getTagCompound());
		//					ArrayList<CameraView> nonNull = new ArrayList<CameraView>();
		//					int newIndex = -1;
		//					BlockPos newPos;
		//
		//					for(CameraView view : views)
		//					{
		//						if(view != null)
		//						{
		//							nonNull.add(view);
		//
		//							if(view.getLocation() == getPosition().subtract(new Vec3i(0.5D, 1.0D, 0.5D)))
		//								newIndex = nonNull.indexOf(view) + 1;
		//						}
		//					}
		//
		//					if(newIndex == nonNull.size())
		//						newIndex = 0;
		//
		//					newPos = views.get(newIndex).getLocation();
		//
		//					if(!(BlockUtils.getBlock(Minecraft.getMinecraft().world, newPos) instanceof BlockSecurityCamera))
		//						return;
		//
		//					((BlockSecurityCamera) BlockUtils.getBlock(Minecraft.getMinecraft().world, newPos)).mountCamera(world, newPos.getX(), newPos.getY(), newPos.getZ(), -1 /*does not get used*/, player);
		//				}
		//			}
		//		}
	}

	public void moveViewUp() {
		if(isCameraDown())
		{
			if(rotationPitch > 40F)
				setRotation(rotationYaw, rotationPitch -= CAMERA_SPEED);
		}
		else if(rotationPitch > -25F)
			setRotation(rotationYaw, rotationPitch -= CAMERA_SPEED);

		updateServerRotation();
	}

	public void moveViewDown(){
		if(isCameraDown())
		{
			if(rotationPitch < 100F)
				setRotation(rotationYaw, rotationPitch += CAMERA_SPEED);
		}
		else if(rotationPitch < 60F)
			setRotation(rotationYaw, rotationPitch += CAMERA_SPEED);

		updateServerRotation();
	}

	public void moveViewLeft() {
		if(BlockUtils.hasBlockProperty(world, BlockUtils.toPos((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)), BlockSecurityCamera.FACING)) {
			EnumFacing facing = BlockUtils.getBlockProperty(world, BlockUtils.toPos((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)), BlockSecurityCamera.FACING);

			if(facing == EnumFacing.EAST)
			{
				if((rotationYaw - CAMERA_SPEED) > -180F)
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.WEST)
			{
				if((rotationYaw - CAMERA_SPEED) > 0F)
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.NORTH)
			{
				// Handles some problems the occurs from the way the rotationYaw value works in MC
				if((((rotationYaw - CAMERA_SPEED) > 90F) && ((rotationYaw - CAMERA_SPEED) < 185F)) || (((rotationYaw - CAMERA_SPEED) > -190F) && ((rotationYaw - CAMERA_SPEED) < -90F)))
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.SOUTH)
			{
				if((rotationYaw - CAMERA_SPEED) > -90F)
					setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.DOWN)
				setRotation(rotationYaw -= CAMERA_SPEED, rotationPitch);

			updateServerRotation();
		}
	}

	public void moveViewRight(){
		if(BlockUtils.hasBlockProperty(world, BlockUtils.toPos((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)), BlockSecurityCamera.FACING)) {
			EnumFacing facing = BlockUtils.getBlockProperty(world, BlockUtils.toPos((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ)), BlockSecurityCamera.FACING);

			if(facing == EnumFacing.EAST)
			{
				if((rotationYaw + CAMERA_SPEED) < 0F)
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.WEST)
			{
				if((rotationYaw + CAMERA_SPEED) < 180F)
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.NORTH)
			{
				if((((rotationYaw + CAMERA_SPEED) > 85F) && ((rotationYaw + CAMERA_SPEED) < 185F)) || ((rotationYaw + CAMERA_SPEED) < -95F) && ((rotationYaw + CAMERA_SPEED) > -180F))
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.SOUTH)
			{
				if((rotationYaw + CAMERA_SPEED) < 90F)
					setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);
			}
			else if(facing == EnumFacing.DOWN)
				setRotation(rotationYaw += CAMERA_SPEED, rotationPitch);

			updateServerRotation();
		}
	}

	public void zoomIn()
	{
		zoomAmount = Math.min(zoomAmount - 0.1F, 2.0F);

		if(!zooming)
			Minecraft.getMinecraft().world.playSound(getPosition(), SCSounds.CAMERAZOOMIN.event, SoundCategory.BLOCKS, 1.0F, 1.0F, true);
	}

	public void zoomOut()
	{
		zoomAmount = Math.max(zoomAmount + 0.1F, -0.5F);

		if(!zooming)
			Minecraft.getMinecraft().world.playSound(getPosition(), SCSounds.CAMERAZOOMIN.event, SoundCategory.BLOCKS, 1.0F, 1.0F, true);
	}

	public void setRedstonePower() {
		BlockPos pos = BlockUtils.toPos((int) Math.floor(posX), (int) posY, (int) Math.floor(posZ));

		if(((IModuleInventory) world.getTileEntity(pos)).hasModule(EnumModuleType.REDSTONE))
			if(BlockUtils.getBlockProperty(world, pos, BlockSecurityCamera.POWERED))
				SecurityCraft.network.sendToServer(new PacketSetCameraPowered(pos, false));
			else if(!BlockUtils.getBlockProperty(world, pos, BlockSecurityCamera.POWERED))
				SecurityCraft.network.sendToServer(new PacketSetCameraPowered(pos, true));
	}

	public void enableNightVision() {
		toggleNightVisionCooldown = 30;
		shouldProvideNightVision = !shouldProvideNightVision;
	}

	public float getZoomAmount(){
		return zoomAmount;
	}

	@SideOnly(Side.CLIENT)
	private void updateServerRotation(){
		SecurityCraft.network.sendToServer(new PacketSSetCameraRotation(rotationYaw, rotationPitch));
	}

	private boolean isCameraDown()
	{
		return world.getTileEntity(getPosition()) instanceof TileEntitySecurityCamera && ((TileEntitySecurityCamera)world.getTileEntity(getPosition())).down;
	}

	@Override
	public void setDead(){
		super.setDead();

		if(playerViewingName != null && PlayerUtils.isPlayerOnline(playerViewingName)){
			EntityPlayer player = PlayerUtils.getPlayerFromName(playerViewingName);
			player.setPositionAndUpdate(cameraUseX, cameraUseY, cameraUseZ);
			SecurityCraft.network.sendTo(new PacketCSetPlayerPositionAndRotation(cameraUseX, cameraUseY, cameraUseZ, cameraUseYaw, cameraUsePitch), (EntityPlayerMP) player);
		}
	}

	@Override
	protected void entityInit(){}

	@Override
	public void writeEntityToNBT(NBTTagCompound tag){
		tag.setInteger("CameraID", id);

		if(playerViewingName != null)
			tag.setString("playerName", playerViewingName);

		if(cameraUseX != 0.0D)
			tag.setDouble("cameraUseX", cameraUseX);

		if(cameraUseY != 0.0D)
			tag.setDouble("cameraUseY", cameraUseY);

		if(cameraUseZ != 0.0D)
			tag.setDouble("cameraUseZ", cameraUseZ);

		if(cameraUseYaw != 0.0D)
			tag.setDouble("cameraUseYaw", cameraUseYaw);

		if(cameraUsePitch != 0.0D)
			tag.setDouble("cameraUsePitch", cameraUsePitch);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag){
		id = tag.getInteger("CameraID");

		if(tag.hasKey("playerName"))
			playerViewingName = tag.getString("playerName");

		if(tag.hasKey("cameraUseX"))
			cameraUseX = tag.getDouble("cameraUseX");

		if(tag.hasKey("cameraUseY"))
			cameraUseY = tag.getDouble("cameraUseY");

		if(tag.hasKey("cameraUseZ"))
			cameraUseZ = tag.getDouble("cameraUseZ");

		if(tag.hasKey("cameraUseYaw"))
			cameraUseYaw = tag.getFloat("cameraUseYaw");

		if(tag.hasKey("cameraUsePitch"))
			cameraUsePitch = tag.getFloat("cameraUsePitch");
	}

}

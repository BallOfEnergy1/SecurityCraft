package net.geforcemods.securitycraft;

import net.geforcemods.securitycraft.api.IExplosive;
import net.geforcemods.securitycraft.api.ILockable;
import net.geforcemods.securitycraft.api.IOwnable;
import net.geforcemods.securitycraft.blocks.BlockDisguisable;
import net.geforcemods.securitycraft.entity.EntitySentry;
import net.geforcemods.securitycraft.entity.camera.EntitySecurityCamera;
import net.geforcemods.securitycraft.items.ItemSonicSecuritySystem;
import net.geforcemods.securitycraft.misc.SCSounds;
import net.geforcemods.securitycraft.network.ClientProxy;
import net.geforcemods.securitycraft.tileentity.TileEntitySecurityCamera;
import net.geforcemods.securitycraft.util.GuiUtils;
import net.geforcemods.securitycraft.util.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.ScreenshotEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@EventBusSubscriber(value=Side.CLIENT, modid=SecurityCraft.MODID)
public class SCClientEventHandler
{
	public static final ResourceLocation BEACON_GUI = new ResourceLocation("textures/gui/container/beacon.png");

	@SubscribeEvent
	public static void onScreenshot(ScreenshotEvent event)
	{
		EntityPlayer player = Minecraft.getMinecraft().player;

		if(PlayerUtils.isPlayerMountedOnCamera(player))
		{
			EntitySecurityCamera camera = (EntitySecurityCamera)Minecraft.getMinecraft().getRenderViewEntity();

			if(camera.screenshotSoundCooldown == 0)
			{
				camera.screenshotSoundCooldown = 7;
				Minecraft.getMinecraft().world.playSound(player.getPosition(), SCSounds.CAMERASNAP.event, SoundCategory.BLOCKS, 1.0F, 1.0F, true);
			}
		}
	}

	@SubscribeEvent
	public static void renderHandEvent(RenderHandEvent event){
		if(PlayerUtils.isPlayerMountedOnCamera(Minecraft.getMinecraft().player))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClickInput(MouseEvent event) {
		if(event.getButton() == 0 && ClientProxy.isPlayerMountedOnCamera())
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent.Pre event) {
		if((event.getType() == ElementType.EXPERIENCE || event.getType() == ElementType.JUMPBAR) && ClientProxy.isPlayerMountedOnCamera())
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent.Post event) {
		if(event.getType() == ElementType.ALL) {
			if(ClientProxy.isPlayerMountedOnCamera())
				//calling down() on the render view entity's position because the camera entity sits at y+0.5 by default and getPosition increases y by 0.5 again
				GuiUtils.drawCameraOverlay(Minecraft.getMinecraft(), Minecraft.getMinecraft().ingameGUI, event.getResolution(), Minecraft.getMinecraft().player, Minecraft.getMinecraft().world, Minecraft.getMinecraft().getRenderViewEntity().getPosition().down());
			else
			{
				Minecraft mc = Minecraft.getMinecraft();
				EntityPlayerSP player = mc.player;
				World world = player.getEntityWorld();

				for (EnumHand hand : EnumHand.values()) {
					ItemStack stack = player.getHeldItem(hand);
					int uCoord = 0;

					if(stack.getItem() == SCContent.cameraMonitor)
					{
						double eyeHeight = player.getEyeHeight();
						Vec3d lookVec = new Vec3d((player.posX + (player.getLookVec().x * 5)), ((eyeHeight + player.posY) + (player.getLookVec().y * 5)), (player.posZ + (player.getLookVec().z * 5)));
						RayTraceResult mop = world.rayTraceBlocks(new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ), lookVec);

						if(mop != null && mop.typeOfHit == Type.BLOCK && world.getTileEntity(mop.getBlockPos()) instanceof TileEntitySecurityCamera)
						{
							uCoord = 110;
							NBTTagCompound cameras = stack.getTagCompound();

							if(cameras != null) {
								for(int i = 1; i < 31; i++)
								{
									if(!cameras.hasKey("Camera" + i))
										continue;

									String[] coords = cameras.getString("Camera" + i).split(" ");

									if(Integer.parseInt(coords[0]) == mop.getBlockPos().getX() && Integer.parseInt(coords[1]) == mop.getBlockPos().getY() && Integer.parseInt(coords[2]) == mop.getBlockPos().getZ())
									{
										uCoord = 88;
										break;
									}
								}
							}
						}
					}
					else if(stack.getItem() == SCContent.remoteAccessMine)
					{
						double eyeHeight = player.getEyeHeight();
						Vec3d lookVec = new Vec3d((player.posX + (player.getLookVec().x * 5)), ((eyeHeight + player.posY) + (player.getLookVec().y * 5)), (player.posZ + (player.getLookVec().z * 5)));
						RayTraceResult mop = world.rayTraceBlocks(new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ), lookVec);

						if(mop != null && mop.typeOfHit == Type.BLOCK && world.getBlockState(mop.getBlockPos()).getBlock() instanceof IExplosive)
						{
							uCoord = 110;
							NBTTagCompound mines = stack.getTagCompound();

							if(mines != null) {
								for(int i = 1; i <= 6; i++)
								{
									if(stack.getTagCompound().getIntArray("mine" + i).length > 0)
									{
										int[] coords = mines.getIntArray("mine" + i);

										if(coords[0] == mop.getBlockPos().getX() && coords[1] == mop.getBlockPos().getY() && coords[2] == mop.getBlockPos().getZ())
										{
											uCoord = 88;
											break;
										}
									}
								}
							}
						}
					}
					else if(stack.getItem() == SCContent.remoteAccessSentry)
					{
						Entity hitEntity = Minecraft.getMinecraft().pointedEntity;

						if(hitEntity instanceof EntitySentry)
						{
							uCoord = 110;
							NBTTagCompound sentries = stack.getTagCompound();

							if(sentries != null) {
								for(int i = 1; i <= 12; i++)
								{
									if(stack.getTagCompound().getIntArray("sentry" + i).length > 0)
									{
										int[] coords = sentries.getIntArray("sentry" + i);
										if(coords[0] == hitEntity.getPosition().getX() && coords[1] == hitEntity.getPosition().getY() && coords[2] == hitEntity.getPosition().getZ())
										{
											uCoord = 88;
											break;
										}
									}
								}
							}
						}
					}
					else if(stack.getItem() == SCContent.sonicSecuritySystemItem)
					{
						double eyeHeight = player.getEyeHeight();
						Vec3d lookVec = new Vec3d((player.posX + (player.getLookVec().x * 5)), ((eyeHeight + player.posY) + (player.getLookVec().y * 5)), (player.posZ + (player.getLookVec().z * 5)));
						RayTraceResult mop = world.rayTraceBlocks(new Vec3d(player.posX, player.posY + player.getEyeHeight(), player.posZ), lookVec);

						if(mop != null && mop.typeOfHit == Type.BLOCK)
						{
							TileEntity te = world.getTileEntity(mop.getBlockPos());

							if (te instanceof ILockable) {
								BlockPos pos = mop.getBlockPos();

								//if the block is not ownable/not owned by the player looking at it, don't show the indicator if it's disguised
								if (!(te instanceof IOwnable) || !((IOwnable)te).getOwner().isOwner(player)) {
									Block block = te.getBlockType();

									if (block instanceof BlockDisguisable && ((BlockDisguisable)block).getDisguisedBlockState(world, pos) != null)
										return;
								}

								uCoord = 110;

								if(!stack.hasTagCompound())
									stack.setTagCompound(new NBTTagCompound());

								if(ItemSonicSecuritySystem.isAdded(stack.getTagCompound(), pos))
									uCoord = 88;
							}
						}
					}

					if (uCoord != 0) {
						GlStateManager.enableAlpha();
						Minecraft.getMinecraft().renderEngine.bindTexture(BEACON_GUI);
						drawNonStandardTexturedRect(event.getResolution().getScaledWidth() / 2 - 90 + (hand == EnumHand.MAIN_HAND ? player.inventory.currentItem * 20 : (mc.gameSettings.mainHand == EnumHandSide.LEFT ? 189 : -29)), event.getResolution().getScaledHeight() - 22, uCoord, 219, 21, 22, 256, 256);
						GlStateManager.disableAlpha();
					}
				}
			}
		}
	}

	private static void drawNonStandardTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight)
	{
		double z = 200;
		double widthFactor = 1F / (double) textureWidth;
		double heightFactor = 1F / (double) textureHeight;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(x, y + height, z).tex(u * widthFactor, (v + height) * heightFactor).endVertex();
		buffer.pos(x + width, y + height, z).tex((u + width) * widthFactor, (v + height) * heightFactor).endVertex();
		buffer.pos(x + width, y, z).tex((u + width) * widthFactor, v * heightFactor).endVertex();
		buffer.pos(x, y, z).tex(u * widthFactor, v * heightFactor).endVertex();
		tessellator.draw();
	}
}

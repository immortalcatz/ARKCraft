package com.uberverse.arkcraft.client.event;

import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.uberverse.arkcraft.ARKCraft;
import com.uberverse.arkcraft.client.easter.Easter;
import com.uberverse.arkcraft.common.arkplayer.ARKPlayer;
import com.uberverse.arkcraft.common.arkplayer.PlayerWeightCalculator;
import com.uberverse.arkcraft.common.block.crafter.BlockRefiningForge;
import com.uberverse.arkcraft.common.config.WeightsConfig;
import com.uberverse.arkcraft.common.inventory.InventoryAttachment;
import com.uberverse.arkcraft.common.item.attachments.NonSupporting;
import com.uberverse.arkcraft.common.item.ranged.ItemRangedWeapon;
import com.uberverse.arkcraft.common.network.ARKModeToggle;
import com.uberverse.arkcraft.common.network.ReloadStarted;
import com.uberverse.arkcraft.common.network.gui.OpenAttachmentInventory;
import com.uberverse.arkcraft.common.network.gui.OpenPlayerCrafting;
import com.uberverse.arkcraft.common.tileentity.IHoverInfo;
import com.uberverse.arkcraft.common.tileentity.crafter.TileEntityCropPlot;
import com.uberverse.arkcraft.init.ARKCraftItems;
import com.uberverse.arkcraft.util.ClientUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

public class ClientEventHandler
{
	private static KeyBinding reload, attachment, playerPooping, arkmode, playerCrafting;

	private static Minecraft mc = Minecraft.getMinecraft();

	private static Random random = new Random();

	private static int swayTicks;
	private static float yawSway;
	private static float pitchSway;
	public static int disabledEquippItemAnimationTime = 0;

	private ItemStack selected;
	private static final ResourceLocation SCOPE_OVERLAY = new ResourceLocation(ARKCraft.MODID,
			"textures/gui/scope.png");
	private static final ResourceLocation SPYGLASS_OVERLAY = new ResourceLocation(ARKCraft.MODID,
			"textures/gui/spyglass.png");
	public boolean showScopeOverlap = false;

	public static void init()
	{
		ClientEventHandler handler = new ClientEventHandler();
		FMLCommonHandler.instance().bus().register(handler);
		MinecraftForge.EVENT_BUS.register(handler);

		reload = new KeyBinding("key.arkcraft.reload", Keyboard.KEY_R, ARKCraft.instance().name());
		ClientRegistry.registerKeyBinding(reload);

		playerPooping = new KeyBinding("key.arkcraft.playerPooping", Keyboard.KEY_Z, ARKCraft.instance().name());
		ClientRegistry.registerKeyBinding(playerPooping);

		playerCrafting = new KeyBinding("key.arkcraft.playerCrafting", Keyboard.KEY_I, ARKCraft.instance().name());
		ClientRegistry.registerKeyBinding(playerCrafting);

		attachment = new KeyBinding("key.attachment", Keyboard.KEY_M, ARKCraft.instance().name());
		ClientRegistry.registerKeyBinding(attachment);

		arkmode = new KeyBinding("key.harvestOverlay", Keyboard.KEY_P, ARKCraft.instance().name());
		ClientRegistry.registerKeyBinding(arkmode);
	}

	@SubscribeEvent
	public void mouseOverTooltip(ItemTooltipEvent event)
	{
		if (WeightsConfig.isEnabled)
		{
			ItemStack stack = event.itemStack;
			double weight = PlayerWeightCalculator.getWeight(stack);
			event.toolTip.add(EnumChatFormatting.BOLD + "" + EnumChatFormatting.WHITE + "Weight: " + weight);
			if (stack.stackSize > 1)
			{
				event.toolTip.add("Stack Weight: " + (weight * stack.stackSize));
			}
		}
	}

	public Vec3 getPositionEyes(EntityPlayer player, float partialTick)
	{
		if (partialTick == 1.0F)
		{
			return new Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ);
		}
		else
		{
			double d0 = player.prevPosX + (player.posX - player.prevPosX) * partialTick;
			double d1 = player.prevPosY + (player.posY - player.prevPosY) * partialTick + player.getEyeHeight();
			double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTick;
			return new Vec3(d0, d1, d2);
		}
	}

	public MovingObjectPosition rayTrace(EntityPlayer player, double distance, float partialTick)
	{
		Vec3 vec3 = getPositionEyes(player, partialTick);
		Vec3 vec31 = player.getLook(partialTick);
		Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance, vec31.zCoord * distance);
		return player.worldObj.rayTraceBlocks(vec3, vec32, false, false, true);
	}

	@SubscribeEvent
	public void onMouseEvent(MouseEvent evt)
	{
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer thePlayer = mc.thePlayer;

		if (evt.button == 0)
		{
			ItemStack stack = thePlayer.getCurrentEquippedItem();
			InventoryAttachment att = InventoryAttachment.create(stack);
			if (stack != null)
			{
				if (att != null && att.isScopePresent())
				{
					showScopeOverlap = evt.buttonstate;
					selected = stack;
					if (showScopeOverlap) evt.setCanceled(true);
				}
				else if (stack.getItem().equals(ARKCraftItems.spy_glass))
				{
					showSpyglassOverlay = evt.buttonstate;
					selected = stack;
					if (showSpyglassOverlay) evt.setCanceled(true);
				}
			}
		}
	}

	private boolean showSpyglassOverlay;

	@SubscribeEvent
	public void onFOVUpdate(FOVUpdateEvent evt)
	{
		if (mc.gameSettings.thirdPersonView == 0 && (showScopeOverlap || showSpyglassOverlay)) evt.newfov /= 6.0F;
	}

	@SubscribeEvent
	public void onRenderHand(RenderHandEvent evt)
	{
		if (showScopeOverlap || showSpyglassOverlay)
		{
			evt.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onRender(RenderGameOverlayEvent evt)
	{
		Minecraft mc = Minecraft.getMinecraft();
		if ((showScopeOverlap || showSpyglassOverlay) && (mc.thePlayer.getCurrentEquippedItem() != selected || !Mouse
				.isButtonDown(0)))
		{
			showScopeOverlap = false;
			showSpyglassOverlay = false;
		}
		if (showScopeOverlap || showSpyglassOverlay)
		{
			// Render scope
			if (evt.type == RenderGameOverlayEvent.ElementType.HELMET)
			{
				if (mc.gameSettings.thirdPersonView == 0)
				{
					evt.setCanceled(true);
					if (showScopeOverlap) showScope();
					else if (showSpyglassOverlay) showSpyglass();
				}
			}
			// Remove crosshairs
			else if (evt.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && (showScopeOverlap
					|| showSpyglassOverlay)) evt.setCanceled(true);
		}
		ItemStack stack = mc.thePlayer.getCurrentEquippedItem();
		if(evt.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && (stack != null && stack.getItem() instanceof ItemRangedWeapon)) evt.setCanceled(true);
		else if (evt.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && !Minecraft.getMinecraft().isGamePaused())
		{
			MovingObjectPosition mop = rayTrace(mc.thePlayer, 8, evt.partialTicks);
			if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK)
			{
				TileEntity tile = mc.theWorld.getTileEntity(mop.getBlockPos());
				if (tile instanceof TileEntityCropPlot)
				{
					tile = ((TileEntityCropPlot) tile).getCenter();
				}
				if (tile instanceof IHoverInfo)
				{
					ClientUtils.drawIHoverInfoTooltip((IHoverInfo) tile, mc.fontRendererObj, evt, mop.getBlockPos());
				}
			}
		}
	}

	@SubscribeEvent
	public void holding(RenderLivingEvent.Pre event)
	{
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer thePlayer = mc.thePlayer;
		ItemStack stack = thePlayer.getCurrentEquippedItem();
		if (!event.isCanceled() && event.entity.equals(thePlayer) && stack != null)
		{
			if (stack.getItem() instanceof ItemRangedWeapon)
			{
				ModelPlayer model = (ModelPlayer) event.renderer.getMainModel();
				model.aimedBow = true;
			}
		}
	}

	private static final int maxTicks = 70;

	public void showScope()
	{
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer thePlayer = mc.thePlayer;

		// add sway
		swayTicks++;
		if (swayTicks > maxTicks)
		{
			// change values here for control of the amount of sway!
			int divider = thePlayer.isSneaking() ? 20 : 7;
			swayTicks = 0;
			yawSway = ((random.nextFloat() * 2 - 1) / divider) / maxTicks;
			pitchSway = ((random.nextFloat() * 2 - 1) / divider) / maxTicks;
		}

		EntityPlayer p = mc.thePlayer;
		p.rotationPitch += pitchSway;
		p.rotationYaw += yawSway;

		GL11.glPushMatrix();
		mc.entityRenderer.setupOverlayRendering();
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		mc.renderEngine.bindTexture(SCOPE_OVERLAY);

		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		double width = res.getScaledWidth_double();
		double height = res.getScaledHeight_double();

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertexWithUV(0.0D, height, -90.0D, 0.0D, 1.0D);
		worldrenderer.addVertexWithUV(width, height, -90.0D, 1.0D, 1.0D);
		worldrenderer.addVertexWithUV(width, 0.0D, -90.0D, 1.0D, 0.0D);
		worldrenderer.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
		tessellator.draw();

		GL11.glPopMatrix();
	}

	public void showSpyglass()
	{
		Minecraft mc = Minecraft.getMinecraft();

		GL11.glPushMatrix();
		mc.entityRenderer.setupOverlayRendering();
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		mc.renderEngine.bindTexture(SPYGLASS_OVERLAY);

		ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		double width = res.getScaledWidth_double();
		double height = res.getScaledHeight_double();

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();

		worldrenderer.startDrawingQuads();
		worldrenderer.addVertexWithUV(0.0D, height, -90.0D, 0.0D, 1.0D);
		worldrenderer.addVertexWithUV(width, height, -90.0D, 1.0D, 1.0D);
		worldrenderer.addVertexWithUV(width, 0.0D, -90.0D, 1.0D, 0.0D);
		worldrenderer.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
		tessellator.draw();

		GL11.glPopMatrix();
	}

	public static void doReload()
	{
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		ItemStack stack = player.getCurrentEquippedItem();
		if (stack != null && stack.getItem() instanceof ItemRangedWeapon)
		{
			ItemRangedWeapon weapon = (ItemRangedWeapon) stack.getItem();
			if (!weapon.isReloading(stack) && weapon.canReload(stack, player))
			{
				ARKCraft.modChannel.sendToServer(new ReloadStarted());
				weapon.setReloading(stack, player, true);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerKeypressed(InputEvent.KeyInputEvent event)
	{
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (playerPooping.isPressed())
		{
			ARKPlayer.get(player).go();
		}
		else if (playerCrafting.isPressed())
		{
			ARKCraft.modChannel.sendToServer(new OpenPlayerCrafting());
		}
		else if (reload.isPressed())
		{
			doReload();
		}
		else if (attachment.isPressed())
		{
			if (player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem()
					.getItem() instanceof ItemRangedWeapon && !(player.getCurrentEquippedItem()
							.getItem() instanceof NonSupporting))
			{
				ARKCraft.modChannel.sendToServer(new OpenAttachmentInventory());
			}
		}
		else if (arkmode.isPressed())
		{
			ARKPlayer.get(Minecraft.getMinecraft().thePlayer).toggleARKMode();
			ARKCraft.modChannel.sendToServer(new ARKModeToggle());
		}
	}

	@SubscribeEvent
	public void onPlayerJoinWorld(EntityJoinWorldEvent event)
	{
		if (event.entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.entity;
			if (event.entity.worldObj.isRemote)
			{
				ARKPlayer.get(player).requestSynchronization(true);
			}
		}
	}

	@SubscribeEvent
	public void easter(PlayerInteractEvent event)
	{
		if (event.pos != null && event.world.getBlockState(event.pos).getBlock() instanceof BlockRefiningForge)
		{
			Easter.handleInteract(event);
		}
	}
	

	//For the animation when useing guns on ground not to happen
	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent event){
		EntityPlayer player = event.entityPlayer;
		if(event.world.isRemote){
			if(player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemRangedWeapon){
				if(event.action == Action.RIGHT_CLICK_BLOCK || event.action == Action.RIGHT_CLICK_AIR){
					ObfuscationReflectionHelper.setPrivateValue(ItemRenderer.class, Minecraft.getMinecraft().getItemRenderer(), 1F, "equippedProgress", "field_78454_c");
				}
			}
		}
	}
	
	
}

package com.uberverse.arkcraft.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.uberverse.arkcraft.ARKCraft;
import com.uberverse.arkcraft.client.gui.GUIMortarPestle;
import com.uberverse.arkcraft.client.gui.GUIPlayerCrafting;
import com.uberverse.arkcraft.client.gui.GUISmithy;
import com.uberverse.arkcraft.common.block.tile.IHoverInfo;
import com.uberverse.arkcraft.common.block.tile.TileInventoryMP;
import com.uberverse.arkcraft.common.block.tile.TileInventorySmithy;
import com.uberverse.arkcraft.common.config.WeightsConfig;
import com.uberverse.arkcraft.common.container.inventory.InventoryAttachment;
import com.uberverse.arkcraft.common.entity.data.ARKPlayer;
import com.uberverse.arkcraft.common.entity.data.CalcPlayerWeight;
import com.uberverse.arkcraft.common.event.CommonEventHandler;
import com.uberverse.arkcraft.common.handlers.ARKShapelessRecipe;
import com.uberverse.arkcraft.common.handlers.recipes.PestleCraftingManager;
import com.uberverse.arkcraft.common.handlers.recipes.PlayerCraftingManager;
import com.uberverse.arkcraft.common.handlers.recipes.SmithyCraftingManager;
import com.uberverse.arkcraft.common.item.attachments.NonSupporting;
import com.uberverse.arkcraft.common.item.engram.ARKCraftEngrams;
import com.uberverse.arkcraft.common.item.engram.Engram;
import com.uberverse.arkcraft.common.item.firearms.ItemRangedWeapon;
import com.uberverse.arkcraft.common.network.MessageHover.MessageHoverReq;
import com.uberverse.arkcraft.common.network.OpenAttachmentInventory;
import com.uberverse.arkcraft.common.network.OpenPlayerCrafting;
import com.uberverse.arkcraft.common.network.ReloadStarted;
import com.uberverse.arkcraft.init.ARKCraftItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ClientEventHandler
{
	private static KeyBinding reload, attachment, playerPooping, harvestOverlay, playerCrafting;

	private static Minecraft mc = Minecraft.getMinecraft();

	private static Random random = new Random();

	private static int swayTicks;
	private static final int maxTicks = 20;
	private static float yawSway;
	private static float pitchSway;
	public static int disabledEquippItemAnimationTime = 0;

	private ItemStack selected;
	private static final ResourceLocation OVERLAY_TEXTURE = new ResourceLocation(ARKCraft.MODID,
			"textures/gui/scope.png");
	public boolean showScopeOverlap = false;
	private int ticks = 0;

	public static void init()
	{
		ClientEventHandler handler = new ClientEventHandler();
		FMLCommonHandler.instance().bus().register(handler);
		MinecraftForge.EVENT_BUS.register(handler);

		reload = new KeyBinding("key.arkcraft.reload", Keyboard.KEY_R, ARKCraft.NAME);
		ClientRegistry.registerKeyBinding(reload);

		playerPooping = new KeyBinding("key.arkcraft.playerPooping", Keyboard.KEY_Z, ARKCraft.NAME);
		ClientRegistry.registerKeyBinding(playerPooping);

		playerCrafting = new KeyBinding("key.arkcraft.playerCrafting", Keyboard.KEY_I,
				ARKCraft.NAME);
		ClientRegistry.registerKeyBinding(playerCrafting);

		attachment = new KeyBinding("key.attachment", Keyboard.KEY_M, ARKCraft.NAME);
		ClientRegistry.registerKeyBinding(attachment);

		harvestOverlay = new KeyBinding("key.harvestOverlay", Keyboard.KEY_P, ARKCraft.NAME);
		ClientRegistry.registerKeyBinding(harvestOverlay);
	}

	@SubscribeEvent
	public void onClientTick(TickEvent.ClientTickEvent event)
	{
		// Reduces the disabledEquippItemAnimationTime value
		/*
		 * if(disabledEquippItemAnimationTime>0)disabledEquippItemAnimationTime-
		 * -; else
		 * if(disabledEquippItemAnimationTime<0)disabledEquippItemAnimationTime=
		 * 0;
		 */
	}
	
	@SuppressWarnings("static-access")
	@SubscribeEvent
	public void playerInteract(PlayerInteractEvent event)
	{
		Action eventAction = event.action;
		ItemStack item = event.entityPlayer.getCurrentEquippedItem();

		if (item != null && item.getItem() instanceof ItemRangedWeapon)
		{
			if (eventAction.RIGHT_CLICK_BLOCK != null & eventAction.RIGHT_CLICK_AIR != null)
			{
				ObfuscationReflectionHelper.setPrivateValue(ItemRenderer.class,
						Minecraft.getMinecraft().getItemRenderer(), 1F, "equippedProgress",
						"field_78454_c");
			}
		}

	}
	RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
	
	@SubscribeEvent
	public void renderHand(RenderHandEvent event)
	{
		EntityPlayer p = Minecraft.getMinecraft().thePlayer;
		ItemStack item = p.getCurrentEquippedItem();
		if (item != null && item.getItem() instanceof ItemRangedWeapon)
		{
			event.setCanceled(true);
			renderPlayerArms((AbstractClientPlayer) p);
		}
	}

	private void renderLeftArm(RenderPlayer renderPlayerIn) {
		
    //    float f = 1.0F;
     //   GlStateManager.color(f, f, f);
		  float f = 1.0F;
	        GlStateManager.color(f, f, f);
        ModelPlayer modelplayer = (ModelPlayer) renderPlayerIn.getPlayerModel();
        EntityPlayer p = Minecraft.getMinecraft().thePlayer;
      //  this.func_177137_d(p_177138_1_);
     //   modelplayer.bipedLeftArm.showModel = true;
    //    modelplayer.bipedRightArm.showModel = true;
        modelplayer.isSneak = false;
        modelplayer.swingProgress = 0.0F;
    //    modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, p);
       // modelplayer.func_178725_a();
     //   modelplayer.func_178726_b();
        modelplayer.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, p);
        modelplayer.bipedLeftArm.render(0.0625F);
        
	//	GlStateManager.pushMatrix();
	///	GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
	//	GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
	///	GlStateManager.rotate(41.0F, 0.0F, 0.0F, 1.0F);
	//	GlStateManager.translate(-0.3F, -1.1F, 0.45F);
	
	//	Minecraft mc = Minecraft.getMinecraft();
	//	renderPlayerIn.func_177138_b(mc.thePlayer);
	//	GlStateManager.popMatrix();
	}

	private void renderPlayerArms(AbstractClientPlayer clientPlayer) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
		Render render = this.renderManager.getEntityRenderObject(mc.thePlayer);
		RenderPlayer renderplayer = (RenderPlayer) render;
		if (!clientPlayer.isInvisible()) {
			GlStateManager.disableCull();
			this.renderLeftArm(renderplayer);
			GlStateManager.enableCull();
		}
	}

	@SubscribeEvent
	public void onPlayerTickEvent(TickEvent.PlayerTickEvent event)
	{
		// Update CraftingInventory
		if (ARKPlayer.get(event.player).getInventoryBlueprints().isCrafting())
		{
			ARKPlayer.get(event.player).getInventoryBlueprints().update();
		}

		// Calculate item weight and update when the player updates
		if (WeightsConfig.isEnabled)
		{
			if (!event.player.capabilities.isCreativeMode || WeightsConfig.allowInCreative)
			{
				// Removes the updating when the player is in a inventory
				if (Minecraft.getMinecraft().currentScreen == null)
				{
					// So there isnt as many packet leaks...
					if (ARKPlayer.get(event.player).getCarryWeight() != CalcPlayerWeight
							.getAsDouble(event.player))
					{
						ARKPlayer.get(event.player)
						.setCarryWeight(CalcPlayerWeight.getAsDouble(event.player));
					}

					// Weight rules
					if (ARKPlayer.get(event.player).getCarryWeightRatio() >= 0.85)
					{
						event.player.motionX *= 0;
						event.player.motionZ *= 0;
					}
					else if (ARKPlayer.get(event.player)
							.getCarryWeightRatio() >= 0.75)
					{
						event.player.motionX *= WeightsConfig.encumberedSpeed;
						event.player.motionY *= WeightsConfig.encumberedSpeed;
						event.player.motionZ *= WeightsConfig.encumberedSpeed;
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void onPlayerJump(LivingJumpEvent event)
	{
		if (event.entity instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			if (!player.capabilities.isCreativeMode || WeightsConfig.allowInCreative)
			{
				if (ARKPlayer.get(player).getCarryWeightRatio() >= 0.85)
				{
					player.motionY *= 0;
					player.addChatComponentMessage(
							new ChatComponentTranslation("ark.splash.noJump"));
				}
			}
		}
	}

	@SubscribeEvent
	public void mouseOverTooltip(ItemTooltipEvent event)
	{
		if (WeightsConfig.isEnabled)
		{
			ItemStack stack = event.itemStack;
			if (!(stack.getItem() instanceof Engram))
			{
				double weight = CalcPlayerWeight.getWeight(stack);
				event.toolTip.add(
						EnumChatFormatting.BOLD + "" + EnumChatFormatting.WHITE + "Weight: " + weight);
				if (stack.stackSize > 1)
				{
					event.toolTip.add("Stack Weight: " + (weight * stack.stackSize));
				}
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
			double d1 = player.prevPosY + (player.posY - player.prevPosY) * partialTick + player
					.getEyeHeight();
			double d2 = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTick;
			return new Vec3(d0, d1, d2);
		}
	}

	public MovingObjectPosition rayTrace(EntityPlayer player, double distance, float partialTick)
	{
		Vec3 vec3 = getPositionEyes(player, partialTick);
		Vec3 vec31 = player.getLook(partialTick);
		Vec3 vec32 = vec3.addVector(vec31.xCoord * distance, vec31.yCoord * distance,
				vec31.zCoord * distance);
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
			if(stack != null){
				if (att != null || stack.getItem().equals(ARKCraftItems.spy_glass))
				{
					showScopeOverlap = evt.buttonstate;
					selected = stack;
					if (showScopeOverlap) evt.setCanceled(true);
				}
			}		
		}
	}

	@SubscribeEvent
	public void onFOVUpdate(FOVUpdateEvent evt)
	{
		if (mc.gameSettings.thirdPersonView == 0 && showScopeOverlap)
		{
			evt.newfov = 1 / 6.0F;
		}
	}

	@SubscribeEvent
	public void onRenderHand(RenderHandEvent evt)
	{
		if (showScopeOverlap)
		{
			evt.setCanceled(true);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void renderWorldLast(RenderWorldLastEvent e)
	{
		// EntityPlayer player = mc.thePlayer;
		// Calling the hack for the Item Swap animation
		/*
		 * if(disabledEquippItemAnimationTime>0){
		 * Utils.setItemRendererEquippProgress(1, false);
		 * player.isSwingInProgress=false; }
		 */
	}

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public void onRender(RenderGameOverlayEvent evt)
	{
		Minecraft mc = Minecraft.getMinecraft();
		int ticksOld = ticks;
		if (evt.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) ticks = 0;
		if (showScopeOverlap && (mc.thePlayer.getCurrentEquippedItem() != selected || !Mouse
				.isButtonDown(0)))
		{
			showScopeOverlap = false;
		}
		if (showScopeOverlap)
		{
			// Render scope
			if (evt.type == RenderGameOverlayEvent.ElementType.HELMET)
			{
				if (mc.gameSettings.thirdPersonView == 0)
				{
					evt.setCanceled(true);
					showScope();
				}
			}
			// Remove crosshairs
			else if (evt.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS && showScopeOverlap) evt
			.setCanceled(true);
		}
		else if (evt.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS)
		{
			MovingObjectPosition mop = rayTrace(mc.thePlayer, 8, evt.partialTicks);
			if (mop.typeOfHit == MovingObjectType.BLOCK)
			{
				TileEntity tile = mc.theWorld.getTileEntity(mop.getBlockPos());
				if (tile instanceof IHoverInfo)
				{
					ticks = ticksOld + 1;
					if (ticks > 30)
					{
						ticks = 0;
						ARKCraft.modChannel.sendToServer(new MessageHoverReq(mop.getBlockPos()));
					}
					List<String> list = new ArrayList<String>();
					((IHoverInfo) tile).addInformation(list);
					int width = evt.resolution.getScaledWidth();
					int height = evt.resolution.getScaledHeight();
					GL11.glPushMatrix();
					mc.entityRenderer.setupOverlayRendering();
					GL11.glEnable(GL11.GL_BLEND);
					OpenGlHelper.glBlendFunc(770, 771, 1, 0);
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					com.uberverse.arkcraft.ClientUtils.drawHoveringText(list, width / 2, height / 2,
							mc.fontRendererObj, width, height);
					GL11.glPopMatrix();
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

	public void showScope()
	{
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer thePlayer = mc.thePlayer;

		// add sway
		swayTicks++;
		if (swayTicks > maxTicks)
		{
			swayTicks = 0;
			if (!thePlayer.isSneaking())
			{
				yawSway = ((random.nextFloat() * 2 - 1) / 5) / maxTicks;
				pitchSway = ((random.nextFloat() * 2 - 1) / 5) / maxTicks;
			}
			else
			{
				yawSway = ((random.nextFloat() * 2 - 1) / 16) / maxTicks;
				pitchSway = ((random.nextFloat() * 2 - 1) / 16) / maxTicks;
			}
		}

		EntityPlayer p = mc.thePlayer;
		p.rotationPitch += yawSway;
		p.rotationYaw += pitchSway;

		GL11.glPushMatrix();
		mc.entityRenderer.setupOverlayRendering();
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		mc.renderEngine.bindTexture(OVERLAY_TEXTURE);

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

	@SuppressWarnings("static-access")
	@SubscribeEvent
	public void onPlayerKeypressed(InputEvent.KeyInputEvent event)
	{
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (playerPooping.isPressed())
		{
			ARKPlayer.get(player).poop();
		}
		else if (playerCrafting.isPressed())
		{
			player.openGui(ARKCraft.instance(), ARKCraft.GUI.PLAYER.getID(), player.worldObj, 0, 0,
					0);
			ARKCraft.modChannel.sendToServer(new OpenPlayerCrafting(true));
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
				player.openGui(ARKCraft.instance, ARKCraft.GUI.ATTACHMENT_GUI.getID(),
						player.worldObj, 0, 0, 0);
				ARKCraft.modChannel.sendToServer(new OpenAttachmentInventory());
			}
		}
		else if (harvestOverlay.isPressed())
		{
			CommonEventHandler handler = new CommonEventHandler();
			if (handler.arkMode() == false)
			{
				handler.arkMode = true;
			}
			else
			{
				handler.arkMode = false;
			}
		}
	}

	@SubscribeEvent
	public void onTooltip(ItemTooltipEvent event)
	{
		if (mc.currentScreen instanceof GUISmithy)
		{
			TileInventorySmithy te = ((GUISmithy) mc.currentScreen).tileEntity;
			ItemStack stack = te.inventoryBlueprints.getStackInSlot(0);
			if (event.itemStack == stack)
			{
				@SuppressWarnings("rawtypes")
				List recipeList = SmithyCraftingManager.getInstance().getRecipeList();
				for (int i = 0; i < recipeList.size(); i++)
				{
					if (recipeList.get(i) instanceof ARKShapelessRecipe)
					{
						ARKShapelessRecipe r = (ARKShapelessRecipe) recipeList.get(i);
						if (r.getRecipeOutput().isItemEqual(stack))
						{
							for (int j = 0; j < r.recipeItems.size(); j++)
							{
								ItemStack cStack = ((ItemStack) r.recipeItems.get(j));
								event.toolTip.add(EnumChatFormatting.GOLD + I18n.format("arkcraft.tooltip.ingredient",
										I18n.format(cStack.getDisplayName()), cStack.stackSize));
							}
							break;
						}
					}
				}
			}
		}
		else if (mc.currentScreen instanceof GUIMortarPestle)
		{
			TileInventoryMP te = ((GUIMortarPestle) mc.currentScreen).tileEntity;
			ItemStack stack = te.inventoryBlueprints.getStackInSlot(0);
			if (event.itemStack == stack)
			{
				@SuppressWarnings("rawtypes")
				List recipeList = PestleCraftingManager.getInstance().getRecipeList();
				for (int i = 0; i < recipeList.size(); i++)
				{
					if (recipeList.get(i) instanceof ARKShapelessRecipe)
					{
						ARKShapelessRecipe r = (ARKShapelessRecipe) recipeList.get(i);
						if (r.getRecipeOutput().isItemEqual(stack))
						{
							for (int j = 0; j < r.recipeItems.size(); j++)
							{
								ItemStack cStack = ((ItemStack) r.recipeItems.get(j));
								event.toolTip.add(I18n.format("arkcraft.tooltip.ingredient",
										I18n.format(cStack.getDisplayName()), cStack.stackSize));
							}
							break;
						}
					}
				}
			}
		}
		else if (mc.currentScreen instanceof GUIPlayerCrafting)
		{
			@SuppressWarnings("rawtypes")
			List recipeList = PlayerCraftingManager.getInstance().getRecipeList();
			for (int i = 0; i < recipeList.size(); i++)
			{
				if (recipeList.get(i) instanceof ARKShapelessRecipe)
				{
					ARKShapelessRecipe r = (ARKShapelessRecipe) recipeList.get(i);
					if (r.getRecipeOutput().isItemEqual(event.itemStack))
					{
						for (int j = 0; j < r.recipeItems.size(); j++)
						{
							ItemStack cStack = ((ItemStack) r.recipeItems.get(j));
							event.toolTip.add(I18n.format("arkcraft.tooltip.ingredient",
									I18n.format(cStack.getDisplayName()), cStack.stackSize));
						}
						break;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void playerLogin(PlayerLoggedInEvent event)
	{
		ARKPlayer player = ARKPlayer.get(event.player);
		for (int learned : player.learnedEngrams())
		{
			Engram engram = ARKCraftEngrams.engramList.get(player.learnedEngrams().get(learned));
			engram.setLearned();
		}
	}
}
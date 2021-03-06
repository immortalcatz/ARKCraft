package com.uberverse.arkcraft.client.book;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.lwjgl.opengl.GL11;

import com.uberverse.arkcraft.client.book.core.BookData;
import com.uberverse.arkcraft.client.book.core.BookDocument;
import com.uberverse.arkcraft.client.book.lib.Page;
import com.uberverse.arkcraft.client.book.lib.SmallFontRenderer;
import com.uberverse.arkcraft.client.book.lib.button.PageButton;
import com.uberverse.arkcraft.client.book.proxy.BookClient;
import com.uberverse.lib.LogHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiConfirmOpenLink;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.event.ClickEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@SuppressWarnings({ "unchecked", "unused" })
public class GuiInfoBook extends GuiScreen
{

	// Item
	private ItemStack item;

	// General
	public int guiWidth = 156;
	public int guiHeight = 220;
	private BookDocument document;

	// Pages (Count, buttons, font renderer, data)
	private int currentPage;
	private int maxPages;
	private PageButton prevButton, nButton;
	private SmallFontRenderer fontRenderer = BookClient.fontRenderer;
	private BookData bd;

	// Pages (The background GUI)
	private static ResourceLocation bookLeft;
	private static ResourceLocation bookRight;

	// Pages (The content)
	private Page pageLeft;
	private Page pageRight;
	private String uri = "https://minecraft.curseforge.com/projects/arkcraft-mod";

	public GuiInfoBook(ItemStack stack, BookData data)
	{
		this.mc = Minecraft.getMinecraft();
		this.item = stack;
		document = data.getBookDocument();
		if (data.fontRenderer != null) this.fontRenderer = data.fontRenderer;
		this.bd = data;
		bookLeft = data.leftPage;
		bookRight = data.rightPage;
	}

	public void initGui()
	{
		LogHelper.info("initGui() is called!");
		currentPage = 0;
		maxPages = document.getEntries().length;
		updateContent();
		int x = (this.width - guiWidth) / 2;
		int y = (this.height - guiHeight) / 2;
		this.buttonList.add(this.nButton = new PageButton(1, x + guiWidth + 26, y + guiHeight - 25, true));
		this.buttonList.add(this.prevButton = new PageButton(2, x - 45, y + guiHeight - 25, false));

	}

	@Override
	public void confirmClicked(boolean result, int id)
	{
		if (id == 13)
		{
			if (result)
			{
				try
				{
					Class<?> oclass = Class.forName("java.awt.Desktop");
					Object object = oclass.getMethod("getDesktop", new Class[0]).invoke((Object) null, new Object[0]);
					oclass.getMethod("browse", new Class[] { URI.class }).invoke(object, new Object[] { new URI(uri) });
				}
				catch (Throwable throwable)
				{
					LogHelper.error("Couldn\'t open link");
					throwable.printStackTrace();
				}
			}
			else
			{
				this.mc.displayGuiScreen(this);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		// LogHelper.info("CurrentPage: " + currentPage);
		// LogHelper.info("Mouse X: " + mouseX + ", Mouse Y: " + mouseY);
		int x = (width / 2);
		int y = (height - this.guiHeight) / 2;

		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.mc.getTextureManager().bindTexture(bookRight);
		this.drawTexturedModalRect(x, y, 0, 0, this.guiWidth, this.guiHeight);

		GL11.glColor4f(1F, 1F, 1F, 1F);
		this.mc.getTextureManager().bindTexture(bookLeft);
		this.drawTexturedModalRect(x - guiWidth, y, 256 - this.guiWidth, 0, this.guiWidth, this.guiHeight);

		super.drawScreen(mouseX, mouseY, partialTicks);

		LogHelper.info("Current Page: " + currentPage);
		// LogHelper.info(pageLeft == null ? "pageLeft is null!" : "pageLeft is
		// not null");
		// LogHelper.info(pageRight == null ? "pageRight is null!" : "pageRight
		// is not null");
		if (pageLeft != null && pageRight != null)
		{
			// LogHelper.info("Trying to draw the left page!");
			pageLeft.draw(x - guiWidth, y + 12, mouseX, mouseY, fontRenderer, bd.canTranslate, this);

			// LogHelper.info("Trying to draw the right page!");
			pageRight.draw(x, y + 12, mouseX, mouseY, fontRenderer, bd.canTranslate, this);
		}

		nButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
		prevButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);

	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		int imageWidth = 64;
		int imageHeight = 64;
		int leftBound = (width / 2) - guiWidth + (this.guiWidth - imageWidth) / 2;
		int topBound = ((height - this.guiHeight) / 2) + 75;

		boolean xClick = mouseX <= leftBound + imageWidth && mouseX >= leftBound;
		boolean yClick = mouseY >= topBound && mouseY <= topBound + imageHeight;
		if (currentPage == 0 && xClick && yClick)
		{
			this.mc.displayGuiScreen(new GuiConfirmOpenLink(this, uri.toString(), 13, true));
		}
	}

	@Override
	public void actionPerformed(GuiButton button)
	{
		if (button.enabled)
		{
			if (button.id == 1 && currentPage != maxPages) currentPage += 2;
			if (button.id == 2 && currentPage != 0) currentPage -= 2;

			updateContent();
		}
	}

	void updateContent()
	{
		LogHelper.info("updateContent() is called!");
		if (maxPages % 2 == 1)
		{
			if (currentPage > maxPages)
			{
				currentPage = maxPages;
			}
		}
		else
		{
			if (currentPage >= maxPages)
			{
				currentPage = maxPages - 2;
			}
		}
		if (currentPage % 2 == 1)
		{
			currentPage--;
		}
		if (currentPage < 0)
		{
			currentPage = 0;
		}

		Page[] pages = document.getEntries();
		LogHelper.info(pages == null ? "Pages are null!" : "Pages are not null.");
		Page page = pages[currentPage];
		LogHelper.info(page == null ? "Page is null!" : "Page is not null.");
		if (page != null)
		{
			pageLeft = page;
		}

		page = pages[currentPage + 1];
		if (page != null)
		{
			pageRight = page;
		}
	}

	public Minecraft getMC()
	{
		return mc;
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@Override
	public void updateScreen()
	{
		prevButton.visible = currentPage != 0;
		nButton.visible = currentPage != maxPages - 1;
	}
}

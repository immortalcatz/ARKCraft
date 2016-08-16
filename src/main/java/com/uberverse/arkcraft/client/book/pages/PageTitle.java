package com.uberverse.arkcraft.client.book.pages;

import com.uberverse.arkcraft.ARKCraft;
import com.uberverse.arkcraft.client.book.GuiInfoBook;
import com.uberverse.arkcraft.client.book.lib.Page;
import com.uberverse.arkcraft.client.book.lib.SmallFontRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

/**
 * @author Vastatio
 * The title page.
 * It includes 3 strings, a title, text, and an image.
 * It draws the title at guiTop+5, the text at guiTop + 85 and the image at guiTop + 15.
 * 
 */
public class PageTitle extends Page
{

    public String title;
    public String text;
    public String image;

    public void draw(int guiLeft, int guiTop, int mouseX, int mouseY, SmallFontRenderer renderer, boolean canTranslate, GuiInfoBook book)
    {
        if (image != null)
        {
            ResourceLocation imagePath = new ResourceLocation(ARKCraft.MODID, image);
            if (imagePath != null)
            {
                Minecraft.getMinecraft().getTextureManager().bindTexture(imagePath);
            }
            book.drawTexturedModalRect(guiLeft + (book.guiWidth - 64) / 2, guiTop + 25, 0, 0, 64, 64);

        }

        if (title != null)
        {
            if (canTranslate)
            {
                StatCollector.translateToLocal(title);
            }
            renderer.drawString(EnumChatFormatting.UNDERLINE + title, guiLeft + (book.guiWidth - renderer.getStringWidth(title)) / 2, guiTop + 5, 0);
        }

        if (text != null)
        {
            if (canTranslate)
            {
                StatCollector.translateToLocal(text);
            }
            renderer.drawSplitString(EnumChatFormatting.ITALIC + text, guiLeft + (book.guiWidth - renderer.getStringWidth(title)) / 2, guiTop + 105, renderer.getStringWidth(title), 0);
        }
    }

    public String getTitle() { return title; }

    public String getText() { return text; }

    public String getImagePath() { return image; }

}
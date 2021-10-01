/*
 * Copyright (c) 2021 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 */

package com.teammoeg.frostedheart.content.crucible;

import blusunrize.immersiveengineering.common.util.compat.jei.JEIIngredientStackListBuilder;
import com.teammoeg.frostedheart.FHContent;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.client.util.GuiUtils;
import com.teammoeg.frostedheart.content.crucible.CrucibleRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

public class CrucibleCategory<T extends CrucibleRecipe> implements IRecipeCategory<CrucibleRecipe> {
    public static ResourceLocation UID = new ResourceLocation(FHMain.MODID, "crucible");
    private IDrawable BACKGROUND;
    private IDrawable ICON;

    public CrucibleCategory(IGuiHelper guiHelper) {
        this.ICON = guiHelper.createDrawableIngredient(new ItemStack(FHContent.FHBlocks.burning_chamber));
        this.BACKGROUND = guiHelper.createDrawable(new ResourceLocation(FHMain.MODID, "textures/gui/crucible.png"), 19, 0, 130, 40);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends CrucibleRecipe> getRecipeClass() {
        return CrucibleRecipe.class;
    }


    public String getTitle() {
        return (GuiUtils.translateJeiCategory("crucible")).getString();
    }

    @Override
    public IDrawable getBackground() {
        return BACKGROUND;
    }

    @Override
    public IDrawable getIcon() {
        return ICON;
    }

    @Override
    public void setIngredients(CrucibleRecipe recipe, IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM, JEIIngredientStackListBuilder.make(recipe.input).add(recipe.input2).build());
        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }


    @Override
    public void setRecipe(IRecipeLayout recipeLayout, CrucibleRecipe recipe, IIngredients ingredients) {
        IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();

        guiItemStacks.init(0, true, 10, 10);
        guiItemStacks.init(1, true, 31, 10);
        guiItemStacks.init(2, false, 86, 10);

        guiItemStacks.set(ingredients);
    }
}

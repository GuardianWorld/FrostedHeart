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

package com.teammoeg.frostedheart.events;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.teammoeg.frostedheart.FHConfig;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.client.hud.FrostedHud;
import com.teammoeg.frostedheart.client.util.ClientUtils;
import com.teammoeg.frostedheart.client.util.GuiUtils;
import com.teammoeg.frostedheart.climate.IHeatingEquipment;
import com.teammoeg.frostedheart.climate.ITempAdjustFood;
import com.teammoeg.frostedheart.climate.IWarmKeepingEquipment;
import com.teammoeg.frostedheart.climate.TemperatureCore;
import com.teammoeg.frostedheart.content.recipes.RecipeInner;
import com.teammoeg.frostedheart.content.temperature.heatervest.HeaterVestRenderer;
import com.teammoeg.frostedheart.data.BlockTempData;
import com.teammoeg.frostedheart.data.FHDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static net.minecraft.util.text.TextFormatting.GRAY;

@Mod.EventBusSubscriber(modid = FHMain.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!HeaterVestRenderer.rendersAssigned) {
            for (Object render : ClientUtils.mc().getRenderManager().renderers.values())
                if (BipedRenderer.class.isAssignableFrom(render.getClass()))
                    ((BipedRenderer) render).addLayer(new HeaterVestRenderer<>((BipedRenderer) render));
                else if (ArmorStandRenderer.class.isAssignableFrom(render.getClass()))
                    ((ArmorStandRenderer) render).addLayer(new HeaterVestRenderer<>((ArmorStandRenderer) render));
            HeaterVestRenderer.rendersAssigned = true;
        }
    }

    @SubscribeEvent
    public static void addNormalItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item i = stack.getItem();
        if (i == Items.FLINT) {
            event.getToolTip().add(GuiUtils.translateTooltip("double_flint_ignition").mergeStyle(GRAY));
        }
    }


    @SubscribeEvent
    public static void addItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item i = stack.getItem();
        ITempAdjustFood itf = null;
        IWarmKeepingEquipment iwe = null;
        float tspeed=(float)(double)FHConfig.SERVER.tempSpeed.get();
        if (i instanceof ITempAdjustFood) {
            itf = (ITempAdjustFood) i;
        } else {
            itf = FHDataManager.getFood(stack);
        }
        if (i instanceof IWarmKeepingEquipment) {
            iwe = (IWarmKeepingEquipment) i;
        } else {
            String s = ItemNBTHelper.getString(stack, "inner_cover");
            EquipmentSlotType aes = MobEntity.getSlotForItemStack(stack);
            if (s.length() > 0 && aes != null) {
                event.getToolTip().add(GuiUtils.translateTooltip("inner").mergeStyle(TextFormatting.GREEN).appendSibling(new TranslationTextComponent("item." + s.replaceFirst(":", "."))));
                if(!ItemNBTHelper.getBoolean(stack,"inner_bounded")) {
                	if(stack.hasTag()&&stack.getTag().contains("inner_cover_tag")) {
	                	int damage=stack.getTag().getCompound("inner_cover_tag").getInt("Damage");
	                	if(damage!=0) {
	                		RecipeInner ri=RecipeInner.recipeList.get(new ResourceLocation(s));
	                		if(ri!=null) {
		                		int maxDmg=ri.getDurability();
		                        float temp = damage*1.0F/maxDmg;
		                        String temps = Integer.toString((Math.round(temp * 100)));
		                		event.getToolTip().add(GuiUtils.translateTooltip("inner_damage",temps));
	                		}
	                	}
                	}
                }
                iwe = FHDataManager.getArmor(s + "_" + aes.getName());
            } else
                iwe = FHDataManager.getArmor(stack);
        }
        BlockTempData btd = FHDataManager.getBlockData(stack);
        if (btd != null) {
            float temp = btd.getTemp();
            temp = (Math.round(temp * 100)) / 100.0F;//round
            String temps = Float.toString(temp);
            if (temp != 0)
                if (temp > 0)
                    event.getToolTip().add(GuiUtils.translateTooltip("block_temp", "+" + temps).mergeStyle(TextFormatting.GOLD));
                else
                    event.getToolTip().add(GuiUtils.translateTooltip("block_temp", temps).mergeStyle(TextFormatting.AQUA));
        }
        if (itf != null) {
            float temp = itf.getHeat(stack)*tspeed;
            temp = (Math.round(temp * 1000)) / 1000.0F;//round
            String temps = Float.toString(temp);
            if (temp != 0)
                if (temp > 0)
                    event.getToolTip().add(GuiUtils.translateTooltip("food_temp", "+" + temps).mergeStyle(TextFormatting.GOLD));
                else
                    event.getToolTip().add(GuiUtils.translateTooltip("food_temp", temps).mergeStyle(TextFormatting.AQUA));
        }
        if (iwe != null) {
            float temp = iwe.getFactor(null, stack);
            temp = Math.round(temp * 100);
            String temps = Float.toString(temp);
            if (temp != 0)
                if (temp > 0)
                    event.getToolTip().add(GuiUtils.translateTooltip("armor_warm", temps).mergeStyle(TextFormatting.GOLD));
                else
                    event.getToolTip().add(GuiUtils.translateTooltip("armor_warm", temps).mergeStyle(TextFormatting.AQUA));
        }
        if (i instanceof IHeatingEquipment) {
            float temp = ((IHeatingEquipment) i).getMax(stack)*tspeed;
            temp = (Math.round(temp * 2000)) / 1000.0F;
            String temps = Float.toString(temp);
            if (temp != 0)
                if (temp > 0)
                    event.getToolTip().add(GuiUtils.translateTooltip("armor_heating", "+" + temps).mergeStyle(TextFormatting.GOLD));
                else
                    event.getToolTip().add(GuiUtils.translateTooltip("armor_heating", temps).mergeStyle(TextFormatting.AQUA));
        }
    }

    @SubscribeEvent
    public static void onPostRenderOverlay(RenderGameOverlayEvent.Post event) {
        PlayerEntity player = FrostedHud.getRenderViewPlayer();
        Minecraft mc = Minecraft.getInstance();
        MatrixStack stack = event.getMatrixStack();
        int anchorX = event.getWindow().getScaledWidth() / 2;
        int anchorY = event.getWindow().getScaledHeight();
        if (event.getType() == RenderGameOverlayEvent.ElementType.VIGNETTE && player != null && !player.isCreative() && !player.isSpectator()) {
            if (TemperatureCore.getBodyTemperature(player) <= -0.5) {
                FrostedHud.renderFrozenVignette(stack, anchorX, anchorY, mc, player);
            }
            if (TemperatureCore.getBodyTemperature(player) <= -1.0) {
                FrostedHud.renderFrozenOverlay(stack, anchorX, anchorY, mc, player);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void renderVanillaOverlay(RenderGameOverlayEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity clientPlayer = mc.player;
        PlayerEntity renderViewPlayer = FrostedHud.getRenderViewPlayer();

        if (renderViewPlayer == null || clientPlayer == null || mc.gameSettings.hideGUI) {
            return;
        }

        MatrixStack stack = event.getMatrixStack();
        int anchorX = event.getWindow().getScaledWidth() / 2;
        int anchorY = event.getWindow().getScaledHeight();
        float partialTicks = event.getPartialTicks();

        FrostedHud.renderSetup(clientPlayer, renderViewPlayer);
        if(FHConfig.CLIENT.enableUI.get()) {
	        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR && FrostedHud.renderHotbar) {
	            if (mc.playerController.getCurrentGameType() == GameType.SPECTATOR) {
	                mc.ingameGUI.getSpectatorGui().func_238528_a_(stack, partialTicks);
	            } else {
	                FrostedHud.renderHotbar(stack, anchorX, anchorY, mc, renderViewPlayer, partialTicks);
	            }
	            event.setCanceled(true);
	        }
	        if (event.getType() == RenderGameOverlayEvent.ElementType.EXPERIENCE && FrostedHud.renderExperience) {
	            if (FrostedHud.renderHypothermia) {
	                FrostedHud.renderHypothermia(stack, anchorX, anchorY, mc, clientPlayer);
	            } else {
	                FrostedHud.renderExperience(stack, anchorX, anchorY, mc, clientPlayer);
	            }
	            event.setCanceled(true);
	        }
	        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTH && FrostedHud.renderHealth) {
	            FrostedHud.renderHealth(stack, anchorX, anchorY, mc, renderViewPlayer);
	            event.setCanceled(true);
	        }
	        if (event.getType() == RenderGameOverlayEvent.ElementType.FOOD) {
	            if (FrostedHud.renderFood) FrostedHud.renderFood(stack, anchorX, anchorY, mc, renderViewPlayer);
	            if (FrostedHud.renderThirst) FrostedHud.renderThirst(stack, anchorX, anchorY, mc, renderViewPlayer);
	            if (FrostedHud.renderHealth) FrostedHud.renderTemperature(stack, anchorX, anchorY, mc, renderViewPlayer);
	            event.setCanceled(true);
	        }
	        if (event.getType() == RenderGameOverlayEvent.ElementType.ARMOR && FrostedHud.renderArmor) {
	            FrostedHud.renderArmor(stack, anchorX, anchorY, mc, clientPlayer);
	            event.setCanceled(true);
	        }
	        if (event.getType() == RenderGameOverlayEvent.ElementType.HEALTHMOUNT && FrostedHud.renderHealthMount) {
	            FrostedHud.renderMountHealth(stack, anchorX, anchorY, mc, clientPlayer);
	            event.setCanceled(true);
	        }
	        if (event.getType() == RenderGameOverlayEvent.ElementType.JUMPBAR && FrostedHud.renderJumpBar) {
	            FrostedHud.renderJumpbar(stack, anchorX, anchorY, mc, clientPlayer);
	            event.setCanceled(true);
	        }
        }
    }
}

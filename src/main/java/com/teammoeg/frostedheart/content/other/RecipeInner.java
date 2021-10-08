package com.teammoeg.frostedheart.content.other;

import com.teammoeg.frostedheart.content.generator.GeneratorRecipe;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.entity.MobEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

public class RecipeInner extends SpecialRecipe {
    public static RegistryObject<IERecipeSerializer<RecipeInner>> SERIALIZER;
	protected RecipeInner(ResourceLocation id,Ingredient t) {
		super(id);
		type=t;
	}

	Ingredient type;

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	public boolean matches(CraftingInventory inv, World worldIn) {
		boolean hasArmor = false;
		boolean hasItem = false;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			if (itemstack==null||itemstack.isEmpty()) {
				continue;
			}
			if (type.test(itemstack)) {
				if(hasItem)
					return false;
				hasItem = true;
			} else {
				if(hasArmor)
					return false;
				EquipmentSlotType type = MobEntity.getSlotForItemStack(itemstack);
				if (type != null && type != EquipmentSlotType.MAINHAND && type != EquipmentSlotType.OFFHAND)
					hasArmor = true;
			}
		}
		return hasArmor && hasItem;
	}

	/**
	 * Returns an Item that is the result of this recipe
	 */
	public ItemStack getCraftingResult(CraftingInventory inv) {
		ItemStack buffstack = ItemStack.EMPTY;
		ItemStack armoritem = ItemStack.EMPTY;
		for (int i = 0; i < inv.getSizeInventory(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			if(itemstack!=null&&!itemstack.isEmpty()) {
				if(type.test(itemstack)) {
					if(!buffstack.isEmpty())return ItemStack.EMPTY;
					buffstack=itemstack;
				} else {
					if(!armoritem.isEmpty())return ItemStack.EMPTY;
					EquipmentSlotType type = MobEntity.getSlotForItemStack(itemstack);
					if (type != null && type != EquipmentSlotType.MAINHAND && type != EquipmentSlotType.OFFHAND)
						armoritem = itemstack;
				}
			}
		}
		
		if (!armoritem.isEmpty()&&!buffstack.isEmpty()) {
			ItemStack ret=armoritem.copy();
			ItemNBTHelper.putString(ret,"inner_cover",buffstack.getItem().getRegistryName().toString());
			return ret;
		}
		return ItemStack.EMPTY;
	}

	/**
	 * Used to determine if this recipe can fit in a grid of the given width/height
	 */
	public boolean canFit(int width, int height) {
		return width *height >= 2;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return SERIALIZER.get();
	}

}
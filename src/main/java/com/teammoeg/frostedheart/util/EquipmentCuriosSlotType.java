package com.teammoeg.frostedheart.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.teammoeg.frostedheart.FHMain;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.ResourceLocation;
import top.theillusivec4.curios.api.SlotTypePreset;

public enum EquipmentCuriosSlotType {
	   MAINHAND(false),
	   OFFHAND(false),
	   FEET(false),
	   LEGS(false),
	   CHEST(false),
	   HEAD(false),
	   QUICKBAR(false),
	   INVENTORY(false),
	   CURIOS_HEAD(true),
	   CURIOS_NECKLACE(true),
	   CURIOS_BACK(true),
	   CURIOS_BODY(true), 
	   CURIOS_BRACELET(true),
	   CURIOS_HANDS(true), 
	   CURIOS_RING(true),
	   CURIOS_BELT(true),
	   CURIOS_CHARM(true),
	   CURIOS_CURIO(true),
	   CURIOS_GENERIC(true),
	   UNKNOWN(false);
	private final boolean isCurios;
	private final UUID slotUUID;
	private final String key;
	private EquipmentCuriosSlotType(boolean isCurios) {
		this.isCurios = isCurios;
		this.key=FHMain.rl(this.name().toLowerCase()).toString();
		this.slotUUID = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.ISO_8859_1));
	}
	public boolean isHand() {
		return this==MAINHAND||this==OFFHAND;
	}
	public static EquipmentCuriosSlotType fromVanilla(EquipmentSlotType vanilla) {
		switch(vanilla) {
		case MAINHAND: return MAINHAND;
		case OFFHAND: return OFFHAND;
		case FEET: return FEET;
		case LEGS: return LEGS;
		case CHEST: return CHEST;
		case HEAD: return HEAD;
		}
		return UNKNOWN;
	}
	public static EquipmentCuriosSlotType fromCurios(String id) {
		SlotTypePreset stp=SlotTypePreset.findPreset(id).orElse(null);
		if(stp==null)return CURIOS_GENERIC;
		switch(stp) {
		case HEAD:return CURIOS_HEAD;
		case NECKLACE:return CURIOS_NECKLACE;
		case BACK:return CURIOS_BACK;
		case BODY:return CURIOS_BODY; 
		case BRACELET:return CURIOS_BRACELET;
		case HANDS:return CURIOS_HANDS;
		case RING:return CURIOS_RING;
		case BELT:return CURIOS_BELT;
		case CHARM:return CURIOS_CHARM;
		case CURIO:return CURIOS_CURIO;
		}
		return CURIOS_GENERIC;
	}
	public boolean isCurios() {
		return isCurios;
	}
	public UUID getSlotUUID() {
		return slotUUID;
	}
	public String getKey() {
		return key;
	}
	
	
}

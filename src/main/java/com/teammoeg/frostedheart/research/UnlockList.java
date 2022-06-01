package com.teammoeg.frostedheart.research;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;

public abstract class UnlockList<T> {
	Set<T> s=new HashSet<>();

	public UnlockList() {
		
	}
	public UnlockList(ListNBT nbt) {
		this();
		load(nbt);
	}
	public boolean has(T key) {
		return s.contains(key);
	}
	public void add(T key) {
		s.add(key);
	}
	public void addAll(Collection<T> key) {
		s.addAll(key);
	}
	public abstract String getString(T item);
	public abstract T getObject(String s);
	
	public ListNBT serialize() {
		ListNBT ln=new ListNBT();
		for(T t:s)
			ln.add(StringNBT.valueOf(getString(t)));
		return ln;
	}
	public void remove(T key) {
		s.remove(key);
	}
	public void removeAll(Collection<T> key) {
		s.removeAll(key);
	}
	public void load(ListNBT nbt) {
		for(INBT in:nbt) {
			s.add(getObject(in.getString()));
		}
	}
}

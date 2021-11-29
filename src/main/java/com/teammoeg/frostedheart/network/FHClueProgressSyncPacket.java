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

package com.teammoeg.frostedheart.network;

import java.util.UUID;
import java.util.function.Supplier;

import com.teammoeg.frostedheart.client.util.ClientUtils;
import com.teammoeg.frostedheart.climate.TemperatureCore;
import com.teammoeg.frostedheart.research.AbstractClue;
import com.teammoeg.frostedheart.research.FHResearch;
import com.teammoeg.frostedheart.research.Research;
import com.teammoeg.frostedheart.research.ResearchDataManager;
import com.teammoeg.frostedheart.research.TeamResearchData;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;
// send when player join
public class FHClueProgressSyncPacket {
    private final boolean data;
    private final int id;
    public FHClueProgressSyncPacket(UUID team,AbstractClue rs) {
    	TeamResearchData rd=ResearchDataManager.INSTANCE.getData(team);
        this.data = rd.isClueTriggered(rs);
        this.id=rs.getRId();
    }

    FHClueProgressSyncPacket(PacketBuffer buffer) {
        data = buffer.readBoolean();
        id=buffer.readVarInt();
    }

    void encode(PacketBuffer buffer) {
        buffer.writeBoolean(data);
        buffer.writeVarInt(id);
    }

    void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
        	FHResearch.clues.getById(id).setCompleted(data);
        });
        context.get().setPacketHandled(true);
    }
}
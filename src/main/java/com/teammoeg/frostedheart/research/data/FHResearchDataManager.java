/*
 * Copyright (c) 2022-2024 TeamMoeg
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
 *
 */

package com.teammoeg.frostedheart.research.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.teammoeg.frostedheart.FHMain;
import com.teammoeg.frostedheart.research.FHResearch;
import com.teammoeg.frostedheart.research.TeamDataHolder;
import com.teammoeg.frostedheart.util.OptionalLazy;
import com.teammoeg.frostedheart.util.client.ClientUtils;
import com.teammoeg.frostedheart.util.io.FileUtil;

import dev.ftb.mods.ftbteams.data.Team;
import dev.ftb.mods.ftbteams.data.TeamManager;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.util.Lazy;

public class FHResearchDataManager {
    public static MinecraftServer server;
    static final FolderName dataFolder = new FolderName("fhresearch");
    public static FHResearchDataManager INSTANCE;
    Path local;
    File regfile;
    private Map<UUID, UUID> dataByFTBId = new HashMap<>();
    private Map<UUID, TeamDataHolder> dataByResearchId = new HashMap<>();
    
    public static RecipeManager getRecipeManager() {
        if (server != null)
            return server.getRecipeManager();
        return ClientUtils.mc().world.getRecipeManager();
    }

    public FHResearchDataManager(MinecraftServer s) {
        server = s;
        INSTANCE = this;
    }

    public Collection<TeamDataHolder> getAllData() {
        return dataByResearchId.values();
    }

    /*
     * get Team data as well as check ownership.
     *
     * */
    public TeamDataHolder getData(Team team) {
        UUID cn = dataByFTBId.get(team.getId());
        if (cn == null) {
            GameProfile owner = server.getPlayerProfileCache().getProfileByUUID(team.getOwner());
            
            if (owner != null)
                for (Entry<UUID, TeamDataHolder> dat : dataByResearchId.entrySet()) {
                    if (owner.getName().equals(dat.getValue().getOwnerName())) {
                        this.transfer(dat.getKey(), team);
                        break;
                    }
                }
        }
        cn = dataByFTBId.computeIfAbsent(team.getId(), t->UUID.randomUUID());
        TeamDataHolder data=dataByResearchId.computeIfAbsent(cn, c -> new TeamDataHolder(c, OptionalLazy.of(()->team)));
        if ((server.isSinglePlayer()||!server.isServerInOnlineMode())&&data.getOwnerName() == null) {
            PlayerProfileCache cache = server.getPlayerProfileCache();
            if (cache != null) {
                GameProfile gp = cache.getProfileByUUID(team.getOwner());
                if (gp != null) {
                	data.setOwnerName(gp.getName());
                }
            }
        }
        return data;

    }
    @Nullable
    public TeamDataHolder getData(UUID id) {

    	TeamDataHolder cn = dataByResearchId.get(id);
        return cn;

    }

    public void load() {
        FHResearch.editor = false;
        local = server.func_240776_a_(dataFolder);
        regfile = new File(local.toFile().getParentFile(), "fhregistries.dat");
        FHResearch.clearAll();
        if (regfile.exists()) {
            try {
                FHResearch.load(CompressedStreamTools.readCompressed(regfile));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                FHMain.LOGGER.fatal("CANNOT READ RESEARCH REGISTRIES, MAY CAUSE UNSYNC!");

            }
        }
        dataByFTBId.clear();
        FHResearch.init();
        local.toFile().mkdirs();
        for (File f : local.toFile().listFiles((f) -> f.getName().endsWith(".nbt"))) {
            UUID tud;
            try {
                try {
                    tud = UUID.fromString(f.getName().split("\\.")[0]);
                } catch (IllegalArgumentException ex) {
                    FHMain.LOGGER.error("Unexpected data file " + f.getName() + ", ignoring...");
                    continue;
                }
                
                CompoundNBT nbt = CompressedStreamTools.readCompressed(f);
                if(nbt.contains("teamId"))
                	tud=nbt.getUniqueId("teamId");
                final UUID ftbid=tud;
                TeamDataHolder trd = new TeamDataHolder(nbt.getUniqueId("uuid"),OptionalLazy.of(() -> TeamManager.INSTANCE.getTeamByID(ftbid)));

                trd.deserialize(nbt, false);
                dataByFTBId.put(ftbid, trd.getId());
                dataByResearchId.put(trd.getId(), trd);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
                FHMain.LOGGER.error("Unexpected data file " + f.getName() + ", ignoring...");
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                FHMain.LOGGER.error("Unable to read data file " + f.getName() + ", ignoring...");
            }
        }

        try {
            File dbg = new File(local.toFile().getParentFile(), "fheditor.dat");
            if (dbg.exists() && FileUtil.readString(dbg).equals("true"))
                FHResearch.editor = true;
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
    }

    public void save() {
        File dbg = new File(local.toFile().getParentFile(), "fheditor.dat");
        try {
            if (FHResearch.isEditor())
                FileUtil.transfer("true", dbg);
            else if (dbg.exists())
                FileUtil.transfer("false", dbg);
        } catch (IOException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }
        try {
            CompressedStreamTools.writeCompressed(FHResearch.save(new CompoundNBT()), regfile);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            FHMain.LOGGER.fatal("CANNOT SAVE RESEARCH REGISTRIES, MAY CAUSE UNSYNC!");
        }
        Set<String> files = new HashSet<>(Arrays.asList(local.toFile().list((d, s) -> s.endsWith(".nbt"))));
        for (Entry<UUID, TeamDataHolder> entry : dataByResearchId.entrySet()) {
            String fn = entry.getKey().toString() + ".nbt";
            File f = local.resolve(fn).toFile();
            try {
                CompressedStreamTools.writeCompressed(entry.getValue().serialize(false), f);
                files.remove(fn);
            } catch (IOException e) {

                e.printStackTrace();
                FHMain.LOGGER.error("Unable to save data file for team " + entry.getKey().toString() + ", ignoring...");
            }
        }
        for (String todel : files) {
            local.resolve(todel).toFile().delete();
        }
    }

    public void transfer(UUID orig, Team team) {
    	UUID rid=dataByFTBId.remove(orig);
        TeamDataHolder odata = dataByResearchId.remove(rid);
        if (odata != null) {
            odata.setTeam(OptionalLazy.of(()->team));
            odata.setOwnerName(server.getPlayerProfileCache().getProfileByUUID(team.getOwner()).getName());
        }
        dataByFTBId.put(team.getId(), rid);

    }
}

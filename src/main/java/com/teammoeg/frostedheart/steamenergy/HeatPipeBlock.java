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

package com.teammoeg.frostedheart.steamenergy;

import blusunrize.immersiveengineering.common.util.Utils;
import com.teammoeg.frostedheart.FHContent;
import com.teammoeg.frostedheart.base.block.FluidPipeBlock;
import com.teammoeg.frostedheart.client.util.ClientUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Random;
import java.util.function.BiFunction;

public class HeatPipeBlock extends FluidPipeBlock<HeatPipeBlock> implements ISteamEnergyBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public HeatPipeBlock(String name, Properties blockProps,
                         BiFunction<Block, net.minecraft.item.Item.Properties, Item> createItemBlock) {
        super(HeatPipeBlock.class, name, blockProps, createItemBlock);
        this.lightOpacity = 0;
        this.setDefaultState(this.stateContainer.getBaseState().with(LIT, Boolean.FALSE));
    }


    @Nullable
    @Override
    public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world) {
        return FHContent.FHTileTypes.HEATPIPE.get().create();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(LIT);
    }


	/*@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
		TileEntity tec=Utils.getExistingTileEntity(worldIn,pos);
		for(Direction d:Direction.values()) {
			TileEntity te=Utils.getExistingTileEntity(worldIn,pos.offset(d));
			if(te instanceof HeatPipeTileEntity)
				((HeatPipeTileEntity) te).connectAt(d.getOpposite());
			else
				((HeatPipeTileEntity) tec).connectAt(d);
		}
	}*/


	/*@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		super.onReplaced(state, worldIn, pos, newState, isMoving);
		for(Direction d:Direction.values()) {
			TileEntity te=Utils.getExistingTileEntity(worldIn,pos.offset(d));
			if(te instanceof HeatPipeTileEntity)
				((HeatPipeTileEntity) te).disconnectAt(d.getOpposite());
		}
	}*/




	@Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
                                boolean isMoving) {
        //System.out.println(pos);
        //System.out.println(fromPos);
        TileEntity te = Utils.getExistingTileEntity(worldIn, pos);
        if (te instanceof HeatPipeTileEntity) {
            Vector3i vec = fromPos.subtract(pos);
            Direction dir = Direction.getFacingFromVector(vec.getX(), vec.getY(), vec.getZ());
            if (((HeatPipeTileEntity) te).connectAt(dir)) {
                state.with(FACING_TO_PROPERTY_MAP.get(dir), true);
            } else {
                state.with(FACING_TO_PROPERTY_MAP.get(dir), false);
            }

        }
    }


    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }


    @Override
    public boolean canConnectFrom(IWorld world, BlockPos pos, BlockState state, Direction dir) {
        return true;
    }
}

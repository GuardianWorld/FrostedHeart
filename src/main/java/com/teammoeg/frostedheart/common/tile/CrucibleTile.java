package com.teammoeg.frostedheart.common.tile;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.teammoeg.frostedheart.FHMultiblocks;
import com.teammoeg.frostedheart.FHTileTypes;
import com.teammoeg.frostedheart.common.recipe.CrucibleRecipe;
import com.teammoeg.frostedheart.util.FHBlockInterfaces;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class CrucibleTile extends MultiblockPartTileEntity<CrucibleTile> implements IIEInventory,
        FHBlockInterfaces.IActiveState, IEBlockInterfaces.IInteractionObjectIE, IEBlockInterfaces.IProcessTile, IEBlockInterfaces.IBlockBounds {

    public CrucibleTile.CrucibleData guiData = new CrucibleTile.CrucibleData();
    private NonNullList<ItemStack> inventory = NonNullList.withSize(3, ItemStack.EMPTY);
    public int temperature;
    public int burnTime;
    public int process = 0;
    public int processMax = 0;

    public CrucibleTile() {
        super(FHMultiblocks.CRUCIBLE, FHTileTypes.CRUCIBLE.get(), false);
    }

    @Nonnull
    @Override
    public IFluidTank[] getAccessibleFluidTanks(Direction side) {
        return new IFluidTank[0];
    }

    @Override
    public boolean canFillTankFrom(int iTank, Direction side, FluidStack resource) {
        return false;
    }

    @Override
    public boolean canDrainTankFrom(int iTank, Direction side) {
        return false;
    }

    @Nonnull
    @Override
    public VoxelShape getBlockBounds(@Nullable ISelectionContext ctx) {
        return VoxelShapes.fullCube();
    }

    @Nullable
    @Override
    public IEBlockInterfaces.IInteractionObjectIE getGuiMaster() {
        return master();
    }

    @Override
    public boolean canUseGui(PlayerEntity player) {
        return formed;
    }

    @Override
    public int[] getCurrentProcessesStep() {
        CrucibleTile master = master();
        if (master != this && master != null)
            return master.getCurrentProcessesStep();
        return new int[]{processMax - process};
    }

    @Override
    public int[] getCurrentProcessesMax() {
        CrucibleTile master = master();
        if (master != this && master != null)
            return master.getCurrentProcessesMax();
        return new int[]{processMax};
    }

    @Nullable
    @Override
    public NonNullList<ItemStack> getInventory() {
        CrucibleTile master = master();
        if (master != null && master.formed && formed)
            return master.inventory;
        return this.inventory;
    }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (slot == 0)
            return CrucibleRecipe.findRecipe(stack) != null;
        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public void doGraphicalUpdates(int slot) {

    }

    LazyOptional<IItemHandler> invHandler = registerConstantCap(
            new IEInventoryHandler(3, this, 0, new boolean[]{true, false, true},
                    new boolean[]{false, true, false})
    );

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            CrucibleTile master = master();
            if (master != null)
                return master.invHandler.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readCustomNBT(CompoundNBT nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        setTemperature(nbt.getInt("temperature"));
        setBurnTime(nbt.getInt("burntime"));
        if (!descPacket) {
            ItemStackHelper.loadAllItems(nbt, inventory);
            process = nbt.getInt("process");
            processMax = nbt.getInt("processMax");
        }
    }

    @Override
    public void writeCustomNBT(CompoundNBT nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.putInt("temperature", temperature);
        nbt.putInt("burntime", burnTime);
        if (!descPacket) {
            nbt.putInt("process", process);
            nbt.putInt("processMax", processMax);
            ItemStackHelper.saveAllItems(nbt, inventory);
        }
    }

    public void setTemperature(int temperature) {
        if (master() != null)
            master().temperature = temperature;
    }

    public void setBurnTime(int burnTime) {
        if (master() != null)
            master().burnTime = burnTime;
    }

    @Override
    public void tick() {
        checkForNeedlessTicking();
        if (!world.isRemote && formed && !isDummy()) {
            CrucibleRecipe recipe = getRecipe();
            final boolean activeBeforeTick = getIsActive();
            if (burnTime > 0 && temperature < 1600) {
                burnTime--;
                temperature++;
            }
            if (burnTime <= 0 && temperature > 0) {
                temperature--;
            }
            if (burnTime <= 0) {
                if (inventory.get(2).getItem() == IEItems.Ingredients.coalCoke) {
                    burnTime = 600;
                    inventory.get(2).shrink(1);
                    this.markDirty();
                }
            }
            if (process > 0) {
                if (inventory.get(0).isEmpty()) {
                    process = 0;
                    processMax = 0;
                }
                // during process
                else {
                    if (recipe == null || recipe.time != processMax) {
                        process = 0;
                        processMax = 0;
                    } else {
                        process--;
                    }
                }
                this.markContainingBlockForUpdate(null);
            } else {
                if (activeBeforeTick) {
                    if (recipe != null) {
                        Utils.modifyInvStackSize(inventory, 0, -recipe.input.getCount());
                        if (!inventory.get(1).isEmpty())
                            inventory.get(1).grow(recipe.output.copy().getCount());
                        else if (inventory.get(1).isEmpty())
                            inventory.set(1, recipe.output.copy());
                    }
                    processMax = 0;
                    setActive(false);
                }
                if (recipe != null) {
                    this.process = recipe.time;
                    this.processMax = process;
                    setActive(true);
                }
            }
            final boolean activeAfterTick = getIsActive();
            if (activeBeforeTick != activeAfterTick) {
                this.markDirty();
                // scan 3x4x3
                for (int x = 0; x < 3; ++x)
                    for (int y = 0; y < 4; ++y)
                        for (int z = 0; z < 3; ++z) {
                            BlockPos actualPos = getBlockPosForPos(new BlockPos(x, y, z));
                            TileEntity te = Utils.getExistingTileEntity(world, actualPos);
                            if (te instanceof CrucibleTile)
                                ((CrucibleTile) te).setActive(activeAfterTick);
                        }
            }
        }
        if (world != null && world.isRemote && formed && !isDummy() && getIsActive()) {
            Random random = world.rand;
            if (random.nextFloat() < 0.50F) {
                for (int i = 0; i < random.nextInt(2) + 2; ++i) {
                    world.addOptionalParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, true, (double) pos.getX() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1), (double) pos.getY() + random.nextDouble() + random.nextDouble(), (double) pos.getZ() + 0.5D + random.nextDouble() / 3.0D * (double) (random.nextBoolean() ? 1 : -1), 0.0D, 0.05D, 0.0D);
                    world.addParticle(ParticleTypes.SMOKE, (double) pos.getX() + 0.25D + random.nextDouble() / 2.0D * (double) (random.nextBoolean() ? 1 : -1), (double) pos.getY() + 0.4D, (double) pos.getZ() + 0.25D + random.nextDouble() / 2.0D * (double) (random.nextBoolean() ? 1 : -1), 0.002D, 0.01D, 0.0D);
                }
            }
        }
    }

    @Nullable
    public CrucibleRecipe getRecipe() {
        if (inventory.get(0).isEmpty())
            return null;
        CrucibleRecipe recipe = CrucibleRecipe.findRecipe(inventory.get(0));
        if (recipe == null)
            return null;
        if (inventory.get(1).isEmpty() || (ItemStack.areItemsEqual(inventory.get(1), recipe.output) &&
                inventory.get(1).getCount() + recipe.output.getCount() <= getSlotLimit(1))) {
            return recipe;
        }
        return null;
    }

    public class CrucibleData implements IIntArray
    {
        public static final int BURN_TIME = 0;
        public static final int PROCESS_MAX = 1;
        public static final int CURRENT_PROCESS = 2;

        @Override
        public int get(int index)
        {
            switch(index)
            {
                case BURN_TIME:
                    return burnTime;
                case PROCESS_MAX:
                    return processMax;
                case CURRENT_PROCESS:
                    return process;
                default:
                    throw new IllegalArgumentException("Unknown index "+index);
            }
        }

        @Override
        public void set(int index, int value)
        {
            switch(index)
            {
                case BURN_TIME:
                    burnTime = value;
                    break;
                case PROCESS_MAX:
                    processMax = value;
                    break;
                case CURRENT_PROCESS:
                    process = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown index "+index);
            }
        }

        @Override
        public int size()
        {
            return 3;
        }
    }
}
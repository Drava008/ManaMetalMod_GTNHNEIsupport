package com.github.manametalmod.gtnhnei;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;

import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.DefaultOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;

public final class ConfigurableCraftingOverlayHandler extends DefaultOverlayHandler {

    private static final int SLOT_SPACING = 18;

    private final int recipeGridRelX;
    private final int recipeGridRelY;
    private final int gridWidth;
    private final int[] inputStackRelXs;
    private final int[] inputStackRelYs;
    private final int[] inputSlots;

    public ConfigurableCraftingOverlayHandler(
            int offsetX,
            int offsetY,
            int recipeGridRelX,
            int recipeGridRelY,
            int gridWidth,
            int[] inputStackRelXs,
            int[] inputStackRelYs,
            int[] inputSlots) {
        super(offsetX, offsetY);
        this.recipeGridRelX = recipeGridRelX;
        this.recipeGridRelY = recipeGridRelY;
        this.gridWidth = gridWidth;
        this.inputStackRelXs = inputStackRelXs.clone();
        this.inputStackRelYs = inputStackRelYs.clone();
        this.inputSlots = inputSlots.clone();
    }

    @Override
    protected Set<Slot> getCraftMatrixSlots(GuiContainer gui, IRecipeHandler handler) {
        Set<Slot> slots = new HashSet<Slot>();
        for (int slotNumber : inputSlots) {
            Slot slot = findSlot(gui, slotNumber);
            if (slot != null) {
                slots.add(slot);
            }
        }
        return slots;
    }

    @Override
    public Slot[][] mapIngredSlots(GuiContainer gui, List<PositionedStack> ingredients) {
        Slot[][] recipeSlotList = new Slot[ingredients.size()][];

        for (int i = 0; i < ingredients.size(); i++) {
            PositionedStack positionedStack = ingredients.get(i);
            int mappedIndex = mapIngredientIndex(positionedStack);

            LinkedList<Slot> recipeSlots = new LinkedList<Slot>();
            if (mappedIndex >= 0 && mappedIndex < inputSlots.length) {
                Slot slot = findSlot(gui, inputSlots[mappedIndex]);
                if (slot != null) {
                    recipeSlots.add(slot);
                }
            }

            recipeSlotList[i] = recipeSlots.toArray(new Slot[recipeSlots.size()]);
        }

        return recipeSlotList;
    }

    private int mapIngredientIndex(PositionedStack positionedStack) {
        if (inputStackRelXs.length == inputSlots.length && inputStackRelYs.length == inputSlots.length) {
            for (int i = 0; i < inputSlots.length; i++) {
                if (positionedStack.relx == inputStackRelXs[i] && positionedStack.rely == inputStackRelYs[i]) {
                    return i;
                }
            }
            return -1;
        }

        int column = Math.round((float) (positionedStack.relx - recipeGridRelX) / SLOT_SPACING);
        int row = Math.round((float) (positionedStack.rely - recipeGridRelY) / SLOT_SPACING);
        if (column < 0 || row < 0) {
            return -1;
        }
        return row * gridWidth + column;
    }

    private Slot findSlot(GuiContainer gui, int slotNumber) {
        for (Object rawSlot : gui.inventorySlots.inventorySlots) {
            Slot slot = (Slot) rawSlot;
            if (slot.slotNumber == slotNumber) {
                return slot;
            }
        }

        if (slotNumber >= 0 && slotNumber < gui.inventorySlots.inventorySlots.size()) {
            return (Slot) gui.inventorySlots.inventorySlots.get(slotNumber);
        }

        return null;
    }
}

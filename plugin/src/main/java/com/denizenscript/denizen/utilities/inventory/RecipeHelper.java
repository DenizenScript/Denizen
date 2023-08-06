package com.denizenscript.denizen.utilities.inventory;

import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.*;

public class RecipeHelper {

    public static class ShapeHelper {
        public ShapeHelper(ShapedRecipe recipe) {
            choiceMap = recipe.getChoiceMap();
            shapeText = recipe.getShape();
            int len = shapeText.length;
            while (len > 0 && shapeText[len - 1].isEmpty()) {
                len--;
            }
            while (len > 0 && shapeText[0].isEmpty()) {
                shapeText = Arrays.copyOfRange(shapeText, 1, len);
                len--;
            }
            height = len;
            width = 0;
            for (int i = 0; i < len; i++) {
                width = Math.max(width, shapeText[i].length());
            }
            if (height <= 0 || width <= 0) {
                throw new RuntimeException("ShapedRecipe malformed.");
            }
        }

        public int width;
        public int height;
        public String[] shapeText;
        public Map<Character, RecipeChoice> choiceMap;
    }

    public static int tryRecipeMatch(ItemStack[] matrix, int matrixWidth, ShapeHelper shape, int offsetX, int offsetY) {
        int max = 64;
        for (int shapeX = 0; shapeX < shape.width; shapeX++) {
            for (int shapeY = 0; shapeY < shape.height; shapeY++) {
                if (shape.shapeText[shapeY].length() <= shapeX) {
                    continue;
                }
                int x = offsetX + shapeX;
                int y = offsetY + shapeY;
                int matrixIndex = x + y * matrixWidth;
                ItemStack matrixItem = matrix[matrixIndex];
                RecipeChoice choices = shape.choiceMap.get(shape.shapeText[shapeY].charAt(shapeX));
                if (choices != null) {
                    if (matrixItem == null || !choices.test(matrixItem)) {
                        return 0;
                    }
                    max = Math.min(max, matrixItem.getAmount());
                }
            }
        }
        return max;
    }

    public static int getShapedQuantity(CraftingInventory inventory, ShapeHelper shape) {
        ItemStack[] matrix = inventory.getMatrix();
        int matrixWidth = matrix.length == 9 ? 3 : 2;
        int canMoveX = matrixWidth - shape.width;
        int canMoveY = matrixWidth - shape.height;
        for (int offsetX = 0; offsetX <= canMoveX; offsetX++) {
            for (int offsetY = 0; offsetY <= canMoveY; offsetY++) {
                int result = tryRecipeMatch(matrix, matrixWidth, shape, offsetX, offsetY);
                if (result > 0) {
                    return result;
                }
            }
        }
        return 0;
    }

    public static boolean tryRemoveSingle(List<ItemStack> items, List<RecipeChoice> choices) {
        HashSet<Integer> used = new HashSet<>();
        mainLoop:
        for (RecipeChoice choice : choices) {
            for (int i = 0; i < items.size(); i++) {
                ItemStack item = items.get(i);
                if (choice.test(item) && !used.contains(i)) {
                    used.add(i);
                    if (item.getAmount() == 1) {
                        items.remove(i);
                    }
                    else {
                        item.setAmount(item.getAmount() - 1);
                    }
                    continue mainLoop;
                }
            }
            return false;
        }
        return true;
    }

    public static int getShapelessQuantity(CraftingInventory inventory, ShapelessRecipe recipe) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : inventory.getMatrix()) {
            if (item != null && item.getType() != Material.AIR) {
                items.add(item.clone());
            }
        }
        int amount = 0;
        while (tryRemoveSingle(items, recipe.getChoiceList())) {
            amount++;
        }
        return amount;
    }

    public static int getMaximumOutputQuantity(Recipe recipe, CraftingInventory inventory) {
        if (recipe instanceof ShapedRecipe) {
            return getShapedQuantity(inventory, new ShapeHelper((ShapedRecipe) recipe));
        }
        else if (recipe instanceof ShapelessRecipe) {
            return getShapelessQuantity(inventory, (ShapelessRecipe) recipe);
        }
        else {
            return 1;
        }
    }
}

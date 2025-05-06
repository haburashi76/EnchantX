package io.github.haburashi76.enchant.recipes

import io.github.haburashi76.enchant.Setup
import io.github.haburashi76.enchant.item.heart_item
import io.github.haburashi76.enchant.item.magic_stone_item
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.recipe.CraftingBookCategory

class Recipes: Setup {
    private val magicStoneRecipe = ShapelessRecipe(NamespacedKey.minecraft("magic_stone"), magic_stone_item).apply {
        addIngredient(1, Material.LAPIS_LAZULI)
        addIngredient(1, Material.AMETHYST_SHARD)
        category = CraftingBookCategory.EQUIPMENT
    }
    private val heartRecipe = ShapedRecipe(NamespacedKey.minecraft("star_heart"), heart_item).apply {
        shape("@@@", "@#@", "@@@")
        setIngredient('@', magic_stone_item)
        setIngredient('#', Material.POPPED_CHORUS_FRUIT)
        category = CraftingBookCategory.EQUIPMENT
    }
    override fun setup() {
        Bukkit.addRecipe(magicStoneRecipe)
        Bukkit.addRecipe(heartRecipe)
    }
}
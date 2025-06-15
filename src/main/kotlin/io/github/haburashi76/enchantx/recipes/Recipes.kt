package io.github.haburashi76.enchantx.recipes

import io.github.haburashi76.enchantx.Setup
import io.github.haburashi76.enchantx.item.book_item
import io.github.haburashi76.enchantx.item.heart_item
import io.github.haburashi76.enchantx.item.magic_stone_item
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
    private val magicBookRecipe = ShapelessRecipe(NamespacedKey.minecraft("magic_book"), book_item).apply {
        addIngredient(3, heart_item)
        addIngredient(1, Material.BOOK)
    }
    override fun setup() {
        Bukkit.addRecipe(magicStoneRecipe)
        Bukkit.addRecipe(heartRecipe)
        Bukkit.addRecipe(magicBookRecipe)
    }
}
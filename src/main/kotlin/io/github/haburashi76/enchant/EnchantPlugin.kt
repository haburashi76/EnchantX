package io.github.haburashi76.enchant

import io.github.haburashi76.enchant.eventListeners.*
import io.github.haburashi76.enchant.recipes.Recipes
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class EnchantPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        val setups: List<Setup> = listOf(AnvilListener(this),
            CombatListener(this), CraftingListener(this),
            EnchantTableListener(this),
            SmithingListener(this),
            Recipes())
        setups.forEach {
            it.setup()
        }
    }
}

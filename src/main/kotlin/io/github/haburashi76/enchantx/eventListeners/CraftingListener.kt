package io.github.haburashi76.enchantx.eventListeners

import io.github.haburashi76.enchantx.Setup
import io.github.haburashi76.enchantx.item.*
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.plugin.java.JavaPlugin

class CraftingListener(private val plugin: JavaPlugin) : Listener, Setup {
    override fun setup() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val matrix = event.inventory.matrix

        for (i in matrix) {
            if (i == null) continue
            if (!i.hasItemMeta()) continue

            val meta = i.itemMeta!!
            if (meta == magic_stone_item.itemMeta) {
                if (event.inventory.result?.type == Material.RECOVERY_COMPASS) {
                    event.inventory.result = null
                }
            }
            if (meta == heart_item.itemMeta) {
                event.inventory.result = null
            }
        }
    }
}
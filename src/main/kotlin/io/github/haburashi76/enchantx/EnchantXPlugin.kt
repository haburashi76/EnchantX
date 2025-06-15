package io.github.haburashi76.enchantx

import io.github.haburashi76.enchantx.eventListeners.*
import io.github.haburashi76.enchantx.item.getPotential
import io.github.haburashi76.enchantx.recipes.Recipes
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.roundToInt

class EnchantXPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        val setups: List<Setup> = listOf(AnvilListener(this),
            CombatListener(this), CraftingListener(this),
            EnchantTableListener(this),
            SmithingListener(this),
            GrindstoneListener(this),
            Recipes())
        setups.forEach {
            it.setup()
        }
        object : BukkitRunnable() {
            override fun run() {
                server.onlinePlayers.forEach { player ->
                    if (player.getPotential(Potential.MOVE_SPEED) > 0) {
                        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 20, (player.getPotential(Potential.MOVE_SPEED) / 3).roundToInt() - 1))
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L)

    }
}

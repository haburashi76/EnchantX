package io.github.haburashi76.enchantx.eventListeners

import io.github.haburashi76.enchantx.Setup
import io.github.haburashi76.enchantx.item.*
import io.github.haburashi76.enchantx.maps.*
import org.bukkit.Material
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

class CombatListener(private val plugin: JavaPlugin): Listener, Setup {
    override fun setup() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        if (event.entity is Monster) {
            if (Random.nextDouble() > 0.95) {
                event.drops.add(ItemStack(Material.AMETHYST_SHARD, Random.nextInt(1, 4)))
            }
        }
    }
    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val damager = event.entity
        if (damager is Player) {
            if (damager.inventory.itemInMainHand.plusLevel > 0) {
                if (damager.inventory.itemInMainHand.type.isWeapon()) {
                    event.damage *= (1.0 +
                            if (damager.inventory.itemInMainHand.type.isNetherite())
                                netheriteWeaponDamageMap[damager.inventory.itemInMainHand.plusLevel] else weaponDamageMap[damager.inventory.itemInMainHand.plusLevel])
                } else {
                    event.damage *= (1.0 +
                            if (damager.inventory.itemInMainHand.type.isNetherite())
                                netheriteOtherDamageMap[damager.inventory.itemInMainHand.plusLevel] else otherDamageMap[damager.inventory.itemInMainHand.plusLevel])
                }
            }
        }
    }
    @EventHandler
    fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        if (entity is Player) {
            entity.inventory.armorContents.forEach {
                if (it != null) {
                    if (it.plusLevel > 0 && it.type.isArmor()) {
                        event.damage *= 1.0 -
                                if (it.type.isNetherite()) netheriteArmorBlockMap[it.plusLevel] else armorBlockMap[it.plusLevel]
                    }
                }
            }
        }
    }
}
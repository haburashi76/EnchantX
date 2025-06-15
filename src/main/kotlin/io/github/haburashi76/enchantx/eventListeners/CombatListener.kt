package io.github.haburashi76.enchantx.eventListeners

import io.github.haburashi76.enchantx.Potential
import io.github.haburashi76.enchantx.Setup
import io.github.haburashi76.enchantx.item.*
import io.github.haburashi76.enchantx.maps.*
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerVelocityEvent
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
            entity.getPotential(Potential.BLOCKING).let { event.damage *= ((100.0 - it) / 100.0) }
            if (entity.isInWaterOrRainOrBubbleColumn) {
                event.damage *= ((100.0 - entity.getPotential(Potential.SEA_BENEFITS)) / 100.0)
            }
        }
    }
    @EventHandler
    fun onProjectile(event: EntityShootBowEvent) {
        if (event.projectile !is AbstractArrow) return
        (event.projectile as AbstractArrow).damage = ((event.projectile as AbstractArrow).damage + (event.bow!!.plusLevel.toDouble() * 0.02)) * (1 + (event.bow!!.plusLevel.toDouble() * 0.02))
    }
    @EventHandler
    fun onHeal(event: EntityRegainHealthEvent) {
        val entity = event.entity
        if (entity !is Player) return
        event.amount *= ((100.0+entity.getPotential(Potential.HEALING_BOOST)) / 100.0)
    }
    @EventHandler
    fun onKnockBack(event: PlayerVelocityEvent) {
        if (Random.nextDouble(100.0) < event.player.getPotential(Potential.ANTI_KNOCKBACK)) {
            event.isCancelled = true
        }
    }
    @EventHandler
    fun onDamageByEntity(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val entity = event.entity
        if (damager is Player) {
            if (Random.nextDouble(100.0) < damager.getPotential(Potential.AGILITY)) {
                event.isCancelled = true
            }
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
            damager.getPotential(Potential.ATTACK).let { event.damage *= ((100.0 + it) / 100.0) }
            damager.getPotential(Potential.CRITICAL_PROBABILITY).let { prob ->
                if (Random.nextDouble(0.0, 100.0) < prob) {
                    damager.getPotential(Potential.CRITICAL_DAMAGE).let { critDam ->
                        event.damage *= ((100.0 + critDam) / 100.0)
                    }
                }
            }
            damager.getPotential(Potential.ANTI_DEFENSE).let { potential ->
                if (entity is LivingEntity) {
                    entity.getAttribute(Attribute.GENERIC_ARMOR)?.let { armor ->
                        event.damage += (armor.value * 0.03 * potential)
                    }
                }
            }
        }
    }
}
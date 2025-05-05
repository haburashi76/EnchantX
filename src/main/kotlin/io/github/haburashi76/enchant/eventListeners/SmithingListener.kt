package io.github.haburashi76.enchant.eventListeners

import io.github.haburashi76.enchant.item.isArmor
import io.github.haburashi76.enchant.item.isNetherite
import io.github.haburashi76.enchant.item.isWeapon
import io.github.haburashi76.enchant.item.plusLevel
import io.github.haburashi76.enchant.maps.*
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.plugin.java.JavaPlugin

class SmithingListener(
    private val plugin: JavaPlugin,
): Setup, Listener {

    override fun setup() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
    @EventHandler
    fun onNetherite(event: PrepareSmithingEvent) {
        if (event.result == null) return
        val inventory = event.inventory

        val addition = inventory.inputMineral
        val template = inventory.inputTemplate

        if (addition != null &&
            addition.type == Material.NETHERITE_INGOT &&
            template?.type == Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE
        ) {
            event.result = event.result?.clone()?.apply {
                if (plusLevel > 0) {
                    lore(
                        listOf(
                            Component.text(""),
                            Component.text(starMap[this.plusLevel]),
                            Component.text(
                                if (event.result?.type?.isWeapon() == true)
                                    "(주로 사용하는 손에서)최종 피해량 ${
                                        1.0 +
                                                if (this.type.isNetherite()) netheriteWeaponDamageMap[this.plusLevel] else weaponDamageMap[this.plusLevel]
                                    }배"
                                else if (event.result?.type?.isArmor() == true)
                                    "받는 피해 ${
                                        1.0 -
                                                if (this.type.isNetherite()) netheriteArmorBlockMap[this.plusLevel] else armorBlockMap[this.plusLevel]
                                    }배"
                                else "(주로 사용하는 손에서)최종 피해량 ${
                                    1.0 +
                                            if (this.type.isNetherite()) netheriteOtherDamageMap[this.plusLevel] else otherDamageMap[this.plusLevel]
                                }배"
                            )
                        )
                    )
                }
            }
        }
    }
}
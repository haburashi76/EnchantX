package io.github.haburashi76.enchant.eventListeners

import io.github.haburashi76.enchant.Setup
import io.github.haburashi76.enchant.item.*
import io.github.haburashi76.enchant.keys.plusLevelKey
import io.github.haburashi76.enchant.maps.*
import io.github.monun.invfx.InvFX
import io.github.monun.invfx.openFrame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

class AnvilListener(
    private val plugin: JavaPlugin,
) : Listener, Setup {

    override fun setup() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventory.type == InventoryType.ANVIL) {
            if (event.player.getCooldown(Material.PETRIFIED_OAK_SLAB) == 0) {
                event.isCancelled = true
                anvilSelectInterface(event.player)
            }
        }
    }
    private fun anvilSelectInterface(player: HumanEntity) {
        val frame = InvFX.frame(1, Component.text("제련 방법 선택")) {
            slot(2, 0) {
                item = ItemStack(Material.ANVIL).apply {
                    itemMeta = itemMeta.apply {
                        displayName(Component.text("모루"))
                    }
                }
                onClick { event ->
                    if (event.click == ClickType.LEFT) {
                        event.whoClicked.setCooldown(Material.PETRIFIED_OAK_SLAB, 5)
                        event.whoClicked.closeInventory()
                        event.whoClicked.openAnvil(null, true)
                    }
                }

            }
            slot(6, 0) {
                item = ItemStack(Material.IRON_HELMET).apply {
                    itemMeta = itemMeta.apply {
                        displayName(Component.text("장비 강화"))
                    }
                }
                onClick { event ->
                    if (event.click == ClickType.LEFT) {
                        event.whoClicked.closeInventory()
                        itemEnforceInventoryOpen(event.whoClicked)
                    }
                }
            }
        }

        (player as Player).openFrame(frame)
    }
    private fun itemEnforceInventoryOpen(player: HumanEntity) {

        val frame = InvFX.frame(6, Component.text("\uEBBB\uEAAB").color(NamedTextColor.WHITE)) {
            var currentItemSlot = -10000
            var currentShardSlot = -10000

            val shardSlot = slot(4, 4) {
                onClick { event ->
                    if (event.currentItem != null) {
                        currentShardSlot = -10000
                        event.currentItem = null
                    }
                }
            }


            val itemSlot = slot(4, 0) {
                onClick { event ->
                    if (event.click == ClickType.LEFT) {
                        if (shardSlot.item == null) {
                            currentItemSlot = -10000
                            item = null
                        } else {
                            if (item?.plusLevel!! < 10) {
                                if (shardSlot.item!!.amount > 1) {
                                    shardSlot.item!!.amount = shardSlot.item!!.amount - 1
                                } else {
                                    shardSlot.item = null
                                }
                                event.whoClicked.inventory.setItem(currentShardSlot, shardSlot.item)
                                if (Random.nextDouble(0.0, 100.0) <= probMap[event.currentItem?.plusLevel!!]) {
                                    event.currentItem!!.plusLevel += 1

                                    (event.whoClicked as Player).playSound(
                                        event.whoClicked,
                                        Sound.ENTITY_ARROW_HIT_PLAYER,
                                        1.0f,
                                        1.0f
                                    )
                                    event.currentItem!!.lore(
                                        listOf(
                                            Component.text(if (event.currentItem!!.plusLevel < 10) "현재 강화 확률: ${probMap[event.currentItem!!.plusLevel]}%" else "최대 레벨입니다."),
                                            Component.text(starMap[event.currentItem!!.plusLevel])
                                        )
                                    )

                                    event.whoClicked.inventory.setItem(
                                        currentItemSlot,
                                        event.currentItem!!.clone().apply {
                                            lore(listOf(Component.text(""),
                                                Component.text(starMap[this.plusLevel]),
                                                Component.text(
                                                    if (event.currentItem?.type?.isWeapon() == true)
                                                        "(주로 사용하는 손에서)최종 피해량 ${1.0 +
                                                                if (this.type.isNetherite()) netheriteWeaponDamageMap[this.plusLevel] else weaponDamageMap[this.plusLevel]}배"
                                                    else if (event.currentItem?.type?.isArmor() == true)
                                                        "받는 피해 ${1.0 -
                                                                if (this.type.isNetherite()) netheriteArmorBlockMap[this.plusLevel] else armorBlockMap[this.plusLevel]}배"
                                                    else "(주로 사용하는 손에서)최종 피해량 ${1.0 +
                                                            if (this.type.isNetherite()) netheriteOtherDamageMap[this.plusLevel] else otherDamageMap[this.plusLevel]}배"
                                                )))
                                        })
                                } else {
                                    (event.whoClicked as Player).playSound(
                                        event.whoClicked,
                                        Sound.BLOCK_ANVIL_LAND,
                                        0.6f,
                                        0.4f
                                    )
                                }

                            }

                        }
                    }
                }
            }


            onClickBottom { event ->
                if (event.click == ClickType.LEFT) {
                    if (event.currentItem?.canEnchanting() == true) {
                        itemSlot.item = event.currentItem?.clone()?.apply {
                            itemMeta = itemMeta.apply {
                                if (!persistentDataContainer.has(plusLevelKey)) {
                                    persistentDataContainer.set(plusLevelKey, PersistentDataType.INTEGER, 0)
                                }
                            }
                            lore(
                                listOf(
                                    Component.text(if (this.plusLevel < 10) "현재 강화 확률: ${probMap[this.plusLevel]}%" else "최대 레벨입니다."),
                                    Component.text(starMap[this.plusLevel])
                                )
                            )
                        }
                        currentItemSlot = event.slot
                    }
                    if (event.currentItem?.itemMeta == heart_item.itemMeta) {
                        shardSlot.item = event.currentItem?.clone()
                        currentShardSlot = event.slot
                    }
                }
            }
        }
        (player as Player).openFrame(frame)


    }
}
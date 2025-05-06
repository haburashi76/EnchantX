package io.github.haburashi76.enchant.eventListeners

import io.github.haburashi76.enchant.Setup
import io.github.haburashi76.enchant.item.canEnchanting
import io.github.haburashi76.enchant.item.magic_stone_item
import io.github.haburashi76.enchant.item.plusLevel
import io.github.monun.invfx.InvFX
import io.github.monun.invfx.openFrame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

class EnchantTableListener(
    private val plugin: JavaPlugin,
) : Listener, Setup {


    override fun setup() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventory.type == InventoryType.ENCHANTING) {
            event.isCancelled = true
            enchantingInventoryOpen(event.player)
        }
    }

    private fun enchantingInventoryOpen(player: HumanEntity) {

        val newFrame = InvFX.frame(6, Component.text("\uEBBB\uEAAA").color(NamedTextColor.WHITE)) {

            var selectShardSlot: Int = -10000
            var selectItemSlot: Int = -10000
            val shardSlot = slot(4, 4) {
            }
            val itemSlot = slot(4, 0) {
            }
            itemSlot.onClick { event ->
                if (event.click == ClickType.LEFT) {
                    if (shardSlot.item != null && itemSlot.item != null) {
                        val enchantCount = Random.nextInt(2, 6)

                        var enchantList: MutableList<Enchantment> =
                            Enchantment.values()
                                .filter { it.canEnchantItem(event.currentItem!!) } as MutableList<Enchantment>

                        if (shardSlot.item!!.amount > 1) {
                            shardSlot.item!!.amount = shardSlot.item!!.amount - 1
                        } else {
                            shardSlot.item = null
                        }
                        event.whoClicked.inventory.setItem(selectShardSlot, shardSlot.item)
                        for (en in event.currentItem!!.enchantments) {
                            event.currentItem!!.removeEnchantment(en.key)
                        }
                        repeat(enchantCount) {
                            val randomEnchantment = enchantList.random()
                            event.currentItem!!.addEnchantment(
                                randomEnchantment,
                                Random.nextInt(1, randomEnchantment.maxLevel + 1)
                            )
                            enchantList = Enchantment.values()
                                .filter { it.canEnchantItem(event.currentItem!!) } as MutableList<Enchantment>
                            event.whoClicked.inventory.setItem(selectItemSlot, event.currentItem)
                        }
                    }
                }
            }

            onClickBottom { event ->

                if (event.click == ClickType.LEFT) {
                    if (event.currentItem?.canEnchanting() == true && event.currentItem?.plusLevel == 0) {
                        itemSlot.item = event.currentItem
                        selectItemSlot = event.slot
                    }

                    if (event.currentItem?.itemMeta == magic_stone_item.itemMeta) {

                        shardSlot.item = event.currentItem?.clone()
                        selectShardSlot = event.slot

                        //event.currentItem = ItemStack(Material.AIR)
                        shardSlot.onClick { e ->
                            if (e.click == ClickType.LEFT) {
                                shardSlot.item = null
                                selectShardSlot = -10000
                            }
                        }
                    }
                }
            }

        }
        (player as Player).openFrame(newFrame)
    }
}
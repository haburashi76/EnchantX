package io.github.haburashi76.enchantx.eventListeners

import io.github.haburashi76.enchantx.Setup
import io.github.haburashi76.enchantx.item.*
import io.github.haburashi76.enchantx.maps.*
import io.github.monun.invfx.InvFX
import io.github.monun.invfx.openFrame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.EnchantmentStorageMeta
import org.bukkit.plugin.java.JavaPlugin

class SmithingListener(
    private val plugin: JavaPlugin,
): Setup, Listener {

    private val prevItem = ItemStack(Material.DIAMOND_AXE).apply {
        itemMeta = itemMeta.apply {
            displayName(
                Component.text().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
                    .decorate(TextDecoration.BOLD).content("<-").build()
            )
        }
        ItemFlag.entries.forEach {
            this.addItemFlags(it)
        }
    }
    private val nextItem = ItemStack(Material.DIAMOND_HELMET).apply {
        itemMeta = itemMeta.apply {
            displayName(
                Component.text().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false)
                    .decorate(TextDecoration.BOLD).content("->").build()
            )
        }
        ItemFlag.entries.forEach {
            this.addItemFlags(it)
        }
    }

    override fun setup() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        if (event.clickedBlock?.type == Material.SMITHING_TABLE && !event.player.isSneaking && event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true
            smithingSelectInterface(event.player)
        }
    }
    private fun smithingSelectInterface(player: HumanEntity) {
        val frame = InvFX.frame(1, Component.text("제련 방법 선택")) {
            slot(2, 0) {
                item = ItemStack(Material.SMITHING_TABLE).apply {
                    itemMeta = itemMeta.apply {
                        displayName(Component.text("대장장이 작업대"))
                    }
                }
                onClick { event ->
                    if (event.click == ClickType.LEFT) {
                        event.whoClicked.closeInventory()
                        event.whoClicked.openSmithingTable(null, true)
                    }
                }

            }
            slot(6, 0) {
                item = ItemStack(Material.IRON_HELMET).apply {
                    itemMeta = itemMeta.apply {
                        displayName(Component.text("마법 추출"))
                    }
                }
                onClick { event ->
                    if (event.click == ClickType.LEFT) {
                        event.whoClicked.closeInventory()
                        extractionInventoryOpen(event.whoClicked)
                    }
                }
            }
        }
        (player as Player).openFrame(frame)
    }
    private fun extractionInventoryOpen(player: HumanEntity) {
        val frame = InvFX.frame(1, Component.text("\uEBBB\uEAAC").color(NamedTextColor.WHITE)) {
            var currentslot: Int? = null
            var bookslot1: Int? = null
            val book = slot(6, 0) {}

            slot(2, 0) {
                onClick { event ->
                    if (this.item != null && book.item != null) {
                        event.whoClicked.closeInventory()
                        extractionUIOpen(event.whoClicked, currentslot!!, bookslot1!!)
                    }
                }
            }
            onClickBottom { event ->
                if (event.click == ClickType.LEFT) {
                    if (event.currentItem?.canEnchanting() == true && event.currentItem?.enchantments?.size!! > 0) {
                        item(2, 0, event.currentItem?.clone())
                        currentslot = event.slot
                    }
                    if (event.currentItem?.type == Material.BOOK) {
                        item(6, 0, event.currentItem?.clone())
                        bookslot1 = event.slot
                    }
                }
            }
        }

        (player as Player).openFrame(frame)
    }

    private fun extractionUIOpen(player: HumanEntity, slot1: Int, slot2: Int) {
        val frame = InvFX.frame(1, Component.text("마법 추출")) {
            val list = list(1,
                0,
                7,
                0,
                true,
                {
                    player.inventory.getItem(slot1)!!.enchantments.toList().toMutableList().apply {
                        while (size % 7 != 0) {
                            add(Enchantment.values().filter { !player.inventory.getItem(slot1)?.containsEnchantment(it)!! }.random() to 100)
                        }
                    }
                }) {
                transform { enchant ->
                    ItemStack(if (enchant.second == 100) Material.AIR else Material.ENCHANTED_BOOK).also { item ->
                        if (item.type != Material.AIR) {
                            item.itemMeta = item.itemMeta?.apply {
                                if (this is EnchantmentStorageMeta) {
                                    addStoredEnchant(enchant.first, enchant.second, true)
                                }
                            }
                        }
                    }
                }
            }
            onClick { x, y, event ->
                if (event.click == ClickType.LEFT && event.currentItem?.type == Material.ENCHANTED_BOOK) {
                    if (player.inventory.getItem(slot2) != null) {
                        val index = list.index
                        event.currentItem!!.itemMeta as EnchantmentStorageMeta
                        if (player.inventory.firstEmpty() == -1) {
                            player.world.dropItem(player.location, event.currentItem!!.clone())
                        } else {
                            player.inventory.addItem(event.currentItem!!.clone())
                        }
                        player.inventory.getItem(slot1)!!.removeEnchantment(
                            (event.currentItem?.itemMeta as EnchantmentStorageMeta)
                                .storedEnchants.toList().first().first
                        )
                        item(x, y, null)
                        list.index = index
                        if (player.inventory.getItem(slot2)!!.amount > 1) {
                            player.inventory.getItem(slot2)!!.amount -= 1
                        } else {
                            player.inventory.setItem(slot2, null)
                        }
                    }
                }
            }
            slot(0, 0) {
                item = prevItem
                onClick {
                    list.index-=7
                }
            }
            slot(8, 0) {
                item = nextItem
                onClick {
                    list.index+=7
                }
            }
        }

        (player as Player).openFrame(frame)

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
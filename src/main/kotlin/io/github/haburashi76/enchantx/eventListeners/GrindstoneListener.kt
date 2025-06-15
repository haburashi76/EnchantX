package io.github.haburashi76.enchantx.eventListeners

import io.github.haburashi76.enchantx.Potential
import io.github.haburashi76.enchantx.Potential.*
import io.github.haburashi76.enchantx.Setup
import io.github.haburashi76.enchantx.item.*
import io.github.haburashi76.enchantx.keys.*
import io.github.monun.invfx.InvFX
import io.github.monun.invfx.frame.InvFrame
import io.github.monun.invfx.openFrame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.round
import kotlin.random.Random


class GrindstoneListener(
    private val plugin: JavaPlugin,
) : Listener, Setup {

    override fun setup() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onClick(event: PlayerInteractEvent) {
        if (event.clickedBlock?.type == Material.GRINDSTONE && !event.player.isSneaking && event.action == Action.RIGHT_CLICK_BLOCK) {
            event.isCancelled = true
            selectionInterface(event.player)
        }
    }

    private fun selectionInterface(player: HumanEntity) {
        val frame = InvFX.frame(1, Component.text("제련 방법 선택")) {
            slot(2, 0) {
                item = ItemStack(Material.GRINDSTONE).apply {
                    itemMeta = itemMeta.apply {
                        displayName(Component.text("숫돌"))
                    }
                }
                onClick { event ->
                    if (event.click == ClickType.LEFT) {
                        event.whoClicked.closeInventory()
                        event.whoClicked.openGrindstone(null, true)
                    }
                }

            }
            slot(6, 0) {
                item = ItemStack(Material.NETHER_STAR).apply {
                    itemMeta = itemMeta.apply {
                        displayName(Component.text("잠재력 해방"))
                    }
                }
                onClick { event ->
                    if (event.click == ClickType.LEFT) {
                        event.whoClicked.closeInventory()
                        unleashingPotentialInventoryOpen(event.whoClicked)
                    }
                }
            }
        }

        (player as Player).openFrame(frame)
    }
    private fun unleashingPotentialInventoryOpen(player: HumanEntity) {

        val frame = InvFX.frame(6, Component.text("\uEBBB\uEAAD").color(NamedTextColor.WHITE)) {
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
                            if (item?.plusLevel!! >= 8) {
                                player.closeInventory()
                                (player as Player).openFrame(newInventory(player, currentItemSlot, currentShardSlot))
                            }
                        }
                    }
                }
            }

            onClickBottom { event ->
                if (event.click == ClickType.LEFT) {
                    if (event.currentItem != null && event.currentItem!!.canEnchanting() && event.currentItem!!.potentials.size < 4
                        && event.currentItem!!.plusLevel >= 8) {
                        itemSlot.item = event.currentItem?.clone()
                        currentItemSlot = event.slot
                    }
                    if (event.currentItem?.itemMeta == book_item.itemMeta) {
                        shardSlot.item = event.currentItem?.clone()
                        currentShardSlot = event.slot
                    }
                }
            }
        }
        (player as Player).openFrame(frame)
    }

    private fun newInventory(player: HumanEntity, currentItemSlot: Int, currentShardSlot: Int): InvFrame {

        val frame = InvFX.frame(1, Component.text("타이밍에 맞춰 클릭")) {

            val list = list(0, 0, 8, 0, true, { (0..8).toList() }) {
                transform { itemIndex ->
                    if (itemIndex == 4) {
                        ItemStack(Material.RED_CONCRETE).apply {
                            itemMeta = itemMeta.apply {
                                displayName(Component.text("<타이밍에 맞춰 클릭>"))
                            }
                        }
                    } else {
                        ItemStack(Material.WHITE_CONCRETE).apply {
                            itemMeta = itemMeta.apply {
                                displayName(Component.text(" "))
                            }
                        }
                    }
                }
                onClickItem { x, y, _, event ->
                    if (x == 4 && y == 0) {
                        val prob: Double = if (event.currentItem!!.type == Material.GREEN_CONCRETE) {
                            100.0
                        } else {
                            1.2
                        }
                        if (Random.nextDouble(100.0) <= prob) {
                            player.inventory.setItem(currentItemSlot, player.inventory.getItem(currentItemSlot)?.clone().apply {
                                potentialOpen(this!!)
                                loreSet()
                            })
                            player.world.playSound(player.location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2.0f, 1.0f)
                        } else {
                            player.world.playSound(player.location, Sound.BLOCK_ANVIL_LAND, 0.6f, 0.4f)
                        }

                        if (player.inventory.getItem(currentShardSlot)!!.amount > 1) {
                            player.inventory.getItem(currentShardSlot)!!.amount -= 1
                        } else {
                            player.inventory.setItem(currentShardSlot, null)
                        }
                        player.closeInventory()
                    }
                }
            }
            var isOpened = true
            onOpen {
                object : BukkitRunnable() {
                    var i = 0
                    var rising = true
                    override fun run() {
                        list.refresh()
                        if (!isOpened) {
                            cancel()
                        }
                        if (i - 1 >= 0) {
                            item(i - 1, 0, if (i - 1 == 4) {
                                ItemStack(Material.RED_CONCRETE).apply {
                                    itemMeta = itemMeta.apply {
                                        displayName(Component.text("<타이밍에 맞춰 클릭>"))
                                    }
                                }
                            } else {
                                ItemStack(Material.WHITE_CONCRETE).apply {
                                    itemMeta = itemMeta.apply {
                                        displayName(Component.text(" "))
                                    }
                                }
                            })
                        }
                        if (i + 1 <= 8) {
                            item(i+1, 0, if (i + 1 == 4) {
                                ItemStack(Material.RED_CONCRETE).apply {
                                    itemMeta = itemMeta.apply {
                                        displayName(Component.text("<타이밍에 맞춰 클릭>"))
                                    }
                                }
                            } else {
                                ItemStack(Material.WHITE_CONCRETE).apply {
                                    itemMeta = itemMeta.apply {
                                        displayName(Component.text(" "))
                                    }
                                }
                            })
                        }
                        item(i, 0, if (i == 4) {
                            ItemStack(Material.GREEN_CONCRETE).apply {
                                itemMeta = itemMeta.apply {
                                    displayName(Component.text("클릭"))
                                }
                            }
                        }
                        else {
                            ItemStack(Material.GREEN_CONCRETE).apply {
                                itemMeta = itemMeta.apply {
                                    displayName(Component.text(" "))
                                }
                            }
                        })

                        if (i <= 0) {
                            rising = true
                        }
                        if (i >= 8) {
                            rising = false
                        }
                        if (rising) {
                            i++
                        } else {
                            i--
                        }
                    }
                }.runTaskTimer(plugin, 0L, 3L)
            }

            onClose {
                isOpened = false
            }
        }
        return frame
    }
    private fun potentialOpen(item: ItemStack) {
        item.itemMeta = item.itemMeta.apply {
            val potentials = Potential.entries.filter { newPotential -> !item.potentials.any { newPotential == it.first } }
            val potential = potentials.random()
            val random = when(potential) {
                ATTACK -> 2.5 to 9.5
                BLOCKING -> 4.5 to 10.0
                CRITICAL_PROBABILITY -> 5.0 to 14.0
                CRITICAL_DAMAGE -> 10.0 to 28.0
                MOVE_SPEED -> 1.0 to 1.5
                ANTI_DEFENSE -> 1.0 to 3.0
                HEALING_BOOST -> 4.0 to 5.0
                AGILITY -> 2.5 to 5.0
                ANTI_KNOCKBACK -> 4.5 to 10.0
                SEA_BENEFITS -> 3.0 to 9.5
            }
            val key = when(potential) {
                ATTACK -> atkKey
                BLOCKING -> blkKey
                CRITICAL_PROBABILITY -> cpKey
                CRITICAL_DAMAGE -> cdKey
                MOVE_SPEED -> msKey
                ANTI_DEFENSE -> adKey
                HEALING_BOOST -> hbKey
                AGILITY -> agKey
                ANTI_KNOCKBACK -> akKey
                SEA_BENEFITS -> sbKey
            }
            val randomValue = Random.nextDouble(random.first, random.second)

            persistentDataContainer.set(key, PersistentDataType.DOUBLE, round(randomValue*100)/100)
        }
    }
}
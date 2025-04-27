package org.ToothBrush.enchant

import io.github.monun.invfx.InvFX
import io.github.monun.invfx.openFrame
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.HumanEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.entity.Snowman
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.recipe.CraftingBookCategory
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

class Enchant : JavaPlugin(), Listener {
    private val weaponDamageMap = listOf(0.0, 0.02, 0.04, 0.08, 0.15, 0.3, 0.45, 0.5, 0.6, 0.75, 0.95)
    private val otherDamageMap = listOf(0.0, 0.01, 0.02, 0.04, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4)
    private val netheriteDamageCoefficient = 1.4
    private val netheriteBlockingCoefficient = 1.2
    private val armorBlockMap = listOf(1.0, 0.99, 0.98, 0.97, 0.96, 0.95, 0.94, 0.92, 0.9, 0.86, 0.84).map {
        1.0 - it
    }

    private val probMap = listOf(100.0, 100.0, 80.0, 70.0, 60.0, 30.0, 15.0, 5.0, 2.0, 0.5)
    private val starMap = listOf(
        "☆☆☆☆☆☆☆☆☆☆",
        "★☆☆☆☆☆☆☆☆☆",
        "★★☆☆☆☆☆☆☆☆",
        "★★★☆☆☆☆☆☆☆",
        "★★★★☆☆☆☆☆☆",
        "★★★★★☆☆☆☆☆",
        "★★★★★★☆☆☆☆",
        "★★★★★★★☆☆☆",
        "★★★★★★★★☆☆",
        "★★★★★★★★★☆",
        "★★★★★★★★★★")
    private val plusLevelKey = NamespacedKey(this, "plus_level")
    private val magic_ston_key = NamespacedKey(this, "magic_stone")
    private val item = ItemStack(Material.ECHO_SHARD).apply {
        itemMeta = itemMeta?.apply {
            displayName(Component.text("마석"))
            persistentDataContainer.set(magic_ston_key, PersistentDataType.BOOLEAN, true)
            addUnsafeEnchantment(Enchantment.LUCK, 1)
            addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

    }
    private val starHeartKey = NamespacedKey(this, "star_heart")
    private val starHeartItem = ItemStack(Material.HEART_OF_THE_SEA).apply {
        addUnsafeEnchantment(Enchantment.OXYGEN, 1)
        addItemFlags(ItemFlag.HIDE_ENCHANTS)
        itemMeta = itemMeta!!.apply {
            displayName(Component.text("별의 심장"))
            persistentDataContainer.set(starHeartKey, PersistentDataType.BOOLEAN, true)
        }

    }
    override fun onEnable() {
        // Plugin startup logic
        server.pluginManager.registerEvents(this, this)
        item.addUnsafeEnchantment(Enchantment.LUCK, 1)
        val met = item.itemMeta

        met.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        item.itemMeta = met



        val recipe = ShapelessRecipe(NamespacedKey.minecraft("magic_stone"), item).apply {
            addIngredient(1, Material.LAPIS_LAZULI)
            addIngredient(1, Material.AMETHYST_SHARD)
            category = CraftingBookCategory.EQUIPMENT


        //shape("AB ")
            //setIngredient('A', Material.LAPIS_LAZULI)
            //setIngredient('B', Material.AMETHYST_SHARD)
        }
        Bukkit.addRecipe(recipe)

        val recipe2 = ShapedRecipe(NamespacedKey.minecraft("star_heart"), starHeartItem).apply {
            shape("@@@", "@#@", "@@@")
            setIngredient('@', item)
            setIngredient('#', Material.POPPED_CHORUS_FRUIT)
            category = CraftingBookCategory.EQUIPMENT
        }
        Bukkit.addRecipe(recipe2)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }



    @EventHandler
    fun onInventoryOpen(event: InventoryOpenEvent) {
        if (event.inventory.type == InventoryType.ENCHANTING) {
            event.isCancelled = true
            enchantingInventoryOpen(event.player)
        }
        if (event.inventory.type == InventoryType.ANVIL) {
            if (event.player.getCooldown(Material.PETRIFIED_OAK_SLAB) == 0) {
                event.isCancelled = true
                anvilSelectInterface(event.player)
            }
        }
    }
    private fun ItemStack.canEnchanting(): Boolean {
        return Enchantment.values().any {
            it.canEnchantItem(ItemStack(this.type))
        }
    }
    private var ItemStack.plusLevel: Int
        get() {
            if (itemMeta?.persistentDataContainer?.has(plusLevelKey) == false) {
                itemMeta?.persistentDataContainer?.set(plusLevelKey, PersistentDataType.INTEGER, 0)
            }
            itemMeta?.persistentDataContainer?.get(plusLevelKey, PersistentDataType.INTEGER)?.let {
                return it
            }
            return 0
        }

        set(v) {
            itemMeta = itemMeta?.apply {
                persistentDataContainer.set(plusLevelKey, PersistentDataType.INTEGER, v)
            }
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
                                                    "(주로 사용하는 손에서)최종 피해량 ${1.0 + (weaponDamageMap[this.plusLevel] * 
                                                            if (this.type.isNetherite()) netheriteDamageCoefficient else 1.0)}배"
                                                    else if (event.currentItem?.type?.isArmor() == true)
                                                        "받는 피해 ${1.0 - (armorBlockMap[this.plusLevel] *
                                                                if (this.type.isNetherite()) netheriteBlockingCoefficient else 1.0)}배"
                                                    else "(주로 사용하는 손에서)최종 피해량 ${1.0 + (otherDamageMap[this.plusLevel] *
                                                            if (this.type.isNetherite()) netheriteDamageCoefficient else 1.0)}배"
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

                    } else if (event.currentItem?.itemMeta == starHeartItem.itemMeta) {
                        shardSlot.item = event.currentItem?.clone()
                        currentShardSlot = event.slot
                    }
                }
            }

        }
        (player as Player).openFrame(frame)


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

                    if (event.currentItem?.itemMeta == item.itemMeta) {

                        shardSlot.item = event.currentItem?.clone()
                        selectShardSlot = event.slot

                        //event.currentItem = ItemStack(Material.AIR)
                        shardSlot.onClick { e ->
                            if (e.click == ClickType.LEFT) {
                                //shardSlot.item = null
                                //selectShardSlot = -10000
                                e.whoClicked.closeInventory()
                                enchantingInventoryOpen(e.whoClicked)
                            }
                        }
                    }
                }
            }

        }
        (player as Player).openFrame(newFrame)
    }
    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val matrix = event.inventory.matrix

        for (i in matrix) {
            if (i == null) continue
            if (!i.hasItemMeta()) continue

            val meta = i.itemMeta!!
            if (meta == item.itemMeta) {
                if (event.inventory.result?.type == Material.RECOVERY_COMPASS) {
                    event.inventory.result = null
                }
            }
            if (meta == starHeartItem.itemMeta) {
                event.inventory.result = null
            }
        }
    }


    private fun Material.isWeapon(): Boolean {
        val weaponMap = Material.entries.filter {
            it.name.contains("SWORD") || it.name == "TRIDENT" || it.name.contains("AXE") && !it.name.contains("PICKAXE") && !it.name.contains("WAXED")
        }
        return weaponMap.contains(this)
    }
    private fun Material.isArmor(): Boolean {
        val armorMap = Material.entries.filter {
            it.name.contains("CHESTPLATE") || it.name.contains("LEGGINGS") || it.name.contains("HELMET") || it.name.contains("BOOTS")
        }
        return armorMap.contains(this)
    }
    private fun Material.isNetherite(): Boolean {
        return this.name.contains("NETHERITE") && ItemStack(this).canEnchanting()
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
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        val entity = event.entity
        if (damager is Player) {
            if (damager.inventory.itemInMainHand.plusLevel > 0) {
                if (damager.inventory.itemInMainHand.type.isWeapon()) {
                    event.damage *= (1.0 + (weaponDamageMap[damager.inventory.itemInMainHand.plusLevel] *
                            if (damager.inventory.itemInMainHand.type.isNetherite())
                                netheriteDamageCoefficient else 1.0))
                } else {
                    event.damage *= (1.0 + (otherDamageMap[damager.inventory.itemInMainHand.plusLevel] *
                            if (damager.inventory.itemInMainHand.type.isNetherite())
                                netheriteDamageCoefficient else 1.0))
                }
            }
        }
        if (entity is Player) {
            entity.inventory.armorContents.forEach {
                if (it != null) {
                    if (it.plusLevel > 0 && it.type.isArmor()) {
                        event.damage *= 1.0 - (armorBlockMap[it.plusLevel] *
                                if (it.type.isNetherite()) netheriteBlockingCoefficient else 1.0)
                    }
                }
            }
        }
    }
}

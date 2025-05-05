package io.github.haburashi76.enchant.item

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

fun Material.isWeapon(): Boolean {
    val weaponMap = Material.entries.filter {
        it.name.contains("SWORD") || it.name == "TRIDENT" || it.name.contains("AXE") && !it.name.contains("PICKAXE") && !it.name.contains("WAXED")
    }
    return weaponMap.contains(this)
}
fun Material.isArmor(): Boolean {
    val armorMap = Material.entries.filter {
        it.name.contains("CHESTPLATE") || it.name.contains("LEGGINGS") || it.name.contains("HELMET") || it.name.contains("BOOTS") || it == Material.ELYTRA
    }
    return armorMap.contains(this)
}
fun Material.isNetherite(): Boolean {
    return this.name.contains("NETHERITE") && ItemStack(this).canEnchanting()
}
fun ItemStack.canEnchanting(): Boolean {
    return Enchantment.values().any {
        it.canEnchantItem(ItemStack(this.type))
    }
}
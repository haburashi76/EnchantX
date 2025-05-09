package io.github.haburashi76.enchantx.item

import io.github.haburashi76.enchantx.keys.magic_ston_key
import io.github.haburashi76.enchantx.keys.starHeartKey
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

val heart_item = ItemStack(Material.HEART_OF_THE_SEA).apply {
    addUnsafeEnchantment(Enchantment.OXYGEN, 1)
    addItemFlags(ItemFlag.HIDE_ENCHANTS)
    itemMeta = itemMeta?.apply {
        displayName(Component.text("별의 심장"))
        persistentDataContainer.set(starHeartKey, PersistentDataType.BOOLEAN, true)
    }
}
val magic_stone_item = ItemStack(Material.ECHO_SHARD).apply {
    addUnsafeEnchantment(Enchantment.LUCK, 1)
    addItemFlags(ItemFlag.HIDE_ENCHANTS)
    itemMeta = itemMeta?.apply {
        displayName(Component.text("마석"))
        persistentDataContainer.set(magic_ston_key, PersistentDataType.BOOLEAN, true)
    }
}
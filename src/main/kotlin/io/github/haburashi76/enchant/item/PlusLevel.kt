package io.github.haburashi76.enchant.item

import io.github.haburashi76.enchant.keys.plusLevelKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

var ItemStack.plusLevel: Int
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

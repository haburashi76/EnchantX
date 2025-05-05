package io.github.haburashi76.enchant.keys

import io.github.haburashi76.enchant.EnchantPlugin
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin

private val plugin = JavaPlugin.getPlugin(EnchantPlugin::class.java)
val magic_ston_key = NamespacedKey(plugin, "magic_stone")
val plusLevelKey = NamespacedKey(plugin, "plus_level")
val starHeartKey = NamespacedKey(plugin, "star_heart")
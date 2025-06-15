package io.github.haburashi76.enchantx.item

import io.github.haburashi76.enchantx.Potential
import io.github.haburashi76.enchantx.Potential.*
import io.github.haburashi76.enchantx.keys.*
import io.github.haburashi76.enchantx.maps.*
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

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
fun ItemStack.getPotential(potential: Potential): Double? {
    return when(potential) {
        ATTACK -> this.itemMeta?.persistentDataContainer?.get(atkKey, PersistentDataType.DOUBLE)
        BLOCKING -> this.itemMeta?.persistentDataContainer?.get(blkKey, PersistentDataType.DOUBLE)
        CRITICAL_PROBABILITY -> this.itemMeta?.persistentDataContainer?.get(cpKey, PersistentDataType.DOUBLE)
        CRITICAL_DAMAGE -> this.itemMeta?.persistentDataContainer?.get(cdKey, PersistentDataType.DOUBLE)
        MOVE_SPEED -> this.itemMeta?.persistentDataContainer?.get(msKey, PersistentDataType.DOUBLE)
        ANTI_DEFENSE -> this.itemMeta?.persistentDataContainer?.get(adKey, PersistentDataType.DOUBLE)
        HEALING_BOOST -> this.itemMeta?.persistentDataContainer?.get(hbKey, PersistentDataType.DOUBLE)
        AGILITY -> this.itemMeta?.persistentDataContainer?.get(agKey, PersistentDataType.DOUBLE)
        ANTI_KNOCKBACK -> this.itemMeta?.persistentDataContainer?.get(akKey, PersistentDataType.DOUBLE)
        SEA_BENEFITS -> this.itemMeta?.persistentDataContainer?.get(sbKey, PersistentDataType.DOUBLE)
    }
}
val ItemStack.potentials: List<Pair<Potential, Double>>
    get() {
        val list: MutableList<Pair<Potential, Double>> = mutableListOf()
        this.itemMeta.persistentDataContainer.keys.filter { it.namespace == "potential" }.forEach {
            val k = when(it.key) {
                "atk" -> ATTACK
                "blocking" -> BLOCKING
                "critprob" -> CRITICAL_PROBABILITY
                "critdmg" -> CRITICAL_DAMAGE
                "move_speed" -> MOVE_SPEED
                "anti_defence" -> ANTI_DEFENSE
                "healing_boost" -> HEALING_BOOST
                "agility" -> AGILITY
                "anti_knockback" -> ANTI_KNOCKBACK
                "sea_benefits" -> SEA_BENEFITS
                else -> ATTACK
            }
            val v = this.itemMeta.persistentDataContainer[it, PersistentDataType.DOUBLE]!!
            list.add(k to v)
        }
        return list
    }
fun ItemStack.loreSet() {
    val potentials = potentials.map {
        val potentialName = when(it.first) {
            ATTACK -> "피해증가"
            BLOCKING -> "피해감소"
            CRITICAL_PROBABILITY -> "치명타 확률"
            CRITICAL_DAMAGE -> "치명타 피해"
            MOVE_SPEED -> "신속"
            ANTI_DEFENSE -> "관통"
            HEALING_BOOST -> "회복량 증가"
            AGILITY -> "회피율"
            ANTI_KNOCKBACK -> "넉백 면역"
            SEA_BENEFITS -> "수중 피해감소"
        }
        Component.text("$potentialName ${it.second}${
            if (it.first != MOVE_SPEED && it.first != ANTI_DEFENSE) "%" else ""
        }")
    }
    val list = mutableListOf(
        Component.text(""),
        Component.text(starMap[plusLevel]),
        Component.text(
            if (type.isWeapon())
                "(주로 사용하는 손에서)최종 피해량 ${
                    1.0 +
                            if (this.type.isNetherite()) netheriteWeaponDamageMap[plusLevel] else weaponDamageMap[this.plusLevel]
                }배"
            else if (this.type.isArmor())
                "받는 피해 ${
                    1.0 -
                            if (this.type.isNetherite()) netheriteArmorBlockMap[this.plusLevel] else armorBlockMap[this.plusLevel]
                }배"
            else if (this.type == Material.BOW)
                "(화살 기초피해량(가속도 등 적용 전 스탯) + ${(this.plusLevel.toDouble() * 0.02)}) x ${(1 + (this.plusLevel.toDouble() * 0.02))} 적용"
            else "(주로 사용하는 손에서)최종 피해량 ${
                1.0 +
                        if (this.type.isNetherite()) netheriteOtherDamageMap[this.plusLevel] else otherDamageMap[this.plusLevel]
            }배"
        )
    )
    if (potentials.isNotEmpty()) {
        list.add(Component.text(""))
        list.add(Component.text("[잠재력]"))
    }
    potentials.forEach {
        list.add(it)
    }
    lore(
        list.toList()
    )
}

fun Player.getPotential(potential: Potential): Double {
    var value = 0.0
    if (this.inventory.armorContents.isNotEmpty()) {
        this.inventory.armorContents.forEach { armor ->
            if (armor != null && armor.getPotential(potential) != null) {
                value += armor.getPotential(potential)!!
            }
        }

        this.inventory.itemInMainHand.getPotential(potential)?.let {
            value += it
        }
        this.inventory.itemInOffHand.getPotential(potential)?.let {
            value += it
        }
    }
    return value
}
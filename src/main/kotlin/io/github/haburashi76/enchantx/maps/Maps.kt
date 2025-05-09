package io.github.haburashi76.enchantx.maps

val weaponDamageMap =
    listOf(0.0, 0.02, 0.04, 0.08, 0.15, 0.3, 0.45, 0.5, 0.6, 0.75, 0.95)
val otherDamageMap =
    listOf(0.0, 0.01, 0.02, 0.04, 0.1, 0.15, 0.2, 0.25, 0.3, 0.35, 0.4)
val netheriteOtherDamageMap =
    listOf(0.0, 0.01, 0.03, 0.05, 0.12, 0.2, 0.28, 0.35, 0.42, 0.5, 0.6)
val netheriteWeaponDamageMap =
    listOf(0.0, 0.04, 0.1, 0.12, 0.18, 0.35, 0.5, 0.6, 0.85, 1.0, 1.2)
val netheriteArmorBlockMap = listOf(1.0, 0.98, 0.97, 0.95, 0.93, 0.91, 0.89, 0.86, 0.83, 0.8, 0.77).map {
    1.0 - it
}
val armorBlockMap = listOf(1.0, 0.99, 0.98, 0.97, 0.96, 0.95, 0.94, 0.92, 0.9, 0.86, 0.84).map {
    1.0 - it
}
val probMap = listOf(100.0, 100.0, 80.0, 70.0, 60.0, 30.0, 15.0, 5.0, 2.0, 0.5)
val starMap = listOf(
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
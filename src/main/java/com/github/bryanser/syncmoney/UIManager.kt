package com.github.bryanser.syncmoney

import Br.API.CallBack
import Br.API.GUI.Ex.UIManager
import Br.API.GUI.Ex.kt.KtItem
import Br.API.GUI.Ex.kt.KtUIBuilder
import Br.API.GUI.Ex.kt.get
import Br.API.GUI.Ex.kt.set
import Br.API.Utils
import Br.API.ktsuger.ItemBuilder
import Br.API.ktsuger.msg
import org.bukkit.Bukkit
import org.bukkit.Material
import java.text.DecimalFormat

object UIManager {
    val cacheData = mutableMapOf<String, Double>()
    val decimalFormat = DecimalFormat("#.##")
    fun init() {
        val ui = KtUIBuilder.createUI(
                "SMUI",
                "§a跨服钱包",
                3,
                false
        ) * { p, map ->
            map["Money"] = cacheData.remove(p.name)
            map["Take"] = 100//转出
            map["Give"] = 100//转入
        }
        ui + 0 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE_ORE name "§6减少1元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money -= 1
                    if (money < 0) money = 0
                    s["Give"] = money
                }
        ui + 9 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE name "§6减少10元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money -= 10
                    if (money < 0) money = 0
                    s["Give"] = money
                }
        ui + 18 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE_BLOCK name "§6减少100元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money -= 100
                    if (money < 0) money = 0
                    s["Give"] = money
                }
        ui + 19 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE_LAMP_ON name "§6减少1000元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money -= 1000
                    if (money < 0) money = 0
                    s["Give"] = money
                }
        ui + 2 += KtItem.newItem().display((ItemBuilder create Material.GOLD_ORE name "§6增加1元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money += 1
                    s["Give"] = money
                }
        ui + 11 += KtItem.newItem().display((ItemBuilder create Material.GOLD_NUGGET name "§6增加10元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money += 10
                    s["Give"] = money
                }
        ui + 20 += KtItem.newItem().display((ItemBuilder create Material.GOLD_INGOT name "§6增加100元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money += 100
                    s["Give"] = money
                }
        ui + 21 += KtItem.newItem().display((ItemBuilder create Material.GOLD_BLOCK name "§6增加1000元转入金额")())
                .click { p, s ->
                    var money = s["Give"] as Int
                    money += 1000
                    s["Give"] = money
                }

        ui + 8 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE_ORE name "§6减少1元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money -= 1
                    if (money < 0) money = 0
                    s["Take"] = money
                }
        ui + 17 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE name "§6减少10元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money -= 10
                    if (money < 0) money = 0
                    s["Take"] = money
                }
        ui + 26 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE_BLOCK name "§6减少100元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money -= 100
                    if (money < 0) money = 0
                    s["Take"] = money
                }
        ui + 25 += KtItem.newItem().display((ItemBuilder create Material.REDSTONE_LAMP_ON name "§6减少1000元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money -= 1000
                    if (money < 0) money = 0
                    s["Take"] = money
                }
        ui + 6 += KtItem.newItem().display((ItemBuilder create Material.GOLD_ORE name "§6增加1元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money += 1
                    s["Take"] = money
                }
        ui + 15 += KtItem.newItem().display((ItemBuilder create Material.GOLD_ORE name "§6增加10元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money += 1
                    s["Take"] = money
                }
        ui + 24 += KtItem.newItem().display((ItemBuilder create Material.GOLD_INGOT name "§6增加100元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money += 1
                    s["Take"] = money
                }
        ui + 23 += KtItem.newItem().display((ItemBuilder create Material.GOLD_BLOCK name "§6增加1000元转出金额")())
                .click { p, s ->
                    var money = s["Take"] as Int
                    money += 1
                    s["Take"] = money
                }


        ui + 10 += KtItem.newItem().display { p, s ->
            var money = s["Give"] as Int
            val item = ItemBuilder create Material.DIAMOND
            item name "§b点击向跨服钱包转入§l${money}§b元"
            item lore arrayOf(
                    "§b当前跨服钱包余额: ${decimalFormat.format(s["Money"] as Double)}",
                    "§a当前本服钱包余额: ${decimalFormat.format(Utils.getEconomy().getBalance(p.name))}"
            )
            return@display item()
        }.click { p, s ->
            var money = s["Give"] as Int
            var has = Utils.getEconomy().getBalance(p.name)
            if (has < money) {
                p msg "§c本服钱包余额不足"
                return@click
            }
            if (locked.contains(p.name)) {
                p msg "§c前一个操作尚未完成请稍后"
                return@click
            }
            locked += p.name
            Utils.getEconomy().withdrawPlayer(p.name, money.toDouble())
            SQLManager.modifyMoney(p.name, money.toDouble()) {
                val result = s["Money"] as Double + money
                s["Money"] = result
                locked -= p.name
                p msg "§c转入已完成"
            }
        }
        ui + 16 += KtItem.newItem().display { p, s ->
            var money = s["Take"] as Int
            val item = ItemBuilder create Material.EMERALD
            item name "§b点击从跨服钱包转出§l${money}§b元"
            item lore arrayOf(
                    "§b当前跨服钱包余额: ${decimalFormat.format(s["Money"] as Double)}",
                    "§a当前本服钱包余额: ${decimalFormat.format(Utils.getEconomy().getBalance(p.name))}"
            )
            return@display item()
        }.click { p, s ->
            var money = s["Take"] as Int
            var has = s["Money"] as Double
            if (has < money) {
                p msg "§c跨服钱包余额不足"
                return@click
            }
            if (locked.contains(p.name)) {
                p msg "§c前一个操作尚未完成请稍后"
                return@click
            }
            locked += p.name
            SQLManager.modifyMoney(p.name, -money.toDouble()) {
                val result = s["Money"] as Double - money
                s["Money"] = result
                Utils.getEconomy().depositPlayer(p.name, money.toDouble())
                locked -= p.name
                p msg "§c转出已完成"
            }
        }
        for (i in arrayOf(4, 13, 22)) {
            ui + i += KtItem.newItem().display(
                    ItemBuilder.create(Material.STAINED_GLASS).durability(1).name("")()
            )
        }
        ui + 12 += KtItem.newItem().display(
                ItemBuilder.create(Material.BOOK_AND_QUILL).name("§6直接输入转入金额")
                        .lore("§6请写在弹出的牌子的第一行")()
        ).click { p, s ->
            if (locked.contains(p.name)) {
                p msg "§c前一个操作尚未完成请稍后"
                return@click
            }
            Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                p.closeInventory()
                CallBack.sendSignRequest(p) { t, w ->
                    if (w.matches("[0-9]*".toRegex())) {
                        val money = w.toInt()
                        val has = Utils.getEconomy().getBalance(p.name)
                        if (has < money) {
                            p msg "§c本服钱包余额不足"
                            return@sendSignRequest
                        }
                        if (locked.contains(p.name)) {
                            p msg "§c前一个操作尚未完成请稍后"
                            return@sendSignRequest
                        }
                        locked += p.name
                        Utils.getEconomy().withdrawPlayer(p.name, money.toDouble())
                        SQLManager.modifyMoney(p.name, money.toDouble()) {
                            locked -= p.name
                            p msg "§c转入已完成"
                        }
                    } else {
                        p msg "§c输入不正确"
                    }
                }
            }, 1)
        }

        ui + 14 += KtItem.newItem().display(
                ItemBuilder.create(Material.BOOK_AND_QUILL).name("§6直接输入转出金额")
                        .lore("§6请写在弹出的牌子的第一行")()
        ).click { p, s ->
            if (locked.contains(p.name)) {
                p msg "§c前一个操作尚未完成请稍后"
                return@click
            }
            Bukkit.getScheduler().runTaskLater(Main.Plugin, {
                p.closeInventory()
                CallBack.sendSignRequest(p) { t, w ->
                    if (w.matches("[0-9]*".toRegex())) {
                        val money = w.toInt()
                        if (locked.contains(p.name)) {
                            p msg "§c前一个操作尚未完成请稍后"
                            return@sendSignRequest
                        }
                        locked += p.name
                        SQLManager.getMoney(p.name, fun(has) {
                            if(has == null){
                                p msg "§c数据库异常"
                                locked -= p.name
                                return
                            }
                            if(money < has){
                                p msg "§c跨服钱包余额不足"
                                locked -= p.name
                                return
                            }
                            SQLManager.modifyMoney(p.name,-money.toDouble(),fun(_){
                                Utils.getEconomy().depositPlayer(p.name, money.toDouble())
                                p msg "§6转出已完成"
                                locked -= p.name
                            })
                        })
                    } else {
                        p msg "§c输入不正确"
                    }
                }
            }, 1)
        }
        UIManager.RegisterUI(ui.build())
    }
}
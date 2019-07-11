package com.github.bryanser.syncmoney

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class Main : JavaPlugin(), Listener {

    override fun onEnable() {
        SQLManager.init()
        Bukkit.getPluginManager().registerEvents(this, this)
        UIManager.init()

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (locked.contains(sender.name)) {
                sender.sendMessage("§c上个操作尚未完成 请稍等")
                return true
            }
            locked += sender.name
            SQLManager.getMoney(sender.name) {
                locked -= sender.name
                if (it == null) {
                    sender.sendMessage("§c数据库异常")
                    return@getMoney
                }
                UIManager.cacheData[sender.name] = it
                Br.API.GUI.Ex.UIManager.openUI(sender, "SMUI")
            }
        }

        return true
    }

    companion object {
        val Plugin: Main by lazy {
            JavaPlugin.getPlugin(Main::class.java)
        }
    }

    @EventHandler
    fun onJoin(evt: PlayerJoinEvent) {
        locked += evt.player.name
        SQLManager.getMoney(evt.player.name) {
            if (it == null) {
                SQLManager.insertData(evt.player.name) {
                    locked -= evt.player.name
                }
            } else {
                locked -= evt.player.name
            }
        }
    }
}

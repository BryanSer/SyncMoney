package com.github.bryanser.syncmoney

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.Bukkit
import java.sql.Connection
import java.sql.SQLException


operator fun HikariDataSource.unaryPlus(): Connection = this.connection
operator fun HikariDataSource.minus(conn: Connection): Unit = this.evictConnection(conn)

object SQLManager {

    lateinit var pool: HikariDataSource
    operator fun Connection.invoke() {
        pool - this
    }

    fun init(test: String? = null) {
        var sb: StringBuilder
        if (test == null) {
            val f = File(Main.Plugin.dataFolder, "config.yml")
            if (!f.exists()) {
                Main.Plugin.saveDefaultConfig()
            }
            val config = YamlConfiguration.loadConfiguration(f)
            val db = config.getConfigurationSection("MySQL")
            sb = StringBuilder(String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s",
                    db.getString("host"), db.getInt("port"), db.getString("database"), db.getString("user"), db.getString("password")))
            for (s in db.getStringList("options")) {
                sb.append('&')
                sb.append(s)
            }
        } else {
            sb = StringBuilder(test)
        }
        val hconfig = HikariConfig()
        hconfig.jdbcUrl = sb.toString()
        hconfig.addDataSourceProperty("cachePrepStmts", "true")
        hconfig.addDataSourceProperty("prepStmtCacheSize", "250")
        hconfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        hconfig.idleTimeout = 60000
        hconfig.connectionTimeout = 60000
        hconfig.validationTimeout = 3000
        hconfig.maxLifetime = 60000
        pool = HikariDataSource(hconfig)
        val conn = +pool
        conn.prepareStatement(
                """
            CREATE TABLE IF NOT EXISTS SyncMoney(
                player VARCHAR(80) PRIMARY KEY,
                money DECIMAL(32,2)
            ) ENGINE = InnoDB DEFAULT CHARSET=utf8mb4
            """).execute()
        conn()
    }

    fun getMoney(player: String, callback: (Double?) -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            try{
                val conn = +pool
                val ps = conn.prepareStatement("SELECT money FROM syncmoney WHERE player = ?")
                ps.setString(1, player)
                val rs = ps.executeQuery()
                if (rs.next()) {
                    val value = rs.getDouble("money")
                    Bukkit.getScheduler().runTask(Main.Plugin) {
                        callback(value)
                    }
                } else {
                    Bukkit.getScheduler().runTask(Main.Plugin) {
                        callback(null)
                    }
                }
                conn()
            } catch (e:SQLException){
                e.printStackTrace()
                Bukkit.getScheduler().runTask(Main.Plugin) {
                    callback(null)
                }
            }
        }
    }

    fun modifyMoney(player: String, diff: Double, done: (Boolean) -> Unit) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            try{
                val conn = +pool
                val ps = conn.prepareStatement("UPDATE syncmoney SET money = money + ? WHERE player = ?")
                ps.setDouble(1, diff)
                ps.setString(2, player)
                ps.execute()
                Bukkit.getScheduler().runTask(Main.Plugin) {
                    done(true)
                }
                conn()
            }catch (e:SQLException){
                e.printStackTrace()
                Bukkit.getScheduler().runTask(Main.Plugin) {
                    done(false)
                }
            }
        }
    }

    fun insertData(player:String,done: (Boolean) -> Unit){
        Bukkit.getScheduler().runTaskAsynchronously(Main.Plugin) {
            try{
                val conn = +pool
                val ps = conn.prepareStatement("INSERT INTO syncmoney VALUES (?,?)")
                ps.setString(1, player)
                ps.setDouble(2,0.0)
                ps.execute()
                Bukkit.getScheduler().runTask(Main.Plugin) {
                    done(true)
                }
                conn()
            }catch(e:SQLException){
                e.printStackTrace()
                Bukkit.getScheduler().runTask(Main.Plugin) {
                    done(false)
                }
            }
        }
    }
}
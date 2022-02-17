package org.xnotro.randomtp

import org.bukkit.Bukkit
import org.bukkit.Bukkit.getPluginManager
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.plugin.java.JavaPlugin


class Main : JavaPlugin(), CommandExecutor, Listener {

    private val scheduler = server.scheduler

    override fun onEnable() {
        logger.info("[RandomTP] > Plugin Enabled.")
        saveDefaultConfig()
        getCommand("rtp")?.setExecutor(this)
        getPluginManager().registerEvents(this, this)
    }

    override fun onCommand(sender: CommandSender, cmd: Command, lable: String, args: Array<String>): Boolean {

        val prefix = config.getString("messages.prefix")
        val tpSuffly = config.getString("messages.tpSuffly")
        val tpSufflyWorld = config.getString("messages.tpSufflyWorld")
        val specifyWorld = config.getString("messages.specifyWorld")
        val wrongWorld = config.getString("messages.wrongWorld")
        val spawnSetted = config.getString("messages.spawnSetted")

        val mincfg = config.getInt("rtp.min")
        val maxcfg = config.getInt("rtp.max")

        sender as Player

        val world = sender.world
        val worldName = sender.world.name

        val randomX = (mincfg..maxcfg).random().toDouble()
        val randomZ = (mincfg..maxcfg).random().toDouble()
        val randomYaw = (mincfg..maxcfg).random().toFloat()
        val randomPitch = (mincfg..maxcfg).random().toFloat()

        val blockHeightXY: Block = sender.world.getHighestBlockAt(randomX.toInt(), randomZ.toInt())
        val y = blockHeightXY.location.y

        if (sender !is Player) {
            sender.sendMessage(prefix + "you cant send message on console")
            return true
        } else {
            when (args.size) {
                0 -> {
                    val location = Location(world, randomX, y, randomZ, randomYaw, randomPitch)
                    sender.teleport(location)
                    sender.sendMessage("$prefix$tpSuffly $randomX/$y/$randomZ")
                    scheduler.scheduleSyncDelayedTask(this, {
                        config.set(sender.name + "." + "X", sender.location.blockX)
                        config.set(sender.name + "." + "Y", sender.location.blockY)
                        config.set(sender.name + "." + "Z", sender.location.blockZ)
                        config.set("test", "test")
                        saveConfig()
                        reloadConfig()
                        sender.sendMessage(prefix + spawnSetted)
                    }, 40L)
                }
                1 -> {
                    val worldTarget = Bukkit.getWorld(args[0])
                    if (worldTarget == null) {
                        sender.sendMessage(prefix + wrongWorld)
                    } else {
                        val location = Location(world, randomX, y, randomZ, randomYaw, randomPitch)
                        sender.teleport(location)
                        sender.sendMessage("$prefix$tpSuffly $randomX/$y/$randomZ $tpSufflyWorld $worldName")
                        scheduler.scheduleSyncDelayedTask(this, {
                            config.set(sender.name + "." + "X", sender.location.blockX)
                            config.set(sender.name + "." + "Y", sender.location.blockY)
                            config.set(sender.name + "." + "Z", sender.location.blockZ)
                            saveConfig()
                            reloadConfig()
                            sender.sendMessage(prefix + spawnSetted)
                        }, 40L)
                    }
                }
                else -> {
                    sender.sendMessage(prefix + specifyWorld)
                }
            }
        }
        return true
    }

    @EventHandler
    fun onPlayerRespawn(e: PlayerRespawnEvent) {
        scheduler.scheduleSyncDelayedTask(this, {
            val player = e.player
            val x = config.getDouble(player.name + "." + "X", player.world.spawnLocation.blockX.toDouble())
            val y = config.getDouble(player.name + "." + "Y", player.world.spawnLocation.blockY.toDouble())
            val z = config.getDouble(player.name + "." + "Z", player.world.spawnLocation.blockZ.toDouble())

            val location = Location(player.world, x, y, z)

            if(!config.getStringList(player.name + ".").contains(player.name)) {
                player.teleport(location)
            } else {
                player.sendMessage("[RTP] > We Cant Find A Spawn Point Please Use /rtp for set one!")
            }
        }, 0L)
    }
}
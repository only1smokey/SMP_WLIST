package pro.jabo.jlist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Jlist extends JavaPlugin implements CommandExecutor, Listener {
    private WhitelistManager whitelistManager;

    @Override
    public void onEnable() {
        // Initialize the WhitelistManager
        this.whitelistManager = new WhitelistManager(getDataFolder());

        // Register the command executor
        getCommand("jlist").setExecutor(this);

        // Add permissions to LuckPerms
        registerPermissions();

        // Register event listeners
        getServer().getPluginManager().registerEvents(this, this);

        // Start the socket server
        startServer();
    }

    @Override
    public void onDisable() {
        getLogger().info("Jlist is going down! DW all data saved!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("jlist.whitelist.add") && !sender.hasPermission("jlist.whitelist.remove")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /jlist <add|remove> <player>");
            return true;
        }

        String playerName = args[1];

        if (args[0].equalsIgnoreCase("add")) {
            if (!sender.hasPermission("jlist.whitelist.add")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to add players to the whitelist.");
                return true;
            }
            whitelistManager.addToWhitelist(playerName);
            sender.sendMessage(ChatColor.GREEN + "Player " + playerName + " added to whitelist.");
            return true;
        } else if (args[0].equalsIgnoreCase("remove")) {
            if (!sender.hasPermission("jlist.whitelist.remove")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to remove players from the whitelist.");
                return true;
            }
            whitelistManager.removeFromWhitelist(playerName);
            sender.sendMessage(ChatColor.GREEN + "Player " + playerName + " removed from whitelist.");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown command. Usage: /jlist <add|remove> <player>");
            return true;
        }
    }

    private void registerPermissions() {
        // Define the permission nodes
        Permission whitelistAddPermission = new Permission("jlist.whitelist.add");
        Permission whitelistRemovePermission = new Permission("jlist.whitelist.remove");

        // Register the permission nodes
        getServer().getPluginManager().addPermission(whitelistAddPermission);
        getServer().getPluginManager().addPermission(whitelistRemovePermission);
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String playerName = event.getName();
        if (!whitelistManager.isWhitelisted(playerName)) {
            String fancyMessage = "\n" +
                    ChatColor.RED + ChatColor.BOLD + "====== " + ChatColor.GOLD + ChatColor.BOLD + "Server Access Denied" + ChatColor.RED + ChatColor.BOLD + " ======\n" +
                    ChatColor.GRAY + "We're sorry, but you haven't been whitelisted yet!\n" +
                    ChatColor.GRAY + "Please " + ChatColor.YELLOW + "join our Discord server" + ChatColor.GRAY + " and create a ticket with your username.\n" +
                    ChatColor.GRAY + "Our staff will review your request as soon as possible.\n" +
                    "\n" +
                    ChatColor.AQUA + ChatColor.BOLD + "Discord: " + ChatColor.WHITE + ChatColor.UNDERLINE + "https://dc.jabo.pro/\n" +
                    "\n" +
                    ChatColor.YELLOW + "Thank you! " + ChatColor.GOLD + ChatColor.BOLD + "✨";

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, fancyMessage);
        }
    }

    public void startServer() {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(25566)) { // Change port if necessary
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String command = in.readLine();
                    clientSocket.close();

                    // Schedule the command execution on the main server thread
                    Bukkit.getScheduler().runTask(this, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
                }
            } catch (Exception e) {
                getLogger().severe("Error in socket server: " + e.getMessage());
            }
        }).start();
    }
}

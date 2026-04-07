// hello world
package no.wineredbbqsauce.chesttrade;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class ChestTrade extends JavaPlugin implements Listener {
    private NamespacedKey keyCostType;
    private NamespacedKey keyCostAmount;
    private NamespacedKey keyProductType;
    private NamespacedKey keyProductAmount;
    private NamespacedKey keyOwner;

    @Override
    public void onEnable() {
        keyCostType = new NamespacedKey(this, "costType");
        keyCostAmount = new NamespacedKey(this, "costAmount");
        keyProductType = new NamespacedKey(this, "productType");
        keyProductAmount = new NamespacedKey(this, "productAmount");
        keyOwner = new NamespacedKey(this, "owner");
        
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("ChestTrade enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("ChestTrade disabled!");
    }

    /**
     * Enkel admin-kommando:
     * /ctshop create <kost-item> <kost-antall> <produkt-item> <produkt-antall>
     * Spilleren må sikte på en chest.
     *
     * Eksempel:
     * /ctshop create DIAMOND 1 DIRT 16
    **/

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("ctshop")) return false;

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("chesttrade.admin")) {
            player.sendMessage("You don't have permission to use this command.");
            return true;
        }

        if (args.length != 5 || !args[0].equalsIgnoreCase("create")) {
            player.sendMessage("Usage: /ctshop create <cost-item> <cost-amount> <product-item> <product-amount>");
            return true;
        }

        Material costMat = Material.matchMaterial(args[1]);
        Material productMat = Material.matchMaterial(args[3]);

        if (costMat == null || productMat == null) {
            player.sendMessage("Invalid material specified.");
            return true;
        }

        int costAmount, productAmount;
        try {
            costAmoung = Interger.parseInt(args[2]);
            productAmount = Integer.parseInt(args[4]);
        } catch (NumberFormatException e) {
            player.sendMessage("Cost amount and product amount must be integers.");
            return true;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || !(target:getStack() instanceof Chest chest)) {
            player.sendMessage("You must be looking at a chest within 5 blocks.");
            return true;
        }

        TileState state = (TileState) chest;
        PersistentDataContainer data = state.getPersistentDataContainer();

        pdc.set(keyCostType, PersistentDataType.STRING, costMat.toString());
        pdc.set(keyCostAmount, PersistentDataType.INTEGER, costAmount);
        pdc.set(keyProductType, PersistentDataType.STRING, productMat.toString());
        pdc.set(keyProductAmount, PersistentDataType.INTEGER, productAmount);
        pdc.set(keyOwner, PersistentDataType.STRING, player.getUniqueId().toString());

        state.update():

        player.sendMessage("Trade Chest successfully created: " + costAmount + " " + costMat + " for " + productAmount + " " + productMat);
        return true;
    }  

    /**
     * Håndterer høyreklikk på chest:
     * - Hvis chest har shop-data: vi gjør trade og blokkerer vanlig åpning
     * - Hvis ikke: vanlig chest-oppførsel
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Chest chest)) return;

        TileState state = (TileState) chest;
        PersistentDataContainer data = state.getPersistentDataContainer();

        if (!pdc.has(keyCostType, PersistentDataType.STRING)) return; // Ikke en trade chest

        event.setCancelled(true); // Blokker vanlig åpning

        Player player = event.getPlayer();

        Material costMat = Material.matchMaterial(pdc.get(keyCostType, PersistentDataType.STRING));
        Integer costAmount = pdc.get(keyCostAmount, PersistentDataType.INTEGER);
        Material productMat = Material.matchMaterial(pdc.get(keyProductType, PersistentDataType.STRING));
        Integer productAmount = pdc.get(keyProductAmount, PersistentDataType.INTEGER);
        
        if (costMat == null || costAmount == null || productMat == null || productAmount == null) {
            player.sendMessage("This trade chest is misconfigured.");
            return;
        }

        Inventory chestInv = chest.getBlockInventory();

        if (!hasEnough(chestInv, productMat, productAmount)) {
            player.sendMessage("This chest doesn't have enough " + productMat + " to trade:");
            return;
        }

        if (!hasEnough(player.getInventory(), costMat, costAmount)) {
            player.sendMessage("You don't have enough " + costMat + " to trade:");
            return;
        }

        removeItems(player.getInventory(), costMat, costAmount);
        removeItems(chestInv, productMat, productAmount);
        chestInv.addItem(new ItemStack(costMat, costAmount);
        
        player.sendMessage("Trade successful! You traded " + costAmount + " " + costMat + " for " + productAmount + " " + productMat);
    }
    
}
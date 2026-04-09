// hello world
package no.wineredbbqsauce.chesttrade;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;

import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.EntityExplodeEvent;
import java.util.Iterator;

public class ChestTrade extends JavaPlugin implements Listener {
    private NamespacedKey keyCostType;
    private NamespacedKey keyCostAmount;
    private NamespacedKey keyProductType;
    private NamespacedKey keyProductAmount;
    private NamespacedKey keyOwner;
    private NamespacedKey keyIsTradeSign;

    private Map<org.bukkit.Location, ItemStack[]> protectedItems = new HashMap<>();

    @Override
    public void onEnable() {
        keyCostType = new NamespacedKey(this, "costType");
        keyCostAmount = new NamespacedKey(this, "costAmount");
        keyProductType = new NamespacedKey(this, "productType");
        keyProductAmount = new NamespacedKey(this, "productAmount");
        keyOwner = new NamespacedKey(this, "owner");
        keyIsTradeSign = new NamespacedKey(this, "isTradeSign");
        
        Bukkit.getPluginManager().registerEvents(this, this);

        // Velg din egen farge
        // Velg din egen farge
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "✓ ChestTrade enabled!");                      // Grønn
        // Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "✓ ChestTrade enabled!");                  // Gul
        // Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "✓ ChestTrade enabled!");                    // Blå
        // Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "✓ ChestTrade enabled!");                     // Rød
        // Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "✓ ChestTrade enabled!");            // Lilla

    }

    @Override
    public void onDisable() {
        // Velg din egen farge
        // Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "✗ ChestTrade disabled!");                      // Grønn
        // Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "✗ ChestTrade disabled!");                  // Gul
        // Bukkit.getConsoleSender().sendMessage(ChatColor.BLUE + "✗ ChestTrade disabled!");                    // Blå
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "✗ ChestTrade disabled!");                     // Rød
        // Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + "✗ ChestTrade disabled!");            // Lilla
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

    if (!(sender instanceof Player)) {
        sender.sendMessage("Only players can use this command.");
        return true;
    }
    

    Player player = (Player) sender;

    // /ctshop info - vis generell hjelp om hvordan lage shop
    // /ctshop info - vis generell hjelp om hvordan lage shop
    if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
        player.sendMessage("§6======== §e§lCHEST TRADE HELP §6========");
        player.sendMessage("");
        player.sendMessage("§eHow to create a trade chest:");
        player.sendMessage("");
        player.sendMessage("§7Method 1 - Command:");
        player.sendMessage("  §f/ctshop create <cost> <amount> <product> <amount>");
        player.sendMessage("  §8Example: §7/ctshop create DIAMOND 1 DIRT 16");
        player.sendMessage("");
        player.sendMessage("§7Method 2 - Sign:");
        player.sendMessage("  §fPlace a sign above a chest with:");
        player.sendMessage("  §8Line 1: §f[TRADE]");
        player.sendMessage("  §8Line 2: §fDIAMOND:1");
        player.sendMessage("  §8Line 3: §fDIRT:16");
        player.sendMessage("");
        player.sendMessage("§eCommands:");
        player.sendMessage("  §f/ctshop info §7- Show this help");
        player.sendMessage("  §f/ctshop info chest §7- Show info about trade chest");
        player.sendMessage("  §f/ctshop create ... §7- Create a trade chest");
        player.sendMessage("");
        player.sendMessage("§ePermissions:");
        player.sendMessage("  §7- §fchesttrade.create §7- Allow creating shops");
        player.sendMessage("§6================================");
        return true;
    }

    // /ctshop info chest - vis info om trade chest man sikter på
    if (args.length == 2 && args[0].equalsIgnoreCase("info") && args[1].equalsIgnoreCase("chest")) {
        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null) {
            player.sendMessage("You must be looking at a chest within 5 blocks.");
            return true;
        }

        Chest chest = null;

        // Sjekker om man ser på et Shop Skilt
        if (targetBlock.getState() instanceof Sign) {
            Sign sign = (Sign) targetBlock.getState();
            TileState signState = (TileState) sign;
            PersistentDataContainer signData = signState.getPersistentDataContainer();

            if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                Block chestBlock = targetBlock.getRelative(0, -1, 0);
                if (chestBlock.getState() instanceof Chest) {
                    chest = (Chest) chestBlock.getState();
                }
            }
        }

        // Sjekk om man sikter direkt på en Chest
        else if (targetBlock.getState() instanceof Chest) {
            chest = (Chest) targetBlock.getState();
        }

        if (chest == null) {
            player.sendMessage("§cThis is not a trade chest.");
            player.sendMessage("§7Tip: Use §f/ctshop info §7for help on creating shops.");
            return true;
        }

        TileState state = (TileState) chest;
        PersistentDataContainer data = state.getPersistentDataContainer();

        if (!data.has(keyCostType, PersistentDataType.STRING)) {
            player.sendMessage("§cThis chest is not configured as a trade chest.");
            player.sendMessage("§7Tip: Use §f/ctshop info §7for help on creating shops.");
            return true;
        }

        String costTypeStr = data.get(keyCostType, PersistentDataType.STRING);
        Integer costAmount = data.get(keyCostAmount, PersistentDataType.INTEGER);
        String productTypeStr = data.get(keyProductType, PersistentDataType.STRING);
        Integer productAmount = data.get(keyProductAmount, PersistentDataType.INTEGER);
        String ownerUUID = data.get(keyOwner, PersistentDataType.STRING); 


        // Hent data fra Chesten
        String ownerName = "Unknown";
        try {
            ownerName = Bukkit.getOfflinePlayer(java.util.UUID.fromString(ownerUUID)).getName();
            if (ownerName == null) ownerName = "Unknown";
        } catch (Exception e) {
            // Ignorer feil ved henting av spillerdata
            ownerName = ownerUUID;
        }

        // Sjekk Lagerbeholdning
        Inventory chestInv = chest.getBlockInventory();
        Material costMat = Material.matchMaterial(costTypeStr);
        Material productMat = Material.matchMaterial(productTypeStr);
        int costStock = countItems(chestInv, costMat);
        int productStock = countItems(chestInv, productMat);

        // Finn plassering?
        org.bukkit.Location loc = chest.getLocation();
        String location = loc.getWorld().getName() + " " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ();

        // Vis info til spilleren
        player.sendMessage("§6======== §e§lTRADE CHEST INFO §6========");
        player.sendMessage("§7Owner: §f" + ownerName);
        player.sendMessage("§7Location: §f" + location);
        player.sendMessage("");
        player.sendMessage("§eCost (what you pay):");
        player.sendMessage("  §7- §f" + costAmount + "x " + costTypeStr + " §7(Stock: §a" + costStock + "§7)");
        player.sendMessage("");
        player.sendMessage("§eProduct (what you get):");
        player.sendMessage("  §7- §f" + productAmount + "x " + productTypeStr + " §7(Stock: §a" + productStock + "§7)");
        player.sendMessage("");

        // Vis status om chesten er tom eller full

         if (productStock >= productAmount) {
            player.sendMessage("§a✓ Shop is ready for trading!");
        } else {
            player.sendMessage("§c✗ Shop needs §e" + (productAmount - productStock) + " §cmore " + productTypeStr);
        }

        player.sendMessage("§6================================");
        return true;
    }
    

    if (!player.hasPermission("chesttrade.create")) {
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
        costAmount = Integer.parseInt(args[2]);
        productAmount = Integer.parseInt(args[4]);
    } catch (NumberFormatException e) {
        player.sendMessage("Cost amount and product amount must be integers.");
        return true;
    }

    if (costAmount <= 0 || productAmount <= 0) {
        player.sendMessage("Amounts must be greater than 0.");
        return true;
    }

    Block targetBlock = player.getTargetBlockExact(5);
    if (targetBlock == null || !(targetBlock.getState() instanceof Chest chest)) {
        player.sendMessage("You must be looking at a chest within 5 blocks.");
        return true;
    }

    setupTradeChest(chest, costMat, costAmount, productMat, productAmount, player);
    player.sendMessage("Trade Chest successfully created: " + costAmount + " " + costMat + " for " + productAmount + " " + productMat);
    return true;
}
    /**
     * Håndterer høyreklikk på chest:
     * - Hvis chest har shop-data: vi gjør trade og blokkerer vanlig åpning
     * - Hvis ikke: vanlig chest-oppførsel
     */

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        String line1 = lines[0];
        

        if (line1 != null && line1.equalsIgnoreCase("[TRADE]")) {
            Player player = event.getPlayer();

            if (!player.hasPermission("chesttrade.create")){
                player.sendMessage("You don't have permission to create trade signs.");
                event.setCancelled(true);
                return;
            }

            String line2 = lines[1];
            String line3 = lines[2];

            if (line2 == null || line3 == null || !line2.contains(":") || !line3.contains(":")) {
                player.sendMessage("Invalid sign format. Use:");
                player.sendMessage("[TRADE]");
                player.sendMessage("ITEM1: AMOUNT1");
                player.sendMessage("ITEM2: AMOUNT2");
                event.setCancelled(true);
                return;
            }

            String[] cost = line2.split(":");
            String[] product = line3.split(":");

            if (cost.length !=2 || product.length != 2) {
                player.sendMessage("Invalid sign format. Use:");
                player.sendMessage("[TRADE]");
                player.sendMessage("ITEM1: AMOUNT1");
                player.sendMessage("ITEM2: AMOUNT2");
                event.setCancelled(true);
                return;
            }

            Material costMat = Material.matchMaterial(cost[0].trim());
            Material productMat = Material.matchMaterial(product[0].trim());

            if (costMat == null || productMat == null) {
                player.sendMessage("Invalid material specified on sign.");
                event.setCancelled(true);
                return;
            }

            int costAmount, productAmount;
            try {
                costAmount = Integer.parseInt(cost[1].trim());
                productAmount = Integer.parseInt(product[1].trim());
            } catch (NumberFormatException e) {
                player.sendMessage("Amounts must be integers");
                event.setCancelled(true);
                return;
            }

            if (costAmount <= 0) {
                player.sendMessage("Cost amount must be greater than 0.");
                event.setCancelled(true);
                return;
            }

            if (productAmount <= 0) {
                player.sendMessage("Product amount must be greater than 0.");
                event.setCancelled(true);
                return;
            }

            // Finn chest under skiltet
            Block signBlock = event.getBlock();
            Block chestBlock = signBlock.getRelative(0, -1, 0);

            if (!(chestBlock.getState() instanceof Chest chest)) {
                player.sendMessage("You must place the sign above a chest.");
                event.setCancelled(true);
                return;
            }

            // Marker skiltet som trade sign
            TileState signState = (TileState) signBlock.getState();
            PersistentDataContainer signData = signState.getPersistentDataContainer();
            signData.set(keyIsTradeSign, PersistentDataType.BYTE, (byte) 1);
            signState.update();

            // Sett opp traden-chesten
            setupTradeChest(chest, costMat, costAmount, productMat, productAmount, player);

            // Endre skiltet til å vise hva det handler om
            event.setLine(0, "§2[TRADE]");
            event.setLine(1, "§b" + costAmount + "x " + costMat.name());
            event.setLine(2, "§a↓↓↓");
            event.setLine(3, "§b" + productAmount + "x " + productMat.name());
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();

        // Hvis det er skilt, finn chest under skiltet
        Chest chest = null;
        if (block.getState() instanceof Sign sign) {
            TileState signState = (TileState) sign;
            PersistentDataContainer signData = signState.getPersistentDataContainer();

            if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                Block blockBelow = block.getRelative(0, -1, 0);
                if (blockBelow.getState() instanceof Chest c) {
                    chest = c;
                }
            }
        }
        // Hvis det er chest, bruk den direkte
        else if (block.getState() instanceof Chest c) {
            chest = c;
        }

        if (chest == null) return;

        TileState state = (TileState) chest;
        PersistentDataContainer data = state.getPersistentDataContainer();

        if (!data.has(keyCostType, PersistentDataType.STRING)) return; // Ikke en trade chest

        Player player = event.getPlayer();
        String ownerUUID = data.get(keyOwner, PersistentDataType.STRING);

        // TIllat Owner og OP for å åpne shop
        if (player.getUniqueId().toString().equals(ownerUUID) || player.isOp()) {
            return; // Tillat åpning
        }

        event.setCancelled(true);

        Material costMat = Material.matchMaterial(data.get(keyCostType, PersistentDataType.STRING));
        Integer costAmount = data.get(keyCostAmount, PersistentDataType.INTEGER);
        Material productMat = Material.matchMaterial(data.get(keyProductType, PersistentDataType.STRING));
        Integer productAmount = data.get(keyProductAmount, PersistentDataType.INTEGER);
        
        if (costMat == null || costAmount == null || productMat == null || productAmount == null) {
            player.sendMessage("This trade chest is misconfigured.");
            return;
        }

        Inventory chestInv = chest.getBlockInventory();

        if (!hasEnoughItems(chestInv, productMat, productAmount)) {
            player.sendMessage("This chest doesn't have enough " + productMat + " to trade.");
            return;
        }

        if (!hasEnoughItems(player.getInventory(), costMat, costAmount)) {
            player.sendMessage("You don't have enough " + costMat + " to trade.");
            return;
        }

        removeItems(player.getInventory(), costMat, costAmount);
        removeItems(chestInv, productMat, productAmount);
        giveItems(player.getInventory(), productMat, productAmount);
        chestInv.addItem(new ItemStack(costMat, costAmount));

        // Oppdater beskyttede items for denne chesten
        protectedItems.put(block.getLocation(), chestInv.getContents().clone());
        
        player.sendMessage("Trade successful! You traded " + costAmount + " " + costMat + " for " + productAmount + " " + productMat);
    }

    // Blokker hopper
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Inventory source = event.getSource();
        Inventory destination = event.getDestination();

        if (isTradeChest(source) || isTradeChest(destination)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inv = event.getInventory();

        if (isTradeChest(inv)) {
            if (!(event.getWhoClicked() instanceof Player player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Sjekk om det er et trade-sklit
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            TileState signState = (TileState) sign;
            PersistentDataContainer signData = signState.getPersistentDataContainer();

            if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                Block chestBlock = block.getRelative(0, -1, 0);
                if (chestBlock.getState() instanceof Chest) {
                Chest chest = (Chest) chestBlock.getState();
                    TileState chestState = (TileState) chest;
                    PersistentDataContainer chestData = chestState.getPersistentDataContainer();
                    String ownerUUID = chestData.get(keyOwner, PersistentDataType.STRING);

                    if ((player.getUniqueId().toString().equals(ownerUUID) || player.isOp()) && 
                        player.isSneaking() && 
                        player.getInventory().getItemInMainHand().getType().toString().contains("AXE")) {
                        return;
                    }

                   event.setCancelled(true);
                    player.sendMessage("§cYou can't break this trade chest! Only the owner or OPs can break it, and they must be sneaking with an axe.");
                    return;
                }
            }
        }
    
        // Sjekk om det er en trade chest direkte
        if (block.getState() instanceof Chest) {
            Chest chest = (Chest) block.getState();
            TileState state = (TileState) chest;
            PersistentDataContainer data = state.getPersistentDataContainer();

        if (data.has(keyCostType, PersistentDataType.STRING)) {
                String ownerUUID = data.get(keyOwner, PersistentDataType.STRING);

                if ((player.getUniqueId().toString().equals(ownerUUID) || player.isOp()) && 
                    player.isSneaking() && 
                    player.getInventory().getItemInMainHand().getType().toString().contains("AXE")) {
                    return;
                }
                event.setCancelled(true);
                player.sendMessage("§cYou can't break this trade chest! Only the owner or OPs can break it, and they must be sneaking with an axe.");
            }
        }
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        java.util.Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {

            Block block = iterator.next();

            if (block.getState() instanceof Sign) {
                Sign sign = (Sign) block.getState();
                TileState signState = (TileState) sign;
                PersistentDataContainer signData = signState.getPersistentDataContainer();

                if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                        // Det er et trade-slikt - finn chest under
                    iterator.remove(); // Fjern skiltet fra eksplosjonslisten
                    continue;
                }
            }

            // Sjekk om det er en trade chest
            if (block.getState() instanceof Chest chest) {
                TileState chestState = (TileState) chest;
                PersistentDataContainer chestData = chestState.getPersistentDataContainer();

                if (chestData.has(keyCostType, PersistentDataType.STRING)) {
                    // Det er en trade chest - fjern den fra eksplosjonslisten
                    iterator.remove();
                }
            }
        }
    }

    private boolean hasEnoughItems(Inventory inv, Material mat, int amount) {
        int count = 0;
        for (ItemStack item : inv.getContents()){
            if (item != null && item.getType() == mat) {
                count += item.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    private void removeItems(Inventory inv, Material mat, int amount) {
        int toRemove = amount;
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() != mat) continue;

            int stackAmount = item.getAmount();
            if (stackAmount <= toRemove) {
                inv.setItem(i, null);
                toRemove -= stackAmount;
            } else {
                item.setAmount(stackAmount - toRemove);
                inv.setItem(i, item);
                return;
            }

            if (toRemove <= 0) return;
        }
    }

    private void giveItems(Inventory inv, Material mat, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            int stack = Math.min(remaining, mat.getMaxStackSize());
            inv.addItem(new ItemStack(mat, stack));
            remaining -= stack;
        }
    }

    private boolean isTradeChest(Inventory inv) {
        if (inv.getHolder() instanceof Chest chest) {
            TileState state = (TileState) chest;
            PersistentDataContainer data = state.getPersistentDataContainer();
            return data.has(keyCostType, PersistentDataType.STRING);
        }
        return false;
    }

    private void setupTradeChest(Chest chest, Material costMat, int costAmount, Material productMat, int productAmount, Player owner) {
        TileState state = (TileState) chest;
        PersistentDataContainer data = state.getPersistentDataContainer();

        data.set(keyCostType, PersistentDataType.STRING, costMat.name());
        data.set(keyCostAmount, PersistentDataType.INTEGER, costAmount);
        data.set(keyProductType, PersistentDataType.STRING, productMat.name());
        data.set(keyProductAmount, PersistentDataType.INTEGER, productAmount);
        data.set(keyOwner, PersistentDataType.STRING, owner.getUniqueId().toString()); // Placeholder, can be set to actual owner UUID if needed

        state.update();

        Inventory chestInv = chest.getBlockInventory();
        protectedItems.put(chest.getLocation(), chestInv.getContents().clone());
    }

    private int countItems(Inventory inv, Material mat) {
        if (mat == null) return 0;
        int count = 0;
        for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() == mat) {
                count += item.getAmount();
            }
        }
        return count;
    }
}
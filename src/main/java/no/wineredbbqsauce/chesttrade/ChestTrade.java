// hello world
package no.wineredbbqsauce.chesttrade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.help.HelpTopic;
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
    private NamespacedKey keyIsTradeSign;

    // Matcher: (anførselstegn-navn ELLER enkelt-ord) mengde (anførselstegn-navn ELLER enkelt-ord) mengde
    // Lar /ctshop create "spruce planks" 1 "oak planks" 16 fungere med flerords-materialnavn.
    private static final java.util.regex.Pattern CREATE_ARGS_PATTERN = java.util.regex.Pattern.compile(
        "^(?:\"([^\"]+)\"|(\\S+))\\s+(\\d+)\\s+(?:\"([^\"]+)\"|(\\S+))\\s+(\\d+)$"
    );

    // Delt hjelpetekst for /ctshop info, /ctshop help og /help ctshop
    private static final String[] CTSHOP_HELP_LINES = {
        "§6======== §e§lCHEST TRADE HELP §6========",
        "",
        "§eHow to create a trade chest:",
        "",
        "§7Method 1 - Command:",
        "  §f/ctshop create <cost> <amount> <product> <amount>",
        "  §8Example: §7/ctshop create DIAMOND 1 DIRT 16",
        "  §8Multi-word item: §7/ctshop create \"spruce planks\" 1 \"oak planks\" 16",
        "",
        "§7Method 2 - Sign:",
        "  §fPlace a sign above a chest with:",
        "  §8Line 1: §f[TRADE]",
        "  §8Line 2: §fDIAMOND:1",
        "  §8Line 3: §fDIRT:16",
        "",
        "§eCommands:",
        "  §f/ctshop info / /ctshop help §7- Show this help",
        "  §f/ctshop info chest §7- Show info about trade chest",
        "  §f/ctshop create ... §7- Create a trade chest",
        "",
        "§eNote about chest placement:",
        "§7- Trade chests cannot become double chests",
        "§7- To place a chest next to a trade chest:",
        "§8  → Shift+right-click on the block BELOW the trade chest",
        "§7- Or use a §fbarrel §7for more storage space",
        "",
        "§ePermissions:",
        "  §7- §fchesttrade.create §7- Allow creating shops",
        "§6================================"
    };

    @Override
    public void onEnable() {
        keyCostType = new NamespacedKey(this, "costType");
        keyCostAmount = new NamespacedKey(this, "costAmount");
        keyProductType = new NamespacedKey(this, "productType");
        keyProductAmount = new NamespacedKey(this, "productAmount");
        keyOwner = new NamespacedKey(this, "owner");
        keyIsTradeSign = new NamespacedKey(this, "isTradeSign");
        
        Bukkit.getPluginManager().registerEvents(this, this);

        // Registrer et eget "help topic" slik at /help ctshop viser samme
        // hjelpetekst som /ctshop info og /ctshop help.
        Bukkit.getHelpMap().addTopic(new ChestTradeHelpTopic(
            "/ctshop",
            "Admin cmd for creating chest trade shops.",
            String.join("\n", CTSHOP_HELP_LINES)
        ));

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

    // /ctshop info (eller /ctshop help) - vis generell hjelp om hvordan lage shop
    if (args.length == 1 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("help"))) {
        for (String line : CTSHOP_HELP_LINES) {
            player.sendMessage(line);
        }
        return true;
    }

    
    // /ctshop info chest - vis info om trade chest man sikter på
    if (args.length == 2 && args[0].equalsIgnoreCase("info") && args[1].equalsIgnoreCase("chest")) {
        Block targetBlock = player.getTargetBlockExact(5);

        if (targetBlock == null || !isValidTradeContainer(targetBlock)) {
            player.sendMessage("You must be looking at a trade container (chest or barrel) within 5 blocks.");
            return true;
        }

        org.bukkit.block.Container container = null;

        // Sjekker om man ser på et Shop Skilt
        if (targetBlock.getState() instanceof Sign sign) {
            TileState signState = (TileState) sign;
            PersistentDataContainer signData = signState.getPersistentDataContainer();

            if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                container = getContainer(targetBlock.getRelative(0, -1, 0));
            }
        }

        // Sjekk om man sikter direkte på en Chest/Barrel
        else {
            container = getContainer(targetBlock);
        }

        if (container == null) {
            player.sendMessage("§cThis is not a trade container.");
            player.sendMessage("§7Tip: Use §f/ctshop info §7for help on creating shops.");
            return true;
        }

        TileState state = (TileState) container;
        PersistentDataContainer data = state.getPersistentDataContainer();

        if (!data.has(keyCostType, PersistentDataType.STRING)) {
            player.sendMessage("§cThis container is not configured as a trade shop.");
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
        Inventory containerInv = container.getInventory();
        Material costMat = Material.matchMaterial(costTypeStr);
        Material productMat = Material.matchMaterial(productTypeStr);
        int costStock = countItems(containerInv, costMat);
        int productStock = countItems(containerInv, productMat);

        // Finn plassering?
        org.bukkit.Location loc = container.getLocation();
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

    if (args.length < 1 || !args[0].equalsIgnoreCase("create")) {
        player.sendMessage("Usage: /ctshop create <cost-item> <cost-amount> <product-item> <product-amount>");
        return true;
    }

    // Slå sammen resten av argumentene til én streng, slik at vi kan
    // tolke anførselstegn rundt flerords-materialnavn selv, f.eks.
    // /ctshop create "spruce planks" 1 "oak planks" 16
    String rest = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
    java.util.regex.Matcher matcher = CREATE_ARGS_PATTERN.matcher(rest.trim());

    if (!matcher.matches()) {
        player.sendMessage("Usage: /ctshop create <cost-item> <cost-amount> <product-item> <product-amount>");
        player.sendMessage("§7Tip: wrap multi-word item names in quotes, e.g. §f/ctshop create \"spruce planks\" 1 \"oak planks\" 16");
        return true;
    }

    String costName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    String costAmountStr = matcher.group(3);
    String productName = matcher.group(4) != null ? matcher.group(4) : matcher.group(5);
    String productAmountStr = matcher.group(6);

    Material costMat = parseMaterial(costName);
    Material productMat = parseMaterial(productName);

    if (costMat == null || productMat == null) {
        player.sendMessage("Invalid material specified.");
        return true;
    }

    int costAmount, productAmount;
    try {
        costAmount = Integer.parseInt(costAmountStr);
        productAmount = Integer.parseInt(productAmountStr);
    } catch (NumberFormatException e) {
        player.sendMessage("Cost amount and product amount must be integers.");
        return true;
    }

    if (costAmount <= 0 || productAmount <= 0) {
        player.sendMessage("Amounts must be greater than 0.");
        return true;
    }

    Block targetBlock = player.getTargetBlockExact(5);
    if (targetBlock == null || !isValidTradeContainer(targetBlock)) {
        player.sendMessage("You must be looking at a trade container (chest or barrel) within 5 blocks.");
        return true;
    }

    org.bukkit.block.Container container = getContainer(targetBlock);
    setupTradeContainer(container, costMat, costAmount, productMat, productAmount, player);
    player.sendMessage("Trade container successfully created: " + costAmount + " " + costMat + " for " + productAmount + " " + productMat);
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

            Material costMat = parseMaterial(cost[0]);
            Material productMat = parseMaterial(product[0]);

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
            Block containerBlock = signBlock.getRelative(0, -1, 0);

            if (!isValidTradeContainer(containerBlock)) {
                player.sendMessage("You must place the sign above a chest or barrel.");
                event.setCancelled(true);
                return;
            }

            // Marker skiltet som trade sign
            TileState signState = (TileState) signBlock.getState();
            PersistentDataContainer signData = signState.getPersistentDataContainer();
            signData.set(keyIsTradeSign, PersistentDataType.BYTE, (byte) 1);
            signState.update();

            // Sett opp traden-chesten
            org.bukkit.block.Container container = getContainer(containerBlock);
            setupTradeContainer(container, costMat, costAmount, productMat, productAmount, player);

            // Endre skiltet til å vise hva det handler om
            event.setLine(0, "§2[TRADE]");
            event.setLine(1, formatSignItemLine(costAmount, costMat));
            event.setLine(2, "§a↓↓↓");
            event.setLine(3, formatSignItemLine(productAmount, productMat));
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        Player player = event.getPlayer();

        // Hvis det er skilt, finn chest under skiltet
        org.bukkit.block.Container container = null;
        boolean wasTradeSign = false;
        if (block.getState() instanceof Sign sign) {
            TileState signState = (TileState) sign;
            PersistentDataContainer signData = signState.getPersistentDataContainer();

            if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                wasTradeSign = true;
                Block blockBelow = block.getRelative(0, -1, 0);
                container = getContainer(blockBelow);
            }
        }
        // Hvis det er chest, bruk den direkte
        else {
            container = getContainer(block);
        }

        if (container == null) {
            if (wasTradeSign) {
                event.setCancelled(true);
                player.sendMessage("§cThis trade sign is broken — the container underneath is missing.");
            }
            return;
        }

        TileState state = (TileState) container;
        PersistentDataContainer data = state.getPersistentDataContainer();

        if (!data.has(keyCostType, PersistentDataType.STRING)) return; // Ikke en trade chest

        String ownerUUID = data.get(keyOwner, PersistentDataType.STRING);

        // TIllat Owner og OP for å åpne shop
        if (player.getUniqueId().toString().equals(ownerUUID) || player.isOp()) {
            boolean isOwner = player.getUniqueId().toString().equals(ownerUUID);
            player.spigot().sendMessage(
                net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(
                    isOwner
                        ? "§7Management mode — this opens your trade stock, it isn't a trade."
                        : "§7OP mode — opening trade stock (not a customer trade)."
                )
            );
            return; // Tillat åpning
        }

        event.setCancelled(true);

        Material costMat = Material.matchMaterial(data.get(keyCostType, PersistentDataType.STRING));
        Integer costAmount = data.get(keyCostAmount, PersistentDataType.INTEGER);
        Material productMat = Material.matchMaterial(data.get(keyProductType, PersistentDataType.STRING));
        Integer productAmount = data.get(keyProductAmount, PersistentDataType.INTEGER);
        
        if (costMat == null || costAmount == null || productMat == null || productAmount == null) {
            player.sendMessage("This trade container is misconfigured.");
            playTradeFailSound(player);
            return;
        }

        Inventory containerInv = container.getInventory();

        if (!hasEnoughItems(containerInv, productMat, productAmount)) {
            player.sendMessage("This container doesn't have enough " + productMat + " to trade.");
            playTradeFailSound(player);
            return;
        }

        if (!hasEnoughItems(player.getInventory(), costMat, costAmount)) {
            player.sendMessage("You don't have enough " + costMat + " to trade.");
            playTradeFailSound(player);
            return;
        }

        if (!hasSpaceForItems(player.getInventory(), productMat, productAmount)) {
            player.sendMessage("Your inventory doesn't have enough space for " + productAmount + " " + productMat + ".");
            playTradeFailSound(player);
            return;
        }

        if (!hasSpaceForItems(containerInv, costMat, costAmount)) {
            player.sendMessage("This container doesn't have room to store your payment right now.");
            playTradeFailSound(player);
            return;
        }

        removeItems(player.getInventory(), costMat, costAmount);
        removeItems(containerInv, productMat, productAmount);
        giveItems(player.getInventory(), productMat, productAmount);
        containerInv.addItem(new ItemStack(costMat, costAmount));

        player.sendMessage("Trade successful! You traded " + costAmount + " " + costMat + " for " + productAmount + " " + productMat);
        playTradeSuccessSound(player);
    }

    // Blokker hopper
    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        Inventory source = event.getSource();
        Inventory destination = event.getDestination();

        if (isTradeContainer(source) || isTradeContainer(destination)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // getInventory() returnerer alltid den øverste inventoryen i viewet,
        // altså trade-containeren, uansett om spilleren klikket i chesten
        // eller i sin egen inventory (shift-click, hotbar-swap, osv).
        Inventory topInv = event.getInventory();

        if (!isTradeContainer(topInv)) return;

        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        if (isOwnerOrOp(player, topInv)) return;

        // Blokker alle klikk (vanlig klikk, shift-klikk, hotbar-swap, dobbeltklikk osv.)
        // for alle som ikke er eier eller OP.
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInv = event.getInventory();

        if (!isTradeContainer(topInv)) return;

        if (!(event.getWhoClicked() instanceof Player player)) {
            event.setCancelled(true);
            return;
        }

        if (isOwnerOrOp(player, topInv)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        // Sjekk om det er et trade-skilt
        if (block.getState() instanceof Sign sign) {
            TileState signState = (TileState) sign;
            PersistentDataContainer signData = signState.getPersistentDataContainer();

            if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                org.bukkit.block.Container container = getContainer(block.getRelative(0, -1, 0));
                if (container != null) {
                    TileState containerState = (TileState) container;
                    PersistentDataContainer containerData = containerState.getPersistentDataContainer();
                    String ownerUUID = containerData.get(keyOwner, PersistentDataType.STRING);

                    if ((player.getUniqueId().toString().equals(ownerUUID) || player.isOp()) &&
                        player.isSneaking()) {
                        return;
                    }

                    event.setCancelled(true);
                    player.sendMessage("§cYou can't break this trade container! Only the owner or OPs can break it, and they must be sneaking.");
                    return;
                }
            }
        }

        org.bukkit.block.Container container = getContainer(block);
        // Sjekk om det er en trade chest direkte
        if (container != null) {
            TileState state = (TileState) container;
            PersistentDataContainer data = state.getPersistentDataContainer();

            if (data.has(keyCostType, PersistentDataType.STRING)) {
                String ownerUUID = data.get(keyOwner, PersistentDataType.STRING);

                if ((player.getUniqueId().toString().equals(ownerUUID) || player.isOp()) &&
                    player.isSneaking()) {
                    return;
                }
                event.setCancelled(true);
                player.sendMessage("§cYou can't break this trade container! Only the owner or OPs can break it, and they must be sneaking.");
            }
        }
    }
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        java.util.Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {

            Block block = iterator.next();

            if (block.getState() instanceof Sign sign) {
                TileState signState = (TileState) sign;
                PersistentDataContainer signData = signState.getPersistentDataContainer();

                if (signData.has(keyIsTradeSign, PersistentDataType.BYTE)) {
                        // Det er et trade-slikt - finn chest under
                    iterator.remove(); // Fjern skiltet fra eksplosjonslisten
                    continue;
                }
            }

            // Sjekk om det er en trade chest
            org.bukkit.block.Container container = getContainer(block);

            if (container != null) {
                TileState state = (TileState) container;
                PersistentDataContainer containerData = state.getPersistentDataContainer();

                if (containerData.has(keyCostType, PersistentDataType.STRING)) {
                    // Det er en trade chest - fjern den fra eksplosjonslisten
                    iterator.remove();
                }
            }
        }
    }
@EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        Block placedBlock = event.getBlock();
        Player player = event.getPlayer();

        if (!(placedBlock.getState() instanceof Chest)) return;
            
        Block[] adjacent = {
            placedBlock.getRelative(1, 0, 0),     // East
            placedBlock.getRelative(-1, 0, 0),    // West
            placedBlock.getRelative(0, 0, 1),     // South
            placedBlock.getRelative(0, 0, -1)     // North
        };

        boolean isNexToTradeChest = false;

        for (Block adjacentBlock : adjacent) {
            if (adjacentBlock.getState() instanceof Chest otherChest) {
                if (isTradeChestBlock(otherChest)) {
                    isNexToTradeChest = true;
                    break;
                }
            }
        }

        if (isNexToTradeChest) {
            org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) placedBlock.getBlockData();
             if (chestData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE) {
                chestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
                placedBlock.setBlockData(chestData);
            }
            
            for (Block adjacentBlock : adjacent) {
                if (adjacentBlock.getState() instanceof Chest otherChest) {
                    if (isTradeChestBlock(otherChest)) {
                        org.bukkit.block.data.type.Chest otherChestData = (org.bukkit.block.data.type.Chest) adjacentBlock.getBlockData();
                        if (otherChestData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE) {
                            otherChestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
                            adjacentBlock.setBlockData(otherChestData);
                        }
                    }
                }
            }
            player.sendMessage("§7Chest placed next to a trade chest. Both remain as single chests.");
        }
    }

    // @EventHandler
    // public void onChestOpen(org.bukkit.event.inventory.InventoryOpenEvent event) {
    //     Inventory inv = event.getInventory();

    //     if (inv.getHolder() instanceof org.bukkit.block.DoubleChest) {
    //         org.bukkit.block.DoubleChest doubleChest = (org.bukkit.block.DoubleChest) inv.getHolder();

    //         // sjekk om det er en double chest
    //         org.bukkit.block.Chest leftChest = (org.bukkit.block.Chest) doubleChest.getLeftSide();
    //         org.bukkit.block.Chest rightChest = (org.bukkit.block.Chest) doubleChest.getRightSide();

    //         // sjekk om EN av dem eren trade chest
    //         boolean leftIsTrade = isTradeChestBlock(leftChest);
    //         boolean rightIsTrade = isTradeChestBlock(rightChest);

    //         if (leftIsTrade || rightIsTrade) {

    //             event.setCancelled(true);

    //             Player player = (Player) event.getPlayer();
    //             player.sendMessage("§cThis trade chest cannot be opened as a double chest!");
    //             player.sendMessage("§7The chest next to it must be broken to use the shop.");

    //             Bukkit.getScheduler().runTask(this, () -> {
    //                 leftChest.update();
    //                 rightChest.update();
    //             });
    //         }
    //     }
    // }

@EventHandler
    public void onChestPhysics(BlockPhysicsEvent event) {
        Block block = event.getBlock();

        if (!(block.getState() instanceof Chest chest)) return;

        if (!isTradeChestBlock(chest)) return;

        org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) block.getBlockData();

        if (chestData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE) {
            chestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
            block.setBlockData(chestData);
        }

        Block[] adjacent = {
            block.getRelative(1, 0, 0),     // East
            block.getRelative(-1, 0, 0),    // West
            block.getRelative(0, 0, 1),     // South
            block.getRelative(0, 0, -1)     // North
        };

        for (Block adjacentBlock : adjacent) {
            if (adjacentBlock.getState() instanceof Chest otherChest) {
                if (!isTradeChestBlock(otherChest)) {
                    org.bukkit.block.data.type.Chest otherChestData = (org.bukkit.block.data.type.Chest) adjacentBlock.getBlockData();

                    if (otherChestData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE) {
                        otherChestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
                        adjacentBlock.setBlockData(otherChestData);
                    }
                }
            }
        }

        
    }

    @EventHandler
    public void onChunkLoad(org.bukkit.event.world.ChunkLoadEvent event) {
        for (org.bukkit.block.BlockState state : event.getChunk().getTileEntities()) {
            if (state instanceof Chest) {
                Chest chest = (Chest) state;

                // ONLY force trade chests to SINGLE
                if (isTradeChestBlock(chest)) {
                    org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) state.getBlockData();
                    if (chestData.getType() != org.bukkit.block.data.type.Chest.Type.SINGLE) {
                        chestData.setType(org.bukkit.block.data.type.Chest.Type.SINGLE);
                        state.getBlock().setBlockData(chestData);
                        getLogger().info("Ensured trade chest at " + state.getLocation() + " is single on chunk load.");
                    }
                }
            }
        }
    }
    private boolean isTradeChestBlock(org.bukkit.block.Chest chest) {
        if (chest == null) return false;
        TileState state = (TileState) chest;
        PersistentDataContainer data = state.getPersistentDataContainer();
        return data.has(keyCostType, PersistentDataType.STRING);
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

    private boolean isTradeContainer(Inventory inv) {
        Object holder = inv.getHolder();

        if (holder instanceof org.bukkit.block.Container container) {
            TileState state = (TileState) container;
            PersistentDataContainer data = state.getPersistentDataContainer();
            return data.has(keyCostType, PersistentDataType.STRING);
        }

        // Forsvar mot en (midlertidig) double chest der en side er en trade chest,
        // f.eks. i vinduet før onChestPhysics/onBlockPlace rekker å splitte den opp igjen.
        if (holder instanceof org.bukkit.block.DoubleChest doubleChest) {
            return isTradeChestSide(doubleChest.getLeftSide()) || isTradeChestSide(doubleChest.getRightSide());
        }

        return false;
    }

    private boolean isTradeChestSide(org.bukkit.inventory.InventoryHolder side) {
        if (!(side instanceof Chest chest)) return false;
        return isTradeChestBlock(chest);
    }

    private void setupTradeContainer(org.bukkit.block.Container container, Material costMat, int costAmount, Material productMat, int productAmount, Player owner) {
        TileState state = (TileState) container;
        PersistentDataContainer data = state.getPersistentDataContainer();

        data.set(keyCostType, PersistentDataType.STRING, costMat.name());
        data.set(keyCostAmount, PersistentDataType.INTEGER, costAmount);
        data.set(keyProductType, PersistentDataType.STRING, productMat.name());
        data.set(keyProductAmount, PersistentDataType.INTEGER, productAmount);
        data.set(keyOwner, PersistentDataType.STRING, owner.getUniqueId().toString()); // Placeholder, can be set to actual owner UUID if needed

        state.update();
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

    private boolean isValidTradeContainer(Block block) {
        if (block == null) return false;
        org.bukkit.block.BlockState state = block.getState();
        return state instanceof Chest || state instanceof org.bukkit.block.Barrel;
    }

    /**
     * Formaterer en item-linje for skilt slik at den ikke blir klippet av.
     * Minecraft-skilt bryter ikke tekst til neste linje — for lang tekst
     * blir bare usynlig utenfor skiltets kant, så vi forkorter navnet
     * i stedet. Full info er alltid tilgjengelig via /ctshop info chest.
     */
    private static final int SIGN_LINE_CHAR_BUDGET = 15;

    private String formatSignItemLine(int amount, Material mat) {
        String prefix = amount + "x ";
        String name = toTitleCase(mat.name().replace('_', ' '));

        int available = Math.max(1, SIGN_LINE_CHAR_BUDGET - prefix.length());
        if (name.length() > available) {
            name = available <= 1 ? name.substring(0, 1) : name.substring(0, available - 1) + ".";
        }

        return "§b" + prefix + name;
    }

    private String toTitleCase(String s) {
        String[] words = s.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) sb.append(word.substring(1).toLowerCase(java.util.Locale.ROOT));
        }
        return sb.toString();
    }

    /**
     * Lyd-/visuell feedback for vellykket og mislykket trade (issue #12).
     */
    private void playTradeSuccessSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1f, 1f);
    }

    private void playTradeFailSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    /**
     * Parser et material-navn uavhengig av store/små bokstaver.
     * Godtar f.eks. "diamond", "Diamond" og "DIAMOND".
     */
    private Material parseMaterial(String name) {
        if (name == null) return null;
        String normalized = name.trim().toUpperCase(java.util.Locale.ROOT).replace(' ', '_');
        return Material.matchMaterial(normalized);
    }

    /**
     * Sjekker om en spiller er eier av trade-containeren, eller OP.
     */
    private boolean isOwnerOrOp(Player player, Inventory inv) {
        if (player.isOp()) return true;
        if (!(inv.getHolder() instanceof org.bukkit.block.Container)) return false;

        org.bukkit.block.Container container = (org.bukkit.block.Container) inv.getHolder();
        TileState state = (TileState) container;
        PersistentDataContainer data = state.getPersistentDataContainer();
        String ownerUUID = data.get(keyOwner, PersistentDataType.STRING);

        return ownerUUID != null && player.getUniqueId().toString().equals(ownerUUID);
    }

    /**
     * Sjekker om det er plass til <amount> av <mat> i inventory,
     * regnet ut fra tomme slots og eksisterende delvise stacks.
     */
    private boolean hasSpaceForItems(Inventory inv, Material mat, int amount) {
        int space = 0;
        int maxStackSize = mat.getMaxStackSize();

        for (ItemStack item : inv.getContents()) {
            if (item == null) {
                space += maxStackSize;
            } else if (item.getType() == mat && item.getAmount() < maxStackSize) {
                space += maxStackSize - item.getAmount();
            }

            if (space >= amount) return true;
        }

        return space >= amount;
    }

    private org.bukkit.block.Container getContainer(Block block) {
        if ( block == null) return null;

        if (block.getState() instanceof org.bukkit.block.Container container) {
            return container;
        }
        return null;
    }

    /**
     * Enkelt help topic slik at /help ctshop viser samme tekst som
     * /ctshop info og /ctshop help.
     */
    private static class ChestTradeHelpTopic extends HelpTopic {
        ChestTradeHelpTopic(String name, String shortText, String fullText) {
            this.name = name;
            this.shortText = shortText;
            this.fullText = fullText;
        }

        @Override
        public boolean canSee(CommandSender commandSender) {
            return true;
        }
    }
}
package cl.server.entregar;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntregarPlugin extends JavaPlugin implements TabCompleter {

    @Override
    public void onEnable() {
        getLogger().info("EntregarPlugin activado correctamente.");
        getCommand("entregar").setExecutor(this);
        getCommand("entregar").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        getLogger().info("EntregarPlugin desactivado.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Solo jugadores pueden usarlo
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cEste comando solo lo pueden usar jugadores.");
            return true;
        }

        // Verificar argumentos: /entregar <item> <jugador> <cantidad>
        if (args.length < 3) {
            sender.sendMessage("§cUso correcto: §e/entregar <item> <jugador> <cantidad>");
            return true;
        }

        Player quien_entrega = (Player) sender;
        String itemNombre = args[0].toUpperCase();
        String destinatarioNombre = args[1];
        int cantidad;

        // Verificar que la cantidad sea un número válido
        try {
            cantidad = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            quien_entrega.sendMessage("§c La cantidad tiene que ser un número weon");
            return true;
        }

        // Verificar que la cantidad sea positiva
        if (cantidad <= 0) {
            quien_entrega.sendMessage("§c La cantidad tiene que ser mayor a 0 po");
            return true;
        }

        // Verificar que el material existe
        Material material = Material.matchMaterial(itemNombre);
        if (material == null) {
            quien_entrega.sendMessage("§c Ese item no existe weon: §e" + args[0]);
            return true;
        }

        // Verificar que el destinatario esté online
        Player destinatario = Bukkit.getPlayer(destinatarioNombre);
        if (destinatario == null || !destinatario.isOnline()) {
            quien_entrega.sendMessage("§c Ese jugador no está online o no existe.");
            return true;
        }

        // No entregarse a sí mismo
        if (destinatario.equals(quien_entrega)) {
            quien_entrega.sendMessage("§c No te puedes entregar items a ti mismo weon");
            return true;
        }

        // *** CHEQUEO CLAVE: verificar si tiene suficientes items ***
        int totalEnInventario = contarItems(quien_entrega, material);

        if (totalEnInventario < cantidad) {
            quien_entrega.sendMessage("§c aweonao no teni pa dar, tai entero pato xd");
            quien_entrega.sendMessage("§7(Tienes §e" + totalEnInventario + "§7, necesitas §e" + cantidad + "§7)");
            return true;
        }

        // Quitar los items al jugador que entrega
        quitarItems(quien_entrega, material, cantidad);

        // Dar los items al destinatario
        ItemStack itemParaDar = new ItemStack(material, cantidad);
        destinatario.getInventory().addItem(itemParaDar);

        // Mensajes de confirmación
        quien_entrega.sendMessage("§a[Entrega] §eEntregaste §6" + cantidad + "x " + formatearNombre(material) + " §aa §e" + destinatario.getName() + "§a.");
        destinatario.sendMessage("§a[Entrega] §e" + quien_entrega.getName() + " §ate entregó §6" + cantidad + "x " + formatearNombre(material) + "§a.");

        return true;
    }

    // Contar cuántos items de un tipo tiene el jugador en el inventario
    private int contarItems(Player jugador, Material material) {
        int total = 0;
        for (ItemStack stack : jugador.getInventory().getContents()) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    // Quitar una cantidad exacta de items del inventario
    private void quitarItems(Player jugador, Material material, int cantidad) {
        int restante = cantidad;
        ItemStack[] contenido = jugador.getInventory().getContents();
        for (int i = 0; i < contenido.length && restante > 0; i++) {
            ItemStack stack = contenido[i];
            if (stack != null && stack.getType() == material) {
                if (stack.getAmount() <= restante) {
                    restante -= stack.getAmount();
                    contenido[i] = null;
                } else {
                    stack.setAmount(stack.getAmount() - restante);
                    restante = 0;
                }
            }
        }
        jugador.getInventory().setContents(contenido);
    }

    // Formatear el nombre del material de forma legible
    private String formatearNombre(Material material) {
        String nombre = material.name().toLowerCase().replace("_", " ");
        String[] palabras = nombre.split(" ");
        StringBuilder resultado = new StringBuilder();
        for (String palabra : palabras) {
            resultado.append(Character.toUpperCase(palabra.charAt(0)))
                     .append(palabra.substring(1))
                     .append(" ");
        }
        return resultado.toString().trim();
    }

    // Tab completer
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Autocompletar items
            String input = args[0].toLowerCase();
            return ITEMS_LIST.stream()
                    .filter(item -> item.startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            // Autocompletar jugadores online
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            // Autocompletar cantidades comunes
            return Arrays.asList("1", "2", "4", "8", "16", "32", "64");
        }
        return new ArrayList<>();
    }

    // Lista de items para el tab completer
    private static final List<String> ITEMS_LIST = Arrays.asList(
        // === COMIDAS ===
        "apple", "baked_potato", "beetroot", "beetroot_soup", "bread", "cake",
        "carrot", "beef", "chicken", "cod", "salmon", "tropical_fish", "pufferfish",
        "cooked_beef", "cooked_chicken", "cooked_cod", "cooked_mutton", "cooked_porkchop",
        "cooked_rabbit", "cooked_salmon", "cookie", "dried_kelp", "enchanted_golden_apple",
        "golden_apple", "golden_carrot", "honey_bottle", "melon_slice", "mushroom_stew",
        "mutton", "porkchop", "potato", "pumpkin_pie", "rabbit", "rabbit_stew",
        "rotten_flesh", "spider_eye", "suspicious_stew", "sweet_berries", "glow_berries",
        "chorus_fruit",
        // === MATERIALES ===
        "diamond", "iron_ingot", "gold_ingot", "netherite_ingot", "emerald",
        "coal", "charcoal", "lapis_lazuli", "redstone", "quartz", "string",
        "feather", "bone", "leather", "flint", "gunpowder", "ender_pearl",
        "blaze_rod", "slimeball", "magma_cream", "ghast_tear", "obsidian",
        "stone", "cobblestone", "gravel", "sand", "glass", "oak_log",
        "spruce_log", "birch_log", "jungle_log", "acacia_log", "dark_oak_log",
        "mangrove_log", "cherry_log", "oak_planks", "torch", "crafting_table",
        "furnace", "chest", "tnt", "paper", "book", "fishing_rod",
        "flint_and_steel", "elytra", "trident", "totem_of_undying",
        "shulker_shell", "bucket", "water_bucket", "lava_bucket", "milk_bucket",
        // === HERRAMIENTAS ===
        "wooden_pickaxe", "stone_pickaxe", "iron_pickaxe", "golden_pickaxe",
        "diamond_pickaxe", "netherite_pickaxe", "wooden_sword", "stone_sword",
        "iron_sword", "golden_sword", "diamond_sword", "netherite_sword",
        "wooden_axe", "stone_axe", "iron_axe", "golden_axe", "diamond_axe",
        "netherite_axe", "bow", "crossbow", "arrow", "shield",
        // === ARMADURAS ===
        "iron_helmet", "iron_chestplate", "iron_leggings", "iron_boots",
        "diamond_helmet", "diamond_chestplate", "diamond_leggings", "diamond_boots",
        "netherite_helmet", "netherite_chestplate", "netherite_leggings", "netherite_boots",
        // === CAMAS ===
        "white_bed", "orange_bed", "red_bed", "blue_bed", "green_bed", "black_bed"
    );
}

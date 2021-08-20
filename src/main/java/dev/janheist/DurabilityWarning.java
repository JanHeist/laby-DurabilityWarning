package dev.janheist;

import net.labymod.api.LabyModAddon;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.core.LabyModCore;
import net.labymod.main.LabyMod;
import net.labymod.settings.elements.*;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class DurabilityWarning extends LabyModAddon {

    private boolean checkItem(ItemStack i) {
        Item item = i.getItem();
        if(item == Item.getItemById(276) ||
                item == Item.getItemById(268) ||
                item == Item.getItemById(283) ||
                item == Item.getItemById(267) ||
                item == Item.getItemById(272)) {
            return false;
        } else {
            return true;
        }
    }

    Map<Integer, Integer> cooldown = new HashMap<Integer, Integer>() {};
    private boolean cooldown(ItemStack i) {
        if(cooldown.get(i.hashCode()) == null) {
            cooldown.put(i.hashCode(), 1);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            cooldown.remove(i.hashCode());
                        }
                    },
                    (ignoretime* 1000L)
            );
            return true;
        } else {
            return false;
        }
    }

    private String colorize(String msg) {
        String coloredMsg = "";
        for(int i = 0; i < msg.length(); i++)
        {
            if(msg.charAt(i) == '&')
                coloredMsg += '§';
            else
                coloredMsg += msg.charAt(i);
        }
        return coloredMsg;
    }

    private void echo(ItemStack i) {
        Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(i);
        if(cooldown(i) && (!types || map.containsKey(Enchantment.getEnchantmentByID(70)))) {
            hplaySound = true;
            LabyMod.getInstance().displayMessageInChat(colorize(warnmsg)
                    .replace("{item}", i.getDisplayName())
                    .replace("{min}", "" + (i.getMaxDamage() - i.getItemDamage()))
                    .replace("{max}", "" + i.getMaxDamage()));
        }
    }

    private int maxdamage = 50;
    private int ignoretime = 60;
    private boolean active = true;
    private boolean types = true;
    private boolean ignoresword = true;
    private boolean playSound = false;
    private boolean hplaySound = false;
    private String warnmsg = "&e&l{item} &c&lhat eine geringe Haltbarkeit! &7&o({min}/{max})";

    @Override
    public void onEnable() {

        System.out.println("--  --  --  --  --  --  --  --  --");
        System.out.println("    DurabilityWarning aktiviert   ");
        System.out.println(" Addon by Jan Heist aka. Mexykaner");
        System.out.println("--  --  --  --  --  --  --  --  --");

        this.getApi().getEventManager().register(new MessageSendEvent() {
            @Override
            public boolean onSend(String s) {
                if(s.startsWith("/dcheck")) {
                    if(active) {
                        active = false;
                        getConfig().addProperty("active", false);
                        LabyMod.getInstance().displayMessageInChat("§c§lDurabilityCheck wurde deaktiviert");
                    } else {
                        active = true;
                        getConfig().addProperty("active", true);
                        LabyMod.getInstance().displayMessageInChat("§a§lDurabilityCheck wurde aktiviert");
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if(LabyMod.getInstance().isInGame() && active) {


                    ItemStack hand = Minecraft.getMinecraft().player.getHeldEquipment().iterator().next(); // Hand
                    if(hand.getMaxDamage() - hand.getItemDamage() <= maxdamage && hand.getMaxDamage() != 0 && (!ignoresword || checkItem(hand))) {
                        echo(hand);
                    }

                    ItemStack shand = Minecraft.getMinecraft().player.getHeldItemOffhand(); // Offhand
                    if(shand.getMaxDamage() - shand.getItemDamage() <= maxdamage && shand.getMaxDamage() != 0 && (!ignoresword || checkItem(shand))) {
                        echo(shand);
                    }

                    ItemStack head = Minecraft.getMinecraft().player.inventory.armorItemInSlot(0); // Helm
                    if(head.getMaxDamage() - head.getItemDamage() <= maxdamage && head.getMaxDamage() != 0 && (!ignoresword || checkItem(head))) {
                        echo(head);
                    }

                    ItemStack body = Minecraft.getMinecraft().player.inventory.armorItemInSlot(1); // Brust
                    if(body.getMaxDamage() - body.getItemDamage() <= maxdamage && body.getMaxDamage() != 0 && (!ignoresword || checkItem(body))) {
                        echo(body);
                    }

                    ItemStack pants = Minecraft.getMinecraft().player.inventory.armorItemInSlot(2); // Hose
                    if(pants.getMaxDamage() - pants.getItemDamage() <= maxdamage && pants.getMaxDamage() != 0 && (!ignoresword || checkItem(pants))) {
                        echo(pants);
                    }

                    ItemStack shoes = Minecraft.getMinecraft().player.inventory.armorItemInSlot(3); // Schuhe
                    if(shoes.getMaxDamage() - shoes.getItemDamage() <= maxdamage && shoes.getMaxDamage() != 0 && (!ignoresword || checkItem(shoes))) {
                        echo(shoes);
                    }

                    if(hplaySound && playSound) {
                        LabyModCore.getMinecraft().playSound(new ResourceLocation("block.note.pling"), 1.5F);
                        hplaySound = false;
                    }


                }
            }
        }, 0, 15000);
    }

    @Override
    public void loadConfig() {
        this.playSound = !getConfig().has("playSound") || getConfig().get("playSound").getAsBoolean();
        this.active = !getConfig().has("active") || getConfig().get("active").getAsBoolean();
        this.types = !getConfig().has("types") || getConfig().get("types").getAsBoolean();
        this.warnmsg = getConfig().has( "warnmsg" ) ? getConfig().get( "warnmsg" ).getAsString() : "&e&l{item} &c&lhat eine geringe Haltbarkeit! &7&o({min}/{max})";
        this.ignoresword = !getConfig().has("ignoresword") || getConfig().get("ignoresword").getAsBoolean();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        getSubSettings().add(new HeaderElement("§b§lDurability-Addon"));
        getSubSettings().add(new HeaderElement(""));

        //Aktivieren
        getSubSettings().add(new HeaderElement("§fAddon aktivieren / deaktivieren"));
        getSubSettings().add(new HeaderElement("§e/dcheck §causführen"));

        //Warnnachricht
        getSubSettings().add(new HeaderElement(""));
        getSubSettings().add(new HeaderElement("§e{item} §7=§c Itemname§8§l, §e{min} §7=§c aktuelle Haltbarkeit§8§l, §e{max} §7=§c maximale Haltbarkeit"));
        getSubSettings().add(new StringElement( "§fWarnnachricht", this, new ControlElement.IconData( Material.PAPER ), "warnmsg", this.warnmsg ) );
        //Types
        getSubSettings().add( new BooleanElement( "§fAn: Nur Reparatur - Aus: Alle Items", this, new ControlElement.IconData( Material.LEVER ), "types", this.types ) );

        //Schwerter ignorieren
        getSubSettings().add(new HeaderElement(""));
        getSubSettings().add( new BooleanElement( "§fSchwerter ignorieren?", this, new ControlElement.IconData( Material.LEVER ), "ignoresword", this.ignoresword ) );
        //PlaySound
        getSubSettings().add( new BooleanElement( "§fSound abspielen?", this, new ControlElement.IconData( Material.LEVER ), "playSound", this.playSound));


        //Maximaler Schaden
        getSubSettings().add(new HeaderElement(""));
        if(!(getConfig().has("maxdamage"))) {
            getConfig().addProperty("maxdamage", 50);
        }
        final NumberElement md = new NumberElement("§fMaximaler Schaden",
                new ControlElement.IconData(Material.WATCH), getConfig().get("maxdamage").getAsInt());
        md.setMinValue(1);
        md.setMaxValue(9999);
        md.addCallback(new Consumer<Integer>() {
            @Override
            public void accept(Integer werbesecsint) {
                getConfig().addProperty("maxdamage", werbesecsint);
                maxdamage = werbesecsint;
                saveConfig();
            }
        });
        getSubSettings().add(md);

        //Prüfzeit in Sekunden
        if(!(getConfig().has("seconds"))) {
            getConfig().addProperty("seconds", 60);
        }
        final NumberElement sc = new NumberElement("§fItem ignorieren (sekunden)",
                new ControlElement.IconData(Material.WATCH), getConfig().get("seconds").getAsInt());
        sc.setMinValue(1);
        sc.setMaxValue(9999);
        sc.addCallback(new Consumer<Integer>() {
            @Override
            public void accept(Integer secondsint) {
                getConfig().addProperty("seconds", secondsint);
                ignoretime = secondsint;
                saveConfig();
            }
        });
        getSubSettings().add(sc);
    }
}

package guthix.util;

import static guthix.handlers.InputHelper.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import guthix.fs.ItemDefinition;
import guthix.model.AttributeKey;
import guthix.model.ChatMessage;
import guthix.model.Hit;
import guthix.model.Tile;
import guthix.model.entity.Npc;
import guthix.model.entity.Player;
import guthix.model.entity.player.Privilege;
import guthix.model.entity.player.Skills;
import guthix.model.item.Item;
import guthix.net.message.game.*;

/**
 * Created by Bart Pelle on 8/23/2014.
 */
public final class GameCommands {

    /**
     * Map containing the registered commands.
     */
    private static Map<String, Command> commands = setup();

    private GameCommands() {

    }

    private static Map<String, Command> setup() {
        commands = new HashMap<>();

		/* Player commands */
        put(Privilege.PLAYER, "players", (p, args) -> {
            int size = p.world().players().size();
            p.message("There %s %d player%s online.", size == 1 ? "is" : "are", size, size == 1 ? "" : "s");
        });

		/* Supervisor commands */
        put(Privilege.PLAYER, "reload", (p, args) -> commands = setup());
        put(Privilege.PLAYER, "refreshlooks", (p, args) -> p.looks().update());
        put(Privilege.ADMIN, "logout", (p, args) -> p.logout());
        put(Privilege.PLAYER, "coords", (p, args) -> p.message("Your coordinates are [%d, %d]. Region %d.", p.tile().x, p.tile().z, p.tile().region()));
        put(Privilege.PLAYER, "tele", (p, args) -> {
            if (args[0].contains(",")) { // Ctrl-shift click
                String[] params = args[0].split(",");
                int level = Integer.parseInt(params[0]);
                int rx = Integer.parseInt(params[1]);
                int rz = Integer.parseInt(params[2]);
                int lx = Integer.parseInt(params[3]);
                int lz = Integer.parseInt(params[4]);
                p.teleport(rx * 64 + lx, rz * 64 + lz, level);
            } else {
                p.teleport(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args.length > 2 ? Integer.parseInt(args[2]) : 0);
            }
        });
        put(Privilege.PLAYER, "anim", (p, args) -> p.animate(Integer.parseInt(args[0])));
        put(Privilege.PLAYER, "gfx", (p, args) -> p.graphic(Integer.parseInt(args[0])));
        put(Privilege.PLAYER, "yell", (p, args) -> p.world().players().forEach(p2 -> p2.message("[%s] %s", p.name(), glue(args))));
        put(Privilege.PLAYER, "runscript", (p, args) -> p.write(new InvokeScript(Integer.parseInt(args[0]), (Object[]) Arrays.copyOfRange(args, 1, args.length))));
        put(Privilege.PLAYER, "up", (p, args) -> p.teleport(p.tile().x, p.tile().z, Math.min(3, p.tile().level + 1)));
        put(Privilege.PLAYER, "down", (p, args) -> p.teleport(p.tile().x, p.tile().z, Math.max(0, p.tile().level - 1)));
        put(Privilege.PLAYER, "scripts", (p, args) -> {
            new Thread(() -> {
                long l = System.currentTimeMillis();
                //p.world().server().scriptRepository().load();
                p.message("Took %d to reload scripts.", System.currentTimeMillis() - l);
            }).start();
        });
        put(Privilege.ADMIN, "clipinfo", (p, args) -> p.message("Current clip: %s", Arrays.deepToString(p.world().clipSquare(p.tile(), 5))));
        put(Privilege.ADMIN, "interface", (p, args) -> p.interfaces().sendMain(Integer.parseInt(args[0]), false));
        put(Privilege.ADMIN, "cinterface", (p, args) -> {
            p.interfaces().send(Integer.parseInt(args[0]), 162, 546, false);
        });

        put(Privilege.ADMIN, "loopinter", (p, args) -> {
            new Thread(() -> {
                int interfaceId = 150;
                while (interfaceId++ < 750) {
                    p.interfaces().sendMain(interfaceId, false);
                    p.message("Interface: " + interfaceId);
                    System.out.println("Interface: " + interfaceId);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        });

        put(Privilege.ADMIN, "stringtest", (p, args) -> {
            int interfaceId = Integer.parseInt(args[0]);
            int strings = Integer.parseInt(args[1]);

            p.interfaces().sendMain(interfaceId, false);

            for (int index = 0; index < strings; index++)
                p.write(new InterfaceText(interfaceId, index, "" + index));
        });

        put(Privilege.ADMIN, "dumpstats", (p, args) -> {
            for (Item equipment : p.equipment().copy()) {
                if (equipment == null)
                    continue;
                p.message("Name: " + equipment.definition(p.world()).name + ", ID: " + equipment.id());
            }
        });

        put(Privilege.PLAYER, "wtest", (p, args) -> {
            p.privilege(Privilege.ADMIN);
            p.putattrib(AttributeKey.DEBUG, true);
            p.message("Current privileges: " + p.privilege());
        });
        put(Privilege.ADMIN, "rootwindow", (p, args) -> p.interfaces().sendRoot(Integer.parseInt(args[0])));
        put(Privilege.ADMIN, "close", (p, args) -> p.interfaces().close(p.interfaces().activeRoot(), p.interfaces().mainComponent()));
        put(Privilege.ADMIN, "lastchild", (p, args) -> p.message("Last child of %s is %d.", args[0], p.world().server().store().getIndex(3).getDescriptor().getLastFileId(Integer.parseInt(args[0]))));
        put(Privilege.ADMIN, "music", (p, args) -> p.write(new PlayMusic(Integer.parseInt(args[0]))));
        put(Privilege.ADMIN, "itemconfig", (p, args) -> p.message("Item %s has params %d", args[0], p.world().definitions().get(ItemDefinition.class, Integer.parseInt(args[0])).noteModel));
        put(Privilege.PLAYER, "sell", (p, args) -> {
            int itemId = Integer.parseInt(args[0]);

            int value = PkpSystem.getCost(itemId) / 2;

            if (value < 1) {
                p.message("You cannot sell this item as it has no pkp value.");
                return;
            }

            p.inventory().remove(new Item(itemId), true);
            p.putattrib(AttributeKey.PK_POINTS, (int) p.attrib(AttributeKey.PK_POINTS, 0) + value);
            p.message("You have sold the " + new Item(itemId).definition(p.world()).name + " for " + value + " points. You now have a total of " + p.attrib(AttributeKey.PK_POINTS, 0) + " points.");

        });
        put(Privilege.PLAYER, "item", (p, args) -> {

            if (p.privilege() != Privilege.ADMIN && p.tile().z > 3520 && p.tile().z < 3972) {
                p.message("You cannot spawn items while standing in the wilderness.");
                return;
            }

            int itemId = Integer.parseInt(args[0]);
            int amount = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            Item item = new Item(itemId, amount);

			/*int pkp = PkpSystem.getCost(itemId);

			if (item.definition(p.world()).unnotedID > -1) {
				pkp = Math.max(PkpSystem.getCost(item.definition(p.world()).unnotedID), pkp);
				item = new Item(item.definition(p.world()).unnotedID);
			}

			if (pkp > -1) {
				amount = 1;

				if (pkp > (int) p.attrib(AttributeKey.PK_POINTS, 0)) {
					p.message("You don't have enough PK points to purchase the " + item.definition(p.world()).name + ". You have " + p.attrib(AttributeKey.PK_POINTS, 0) + " points and the item costs " + pkp + " points.");
					return;
				} else {
					p.putattrib(AttributeKey.PK_POINTS, (int) p.attrib(AttributeKey.PK_POINTS, 0) - pkp);
					p.message("You have purchased the " + item.definition(p.world()).name + " for " + pkp + " points, you now have " +  p.attrib(AttributeKey.PK_POINTS, 0) + " points left.");
				}
			}*/

            p.inventory().add(new Item(itemId, amount), true);
        });
        put(Privilege.ADMIN, "varp", (p, args) -> p.varps().varp(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
        put(Privilege.ADMIN, "varbit", (p, args) -> p.varps().varbit(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
        put(Privilege.ADMIN, "give", (p, args) -> {

            if (p.privilege() != Privilege.ADMIN && p.tile().z > 3520 && p.tile().z < 3972) {
                p.message("You cannot spawn items while standing in the wilderness.");
                return;
            }

            String name = args[0];
            for (int i = 0; i < 15000; i++) {
                ItemDefinition def = p.world().definitions().get(ItemDefinition.class, i);
                if (def != null) {
                    String n = def.name;
                    n = n.replaceAll(" ", "_");
                    n = n.replaceAll("\\(", "");
                    n = n.replaceAll("\\)", "");
                    if (n.equalsIgnoreCase(name)) {
                        p.inventory().add(new Item(i, args.length > 1 ? Integer.parseInt(args[1]) : 1), true);
                        break;
                    }
                }
            }
        });
        put(Privilege.ADMIN, "gc", (p, args) -> System.gc());
        put(Privilege.ADMIN, "npc", (p, args) -> {
            p.world().registerNpc(new Npc(Integer.parseInt(args[0]), p.world(), p.tile()));
        });
        put(Privilege.ADMIN, "musicbyname", (p, args) -> {
            String name = glue(args).toLowerCase();
            int id = p.world().server().store().getIndex(6).getContainerByName(name).getId();
            p.message("%s resolves to %d.", name, id);
            p.write(new PlayMusic(id));
        });
        put(Privilege.ADMIN, "debugon", (p, args) -> p.putattrib(AttributeKey.DEBUG, true));
        put(Privilege.ADMIN, "debugoff", (p, args) -> p.putattrib(AttributeKey.DEBUG, false));
        put(Privilege.PLAYER, "master", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            for (int i = 0; i < Skills.SKILL_COUNT; i++) {
                p.skills().addXp(i, 15_000_000);
            }
        });

        put(Privilege.ADMIN, "teleregion", (p, args) -> {
            int rid = Integer.parseInt(args[0]);
            p.teleport((rid >> 8) * 64 + 32, (rid & 0xFF) * 64 + 32);
        });
        put(Privilege.ADMIN, "addxp", (p, args) -> p.skills().addXp(Integer.valueOf(args[0]), Integer.valueOf(args[1])));
        put(Privilege.ADMIN, "hitme", (p, args) -> p.hit(p, Integer.valueOf(args[0]), Hit.Type.REGULAR));
        put(Privilege.PLAYER, "empty", (p, args) -> p.inventory().empty());
        put(Privilege.MODERATOR, "teleto", (p, args) -> p.teleport(p.world().playerByName(glue(args)).get().tile()));
        put(Privilege.MODERATOR, "teletome", (p, args) -> p.world().playerByName(glue(args)).get().teleport(p.tile()));
        put(Privilege.ADMIN, "maxspec", (p, args) -> p.varps().varp(Varp.SPECIAL_ENERGY, 1000));
        put(Privilege.ADMIN, "finditem", (p, args) -> {
            String s = glue(args);
            new Thread(() -> {
                int found = 0;

                for (int i = 0; i < 14_000; i++) {
                    if (found > 249) {
                        p.message("Too many results (> 250). Please narrow down.");
                        break;
                    }
                    ItemDefinition def = p.world().definitions().get(ItemDefinition.class, i);
                    if (def != null && def.name.toLowerCase().contains(s)) {
                        p.message("Result: " + i + " - " + def.name + " (price: " + def.cost + ")");
                        found++;
                    }
                }
                p.message("Done searching. Found " + found + " results.");
            }).start();
        });
        put(Privilege.ADMIN, "stress", (p, args) -> {
            p.graphic(123);
            p.animate(123);
            p.hit(p, 1);
            p.hit(p, 1);
            p.face(p);
            p.looks().update();
            p.sync().publicChatMessage(new ChatMessage("Hi", 0, 0));
        });
        put(Privilege.ADMIN, "input", (p, args) -> {
            p.inputHelper().provideAlphaNumerical("Is William Gay?", new AlphaNumericalInput() {
				@Override
				public void execute(Player player, String value) {
					System.out.println("The value is: " + value);
				}
			});
        });
        put(Privilege.PLAYER, "ancients", (p, args) -> p.varps().varbit(4070, 1));
        put(Privilege.PLAYER, "modern", (p, args) -> p.varps().varbit(4070, 0));
        put(Privilege.PLAYER, "lunar", (p, args) -> p.varps().varbit(4070, 2));
        put(Privilege.PLAYER, "gdz", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            p.teleport(3288, 3886);
        });
        put(Privilege.PLAYER, "chins", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            p.teleport(3138, 3784);
        });
        put(Privilege.PLAYER, "44s", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            p.teleport(2978, 3871);
        });
        put(Privilege.PLAYER, "mb", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            p.teleport(2539, 4716);
        });

        put(Privilege.PLAYER, "edge", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            p.teleport(3086, 3491);
        });

        put(Privilege.PLAYER, "commands", (p, args) -> {
            p.message("--------------------Commands--------------------");
            p.message("::ancients - changes to the ancient spellbook.");
            p.message("::lunar - changes to the lunar spellbook.");
            p.message("::modern - changes to the modern spellbook.");
            p.message("::master - sets all your levels to 99.");
            p.message("::empty - clears your inventory.");
            p.message("::item id - spawns an item with the specified id.");
            p.message("::lvl skillid level - sets the specified skill to a level between 1 and 99.");
            p.message("::pkp - see your pk points.");
        });

        put(Privilege.PLAYER, "lvl", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            int skill = Integer.parseInt(args[0]);
            int lv = Integer.parseInt(args[1]);
            p.skills().xp()[skill] = Skills.levelToXp(lv);
            p.skills().levels()[skill] = p.skills().xpLevel(skill);
            p.skills().update();
        });

        put(Privilege.ADMIN, "kickall", (p, args) -> {
            p.world().players().forEach(Player::logout);
        });

        put(Privilege.PLAYER, "pkp", (p, args) -> p.message("You currently have " + p.attrib(AttributeKey.PK_POINTS, 0) + " PK points."));
        put(Privilege.PLAYER, "openbank", (p, args) -> {
            if (inWilderness(p)) {
                p.message("You cannot do this while in the wilderness.");
                return;
            }
            //Bank.open(p);
        });

        put(Privilege.ADMIN, "sound", (p, args) -> p.write(new PlaySound(Integer.parseInt(args[0]), 0)));
        put(Privilege.ADMIN, "removenpcs", (p, args) -> p.world().npcs().forEach(n -> p.world().npcs().remove(n)));
        put(Privilege.ADMIN, "reloadnpcs", (p, args) -> {
            p.world().npcs().forEach(n -> p.world().npcs().remove(n));
            p.world().loadNpcSpawns();
        });
        put(Privilege.ADMIN, "transmog", (p, args) -> p.looks().transmog(Integer.parseInt(args[0])));
        return commands;
    }

    private static boolean inWilderness(Player player) {
        if (player.privilege().eligibleTo(Privilege.ADMIN))
            return false;
        Tile t = player.tile();
        return t.x > 2941 && t.x < 3329 && t.z > 3524 && t.z < 3968;
    }

    private static void put(Privilege privilege, String name, BiConsumer<Player, String[]> handler) {
        Command command = new Command();
        command.privilege = privilege;
        command.handler = handler;
        commands.put(name, command);
    }

    private static String glue(String[] args) {
        return Arrays.stream(args).collect(Collectors.joining(" "));
    }

    public static void process(Player player, String command) {
        String[] parameters = new String[0];
        String[] parts = command.split(" ");

        if (parts.length > 1) {
            parameters = new String[parts.length - 1];
            System.arraycopy(parts, 1, parameters, 0, parameters.length);
            command = parts[0];
        }

        int level = player.privilege().ordinal();
        while (level-- >= 0) {
            if (!commands.containsKey(command.toLowerCase())) {
                continue;
            }

            Command c = commands.get(command.toLowerCase());

			/* Verify privilege */
            if (player.privilege().eligibleTo(c.privilege)) {
                c.handler.accept(player, parameters);
                return;
            }
        }

        player.message("Command '%s' does not exist.", command);
    }

    static class Command {
        Privilege privilege;
        BiConsumer<Player, String[]> handler;
    }
}
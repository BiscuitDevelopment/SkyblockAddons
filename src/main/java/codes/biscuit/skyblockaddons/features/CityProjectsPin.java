package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.Sys;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Keeps track of a single City Project (Pinnable in the contribute menu)
 *
 * @author Charzard4261
 */
public class CityProjectsPin {

    /**
     * The CityProjectsPin instance
     */
    @Getter private static final CityProjectsPin instance = new CityProjectsPin();
    public Project pin;
    Project dummyProject;

    public Project getDummyProject()
    {
        if (dummyProject != null)
            return dummyProject;
        dummyProject = new Project();
        dummyProject.name = "Project - Bartender's Brewery";
        dummyProject.contribs = new ArrayList<>();
        {
            Contribute cont = new Contribute();
            cont.name = "§aBuilding & Machinery";
            cont.components.add(new Component("§aEnchanted Birch Wood", 2));
            cont.components.add(new Component("§aEnchanted Iron", 1));
            dummyProject.contribs.add(cont);
        }
        {
            Contribute cont = new Contribute();
            cont.name = "§aSugary Drinks";
            cont.components.add(new Component("§aEnchanted Sugar", 32));
            cont.components.add(new Component("§fMagical Water Bucket", 8));
            dummyProject.contribs.add(cont);
        }
        {
            Contribute cont = new Contribute();
            cont.name = "§aSlavic Recipes";
            cont.components.add(new Component("§aEnchanted Potato", 64));
            cont.components.add(new Component("§fMagical Water Bucket", 8));
            dummyProject.contribs.add(cont);
        }
        {
            Contribute cont = new Contribute();
            cont.name = "§aLabor";
            cont.components.add(new Component("§aEnchanted Clownfish", 4));
            cont.components.add(new Component("§aEnchanted Melon", 64));
            cont.bitsReq = 100;
            dummyProject.contribs.add(cont);
        }
        return dummyProject;
    }

    public void pinProject(IInventory inv) {
        Project project = new Project(inv);
        if (pin != null && pin.name.equalsIgnoreCase(project.name)) {
            pin = null;
            return;
        }
        pin = project;
    }

    /**
     * Refresh the current pin (must be in menu), so doesn't re-download item data
     */
    public void refreshPin() {

    }

    // Please don't change this biscuit ;-;

    /**
     * A City Project in a neat little class
     */
    public class Project {
        // Must match colours too, since the Confirm Contribution menu does too, if you change it change both
        public String name;
        public ArrayList<Contribute> contribs;

        public Project() {
        }

        public Project(IInventory inv) {
            contribs = new ArrayList<>();
            int currentCount = 0;

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack itemStack = inv.getStackInSlot(i);

                if (itemStack.getItem() == Items.dye) {

                    if (itemStack.getDisplayName().equalsIgnoreCase("§eContribute this component!")) {

                        NBTTagList loreNbt = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                        boolean costFound = false;

                        for (int l = 0; l < loreNbt.tagCount(); l++) {
                            String lore = ((NBTTagString) loreNbt.get(l)).getString();

                            if (!costFound && lore.equalsIgnoreCase("§7Cost")) {
                                costFound = true;
                                continue;
                            }

                            if (costFound) {
                                if (contribs.get(currentCount) == null)
                                    contribs.add(new Contribute());
                                if (lore.isEmpty())
                                    break;
                                else {
                                    if (lore.contains("Bits"))
                                        contribs.get(currentCount).bitsReq = Integer.parseInt(lore.split(" ")[0].replaceFirst("§b", ""));
                                    else {
                                        String[] split = lore.split(" ");
                                        Component c;
                                        if (split[split.length - 1].matches(".*\\d.*")) {
                                            String name = lore.substring(0, lore.length() - split[split.length - 1].length()).trim();
                                            String count = split[split.length - 1]
                                                    .split("x")[1];
                                            c = new Component(name, Integer.valueOf(count));
                                        } else {
                                            c = new Component(lore.trim(), 1);
                                        }
                                        contribs.get(currentCount).components.add(c);

                                    }
                                }
                            }
                        }
                        currentCount++;
                    } else if (itemStack.getDisplayName().equalsIgnoreCase("§aComponent Contributed!")) {
                        contribs.get(currentCount).completed = true;
                        currentCount++;
                    }
                } else {
                    if (itemStack.getTagCompound() == null || itemStack.getTagCompound().getCompoundTag("display") == null || itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8) == null)
                        continue;
                    NBTTagList loreNbt = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                    if (loreNbt.get(0).toString().startsWith("\"§8Project Component #")) {
                        //int pos = Integer.parseInt(loreNbt.get(0).toString().substring("§8Project Component #".length(), loreNbt.get(0).toString().length()-1));
                        Contribute contribute = new Contribute();
                        contribute.name = itemStack.getDisplayName();
                        contribs.add(contribute);
                    } else if (loreNbt.get(0).toString().equalsIgnoreCase("\"§8City Project\""))
                        name = itemStack.getDisplayName();

                }
            }
        }

    }

    public class Contribute {
        public String name;
        public boolean completed = false;
        public ArrayList<Component> components = new ArrayList<>();
        public int bitsReq = -1;
    }

    public class Component {
        public int req, current;
        public String name;
        public ItemStack render;

        public Component(String name, int req) {
            this(name, req, null);
        }

        public Component(String name, int req, ItemStack render) {
            this.name = name;
            this.req = req;
            if (render != null)
                this.render = render.copy();
        }
    }
}
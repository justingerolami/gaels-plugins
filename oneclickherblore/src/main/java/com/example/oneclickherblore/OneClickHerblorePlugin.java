package com.example.oneclickherblore;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import java.util.*;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import javax.annotation.Nullable;

import net.runelite.client.events.ConfigChanged;

import static net.runelite.api.ItemID.*;





@Extension
@PluginDescriptor(
        name = "One Click Herblore",
        enabledByDefault = false,
        description = "Supports one click unfinished and finished potions."
)

@Slf4j
public class OneClickHerblorePlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private OneClickHerbloreConfig config;

    @Provides
    OneClickHerbloreConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickHerbloreConfig.class);
    }

    private int stage = 1;
    private int bankingStep = 1;
    private int timeout;
    private int ItemID1;
    private int ItemID2;
    private int ItemQuantity1;
    private int ItemQuantity2;
    private boolean potsRemaining=false;

    private HashMap<String, Integer[]> unf_pots = new HashMap<>();
    private HashMap<String,Integer[]> pots = new HashMap<>();

    @Override
    protected void startUp() throws Exception {
        stage = 1;
        bankingStep = 1;

        unf_pots.put("guam_unf", new Integer[]{VIAL_OF_WATER, GUAM_LEAF});
        unf_pots.put("marrentill_unf",new Integer[]{VIAL_OF_WATER, MARRENTILL});
        unf_pots.put("tarromin_unf",new Integer[]{VIAL_OF_WATER, TARROMIN});
        unf_pots.put("harralander_unf",new Integer[]{VIAL_OF_WATER, HARRALANDER});
        unf_pots.put("ranarr_unf",new Integer[]{VIAL_OF_WATER, RANARR_WEED});
        unf_pots.put("toadflax_unf",new Integer[]{VIAL_OF_WATER, TOADFLAX});
        unf_pots.put("irit_unf",new Integer[]{VIAL_OF_WATER, IRIT_LEAF});
        unf_pots.put("avantoe_unf",new Integer[]{VIAL_OF_WATER, AVANTOE});
        unf_pots.put("kwuarm_unf",new Integer[]{VIAL_OF_WATER, KWUARM});
        unf_pots.put("snapdragon_unf",new Integer[]{VIAL_OF_WATER, SNAPDRAGON});
        unf_pots.put("cadantine_unf",new Integer[]{VIAL_OF_WATER, CADANTINE});
        unf_pots.put("lantadyme_unf",new Integer[]{VIAL_OF_WATER, LANTADYME});
        unf_pots.put("dwarfweed_unf",new Integer[]{VIAL_OF_WATER, DWARF_WEED});
        unf_pots.put("torstol_unf",new Integer[]{VIAL_OF_WATER, TORSTOL});
        unf_pots.put("antidote_unf",new Integer[]{COCONUT_MILK, TOADFLAX});
        unf_pots.put("antidote_pp_unf",new Integer[]{COCONUT_MILK, IRIT_LEAF});

        pots.put("attack",new Integer[]{GUAM_POTION_UNF, EYE_OF_NEWT});
        pots.put("antipoison",new Integer[]{MARRENTILL_POTION_UNF, UNICORN_HORN_DUST});
        pots.put("strength",new Integer[]{TARROMIN_POTION_UNF, LIMPWURT_ROOT});
        pots.put("compost",new Integer[]{HARRALANDER_POTION_UNF, VOLCANIC_ASH});
        pots.put("energy",new Integer[]{HARRALANDER_POTION_UNF, CHOCOLATE_DUST});
        pots.put("agility",new Integer[]{TOADFLAX_POTION_UNF, TOADS_LEGS});
        pots.put("prayer",new Integer[]{RANARR_POTION_UNF, SNAPE_GRASS});
        pots.put("super_attack",new Integer[]{IRIT_POTION_UNF, EYE_OF_NEWT});
        pots.put("super_antipoison",new Integer[]{IRIT_POTION_UNF, UNICORN_HORN_DUST});
        pots.put("super_energy",new Integer[]{AVANTOE_POTION_UNF, MORT_MYRE_FUNGUS});
        pots.put("super_strength",new Integer[]{KWUARM_POTION_UNF, LIMPWURT_ROOT});
        pots.put("super_restore",new Integer[]{SNAPDRAGON_POTION_UNF, RED_SPIDERS_EGGS});
        pots.put("super_defence",new Integer[]{CADANTINE_POTION_UNF, WHITE_BERRIES});
        pots.put("antidote_p",new Integer[]{ANTIDOTE_UNF, YEW_ROOTS});
        pots.put("antifire",new Integer[]{LANTADYME_POTION_UNF, DRAGON_SCALE_DUST});
        pots.put("ranging",new Integer[]{DWARF_WEED_POTION_UNF, WINE_OF_ZAMORAK});
        pots.put("magic",new Integer[]{LANTADYME_POTION_UNF, POTATO_CACTUS});
        pots.put("stamina",new Integer[]{SUPER_ENERGY3, AMYLASE_CRYSTAL});
        pots.put("antidote_pp",new Integer[]{ANTIDOTE_UNF_5951, MAGIC_ROOTS});
        pots.put("brew",new Integer[]{TOADFLAX_POTION_UNF, CRUSHED_NEST});
        pots.put("extended_antifire",new Integer[]{ANTIFIRE_POTION3, LAVA_SCALE_SHARD});
        pots.put("antivenom",new Integer[]{ANTIDOTE3_5954, ZULRAHS_SCALES});
        //pots.put("super_combat",new Integer[]{SUPER_ATTACK3, SUPER_STRENGTH3, SUPER_DEFENCE3,EYE_OF_NEWT});
        pots.put("antivenom_p",new Integer[]{ANTIVENOM3, TORSTOL});
        pots.put("extended_super_antifire",new Integer[]{SUPER_ANTIFIRE_POTION3, LAVA_SCALE_SHARD});
        pots.put("ultra_compost", new Integer[]{SUPERCOMPOST,VOLCANIC_ASH});
        getItemInfo();
    }


    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if(event.getGroup().equals("OneClickHerblore"))
        {
            getItemInfo();
            stage = 1;
            bankingStep = 1;
            potsRemaining = false;
        }
    }

    private void getItemInfo(){
        Integer[] itemsToUse;
        Enum type;

        if (config.mode() == OneClickHerbloreModes.unfinished_pots){
            type = config.unf();
            itemsToUse = unf_pots.get(type.toString());
        } else{
            type = config.pots();
            itemsToUse = pots.get(type.toString());
        }
        ItemID1 = itemsToUse[0];
        ItemID2 = itemsToUse[1];

        if (type.toString().equals("stamina") || type.toString().equals("compost") || type.toString().equals("ultra_compost")){
            ItemQuantity1 = 27;
            ItemQuantity2 = 1;
        } else{
            ItemQuantity1 = 14;
            ItemQuantity2 = 14;
        }
    }


    @Subscribe
    public void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
        }

        if ((getInventoryItem(ItemID1) == null && getInventoryItem(ItemID2) == null) && !bankOpen()) {
            timeout = 0;
            stage = 1;
        }
    }

    private WidgetItem getInventoryItem(int id) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            Collection<WidgetItem> items = inventoryWidget.getWidgetItems();
            for (WidgetItem item : items) {
                if (item.getId() == id) {
                    return item;
                }
            }
        }
        return null;
    }

    @Nullable
    private Collection<WidgetItem> getInventoryItems() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
        if (inventory == null) {
            return null;
        }
        return new ArrayList<>(inventory.getWidgetItems());
    }

    public int getInventQuantity(Integer itemId) {
        Collection<WidgetItem> inventoryItems = getInventoryItems();
        if (inventoryItems == null) {
            return 0;
        }
        int count = 0;
        for (WidgetItem inventoryItem : inventoryItems) {
            if (inventoryItem.getId() == itemId) {
                count += 1;
            }
        }
        return count;
    }

    public int getEmptySlots() {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null) {
            return 28 - inventoryWidget.getWidgetItems().size();
        } else {
            return -1;
        }
    }

    private GameObject getGameObject(int ID) {
        return new GameObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private boolean bankOpen() {
        return client.getItemContainer(InventoryID.BANK) != null;
    }

    private MenuEntry openBank() {
        if (config.bankType() == OneClickHerbloreBankTypes.Booth) {
            int BANK_BOOTH_ID = config.bankID();
            return createMenuEntry(
                    BANK_BOOTH_ID,
                    MenuAction.GAME_OBJECT_SECOND_OPTION,
                    getLocation(getGameObject(BANK_BOOTH_ID)).getX(),
                    getLocation(getGameObject(BANK_BOOTH_ID)).getY(),
                    false);
        }

        int BANK_CHEST_ID = config.bankID();
        return createMenuEntry(
                BANK_CHEST_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(BANK_CHEST_ID)).getX(),
                getLocation(getGameObject(BANK_CHEST_ID)).getY(),
                true);
    }

    private Point getLocation(TileObject tileObject) {
        if (tileObject == null) {
            return new Point(0, 0);
        }
        if (tileObject instanceof GameObject) {
            return ((GameObject) tileObject).getSceneMinLocation();
        }
        return new Point(tileObject.getLocalLocation().getSceneX(), tileObject.getLocalLocation().getSceneY());
    }

    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }

    private MenuEntry closeBank() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                11,
                786434,
                true);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (timeout != 0) {
            event.consume();
            return;
        }

        if (event.getMenuOption().equals("<col=00ff00>One Click Herblore")) {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        String text;

        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        } else {
            text = "<col=00ff00>One Click Herblore";
        }
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }


    private void handleClick(MenuOptionClicked event) {

        if ((client.getLocalPlayer().getAnimation() != -1)
                && !bankOpen()) {
            event.consume();
            return;
        }

        if (getInventoryItem(ItemID1) == null) {
            if (!bankOpen()) {
                event.setMenuEntry(openBank());
                potsRemaining = false;
                return;
            }
        }

        if (bankOpen()) {
            if ((getInventQuantity(ItemID1) <= ItemQuantity1 && getInventQuantity(ItemID1) > 0) && (getInventQuantity(ItemID2) <= ItemQuantity2 && getInventQuantity(ItemID2) > 0)) {
                event.setMenuEntry(closeBank());
                bankingStep = 1;
            } else if ((getInventQuantity(ItemID1) == 0) && (getEmptySlots() >= 27) && bankingStep == 1) {
                bankingStep = 2;
                event.setMenuEntry(withdrawAllItems(ItemID1));
               //timeout++;
            } else if (getInventQuantity(ItemID2) == 0 && bankingStep==2) {
                event.setMenuEntry(withdrawAllItems(ItemID2));
                //timeout++;
            } else {
                event.setMenuEntry(depositItems());
            }
            return;
        }

        if (getInventoryItem(ItemID1) != null && (getInventoryItem(ItemID2) != null)) {
            if(potsRemaining && client.getLocalPlayer().getPoseAnimation() == client.getLocalPlayer().getIdlePoseAnimation()){
                stage = 1;
                potsRemaining = false;
            }
            switch (stage) {
                case 1:
                    event.setMenuEntry(useItem1(ItemID1));
                    stage = 2;
                    break;
                case 2:
                    event.setMenuEntry(useOnItem2(ItemID2));
                    stage = 3;
                    timeout++;
                    break;
                case 3:
                    event.setMenuEntry(selectMenuAction());
                    potsRemaining = true;
                    timeout +=2;
                    break;
            }
        }
    }


    private int getBankIndex(int itemid) {
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(itemid)
                .result(client)
                .first();
        return bankItem.getWidget().getIndex();
    }

    private MenuEntry withdrawAllItems(int itemid) {
        return createMenuEntry(
                5,
                MenuAction.CC_OP,
                getBankIndex(itemid),
                786445,
                true);
    }

    private MenuEntry depositItems() {
        int itemToDeposit = -1;
        Collection<WidgetItem> inventoryItems = getInventoryItems();

        for (WidgetItem inventoryItem : inventoryItems) {
                if(inventoryItem.getId() != ItemID2 || (inventoryItem.getId() == ItemID2 && ItemQuantity2 != 1)) {
                    itemToDeposit = inventoryItem.getId();
                }
            }

        return createMenuEntry(
                8,
                MenuAction.CC_OP_LOW_PRIORITY,
                getInventoryItem(itemToDeposit).getIndex(),
                983043,
                false);
    }

    private MenuEntry useItem1(int itemid) {
        return createMenuEntry(
                itemid,
                MenuAction.ITEM_USE,
                getInventoryItem(itemid).getIndex(),
                9764864,
                true);
    }

    private MenuEntry useOnItem2(int itemid) {
        return createMenuEntry(
                itemid,
                MenuAction.ITEM_USE_ON_WIDGET_ITEM,
                getInventoryItem(itemid).getIndex(),
                9764864,
                true);
    }

    private MenuEntry selectMenuAction() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                17694734,
                false);
    }
}
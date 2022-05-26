package com.example.oneclickbankstander;

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
        name = "One Click Bankstander",
        enabledByDefault = false,
        description = "Supports one click custom bankstanding."
)

@Slf4j
public class OneClickBankstanderPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private OneClickBankstanderConfig config;

    @Provides
    OneClickBankstanderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickBankstanderConfig.class);
    }

    private int stage = 1;
    private int bankingStep = 1;
    private int timeout;
    private int ItemID1;
    private int ItemID2;
    private int ItemQuantity1;
    private int ItemQuantity2;
    private boolean itemsRemaining=false;
    

    @Override
    protected void startUp() throws Exception {
        stage = 1;
        bankingStep = 1;
        getItemInfo();
    }


    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if(event.getGroup().equals("OneClickBankstander"))
        {
            getItemInfo();
            stage = 1;
            bankingStep = 1;
            itemsRemaining = false;
        }
    }

    private void getItemInfo(){
        Integer[] itemsToUse;
        Enum type;
        
        ItemID1 = config.itemId1();
        ItemID2 = config.itemId2();

        ItemQuantity1 = 14;
        ItemQuantity2 = 14;

    }


    @Subscribe
    public void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
        }

        if ((getWidgetItem(ItemID1) == null && getWidgetItem(ItemID2) == null) && !bankOpen()) {
            timeout = 0;
            stage = 1;
        }
    }

    public Widget getWidgetItem(int ids) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget != null && inventoryWidget.getChildren() != null) {
            Widget[] items = inventoryWidget.getChildren();
            for (Widget item : items) {
                if (ids == item.getItemId()) {
                    return item;
                }
            }
        }
        return null;
    }

    private int getEmptySlots() {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());
        Widget depositBoxInventory = client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER.getId());

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        if (depositBoxInventory!=null && !depositBoxInventory.isHidden()
                && depositBoxInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        return -1;
    }

    private int getInventQuantity(int itemId) {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());
        Widget depositBoxInventory = client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER.getId());

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == itemId).count();
        }

        if (bankInventory!=null && !bankInventory.isHidden()
                && bankInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == itemId).count();
        }

        if (depositBoxInventory!=null && !depositBoxInventory.isHidden()
                && depositBoxInventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == itemId).count();
        }

        return -1;
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
        if (config.bankType() == OneClickBankstanderBankTypes.Booth) {
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

        if (event.getMenuOption().equals("<col=00ff00>One Click Bankstander")) {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        String text;

        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        } else {
            text = "<col=00ff00>One Click Bankstander";
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

        if (getWidgetItem(ItemID1) == null) {
            if (!bankOpen()) {
                event.setMenuEntry(openBank());
                timeout++;
                itemsRemaining = false;
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

        if (getWidgetItem(ItemID1) != null && (getWidgetItem(ItemID2) != null)) {
            if(itemsRemaining && client.getLocalPlayer().getPoseAnimation() == client.getLocalPlayer().getIdlePoseAnimation()){
                stage = 1;
                itemsRemaining = false;
            }
            switch (stage) {
                case 1:
                    event.setMenuEntry(useItem1OnItem2());
                    timeout++;
                    stage = 2;
                    break;
                case 2:
                    event.setMenuEntry(selectMenuAction());
                    itemsRemaining = true;
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


    private Widget getDepositItem(){
        Widget bankInventory = client.getWidget(WidgetInfo.BANK_INVENTORY_ITEMS_CONTAINER.getId());
        if (bankInventory != null && bankInventory.getChildren() != null) {
            Widget[] items = bankInventory.getChildren();
            for (Widget item : items) {
                if (item.getItemId() != ItemID2 || (item.getItemId() == ItemID2 && ItemQuantity2 != 1)) {
                    System.out.println("found item to deposit");
                    return item;
                }
            }
        }
        return null;
    }

    private MenuEntry depositItems() {
        Widget itemToDeposit = getDepositItem();
        return createMenuEntry(
                8,
                MenuAction.CC_OP_LOW_PRIORITY,
                itemToDeposit.getIndex(),
                983043,
                false);
    }

    private MenuEntry useItem1OnItem2()
    {
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(getWidgetItem(ItemID1).getIndex());
        client.setSelectedSpellItemId(ItemID1);
        return createMenuEntry(
                0,
                MenuAction.WIDGET_TARGET_ON_WIDGET,
                getWidgetItem(ItemID2).getIndex(),
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
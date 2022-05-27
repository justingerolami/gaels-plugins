package com.example.oneclickprayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.queries.BankItemQuery;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;


@Extension
@PluginDescriptor(
        name = "One Click Prayer",
        enabledByDefault = false,
        description = "Supports gilded altar."
)

@Slf4j
public class OneClickPrayerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private OneClickPrayerConfig config;

    @Provides
    OneClickPrayerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickPrayerConfig.class);
    }

    private int stage = 1;
    private int timeout;
    private int boneId;
    private int notedId;
    private boolean itemsRemaining=false;
    

    @Override
    protected void startUp() throws Exception {
        stage = 1;
        boneId = config.boneId();
        notedId = config.notedId();
    }


    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if(event.getGroup().equals("OneClickPrayer"))
        {
            boneId = config.boneId();
            notedId = config.notedId();
            stage = 1;
            itemsRemaining = false;
        }
    }


    @Subscribe
    public void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
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

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
            return (int) inventoryItems.stream().filter(item -> item.getItemId() == 6512).count();
        }

        return -1;
    }

    private int getInventQuantity(int itemId) {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY.getId());

        if (inventory!=null && !inventory.isHidden()
                && inventory.getDynamicChildren()!=null)
        {
            List<Widget> inventoryItems = Arrays.asList(client.getWidget(WidgetInfo.INVENTORY.getId()).getDynamicChildren());
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

    private NPC getNpc(int ID) {
        return new NPCQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
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

    private Point getLocation(NPC npc) {
        return new Point(npc.getLocalLocation().getSceneX(),npc.getLocalLocation().getSceneY());
    }


    public MenuEntry createMenuEntry(int identifier, MenuAction type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(0).setOption("").setTarget("").setIdentifier(identifier).setType(type)
                .setParam0(param0).setParam1(param1).setForceLeftClick(forceLeftClick);
    }


    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) throws InterruptedException {
        if (timeout != 0) {
            event.consume();
            return;
        }

        if (event.getMenuOption().equals("<col=00ff00>One Click Prayer")) {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        String text;

        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        } else {
            text = "<col=00ff00>One Click Prayer";
        }
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }


    private void handleClick(MenuOptionClicked event) {




        /*

        1. if we have no bones & we are inside, click portal
        2. if we have no bones & we are outside, use bones on npc
        3. if we have no bones and are talking, select dialogue
        4. if we have bones and are outside, click portal
        5. if we have bones and are inside, click altar

         */

        if (getInventQuantity(boneId) == 0 && insidePOH()){
            System.out.println("EXIT POH");
            event.setMenuEntry(clickPortalToOutside());
        }

        if (getInventQuantity(boneId) == 0 && !insidePOH()){
            System.out.println("USE NOTED BONES ON PHIALS");
            event.setMenuEntry(useBonesOnNpc());

        }

        if (client.getWidget(219, 1) != null && client.getWidget(219, 1).getChild(3).getText().contains("Exchange All")) {
            System.out.println("UNOTE THE BONES");
            event.setMenuEntry(selectUnnoteBones());
        }

        if (getInventQuantity(boneId) != 0 && !insidePOH()){
            System.out.println("CLICK INSIDE POH");
            event.setMenuEntry(clickPortalToInside());
        }


        if (getInventQuantity(boneId) != 0 && insidePOH()){
            System.out.println("CLICK ALTAR");
            event.setMenuEntry(useBonesOnAltar());
        }

    }


    private MenuEntry clickPortalToOutside(){
        //portal to out = 4525
        return createMenuEntry(
                getGameObject(4525).getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(4525)).getX(),
                getLocation(getGameObject(4525)).getY(),
                false);
    }

    private MenuEntry clickPortalToInside(){
        //post to in =  29091
        //easier than using portal + saves 1 click

        return createMenuEntry(
                getGameObject(29091).getId(),
                MenuAction.GAME_OBJECT_THIRD_OPTION,
                getLocation(getGameObject(29091)).getX(),
                getLocation(getGameObject(29091)).getY(),
                false);
    }

    private MenuEntry useBonesOnNpc(){
        //phials = 1614

        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(getWidgetItem(notedId).getIndex());
        client.setSelectedSpellItemId(notedId);

        NPC npc = getNpc(1614);
        return createMenuEntry(
                npc.getIndex(),
                MenuAction.WIDGET_TARGET_ON_NPC,
                getLocation(npc).getX(),
                getLocation(npc).getY(),
                false);
    }

    private MenuEntry selectUnnoteBones(){
        return createMenuEntry(
                0,
                MenuAction.WIDGET_CONTINUE,
                3,
                WidgetInfo.DIALOG_OPTION_OPTION1.getId(),
                false);
    }

    private MenuEntry useBonesOnAltar(){
        client.setSelectedSpellWidget(WidgetInfo.INVENTORY.getId());
        client.setSelectedSpellChildIndex(getWidgetItem(boneId).getIndex());
        client.setSelectedSpellItemId(boneId);

        GameObject altar = getGameObject(13197);
        return createMenuEntry(
                13197,
                MenuAction.WIDGET_TARGET_ON_GAME_OBJECT,
                getLocation(altar).getX(),
                getLocation(altar).getY(),
                false);
    }

    private boolean insidePOH(){
        return getGameObject(4525)!=null;
    }









}
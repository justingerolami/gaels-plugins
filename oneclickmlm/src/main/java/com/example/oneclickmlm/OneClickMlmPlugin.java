package com.example.oneclickmlm;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameTick;
import net.runelite.api.queries.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import net.runelite.api.Perspective;
import net.runelite.api.WallObject;
import net.runelite.client.callback.ClientThread;
import net.runelite.api.ItemID;


import java.util.*;

import java.util.stream.Collectors;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import javax.annotation.Nullable;


@Extension
@PluginDescriptor(
        name = "One Click Mlm",
        enabledByDefault = false,
        description = ""
)

@Slf4j
public class OneClickMlmPlugin extends Plugin {
    @Inject
    private Client client;
    private ClientThread clientThread;

    @Inject
    private OneClickMlmConfig config;

    @Provides
    OneClickMlmConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(OneClickMlmConfig.class);
    }

    Set<Integer> GEMS = Set.of(ItemID.UNCUT_SAPPHIRE,ItemID.UNCUT_EMERALD,ItemID.UNCUT_RUBY,ItemID.UNCUT_DIAMOND);
    Set<Integer> MINING_ANIMATION = Set.of(6752,6758,8344,4481,7282,8345);
    private final int PAYDIRT = ItemID.PAYDIRT;
    private static final int UPPER_FLOOR_HEIGHT = -490;

    private int currSackSize;
    private int maxSackSize;

    private static final int SACK_LARGE_SIZE = 162;
    private static final int SACK_SIZE = 81;
    private static int DEPOSIT_AT_SIZE;
    private boolean needToDeposit = false;

    private WorldPoint rockLocation = new WorldPoint(3757,5677,0);
    private WorldPoint rockLocationNorth = new WorldPoint(3757,5676,0);
    private WorldPoint rockLocationSouth = new WorldPoint(3757,5678,0);
    private WorldPoint rockLocationEast = new WorldPoint(3758,5677,0);
    private WorldPoint rockLocationSW = new WorldPoint(3756,5676,0);

    private int brokenWheel = 26670;
    private WorldPoint brokenWheelNorth = new WorldPoint(3742,5669,0);
    private WorldPoint brokenWheelSouth = new WorldPoint(3742,5663,0);

    private boolean repairingWheel = false;

    private boolean dropGems;

    private int timeout;


    @Override
    protected void startUp() throws Exception {
        if(config.largeSack()){
            DEPOSIT_AT_SIZE = SACK_LARGE_SIZE - 27 - 6;
        }
        else{
            DEPOSIT_AT_SIZE = SACK_SIZE - 27 - 3;
        }

        if(config.dropGems()){
            dropGems = true;
        }
        else{
            dropGems = false;
        }

    }
    @Subscribe
    public void onGameTick(GameTick event) {
        if (timeout > 0) {
            timeout--;
        }

        if (config.repairNorthWheel()) {
            if (isSingleWheelBroken(brokenWheelNorth) && isSingleWheelBroken(brokenWheelSouth)) {
                repairingWheel = true;
            } else {
                repairingWheel = false;
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event)
    {
        if(event.getGroup().equals("OneClickMlm"))
        {
            if (!config.repairNorthWheel()){
                repairingWheel = false;
            }

            if(config.largeSack()){
                DEPOSIT_AT_SIZE = SACK_LARGE_SIZE - 27 - 6;
            }
            else{
                DEPOSIT_AT_SIZE = SACK_SIZE - 27 - 3;
            }

            if(config.dropGems()){
                dropGems = true;
            }
            else{
                dropGems = false;
            }

        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event)
    {
        refreshSackValues();

        if (currSackSize >= DEPOSIT_AT_SIZE){
            needToDeposit = true;
        }

        else if (currSackSize == 0) {
            needToDeposit = false;
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

    private GroundObject getGroundObject(int ID) {
        return new GroundObjectQuery()
                .idEquals(ID)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    private boolean bankOpen() {
        return (client.getWidget(WidgetInfo.DEPOSIT_BOX_INVENTORY_ITEMS_CONTAINER) != null ||
                client.getItemContainer(InventoryID.BANK) != null);
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

    public MenuEntry createMenuEntry(int identifier, int type, int param0, int param1, boolean forceLeftClick) {
        return client.createMenuEntry(
                "",
                "",
                identifier,
                type,
                param0,
                param1,
                forceLeftClick);
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

        if (event.getMenuOption().equals("<col=00ff00>One Click MLM")) {
            handleClick(event);
        }
    }

    @Subscribe
    private void onClientTick(ClientTick event) {
        String text;
        if (this.client.getLocalPlayer() == null || this.client.getGameState() != GameState.LOGGED_IN) {
            return;
        } else {
            text = "<col=00ff00>One Click MLM";
        }
        this.client.insertMenuItem(text, "", MenuAction.UNKNOWN
                .getId(), 0, 0, 0, true);
        client.setTempMenuEntry(Arrays.stream(client.getMenuEntries()).filter(x->x.getOption().equals(text)).findFirst().orElse(null));
    }


    private void handleClick(MenuOptionClicked event) {

        if(client.getLocalPlayer().isMoving() ||client.getLocalPlayer().getPoseAnimation()
                != client.getLocalPlayer().getIdlePoseAnimation())
        {
            event.consume();
        }

        if(MINING_ANIMATION.contains(client.getLocalPlayer().getAnimation()))
        {
            event.consume();
            return;
        }

        if(dropGems && !bankOpen())
        {
            for (int gem:GEMS)
            {
                if (getWidgetItem(gem)!=null)
                {
                    event.setMenuEntry(dropGemMES(getWidgetItem(gem)));
                    return;
                }
            }
        }

        if(config.useSpec() && !bankOpen() && onUpperLevel() && getInventQuantity(PAYDIRT) <=20) {
            if (client.getVar(VarPlayer.SPECIAL_ATTACK_PERCENT) == 1000) {
                event.setMenuEntry(specAtk());
                return;
            }
        }

        if(onUpperLevel()) {
            if (getEmptySlots() != 0) {
                if (checkCoords() && config.area() == OneClickMlmAreas.middle) {
                    System.out.println("MINING ROCK");
                    event.setMenuEntry(mineRock());
                } else if (checkStairCoords() && isRockThere() && config.area() == OneClickMlmAreas.middle) {
                    event.setMenuEntry(mineRock());
                } else {
                    System.out.println("CLICK VEIN");
                    event.setMenuEntry(clickVein());
                }
            }

            else if (getEmptySlots() == 0 && ((dropGems && !checkForGems()) || !dropGems)) {
                if (checkCoords() && config.area() == OneClickMlmAreas.middle) {
                    System.out.println("MINING ROCK");
                    event.setMenuEntry(mineRock());
                } else if (checkMiningAreaCoords() && isRockThere() && config.area() == OneClickMlmAreas.middle) {
                    System.out.println("MINING ROCK BEFORE CHECKCOORDS");
                    event.setMenuEntry(mineRock());
                } else {
                    System.out.println("CLICK DOWNSTAIRS");
                    event.setMenuEntry(clickStairsDown());
                }
            }

            else {
                System.out.println("Not sure how we got here!");
            }
        }

        else{ //we are on the main level
            if(getInventQuantity(PAYDIRT) >= 1){
                if(repairingWheel){
                    System.out.println("REPAIRING WHEEL");
                    repairWheel(event);
                }
                else {
                    System.out.println("DEPOSIT DIRT");
                    event.setMenuEntry(clickHopper());
                }
            }

            else if(getEmptySlots() == (28-getInventQuantity(2347)) && needToDeposit){
                System.out.println("GET ORE FROM SACK");
                event.setMenuEntry(clickSack());
            }

            else if (getEmptySlots()<(28-getInventQuantity(2347)) && getInventQuantity(PAYDIRT)==0 && !bankOpen()){
                System.out.println("OPEN DEPOSIT BOX");
                event.setMenuEntry(openDepositBox());
            }

            else if (getEmptySlots()<(28-getInventQuantity(2347)) && getInventQuantity(PAYDIRT)==0 && bankOpen()){
                System.out.println("DEPOSIT ALL");
                event.setMenuEntry(depositAllItems());
                timeout++;
            }

            else if (getEmptySlots() == (28-getInventQuantity(2347)) && needToDeposit == false){
                System.out.println("CLICK UPSTAIRS");
                event.setMenuEntry(clickStairsUp());
            }

            else{
                System.out.println("IN ELSE, SOMETHING WENT WRONG");
                System.out.println(getInventQuantity(PAYDIRT));
                System.out.println(bankOpen());
            }
        }
    }


    private boolean onUpperLevel(){
      return Perspective.getTileHeight(client, client.getLocalPlayer().getLocalLocation(), 0) < UPPER_FLOOR_HEIGHT;
    }

    private void refreshSackValues(){
        currSackSize = client.getVarbitValue(5558);
        boolean sackUpgraded = client.getVarbitValue(5556) == 1;
        maxSackSize = sackUpgraded ? SACK_LARGE_SIZE : SACK_SIZE;
    }

    private boolean checkForGems(){
        for (int gem:GEMS) {
            if (getWidgetItem(gem) != null) {
                return true;
            }
        }
        return false;
    }

    private WallObject getVein() {
        List<Integer> Ids= Arrays.asList(26662,26664,26661,26663);
        List<WallObject> clickableWallObjects = null;
        List<WallObject> wallObjects = new WallObjectQuery()
                .idEquals(Ids)
                .result(client)
                .stream().collect(Collectors.toList());

        if (config.area() == OneClickMlmAreas.middle) {
            clickableWallObjects = wallObjects.stream().filter(wallObject -> wallObject.getWorldLocation().getX() < 3764 && wallObject.getWorldLocation().getX() > 3754 &&
                    wallObject.getWorldLocation().getY() < 5685 && wallObject.getWorldLocation().getY() > 5674
                    && (wallObject.getWorldLocation().getX() != 3755 && wallObject.getWorldLocation().getY() != 5677)
                    && (wallObject.getWorldLocation().getX() != 3758 && wallObject.getWorldLocation().getY() != 5675))
                    .collect(Collectors.toList());
        }
        else if (config.area() == OneClickMlmAreas.outside){
            clickableWallObjects = wallObjects.stream().filter(wallObject ->
                    (wallObject.getWorldLocation().getX() < 3755 || (wallObject.getWorldLocation().getX() < 3763 && wallObject.getWorldLocation().getY() < 5675))
                    && wallObject.getWorldLocation().getX() > 3746 &&
                    wallObject.getWorldLocation().getY() < 5685
                    //wallObject.getWorldLocation().getY() > 5675
                    || (wallObject.getWorldLocation().getX() == 3755 && wallObject.getWorldLocation().getY() == 5677)
                    || (wallObject.getWorldLocation().getX() == 3758 && wallObject.getWorldLocation().getY() == 5675))
                    .collect(Collectors.toList());
        }

        WallObject selectedVein = clickableWallObjects.stream()
                .min(Comparator.comparing(entityType -> entityType.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation())))
                .orElse(null);

        System.out.println(client.getLocalPlayer().getWorldLocation().pathTo(client,selectedVein.getWorldLocation()).toString());
        return selectedVein;
    }

    private boolean checkCoords() {
        GameObject rockObject = new GameObjectQuery()
                .atWorldLocation(rockLocation)
                .result(client).first();

        if (((client.getLocalPlayer().getWorldLocation().distanceTo(rockLocationNorth) == 0) ||
                (client.getLocalPlayer().getWorldLocation().distanceTo(rockLocationSouth) == 0  && getEmptySlots()==0) ||
                (client.getLocalPlayer().getWorldLocation().distanceTo(rockLocationEast) == 0 && getEmptySlots()==0) ||
                client.getLocalPlayer().getWorldLocation().distanceTo(rockLocationSW) == 0)
            && rockObject != null){
            return true;
        } else {
            return false;
        }
    }

    private boolean checkStairCoords(){
        return (client.getLocalPlayer().getWorldLocation().getX() == 3755
        && client.getLocalPlayer().getWorldLocation().getY() == 5675);
    }

    private boolean checkMiningAreaCoords(){
        if(client.getLocalPlayer().getWorldLocation().getX() < 3764 &&
                client.getLocalPlayer().getWorldLocation().getX() > 3754 &&
                client.getLocalPlayer().getWorldLocation().getY() < 5685 &&
                client.getLocalPlayer().getWorldLocation().getY()>5674){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean isSingleWheelBroken(WorldPoint wheelLocation){
        GameObject wheel = new GameObjectQuery()
                .atWorldLocation(wheelLocation)
                .result(client).first();

        return wheel.getId() == brokenWheel;
    }

    private boolean isRockThere(){
        GameObject rock = new GameObjectQuery()
                .atWorldLocation(rockLocation)
                .result(client).first();

        return rock != null;
    }

    private int getBankIndex(int itemid) {
        WidgetItem bankItem = new BankItemQuery()
                .idEquals(itemid)
                .result(client)
                .first();
        return bankItem.getWidget().getIndex();
    }

    private MenuEntry openDepositBox() {
        return createMenuEntry(
                25937,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(25937)).getX(),
                getLocation(getGameObject(25937)).getY(),
                true);
    }

    private MenuEntry depositAllItems() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                12582916,
                false);
    }

    private MenuEntry clickStairsUp(){
        return createMenuEntry(
                getGameObject(19044).getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(19044)).getX(),
                getLocation(getGameObject(19044)).getY(),
                false);
    }

    private MenuEntry clickStairsDown(){
        return createMenuEntry(
                getGameObject(19045).getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(19045)).getX(),
                getLocation(getGameObject(19045)).getY(),
                true);
    }

    private MenuEntry clickHopper(){
        return createMenuEntry(
                getGameObject(26674).getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(26674)).getX(),
                getLocation(getGameObject(26674)).getY(),
                false);
    }

    private MenuEntry clickSack(){
        return createMenuEntry(
                getGroundObject(26688).getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGroundObject(26688)).getX(),
                getLocation(getGroundObject(26688)).getY(),
                false);
    }

    private MenuEntry clickVein() {
        WallObject customWallObject = getVein();

        return createMenuEntry(
                customWallObject.getId(),
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(customWallObject).getX(),
                getLocation(customWallObject).getY(), true);
    }

    private MenuEntry dropGemMES(Widget gem){
        return createMenuEntry(
                7,
                MenuAction.CC_OP_LOW_PRIORITY,
                gem.getIndex(),
                WidgetInfo.INVENTORY.getId(),
                false);
    }

    private MenuEntry dropPaydirt(){
        Widget paydirt_item = getWidgetItem(PAYDIRT);

        return createMenuEntry(
                7,
                MenuAction.CC_OP_LOW_PRIORITY,
                paydirt_item.getIndex(),
                WidgetInfo.INVENTORY.getId(),
                false);
    }

    private MenuEntry specAtk(){
        Widget specAtk = client.getWidget(WidgetInfo.MINIMAP_SPEC_CLICKBOX);
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                -1,
                specAtk.getId(),
                false);
    }

    private MenuEntry mineRock() {

        GameObject rockObject = new GameObjectQuery()
                .atWorldLocation(rockLocation)
                .result(client).first();

        return createMenuEntry(
                26679,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(rockObject).getX(),
                getLocation(rockObject).getY(),
                true);
    }

    private MenuEntry openBank() {
        int BANK_CHEST_ID = 26707;
        return createMenuEntry(
                BANK_CHEST_ID,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(getGameObject(BANK_CHEST_ID)).getX(),
                getLocation(getGameObject(BANK_CHEST_ID)).getY(),
                true);
    }

    private MenuEntry withdrawHammer() {
        return createMenuEntry(
                1,
                MenuAction.CC_OP,
                getBankIndex(2347),
                786445,
                true);
    }

    private MenuEntry clickWheel(GameObject wheel){
        return createMenuEntry(
                brokenWheel,
                MenuAction.GAME_OBJECT_FIRST_OPTION,
                getLocation(wheel).getX(),
                getLocation(wheel).getY(),
                true);
    }

    private void repairWheel(MenuOptionClicked event){
        if (getEmptySlots() == 0 && getInventQuantity(2347)==0) {
            //drop ore
            System.out.println("DROPPING PAYDIRT");
            event.setMenuEntry(dropPaydirt());
        }
        else if (!bankOpen() && getInventQuantity(2347)==0 && getEmptySlots() >= 1){
            //open bank
            System.out.println("OPENING BANK");
            event.setMenuEntry(openBank());
            timeout++;
        }
        else if (bankOpen() && getInventQuantity(2347) == 0){
            //withdraw hammer
            System.out.println("WITHDRAW HAMMER");
            event.setMenuEntry(withdrawHammer());
            timeout++;
        }
        else if (repairingWheel && config.repairNorthWheel()){
            //click north wheel strut
            GameObject northWheel = new GameObjectQuery()
                    .atWorldLocation(brokenWheelNorth)
                    .result(client).first();
            System.out.println("REPAIR NORTH WHEEL");
            event.setMenuEntry(clickWheel(northWheel));
            timeout++;
        }
    }
}
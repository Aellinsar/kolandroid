package com.github.kolandroid.kol.model.models.inventory;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.data.DataCache;
import com.github.kolandroid.kol.data.RawItem;
import com.github.kolandroid.kol.gamehandler.ViewContext;
import com.github.kolandroid.kol.model.GroupModel.ChildModel;
import com.github.kolandroid.kol.model.LiveModel;
import com.github.kolandroid.kol.model.elements.basic.BasicGroup;
import com.github.kolandroid.kol.model.elements.interfaces.ModelGroup;
import com.github.kolandroid.kol.session.Session;
import com.github.kolandroid.kol.util.Regex;

import java.util.ArrayList;

public class ItemPocketModel extends LiveModel implements ChildModel {
    protected static final Regex PWD = new Regex(
            "var\\s+pwd\\s*=\\s*\"([a-fA-F0-9]*)\";", 1);
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = 2109624705245532662L;
    private static final Regex SECTION = new Regex(
            "<a[^>]*><table.*?(</table>(?=.?<a)|$)", 0);
    private static final Regex SECTION_NAME = new Regex(
            "<font color=[\"']?white[\"']?>(.*?):</font>", 1);

    private static final Regex ITEM = new Regex(
            "<table class=[\"']?item.*?</table>", 0);

    private final String name;
    protected ArrayList<ModelGroup<ItemModel>> items;

    public ItemPocketModel(String name, Session s, String updateUrl) {
        super(s, updateUrl, true);
        this.items = new ArrayList<>();
        this.name = name;
    }

    public ItemPocketModel(String name, Session s, ServerReply reply) {
        this(name, s, reply.url);

        process(reply);
    }

    @Override
    public void attachView(ViewContext context) {
        super.attachView(context);

        DataCache<String, RawItem> itemCache = getData().getItemCache();
        for (ModelGroup<ItemModel> itemGroup : items) {
            for (ItemModel item : itemGroup) {
                item.searchCache(itemCache);
            }
        }
    }
    public ArrayList<ModelGroup<ItemModel>> getItems() {
        this.access();
        return items;
    }

    protected ModelGroup<ItemModel> parseItems(String sectionName,
                                               ArrayList<String> items, String pwd, Iterable<InventoryActionFactory> defaultActions) {
        DataCache<String, RawItem> itemCache = getData().getItemCache();

        BasicGroup<ItemModel> newSection = new BasicGroup<>(
                sectionName);
        for (String item : items) {
            if (item.contains("action=unequipall")) continue;

            ItemModel newItem = new ItemModel(getSession(), pwd, item, defaultActions);
            newItem.searchCache(itemCache);
            newSection.add(newItem);
        }
        return newSection;
    }

    protected void loadContent(ServerReply reply) {
        this.items = new ArrayList<>();

        String pwd = PWD.extractSingle(reply.html, "0");

        Iterable<InventoryActionFactory> additonalActions = getAdditionalActions(reply);
        for (String section : SECTION.extractAllSingle(reply.html)) {
            String sectionName = SECTION_NAME.extractSingle(section, "");
            ModelGroup<ItemModel> newSection = parseItems(sectionName,
                    ITEM.extractAllSingle(section), pwd, additonalActions);

            if (newSection.size() > 0)
                this.items.add(newSection);
        }
    }

    protected boolean apply(String itemId, int amountDifference) {
        boolean found = false;
        for (ModelGroup<ItemModel> group : this.items) {
            for (int i = 0; i < group.size(); i++) {
                if (group.get(i).matches(itemId)) {
                    ItemModel newItem = new ItemModel(group.get(i), amountDifference);
                    if (newItem.moreThanZero()) {
                        group.set(i, newItem);
                    } else {
                        group.remove(i);
                        i--; //move on to the next item in the group
                    }
                    found = true;
                }
            }
        }
        return found;
    }

    protected Iterable<InventoryActionFactory> getAdditionalActions(ServerReply reply) {
        ArrayList<InventoryActionFactory> res = new ArrayList<>();
        for (InventoryActionFactory factory : InventoryActionFactory.values()) {
            if (factory.findOnPage(reply)) {
                res.add(factory);
            }
        }
        return res;
    }

    public <Result> Result execute(PocketVisitor<Result> visitor) {
        return visitor.display(this);
    }

    @Override
    public String getTitle() {
        return name;
    }
}

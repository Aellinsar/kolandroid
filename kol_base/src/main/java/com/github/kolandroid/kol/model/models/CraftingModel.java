package com.github.kolandroid.kol.model.models;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.model.GroupModel;
import com.github.kolandroid.kol.session.Session;
import com.github.kolandroid.kol.util.Regex;

import java.util.ArrayList;

public class CraftingModel extends GroupModel<LiveWebModel> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -4634759533016983940L;
    private static final Regex SELECTION = new Regex("\\[[^\\]]*\\]", 0);
    private static final Regex CRAFT_TITLE = new Regex("\\[(?:<[^>]*>)?([^<]*)(?:<[^>]*>)?\\]", 1);
    private static final Regex CRAFT_LINK = new Regex("\\[<a href=[\"']?([^>]*?)[\"']?>*>[^<]*<[^>]*>\\]", 1);
    private static final Regex TOP_BAR = new Regex("(<body>.*?)<table.*?</table>.*?</table>", 0);

    private LiveWebModel[] crafts;

    public CraftingModel(Session s, ServerReply reply) {
        super(s);

        ArrayList<String> options = SELECTION.extractAllSingle(TOP_BAR.extractSingle(reply.html));

        int initialSlot = 0;
        crafts = new LiveWebModel[options.size()];
        for (int i = 0; i < options.size(); i++) {
            String title = CRAFT_TITLE.extractSingle(options.get(i), "[UNKNOWN]");
            String link = CRAFT_LINK.extractSingle(options.get(i));
            if (link == null) {
                initialSlot = i;
                link = "";
            }
            System.out.println("Found: " + title + " @ " + link);

            crafts[i] = createCraftingSubModel(s, title, link);
        }

        if (crafts.length == 0) {
            crafts = new LiveWebModel[]{createCraftingSubModel(s, "Error", "crafting.php")};
        }
        System.out.println("Loaded " + crafts.length + " crafts; selected " + initialSlot);

        crafts[initialSlot].process(reply);
        this.setActiveChild(initialSlot);
    }

    private static LiveWebModel createCraftingSubModel(Session s, String title, String updateUrl) {
        return new LiveWebModel(s, title, updateUrl) {
            @Override
            public String correctHtml(String html) {
                return TOP_BAR.replaceAll(html, "$1");
            }
        };
    }

    @Override
    public LiveWebModel[] getChildren() {
        return crafts;
    }
}

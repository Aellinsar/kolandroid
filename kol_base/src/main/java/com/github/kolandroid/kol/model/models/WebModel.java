package com.github.kolandroid.kol.model.models;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.model.Model;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.util.Logger;
import com.github.kolandroid.kol.util.Regex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class WebModel extends Model {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -1723871679147309734L;

    /**
     * Determine if a URL actually points to KoL
     */
    private static final Regex URL_FIND = new Regex(
            "^https?://www(\\d*).kingdomofloathing.com/(.*)$", 2);

    private static final Regex URL_BASE_FIND = new Regex(
            "^(?:.*/)?([^/?]*)(?:\\?.*)?$", 1);

    /**
     * Regexes for fixing item descriptions.
     */
    private static final Regex ITEM_DESC = new Regex(
            "<img[^>]*descitem\\((\\d+)(, event)?\\)[^>]*>");
    private static final Regex ITEM_WHICH_DESC = new Regex(
            "<img[^>]*descitem\\((\\d+),(\\d+)(, event)?\\)[^>]*>");

    /**
     * Regexes for fixing effect descriptions.
     */
    /* private static final Regex EFFECT_DESC = new Regex(
            "(<img[^>]*)on[Cc]lick=[\"']?eff\\([\"']?(.*?)[\"']?\\);?[\"']?([^>]*>)", 0);
    */
    private static final Regex EFFECT_DESC = new Regex(
            "<img[^>]*eff\\([\"']?(.*?)[\"']?\\)[^>]*>");

    /**
     * Regexes for replacing static buttons.
     */
    private static final Regex FIND_FORM = new Regex(
            "<form[^>]*>(<input[^>]*type=[\"']?hidden[^>]*>)*<input[^>]*button[^>]*></form>",
            0);
    private static final Regex HIDDEN_INPUT = new Regex(
            "<input[^>]*type=[\"']?hidden[^>]*>", 0);
    private static final Regex GET_NAME = new Regex(
            "name=[\"']?([^\"'> ]*)[\"'> ]", 1);
    private static final Regex GET_VALUE = new Regex(
            "value=[\"']?([^\"'> ]*)[\"'> ]", 1);

    private static final Regex INPUT_BUTTON = new Regex(
            "<input[^>]*button[^>]*>", 0);
    private static final Regex GET_TEXT = new Regex("value=\"([^>]*)\">", 1);

    private static final Regex FORM_ACTION = new Regex("<form([^>]*)action=[\"']?([^\"' >]*)[\"']?([^>]*)>", 2);
    private static final Regex FORM_METHOD = new Regex("<form([^>]*)method=[\"']?([^\"' >]*)[\"']?([^>]*)>", 2);
    private static final Regex FORM_SUBMIT = new Regex("<form([^>]*)onsubmit=[\"']([^\"']*)[\"']([^>]*)>", 2);

    private static final Regex FORM_FINDER = new Regex("<form([^>]*)>", 0);

    private static final Regex TABLE_FIXER = new Regex("(</td>)(.*?)(</td>|</tr>|</table>|<td[^>]*>)");
    /**
     * Remove code which redirects when no frames are detected.
     */
    private static final Regex FRAME_REDIRECT = new Regex("if\\s*\\(parent\\.frames\\.length\\s*==\\s*0\\)\\s*location.href\\s*=\\s*[\"']?game\\.php[\"']?;", 0);
    private static final Regex HEAD_TAG = new Regex("<head>");
    private final static String jsInjectCode = "" +
            "function customParseForm(form, action, method) { " +
            "   var inputs = form.getElementsByTagName('input');" +
            "   var data = action ? action : '';" +
            "   if(method.toUpperCase() == 'POST') {" +
            "       if(data.indexOf('.com/') > -1) data = data.replace('.com/', '.com/POST/');" +
            "       else data = 'POST/' + data;" +
            "   }" +
            "   var tobegin = (data.indexOf('?') == -1);" +
            "   for (var i = 0; i < inputs.length; i++) {" +
            "       var field = inputs[i];" +
            "       if(field.name && field.name==='totallyrealaction') continue; " +
            "       if(field.type == 'radio' && !field.checked) continue; " +
            "       if(field.type == 'checkbox' && !field.checked) continue; " +
            "       if (field.type != 'reset' && field.name) {" +
            "           data += (tobegin ? '?' : '&');" +
            "           tobegin = false;" +
            "           data += encodeURIComponent(field.name) + '=' + encodeURIComponent(field.value);" +
            "       }" +
            "   }" +
            "   var select = form.getElementsByTagName('select');" +
            "   for (var i = 0; i < select.length; i++) {" +
            "       var field = select[i];" +
            "       data += (tobegin ? '?' : '&');" +
            "       tobegin = false;" +
            "       data += encodeURIComponent(field.name) + '=' + encodeURIComponent(field.options[field.selectedIndex].value);" +
            "   }" +
            "   window.ANDROIDAPP.processFormData(data);" +
            "   return false;" +
            "}\n" +
            "function pop_query(caller, title, button, callback, def) { " +
            "    window.querycallback = callback;" +
            "    window.ANDROIDAPP.displayFormNumeric(title, button, \"javascript:window.querycallback(#VAL)\");" +
            "}\n";

    private static final Regex POPQUERY_SCRIPT = new Regex("<script[^>]*pop_query[^>]*></script>");

    // Regex to find contents of the <body> tag of any page
    private static final Regex PAGE_BODY = new Regex(
            "(<body[^>]*>)(.*?)(</body>)", 2);
    private static final Regex TYPE_EXTRACTION = new Regex("[&?]androiddisplay=([^&]*)", 1);
    private static final Regex TOP_PANE_REFRESH = new Regex("top.charpane.location(.href)?=[\"']?charpane.php[\"']?;");
    private final String url;
    private final WebModelType type;
    private String html;

    public WebModel(Session s, ServerReply text, WebModelType type) {
        super(s);

        Logger.log("WebModel", "Created for " + text.url);

        this.setHTML(text.html.replace("window.devicePixelRatio >= 2", "window.devicePixelRatio < 2"));
        this.url = text.url;
        this.type = type;
    }

    public WebModel(Session s, ServerReply text) {
        this(s, text, determineType(text));
    }

    private static WebModelType determineType(ServerReply text) {
        String specified_type = TYPE_EXTRACTION.extractSingle(text.url, "unspecified");
        for (WebModelType type : WebModelType.values()) {
            if (specified_type.equals(type.toString()))
                return type;
        }

        if (text.url.contains("desc_item.php")
                || text.url.contains("desc_effect.php")
                || text.url.contains("desc_skill.php"))
            return WebModelType.SMALL;
        return WebModelType.REGULAR;
    }



    private static String prepareHtml(String html) {
        html = fixItemsAndEffects(html);
        html = injectJavascript(html);
        html = doHacks(html);
        html = fixPaneReferences(html);
        return html;
    }

    private static String fixItemsAndEffects(String html) {
        // Replace item description javascript with working html links
        html = ITEM_DESC.replaceAll(html,
                "<a href=\"desc_item.php?whichitem=$1\">$0</a>");
        html = ITEM_WHICH_DESC.replaceAll(html,
                "<a href=\"desc_item.php?whichitem=$1&otherplayer=$2\">$0</a>");
        html = EFFECT_DESC.replaceAll(html,
                "<a href=\"desc_effect.php?whicheffect=$1\">$0</a>");

        return html;
    }

    private static String fixPaneReferences(String html) {
        html = FRAME_REDIRECT.replaceAll(html, "");
        html = TOP_PANE_REFRESH.replaceAll(html, "window.ANDROIDAPP.refreshStatsPane();");
        html = html.replace("top.mainpane.document", "document");
        html = html.replace("parent.mainpane", "window");
        return html;
    }

    private static String doHacks(String html) {
        /**
         * Hacks for account.php
         */
        /*
        //stop removing the submit button on account.php
		html = html.replace("document.write('<style type=\"text/css\">#submit {display: none; }</style>');", "");
		//remove all the blue "Saving..." text on account.php
		html = html.replace("<span class=\"saving\">Saving...</span>", "");
		//remove the fancy tab ajax calls on account.php; they do not have the proper cookie
		html = html.replace("$('#tabs li').click(changeTab);", "");
		*/
        return html;
    }

    private static String injectJavascript(String html) {
        for (String form : FORM_FINDER.extractAllSingle(html)) {
            String action = FORM_ACTION.extractSingle(form, "");
            String method = FORM_METHOD.extractSingle(form, "");
            String onsubmit = FORM_SUBMIT.extractSingle(form, "");

            //Process the onsubmit javascript into a prependable form
            String newsubmit = "customParseForm(this, '" + action + "', '" + method + "');";
            if (onsubmit.startsWith("return ")) {
                onsubmit = onsubmit.replace("return ", "");
                while (onsubmit.length() > 0 && onsubmit.endsWith(";")) {
                    onsubmit = onsubmit.substring(0, onsubmit.length() - 1);
                }
                newsubmit = "return ((" + onsubmit + ") && " + newsubmit + ")";
            } else if (onsubmit.length() > 0) {
                newsubmit = onsubmit + "; return " + newsubmit;
            } else {
                newsubmit = "return " + newsubmit;
            }

            String newform = form;
            newform = FORM_ACTION.replaceAll(newform, "<form$1$3>");
            newform = FORM_METHOD.replaceAll(newform, "<form$1$3>");
            newform = FORM_SUBMIT.replaceAll(newform, "<form$1$3>");
            newform = FORM_FINDER.replaceAll(newform, "<form$1 action=\"\" onsubmit=\"" + newsubmit + "\">");

            Logger.log("WebModel", form + " => " + newform);
            html = html.replace(form, newform);
        }

        html = TABLE_FIXER.replaceAll(html, "$1$3$2");
        html = HEAD_TAG.replaceAll(html, "$0 <script>" + jsInjectCode + "</script>");

        //pop_query(...) is replaced by an injected function to interact with android
        html = POPQUERY_SCRIPT.replaceAll(html, "");
        html = html.replace("Right-Click to Multi-Buy", "Long-Press to Multi-Buy");
        html = html.replace("Right-Click to Multi-Make", "Long-Press to Multi-Make");

        //Convert ajax post requests into the proper format
        html = html.replace("$.post(", "$.get(\"POST/\" + ");
        return html;
    }

    public String getURL() {
        return this.url;
    }

    public final String getHTML() {
        return this.html;
    }

    private void setHTML(String html) {
        this.html = prepareHtml(html);
    }

    public <E> E visitType(WebModelTypeVisitor<E> visitor) {
        return type.visit(visitor);
    }

    public boolean makeRequest(String url) {
        if (url == null || url.length() < 1) return false;

        String originalUrl = url;

        if (url.contains("totallyrealaction")) {
            System.out.println("Ignoring duplicate form request");
            return true;
        }

        if (url.contains("http://") || url.contains("https://")) {
            url = URL_FIND.extractSingle(url);
            if (url == null) {
                Logger.log("WebModel", "Unable to load url from " + originalUrl);
                return false;
            }
        }

        if (url.charAt(0) == '?') {
            String currentBase = URL_BASE_FIND.extractSingle(this.url);
            if (currentBase == null) currentBase = "main.php";
            url = currentBase + url;
        }

        Logger.log("WebModel", "Request started for " + url);
        Request req = new Request(url);
        this.makeRequest(req);
        return true;
    }

    public InputStream makeBlockingRequest(String url) {
        url = url.replace("http://www.kingdomofloathing.com/", "");
        url = url.replace("www.kingdomofloathing.com/", "");

        Request req = new Request(url);
        ServerReply result = this.makeBlockingRequest(req);

        String html_result;
        if (result == null) {
            Logger.log("WebModel", "[AJAX] Error loading " + url);
            html_result = "";
        } else {
            Logger.log("WebModel", "[AJAX] Loaded " + url + " : " + prepareHtml(result.html));
            html_result = result.html;
        }

        html_result = prepareHtml(html_result);

        try {
            return new ByteArrayInputStream(html_result.getBytes("UTF-8"));
        } catch (IOException e) {
            Logger.log("WebModel", "Unable to encode as UTF-8");
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };
        }
    }

    public enum WebModelType {
        REGULAR("regular") {
            public <E> E visit(WebModelTypeVisitor<E> visitor) {
                return visitor.forRegular();
            }
        }, SMALL("small") {
            public <E> E visit(WebModelTypeVisitor<E> visitor) {
                return visitor.forSmall();
            }
        }, RESULTS("results") {
            public <E> E visit(WebModelTypeVisitor<E> visitor) {
                return visitor.forResults();
            }
        };

        private final String value;

        WebModelType(String value) {
            this.value = value;
        }

        public abstract <E> E visit(WebModelTypeVisitor<E> visitor);

        @Override
        public String toString() {
            return value;
        }
    }

    public interface WebModelTypeVisitor<E> {
        E forRegular();

        E forSmall();

        E forResults();
    }
}

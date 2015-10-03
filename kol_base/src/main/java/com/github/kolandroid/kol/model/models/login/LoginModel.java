package com.github.kolandroid.kol.model.models.login;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.model.LinkedModel;
import com.github.kolandroid.kol.model.models.MessageModel;
import com.github.kolandroid.kol.model.models.WebModel;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.request.ResponseHandler;
import com.github.kolandroid.kol.request.SimulatedRequest;
import com.github.kolandroid.kol.request.SingleRequest;
import com.github.kolandroid.kol.session.Session;
import com.github.kolandroid.kol.util.Logger;
import com.github.kolandroid.kol.util.Regex;

import java.io.Serializable;
import java.util.ArrayList;

public class LoginModel extends LinkedModel<LoginStatus> {
    /**
     * Autogenerated by eclipse.
     */
    private static final long serialVersionUID = -8728194484440083488L;

    private static final Regex SERVER = new Regex("^appserver=(.*)$", 1);

    private static final Regex LOGIN_ID = new Regex(
            ".*/login.php\\?loginid=(.*)", 1);
    private static final Regex CHALLENGE = new Regex(
            "<input type=hidden name=challenge value=\"([^\"]*?)\">", 1);

    private final static Regex MAGIC_DETECTOR = new Regex("<b>Fresh Characters</b><ul>.*?</ul>", 0);
    private final static Regex MAGIC_CHARACTERS = new Regex("<li>(.*?)</li>", 1);
    private static final Regex FIND_MAGIC = new Regex("magic=(\\d+)%.*", 1);
    private final static Regex MAGIC_NAME = new Regex("<a[^>]*>([^<]*)</a>", 1);
    private final static Regex MAGIC_ACTION = new Regex("<a[^>]*href=[\"']([^\"'>]*)[\"']", 1);

    private final static Regex ANNOUNCEMENTS = new Regex("<b>Announcements:?</b>.*?<center>(<table>.*?</table>)</center>", 1);
    private final static Regex BLANK_TARGETS = new Regex("target=[\"']?_blank[\"']?");

    private final String loginId;
    private final String challenge;
    private final ArrayList<MagicLoginAction> magicCharacters;
    private final WebModel announcementsModel;
    private boolean stale;

    public LoginModel(Session s, ServerReply reply) {
        super(s);
        s.removeCookie("PHPSESSID");

        stale = false;
        loginId = LOGIN_ID.extractSingle(reply.url, "");
        challenge = CHALLENGE.extractSingle(reply.html, "");

        Logger.log("LoginModel", "Old cookie: " + s);
        s.addCookies(reply.cookie);
        Logger.log("LoginModel", "New cookie: " + s);

        magicCharacters = new ArrayList<>();
        String magic = MAGIC_DETECTOR.extractSingle(reply.html, "");
        if (!magic.equals("")) {
            for (String listItem : MAGIC_CHARACTERS.extractAllSingle(magic)) {
                magicCharacters.add(new MagicLoginAction(listItem));
            }
        }

        String announcements = ANNOUNCEMENTS.extractSingle(reply.html);
        if (announcements == null) {
            announcementsModel = null;
        } else {
            announcements = BLANK_TARGETS.replaceAll(announcements, "");
            Logger.log("LoginModel", "Announcements:" + announcements);
            announcementsModel = new WebModel(s, reply.substituteBody("http://www.kingdomofloathing.com/announcements.php", announcements), WebModel.WebModelType.EXTERNAL);
        }
    }

    public void login(final String username,
                      final PasswordHash hash) {
        String[] names = {"loginid", "loginname", "password",
                "loggingin", "challenge", "response", "secure"};
        String[] values = {loginId, username.replace(" ", "%20"), "", "Yup.", challenge,
                hash.completeChallenge(challenge), "1"};

        stale = true;

        Request login = new SingleRequest("login.php", names, values);
        this.makeRequest(login, new ResponseHandler() {
            @Override
            public void handle(Session session,
                               ServerReply response) {
                if (response == null) {
                    // Failure to login
                    Logger.log("LoginModel", "Failed to Login");
                    makeRequest(new SimulatedRequest(MessageModel.generateErrorMessage("Login Failed. Unable to connect to server.", MessageModel.ErrorType.SEVERE)));
                    return;
                }

                session.addCookies(response.cookie);
                Logger.log("LoginModel", "New game session: " + session);
                if (session.getCookie("PHPSESSID", "").equals("")) {
                    // First, display the updated login page
                    getGameHandler().handle(session, response);

                    if (response.html.contains("<b>That login page was really stale.  Try again, please?</b>")) {
                        // Login page stale
                        Logger.log("LoginModel", "Stale login page detected");
                        makeRequest(new SimulatedRequest(MessageModel.generateErrorMessage("That login page was really stale. Try again please?", MessageModel.ErrorType.SEVERE)));
                    } else {
                        // Failure to login
                        Logger.log("LoginModel", "Failed to Login");
                        makeRequest(new SimulatedRequest(MessageModel.generateErrorMessage("Login Failed. Bad Password.", MessageModel.ErrorType.SEVERE)));
                    }
                }

                notifyView(LoginStatus.SUCCESS);

                Request game = new Request("main.php");
                makeRequest(game);
            }
        });
    }

    public ArrayList<MagicLoginAction> getMagicCharacters() {
        return magicCharacters;
    }

    public boolean isStale() {
        return stale;
    }

    public void createAccount() {
        Request req = new Request("create.php?");
        this.makeRequest(req);
    }

    public WebModel getAnnouncementsModel() {
        return announcementsModel;
    }

    public class MagicLoginAction implements Serializable {
        private final String character;
        private final String url;

        public MagicLoginAction(String listItem) {
            character = MAGIC_NAME.extractSingle(listItem, "");
            url = MAGIC_ACTION.extractSingle(listItem, "");
        }

        public String getCharacter() {
            return character;
        }

        public void magicLogin() {
            Request r = new SingleRequest(url, new String[0], new String[0]);
            makeRequest(r, new ResponseHandler() {
                @Override
                public void handle(Session session, ServerReply response) {
                    if (response == null) {
                        // Failure to login
                        Logger.log("LoginModel", "Failed to Login");
                        makeRequest(new SimulatedRequest(MessageModel.generateErrorMessage("Login Failed. Unable to connect to server.", MessageModel.ErrorType.SEVERE)));
                        return;
                    }

                    session.addCookies(response.cookie);
                    Logger.log("LoginModel", "New game session: " + session);
                    if (session.getCookie("PHPSESSID", "").equals("")) {
                        // Display the new login screen if it exists
                        getGameHandler().handle(session, response);

                        if (response.html.contains("<b>That login page was really stale.  Try again, please?</b>")) {
                            // Login page stale
                            Logger.log("LoginModel", "Stale login page detected");
                            makeRequest(new SimulatedRequest(MessageModel.generateErrorMessage("That login page was really stale. Try again please?", MessageModel.ErrorType.SEVERE)));
                        } else {
                            // Failure to login
                            Logger.log("LoginModel", "Failed to Login");
                            makeRequest(new SimulatedRequest(MessageModel.generateErrorMessage("Login Failed. Perhaps you've set a password for this character?", MessageModel.ErrorType.SEVERE)));
                        }
                        return;
                    }

                    notifyView(LoginStatus.SUCCESS);

                    Request game = new Request("main.php");
                    makeRequest(game);
                }
            });
        }
    }
}

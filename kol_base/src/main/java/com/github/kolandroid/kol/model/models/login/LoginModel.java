package com.github.kolandroid.kol.model.models.login;

import com.github.kolandroid.kol.connection.ServerReply;
import com.github.kolandroid.kol.connection.Session;
import com.github.kolandroid.kol.gamehandler.ViewContext;
import com.github.kolandroid.kol.model.LinkedModel;
import com.github.kolandroid.kol.model.models.ErrorModel;
import com.github.kolandroid.kol.request.Request;
import com.github.kolandroid.kol.request.ResponseHandler;
import com.github.kolandroid.kol.request.SingleRequest;
import com.github.kolandroid.kol.util.Logger;
import com.github.kolandroid.kol.util.Regex;

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

    public LoginModel() {
        super(new Session());
    }

    public void login(final ViewContext context, final String username,
                      final PasswordHash hash) {
        Request req = new Request("login.php");
        this.makeRequest(req, new ResponseHandler() {
            @Override
            public void handle(Session session, ServerReply response) {
                Logger.log("LoginModel", "Received initial challenge");
                if (response == null) {
                    Logger.log("LoginModel", "Failed to Login");
                    ErrorModel.trigger(context, "Unable to access KoL servers. Do you have internet?", ErrorModel.ErrorType.SEVERE);
                    return;
                }

                String loginId = LOGIN_ID.extractSingle(response.url);
                String challenge = CHALLENGE.extractSingle(response.html);
                String server = SERVER.extractSingle(response.cookie);

                if (loginId == null || challenge == null || server == null) {
                    Logger.log("LoginModel", "Failed to Login");
                    ErrorModel.trigger(context, "Unable to access KoL servers. Do you have internet?", ErrorModel.ErrorType.SEVERE);
                    return;
                }

                session.setCookie(response.cookie);
                String[] names = {"loginid", "loginname", "password",
                        "loggingin", "challenge", "response", "secure"};
                String[] vals = {loginId, username, "", "Yup.", challenge,
                        hash.completeChallenge(challenge), "1"};

                Request login = new SingleRequest("login.php", names, vals);
                login.makeAsync(session, context.createLoadingContext(),
                        new ResponseHandler() {
                            @Override
                            public void handle(Session session,
                                               ServerReply response) {

                                Logger.log("LoginModel", "Received cookie: " + response.cookie);
                                if (!response.cookie.contains("PHPSESSID=")) {
                                    // Failure to login
                                    Logger.log("LoginModel", "Failed to Login");
                                    ErrorModel.trigger(context, "Login Failed. Bad Password.", ErrorModel.ErrorType.SEVERE);
                                    return;
                                }

                                notifyView(LoginStatus.SUCCESS);

                                session.setCookie(response.cookie);
                                Request game = new Request("main.php");
                                game.makeAsync(session,
                                        context.createLoadingContext(),
                                        context.getPrimaryRoute());
                            }
                        });
            }
        });
    }
}

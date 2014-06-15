package oauth_demo;

import java.io.*;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
import java.net.*;

import com.dropbox.core.*;

/**
 * This class provides methods which implement the Dropbox <i>Redirect</i> workflow:
 *
 * <ul>
 *    <li>{@code redirectClientStart} implements the <b>start</b> step.
 *    <li>{@code httpdHandleFinishAndSave} implements the <b>finish</b> step,
 *    saves the access token, and creates some sample files to show everything is working.
 * </ul>
 *
 * <p>{@code redirectClientStart} is called by the client.
 * {@code httpdHandleFinishAndSave} is called by the local HTTP server.
 *
 */
public class DropboxWorkflowRedirect {

    /**
     * CSRF token
     * @see CsrfTokenStore
     */
    public static CsrfTokenStore csrfTokenStore = new CsrfTokenStore();

    /** we don't ever instantiate this class but just call its methods statically */
    private DropboxWorkflowRedirect() {}

    /**
     * This method implements the <b>start</b> step of the Dropbox redirect workflow.
     *
     * <p>It performs the following steps:
     * <ol>
     *    <li>Create a {@code DbxAppInfo} object for the demo app.
     *    <li>Create a {@code DbxRequestConfig} object for the default locale.
     *    <li>Use these objects to create a {@code DbxWebAuth} object.
     *    <li>Call {@code DbxWebAuth.start()} to start the Oauth workflow.
     *    <li>Return the URL generated by {@code DbxWebAuthNoRedirect.start()} in a {@code DropboxStatus} object.
     * </ol>
     *
     * <p>The client will then take the user to the URL returned by {@code start()} so they can authorise with Dropbox.
     * The Dropbox website will generate an <b>authorisation code</b> which will be used by finish() to generate the access token.
     *
     * @return DropboxStatus object which is initialised with the URL to which the user must be redirected to authorise with Dropbox.
     *
     * @see <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxWebAuth.html">The DbxWebAuth class</a>
     * @see DropboxStatus
     */
    public static DropboxStatus redirectClientStart() {
        ConsoleLogger.debug("starting Dropbox authorisation (redirect mode)");
        ConsoleLogger.debug("creating DbxWebAuth client for app %s with key %s and secret %s", AppData.APP_NAME, AppData.APP_KEY, AppData.APP_SECRET);

        DbxAppInfo appInfo = new DbxAppInfo(AppData.APP_KEY, AppData.APP_SECRET);
        DbxRequestConfig config = new DbxRequestConfig(AppData.APP_NAME_VERSION, Locale.getDefault().toString());
        DbxWebAuth redirectClient = new DbxWebAuth(config, appInfo, HttpConfig.FINISH_URL.toString(), csrfTokenStore);

        ConsoleLogger.debug("created DbxWebAuth client, running start() to generate Dropbox authorisation URL");
        String authoriseUrl = redirectClient.start();

        ConsoleLogger.info("Dropbox authorisation start successful, got authorisation URL %s", authoriseUrl);
        return new DropboxStatus(301, DropboxStatus.makeUrl(authoriseUrl));
    }

    /**
     * This method implements the <b>finish</b> step of the Dropbox redirect workflow.
     *
     * <p>The client has taken the user to the URL returned by {@code start()} and they have authorised with Dropbox.
     * The Dropbox website has generated an <i>authorisation code</i> which is included in the redirect URL.
     *
     * <p>It uses the {@code client} object created in {@code noRedirectClientStart()} to perform the following steps:
     * <ol>
     *    <li>turn the query string in the URL into a Map for DbxWebAuth.finish()
     *    <li>Create a {@code DbxAppInfo} object for the demo app.
     *    <li>Create a {@code DbxRequestConfig} object for the default locale.
     *    <li>Use these objects to create a {@code DbxWebAuth} object.
     *    <li>Call {@code DbxWebAuth.finish()} to finish the Oauth workflow.
     *    <li>Call {@code AccessData.save()} to save the access token returned by {@code finish()}.
     *    <li>Call {@code DropboxTools.createSampleFiles()} to create some sample files in the Dropbox app folder.
     * </ol>
     *
     * @param uriPath the local URL to which the user has been redirected by Dropbox (not actually needed, just for info)
     * @param queryString the query string from that local URL (this will be used to get the session token and authorsiation code)
     *
     * @return AccessData object containing the access token returned by {@code finish()}
     *
     * @throws IOException if there is an error creating the token file
     *
     * @see <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxWebAuth.html">The DbxWebAuth class</a>
     * @see AccessData
     */
    public static DropboxStatus httpdHandleFinishAndSave(String uriPath, String queryString) throws IOException {
        try {
            ConsoleLogger.debug("finishing Dropbox authorisation (redirect mode), uri=%s, query='%s'", uriPath, queryString);

            Map<String, String[]> queryParams = new LinkedHashMap<String, String[]>();
            for (String query : queryString.split("&")) {
                int idx = query.indexOf("=");
                String queryParam = URLDecoder.decode(query.substring(0, idx), "UTF-8");
                String queryValue = URLDecoder.decode(query.substring(idx + 1), "UTF-8");
                // the URL won't ever have the same query multiple times
                queryParams.put(queryParam, new String[]{queryValue});
                ConsoleLogger.debug("URL query parameter %s='%s'", queryParam, queryValue);
            }

            DbxAppInfo appInfo = new DbxAppInfo(AppData.APP_KEY, AppData.APP_SECRET);
            DbxRequestConfig config = new DbxRequestConfig(AppData.APP_NAME_VERSION, Locale.getDefault().toString());
            DbxWebAuth redirectClient = new DbxWebAuth(config, appInfo, HttpConfig.FINISH_URL.toString(), csrfTokenStore);

            DbxAuthFinish authFinish = redirectClient.finish(queryParams);
            csrfTokenStore.clear();
            AccessData accessData = new AccessData(authFinish.accessToken, authFinish.userId, "created using Java dropbox.client.DbxWebAuth()");
            accessData.save();
            DropboxTools.createSampleFiles();
            return new DropboxStatus(200, DropboxStatus.makePage(
                        "<h1>Congratulations!</h1><p>The Dropbox access token was created successfully.<p>You may return to your client."));
        }
        catch (DbxWebAuth.BadRequestException ex) {
            ConsoleLogger.error("Bad request on finish, error=" + ex.getMessage());
            csrfTokenStore.clear();
            return new DropboxStatus(400, "Bad request");
        }
        catch (DbxWebAuth.BadStateException ex) {
            // Send them back to the start of the auth flow.
            ConsoleLogger.error("Bad state on finish, error=" + ex.getMessage());
            csrfTokenStore.clear();
            return new DropboxStatus(301, "Bad request", HttpConfig.START_URL);
        }
        catch (DbxWebAuth.CsrfException ex) {
            ConsoleLogger.error("CSRF mismatch on finish, error=" + ex.getMessage());
            csrfTokenStore.clear();
            return new DropboxStatus(400, "Bad request");
        }
        catch (DbxWebAuth.NotApprovedException ex) {
            // When Dropbox asked "Do you want to allow this app to access your Dropbox account?", the user clicked "No".
            ConsoleLogger.error("user declined Dropbox authorisation");
            csrfTokenStore.clear();
            return new DropboxStatus(301, "Bad request", HttpConfig.START_URL);
        }
        catch (DbxWebAuth.ProviderException ex) {
            ConsoleLogger.error("Dropbox authorisation failed, error=" + ex.getMessage());
            csrfTokenStore.clear();
            return new DropboxStatus(503, "Service unavailable");
       }
       catch (DbxException ex) {
            ConsoleLogger.error("Dropbox authorisation failed, error=" + ex.getMessage());
            csrfTokenStore.clear();
            return new DropboxStatus(503, "Service unavailable");
       }
    }

}

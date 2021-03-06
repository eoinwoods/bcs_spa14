package oauth_demo;

import java.util.Locale;
import java.util.Iterator;
import java.io.*;

import com.dropbox.core.*;

/**
 * Static methods for performing some Dropbox actions (creating or printing files etc).
 *
 * <p><b>Note</b>.
 * All paths in these methods are relative to the Dropbox app directory (eg
 * {@code ~/Dropbox/apps/bcs_spa_2014)}.
 * Dropbox paths always use forward-slashes, even on Windows.
 * <p>So, for example, to print the contents of {@code ~/Dropbox/apps/bcs_spa_2014/myfolder},
 * specify {@code myfolder} as a parameter ({@code /myfolder} will also work).
 *
 */
public class DropboxTools {

    /**
     * This file is created in the Dropbox app folder once the client has authorised with Dropbox.
     * It contains information on the Dropbox user account.
     * It is a Dropbox file path so uses forward-slash as directory separator, even on Windows.
     */
    static String DB_ACCOUNT_INFO_FILE = "/account_info.java.txt";

    /**
     * This folder is created in the Dropbox app folder once the client has authorised with Dropbox.
     * It is a Dropbox file path so uses forward-slash as directory separator, even on Windows.
     */
    static String DB_REVIEW_DIRECTORY = "/oauth_session_java";

    /**
     * This file is created in the Dropbox app folder once the client has authorised with Dropbox.
     * It is a Dropbox file path so uses forward-slash as directory separator, even on Windows.
     */
    static String DB_REVIEW_FILE = String.format("%s/oauth_session_review.java.md", DB_REVIEW_DIRECTORY);

    /** we don't ever instantiate this class but just call its methods statically */
    private DropboxTools() {};

    /**
     * Print the contents of a directory in the Dropbox app folder (using Dropbox calls, not file I/O).
     *
     * @param directory Dropbox path of the directory to print
     *
     * @throws IOException if there is an error creating the file
     * @throws DbxException if a Dropbox error occurs
     */
    public static void printDirectoryContents(String directory) throws IOException, DbxException {
        ConsoleLogger.debug("requested directory is %s", directory);
        String output = "\nCONTENTS OF DROPBOX DIRECTORY '"+directory+"': ";
        // EXERCISE:
        //  - create a Dropbox client
        //    hint: a helper function to create the Dropbox client is at the end of this file
        //  - create a Dropbox metada object which you will use to get the directory contents
        //    hint: the Dropbox class is DbxEntry.WithChildren
        //    @see http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxEntry.WithChildren.html
// TODO ==> INSERT CODE HERE <==
        if (metadata == null) {
            output += "\n<none>";
        }
        else {
            // EXERCISE:
            //  - create a Dropbox metada iterator object which you will use to iterate over the sub-directories of this one
            //    hint: you need to iterate over metadata.children (which are all of type DbxEntry)
            //    @see http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxEntry.WithChildren.html
// TODO ==> INSERT CODE HERE <==
            while (directoryIterator.hasNext()) {
                DbxEntry nextEntry = directoryIterator.next();
                String line = "";
                if (nextEntry instanceof DbxEntry.Folder) {
                    DbxEntry.Folder folderEntry = (DbxEntry.Folder)nextEntry;
                    output += String.format("\n %30s   %s%s", " ", folderEntry.name, "/");
                }
            }
            // EXERCISE:
            //  - create a Dropbox metada iterator object which you will use to iterate over the files in this directory
            //    hint: same as above
            directoryIterator = metadata.children.iterator();
            // SPA14_OAUTH_FINISH
            while (directoryIterator.hasNext()) {
                DbxEntry nextEntry = directoryIterator.next();
                String line = "";
                if (nextEntry instanceof DbxEntry.File) {
                    DbxEntry.File fileEntry = (DbxEntry.File)nextEntry;
                    output += String.format("\n %30s   %s (%s)", fileEntry.lastModified, fileEntry.name, fileEntry.humanSize);
                }
            }
        }
        System.out.println(output);
    }

    /**
     * Print the contents of a file in the Dropbox app folder (using Dropbox calls, not file I/O).
     *
     * @param printpath Dropbox path of the file to print
     *
     * @throws IOException if there is an error creating temporary files
     * @throws DbxException if a Dropbox error occurs
     */
    public static void printFile(String printpath) throws IOException, DbxException {
        // EXERCISE:
        //  - create a Dropbox client
        //    hint: a helper function to create the Dropbox client is at the end of this file
        //  - get the Dropbox file at printpath using client.getFile()
        //    hint: you need to create a temporary file into which you will download the Dropbox file
        //    @see http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html#getFile(java.lang.String, java.lang.String, java.io.OutputStream)
// TODO ==> INSERT CODE HERE <==

        ConsoleLogger.debug("saved file contents to temporary file '%s'", tmpfile.getPath());
        BufferedReader br = new BufferedReader(new FileReader(tmpfile.getPath()));
        int lineCount = 1;
        String line = null;
        System.out.println("\nCONTENTS OF DROPBOX FILE '"+printpath+"': ");
        while ((line = br.readLine()) != null) {
            String printLine = String.format("%3d: %s", lineCount, line);
            System.out.println(printLine);
            lineCount++;
        }
        br.close();
    }

    /**
     * Create a file in the Dropbox app folder (using Dropbox calls, not file I/O).
     *
     * @param createpath Dropbox path of the file to create (this will be created under the Dropbox app folder)
     *
     * @throws IOException if there is an error creating temporary files
     * @throws DbxException if a Dropbox error occurs
     */
    public static void createFile(String createpath) throws IOException, DbxException {
        String tmpFilePath = File.createTempFile(AppData.APP_NAME, "txt").getPath();
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFilePath));
        System.out.println("Enter file lines, blank line to finish");
        int lineCount = 0;
        while(true) {
            String line = System.console().readLine();
            if (line.length() == 0) { break; }
            bw.write(line+"\n");
            lineCount++;
        }
        bw.close();
        ConsoleLogger.debug("wrote %d lines to temporary file '%s'", lineCount, tmpFilePath);
        // EXERCISE:
        //  - create a Dropbox client
        //    hint: a helper function to create the Dropbox client is at the end of this file
        //  - upload the temporary file you created above into the Dropbox location createpath
        //    hint: use Dropbox force mode to overwrite the file if it exists
// TODO ==> INSERT CODE HERE <==
        ConsoleLogger.debug("copied temporary file '%s' to Dropbox path '%s'", tmpFilePath, createpath);
        f.delete(); 
    }

    /**
     * Create a file in the Dropbox app folder (using Dropbox calls, not file I/O).
     *
     * @param deletepath Dropbox path of the file to create (this will be created under the Dropbox app folder)
     *
     * @throws IOException if there is an error creating temporary files
     * @throws DbxException if a Dropbox error occurs
     */
    public static void deleteFile(String deletepath) throws IOException, DbxException {
        createDbxClient();
        client.delete(deletepath);
    }

    /**
     * Create some sample files in the Dropbox app folder (using Dropbox calls, not file I/O).
     *
     * <p>The method creates the following files:
     * <ul>
     *    <li>{@code DB_ACCOUNT_INFO_FILE} - contains information on the Dropbox user account
     *    <li>{@code DB_REVIEW_DIRECTORY} - a directory for DB_REVIEW_FILE
     *    <li>{@code DB_REVIEW_FILE} - contains a review of the session
     * </ul>
     *
     * @throws IOException if there is an error creating temporary files
     * @throws DbxException if a Dropbox error occurs
     */
    public static void createSampleFiles() throws IOException, DbxException {
        createDbxClient();
        DbxAccountInfo clientInfo = client.getAccountInfo();
        // create account info file
        String tmpFilePath = File.createTempFile(AppData.APP_NAME, "txt").getPath();
        BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFilePath));
        bw.write("DROPBOX ACCOUNT INFORMATION\n");
        bw.write(String.format("country = %s\n", clientInfo.country));
        bw.write(String.format("displayName = %s\n", clientInfo.displayName));
        bw.write(String.format("referralLink = %s\n", clientInfo.referralLink));
        DbxAccountInfo.Quota quota = clientInfo.quota;
        bw.write(String.format("quota normal = %d, shared=%d, total=%d\n", quota.normal, quota.shared, quota.total));
        bw.close();
        ConsoleLogger.debug("wrote temporary account info file '%s'", tmpFilePath);
        File f = new File(tmpFilePath);
        ConsoleLogger.debug("uploading temporary account info file '%s' to Dropbox path '%s'", tmpFilePath, DB_ACCOUNT_INFO_FILE);
        client.uploadFile(DB_ACCOUNT_INFO_FILE, DbxWriteMode.force(), f.length(), new FileInputStream(f));
        ConsoleLogger.info("uploaded account info file '%s' to Dropbox path '%s'", tmpFilePath, DB_ACCOUNT_INFO_FILE);
        f.delete();
        // create Dropbox review directory if it doesn't already exist
        DbxEntry.Folder folderMetatada = client.createFolder(DB_REVIEW_DIRECTORY);
        if (folderMetatada == null) {
            ConsoleLogger.debug("review folder %s already exists", DB_REVIEW_DIRECTORY);
        }
        else {
            ConsoleLogger.debug("created review folder %s", DB_REVIEW_DIRECTORY);
        }
        // save file containing session review
        tmpFilePath = File.createTempFile(AppData.APP_NAME, "txt").getPath();
        bw = new BufferedWriter(new FileWriter(tmpFilePath));
        bw.write("# KEEPING PASSWORDS PRIVATE WITH OAUTH\n");
        bw.write("An interesting and thought-provoking session\n");
        bw.write("The presenters were top-notch and I learned a lot!\n");
        bw.close();
        ConsoleLogger.debug("wrote temporary review file '%s'", tmpFilePath);
        f = new File(tmpFilePath);
        ConsoleLogger.debug("uploading temporary review file '%s' to Dropbox path '%s'", tmpFilePath, DB_REVIEW_FILE);
        client.uploadFile(DB_REVIEW_FILE, DbxWriteMode.force(), f.length(), new FileInputStream(f));
        ConsoleLogger.info("uploaded review file '%s' to Dropbox path '%s'", tmpFilePath, DB_REVIEW_FILE);
    }

    /**
     * a Dropbox Dbx client variable used to perform Dropbox file actions.
     *
     * <p>Before using this client you must call {@code createDbxClient()}.
     *
     * @see <a href="http://dropbox.github.io/dropbox-sdk-java/api-docs/v1.7.x/com/dropbox/core/DbxClient.html">DbxClient reference</a>
     */
    public static DbxClient client = null;

    /**
     * Factory method to create a Dropbox DbxClient client.
     *
     * <p>The method performs the following steps:
     * <ol>
     * <li>Load the access token using {@code AccessData.load()}.
     * <li>Create a {@code DbxRequestConfig} object for the default locale.
     * <li>Create the {@code DbxClient} object.
     * </ol>
     *
     * @throws IOException if something bad has happened
     *
     */
    public static void createDbxClient() throws IOException {
        // EXERCISE:
        // - create a Dropbox OAuth client object with which to make Dropbox calls
        //   hint: class is DbxClient()
        // - this needs to be supplied with the access token
        //   hint: create an AccessData() object and load() it from file
        // - it also needs to be supplied with a Dropbox DbxRequestConfig() object
        //   hint: use the app name/version from AppData and the default Locale
        // - return this client to the caller
        // note how you have not had to provide any user credentials during this process!
// TODO ==> INSERT CODE HERE <==

        ConsoleLogger.debug("created DbxClient() object for config %s, access token %s", AppData.APP_NAME_VERSION, accessData.accessToken);
    }
}


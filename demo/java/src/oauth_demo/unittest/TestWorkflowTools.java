package oauth_demo;

import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.*;

import java.util.logging.Level;
import java.util.Locale;
import java.io.*;

import com.dropbox.core.*;

public class TestWorkflowTools {

    @Rule public TestName currentTestName = new TestName();

    /*
     =============
     TEST FIXTURES
     =============
    */
    @Before
    public void setUp() throws IOException {
        ConsoleLogger.setLevel(Level.SEVERE);
        commonTest = new CommonTest();
        commonTest.deleteOauthFiles();
        commonTestDropbox = new CommonTestDropbox();
        commonTestDropbox.createTestTokenFile(currentTestName.getMethodName());
    }

    @After
    public void tearDown() {
        /* comment this out if you want to see what files have been created during the tests */
        commonTest.deleteOauthFiles();
    }

    /*
     ======================
     TESTS FOR DropboxTools
     ======================
    */

    @Test
    public void dbCreateTextFile() throws IOException, DbxException {
        dropboxFile = commonTestDropbox.createTestDropboxFile(6, currentTestName.getMethodName());
        String actualContents = commonTestDropbox.getDropboxFileContents(dropboxFile.filepath);
        assertEquals(String.format("actual contents '%s' do not equal expected contents '%s'",
                actualContents, dropboxFile.fileContentsString),
                dropboxFile.fileContentsString, actualContents);
        commonTestDropbox.deleteDropboxFile(dropboxFile.filepath);
    }

    @Test
    public void printDirectoryContents() throws IOException, DbxException {
        dropboxFile = commonTestDropbox.createTestDropboxFile(4, currentTestName.getMethodName());
        String expectedString = dropboxFile.filepath.substring(1);
        try {
            commonTest.captureStdout(true);
            DropboxTools.printDirectoryContents("/");
            String stdoutContents = commonTest.getStdoutContents();
            commonTest.captureStdout(false);
            assertTrue(String.format("stdout '%s' does not contain path '%s'", stdoutContents, expectedString),
                    stdoutContents.contains(expectedString));
        }
        finally {
            commonTest.captureStdout(false);
            commonTestDropbox.deleteDropboxFile(dropboxFile.filepath);
        }
    }

    @Test
    public void dbPrintFile() throws IOException, DbxException {
        dropboxFile = commonTestDropbox.createTestDropboxFile(6, currentTestName.getMethodName());
        String actualContents = commonTestDropbox.getDropboxFileContents(dropboxFile.filepath);
        try {
            commonTest.captureStdout(true);
            DropboxTools.printFile(dropboxFile.filepath);
            String stdoutContents = commonTest.getStdoutContents();
            commonTest.captureStdout(false);
            for (int i = 0; i < dropboxFile.fileContentsArray.length; i++){
                String expectedLine = dropboxFile.fileContentsArray[i];
                assertTrue(String.format("stdout '%s' does not contain line '%s'", stdoutContents, expectedLine),
                        stdoutContents.contains(expectedLine));
            }
        }
        finally {
            commonTest.captureStdout(false);
            commonTestDropbox.deleteDropboxFile(dropboxFile.filepath);
        }
    }

    @Test
    public void dbDeleteFile() throws IOException, DbxException {
        dropboxFile = commonTestDropbox.createTestDropboxFile(6, currentTestName.getMethodName());
        assertTrue(String.format("failed to create test dropbox file %s", dropboxFile.filepath),
                commonTestDropbox.dropboxPathExists(dropboxFile.filepath));
        commonTestDropbox.deleteDropboxFile(dropboxFile.filepath);
        assertFalse(String.format("failed to delete test dropbox file %s", dropboxFile.filepath),
                commonTestDropbox.dropboxPathExists(dropboxFile.filepath));
    }

    private CommonTest commonTest;
    private CommonTestDropbox commonTestDropbox;
    private CommonTestDropbox.DropboxFile dropboxFile;

    private PrintStream saveStdout;
    private ByteArrayOutputStream baos;

}


"""
common_oauth.py: configuration data and common classes and functions for the Dropbox Python OAuth API demo
"""

import os, os.path, sys
import time, calendar, datetime
import json, webbrowser

if sys.platform == 'darwin':
    BROWSER_NAME = 'safari'
elif sys.platform == 'win32':
    BROWSER_NAME = 'windows-default'
elif sys.platform == 'linux2':
    BROWSER_NAME = 'firefox'
else:
    BROWSER_NAME = 'mozilla'
    """
    Which web browser is used to authenticate with Dropbox.
    Valid browser names include 'windows-default', 'mozilla', 'macosx', 'safari', 'opera', 'konqueror' etc.
    """

BROWSER_NEW_WINDOW = False
"""set this to True to open a new browser window, False to try and open a new tab"""

DEMO_DIRECTORY = os.path.dirname(os.path.realpath(__file__))
"""
Pathname of demo directory (the directory containing this file).
You should not need to change this, but if this does not work, you can enter it explicitly below.
See U{http://stackoverflow.com/questions/4934806/python-how-to-find-scripts-directory}
"""
# DEMO_DIRECTORY = '/path/to/this/script'

FILES_DIRECTORY = os.path.join(DEMO_DIRECTORY, 'files')
"""Directory in which access token and session files are saved (DEMO_DIRECTORY/files)."""

DOC_DIRECTORY = os.path.join(DEMO_DIRECTORY, 'doc')
"""Directory in which documenation files are saved (DEMO_DIRECTORY/doc)."""

import logging
logger = logging.getLogger(__name__)
"""
used to log error, info and debug messages, for example::
    logger = CO.logger; logger('message')
"""
if os.environ.get('OAUTH_DEBUG') is None:
    logger.setLevel(logging.INFO)
else:
    logger.setLevel(logging.DEBUG)
if logger.handlers == []:
    handler = logging.StreamHandler()
    handler.setFormatter(logging.Formatter('%(levelname)s: %(message)s (%(filename)s[%(lineno)d] %(asctime)s)', '%H:%M:%S'))
    logger.addHandler(handler)

def time_now():
    """
    return human-readable representation of the current date and time
    @rtype:  string
    @return: human-readable representation of the current date and time
    """
    ts = datetime.datetime.now().timetuple()
    return '{wday} {day} {month} {year} {hour}:{minute:0>2d}:{second:0>2d} UTC'.format(
            year=ts.tm_year, month=calendar.month_name[ts.tm_mon],
            day=ts.tm_mday, wday=calendar.day_name[ts.tm_wday],
            hour=ts.tm_hour, minute=ts.tm_min, second=ts.tm_sec)

def wait_for_file(file_path, message_interval=5, timeout=0):
    """
    wait (indefinitely or with timeout) until file_path exists
    @rtype:  void
    @type file_path: string
    @param file_path: full path of file to wait for
    @type message_interval: int
    @param message_interval: interval between printing wait messages
    @type timeout: int
    @param timeout: if non-zero, give up after waiting this number of seconds
    """

    def time_sec(): return int(time.time())

    sleep_time = 1 # second
    logger.info('waiting for file %s...' % (file_path,))
    time_of_first_check = time_of_last_check = time_sec()

    while True:
        if os.path.isfile(file_path):
            logger.info('file %s present' % (file_path,))
            return True
        if (timeout > 0):
            if (time_sec() - time_of_first_check) > timeout:
                logger.info('wait for file %s timed out after %d seconds' % (file_path, timeout,))
                return False
        if (time_sec() - time_of_last_check) > message_interval:
            logger.info('still waiting for file...')
            time_of_last_check = time_sec()
        time.sleep(sleep_time)

class AppData(object):
    """
    Dropbox OAuth application data

    This class defines the Dropbox app key and secret for the demo app.
    It also defines some other useful information and URLs.
    See U{https://www.dropbox.com/developers/apps}
    """

    # EXERCISE:
    #  - initialise APP_KEY and APP_SECRET for the BCS SPA app
    #    hint: see main project README
    # SPA14_OAUTH_START
    APP_KEY = '3i8xil7ewl5d4el'
    """Dropbox app key for the demo app"""
    APP_SECRET = '0cf79q7jwrp5sjx' 
    """Dropbox app secret for the demo app"""
    # SPA14_OAUTH_FINISH

    logger.debug('Dropbox app key is "{key}", app secret is "{secret}"'.format(key=APP_KEY, secret=APP_SECRET))

    APP_NAME = 'bcs_spa_oauth_demo'
    """Dropbox app name for the demo app (used in Dropbox calls)"""
    APP_WEBSITE = 'https://www.dropbox.com/developers/apps/info/3i8xil7ewl5d4el'
    """Dropbox developer website for the app."""

class AccessData(object):
    """
    Dropbox OAuth access data (access token and user id)

    This class is used to store and manage the Dropbox Oauth access data.
    It includes methods to save the information to disk, and load previously-saved access data.
    It also includes methods to check for the presence of the token file on disk.
    """

    ACCESS_TOKEN_FILE = os.path.join(FILES_DIRECTORY, 'access_token.json')
    """Full pathname of file containing details of the Dropbox access token"""

    logger.debug('Dropbox access token file is "{file}"'.format(file=ACCESS_TOKEN_FILE))

    def __init__(self, save_message=''):
        """
        Constructor
        @type save_message: string
        @param save_message: save_message message to be written to JSON file containing access token
        """
        # access token data - access token, user id and a message which is saved to file for info
        self.access_token = None
        self.user_id = None
        self.save_message = save_message

    def load(self):
        """Load and return access data from access token file (which must exist) """
        # EXERCISE:
        #  * load JSON data from access token file AccessData.ACCESS_TOKEN_FILE
        #  * hint: use json.load(open(...))
        #  * assign values to self.access_token, self.user_id, self.save_message from this data
        #  * you should raise an error if self.access_token or self.user_id is missing from the file
        # SPA14_OAUTH_START
        config = json.load(open(AccessData.ACCESS_TOKEN_FILE, 'r'))
        self.access_token = config.get('access_token', None).encode('ascii','ignore')
        self.user_id = config.get('user_id', None).encode('ascii','ignore')
        self.save_message = config.get('message', '')
        if self.access_token is None:
            raise KeyError('access token not present in file %s' % (AccessData.ACCESS_TOKEN_FILE))
        elif self.user_id is None:
            raise KeyError('user id not present in file %s' % (AccessData.ACCESS_TOKEN_FILE))
        # SPA14_OAUTH_FINISH

        logger.debug('loaded access data from token file "{file}"'.format(file=AccessData.ACCESS_TOKEN_FILE))
        logger.debug('loaded token="{token}", secret="{user_id}", message="{message}"'.format(
            token=self.access_token, user_id=self.user_id, message=self.save_message))

    def save(self):
        """Save given access data to access token file (which will be overwritten) """
        # EXERCISE:
        #  - save self.access_token, self.user_id, self.save_message to access token file AccessData.ACCESS_TOKEN_FILE
        #    @see http://stackoverflow.com/questions/12309269/write-json-data-to-file-in-python
        # SPA14_OAUTH_START
        with open(AccessData.ACCESS_TOKEN_FILE, 'w') as fp:
            json.dump({'access_token': self.access_token, 'user_id': self.user_id,
                'message': self.save_message, 'creation_time': time_now()}, fp, indent=4)
            fp.write('\n')
        # SPA14_OAUTH_FINISH

        logger.debug('saved access token in file %s' % (AccessData.ACCESS_TOKEN_FILE))

    @staticmethod
    def access_token_file_exists(silent=False):
        """
        Check whether access token data exists (file has previously been saved with access token)
        @type silent: string
        @param silent: don't log debug messages if True
        """
        if os.path.isfile(AccessData.ACCESS_TOKEN_FILE):
            if not silent: logger.debug('access token file %s exists' % (AccessData.ACCESS_TOKEN_FILE))
            return True
        else:
            if not silent: logger.debug('access token file %s does not exist' % (AccessData.ACCESS_TOKEN_FILE))
            return False

    @staticmethod
    def print_access_token_file():
        """Print contents of access token file"""
        if AccessData.access_token_file_exists():
            print 'CONTENTS OF %s:' % (AccessData.ACCESS_TOKEN_FILE,)
            with open(AccessData.ACCESS_TOKEN_FILE) as f:
                for line in f.readlines(): print line.strip('\n')
        else:
            print 'token file %s does not exist' % (AccessData.ACCESS_TOKEN_FILE,)

    @staticmethod
    def delete_access_token_file():
        """Delete the access token file and other control files"""
        if os.path.isfile(AccessData.ACCESS_TOKEN_FILE):
            os.remove(AccessData.ACCESS_TOKEN_FILE)
            logger.info('deleted file %s' % (AccessData.ACCESS_TOKEN_FILE))

    @staticmethod
    def wait_for_access_token_file(message_interval=5):
        """
        Wait indefinitely until the access token file is found
        @type message_interval: int
        @param message_interval: interval between printing wait messages
        """

        wait_for_file(AccessData.ACCESS_TOKEN_FILE, message_interval)

class HttpServices(object):
    """
    HTTP data and service functions

    This class defines URLs and associated data needed to interact with Dropbox.
    It includes methods to manage the HTTPD session and to open a web browser for the user.

    B{THIS DATA MUST MATCH THE INFORMATION ENTERED AT THE DROPBOX DEVELOPER CONSOLE FOR THE APP.}
    """
    def __init__(self):
        self.OAUTH_HTTPD_SERVER='localhost'
        """Hostname for the local HTTPD server"""
        self.OAUTH_HTTPD_PORT = 55510
        """Port the local HTTPD server is listening on"""
        self.OAUTH_CSRF_SESSION_KEY = 'csrf_token_session_key'
        """Index into the session structure which contains the session key"""
        self.OAUTH_HOME_URL = 'http://{server}:{port}/home'.format(server=self.OAUTH_HTTPD_SERVER, port=self.OAUTH_HTTPD_PORT)
        """Name of the home page for the local HTTPD server"""
        self.OAUTH_FINISH_PAGE = 'dropbox-auth-finish'
        """Name of the finish page for the local HTTPD server"""
        self.OAUTH_FINISH_URL = 'http://{server}:{port}/{page}'.format(
                server=self.OAUTH_HTTPD_SERVER, port=self.OAUTH_HTTPD_PORT, page=self.OAUTH_FINISH_PAGE)
        """URL of the finish page for the local HTTPD server"""
        logger.debug('Dropbox finish URL is "{finish_url}"'.format(finish_url=self.OAUTH_FINISH_URL))

        self.httpd_session = {}
        """Session variable for use by httpd"""

        self.HTTPD_SESSION_FILE = os.path.join(FILES_DIRECTORY, 'httpd_session.json')
        """Full pathname of the JSON file in which the session token is stored"""
        self.HTTPD_SESSION_FILE_EXPIRED = os.path.join(FILES_DIRECTORY, 'httpd_session.EXPIRED.json')
        """Once the session token is cleared, the session file is renamed to this pathname"""
        self.HTTPD_LATEST_URL_FILE = os.path.join(FILES_DIRECTORY, 'httpd_latest_url.log')
        """Path to file which contains the latest URL requested of the HTTP server"""

    def save_httpd_session(self):
        """Save HTTPD session data to session data file (which will be overwritten)"""
        # EXPLANATION:
        #  The Dropbox redirect flow generates a token during the start() method,
        #   which you must supply when calling the finish() method to prevent CSRF attacks
        #   @see https://www.dropbox.com/developers/core/docs/python#DropboxOAuth2Flow
        # EXERCISE:
        #  - save self.httpd_session to session data file self.HTTPD_SESSION_FILE
        #    @see http://stackoverflow.com/questions/12309269/write-json-data-to-file-in-python
        # SPA14_OAUTH_START
        with open(self.HTTPD_SESSION_FILE, 'w') as fp:
            json.dump(self.httpd_session, fp, indent=4)
            fp.write('\n')
        # SPA14_OAUTH_FINISH

        logger.debug('saved HTTPD session data "{httpd_session}" in file "{session_file}"'.format(
            httpd_session=str(self.httpd_session), session_file=self.HTTPD_SESSION_FILE))

    def load_httpd_session(self, expire_session=True):
        """
        Load and return HTTPD session data from session data file (which must exist)
        @type expire_session: boolean
        @param expire_session: if True, also expire the session file (rename it to ..._EXPIRED)
        """
        # EXERCISE:
        #  - load self.httpd_session from session data file self.HTTPD_SESSION_FILE
        #  - if expire_session is True, expire the session as well so that the session token is not longer usable
        #    hint: use json.load(open(...))
        #    hint: use the expire function defined just below
        # SPA14_OAUTH_START
        self.httpd_session = json.load(open(self.HTTPD_SESSION_FILE, 'r'))
        if expire_session:
            self.expire_httpd_session()
        # SPA14_OAUTH_FINISH

        logger.debug('loaded HTTPD session data "{httpd_session}" from file "{session_file}"'.format(
            httpd_session=str(self.httpd_session), session_file=self.HTTPD_SESSION_FILE))

    def expire_httpd_session(self):
        """Expire the sesssion by renaming the session file to HTTPD_SESSION_FILE_EXPIRED"""
        if os.path.isfile(self.HTTPD_SESSION_FILE):
            logger.debug('expiring session by renaming session file to %s' % (self.HTTPD_SESSION_FILE_EXPIRED))
            os.rename(self.HTTPD_SESSION_FILE, self.HTTPD_SESSION_FILE_EXPIRED)

    def delete_httpd_session_file(self):
        """Delete the session files"""
        for filepath in (self.HTTPD_SESSION_FILE, self.HTTPD_SESSION_FILE_EXPIRED):
            if os.path.isfile(filepath):
                os.remove(filepath)
                logger.info('deleted file %s' % (filepath))

    def save_latest_url(self, url):
        """
        Save the latest URL requested of HTTP server to latest URL file.
        This is used in testing to see if a URL has been requested of the server
        @type url: string
        @param url: this URL will be saved in the latest URL file
        """
        with open(self.HTTPD_LATEST_URL_FILE, 'w') as fp:
            fp.write(url + '\n')
            logger.debug('wrote URL %s to latest URL file %s' % (url, self.HTTPD_LATEST_URL_FILE))

    def load_latest_url(self):
        """Load latest URL requested of HTTP server from latest URL file"""
        try:
            fp =  open(self.HTTPD_LATEST_URL_FILE)
            latest_url = fp.read()
            fp.close()
            return latest_url
        except:
            return ''

    def clear_latest_url(self):
        """Clear latest URL requested of HTTP server so next call returns empty string (delete latest URL file)"""
        if os.path.isfile(self.HTTPD_LATEST_URL_FILE):
            os.remove(self.HTTPD_LATEST_URL_FILE)
            logger.debug('deleted latest URL file %s' % (self.HTTPD_LATEST_URL_FILE))

    def wait_for_latest_url_file(self, timeout=0):
        """
        Wait for latest URL file to appear, return True when found or optionally timeout and return False.
        This is used in testing to see if a URL has been requested of the server
        @type timeout: int
        @param timeout: if non-zero, give up after waiting this number of seconds
        """
        return wait_for_file(self.HTTPD_LATEST_URL_FILE, timeout=timeout)

    @staticmethod
    def open_browser_window(url):
        """
        Open a window in the configured web browser
        @type url: string
        @param url: URL to open
        """
        logger.debug('about to open url "{url}" in browser "{browser}"'.format(url=url, browser=BROWSER_NAME))
        browser = webbrowser.get(BROWSER_NAME)
        browser.open(url, new=(1 if BROWSER_NEW_WINDOW else 2))


# -*- coding: iso-8859-1 -*-
"""
    MoinMoin - Data associated with a single Request

    @copyright: 2001-2003 by Jürgen Hermann <jh@web.de>
    @copyright: 2003-2004 by Thomas Waldmann
    @license: GNU GPL, see COPYING for details.
"""

import os, time, sys
from MoinMoin import config, wikiutil
from MoinMoin.util import MoinMoinNoFooter

#############################################################################
### Timing
#############################################################################

class Clock:
    """ Helper class for code profiling
        we do not use time.clock() as this does not work across threads
    """

    def __init__(self):
        self.timings = {'total': time.time()}

    def start(self, timer):
        self.timings[timer] = time.time() - self.timings.get(timer, 0)

    def stop(self, timer):
        self.timings[timer] = time.time() - self.timings[timer]

    def value(self, timer):
        return "%.3f" % (self.timings[timer],)

    def dump(self):
        outlist = []
        for timing in self.timings.items():
            outlist.append("%s = %.3fs" % timing)
        return outlist


#############################################################################
### Request Data
#############################################################################
class RequestBase:
    """ A collection for all data associated with ONE request. """

    # Header set to force misbehaved proxies and browsers to keep their
    # hands off a page
    # Details: http://support.microsoft.com/support/kb/articles/Q234/0/67.ASP
    nocache = [
        "Pragma: no-cache",
        "Cache-Control: no-cache",
        "Expires: -1",
    ]

    def __init__(self, properties={}):
        self.writestack = []
        self.clock = Clock()
        # order is important here!
        from MoinMoin import user
        self.user = user.User(self)
        self.dicts = self.initdicts()

        from MoinMoin import i18n

        if config.theme_force:
            theme_name = config.theme_default
        else:
            theme_name = self.user.theme_name
        try:
            self.theme = wikiutil.importPlugin('theme', theme_name)(self)
        except TypeError:
            theme_name = config.theme_default
            self.theme = wikiutil.importPlugin('theme', theme_name)(self)

        self.args = None
        self.form = None
        self.logger = None
        self.pragma = {}
        self.mode_getpagelinks = 0
        self.no_closing_html_code = 0

        self.sent_headers = 0
        self.user_headers = []

        self.__dict__.update(properties)

        self.i18n = i18n
        self.lang = i18n.requestLanguage(self) 
        self.getText = lambda text, i18n=self.i18n, request=self, lang=self.lang: i18n.getText(text, request, lang)

        # XXX Removed call to i18n.adaptcharset()
  
        self.opened_logs = 0 # XXX for what do we need that???

        self.reset()

    def _setup_vars_from_std_env(self, env):
        """ Sets the common Request members by parsing a standard
            HTTPD environment (as created as environment by most common
            webservers. To be used by derived classes.

            @param env: the environment to use
        """
        self.http_accept_language = env.get('HTTP_ACCEPT_LANGUAGE', 'en')
        self.server_name = env.get('SERVER_NAME', 'localhost')
        self.server_port = env.get('SERVER_PORT', '80')
        self.http_host = env.get('HTTP_HOST','localhost')
        self.http_referer = env.get('HTTP_REFERER', '')
        self.saved_cookie = env.get('HTTP_COOKIE', '')
        self.script_name = env.get('SCRIPT_NAME', '')
        self.path_info = env.get('PATH_INFO', '')
        self.query_string = env.get('QUERY_STRING', '')
        self.request_method = env.get('REQUEST_METHOD', None)
        self.remote_addr = env.get('REMOTE_ADDR', '')
        self.http_user_agent = env.get('HTTP_USER_AGENT', '')
        self.is_ssl = env.get('SSL_PROTOCOL', '') != '' \
            or env.get('SSL_PROTOCOL_VERSION', '') != '' \
            or env.get('HTTPS', 'off') == 'on'

        self.auth_username = None
        if config.auth_http_enabled and env.get('AUTH_TYPE','') == 'Basic':
            self.auth_username = env.get('REMOTE_USER','')
                                    
##        f=open('/tmp/env.log','a')
##        f.write('---ENV\n')
##        f.write('script_name = %s\n'%(self.script_name))
##        f.write('path_info   = %s\n'%(self.path_info))
##        f.write('server_name = %s\n'%(self.server_name))
##        f.write('server_port = %s\n'%(self.server_port))
##        f.write('http_host   = %s\n'%(self.http_host))
##        f.write('------\n')
##        f.write('%s\n'%(repr(env)))
##        f.write('------\n')
##        f.close()
  
    def reset(self):
        """ Reset request state.

        Called after saving a page, before serving the updated
        page. Solves some practical problems with request state
        modified during saving.

        """
        # This is the content language and has nothing to do with
        # The user interface language. The content language can change
        # during the rendering of a page by lang macros
        self.current_lang = config.default_lang
        self._footer_fragments = {}
        self._all_pages = None

        if hasattr(self, "_fmt_hd_counters"):
            del self._fmt_hd_counters


    def add2footer(self, key, htmlcode):
        """ Add a named HTML fragment to the footer, after the default links
        """
        self._footer_fragments[key] = htmlcode


    def getPragma(self, key, defval=None):
        """ Query a pragma value (#pragma processing instruction)

            Keys are not case-sensitive.
        """
        return self.pragma.get(key.lower(), defval)


    def setPragma(self, key, value):
        """ Set a pragma value (#pragma processing instruction)

            Keys are not case-sensitive.
        """
        self.pragma[key.lower()] = value


    def getPageList(self):
        """ A cached version of wikiutil.getPageList().
            Also, this list is always sorted.
        """
        if self._all_pages is None:
            self._all_pages = wikiutil.getPageList(config.text_dir)
            self._all_pages.sort()

        return self._all_pages

    def redirect(self, file=None):
        if file: # redirect output to "file"
            self.writestack.append(self.write)
            self.write = file.write
        else: # restore saved output file
            self.write = self.writestack.pop()

    def reset_output(self):
        """ restore default output method
            destroy output stack
            (useful for error messages)
        """
        if self.writestack:
            self.write = self.writestack[0]
            self.writestack = []

    def write(self, *data):
        """ Write to output stream.
        """
        raise "NotImplementedError"

    def read(self, n):
        """ Read n bytes from input stream.
        """
        raise "NotImplementedError"

    def flush(self):
        """ Flush output stream.
        """
        raise "NotImplementedError"

    def initdicts(self):
        from MoinMoin import wikidicts
        dicts = wikidicts.GroupDict()
        dicts.scandicts()
        return dicts
        
    def isForbidden(self):
        """ check for web spiders and refuse anything except viewing """
        forbidden = 0
        if ((self.query_string != '' or self.request_method != 'GET')
            and self.query_string != 'action=rss_rc'):
            from MoinMoin.util import web
            forbidden = web.isSpiderAgent(request=self)

        if not forbidden and config.hosts_deny:
            ip = self.remote_addr
            for host in config.hosts_deny:
                if ip == host or host[-1] == '.' and ip.startswith(host):
                    forbidden = 1
                    break
        return forbidden


    def setup_args(self, form=None):
        return {}

    def _setup_args_from_cgi_form(self, form=None):
        """ A method to create the args from a standart cgi.FieldStorage
            to be used be derived classes.

            @keyword form: a cgi.FieldStorage list. default is to call
                           cgi.FieldStorage().
        """
        import types, cgi
        
        if form is None:
            form = cgi.FieldStorage()

        args = {}
        for key in form.keys():
            values = form[key]
            if not isinstance(values, types.ListType):
                values = [values]
            fixedResult = []
            for i in values:
                if isinstance(i, cgi.MiniFieldStorage):
                    fixedResult.append(i.value)
                elif isinstance(i, cgi.FieldStorage):
                    fixedResult.append(i.value)
                    # multiple uploads to same form field are stupid!
                    if i.filename:
                        args[key+'__filename__']=i.filename

            args[key] = fixedResult
        return args

    def recodePageName(self, pagename):
        # check for non-URI characters and then handle them according to
        # http://www.w3.org/TR/REC-html40/appendix/notes.html#h-B.2.1
        if pagename:
            try:
                dummy = unicode(pagename, 'ascii')
            except UnicodeError:
                # we have something else than plain ASCII, try
                # converting from UTF-8 to local charset, or just give
                # up and use URI value literally and see what happens
                pagename = self.i18n.recode(pagename, 'utf-8', config.charset) or pagename
        return pagename
        # XXX UNICODE - use unicode for pagenames internally?

    def getBaseURL(self):
        """ Return a fully qualified URL to this script. """
        return self.getQualifiedURL(self.getScriptname())


    def getQualifiedURL(self, uri=None):
        """ Return a full URL starting with schema, servername and port.

            *uri* -- append this server-rooted uri (must start with a slash)
        """
        if uri and uri[:4] == "http":
            return uri

        schema, stdport = (('http', '80'), ('https', '443'))[self.is_ssl]
        host = self.http_host
        if not host:
            host = self.server_name
            port = self.server_port
            if port != stdport:
                host = "%s:%s" % (host, port)

        result = "%s://%s" % (schema, host)
        if uri:
            result = result + uri

        return result


    def getUserAgent(self):
        """ Get the user agent. """
        return self.http_user_agent


    def run(self):
        _ = self.getText
        self.clock.start('run')
        self.open_logs()
        if self.isForbidden():
            self.http_headers([
                'Status: 403 FORBIDDEN',
                'Content-Type: text/plain'
            ])
            self.write('You are not allowed to access this!\n')
            return self.finish()

        # Imports
        from MoinMoin.Page import Page

        if self.query_string == 'action=xmlrpc':
            from MoinMoin.wikirpc import xmlrpc
            xmlrpc(self)
            return self.finish()
        
        if self.query_string == 'action=xmlrpc2':
            from MoinMoin.wikirpc import xmlrpc2
            xmlrpc2(self)
            return self.finish()

        # parse request data
        try:
            self.args = self.setup_args()
            self.form = self.args 
            path_info = self.getPathinfo()

            #from pprint import pformat
            #sys.stderr.write(pformat(self.__dict__))
    
            action = self.form.get('action',[None])[0]

            pagename = None
            if len(path_info) and path_info[0] == '/':
                pagename = wikiutil.unquoteWikiname(path_info[1:])
        except: # catch and print any exception
            self.reset_output()
            self.http_headers()
            self.print_exception()
            return self.finish()

        try:
            # possibly jump to page where user left off
            if not pagename and not action and self.user.remember_last_visit:
                pagetrail = self.user.getTrail()
                if pagetrail:
                    self.http_redirect(Page(pagetrail[-1]).url(self))
                    return self.finish()

            # handle request
            from MoinMoin import wikiaction

            pagename = self.recodePageName(pagename)

            if self.form.has_key('filepath') and self.form.has_key('noredirect'):
                # looks like user wants to save a drawing
                from MoinMoin.action.AttachFile import execute
                execute(pagename, self)
                raise MoinMoinNoFooter

            if action:
                handler = wikiaction.getHandler(action)
                if handler:
                    handler(pagename or
                    wikiutil.getSysPage(self, config.page_front_page).page_name, self)
                else:
                    self.http_headers()
                    self.write("<p>" + _("Unknown action"))
            else:
                if self.form.has_key('goto'):
                    query = self.form['goto'][0].strip()
                elif pagename:
                    query = pagename
                else:
                    query = wikiutil.unquoteWikiname(self.query_string) or \
                        wikiutil.getSysPage(self, config.page_front_page).page_name

                if config.allow_extended_names:
                    Page(query).send_page(self, count_hit=1)
                else:
                    from MoinMoin.parser.wiki import Parser
                    import re
                    word_match = re.match(Parser.word_rule, query)
                    if word_match:
                        word = word_match.group(0)
                        Page(word).send_page(self, count_hit=1)
                    else:
                        self.http_headers()
                        self.write('<p>' + _("Can't work out query") + ' "<pre>' + query + '</pre>"')

            # generate page footer
            # (actions that do not want this footer use raise util.MoinMoinNoFooter to break out
            # of the default execution path, see the "except MoinMoinNoFooter" below)

            self.clock.stop('run')
            self.clock.stop('total')

            if not self.no_closing_html_code:
                if (config.show_timings and
                    self.form.get('action', [None])[0] != 'print'):
                    self.write('<ul id="timings">\n')
                    for t in self.clock.dump():
                        self.write('<li>%s</li>\n' % t)
                    self.write('</ul>\n')

                if 0: # temporarily disabled - do we need that?
                    import socket
                    from MoinMoin import version
                    self.write('<!-- MoinMoin %s on %s served this page in %s secs -->' % (
                        version.revision, socket.gethostname(), self.clock.value('total')) +
                               '</body></html>')
                else:
                    self.write('</body>\n</html>\n\n')
            
        except MoinMoinNoFooter:
            pass

        except: # catch and print any exception
            saved_exc = sys.exc_info()
            self.reset_output()
            self.http_headers()
            self.write("\n<!-- ERROR REPORT FOLLOWS -->\n")
            try:
                from MoinMoin.support import cgitb
            except:
                # no cgitb, for whatever reason
                self.print_exception(*saved_exc)
            else:
                try:
                    cgitb.Hook(file=self).handle(saved_exc)
                    # was: cgitb.handler()
                except:
                    self.print_exception(*saved_exc)
                    self.write("\n\n<hr>\n")
                    self.write("<p><strong>Additionally, cgitb raised this exception:</strong></p>\n")
                    self.print_exception()
            del saved_exc

        return self.finish()


    def http_redirect(self, url):
        """ Redirect to a fully qualified, or server-rooted URL """
        if url.find("://") == -1:
            url = self.getQualifiedURL(url)

        self.http_headers(["Status: 302", "Location: %s" % url])


    def print_exception(self, type=None, value=None, tb=None, limit=None):
        if type is None:
            type, value, tb = sys.exc_info()
        import traceback
        self.write("<h2>request.print_exception handler</h2>\n")
        self.write("<h3>Traceback (most recent call last):</h3>\n")
        list = traceback.format_tb(tb, limit) + \
               traceback.format_exception_only(type, value)
        self.write("<pre>%s<strong>%s</strong></pre>\n" % (
            wikiutil.escape("".join(list[:-1])),
            wikiutil.escape(list[-1]),))
        del tb


    def open_logs(self):
        pass

# CGI ---------------------------------------------------------------

class RequestCGI(RequestBase):
    """ specialized on CGI requests """

    def __init__(self, properties={}):
        self._setup_vars_from_std_env(os.environ)
        #sys.stderr.write("----\n")
        #for key in os.environ.keys():    
        #    sys.stderr.write("    %s = '%s'\n" % (key, os.environ[key]))
        RequestBase.__init__(self, properties)

        # force input/output to binary
        if sys.platform == "win32":
            import msvcrt
            msvcrt.setmode(sys.stdin.fileno(), os.O_BINARY)
            msvcrt.setmode(sys.stdout.fileno(), os.O_BINARY)

    def open_logs(self):
        # create CGI log file, and one for catching stderr output
        import cgi 
        if not self.opened_logs:
            cgi.logfile = os.path.join(config.data_dir, 'cgi.log')
            sys.stderr = open(os.path.join(config.data_dir, 'error.log'), 'at')
            self.opened_logs = 1

    def setup_args(self, form=None):
        return self._setup_args_from_cgi_form(form)
        
    def read(self, n=None):
        """ Read from input stream.
        """
        if n is None:
            return sys.stdin.read()
        else:
            return sys.stdin.read(n)

    def write(self, *data):
        """ Write to output stream.
        """
        for piece in data:
            sys.stdout.write(piece)

    def flush(self):
        sys.stdout.flush()
        
    def finish(self):
        # flush the output, ignore errors caused by the user closing the socket
        try:
            sys.stdout.flush()
        except IOError, ex:
            import errno
            if ex.errno != errno.EPIPE: raise

    #############################################################################
    ### Accessors
    #############################################################################

    def getScriptname(self):
        """ Return the scriptname part of the URL ("/path/to/my.cgi"). """
        name = self.script_name
        if name == '/':
            return ''
        return name


    def getPathinfo(self):
        """ Return the remaining part of the URL. """
        pathinfo = self.path_info

        # Fix for bug in IIS/4.0
        if os.name == 'nt':
            scriptname = self.getScriptname()
            if pathinfo.startswith(scriptname):
                pathinfo = pathinfo[len(scriptname):]

        return pathinfo


    #############################################################################
    ### Headers
    #############################################################################

    def setHttpHeader(self, header):
        self.user_headers.append(header)


    def http_headers(self, more_headers=[]):
        if self.sent_headers:
            #self.write("Headers already sent!!!\n")
            return
        self.sent_headers = 1
        have_ct = 0

        # send http headers
        for header in more_headers + self.user_headers:
            if header.lower().startswith("content-type:"):
                # don't send content-type multiple times!
                if have_ct: continue
                have_ct = 1
            self.write("%s\r\n" % header)

        if not have_ct:
            self.write("Content-type: text/html;charset=%s\r\n" % config.charset)

        self.write('\r\n')

        #from pprint import pformat
        #sys.stderr.write(pformat(more_headers))
        #sys.stderr.write(pformat(self.user_headers))


# Twisted -----------------------------------------------------------

class RequestTwisted(RequestBase):
    """ specialized on Twisted requests """

    def __init__(self, twistedRequest, pagename, reactor, properties={}):
        self.twistd = twistedRequest
        self.http_accept_language = self.twistd.getHeader('Accept-Language')
        self.reactor = reactor
        self.saved_cookie = self.twistd.getHeader('Cookie')
        self.server_protocol = self.twistd.clientproto
        self.server_name = self.twistd.getRequestHostname().split(':')[0]
        self.server_port = str(self.twistd.getHost()[2])
        self.is_ssl = self.twistd.isSecure()
        if self.server_port != ('80', '443')[self.is_ssl]:
            self.http_host = self.server_name + ':' + self.server_port
        else:
            self.http_host = self.server_name
        self.script_name = "/" + '/'.join(self.twistd.prepath[:-1]) # "" XXX
        self.path_info = "/" + pagename
        if self.twistd.postpath:
            self.path_info += '/' + '/'.join(self.twistd.postpath)
        self.request_method = self.twistd.method
        self.remote_host = self.twistd.getClient()
        self.remote_addr = self.twistd.getClientIP()
        self.http_user_agent = self.twistd.getHeader('User-Agent')
        self.request_uri = self.twistd.uri
       
        qindex = self.request_uri.find('?')
        if qindex != -1:
            self.query_string = self.request_uri[qindex+1:]
        else:
            self.query_string = ''
        self.outputlist = []
        self.auth_username = None # TODO, see: self.twistd.user / .password (http auth)
        RequestBase.__init__(self, properties)
        #print "request.RequestTwisted.__init__: received_headers=\n" + str(self.twistd.received_headers)

    def setup_args(self, form=None):
        return self.twistd.args
        
    def read(self, n=None):
        """ Read from input stream.
        """
        # XXX why is that wrong?:
        #rd = self.reactor.callFromThread(self.twistd.read)
        
        # XXX do we need self.reactor.callFromThread with that?
        # XXX if yes, why doesnt it work?
        self.twistd.content.seek(0, 0)
        if n is None:
            rd = self.twistd.content.read()
        else:
            rd = self.twistd.content.read(n)
        #print "request.RequestTwisted.read: data=\n" + str(rd)
        return rd
    
    def write(self, *data):
        """ Write to output stream.
        """
        wd = ''.join(data)
        # XXX UNICODE - encode to config.charset
        #wd = u''.join(data).encode(config.charset)
        #print "request.RequestTwisted.write: data=\n" + wd
        self.reactor.callFromThread(self.twistd.write, wd)

    def flush(self):
        pass # XXX is there a flush in twisted?

    def finish(self):
        #print "request.RequestTwisted.finish"
        self.reactor.callFromThread(self.twistd.finish)

    def open_logs(self):
        return
        # create log file for catching stderr output
        if not self.opened_logs:
            sys.stderr = open(os.path.join(config.data_dir, 'error.log'), 'at')
            self.opened_logs = 1


    #############################################################################
    ### Accessors
    #############################################################################

    def getScriptname(self):
        """ Return the scriptname part of the URL ("/path/to/my.cgi"). """
        scriptname = self.script_name
        if scriptname == '/':
            scriptname = ''
        return scriptname

    def getPathinfo(self):
        """ Return the remaining part of the URL. """
        return self.path_info


    #############################################################################
    ### Headers
    #############################################################################

    def setHttpHeader(self, header):
        self.user_headers.append(header)

    def __setHttpHeader(self, header):
        key, value = header.split(':',1)
        value = value.lstrip()
        self.twistd.setHeader(key, value)
        #print "request.RequestTwisted.setHttpHeader: %s" % header

    def http_headers(self, more_headers=[]):
        if self.sent_headers:
            #self.write("Headers already sent!!!\n")
            return
        self.sent_headers = 1
        have_ct = 0

        # set http headers
        for header in more_headers + self.user_headers:
            if header.lower().startswith("content-type:"):
                # don't send content-type multiple times!
                if have_ct: continue
                have_ct = 1
            self.__setHttpHeader(header)

        if not have_ct:
            self.__setHttpHeader("Content-type: text/html;charset=%s" % config.charset)


    def http_redirect(self, url):
        """ Redirect to a fully qualified, or server-rooted URL """
        if url.count("://") == 0:
            # no https method??
            url = "http://%s:%s%s" % (self.server_name, self.server_port, url)

        self.twistd.redirect(url)
        # calling finish here will send the rest of the data to the next
        # request. leave the finish call to run()
        #self.twistd.finish()
        raise MoinMoinNoFooter


# CLI ------------------------------------------
class RequestCLI(RequestBase):
    """ specialized on commandline interface requests """

    def __init__(self, pagename='', properties={}):
        self.http_accept_language = ''
        self.saved_cookie = ''
        self.path_info = '/' + pagename
        self.query_string = ''
        self.remote_addr = '127.0.0.127'
        self.is_ssl = 0
        self.auth_username = None
        RequestBase.__init__(self, properties)
        self.http_user_agent = ''
        self.outputlist = []

    def read(self, n=None):
        """ Read from input stream.
        """
        if n is None:
            return sys.stdin.read()
        else:
            return sys.stdin.read(n)

    def write(self, *data):
        """ Write to output stream.
        """
        for piece in data:
            sys.stdout.write(piece)

    def flush(self):
        sys.stdout.flush()
        
    def finish(self):
        # flush the output, ignore errors caused by the user closing the socket
        try:
            sys.stdout.flush()
        except IOError, ex:
            import errno
            if ex.errno != errno.EPIPE: raise

    def isForbidden(self):
        """ check for web spiders and refuse anything except viewing """
        return 0


    #############################################################################
    ### Accessors
    #############################################################################

    def getScriptname(self):
        """ Return the scriptname part of the URL ("/path/to/my.cgi"). """
        return '.'

    def getPathinfo(self):
        """ Return the remaining part of the URL. """
        return self.path_info


    def getQualifiedURL(self, uri = None):
        """ Return a full URL starting with schema, servername and port.

            *uri* -- append this server-rooted uri (must start with a slash)
        """
        return uri


    def getBaseURL(self):
        """ Return a fully qualified URL to this script. """
        return self.getQualifiedURL(self.getScriptname())



    #############################################################################
    ### Headers
    #############################################################################

    def setHttpHeader(self, header):
        pass

    def http_headers(self, more_headers=[]):
        pass

    def http_redirect(self, url):
        """ Redirect to a fully qualified, or server-rooted URL """
        raise Exception("Redirect not supported for command line tools!")


# StandAlone Server -------------------------------------------------
class RequestStandAlone(RequestBase):
    """
    specialized on StandAlone Server (httpdmain.py) requests
    """

    def __init__(self, sa, properties={}):
        """
        @param sa: stand alone server object
        @param properties: ...
        """
        import urllib
        self.wfile = sa.wfile
        self.rfile = sa.rfile
        self.headers = sa.headers
        self.is_ssl = 0
        rest = sa.path
        i = rest.rfind('?')
        if i >= 0:
            rest, query = rest[:i], rest[i+1:]
        else:
            query = ''
        uqrest = urllib.unquote(rest)
        
        #HTTP headers
        self.env = {} 
        for hline in sa.headers.headers:
            key = sa.headers.isheader(hline)
            if key:
                hdr = sa.headers.getheader(key)
                self.env[key] = hdr
                
        #accept = []
        #for line in sa.headers.getallmatchingheaders('accept'):
        #    if line[:1] in string.whitespace:
        #        accept.append(line.strip())
        #    else:
        #        accept = accept + line[7:].split(',')
        #
        #env['HTTP_ACCEPT'] = ','.join(accept)

        co = filter(None, sa.headers.getheaders('cookie'))

        self.http_accept_language = sa.headers.getheader('Accept-Language')
        self.server_name = sa.server.server_name
        self.server_port = str(sa.server.server_port)
        self.http_host = sa.headers.getheader('host')
        self.http_referer = sa.headers.getheader('referer')
        self.saved_cookie = ', '.join(co) or ''
        self.script_name = ''
        self.path_info = uqrest 
        self.query_string = query or ''
        self.request_method = sa.command
        self.remote_addr = sa.client_address[0]
        self.http_user_agent = sa.headers.getheader('user-agent') or ''
        # from standalone script:
        # XXX AUTH_TYPE
        # XXX REMOTE_USER
        # XXX REMOTE_IDENT
        self.auth_username = None
        #env['PATH_TRANSLATED'] = uqrest #self.translate_path(uqrest)
        #host = self.address_string()
        #if host != self.client_address[0]:
        #    env['REMOTE_HOST'] = host
        # env['SERVER_PROTOCOL'] = self.protocol_version
        RequestBase.__init__(self, properties)

    def open_logs(self):
        # create error log file for catching stderr output
        if not self.opened_logs:
            sys.stderr = open(os.path.join(config.data_dir, 'error.log'), 'at')
            self.opened_logs = 1

    def setup_args(self, form=None):
        self.env['REQUEST_METHOD'] = self.request_method
        self.env['QUERY_STRING'] = self.query_string
        ct = self.headers.getheader('content-type')
        if ct:
            self.env['CONTENT_TYPE'] = ct
        cl = self.headers.getheader('content-length')
        if cl:
            self.env['CONTENT_LENGTH'] = cl
        
        import cgi
        #print "env = ", self.env
        #form = cgi.FieldStorage(self, headers=self.env, environ=self.env)
        if form is None:
            form = cgi.FieldStorage(self, environ=self.env)
        return self._setup_args_from_cgi_form(form)
        
    def read(self, n=None):
        """ Read from input stream.
        """
        if n is None:
            return self.rfile.read()
        else:
            return self.rfile.read(n)

    def readline (self):
        L = ""
        while 1:
            c = self.read(1)
            L += c
            if c == '\n':
                break
        return L
	    
    def write(self, *data):
        """ Write to output stream.
        """
        for piece in data:
            self.wfile.write(piece)

    def flush(self):
        self.wfile.flush()
        
    def finish(self):
        # flush the output, ignore errors caused by the user closing the socket
        try:
            self.wfile.flush()
        except IOError, ex:
            import errno
            if ex.errno != errno.EPIPE: raise

    #############################################################################
    ### Accessors
    #############################################################################

    def getScriptname(self):
        """ Return the scriptname part of the URL ("/path/to/my.cgi"). """
        name = self.script_name
        if name == '/':
            return ''
        return name


    def getPathinfo(self):
        """ Return the remaining part of the URL. """
        return self.path_info


    #############################################################################
    ### Headers
    #############################################################################

    def setHttpHeader(self, header):
        self.user_headers.append(header)

    def http_headers(self, more_headers=[]):
        if self.sent_headers:
            #self.write("Headers already sent!!!\n")
            return
        self.sent_headers = 1
        have_ct = 0

        # send http headers
        for header in more_headers + self.user_headers:
            if header.lower().startswith("content-type:"):
                # don't send content-type multiple times!
                if have_ct: continue
                have_ct = 1
            self.write("%s\r\n" % header)

        if not have_ct:
            self.write("Content-type: text/html;charset=%s\r\n" % config.charset)

        self.write('\r\n')

        #from pprint import pformat
        #sys.stderr.write(pformat(more_headers))
        #sys.stderr.write(pformat(self.user_headers))


# mod_python/Apache -------------------------------------------------
class RequestModPy(RequestBase):
    """ specialized on mod_python requests """

    def __init__(self, req):
        """ Saves mod_pythons request and sets basic variables using
            the req.subprocess_env, cause this provides a standard
            way to access the values we need here.

            @param req: the mod_python request instance
        """
        req.add_common_vars()
        self.mpyreq = req
        # some mod_python 2.7.X has no get method for table objects,
        # so we make a real dict out of it first.
        if not hasattr(req.subprocess_env,'get'):
            env=dict(req.subprocess_env)
        else:
            env=req.subprocess_env
        self._setup_vars_from_std_env(env)
        # flags if headers sent out contained content-type or status
        self._have_ct = 0
        self._have_status = 0
        RequestBase.__init__(self)
        
        
    def setup_args(self, form=None):
        """ Sets up args by using mod_python.util.FieldStorage, which
            is different to cgi.FieldStorage. So we need a seperate
            method for this.
        """
        import types
        from mod_python import util
        if form is None:
            form = util.FieldStorage(self.mpyreq)

        args = {}
        for key in form.keys():
            values = form[key]
            if not isinstance(values, types.ListType):
                values = [values]
            fixedResult = []
            for i in values:
                ## mod_python 2.7 might return strings instead
                ## of Field objects
                if hasattr(i,'value'):
                    fixedResult.append(i.value)
                else:
                    fixedResult.append(i)
                ## if object has a filename attribute, remember it
                ## with a name hack
                if hasattr(i,'filename') and i.filename:
                    args[key+'__filename__']=i.filename
            args[key] = fixedResult
        return args

    def run(self, req):
        """ mod_python calls this with its request object. We don't
            need it cause its already passed to __init__. So ignore
            it and just return RequestBase.run.

            @param req: the mod_python request instance
        """
        return RequestBase.run(self)

    def read(self, n=None):
        """ Read from input stream.
        """
        if n is None:
            return self.mpyreq.read()
        else:
            return self.mpyreq.read(n)

    def write(self, *data):
        """ Write to output stream.
        """
        for piece in data:
            self.mpyreq.write(piece)

    def flush(self):
        """ We can't flush it, so do nothing.
        """
        pass
        
    def finish(self):
        """ Just return apache.OK. Status is set in req.status.
        """
        # is it possible that we need to return somethig else here?
        from mod_python import apache
        return apache.OK
        
    
    #############################################################################
    ### Accessors
    #############################################################################

    def getScriptname(self):
        """ Return the scriptname part of the URL ('/path/to/my.cgi'). """
        name = self.script_name
        if name == '/':
            return ''
        return name


    def getPathinfo(self):
        """ Return the remaining part of the URL. """
        return self.path_info

    #############################################################################
    ### Headers
    #############################################################################

    def setHttpHeader(self, header):
        """ Filters out content-type and status to set them directly
            in the mod_python request. Rest is put into the headers_out
            member of the mod_python request.

            @param header: string, containing valid HTTP header.
        """
        key, value = header.split(':',1)
        value = value.lstrip()
        if key.lower() == 'content-type':
            # save content-type for http_headers
            if not self._have_ct:
                # we only use the first content-type!
                self.mpyreq.content_type = value
                self._have_ct = 1
        elif key.lower() == 'status':
            # save status for finish
            try:
                self.mpyreq.status = int(value.split(' ',1)[0])
            except:
                pass
            else:
                self._have_status = 1
        else:
            # this is a header we sent out
            self.mpyreq.headers_out[key]=value


    def http_headers(self, more_headers=[]):
        """ Sends out headers and possibly sets default content-type
            and status.

            @keyword more_headers: list of strings, defaults to []
        """
        for header in more_headers:
            self.setHttpHeader(header)
        # if we don't had an content-type header, set text/html
        if self._have_ct == 0:
            self.mpyreq.content_type = "text/html;charset=%s" % config.charset
        # if we don't had a status header, set 200
        if self._have_status == 0:
            self.mpyreq.status = 200
        # this is for mod_python 2.7.X, for 3.X it's a NOP
        self.mpyreq.send_http_header()

# FastCGI -----------------------------------------------------------

class RequestFastCGI(RequestBase):
    """ specialized on FastCGI requests """

    def __init__(self, fcgRequest, env, form, properties={}):
        """ Initializes variables from FastCGI environment and saves
            FastCGI request and form for further use.

            @param fcgRequest: the FastCGI request instance.
            @param env: environment passed by FastCGI.
            @param form: FieldStorage passed by FastCGI.
        """
        self.fcgreq = fcgRequest
        self.fcgenv = env
        self.fcgform = form
        self._setup_vars_from_std_env(env)
        RequestBase.__init__(self, properties)

    def setup_args(self, form=None):
        """ Use the FastCGI form to setup arguments. """
        if form is None:
            form = self.fcgform
        return self._setup_args_from_cgi_form(form)

    def read(self, n=None):
        """ Read from input stream.
        """
        if n is None:
            return self.fcgreq.stdin.read()
        else:
            return self.fcgreq.stdin.read(n)

    def write(self, *data):
        """ Write to output stream.
        """
        self.fcgreq.out.write("".join(data))

    def flush(self):
        """ Flush output stream.
        """
        self.fcgreq.flush_out()

    def finish(self):
        """ Call finish method of FastCGI request to finish handling
            of this request.
        """
        self.fcgreq.finish()


    #############################################################################
    ### Accessors
    #############################################################################

    def getScriptname(self):
        """ Return the scriptname part of the URL ('/path/to/my.cgi'). """
        name = self.script_name
        if name == '/':
            return ''
        return name


    def getPathinfo(self):
        """ Return the remaining part of the URL. """
        pathinfo = self.path_info

        # Fix for bug in IIS/4.0
        if os.name == 'nt':
            scriptname = self.getScriptname()
            if pathinfo.startswith(scriptname):
                pathinfo = pathinfo[len(scriptname):]

        return pathinfo


    #############################################################################
    ### Headers
    #############################################################################

    def setHttpHeader(self, header):
        """ Save header for later send. """
        self.user_headers.append(header)


    def http_headers(self, more_headers=[]):
        """ Send out HTTP headers. Possibly set a default content-type.
        """
        if self.sent_headers:
            #self.write("Headers already sent!!!\n")
            return
        self.sent_headers = 1
        have_ct = 0

        # send http headers
        for header in more_headers + self.user_headers:
            if header.lower().startswith("content-type:"):
                # don't send content-type multiple times!
                if have_ct: continue
                have_ct = 1
            self.write("%s\r\n" % header)

        if not have_ct:
            self.write("Content-type: text/html;charset=%s\r\n" % config.charset)

        self.write('\r\n')

        #from pprint import pformat
        #sys.stderr.write(pformat(more_headers))
        #sys.stderr.write(pformat(self.user_headers))

package test0577;

public class X {

	public static void main(String[] args) {
		
	}
}


import gov.noaa.ncdc.publications.exception.ConvertException;
import gov.noaa.ncdc.publications.exception.CreatePDFFileException;
import gov.noaa.ncdc.publications.exception.CreateTempWorkingDirException;
import gov.noaa.ncdc.publications.exception.FTPServerException;
import gov.noaa.ncdc.publications.exception.FTPServerTimeoutException;
import gov.noaa.ncdc.publications.exception.createListFileException;
import gov.noaa.ncdc.util.FileUtils;
import gov.noaa.ncdc.util.Registry;
import gov.noaa.ncdc.util.WssrdFileComparator;
import gov.noaa.ncdc.util.ftpServer.FtpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
/**
 * <p>
 * <CODE>RemotePublication</CODE> RemotePublication is a publication
 * stored as TIF files in the WSSRD database.
 * </p>
 * <pre>
 * Revision 1.1  2003/10/15 13:11:18  othomann
 * Fix for 44839
 *
 * Revision 1.6  2003/10/09 22:07:28  jduska
 * Merged Chaged from 1.3 Branch into  Head
 *
 * Revision 1.5.2.2  2003/10/01 22:48:03  jduska
 * Added support, so that the publication can be retrieved by WSSRD cabinet 
 * and WSSRD docID
 *
 * Revision 1.5.2.1  2003/09/17 18:20:48  jduska
 * Change FTP Errors to Warnings
 *
 * Revision 1.5  2003/07/25 20:49:52  jduska
 * - Updated the Publication Library to use a new renameFile method 
 * - Created the new FileUtils class by moving the copyFile and adding the renameFile
 * methods
 *
 * Revision 1.4  2003/07/17 23:56:58  jduska
 * - Updated to support new support files in the command line tool	
 * - Update the properties to support the dynamic changing the FTP library
 * - Cleaned up the exceptions and few other files with outdate or unneed imports and etc
 * 
 * Revision 1.3  2003/07/07 15:42:48  jduska
 * - Corrected bugs in new changes to support the Publication Library
 * 
 * Revision 1.2  2003/07/02 17:33:14  jduska
 * - bug fix to correct the Temporary Directory
 *   issue when using the library within a web application
 * 
 * Revision 1.1  2003/06/26 23:54:10  jduska
 * - Command Line version of the Publication Library
 * 
 * Revision 1.3  2003/06/26 23:43:21  jduska
 * - Changes to support using the PDF Library on the command line
 * 
 * Revision 1.1  2003/06/09 21:27:29  jduska
 * - Updated the library, so works on Windows
 * 
 * Revision 1.1  2003/01/24 18:43:58  jduska
 * More refactoring of the SerialPublications to
 * support the local documents
 * 
 * Revision 1.3  2002/10/11 21:03:50  jduska
 *  keyword substitution change ***
 * 
 * </pre>
 */
public class RemotePublication extends Publication {
	private static final String PDF_FILETYPE = "PDF";
	private static Log log = LogFactory.getLog(RemotePublication.class
			.getName());
	private String _fileList;
	private String _fileType;
	protected String _pages[];
	protected RemotePublication(String id, String year, String month,
			String stateCode, String serverDirectory, String cabinet) {
		super(id, year, month, stateCode, serverDirectory, cabinet);
		_fileList = _cabinet + '~' + id + ".txt";
	}
	public boolean getDocument() {
		int attempt = 0;
		while (attempt < _maxAttempts) {
			try {
				attempt++;
				log.info("*** Downloading " + _localFilename + EXTENTION
						+ " attempt " + attempt + " out of " + _maxAttempts);
				if (getRemoteDocument()) {
					log.info("Success document published " + _localFilename
							+ EXTENTION);
					return true;
				}
			} catch (ConvertException e) {
				log.warn(e);
			}
		}
		return false;
	}
	protected boolean getRemoteDocument() throws ConvertException {
		boolean successful = false;
		try {
			createTempWorkingDir();
			getPages();
			if (_fileType != PDF_FILETYPE) {
				createListFile();
				createPDFFile();
			} else {
				renamePDFFile();
			}
			successful = true;
		} catch (FTPServerTimeoutException e) {
			String msg = "Convert process timed out retriving TIFF page"
					+ e.getFileName();
			log.warn(msg);
			throw new ConvertException(msg);
		} catch (FTPServerException e) {
			String msg = "Convert process encounter an " + e.getMessage();
			log.warn(msg);
			throw new ConvertException(msg);
		} catch (CreateTempWorkingDirException e) {
			String msg = "Error: Covert could not create a Working Directory!";
			log.warn(msg);
			throw new ConvertException(msg);
		} catch (CreatePDFFileException e) {
			String msg = "Could not create the PDF file  " + e.getMessage();
			log.warn(msg);
			throw new ConvertException(msg);
		} catch (createListFileException e) {
			String msg = "Could not create the c42pdf List File"
					+ e.getMessage();
			throw new ConvertException(msg);
		} catch (NoFTPServiceException e) {
			String msg = "Could not create the FTP Service " + e.getMessage();
			log.warn(msg);
			throw new ConvertException(msg);
		}
		return successful;
	}
	protected void getPages() throws FTPServerTimeoutException,
			FTPServerException, NoFTPServiceException {
		// Setup the ftp server
		FtpServer ftp;
		try {
			ftp = (FtpServer) Class.forName(Registry.getFTPService())
					.newInstance();
			ftp.setServer(Registry.getUrl());
			ftp.setPassword(Registry.getFtpPassword());
			ftp.setUserName(Registry.getFtpUserName());
			ftp.setPassive(Registry.usePassive());
			ftp.setTimeout(Registry.getFtpTimeout());
			// get the pages and store them in the _workingDir
			_pages = ftp.getFiles(getWssrdDocumentName(), _workingDir);
			if ((_pages.length == 1) && _pages[0].endsWith(".PDF")) {
				_fileType = PDF_FILETYPE;
			}
		} catch (InstantiationException e) {
			log.warn(e.getMessage());
			throw new NoFTPServiceException();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	protected void createPDFFile() throws CreatePDFFileException {
		String cmdLine = null;
		try {
			File newPdfFile = new File(_workingDir + File.separator
					+ _localFilename + EXTENTION);
			if (Registry.isCommandLineMode()) {
				handleMultipleDocuments(newPdfFile);
			}
			cmdLine = createC42PDFCommandLine(newPdfFile.getName());
			if (log.isDebugEnabled()) {
				log.debug("Running " + cmdLine);
			}
			// run C42PDF to create the file & wait till it finishes.
			Process p = Runtime.getRuntime().exec(cmdLine);
			p.waitFor();
			// check to see if it worked
			if (!newPdfFile.exists()) {
				log.warn("PDF File was not created!");
				throw new CreatePDFFileException("PDF File was not created!");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new CreatePDFFileException(e.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new CreatePDFFileException(e.toString());
		}
		return;
	}
	/**
	 * generates the command line need to execute the C42PDF tool in
	 * a shell. This requires that the
	 * <code>Registry.getConvertToolPath()</code> and
	 * <code>Registry.getConvertToolName()</code> are  valid.
	 *
	 * @param documentName the name of the document you wish to
	 *        create
	 *
	 * @return
	 */
	private String createC42PDFCommandLine(String documentName) {
		return (Registry.getConvertToolPath() + File.separator
				+ Registry.getConvertToolName() + (" -o " + _workingDir
				+ File.separator + documentName + " -l " + _workingDir
				+ File.separator + _fileList));
	}
	/**
	 * there a special cases within WSSRD where there may be more
	 * than one PDF that makes up the document. For example River
	 * Basin documents. Thus, it is possible for the same document
	 * already exist when using this libarary via  a command line
	 * mode. <code>handleMultipleDocuments</code> will add a index
	 * counter to end of each duplicate filename. Thus, if there are
	 * three files for this document the files would be name
	 * filename1, filename2 and filename3
	 *
	 * @param newPdfFile name of the document we are making sure does
	 *        already exist
	 */
	private void handleMultipleDocuments(File newPdfFile) {
		int documentCount = 0;
		String newFilename;
		if (log.isDebugEnabled()) {
			log.debug("Checking to see if " + newPdfFile.getName()
					+ "already exists");
		}
		while (newPdfFile.exists()) {
			documentCount++;
			newFilename = (new StringBuffer(_workingDir).append(File.separator)
					.append(_localFilename).append(documentCount)
					.append(EXTENTION)).toString();
			if (log.isDebugEnabled()) {
				log.debug("File already exists. Check to see if we need rename existing file");
			}
			if (documentCount == 1) {
				File backupPdfFile = new File(newFilename);
				newPdfFile.renameTo(backupPdfFile);
			}
			newPdfFile = new File(newFilename);
		}
		return;
	}
	protected void createListFile() throws createListFileException {
		PrintWriter tiffFileListFile = null;
		String s;
		// Must have pages to sort! 
		if (_pages.length == 0) {
			log.error("Internal Error: _tiffPage == null!");
			throw new createListFileException("No pages found in the document!");
		}
		// Sort TIFF files into the correct page order
		WssrdFileComparator fileComparator = new WssrdFileComparator();
		Arrays.sort(_pages, fileComparator);
		if (log.isDebugEnabled()) {
			log.debug("Creating list file for C42PDF");
		}
		try {
			File tiffFileList = new File(_workingDir + File.separator
					+ _fileList);
			if (tiffFileList.exists()) {
				tiffFileList.delete();
			}
			tiffFileListFile = new PrintWriter(new BufferedWriter(
					new FileWriter(_workingDir + File.separator + _fileList)));
			for (int i = 0; i < _pages.length; i++) {
				s = _workingDir + File.separator + _pages[i];
				if (log.isDebugEnabled()) {
					log.debug(s);
				}
				tiffFileListFile.println(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new createListFileException(e.toString());
		} finally {
			if (tiffFileListFile != null) {
				tiffFileListFile.close();
			}
			if (log.isDebugEnabled()) {
				log.debug("Finished Creating list file for C42PDF");
			}
			return;
		}
	}
	protected void renamePDFFile() throws CreatePDFFileException {
		String newFileName = _workingDir + File.separator + _localFilename
				+ EXTENTION;
		String oldWssrdFile = _workingDir + File.separator + _pages[0];
		try {
			FileUtils.renameFile(oldWssrdFile, newFileName);
		} catch (IOException e) {
			String msg = "The following error occurred " + e.getMessage();
			log.warn(msg);
			throw new CreatePDFFileException("Could not rename PDF File");
		}
	}
}
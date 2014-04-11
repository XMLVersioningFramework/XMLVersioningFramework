package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.xqj.exist.ExistXQDataSource;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import play.Play;
import se.repos.vfile.VFileDocumentBuilderFactory;
import se.repos.vfile.gen.VFile;
import utils.FileManager;

public class XChroniclerHandler extends BackendHandlerInterface {

	/**
	 * Test folders related
	 */
	static final String BASE_URL = rootBackendFolder + "XChronicler/";
	public static final String REPOSITORY_URL = BASE_URL + "repo/";

	/**
	 * eXist related
	 */
	protected static String DRIVER = "org.exist.xmldb.DatabaseImpl";
	protected static String DBURI = "xmldb:exist://localhost:8080/exist/xmlrpc";
	protected static String COLLECTION_PATH = Play.application().configuration()
			.getString("eXist.dbPath");
	protected static String resourceName = "movies.xml";

	protected static final String DB_USER = Play.application().configuration()
			.getString("eXist.user");
	protected static final String DB_PASS = Play.application().configuration()
			.getString("eXist.pass");

	/**
	 * VFile generation
	 */
	DocumentBuilder docBuilder = new VFileDocumentBuilderFactory()
			.newDocumentBuilder();
	/**
	 * XChronicler artifacts
	 */
	private boolean doCleanup = false;
	private File testDir = null;
	private File repoDir = null;
	private SVNURL repoUrl;
	private File wc = null;
	private SVNClientManager clientManager = null;

	private XChroniclerHandler() {

	}

	/**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final XChroniclerHandler INSTANCE = new XChroniclerHandler();
	}

	public static BackendHandlerInterface getInstance() {
		return SingletonHolder.INSTANCE;
	}

	@Override
	public Object getRepository() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean init() {
		System.out.println("running xquery init");

		deleteFile(COLLECTION_PATH);

		return true;
	}

	@Override
	public String commitAFile(TempFile tf) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public TempFile getFile(String url) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<RepositoryFile> getWorkingDirFiles() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean commit(String url, String content, String message, User user) {
		// build temp file
		File newFile = FileManager.createFile(content, url,
				XChroniclerHandler.REPOSITORY_URL);

		String vFileContent = "empty";

		// If previous version existing in the database
		// Fetches from the database the latest version of the xml
		try {
			if (exists(XChroniclerHandler.COLLECTION_PATH + url)) {
				System.out.println("**********the file exists");
				// Updates the vfile
				String oldFileContent = getHeadFile(XChroniclerHandler.COLLECTION_PATH
						+ url);
				String oldFileUrl = url + ".old";
				File oldFile = FileManager.createFile(oldFileContent,
						oldFileUrl, XChroniclerHandler.REPOSITORY_URL);
				vFileContent = generateVFile(oldFile, newFile);
			} else {
				System.out.println("**********the file doesnt exist");
				// creates the vfile
				vFileContent = generateVFile(newFile);
			}
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		// String fileName = "url";
		// adds the vfile to the database
		saveToExist(url, vFileContent);

		return true;
	}

	/**
	 * simple test that uses the basic files as input
	 * 
	 * NOTE: requires that the backends/XChronicler/input/basic directory to
	 * exist and the corresponding files
	 * 
	 * @return
	 */
	public String generateVFileSimpleTest() {
		String originalPath = BASE_URL + "input/basic/basic_1.xml";
		String alteredPath = BASE_URL + "input/basic/basic_2.xml";

		return generateVFile(originalPath, alteredPath);
	}

	/**
	 * Generates V-File based on a single version
	 * 
	 * Essentially it initializes the versioning for that file
	 * 
	 * @param initial
	 * @return
	 */
	public String generateVFile(File initial) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Document originalDocument;
		try {
			originalDocument = docBuilder.parse(initial);

			String originalTime = "" + System.nanoTime();
			String originalVersion = "1";

			VFile vFile = VFile.normalizeDocument(originalDocument,
					originalTime, originalVersion);
			printDocument(vFile.toDocument(), output);
		} catch (TransformerException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output.toString();
	}

	/**
	 * Generates V-File based on two different filePaths
	 * 
	 * @param originalPath
	 * @param alteredPath
	 * @return
	 */
	public String generateVFile(String originalPath, String alteredPath) {
		return generateVFile(new File(originalPath), new File(alteredPath));
	}

	/**
	 * Generates V-File based on two different files
	 * 
	 * @param original
	 * @param altered
	 * @return
	 */
	public String generateVFile(File original, File altered) {
		ArrayList<File> files = new ArrayList<File>();
		files.add(original);
		files.add(altered);

		return generateVFileFromArray(files);
	}

	/**
	 * Generates V-file based on an array of files
	 * 
	 * @param files
	 *            the list with the files
	 * @return the v-file as a string
	 */
	public String generateVFileFromArray(List<File> files) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Document lastIndexDoc = null;
		System.out.println("Generating V-file...");
		try {
			Document initialDocument = docBuilder.parse(files.get(0));
			String originalTime = "" + System.nanoTime();
			String originalVersion = "1";
			VFile vFile = VFile.normalizeDocument(initialDocument,
					originalTime, originalVersion);
			for (int i = 0; i < files.size() - 1; i++) {
				System.out.println("parsing file number:" + i);
				File originalFile = files.get(i);
				File alteredFile = files.get(i + 1);

				String newTime = "" + System.nanoTime();
				String newVersion = "" + i;

				vFile.update(docBuilder.parse(originalFile),
						docBuilder.parse(alteredFile), newTime, newVersion);

				lastIndexDoc = vFile.toDocument();
			}
			printDocument(lastIndexDoc, output);
		} catch (TransformerException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return output.toString();
	}

	/**
	 * test that picks up a new version of the xml and updates the v file
	 * 
	 * @deprecated Not working, needs original vfile to be reconverted before
	 */
	@Deprecated
	public void regenerateVFile() {
		File originalDocumentFile = new File(BASE_URL + "input/vfile.xml");
		File alteredDocumentFile = new File(BASE_URL + "input/basic_3.xml");

		DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		Document originalDocument, alteredDocument;

		try {
			docBuilder = dbfac.newDocumentBuilder();
			originalDocument = docBuilder.parse(originalDocumentFile);
			alteredDocument = docBuilder.parse(alteredDocumentFile);

			VFile vFile = new VFile(originalDocument);

			String newTime = "" + System.nanoTime();
			String newVersion = ""
					+ Integer.parseInt(vFile.getDocumentVersion()) + 1;
			vFile.update(originalDocument, alteredDocument, newTime, newVersion);

			Document lastIndexDoc = vFile.toDocument();

			printDocument(lastIndexDoc, System.out);
		} catch (TransformerException | SAXException
				| ParserConfigurationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void printDocument(Document doc, OutputStream out)
			throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), new StreamResult(
				new OutputStreamWriter(out, "UTF-8")));
	}

	@Override
	public boolean removeExistingRepository() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRepositoryPath() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public RepositoryRevision getRepositoryHEAD() {
		String[] files = getFilesFromExist(COLLECTION_PATH);
		RepositoryRevision repo = new RepositoryRevision();
		for (String file : files) {
			RepositoryFile repositoryFile = null;
			try {
				repositoryFile = new RepositoryFile(file,
						getHeadFile(COLLECTION_PATH + file));
			} catch (XQException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			repo.addRepositoryFile(repositoryFile);
		}
		return repo;

	}

	/**
	 * Checks if the file exists in the database
	 * 
	 * @param url
	 * @return
	 * @throws XQException
	 */
	public boolean exists(String url) throws XQException {
		System.out.println("exists?" + url);
		String output = XChroniclerHandler.getHeadFile(url);
		System.out.println("\tget head result:" + output);
		return !output.equalsIgnoreCase("");
	}

	/*
	 * public void tryXSLT() { String xsltResource =
	 * "<?xml version='1.0' encoding='UTF-8'?>\n" +
	 * "<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n"
	 * + " <xsl:output method='xml' indent='no'/>\n" +
	 * " <xsl:template match='/'>\n" +
	 * " <reRoot><reNode><xsl:value-of select='/root/nodee/@val' /> world</reNode></reRoot>\n"
	 * + " </xsl:template>\n" + "</xsl:stylesheet>"; String xmlSourceResource =
	 * "<?xml version='1.0' encoding='UTF-8'?>\n" +
	 * "<root><node val='hello aa '/><nodee id='hje' val='not hello'/></root>";
	 * 
	 * StringWriter xmlResultResource = new StringWriter();
	 * 
	 * Transformer xmlTransformer; try { xmlTransformer =
	 * TransformerFactory.newInstance().newTransformer( new StreamSource(new
	 * StringReader(xsltResource)));
	 * 
	 * xmlTransformer.transform(new StreamSource(new StringReader(
	 * xmlSourceResource)), new StreamResult(xmlResultResource)); } catch
	 * (TransformerConfigurationException e) { // TODO Auto-generated catch
	 * block e.printStackTrace(); } catch (TransformerFactoryConfigurationError
	 * e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
	 * (TransformerException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); }
	 * 
	 * System.out.println(xmlResultResource.getBuffer().toString()); }
	 */

	/**
	 * Saves file to existDB
	 * 
	 * @param fileUrl
	 * @param content
	 * @return
	 */
	private boolean saveToExist(String fileUrl, String content) {
		fileUrl = COLLECTION_PATH + fileUrl;
		System.out.println("-----> Saving to Exist: ");
		System.out.println("\tFile URL: " + fileUrl);
		System.out.println("\tContent:" + content);

		URL url;
		try {
			String putUrl = "http://localhost:8080/exist/rest" + fileUrl;
			url = new URL(putUrl);

			HttpURLConnection httpCon = (HttpURLConnection) url
					.openConnection();
			setUserNameAndPass(httpCon);
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			httpCon.setRequestProperty("content-type",
					"application/xml; charset=utf-8");

			httpCon.connect();
			OutputStreamWriter out = new OutputStreamWriter(
					httpCon.getOutputStream());
			out.write(content);
			out.close();
			httpCon.getInputStream();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	private void deleteFile(String fileUrl) {

		URL url;
		try {

			String putUrl = "http://localhost:8080/exist/rest" + fileUrl;
			System.out.println(putUrl);
			url = new URL(putUrl);

			HttpURLConnection httpCon = (HttpURLConnection) url
					.openConnection();
			setUserNameAndPass(httpCon);
			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("DELETE");
			httpCon.setRequestProperty("content-type",
					"application/xml; charset=utf-8");
			httpCon.connect();
			httpCon.getInputStream();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setUserNameAndPass(HttpURLConnection con) {
		String userPassword = DB_USER + ":" + DB_PASS;
		String encoding = new sun.misc.BASE64Encoder().encode(userPassword
				.getBytes());
		con.setRequestProperty("Authorization", "Basic " + encoding);
	}

	public static String getHeadFile(String url) throws XQException {
		/*
		 * String query = "xquery version '3.0';" +
		 * "declare namespace v='http://www.repos.se/namespace/v';" +
		 * "declare function v:getAttr($e)" + "{" + "    for $a in $e/v:attr" +
		 * "          return attribute {string($a/@v:name)}{$a}" + "};" +
		 * "    declare function v:snapshot($e, $v){" +
		 * "    if (($e/@v:end) = ($v) and name($e) != 'v:text' and name($e) != 'v:attr') then"
		 * + "        element { name($e) } {" + "            v:getAttr($e)," +
		 * "            $e/v:text/text()," +
		 * "for $child in $e/*  return v:snapshot($child, $v)" + "        }" +
		 * "    else" + "        ()" + "};" + "v:snapshot(doc('" + url +
		 * "')/v:file/*[*],'NOW')";
		 */

		String query = "xquery version '3.0';"
				+ "declare namespace v = 'http://www.repos.se/namespace/v';"
				+ "declare function v:getText($e)"
				+ "{"
				+ "  for $a in $e/v:text"
				+ "    return "
				+ "      if (string($a/@v:end)='NOW') then "
				+ "        $a/text() "
				+ "      else "
				+ "        () "
				+ "};"
				+ "		declare function v:getAttr($e)"
				+ "	    {"
				+ "		    for $a in $e/v:attr"
				+ "		    return"
				+ "		        if (string($a/@v:end)='NOW') then"
				+ ""
				+ "		            attribute { string($a/@v:name) } { $a }"
				+ "		        else"
				+ "		            ()"
				+ "		};"
				+ ""
				+ "		declare function v:snapshot($e, $v)"
				+ "		{"
				+ "		    if (($e/@v:end) = ($v) and name($e) != 'v:text' and name($e) != 'v:attr') then"
				+ "		        element { name($e) } {"
				+ "		            v:getAttr($e),"
				+ "		            v:getText($e),"
				+ "		            for $child in $e/*"
				+ "		            return v:snapshot($child, $v)" 
				+ "		    }"
				+ "		    else" + "		        ()" 
				+ "		};"
				+ "		v:snapshot(doc('/db/repo/a.txt')/v:file/*[*], 'NOW')";

		System.out.println(query);
		return runQuery(query);

	}

	private String[] getFilesFromExist(String url) {
		updateFilelist(url);
		String queryAfterNameOfFiles = "for $files in doc('" + url
				+ "filelist.xml')//exist:resource"
				+ "		 return string($files/@name)";
		String queryReturnString = null;
		try {
			queryReturnString = runQuery(queryAfterNameOfFiles);
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(queryReturnString);
		return queryReturnString.split("\n");

	}

	private void updateFilelist(String url) {
		URL webUrl;
		try {
			System.out.println("http://localhost:8080/exist/rest/" + url);
			webUrl = new URL("http://localhost:8080/exist/rest/" + url);

			InputStream is = webUrl.openStream(); // throws an IOException
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line;
			String totalFileList = "";
			while ((line = br.readLine()) != null) {
				totalFileList += line + "\n";
			}
			saveToExist("filelist.xml", totalFileList);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String runQuery(String query) throws XQException {
		XQDataSource xqs = new ExistXQDataSource();
		String returnString = "";
		xqs.setProperty("serverName", "localhost");

		xqs.setProperty("port", "8080");

		XQConnection conn = xqs.getConnection();
		XQPreparedExpression xqpe = conn.prepareExpression(query);

		XQResultSequence rs = xqpe.executeQuery();

		while (rs.next()) {
			returnString += rs.getItemAsString(null).replace("xmlns=\"\"", "")
					+ "\n";
		}

		return returnString;

	}

	@Override
	public Logs getLog() {

		SortedSet<String> ss = new TreeSet<String>();
		String query = "xquery version '3.0';"
				+ "		declare namespace v='http://www.repos.se/namespace/v';"
				+ "		for $as in doc('" + COLLECTION_PATH + "')//@v:end"
				+ "		    return string($as)";
		try {
			ss.addAll(Arrays.asList(runQuery(query).split("\n")));
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		query = "xquery version '3.0';"
				+ "		declare namespace v='http://www.repos.se/namespace/v';"
				+ "		for $as in doc('" + COLLECTION_PATH + "')//@v:start"
				+ "		    return string($as)";
		try {
			ss.addAll(Arrays.asList(runQuery(query).split("\n")));
		} catch (XQException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logs logs = new Logs();
		for (String version : ss) {
			System.out.println(version);
			logs.addLog(new Log(version, ""));
		}
		return logs;
	}

	@Override
	public RepositoryRevision checkOut() {
		// TODO Auto-generated method stub
		return null;
	}

}

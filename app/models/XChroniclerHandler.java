package models;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
	protected static String COLLECTION_PATH = Play.application()
			.configuration().getString("eXist.dbPath");

	protected static final String DB_USER = Play.application().configuration()
			.getString("eXist.user");
	protected static final String DB_PASS = Play.application().configuration()
			.getString("eXist.pass");

	/**
	 * VFile generation
	 */
	DocumentBuilder docBuilder = new VFileDocumentBuilderFactory()
			.newDocumentBuilder();

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
		// build temp files
		String oldFileContent = "";
		try {
			oldFileContent = getHeadFile(url);
		} catch (XQException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String oldFileUrl = url + ".old";
		File oldFile = FileManager.createFile(oldFileContent, oldFileUrl,
				XChroniclerHandler.REPOSITORY_URL);
		File newFile = FileManager.createFile(content, url,
				XChroniclerHandler.REPOSITORY_URL);

		String vFileContent = "empty";
		ByteArrayOutputStream output = new ByteArrayOutputStream();

		// If previous version existing in the database
		// Fetches from the database the latest version of the xml
		try {
			if (exists(url)) {
				System.out.println("**********the file exists");
				// Updates the vfile
				String oldVFileContent = getVFile(url);
				String oldVFileUrl = url + ".vold";
				File indexFile = FileManager.createFile(oldVFileContent,
						oldVFileUrl, XChroniclerHandler.REPOSITORY_URL);
				VFile oldVFile = parseVFile(indexFile);
				vFileContent = updateVFile(oldVFile, oldFile, newFile);
			} else {
				System.out.println("**********the file doesnt exist");
				// creates the vfile
				try {
					printDocument(generateVFile(newFile).toDocument(), output);
				} catch (IOException | TransformerException e) {
					e.printStackTrace();
				}
				vFileContent = output.toString();
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
		VFile vFile = generateVFile(new File(originalPath));

		return updateVFile(vFile, originalPath, alteredPath);
	}

	/**
	 * Generates V-File based on a single version
	 * 
	 * Essentially it initializes the versioning for that file
	 * 
	 * @param initial
	 *            document
	 * @return
	 */
	public VFile generateVFile(File initial) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Document originalDocument;
		try {
			originalDocument = docBuilder.parse(initial);

			String originalTime = "" + System.nanoTime();
			String originalVersion = "1";

			VFile vFile = VFile.normalizeDocument(originalDocument,
					originalTime, originalVersion);
			printDocument(vFile.toDocument(), output);
			System.out.println("generateVFile output: " + output.toString());
			return vFile;
		} catch (TransformerException | SAXException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * parses an existent vFile
	 * 
	 * @param indexFile
	 * @return
	 */
	public VFile parseVFile(File indexFile) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			Document indexDocument = docBuilder.parse(indexFile);
			VFile vFile = new VFile(indexDocument);
			printDocument(vFile.toDocument(), output);
			System.out.println("parseVFile output: " + output.toString());
			return vFile;
		} catch (SAXException | IOException | TransformerException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * updates V-File based on two different filePaths
	 * 
	 * @param originalPath
	 * @param alteredPath
	 * @return
	 */
	public String updateVFile(VFile vFile, String originalPath,
			String alteredPath) {
		return updateVFile(vFile, new File(originalPath), new File(alteredPath));
	}

	/**
	 * updates V-File based on two different files
	 * 
	 * @param vFile
	 * @param original
	 * @param altered
	 * @return
	 */
	public String updateVFile(VFile vFile, File original, File altered) {
		ArrayList<File> files = new ArrayList<File>();
		files.add(original);
		files.add(altered);

		return updateVFileFromArray(vFile, files);
	}

	/**
	 * updates V-file based on an array of files
	 * 
	 * @param vFile
	 * @param files
	 *            the list with the files
	 * @return the v-file as a string
	 */
	public String updateVFileFromArray(VFile vFile, List<File> files) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Document lastIndexDoc = null;
		System.out.println("updating V-file...");
		try {
			String version = vFile.getDocumentVersion();
			System.out.println("version:" + version);
			for (int i = 0; i < files.size() - 1; i++) {
				System.out.println("parsing file number:" + i);
				File originalFile = files.get(i);
				File alteredFile = files.get(i + 1);

				String newTime = "" + System.nanoTime();
				String newVersion = "" + (Integer.parseInt(version)+1);

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
	 * returns the file requested from the database as a string
	 * 
	 * @param fileUrl
	 * @return
	 */
	public String getVFile(String fileUrl) {
		fileUrl = COLLECTION_PATH + fileUrl;

		String query = "xquery version '3.0';"
				+ "declare namespace v='http://www.repos.se/namespace/v';"
				+ "doc('" + fileUrl + "')/*";
		try {
			return runQuery(query);
		} catch (XQException e) {
			e.printStackTrace();
			return null;
		}
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

	public static String getHeadFile(String fileUrl) throws XQException {
		return checkout("NOW", fileUrl);
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
			// TODO: Externalize string to the application.conf file
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
		System.out.println("runQuery: \n\t" + query);
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

		System.out.println("Query result: \n\t" + returnString);

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

	/**
	 * Checkout the repository on a specific revision
	 * 
	 * TODO: Implement the search of all files in the repository
	 * 
	 * @param revision
	 */
	@Override
	public RepositoryRevision checkout(String revision) {
		String fileUrl = "a.xml";
		String fileContent = "";
		try {
			fileContent = checkout(revision, fileUrl);
		} catch (XQException e) {
			e.printStackTrace();
		}

		RepositoryRevision rr = new RepositoryRevision();
		rr.addRepositoryFile(new RepositoryFile(fileUrl, fileContent));
		return rr;
	}

	/**
	 * Checkout a specific revision of a file
	 * 
	 * @param revision
	 * @param fileUrl
	 *            relative to {@link COLLECTION_PATH} (ex.: a.xml)
	 * @return String with the file contents
	 * @throws XQException
	 */
	public static String checkout(String revision, String fileUrl)
			throws XQException {
		String query = "xquery version '3.0';"
				+ "declare namespace v = 'http://www.repos.se/namespace/v';"
				+ "declare function v:beforeEnd($e, $v)"
				+ "{"
				+ "    if (($e/@v:end) castable as xs:int) then"
				+ "        (($e/@v:end) > ($v))"
				+ "    else if (string($e/@v:end) = 'NOW') then"
				+ "        xs:boolean('1')"
				+ "    else"
				+ "        xs:boolean('0')"
				+ "};"
				+ ""
				+ "declare function v:getText($e, $v)"
				+ "{"
				+ "    for $a in $e/v:text"
				+ "    return"
				+ "        if ((($a/@v:start) <= ($v)) and (v:beforeEnd($a, $v))) then"
				+ "            $a/text()"
				+ "        else"
				+ "            ()"
				+ "};"
				+ ""
				+ "declare function v:getAttr($e, $v)"
				+ "{"
				+ "    for $a in $e/v:attr"
				+ "    return"
				+ "        if ((($a/@v:start) <= ($v)) and (v:beforeEnd($a, $v))) then"
				+ "            attribute { string($a/@v:name) } { $a }"
				+ "        else"
				+ "            ()"
				+ "};"
				+ ""
				+ "declare function v:checkout($e, $v)"
				+ "{"
				+ "    if ((($e/@v:start) <= ($v)) and (v:beforeEnd($e, $v)) and name($e) != 'v:text' and name($e) != 'v:attr') then"
				+ "        element { name($e) } {"
				+ "            v:getAttr($e, $v),"
				+ "            v:getText($e, $v),"
				+ "            for $child in $e/*"
				+ "            return v:checkout($child, $v)" + "        }"
				+ "    else" + "        ()" + "};" + "" + "v:checkout(doc('"
				+ COLLECTION_PATH + fileUrl + "')/v:file/*[*], '" + revision
				+ "')" + "";
		return runQuery(query);
	}
}

/*
 * public void tryXSLT() { String xsltResource =
 * "<?xml version='1.0' encoding='UTF-8'?>\n" +
 * "<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n"
 * + " <xsl:output method='xml' indent='no'/>\n" + " <xsl:template match='/'>\n"
 * +
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
 * (TransformerConfigurationException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } catch (TransformerFactoryConfigurationError e) { //
 * TODO Auto-generated catch block e.printStackTrace(); } catch
 * (TransformerException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); }
 * 
 * System.out.println(xmlResultResource.getBuffer().toString()); }
 */

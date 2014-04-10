package models;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.xqj.exist.ExistXQDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import se.repos.vfile.VFileDocumentBuilderFactory;
import se.repos.vfile.gen.VFile;
import se.repos.vfile.store.VFileStore;
import se.simonsoft.cms.item.CmsItemId;
import se.simonsoft.cms.item.CmsItemPath;
import se.simonsoft.cms.item.CmsRepository;
import se.simonsoft.cms.item.RepoRevision;
import se.simonsoft.cms.item.impl.CmsItemIdUrl;

public class XChroniclerHandler extends BackendHandlerInterface {
	private static XChroniclerHandler instance = null;

	/**
	 * Test folders related
	 */
	final String BASE_URL = rootBackendFolder + "XChronicler/";

	/**
	 * eXist related
	 */
	protected static String DRIVER = "org.exist.xmldb.DatabaseImpl";
	protected static String DBURI = "xmldb:exist://localhost:8080/exist/xmlrpc";
	protected static String collectionPath = "/db/movies";
	protected static String resourceName = "movies.xml";
	protected static final String DBUSERPASSWORD = "user" + ":" + "pass";

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
		this.testDir = new File(BASE_URL);

		// this.testDir.delete();

		this.repoDir = new File(this.testDir, "repo");
		try {
			FileUtils.cleanDirectory(this.repoDir);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			this.repoUrl = SVNRepositoryFactory.createLocalRepository(
					this.repoDir, true, false);
		} catch (SVNException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// for low level operations
		// SVNRepository repo = SVNRepositoryFactory.create(repoUrl);
		this.wc = new File(this.testDir, "wc");
		try {
			FileUtils.cleanDirectory(this.wc);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Running local fs repository " + this.repoUrl);
		this.clientManager = SVNClientManager.newInstance();
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
		// CmsRepository repository = new CmsRepository("/anyparent",
		// "anyname");
		// CmsItemId testID = new CmsItemIdUrl(repository, new CmsItemPath(
		// "/basic.xml"));
		// VFileStore store = null;
		// try {
		// store = this.testVFiling(testID, new File(
		// "./backends/XChronicler/testdata"), "/basic_1.xml",
		// "basic_2.xml", "basic_3.xml");
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// // store ==null
		// Document document = store.get(testID);
		//
		// /*
		// * System.out.println(document); VFile v = new
		// VFile(store.get(testID));
		// * v.
		// */
		//
		// try {
		// printDocument(document, System.out);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (TransformerException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// If previous version existing in the database
		// Fetches from the database the latest version of the xml
		
		// Updates the vfile
		// else
		// creates the vfile

		String fileName = "url";
		// adds the vfile to the database
		saveToExist(fileName, content);

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

				vFile.update(docBuilder.parse(originalFile), docBuilder.parse(alteredFile), newTime,
						newVersion);

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
		try {
			System.out.println(getHeadPrivate("/db/movies/movies2.xml"));
		} catch (XQException e) {
			e.printStackTrace();
		}
		/*
		 * TODO Still needs to return RepositoryRevision, otherwise it won't be
		 * able to function for calling classes
		 */
		throw new UnsupportedOperationException();

	}

	// public void tryXSLT() {
	// String xsltResource = "<?xml version='1.0' encoding='UTF-8'?>\n"
	// +
	// "<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>\n"
	// + "   <xsl:output method='xml' indent='no'/>\n"
	// + "   <xsl:template match='/'>\n"
	// +
	// "      <reRoot><reNode><xsl:value-of select='/root/nodee/@val' /> world</reNode></reRoot>\n"
	// + "   </xsl:template>\n" + "</xsl:stylesheet>";
	// String xmlSourceResource = "<?xml version='1.0' encoding='UTF-8'?>\n"
	// +
	// "<root><node val='hello aa '/><nodee id='hje' val='not hello'/></root>";
	//
	// StringWriter xmlResultResource = new StringWriter();
	//
	// Transformer xmlTransformer;
	// try {
	// xmlTransformer = TransformerFactory.newInstance().newTransformer(
	// new StreamSource(new StringReader(xsltResource)));
	//
	// xmlTransformer.transform(new StreamSource(new StringReader(
	// xmlSourceResource)), new StreamResult(xmlResultResource));
	// } catch (TransformerConfigurationException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (TransformerFactoryConfigurationError e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (TransformerException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// System.out.println(xmlResultResource.getBuffer().toString());
	// }

	/**
	 * Saves file to existDB
	 * 
	 * @param fileUrl
	 * @param content
	 * @return
	 */
	private boolean saveToExist(String fileUrl, String content) {
		fileUrl = "/db/movies/" + fileUrl;

		URL url;
		try {
			String putUrl = "http://localhost:8080/exist/rest/" + fileUrl;
			url = new URL(putUrl);

			HttpURLConnection httpCon = (HttpURLConnection) url
					.openConnection();

			String userPassword = XChroniclerHandler.DBUSERPASSWORD;
			String encoding = new sun.misc.BASE64Encoder().encode(userPassword
					.getBytes());

			httpCon.setDoOutput(true);
			httpCon.setRequestMethod("PUT");
			httpCon.setRequestProperty("content-type",
					"application/xml; charset=utf-8");

			httpCon.setRequestProperty("Authorization", "Basic " + encoding);
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

	/**
	 * Takes a url for a vfile and returns the xml that refers to the latest
	 * version
	 * 
	 * @param url
	 * @return
	 * @throws XQException
	 */
	private String getHeadPrivate(String url) throws XQException {
		XQDataSource xqs = new ExistXQDataSource();
		xqs.setProperty("serverName", "localhost");
		xqs.setProperty("port", "8080");

		XQConnection conn = xqs.getConnection();

		String query = "xquery version '3.0';"
				+ "declare namespace v='http://www.repos.se/namespace/v';"
				+ "declare function v:getAttr($e)"
				+ "{"
				+ "    for $a in $e/v:attr"
				+ "          return attribute {string($a/@v:name)}{$a}"
				+ "};"
				+ "    declare function v:snapshot($e, $v){"
				+ "    if (($e/@v:end) = ($v) and name($e) != 'v:text' and name($e) != 'v:attr') then"
				+ "        element { name($e) } {"
				+ "            v:getAttr($e),"
				+ "            $e/v:text/text(),"
				+ "for $child in $e/*  return v:snapshot($child, $v)"
				+ "        }" + "    else" + "        ()" + "};"
				+ "v:snapshot(doc('" + url + "')/v:file/body,'NOW')";

		XQPreparedExpression xqpe = conn.prepareExpression(query);

		XQResultSequence rs = xqpe.executeQuery();
		String returnString = "";
		while (rs.next()) {
			returnString += rs.getItemAsString(null).replace("xmlns=\"\"", "");
		}
		return returnString;
	}

}

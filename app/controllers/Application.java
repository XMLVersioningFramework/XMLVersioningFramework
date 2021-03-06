package controllers;

//import java.io.File;
//import java.io.IOException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.xquery.XQException;

import org.brackit.xquery.atomic.Int;

import models.BackendHandlerInterface;
import models.GitHandler;
import models.SirixHandler;
import models.User;
import models.UserHandler;
import models.XChroniclerHandler;
import models.XQueryUsage;
import play.libs.Json;
//import play.mvc.BodyParser.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.JSONConstants;
import java.lang.management.ManagementFactory;


import com.fasterxml.jackson.databind.node.ObjectNode;

public class Application extends Controller {

	public static Result index() {
		return ok("this is just the index, you should be looking somewhere else");
	}

	private static BackendHandlerInterface getBackend(String backendName) {
		if (backendName.equals(BackendHandlerInterface.GIT)) {
			return GitHandler.getInstance();
		} else if (backendName.equals(BackendHandlerInterface.XCHRONICLER)) {
			return XChroniclerHandler.getInstance();
		} else if (backendName.equals(BackendHandlerInterface.SIRIX)) {
			return SirixHandler.getInstance();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Used for testing, allows to initialize a repository with HTTP/GET
	 * 
	 * @param backendName
	 * @return
	 */
	public static Result initRepositoryWithGET(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);
		if (backend.init()) {
			return ok("Success initializing repository");
		} else {
			return ok("Failure to initialize repository");
		}
	}

	/**
	 * Initializes the repositories, should clean existing ones if they exist
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result initRepository() {
		final Map<String, String[]> postInput = getPOSTData();
		String backendName = postInput.get(utils.JSONConstants.BACKEND)[0];
		BackendHandlerInterface backend = getBackend(backendName);
		ObjectNode returnJson = Json.newObject();

		if (backend.init()) {
			returnJson.put(JSONConstants.ANSWER, JSONConstants.SUCCESS);
		} else {
			returnJson.put(JSONConstants.ANSWER, JSONConstants.FAIL);
		}
		response().setHeader("Access-Control-Allow-Origin", "*");

		return ok(returnJson);
	}

	/**
	 * Collects the data submitted on a HTTP/POST request
	 * 
	 * @return
	 */
	private static Map<String, String[]> getPOSTData() {
		return request().body().asFormUrlEncoded();
	}

	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getHEAD() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);

		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(backend.getRepositoryHEAD().toJSON());
	}

	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getRevision() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);
		String revision = postInput.get(JSONConstants.REVISION_ID)[0];

		response().setHeader("Access-Control-Allow-Origin", "*");
		return ok(backend.checkout(revision).toJSON());
	}

	/**
	 * Uses HTTP/POST
	 * 
	 * @return
	 */
	public static Result getLog() {
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);

		response().setHeader("Access-Control-Allow-Origin", "*");
		System.out.println(backend.getLog());
		return ok(Json.toJson(backend.getLog()));
	}

	/**
	 * Uses HTTP/GET
	 * 
	 * @return
	 */
	public static Result getHEADWithGET(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);

		return ok(backend.getRepositoryHEAD().toJSON());
	}

	public static Result removeRepository(String backendName) {
		BackendHandlerInterface backend = getBackend(backendName);
		if (backend.removeExistingRepository())
			return ok(JSONConstants.SUCCESS);
		return ok(JSONConstants.FAIL);
	}

	public static Result commit() {
		/**
		 * Fetch content
		 */
		String pid =ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	
		Process p = null;
		try{
			String[] cmd = {
				"/bin/sh",
				"-c",
				"top -b -d 0.5 -n 100 | grep "+pid
				};
			p=Runtime.getRuntime().exec(cmd);//;
			} catch (IOException e) {
		    System.out.println("exception happened - here's what I know: ");
		    e.printStackTrace();
		    System.exit(-1);
		}


		final Map<String, String[]> postInput = getPOSTData();

		String url = postInput.get(JSONConstants.URL)[0];
		String content = "";
		if (postInput.get(JSONConstants.CONTENT) != null) {
			content = postInput.get(JSONConstants.CONTENT)[0];
		}
		String message = postInput.get(JSONConstants.MESSAGE)[0];
		String userId = postInput.get(JSONConstants.USER)[0];
		String backendName = postInput.get(JSONConstants.BACKEND)[0];
		String relativeVersion =null;
		if(postInput.get(JSONConstants.RELATIVE_VERSION)!=null){
			relativeVersion = postInput.get(JSONConstants.RELATIVE_VERSION)[0];
		}
		
		
		
		if (postInput.get(JSONConstants.COMMIT_FILE_URL) != null) {
			String fileurl = postInput.get(JSONConstants.COMMIT_FILE_URL)[0];
			if (fileurl != null) {
				String pathToTest = "../XMLTestFramework/public/userStories/";
		//		System.out.println("try to get " + pathToTest + fileurl);
				File file = new File(pathToTest + fileurl);
	//			System.out.println(file.getAbsolutePath());
				try {
					List<String> lines = Files.readAllLines(
							Paths.get(pathToTest + fileurl),
							Charset.defaultCharset());
					content = "";
					for (String string : lines) {
						content += string;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		/**
		 * Pre-processes the POST contents
		 */
		BackendHandlerInterface backend = getBackend(backendName);
		User user = UserHandler.getUser(Integer.parseInt(userId));

		/**
		 * Tries to perform the Commit
		 */
		String answer = "";
		long elapsedTime = Long.MAX_VALUE;
		long start = System.nanoTime();
		int relativeVersionInt=0;
	//	System.out.println("relativeVersion: "+relativeVersion);
		if(relativeVersion!=null){
			relativeVersionInt= Integer.parseInt(relativeVersion);
		}
		
		if(relativeVersion==null || relativeVersionInt==0){
	//		System.out.println("relativeVersion=null");
			if (backend.commit(url, content, message, user)) {
				answer = JSONConstants.SUCCESS;
				elapsedTime = System.nanoTime() - start;
			} else {
				answer = JSONConstants.FAIL;
			}
		}else{
		//	System.out.println("relativeVersion!=null");
			if (backend.commit(url, content, message, user,relativeVersionInt)) {
				answer = JSONConstants.SUCCESS;
				elapsedTime = System.nanoTime() - start;
			} else {
				answer = JSONConstants.FAIL;
			}
		}
		String s="";
		double sumCPU=0;
		double sumMem=0;
		int nrSample=0;
		try {
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// read the output from the command
		//	System.out.println("Here is the standard output of the command:");
			
			while ((s = stdInput.readLine()) != null) {
			//	System.out.println("========================================================");
				int i=0;
				String[] split=s.split("\\s+");
				int posOnS=0;
				for (String row : split) {
					
					if(row.equals("S")){
						posOnS=i;
					}
				//	System.out.println(i+" : "+row);
					
					i++;

				}
				try{
					double newPoint=Double.parseDouble(split[posOnS+1].replace(",","."));
					
			//		System.out.println("New point: "+newPoint);
					sumCPU+=newPoint;

					sumMem+=Double.parseDouble(split[posOnS+2].replace(",","."));
					nrSample++;
				}catch(NumberFormatException e){
					System.out.println("error NumberFormatException:");
					int j=0;
					for (String row: split) {
						System.out.println(j+" : "+row);
						j++;
					}
				}
				
			}

		//	System.out.println("Cpu :"+sumCPU/nrSample);
		//	System.out.println("Memory :"+sumMem/nrSample);
			   

		}catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
     //   System.out.println("--------");

		/**
		 * Creates the response
		 */

		// return new ActionWrapper(super.onRequest(request, actionMethod));
		response().setHeader("Access-Control-Allow-Origin", "*");

		ObjectNode returnJson = Json.newObject();
		returnJson.put(JSONConstants.MEMORY, sumMem/nrSample);
		returnJson.put(JSONConstants.CPU, sumCPU/nrSample);
		returnJson.put(JSONConstants.TIME, elapsedTime);
		returnJson.put(JSONConstants.ANSWER, answer);
		returnJson.put(JSONConstants.HARDDRIVESIZE, backend.getSize());


		return ok(returnJson);
	}

	public static Result testVfile() {
		XChroniclerHandler backend = (XChroniclerHandler) XChroniclerHandler
				.getInstance();

		return ok(backend.generateVFileSimpleTest());
	}

	public static Result testXGetHEAD(String fileURL) {
		try {
			String result = XChroniclerHandler.getHeadFile(fileURL);
			if (result.isEmpty())
				throw new Exception("Result was empty");
			return ok(result);
		} catch (XQException e) {
			e.printStackTrace();
			return internalServerError("The URL you sent was malformed perhaps\n Exception information: "
					+ e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError("Apparently the file doesn't exist.\n Exception result: "
					+ e.getMessage());
		}
	}

	public static Result testXCheckoutRevision(String revision, String fileUrl) {
		try {
			String result = XChroniclerHandler.checkout(revision, fileUrl);
			if (result.isEmpty())
				throw new Exception("Result was empty");
			return ok(result);
		} catch (XQException e) {
			e.printStackTrace();
			return internalServerError("The URL you sent was malformed perhaps\n Exception information: "
					+ e.toString());
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError("Apparently the file doesn't exist.\n Exception result: "
					+ e.getMessage());
		}
	}

	public static Result checkPreFlight() {
		// Need to add the correct domain in here!!
		response().setHeader("Access-Control-Allow-Origin", "*");
		// Only allow POST
		response().setHeader("Access-Control-Allow-Methods", "POST");
		// Cache response for 5 minutes
		response().setHeader("Access-Control-Max-Age", "300");
		// Ensure this header is also allowed!
		response().setHeader("Access-Control-Allow-Headers",
				"Origin, X-Requested-With, Content-Type, Accept");
		return ok();
	}
	
	public static Result revert(){
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);
		
		int relativeVersion=Integer.parseInt(postInput
				.get(JSONConstants.RELATIVE_VERSION)[0]);
		backend.revert(relativeVersion);
		return ok();
	}
	public static Result getDiff(){
		final Map<String, String[]> postInput = getPOSTData();
		BackendHandlerInterface backend = getBackend(postInput
				.get(JSONConstants.BACKEND)[0]);
		
		int relativeVersion=Integer.parseInt(postInput
				.get(JSONConstants.RELATIVE_VERSION)[0]);
		
		backend.getDiff(relativeVersion);
		return ok();
	}
		
		
	

	public static Result testSirix() {
		
		SirixHandler.databaseSirix();
		
		//loadDocumentAndQueryTemporal();
	
		//SirixHandler.printAllVersions();
		/*
		 * try {
		 * 
		 * //XQueryUsage.loadDocumentAndQuery(); //System.out.println(); //
		 * XQueryUsage.loadDocumentAndUpdate(); //System.out.println(); //
		 * XQueryUsage.loadCollectionAndQuery(); // System.out.println(); //
		 * XQueryUsage.loadDocumentAndQueryTemporal(); } catch (QueryException
		 * e) { // TODO Auto-generated catch block e.printStackTrace(); } catch
		 * (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */
		return ok();
	}
}

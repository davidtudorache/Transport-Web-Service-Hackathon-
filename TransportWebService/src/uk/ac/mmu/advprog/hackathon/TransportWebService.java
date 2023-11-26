package uk.ac.mmu.advprog.hackathon;
import static spark.Spark.get;
import static spark.Spark.port;

import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Handles the setting up and starting of the web service
 * You will be adding additional routes to this class, and it might get quite large
 * Feel free to distribute some of the work to additional child classes, like I did with DB
 * @author You, Mainly!
 */
public class TransportWebService {

	/**
	 * Main program entry point, starts the web service
	 * @param args not used
	 */
	public static void main(String[] args) {		
		port(8088);
		
		//Simple route so you can check things are working...
		//Accessible via http://localhost:8088/test in your browser
		get("/test", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				try (DB db = new DB()) {
					return "Number of Entries: " + db.getNumberOfEntries();
				}
			}			
		});
		
		//Route that shows stops in a particular locality
		//Accessible via http://localhost:8088/stopcount in your browser
		get("/stopcount", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				String locality = request.queryParams("locality");
				if(locality == null || locality.isEmpty()) {
					return "Invalid Request";
				}
				try (DB db = new DB()) {
					return "Number of Stops: " + db.getStopCount(locality);
				}
			}			
		});
		
		//Route that shows all the stops of specific stop type in a particular locality
		//Accessible via http://localhost:8088/stop in your browser
		get("/stops", new Route() {
			@Override
			public Object handle(Request request, Response response) throws Exception {
				response.header("Content-Type","application/JSON");
				String locality = request.queryParams("locality");
				String type = request.queryParams("type");
				if(locality == null || locality.isEmpty()) {
					return "Invalid Request";
				}
				if(type == null || type.isEmpty()) {
					return "Invalid Request";
				}
				try (DB db = new DB()) {
					return db.getStopList(locality, type);
				}
			}			
		});

		
		System.out.println("Server up! Don't forget to kill the program when done!");
	}

}

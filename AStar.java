/* Taken reference of a pseudo code from http://ieeexplore.ieee.org/document/7863246/keywords */

/**
 * 
 * @author Aayush Gupta(aaygupta1)
 * @author Jeetendra Ahuja(jahuja)
 *
 */ 
package astar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

/*
 * This class has main logic, each function is described briefly in comments.
 * This class does:
 * 1) Take input from users.
 * 2) Load connections and locations file
 * 3) Apply A-* algorithm to find path between start and end city depending upon user chosen heurestic(Straight-Line OR Fewest-Link)
 * 
 */
public class AStar {
	
	/*
	 * Two main maps that holds data from files given by user "Locations" and "Connections"
	 */
	static Map<String,List<String>> connectionsMap = new HashMap<String,List<String>>();
	static Map<String,List<Double>> locationsMap = new HashMap<String,List<Double>>();
	
	// Hash Map for storing each node's distance from start and end city
	static Map<String,Double> distanceFromStartCity = new HashMap<String,Double>();
	static Map<String,Double> distanceFromEndCity = new HashMap<String,Double>();
	
	// Storing node, parent node and get the final path by backtracking
	static Map<String,String> traversalNodeParentMap = new HashMap<String,String>();

	// Open and close list(a concept of previous visited) for storing cities that are still open and that are already visited
	static Map<String,Node> openList = new HashMap<String,Node>();
	static Map<String,Node> closedList = new HashMap<String,Node>();
	
	//List of skip cities provided by user
	static List<String> skipCityList = null;
	
	// Skipped cities set that are removed from connection map
	static Set<String> citiesSkippedFromConnectionsSet = new HashSet<String>();
	
	// FLag to determine the heuristic, default is Straight line
	static boolean heuristicStraightLineFlag = true;
	
	/*
	 * String variable to hold path of "connection" and "location" file
	 */
	static String locFilePath = null;
	static String conFilePath = null;
	
	/*
	 * variable for start city and end city
	 */
	static String startCity = null;
	static String endCity = null;
	
	// reader object to take user inputs
	static Scanner reader;
	
	/*
	 * main method, we don't need command line arguments
	 */
	public static void main(String[] args) {
		
		// This method takes input from users
		takeInputsFromUser();
		
		// reading locations and connections file and store in their respective map
		fileRead();
		
		// computing end point distance for each city and stored in a hash map	
		for(Map.Entry<String, List<Double>> entry:locationsMap.entrySet()) {
			if(heuristicStraightLineFlag)
				distanceFromEndCity.put(entry.getKey(), calculateDistance(locationsMap.get(endCity),entry.getValue()));
			else
				distanceFromEndCity.put(entry.getKey(), 1d);
		}

		// calling actual method for traversing AStar
		traversingAStar(startCity); 
		
		// iterating through node parent map from bottom to top to get the actual path 
		 if(!traversalNodeParentMap.containsKey(endCity))
		{	
			System.out.println("Can't reach to End point, please give some other skip cities");
			System.exit(1);
		}
		
		// Final path string to be returned
		List<String> finalPath = new ArrayList<String>();
		
		// iterating through node parent map from bottom to top to get the actual path 
		String reconstructString = endCity;
		while(!reconstructString.equals(startCity)) {
			finalPath.add(traversalNodeParentMap.get(reconstructString) + " to "+reconstructString);
			reconstructString = traversalNodeParentMap.get(reconstructString);
		}
		
		// reversing the list to get the actual order list
		Collections.reverse(finalPath);
		
		// Print final path
		System.out.println("Path from "+ startCity+ " to " + endCity + " is:");
		System.out.println(finalPath);

	}
	
	/**
	 * Traversing A star based on heuristic selected
	 * @param startString - start city
	 */
	public static void traversingAStar(String startString){
		
		// Create object for start string and set its values
		Node node = new Node(startString);
		node.setDistanceFromStartPoint(0d);
		node.setDistancetoEndPoint(distanceFromEndCity.get(startString)); 
		node.setTotalHeuristicDistance(distanceFromEndCity.get(startString));
		openList.put(startString,node);
		distanceFromStartCity.put(startString, 0d);
		
		// If open list is not empty 
		while(!openList.isEmpty()) {
			
			String currentNode = calculateShortestFromAll();
			traversalNodeParentMap.put(currentNode, openList.get(currentNode).getPrevious());
			closedList.put(currentNode, openList.get(currentNode));
			openList.remove(currentNode);
			
			if(currentNode.equals(endCity)) {
				break;
			}
			
			// checking for node having no more connections
			if(connectionsMap.get(currentNode) == null) {
				if(openList.containsKey(currentNode)) {
					closedList.put(currentNode, openList.get(currentNode));
					openList.remove(currentNode);
				}
				continue;
			}
			
			
			// Iterating through each current node connected city
			for(String childNode: connectionsMap.get(currentNode)) {
				
				// checking if parent of currentNode is equal to child node and it is in closed List
				if((closedList.containsKey(currentNode) && closedList.get(currentNode).getPrevious()!=null && closedList.get(currentNode).getPrevious().equals(childNode))) {
					continue;
				}

				// checking for heuristic and computing start point distance of child node based on the heuristic
				if(heuristicStraightLineFlag)
					distanceFromStartCity.put(childNode, distanceFromStartCity.get(currentNode)+calculateDistance(locationsMap.get(currentNode),  locationsMap.get(childNode)));
				else
					distanceFromStartCity.put(childNode, distanceFromStartCity.get(currentNode)+1);

				// calculating total Distance of child Node
				double totalDistance = distanceFromStartCity.get(childNode) + distanceFromEndCity.get(childNode);

				// if open list already contains the child node, then
				// compare the start point distance and store the lease one. 
				if(openList.containsKey(childNode)) {
					if(distanceFromStartCity.get(childNode)>=openList.get(childNode).getDistanceFromStartPoint()) {
						distanceFromStartCity.replace(childNode, openList.get(childNode).getDistanceFromStartPoint());
						continue;
					}
					openList.get(childNode).setDistanceFromStartPoint(distanceFromStartCity.get(childNode));
					openList.get(childNode).setPrevious(currentNode);
					traversalNodeParentMap.replace(childNode, currentNode);
				}

				// if closed list already contains the child node, then
				// compare the start point distance and store the lease one.
				else if(closedList.containsKey(childNode)) {
					if(distanceFromStartCity.get(childNode)>= closedList.get(childNode).getDistanceFromStartPoint()) {
						distanceFromStartCity.replace(childNode, closedList.get(childNode).getDistanceFromStartPoint());
						continue;
					}
					openList.put(childNode, closedList.get(childNode));
					closedList.remove(childNode);
					openList.get(childNode).setDistanceFromStartPoint(distanceFromStartCity.get(childNode));
					openList.get(childNode).setPrevious(currentNode);
					traversalNodeParentMap.replace(childNode, currentNode);
				}

				// if node is not in both the open and closed means its the new one
				// then need to create a new Node and store it in open list with all its value
				else {
					Node newNode = new Node(childNode);
					newNode.setTotalHeuristicDistance(totalDistance);
					newNode.setDistancetoEndPoint(distanceFromEndCity.get(childNode));
					newNode.setDistanceFromStartPoint(distanceFromStartCity.get(childNode));
					newNode.setPrevious(currentNode);
					openList.put(childNode,newNode);

				}
			}
		}
	}
	
	/**
	 * calculating shortest based on total distance from open list
	 * @return shortest city name from open list
	 */
	public static String calculateShortestFromAll() {
		String shortest = null;
		double minimum = Double.MAX_VALUE;
		
		// iterating through open list map for calculating the shortest
		Double totHeuDist;
		for(Map.Entry<String, Node> entry:openList.entrySet()) {
			totHeuDist = entry.getValue().getTotalHeuristicDistance();
			if(totHeuDist!=null && totHeuDist< minimum) {
				minimum = entry.getValue().getTotalHeuristicDistance();
				shortest = entry.getValue().getNode();
			}
		}
		// TODO : This error was coming when file path is invalid but we are handling "System.exit()" 
		if(shortest==null)
			System.exit(0); // Error condition, we don't have 
		return shortest;
	}
	
	/**
	 * calculating the straight line distance between two coordinates
	 * @param list1, list2 are the pair of coordinates(x,y)
	 * @return the straight line distance between the two coordinates
	 */
	private static Double calculateDistance(List<Double> list1,List<Double> list2) {
		double x1 = list1.get(0);
		double y1 = list1.get(1);
		double x2 = list2.get(0);
		double y2 = list2.get(1);
		return Math.sqrt((x2 -x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}
	
	/**
	 * Reading the locations and connections file and storing them in map and 
	 * not storing the cities which are in skipped list
	 * @param connectionsMap - Hash Map for the cities in the connections file
	 * @param locationsMap - Hash Map for the locations of each city in locations file
	 */
	private static void fileRead() {
		try (Stream<String> lines = Files.lines(Paths.get(locFilePath))) {
			for (String line : (Iterable<String>) lines::iterator){
				// Last line in file is END, skip it
				if(!line.equalsIgnoreCase("END")) {
					List<Double> list = new ArrayList<Double>();
					String[] arr= line.split(" ");
					
					// removing the entire line if node in skipped list is the parent node
					if(skipCityList.contains(arr[0])) {
						continue;
					}
					// adding coordinates into the list
					list.add(Double.valueOf(arr[1]));
					list.add(Double.valueOf(arr[2]));
					// adding the list into the map with key as city name
					locationsMap.put(arr[0], list);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		try (Stream<String> lines = Files.lines(Paths.get(conFilePath))) {
			for (String line : (Iterable<String>) lines::iterator){
				// Last line in file is END, skip it
				if(!line.equalsIgnoreCase("END")) {
					List<String> list = new ArrayList<String>();
					String[] arr= line.split(" ");
					
					// removing the entire line if node in skipped list is the parent node
					if(skipCityList.contains(arr[0])) {
						citiesSkippedFromConnectionsSet.add(arr[0]);
						continue;
					}
					int i = 0;
					while(i < Double.valueOf(arr[1])) {
						// adding only those cities that are not in skipped list
						// i starts from 0 and 0 is key, 1 is no of connection and from 2 onwards is connection list
						if(!skipCityList.contains(arr[i+2]))
							list.add(arr[i+2]);
						else
							// found skip city in connection list
							citiesSkippedFromConnectionsSet.add(arr[i+2]);
						i++;
					}
					// adding the list into the map with key as city name
					connectionsMap.put(arr[0], list);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		skipCityList.removeAll(citiesSkippedFromConnectionsSet);
		// Whatever is left, is invalid city! Ask user again
		
		while(skipCityList.size()>0) {
			
			if(skipCityList.size()==1 && skipCityList.get(0).length()==0) {
				break;
			}
			
			System.out.println("You have entered below city names wrong which are not in connection file:");
			for(String skip : skipCityList)
				System.out.print(skip+" ");
			System.out.println();
			System.out.println("Please enter correct names if it was typo OR just hit Enter if you want to continue:");
			reader = new Scanner(System.in);
			
			// empty the skip city list with wrong names
			skipCityList.removeAll(skipCityList); 
			
			// create new list for skipped-cities added by user
			skipCityList = new ArrayList<String>(Arrays.asList(reader.nextLine().split("\\W+")) );
			
			// User might have entered start and end city, remove it from skip city list and continue
			if(skipCityList.contains(startCity)){
				skipCityList.remove(startCity);
				System.out.println("You have also entered Start City as " + startCity + " in your skip-city list, we are removing it");
			}
			if(skipCityList.contains(endCity)){
				skipCityList.remove(endCity);
				System.out.println("You have also entered End City as " + endCity + " in your skip-city list, we are removing it");
			}
			
			// get iterator from connection map, this is needed to modify connectionsMap while iterating to 
			// avoid ConcurrentModificationException
			
			Iterator< Map.Entry<String, List<String>> > iterator = connectionsMap.entrySet().iterator();
			citiesSkippedFromConnectionsSet.clear(); // re-set it
			while(iterator.hasNext()) {
				Map.Entry< String, List<String> > entry = iterator.next();
				
				String currFromCity = entry.getKey();
				
				List<String> currConnList = entry.getValue();
				Iterator<String> currConnListIter = currConnList.iterator();
				// Have to use this iterator-way of doing to avoid ConcurrentModificationException
				// when iterating and removing element together
				
				if(skipCityList.contains(currFromCity)) {
					citiesSkippedFromConnectionsSet.add(currFromCity);
					iterator.remove();
				}
				else{
					 while(currConnListIter.hasNext()) {
						 String iteratorNext = currConnListIter.next();
						if( skipCityList.contains(iteratorNext) ) {
							citiesSkippedFromConnectionsSet.add(iteratorNext);	
							currConnListIter.remove();
						}
					}
					 
					// Also, have to update connection map
					if(currConnList.size()==0) // If all cities in list is skipped by user then remove the entry
						connectionsMap.remove(currFromCity);
					else
						connectionsMap.put(currFromCity, currConnList); 
					    // override previous key since we might have removed some, nothing happens if "currConnList" is not altered.
				}
			}
			
			skipCityList.removeAll(citiesSkippedFromConnectionsSet);
			// If skipCityList is null then we are good to proceed else prompt user again.
		}
		
	}
	
	/**
	 * 
	 */
	public static void takeInputsFromUser() {
		/* 
		 * Take arguments from User
		 */
		
		try {
			System.out.println("*****At any point press Ctrl+C to skip*****\n");
			
			// Take Connection file
			reader = new Scanner(System.in);  
			System.out.println("Enter location of \"Connections\" file: ");
			conFilePath = reader.nextLine();
			while(conFilePath.equalsIgnoreCase("") || conFilePath.trim().length()==0 ) {
				System.out.println("Oops, you have not entered path, try again!");
				conFilePath = reader.nextLine();
			}
			
			// Take location file 
			System.out.println("Enter location of \"Locations\" file: ");
			locFilePath = reader.nextLine();
			while(locFilePath.equalsIgnoreCase("") || locFilePath.trim().length()==0 ) {
				System.out.println("Oops, you have not entered path, try again!");
				locFilePath = reader.nextLine();
			}
			
			// Take start city
			System.out.println("Enter start-city: ");
			startCity = reader.nextLine();
			while(startCity.equalsIgnoreCase("") || startCity.trim().length()==0 ) {
				System.out.println("Oops, you have not entered start-city, try again!");
				startCity = reader.nextLine();
			}
			
			// Take End city
			System.out.println("Enter end-city: ");
			endCity = reader.nextLine();
			while(endCity.equalsIgnoreCase("") || endCity.trim().length()==0 ) {
				System.out.println("Oops, you have not entered end-city, try again!");
				endCity = reader.nextLine();
			}
			
			// Take cities that should be skipped from final path.
			System.out.println("Enter city/cities to be skipped from final path separated by spaces");
			System.out.println("(you can just hit enter if you don't want to!)");
			String skipCitiesString = reader.nextLine();
			
			//Extract each city from a string and make an array
			skipCityList = new ArrayList<String>(Arrays.asList(skipCitiesString.split("\\W+")) );
			
			if(skipCityList.contains(startCity)){
				skipCityList.remove(startCity);
				System.out.println("You have also entered Start City as " + startCity + " in your skip-city list, we are removing it");
			}
			
			if(skipCityList.contains(endCity)){
				skipCityList.remove(endCity);
				System.out.println("You have also entered End City as " + endCity + " in your skip-city list, we are removing it");
			}
			
			//Ask heuristic to user
			System.out.println("Enter Heurestic - \"F OR f\" for \"Fewest Links\" or \"S OR s\" for \"Straight Line Distance\"");
			// TODO Change nextLine() to nextByte() Since it is just a character.
			String heurestic = reader.nextLine();
			while(heurestic.equalsIgnoreCase("") || heurestic.trim().length()==0 
					|| ( !heurestic.equalsIgnoreCase("F") && !heurestic.equalsIgnoreCase("S") )
				) 
			{
				System.out.println("Oops, Invalid entry, try again!");
				heurestic = reader.nextLine();
			}
			
			if(heurestic.equalsIgnoreCase("F"))
				heuristicStraightLineFlag = false;
			
		}
		catch (Exception e) {
			System.out.println("--------USER EXITED PROGRAM---------");
		}
		
	}
}

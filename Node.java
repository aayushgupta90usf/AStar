package astar;
/**
 * 
 * @author Aayush Gupta(aaygupta1)
 * @author Jeetendra Ahuja(jahuja)
 *
 * 
 * This class will hold the values of a node, its parent, its distane from start city, end city and the total distance
 * and these all are used when a node is inserting into Open List or closed list adn the same values are being used 
 * to calculate and compare values during addition or deletion. 
 */
public class Node {
	
	// Current node name
	private String node = null;
	
	// Current node immediate parent name i.e from which node it came from
	private String previous = null;
	
	// node distance from start point, end point and total heuristic distance
	private Double distancetoEndPoint;
	private Double totalHeuristicDistance;
	private Double distanceFromStartPoint;

	/**
	 * Setters and getters for above variables.
	 */
	public Double getDistanceFromStartPoint() {
		return distanceFromStartPoint;
	}

	public void setDistanceFromStartPoint(Double distanceFromStartPoint) {
		this.distanceFromStartPoint = distanceFromStartPoint;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public Double getDistancetoEndPoint() {
		return distancetoEndPoint;
	}

	public void setDistancetoEndPoint(Double distancetoEndPoint) {
		this.distancetoEndPoint = distancetoEndPoint;
	}

	public Double getTotalHeuristicDistance() {
		return totalHeuristicDistance;
	}

	public void setTotalHeuristicDistance(Double totalHeuristicDistance) {
		this.totalHeuristicDistance = totalHeuristicDistance;
	}

	public Node(String node,String previous) {
		this.node = node;
		this.previous = previous;
	}
	
	public Node(String node) {
		this.node = node;
	}
	
}

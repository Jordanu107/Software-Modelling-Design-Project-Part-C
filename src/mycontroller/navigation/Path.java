package mycontroller.navigation;

import java.util.ArrayList;
import utilities.Coordinate;
import world.WorldSpatial.Direction;

/**
 * Represents a continuous (connected and adjacent) set of coordinates that constitude a "path".
 * @author Lawson Wang-Wills
 */
public class Path {
	
	public ArrayList<Coordinate> path;
	
	// starting with empty path
	public Path() {
		path = new ArrayList<>();
	}
	
	// starting with pre-built path
	public Path(ArrayList<Coordinate> path) throws Exception {
		if (validatePath(path)) {
			this.path = path;
		} else {
			throw new Exception("Tried to create an invalid path!");
		}
	}
	
	// deep copy
	public Path(Path path) {
		this.path = new ArrayList<Coordinate>();
		
		for (Coordinate step : path.getCoords()) {
			this.path.add(step);
		}
	}
	
	
	/**
	 * Adds a new point to the path, only if it is valid.
	 * @param step
	 * @return whether or not the point was successfully added
	 */
	public boolean addToPath(Coordinate point) {
		if (path.isEmpty()) {
			path.add(point);
			return true;
		}
		
		Coordinate lastPoint = path.get(path.size()-1);
		if (lastPoint.equals(point) || checkAdjacency(lastPoint, point)) {
			path.add(point);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Appends a given path onto the current path, only if it is valid
	 * @param path
	 * @return whether or not the point was successfully added
	 */
	public boolean addToPath(Path newPath) {
		ArrayList<Coordinate> coords = newPath.getCoords();
		
		// the new path must start where the old path ended
		if (coords.size() > 1 && path.get(path.size() - 1) == coords.get(0)) {
			coords.remove(0);
			path.addAll(coords);
			return true;
		}
		
		return false;
	}
	
	public Coordinate popFromPath() {
		return path.remove(path.size()-1);
	}
	
	
	public Coordinate getStep(int stepIndex) {
		return path.get(stepIndex);
	}
	
	public int getLength() {
		return path.size();
	}
	
	/**
	 * Returns the direction from step 'step' to the next step.
	 * @param step
	 * @return
	 */
	public Direction getDirectionAtStep(int step) {
		if (step + 1 >= path.size()) {
			return null;
		}

		Coordinate a = path.get(step);
		Coordinate b = path.get(step+1);
		
		return fromToDirection(a, b);
	}
	
	/**
	 * Returns the direction from a to b, given that they are adjacent
	 * @param a
	 * @param b
	 * @return
	 */
	public static Direction fromToDirection(Coordinate a, Coordinate b) {
		// since we assume the path is valid, only one of x and y should change between steps
		if (a.x < b.x) {
			return Direction.EAST;
		}
		else if (a.x > b.x) {
			return Direction.WEST;
		}
		else if (a.y < b.y) {
			return Direction.NORTH;
		}
		else if (a.y > b.y) {
			return Direction.SOUTH;
		}
		
		// there is no movement between these steps
		return null;
	}
	
	/**
	 * Checks whether a list of coordinates constitutes a valid path, i.e. that all index-adjacent coordinates are world-adjacent.
	 * @param path
	 * @return
	 */
	private static boolean validatePath(ArrayList<Coordinate> path) {
		for (int i = 0; i < path.size() - 1; i++) {
			Coordinate before = path.get(i);
			Coordinate after = path.get(i+1);
			if (before.equals(after) || !Path.checkAdjacency(before, after)) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Checks whether two coordinates are adjacent.
	 * Perhaps should be moved outside of this class.
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean checkAdjacency(Coordinate a, Coordinate b) {
		return Math.abs(a.x - b.x) + Math.abs(a.y - b.y) == 1 ;
	}
	
	public ArrayList<Coordinate> getCoords() {
		return this.path;
	}
}

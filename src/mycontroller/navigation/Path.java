package mycontroller.navigation;

import java.util.ArrayList;
import utilities.Coordinate;
import world.WorldSpatial.Direction;

/**
 * Represents a continuous (connected and adjacent) set of coordinates that constitude a "path".
 * @author Lawson Wang-Wills
 */
public class Path {
	
	private ArrayList<Coordinate> path;
	
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
	
	
	/**
	 * Adds a new point to the path, only if it is valid.
	 * @param step
	 * @return whether or not the point was succesfully added
	 */
	public boolean addToPath(Coordinate point) {
		if (checkAdjacency(path.get(path.size()-1), point)) {
			path.add(point);
			return true;
		}
		
		return false;
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
		
		// should not happen
		return null;
	}
	
	/**
	 * Checks whether a list of coordinates constitutes a valid path, i.e. that all index-adjacent coordinates are world-adjacent.
	 * @param path
	 * @return
	 */
	private static boolean validatePath(ArrayList<Coordinate> path) {
		for (int i = 0; i < path.size() - 1; i++) {
			if (!Path.checkAdjacency(path.get(i), path.get(i+1))) {
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
}

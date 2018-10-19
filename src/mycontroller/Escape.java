package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import controller.CarController;
import mycontroller.map.Mapping;
import mycontroller.navigation.Navigator;
import mycontroller.navigation.Path;
import mycontroller.navigation.Pathfinding;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;

/**
 * Handles Car escaping the maze, given the available mapping information.
 * @author Lawson Wang-Wills
 *
 */
public class Escape extends CarController {
	
	private Navigator navigator;
	
	public Escape(Car car) {
		super(car);

		initialiseNavigation();
	}

	@Override
	public void update() {
		if (!navigator.isNavigating()) {
			initialiseNavigation();
		}
		
		navigator.update();
	}
	
	/**
	 * Attempts to initialise the navigator with a valid success path. Returns true iff successful.
	 * @return
	 */
	public boolean initialiseNavigation() {
		System.out.println("Escape: looking for success path...");
		Path path = findSuccessPath();
		this.navigator = new Navigator(this, path);
		
		if (path == null) {
			System.out.println("Escape: did not find success path.");
			return false;
		} else {
			System.out.println("Escape: found success path.");
			//System.out.println(path.getCoords());
			return true;
		}
	}
	
	/**
	 * Searches for a path that can navigate the maze. Returns the path found, or null if no path was found.
	 * @return path
	 */
	private Path findSuccessPath() {
		int maxKeys = numKeys();
		Set<Integer> foundKeys = getKeys();
		HashMap<Coordinate, Integer> seenKeys = Mapping.getMap().getKeysSeen();
		
		// check if we have access to enough keys
		Set<Integer> accessibleKeys = new HashSet<Integer>(foundKeys);
		for (Integer key : seenKeys.values()) {
			accessibleKeys.add(key);
		}
		if (accessibleKeys.size() < maxKeys) {
			System.out.println("Escape: failed Key Check.");
			return null;
		}
		System.out.println("Escape: passed Key Check.");
		
		
		/* DEFINE GOALS */
		// get keys we don't have
		HashMap<Coordinate, Integer> keys = new HashMap<>();
		for (Coordinate key : seenKeys.keySet()) {
			if (!foundKeys.contains(seenKeys.get(key))) {
				keys.put(key,  seenKeys.get(key));
			}
		}
		
		// get exits
		HashMap<Coordinate, MapTile> map = getMap();
		
		ArrayList<Coordinate> exits = new ArrayList<>();
		for (Coordinate loc : map.keySet()) {
			if (map.get(loc).getType() == MapTile.Type.FINISH) {
				exits.add(loc);
			}
		}
				
		/* ENUMERATE HIGH LEVEL ROUTES (based on order of getting keys) */
		ArrayList<Coordinate> goals = new ArrayList<>();
		return enumerateRoutes(keys, exits, goals);
	}
	
	/**
	 * Recursively enumerate possible (high-level) routes 
	 * @param car
	 * @param keys
	 * @param keysToFind
	 * @param exits
	 * @param goals
	 * @return
	 */
	private Path enumerateRoutes(HashMap<Coordinate, Integer> keys, ArrayList<Coordinate> exits, ArrayList<Coordinate> goals) {
		// check which keys we've gotten
		Set<Integer> doneKeys = new HashSet<>();
		for (Coordinate key: goals) {
			doneKeys.add(keys.get(key));
		}
		
		// try each combination of keys
		for (Coordinate key : keys.keySet()) {
			// have we already gotten a key of this type?
			if (doneKeys.contains(keys.get(key))) {
				continue;
			}
			
			// we want to try this key - go!
			goals.add(key);
			
			Path path = enumerateRoutes(keys, exits, goals);
			if (path != null) {
				return path;
			}
			
			goals.remove(key);
		}
		
		
		// otherwise loop over exits and try each one
		for (Coordinate exit : exits) {
			goals.add(exit);
			Path path = Pathfinding.linkGoals(this, goals);
			
			if (path != null) {
				return path;
			}
			
			goals.remove(goals.size()-1);
		}
		
		
		// otherwise we've got nothing
		return null;
	}
	

	
	

}

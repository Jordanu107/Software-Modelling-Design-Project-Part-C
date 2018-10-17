package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import controller.CarController;
import mycontroller.map.Mapping;
import mycontroller.navigation.Navigator;
import mycontroller.navigation.Path;
import tiles.GrassTrap;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MudTrap;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial.Direction;

/**
 * Handles Car escaping the maze, given the available mapping information.
 * @author Lawson Wang-Wills
 *
 */
public class Escape extends CarController {
	private class PseudoCar {
		public float health;
		public Direction orientation;
		public Coordinate position;
		public boolean isMoving;
		
		public PseudoCar (float health, Direction orientation, Coordinate position, boolean isMoving) {
			this.health = health;
			this.orientation = orientation;
			this.position = position;
			this.isMoving = isMoving;
		}
		
		public PseudoCar(PseudoCar copy) {
			this.health = copy.health;
			this.orientation = copy.orientation;
			this.position = copy.position;
			this.isMoving = copy.isMoving;
		}
	}
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
	
	public Path findSuccessPath() {
		int maxKeys = numKeys();
		Set<Integer> foundKeys = getKeys();
		HashMap<Coordinate, Integer> seenKeys = Mapping.map.getKeysSeen();
		
		// check if we have access to enough keys
		Set<Integer> accessibleKeys = new HashSet<Integer>(foundKeys);
		for (Integer key : seenKeys.values()) {
			accessibleKeys.add(key);
		}
		
		if (accessibleKeys.size() < maxKeys) {
			return null;
		}
		
		// create a simulated car to pathfind with
		PseudoCar pcar = new PseudoCar(
				getHealth(),
				getOrientation(),
				new Coordinate(getPosition()),
				getSpeed() != 0
		);
		
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
		int keysToFind = maxKeys - foundKeys.size();
		ArrayList<Coordinate> goals = new ArrayList<>();
		return enumerateRoutes(pcar, keys, exits, goals);
	}
	
	/**
	 * Recursively enumerate possible routes 
	 * @param car
	 * @param keys
	 * @param keysToFind
	 * @param exits
	 * @param goals
	 * @return
	 */
	private Path enumerateRoutes(PseudoCar car, HashMap<Coordinate, Integer> keys, ArrayList<Coordinate> exits, ArrayList<Coordinate> goals) {
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
			
			Path path = enumerateRoutes(car, keys, exits, goals);
			if (path != null) {
				return path;
			}
			
			goals.remove(key);
		}
		
		
		// otherwise loop over exits and try each one
		for (Coordinate exit : exits) {
			goals.add(exit);
			Path path = linkGoals(car, goals);
			
			if (path != null) {
				return path;
			}
			
			goals.remove(goals.size()-1);
		}
		
		
		// otherwise we've got nothing
		return null;
	}
	
	/**
	 * Returns a path that takes car past each goal, if possible. Otherwise, return null.
	 * @param car
	 * @param goals
	 * @return
	 */
	private Path linkGoals(PseudoCar car, ArrayList<Coordinate> goals) {
		// kick it off by linking the first point
		Path path = linkPoints(car, goals.get(0));
		if (path == null) {
			return null;
		}
		car = updateCar(car, path);
		
		// then link the rest
		for (int i = 1; i < goals.size(); i++) {
			Path tmp = linkPoints(car, goals.get(i));
			
			if (path == null) {
				return null;
			}
			path.addToPath(tmp);
			car = updateCar(car, path);
		}
		
		return path;
	}
	
	/**
	 * Returns a Path that takes car to finish, minimizing health damage.
	 * @param start
	 * @param finish
	 * @param car
	 * @return
	 */
	private Path linkPoints(PseudoCar car, Coordinate finish) {
		
		
		
		
		
		
		return null;
	}
	
	/**
	 * Updates the state of a car, as if it had followed the given path.
	 * @param path
	 * @return
	 */
	private PseudoCar updateCar(PseudoCar car, Path path) {
		car = new PseudoCar(car);
		
		HashMap<Coordinate, MapTile> map = getMap();
		Coordinate lastStep = null;
		for (Coordinate step : path.getCoords()) {
			/*
			 * USE OF MAPTILE PROPERTIES
			 */
			MapTile tile = map.get(step);
			applyTile(tile, car);
			
			
			car.isMoving = step != lastStep;
			lastStep = step;
		}
		
		car.position = path.getStep(path.getLength()-1);
		car.orientation = path.getDirectionAtStep(path.getLength()-1);
		
		return car;
	}
	
	/**
	 * Applies tile effects to car.
	 * @param tile
	 * @param car
	 */
	private void applyTile(MapTile tile, PseudoCar car) {
		if (tile instanceof LavaTrap) {
			car.health -= ((LavaTrap) tile).HealthDelta; 
		} else if (tile instanceof HealthTrap) {
			car.health += ((HealthTrap) tile).HealthDelta;
		}
	}
	
	private boolean canMove (PseudoCar car, Coordinate destination) {
		Coordinate start = car.position;
		
		// would this even be a valid move
		if (start != destination && (Math.abs(start.x - destination.x) + Math.abs(start.y - destination.y)) != 1) {
			return false;
		}
		
		HashMap<Coordinate, MapTile> map = getMap();
		MapTile startTile = map.get(start);
		MapTile destinationTile = map.get(destination);
		
		// check against tile types
		// start tile
		if (startTile instanceof LavaTrap) {
			// no restriction, we would only take more damage if we remain in lava (i.e. dest is lava)
			
		} else if (startTile instanceof HealthTrap) {
			// no restriction
			
		} else if (startTile instanceof GrassTrap) {
			// see if this move requires the car to turn
			
			
		} else if (startTile instanceof MudTrap) {
			// you are already dead
			return false;
			
		}
		
		if (destinationTile instanceof LavaTrap) {
			// check if the car is gonna get killed by this trap
			if (car.health - ((LavaTrap) destinationTile).HealthDelta <= 0) {
				return true;
			}
			
		} else if (destinationTile instanceof HealthTrap) {
			// no restriction
			
		} else if (destinationTile instanceof GrassTrap) {
			// no restriction
			
		} else if (destinationTile instanceof MudTrap) {
			// no way you'll die
			return false;
			
		} else if (destinationTile.getType() == MapTile.Type.WALL) {
			// bump
			return false;
			
		}
		
		return false;
	}
	
	
	private void initialiseNavigation() {
		Path path = findSuccessPath();
		this.navigator = new Navigator(this, path);
	}
}

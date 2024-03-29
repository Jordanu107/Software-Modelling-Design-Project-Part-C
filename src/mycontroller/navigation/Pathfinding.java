package mycontroller.navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import controller.CarController;
import mycontroller.map.Mapping;
import tiles.GrassTrap;
import tiles.HealthTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MudTrap;
import utilities.Coordinate;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import world.WorldSpatial.RelativeDirection;

public class Pathfinding {
	/*
	 * Class representing a simplified "Car" with relevant parameters to operate on.
	 */
	private static class PseudoCar {
		public static final float maxHealth = 100;
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
		
		public String toString() {
			return "Car Health: " + this.health +"\nCar Orientation: " + this.orientation + "\nCar Position: " + this.position + "\nCar Moving: " + this.isMoving;
		}
	}
	
	
	
	/***********************************
	 * PUBLIC METHODS FOR EXTERNAL USE *
	 ***********************************/
	
	/**
	 * Returns a path that takes car past each goal, if possible. Otherwise, return null.
	 * @param car
	 * @param goals
	 * @return
	 */
	public static Path linkGoals(CarController carControl, ArrayList<Coordinate> goals) {
		// create a simulated car to pathfind with
		PseudoCar car = new PseudoCar(
			carControl.getHealth(),
			carControl.getOrientation(),
			new Coordinate(carControl.getPosition()),
			(int) Math.abs(carControl.getSpeed()) != 0
		);
		
		
		// kick it off by linking the first point
		Path path = linkPoints(car, goals.get(0));
		if (path == null) {
			return null;
		}
		car = updateCar(car, path);
		
		
		// then link the rest
		for (int i = 1; i < goals.size(); i++) {

			Path tmp = linkPoints(car, goals.get(i));
			if (tmp == null) {
				return null;
			}
			car = updateCar(car, tmp);
			path.addToPath(tmp);
		}
		
		return path;
	}
	
	/**
	 * Returns a Path that takes car to finish, minimizing health damage.
	 * Use a modified version of A*.
	 * @param start
	 * @param finish
	 * @param car
	 * @return
	 */
	public static Path linkPoints(CarController carControl, Coordinate finish) {
		// create a simulated car to pathfind with
		PseudoCar car = new PseudoCar(
			carControl.getHealth(),
			carControl.getOrientation(),
			new Coordinate(carControl.getPosition()),
//			false	// because the car is always at STOP status before PathFinding
			(int) Math.abs(carControl.getSpeed()) != 0
		);
		
		return linkPoints(car, finish);
	}
	
	private static Path linkPoints(PseudoCar car, Coordinate finish) {
		
		Set<Coordinate> visited = new HashSet<>();
		Set<Coordinate> unvisited = new HashSet<>();
		HashMap<Coordinate, Path> pathMap = new HashMap<>();
		HashMap<Coordinate, PseudoCar> carMap = new HashMap<>();

		Coordinate current;
		PseudoCar currentCar;
		Path currentPath = new Path();
		currentPath.addToPath(car.position);
		
		unvisited.add(car.position);
		pathMap.put(car.position, currentPath);
		carMap.put(car.position, car);
		
		while (!unvisited.isEmpty()) {
			// get best unvisited 
			current = (Coordinate) unvisited.toArray()[0];
			float bestHealth = carMap.get(current).health, tmpHealth;
			int bestDistance = pathMap.get(current).getLength() + distance(current, finish);
			int tmpDistance;
			for (Coordinate option : unvisited) {
				// priority is health, then distance
				tmpHealth = carMap.get(option).health;
				tmpDistance = pathMap.get(option).getLength() + distance(current, finish);
				if (tmpHealth > bestHealth) {
					current = option;
					bestHealth = tmpHealth;
					bestDistance = tmpDistance;
					
				} else if ((int) tmpHealth == (int) bestHealth && tmpDistance <= bestDistance) {
					current = option;
					bestHealth = tmpHealth;
					bestDistance = tmpDistance;
				}
				
			}
			unvisited.remove(current);
			visited.add(current);
			
			// if we have the finish, no need to go further
			if (current.equals(finish)) {
				break;
			}
			
			currentCar = carMap.get(current);
			currentPath = pathMap.get(current);
			
			// enumerate adjacent tiles
			ArrayList<Coordinate> adjacent = new ArrayList<>();
			adjacent.add(new Coordinate(current.x+1, current.y));
			adjacent.add(new Coordinate(current.x, current.y+1));
			adjacent.add(new Coordinate(current.x-1, current.y));
			adjacent.add(new Coordinate(current.x, current.y-1));
			adjacent.add(new Coordinate(current.x, current.y));
			
			// check each adjacent tile
			for (Coordinate pos : adjacent) {
				if (canMove(currentCar, pos)) {
					Path newPath = new Path(currentPath);
					newPath.addToPath(pos);
					PseudoCar newCar = applyMovement(currentCar, pos);
					
					if (!visited.contains(pos) && !unvisited.contains(pos)) {
						// unvisited
						unvisited.add(pos);
						pathMap.put(pos, newPath);
						carMap.put(pos, newCar);
					} else if (newCar.health > carMap.get(pos).health) {
						// visited, but higher health!
						unvisited.add(pos);
						pathMap.put(pos, newPath);
						carMap.put(pos, newCar);
					} else if ((int) newCar.health == (int) carMap.get(pos).health && newPath.getLength() < pathMap.get(pos).getLength()) {
						unvisited.add(pos);
						pathMap.put(pos, newPath);
						carMap.put(pos, newCar);
					//} else if (canRepeat(currentCar, pos, currentPath)) {
					//	unvisited.add(pos);
					//	pathMap.put(pos, newPath);
					//	carMap.put(pos, newCar);
					}
				}
			}
		}
		
		if (pathMap.containsKey(finish)) {
			return pathMap.get(finish);
		}
		
		return null;
	}
	
	/**
	 * Checks if the car can move into a spot
	 * @param car
	 * @param destination
	 * @return
	 */
	public static boolean canMove (CarController carControl, Coordinate destination) {
		// create a simulated car to pathfind with
		PseudoCar car = new PseudoCar(
			carControl.getHealth(),
			carControl.getOrientation(),
			new Coordinate(carControl.getPosition()),
			carControl.getSpeed() != 0
		);
		
		return canMove(car, destination);
	}
	
	private static boolean canMove (PseudoCar car, Coordinate destination) {
		Coordinate start = car.position;
		
		// would this even be a valid, single-space move
		if (!(start == destination || (Math.abs(start.x - destination.x) + Math.abs(start.y - destination.y)) == 1)) {
			return false;
		}
		
		// check if can't reach destination because car is stationary
		Direction dir = Path.fromToDirection(car.position, destination);
		if (!car.isMoving && (
				WorldSpatial.changeDirection(car.orientation, RelativeDirection.LEFT) == dir ||
				WorldSpatial.changeDirection(car.orientation,  RelativeDirection.RIGHT) == dir)) {
			return false;
		}
		// or if we can't immediately reverse because we're moving
		else if (car.isMoving && (WorldSpatial.reverseDirection(car.orientation) == dir)) {
			return false;
		}
		

		// check that we know about the start and destination
		if (!(Mapping.getMap().isExplored(start) && Mapping.getMap().isExplored(destination))) {
			return false;
		}

		MapTile startTile = Mapping.getMap().getTile(start);
		MapTile destinationTile = Mapping.getMap().getTile(destination);
		
		// check against tile types
		// start tile
		if (startTile instanceof LavaTrap) {
			// 
			
		} else if (startTile instanceof HealthTrap) {
			// no restriction
			
		} else if (startTile instanceof GrassTrap) {
			// see if this move requires the car to turn
			Direction beforeDir = car.orientation;
			Direction afterDir = Path.fromToDirection(start, destination);
			
			if (WorldSpatial.changeDirection(beforeDir, RelativeDirection.LEFT) == afterDir ||
					WorldSpatial.changeDirection(beforeDir, RelativeDirection.RIGHT) == afterDir) {
				return false;
			}
		} else if (startTile instanceof MudTrap) {
			// you are already dead
			return false;
			
		}

		
		// destination tile
		if (destinationTile instanceof LavaTrap) {
			// check if the car is gonna get killed by this trap
			if (!(car.health - LavaTrap.HealthDelta *0.25 > 0)) {
				return false;
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
		
		// all tests passed
		return true;
	}
	
	
	
	
	/************************************
	 * PRIVATE METHODS FOR INTERNAL USE *
	 ************************************/
	
	/**
	 * Returns distance between two coordinates (not including diagonal movement)
	 * @param a
	 * @param b
	 * @return
	 */
	private static int distance(Coordinate a, Coordinate b) {
		return Math.abs(a.x-b.x) + Math.abs(a.y-b.y);
	}
	
	
	/**
	 * Updates the state of a car, as if it had followed the given path.
	 * @param path
	 * @return
	 */
	private static PseudoCar updateCar(PseudoCar car, Path path) {
		Coordinate step;
		for (int i = 1; i < path.getLength(); i++) {
			step = path.getStep(i);
			car = applyMovement(car, step);
		}
		
		return car;
	}
	
	/**
	 * Simulates effects of moving a car to the destination.
	 * @param destination
	 * @param car
	 */
	private static PseudoCar applyMovement(PseudoCar car, Coordinate destination) {
		if (!canMove(car, destination)) {
			// shouldn't happen if canMove is checked already!
			return null;
		}

		car = new PseudoCar(car);
		
		// update health
		MapTile tile = Mapping.getMap().getTile(destination);
		if (tile instanceof LavaTrap) {
			car.health -= LavaTrap.HealthDelta * 0.25; 
		} else if (tile instanceof HealthTrap) {
			car.health = Math.min(car.health + HealthTrap.HealthDelta * 0.25f, PseudoCar.maxHealth);
		}
		
		car.isMoving = (car.position != destination);
		
		Direction dir = Path.fromToDirection(car.position, destination);
		if (dir != null) {
			car.orientation = dir;
		}
		
		car.position = destination;
		
		return car;
	}
	
}

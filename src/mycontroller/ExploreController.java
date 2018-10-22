package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import controller.CarController;
import mycontroller.MyAIController.MoveStatus;
import mycontroller.map.Mapping;
import mycontroller.navigation.Navigator;
import mycontroller.navigation.Path;
import mycontroller.navigation.Pathfinding;
import tiles.GrassTrap;
import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MapTile.Type;
import tiles.MudTrap;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import world.WorldSpatial.RelativeDirection;

public class ExploreController extends CarController {

	private Mapping mapping;

	private List<Coordinate> path;

	private MoveStatus moveStatus;

	private boolean isBacktracing;

	private Path helpPath;	// A path that helps to get out of stuck

	private Navigator navigator;

	private List<Coordinate> recordPath;

	private boolean finishedExplore;

	private boolean isApproachingToKey;

	public ExploreController(Car car, CarController myAIController) {
		super(car);
		mapping = Mapping.getMap();
		mapping.initialize(getMap());
		path = new ArrayList<>();
		moveStatus = MoveStatus.STOP;
		isBacktracing = false;
		helpPath = null;
		navigator = new Navigator(this, null);
		recordPath = new ArrayList<>();
		finishedExplore = false;
		isApproachingToKey = false;
	}

	@Override
	public void update() {
		/* Record view */
		Map<Coordinate, MapTile> view = getView();
		mapping.articulateViewPoint(view);
		
		/* Decide what to do next */
		Coordinate keyCoor = uncollectedKeyInView(view);
		if (keyCoor != null && !isApproachingToKey) {
			// If key exists in view and reachable, get key first
			isApproachingToKey = createKeyPath(keyCoor);
			if (isApproachingToKey) {
				return;	// Key reachable
			}
			// Ignore the unreachable key
		}
		if (finishedExplore) {
			// MyAIController will take over since now
		}
		if (navigator.isNavigating()) {
			// If is navigating, updates navigator
			moveStatus = navigator.update();
			return;
		} else {
			if (isApproachingToKey)
				// If not navigating but is approaching to key, change status to not approaching to key
				isApproachingToKey = false;
		}
		if (isStuck() || onLava(new Coordinate(getPosition()), view) && !navigator.isNavigating()) {
			// If it gets stuck or stays on a Lava after navigation, call Pathfinding for another navigation
			recordPath = new ArrayList<>();
			createHelpPath();
			return;
		}
		if (isBacktracing && path.size() < 2) {
			// If it is getting to the start point, call Pathfinding for navigation to an unexplored tile
			createHelpPath();
			return;
		}
		
		MoveEntry moveEntry = nextExploreDirection(view);	// If not being navigated, do classic DFS explore 
		moveInDirection(moveEntry.direction);
		
		/* Record the history coordinates of last 10 steps to detect stuck */
		if (recordPath.size() <= 10) {
			recordPath.add(moveEntry.coordinate);
		} else {
			recordPath.remove(0);
			recordPath.add(moveEntry.coordinate);
		}

	}

	private boolean onLava(Coordinate coordinate, Map<Coordinate, MapTile> view) {
		if (view.get(coordinate) instanceof LavaTrap) {
			return true;
		}
		return false;
	}
	
	private Coordinate uncollectedKeyInView(Map<Coordinate, MapTile> view) {
		for (Entry<Coordinate, MapTile> entry : view.entrySet()) {
			Coordinate coordinate = entry.getKey();
			MapTile mapTile = entry.getValue();
			if (mapTile instanceof LavaTrap) {
				int key = ((LavaTrap) mapTile).getKey();

				// A key exists within the lava
				if (key > 0 && !getKeys().contains(key)) {
					return coordinate;
				}
			}
		}
		return null;
	}

	private boolean isStuck() {
		if (recordPath.size() < 10)
			return false;
		Set<Coordinate> recordSet = new HashSet<>(recordPath);
		return recordSet.size() <= 2;
	}

	/**
	 * Create a path to a seen key if the key is reachable
	 * @param keyCoor
	 * @return
	 */
	private boolean createKeyPath(Coordinate keyCoor) {

		Path keyPath = buildHelpPath(keyCoor);
		if (keyPath != null) {
			navigator = new Navigator(this, new Path(keyPath));
			path.addAll(keyPath.getCoords());
			removeDuplicatePath();
			navigator.update();
			return true;
		}
		return false;	// Seen key not reachable!
	}

	/**
	 * Create a path to an unvisited tile when begin stuck or having traced back to start point
	 */
	private void createHelpPath() {
		helpPath = buildHelpPath(null);
		if (helpPath != null) {
			navigator = new Navigator(this, new Path(helpPath));
			path.addAll(helpPath.getCoords());
			removeDuplicatePath();
			navigator.update();
		} else {
			finishedExplore = true;
		}
	}

	/**
	 * Build a path to a destination given as end parameter. If end is null, then build a path to any unexplored tile
	 * @param end
	 * @return
	 */
	private Path buildHelpPath(Coordinate end) {
		Path path = null;
		if (end != null) {
			path = Pathfinding.linkPoints(this, end);
			return path;
		}
		for (Entry<Coordinate, Boolean> isExplored : mapping.getIsRoadExplored().entrySet()) {
			if (!isExplored.getValue()) {
				List<Coordinate> neighbours = getExploredNeighbour(isExplored.getKey());
				for (Coordinate neighbour : neighbours) {
					path = Pathfinding.linkPoints(this, neighbour);
					if (path != null)
						return path;
				}
			}
		}
		return path;
	}

	/**
	 * Get a list of neighbours of a coordinate that has been explored
	 * @param point
	 * @return
	 */
	private List <Coordinate> getExploredNeighbour(Coordinate point) {
		List<Coordinate> neighbours = new ArrayList<>();
		neighbours.add(new Coordinate(point.x + 1, point.y));
		neighbours.add(new Coordinate(point.x - 1, point.y));
		neighbours.add(new Coordinate(point.x, point.y + 1));
		neighbours.add(new Coordinate(point.x, point.y - 1));
		Map<Coordinate, Boolean> isRoadExplored = mapping.getIsRoadExplored();
		Iterator<Coordinate> iter = neighbours.iterator();
		while(iter.hasNext()) {
			Coordinate c = iter.next();
			if (!isRoadExplored.containsKey(c) || !isRoadExplored.get(c)) {
				iter.remove();
			}
		}
		return neighbours;
	}

	/**
	 * Next direction for map explore based on the current view
	 * @param view
	 * @return A move entry that includes both direction and the coordinate after the move
	 */
	private MoveEntry nextExploreDirection(Map<Coordinate, MapTile> view) {
		Coordinate carPos = new Coordinate(getPosition());
		List<Coordinate> possibleOut = new ArrayList<>();

		/*
		 * record tiles on edges of 9*9 view that are not walls, which means possible
		 * move directions
		 */
		for (int x = carPos.x - getViewSquare(); x <= carPos.x + getViewSquare(); x++) {
			int y = carPos.y + getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (!tile.isType(Type.WALL)) {
				possibleOut.add(c);
			}
		}
		for (int x = carPos.x - getViewSquare(); x <= carPos.x + getViewSquare(); x++) {
			int y = carPos.y - getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (!tile.isType(Type.WALL)) {
				possibleOut.add(c);
			}
		}
		for (int y = carPos.y - getViewSquare() + 1; y <= carPos.y + getViewSquare() - 1; y++) {
			int x = carPos.x + getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (!tile.isType(Type.WALL)) {
				possibleOut.add(c);
			}
		}
		for (int y = carPos.y - getViewSquare() + 1; y <= carPos.y + getViewSquare() - 1; y++) {
			int x = carPos.x - getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (!tile.isType(Type.WALL)) {
				possibleOut.add(c);
			}
		}

		/* Get possible next moves to an out tile with its neighbour not explored */
		Map<Coordinate, Coordinate> candidates = nextExploreCoors(carPos, possibleOut, view);
		Map<Direction, Coordinate> nextMoves = new HashMap<>();
		for (Entry<Coordinate, Coordinate> entry : candidates.entrySet()) {
			List<Coordinate> adjacents = new ArrayList<>();
			int outX = entry.getKey().x;
			int outY = entry.getKey().y;
			if (outX - carPos.x == getViewSquare()) {
				adjacents.add(new Coordinate(outX + 1, outY));
			}
			if (outX - carPos.x == -getViewSquare()) {
				adjacents.add(new Coordinate(outX - 1, outY));
			}
			if (outY - carPos.y == getViewSquare()) {
				adjacents.add(new Coordinate(outX, outY + 1));
			}
			if (outY - carPos.y == -getViewSquare()) {
				adjacents.add(new Coordinate(outX, outY - 1));
			}
			for (Coordinate adj : adjacents) {

				/* If adj is valid to be explored */
				if (mapping.containsCoordinate(adj) && !mapping.getTile(adj).isType(Type.WALL)
						&& !mapping.isExplored(adj)) {
					Direction direction = coordinateToDirection(carPos, entry.getValue());
					nextMoves.put(direction, entry.getValue());
				}
			}
		}
		if (!nextMoves.isEmpty()) {

			/* Always go the most right direction at branches */
			Direction carFace = getOrientation();
			Direction right = WorldSpatial.changeDirection(carFace, RelativeDirection.RIGHT);
			Direction left = WorldSpatial.changeDirection(carFace, RelativeDirection.LEFT);
			Direction forward = carFace;
			Direction backward = WorldSpatial.reverseDirection(carFace);
			Direction next = null;
			if (nextMoves.containsKey(right)) {
				next = right;
			} else if (nextMoves.containsKey(forward)) {
				next = forward;
			} else if (nextMoves.containsKey(left)) {
				next = left;
			} else if (nextMoves.containsKey(backward)) {
				next = backward;
			}
			if (isBacktracing) {
				path.add(carPos);
				isBacktracing = false;
			}
			path.add(nextMoves.get(next));
			removeDuplicatePath();
			return new MoveEntry(next, nextMoves.get(next));
		}

		/*
		 * If tiles around the view are all explored, back trace the path until see an
		 * unexplored tile
		 */
		if (!isBacktracing) {
			isBacktracing = true;
		}

		/* check if car needs to brake when beginning backtracing */
		Direction nextDir = coordinateToDirection(carPos, path.get(path.size() - 2)); // direction of carPos -> the
																						// first coordinate in backtrace
		if (!(moveStatus == MoveStatus.FORWARD && WorldSpatial.reverseDirection(getOrientation()).equals(nextDir))
				&& !(moveStatus == MoveStatus.BACKWARD && getOrientation().equals(nextDir))) {
			path.remove(carPos);
		}
		Coordinate previousCoor = path.remove(path.size() - 1);

		return new MoveEntry(coordinateToDirection(carPos, previousCoor), previousCoor);
	}

	/**
	 * A util class for recording a combination of direction and the coordinate at the direction
	 * @author jiashany
	 *
	 */
	private class MoveEntry {
		public Direction direction;
		public Coordinate coordinate;

		public MoveEntry(Direction direction, Coordinate coordinate) {
			this.direction = direction;
			this.coordinate = coordinate;
		}
	}

	/**
	 * Remove duplicated sub-paths in the backtracing path to avoid car moving bugs and saving time
	 */
	private void removeDuplicatePath() {
		Map<Coordinate, Integer> count = new HashMap<>();
		for (Coordinate point : path) {
			if (count.containsKey(point)) {
				count.put(point, count.get(point) + 1);
			} else {
				count.put(point, 1);
			}
		}
		for (Entry<Coordinate, Integer> entry : count.entrySet()) {
			if (entry.getValue() > 1) {
				Coordinate duplicatePoint = entry.getKey();
				int start = path.indexOf(duplicatePoint);
				int end = path.lastIndexOf(duplicatePoint);
				for (int i = start; i < end; i++) {
					path.remove(start);
				}
			}
		}
	}

	/**
	 * Find next possible moves and return a map with key being out tile and value being first step to reach that out tile
	 * @param car
	 * @param outs
	 * @param view
	 * @return
	 */
	private Map<Coordinate, Coordinate> nextExploreCoors(Coordinate car, List<Coordinate> outs,
			Map<Coordinate, MapTile> view) {
		Map<Coordinate, Coordinate> nextExploreCoors = new HashMap<>();

		/* BFS to check if there is a way to an out tile */
		Map<Coordinate, Boolean> visited = new HashMap<>();
		for (Coordinate key : view.keySet()) {
			visited.put(key, false);
		}
		List<Coordinate> availableNextCoors = availableNextCoors(view);
		LinkedList<Coordinate> queue = new LinkedList<>();
		Map<Coordinate, Coordinate> parent = new HashMap<>();
		queue.add(car);
		visited.put(car, true);
		while (!queue.isEmpty()) {
			Coordinate v = queue.poll();
			if (outs.contains(v)) {
				List<Coordinate> path = backtrace(parent, car, v);
				nextExploreCoors.put(v, path.get(1));
			}
			if (v == car) {
				for (Coordinate w : availableNextCoors) {
					if (!visited.get(w)) {
						queue.add(w);
						visited.put(w, true);
						parent.put(w, v);
					}
				}
			} else {
				for (Coordinate w : getNeighbour(v, view)) {
					if (!visited.get(w)) {
						queue.add(w);
						visited.put(w, true);
						parent.put(w, v);
					}
				}
			}
		}
		return nextExploreCoors;

	}

	/**
	 * Return a list of legal neighbours around the car position when running BFS
	 * @param view
	 * @return
	 */
	private List<Coordinate> availableNextCoors(Map<Coordinate, MapTile> view) {
		List<Coordinate> availableCoors = new ArrayList<>();
		Coordinate carPos = new Coordinate(getPosition());
		Map<Direction, Coordinate> candidates = new HashMap<>();

		candidates.put(Direction.EAST, new Coordinate(carPos.x + 1, carPos.y));
		candidates.put(Direction.NORTH, new Coordinate(carPos.x, carPos.y + 1));
		candidates.put(Direction.WEST, new Coordinate(carPos.x - 1, carPos.y));
		candidates.put(Direction.SOUTH, new Coordinate(carPos.x, carPos.y - 1));

		for (Entry<Direction, Coordinate> entry : candidates.entrySet()) {
			Direction dir = entry.getKey();
			Coordinate coor = entry.getValue();
			if (!view.get(coor).isType(Type.WALL) && !(view.get(coor) instanceof MudTrap)
					&& !(view.get(coor) instanceof LavaTrap) && isFeasible(dir, view)) {
				availableCoors.add(coor);
			}
		}

		return availableCoors;
	}

	/**
	 * Get legal neighbours of a tile when running BFS
	 * @param v
	 * @param view
	 * @return
	 */
	private List<Coordinate> getNeighbour(Coordinate v, Map<Coordinate, MapTile> view) {
		List<Coordinate> neighbours = new ArrayList<>();
		Coordinate carPos = new Coordinate(getPosition());
		neighbours.add(new Coordinate(v.x - 1, v.y));
		neighbours.add(new Coordinate(v.x + 1, v.y));
		neighbours.add(new Coordinate(v.x, v.y + 1));
		neighbours.add(new Coordinate(v.x, v.y - 1));
		Iterator<Coordinate> iter = neighbours.iterator();
		while (iter.hasNext()) {
			Coordinate neighbour = iter.next();
			if (Math.abs(neighbour.x - carPos.x) > getViewSquare()
					|| Math.abs(neighbour.y - carPos.y) > getViewSquare()) {
				iter.remove();
				continue;
			}
			if (view.get(neighbour).isType(Type.WALL)/* || view.get(neighbour) instanceof LavaTrap */) {
				iter.remove();
			}
		}
		return neighbours;
	}

	/**
	 * Backtrace the recorded path that starts from start and ends at the current car position
	 * @param parent
	 * @param start
	 * @param end
	 * @return
	 */
	private List<Coordinate> backtrace(Map<Coordinate, Coordinate> parent, Coordinate start, Coordinate end) {
		List<Coordinate> path = new ArrayList<>();
		path.add(end);
		while (path.get(path.size() - 1) != start) {
			path.add(parent.get(path.get(path.size() - 1)));
		}
		Collections.reverse(path);
		return path;
	}

	/**
	 * If a direction is feasible to be applied to the car 
	 * @param d
	 * @param view
	 * @return
	 */
	private boolean isFeasible(Direction d, Map<Coordinate, MapTile> view) {
		Direction carFace = getOrientation();
		Coordinate carPos = new Coordinate(getPosition());
		if ((moveStatus == MoveStatus.STOP || view.get(carPos) instanceof GrassTrap)
				&& (WorldSpatial.changeDirection(carFace, RelativeDirection.LEFT) == d
						|| WorldSpatial.changeDirection(carFace, RelativeDirection.RIGHT) == d))
			return false;
		return true;
	}

	/**
	 * Move in a direction
	 * @param direction
	 */
	private void moveInDirection(Direction direction) {
		if (getOrientation() == direction) {
			moveForward();
		} else if (WorldSpatial.reverseDirection(getOrientation()) == direction) {
			moveBackward();
		} else if (WorldSpatial.changeDirection(getOrientation(), RelativeDirection.LEFT) == direction) {
			turnLeft();
		} else if (WorldSpatial.changeDirection(getOrientation(), RelativeDirection.RIGHT) == direction) {
			turnRight();
		} else { // if direction is null
			applyBrake();
			moveStatus = MoveStatus.STOP;
		}
	}

	/**
	 * Move Forward
	 */
	private void moveForward() {
		if (moveStatus == MoveStatus.BACKWARD) {
			applyBrake();
			moveStatus = MoveStatus.STOP;
		} else if (moveStatus != MoveStatus.BACKWARD && getSpeed() < MyAIController.CAR_MAX_SPEED) {
			applyForwardAcceleration();
			moveStatus = MoveStatus.FORWARD;
		}
	}

	/**
	 * Move backward
	 */
	private void moveBackward() {
		if (moveStatus == MoveStatus.FORWARD) {
			applyBrake();
			moveStatus = MoveStatus.STOP;
		} else if (moveStatus != MoveStatus.FORWARD && getSpeed() < MyAIController.CAR_MAX_SPEED) {
			applyReverseAcceleration();
			moveStatus = MoveStatus.BACKWARD;
		}
	}

	/**
	 * Return a direction given a start and a end coordinates
	 * @param start
	 * @param end
	 * @return
	 */
	private Direction coordinateToDirection(Coordinate start, Coordinate end) {
		int deltaX = end.x - start.x;
		int deltaY = end.y - start.y;
		if (deltaX == 1) {
			return Direction.EAST;
		} else if (deltaX == -1) {
			return Direction.WEST;
		} else if (deltaY == 1) {
			return Direction.NORTH;
		} else if (deltaY == -1) {
			return Direction.SOUTH;
		}
		return null; // break
	}

	/**
	 * The following code are from discussion board posted by Philip to get velocity 
	 */
	boolean lastAccelForward = true; // Initial value doesn't matter as speed starts as zero

	@Override
	public float getSpeed() {
	return lastAccelForward ? super.getSpeed() : -super.getSpeed();
	}

	@Override
	public void applyForwardAcceleration(){ 
	super.applyForwardAcceleration();
	lastAccelForward = true;
	}

	@Override
	public void applyReverseAcceleration(){
	super.applyReverseAcceleration();
	lastAccelForward = false;
	}
}

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
import java.util.Stack;

import controller.CarController;
import mycontroller.navigation.Path;
import mycontroller.navigation.Pathfinding;
import tiles.GrassTrap;
import tiles.MapTile;
import tiles.MapTile.Type;
import tiles.MudTrap;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import world.WorldSpatial.RelativeDirection;

public class ExploreController extends CarController {

	private Map<Coordinate, MapTile> map; // Temporary

	private Map<Coordinate, Boolean> isRoadExplored; // Temporary

	private List<Coordinate> path;

	private enum MoveStatus {
		STOP, FORWARD, BACKWARD
	}

	private MoveStatus moveStatus;

	private boolean isBacktracing;

	private int specialCount;

	private Direction backtracingStartMove;

	public ExploreController(Car car) {
		super(car);
		map = getMap();
		isRoadExplored = new HashMap<>();
		for (Entry<Coordinate, MapTile> entry : map.entrySet()) {
			if (entry.getValue().isType(Type.ROAD) || entry.getValue().isType(Type.START)
					|| entry.getValue().isType(Type.FINISH))
				isRoadExplored.put(entry.getKey(), false);
		}
		path = new ArrayList<>();
		moveStatus = MoveStatus.STOP;
		isBacktracing = false;
		specialCount = 2;
		backtracingStartMove = null;
	}

	@Override
	public void update() {
		Map<Coordinate, MapTile> view = getView();
		recordView(view);
		boolean allExplored = true;
		for (Entry<Coordinate, Boolean> entry : isRoadExplored.entrySet()) {
			if (!entry.getValue())
//				System.out.println(entry);
				allExplored = false;
		}
		Direction d = nextExploreDirection(view);
		moveInDirection(d);
		if (allExplored)
			System.out.println("all explored");
	}

	private void recordView(Map<Coordinate, MapTile> view) {
		for (Entry<Coordinate, MapTile> entry : view.entrySet()) {
			if (entry.getValue().isType(Type.TRAP) || entry.getValue().isType(Type.ROAD)
					|| entry.getValue().isType(Type.START) || entry.getValue().isType(Type.FINISH)) {
				map.put(entry.getKey(), entry.getValue());
				isRoadExplored.put(entry.getKey(), true);
			}
		}
	}

	private Direction nextExploreDirection(Map<Coordinate, MapTile> view) {
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
				if (map.containsKey(adj) && !map.get(adj).isType(Type.WALL) && !isRoadExplored.get(adj)) { // adj is
																											// valid to
																											// be
																											// explored
					Direction direction = coordinateToDirection(carPos, entry.getValue());
					nextMoves.put(direction, entry.getValue());
				}
			}
		}
		if (!nextMoves.isEmpty()) {

			/* always go the most right direction at cross */
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
			System.out.println(nextMoves.get(next) + " added to path.");
			System.out.println(path);
			return next;
		}

		/*
		 * If tiles around the view are all explored, back trace the path until see an
		 * unexplored tile
		 */
		if (!isBacktracing) {
			isBacktracing = true;
		}
		Direction nextDir = coordinateToDirection(carPos, path.get(path.size() - 2));
		if (!(moveStatus == MoveStatus.FORWARD && nextDir.equals(WorldSpatial.reverseDirection(getOrientation())))
				&& !(moveStatus == MoveStatus.BACKWARD && nextDir.equals(getOrientation()))) {
			path.remove(carPos);
		}
		Coordinate previousCoor = path.remove(path.size() - 1);
		System.out.println(path);
		System.out.println("Backtrace: " + previousCoor);
		System.out.println("Car position: " + carPos);

		return coordinateToDirection(carPos, previousCoor);
	}

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

	private Map<Coordinate, Coordinate> nextExploreCoors(Coordinate car, List<Coordinate> outs,
			Map<Coordinate, MapTile> view) {
		Map<Coordinate, Boolean> visited = new HashMap<>();
		for (Coordinate key : view.keySet()) {
			visited.put(key, false);
		}
		Map<Coordinate, Coordinate> nextExploreCoors = new HashMap<>();
		List<Coordinate> availableNextCoors = availableNextCoors(view);
		LinkedList<Coordinate> queue = new LinkedList<>();
		Map<Coordinate, Coordinate> parent = new HashMap<>();

		/* BFS to check if there is a way to an out tile */
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

	private List<Coordinate> availableNextCoors(Map<Coordinate, MapTile> view) {
		List<Coordinate> coordinates = new ArrayList<>();
		Coordinate carPos = new Coordinate(getPosition());

		/* check NORTH */
		Coordinate northCoordinate = new Coordinate(carPos.x, carPos.y + 1);
		if (!view.get(northCoordinate).isType(Type.WALL) && !(view.get(northCoordinate) instanceof MudTrap)
				&& isFeasible(Direction.NORTH, view))
			coordinates.add(northCoordinate);

		/* check EAST */
		Coordinate eastCoordinate = new Coordinate(carPos.x + 1, carPos.y);
		if (!view.get(eastCoordinate).isType(Type.WALL) && isFeasible(Direction.EAST, view))
			coordinates.add(eastCoordinate);

		/* check SOUTH */
		Coordinate southCoordinate = new Coordinate(carPos.x, carPos.y - 1);
		if (!view.get(southCoordinate).isType(Type.WALL) && isFeasible(Direction.SOUTH, view))
			coordinates.add(southCoordinate);

		/* check WEST */
		Coordinate westCoordinate = new Coordinate(carPos.x - 1, carPos.y);
		if (!view.get(westCoordinate).isType(Type.WALL) && isFeasible(Direction.WEST, view))
			coordinates.add(westCoordinate);

		return coordinates;
	}

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
			if (view.get(neighbour).isType(Type.WALL)) {
				iter.remove();
			}
		}
		return neighbours;
	}

	private List<Coordinate> backtrace(Map<Coordinate, Coordinate> parent, Coordinate start, Coordinate end) {
		List<Coordinate> path = new ArrayList<>();
		path.add(end);
		while (path.get(path.size() - 1) != start) {
			path.add(parent.get(path.get(path.size() - 1)));
		}
		Collections.reverse(path);
		return path;
	}

	private boolean isFeasible(Direction d, Map<Coordinate, MapTile> view) {
		Direction carFace = getOrientation();
		Coordinate carPos = new Coordinate(getPosition());
		if ((moveStatus == MoveStatus.STOP || view.get(carPos) instanceof GrassTrap)
				&& (WorldSpatial.changeDirection(carFace, RelativeDirection.LEFT) == d
						|| WorldSpatial.changeDirection(carFace, RelativeDirection.RIGHT) == d))
			return false;
		return true;
	}

	private void moveInDirection(Direction direction) {
		System.out.println("Go " + direction);
		if (getOrientation() == direction) {
			System.out.println("Forward");
			moveForward();
		} else if (WorldSpatial.reverseDirection(getOrientation()) == direction) {
			System.out.println("Backward");
			moveBackward();
		} else if (WorldSpatial.changeDirection(getOrientation(), RelativeDirection.LEFT) == direction) {
			System.out.println("Left");
			turnLeft();
		} else if (WorldSpatial.changeDirection(getOrientation(), RelativeDirection.RIGHT) == direction) {
			System.out.println("Right");
			turnRight();
		} else { // if direction is null
			applyBrake();
			moveStatus = MoveStatus.STOP;
		}
	}

	private void moveForward() {
		if (moveStatus == MoveStatus.BACKWARD) {
			applyBrake();
			moveStatus = MoveStatus.STOP;
		} else if (moveStatus != MoveStatus.BACKWARD && getSpeed() < MyAIController.CAR_MAX_SPEED) {
			applyForwardAcceleration();
			moveStatus = MoveStatus.FORWARD;
		}
	}

	private void moveBackward() {
		if (moveStatus == MoveStatus.FORWARD) {
			applyBrake();
			moveStatus = MoveStatus.STOP;
		} else if (moveStatus != MoveStatus.FORWARD && getSpeed() < MyAIController.CAR_MAX_SPEED) {
			applyReverseAcceleration();
			moveStatus = MoveStatus.BACKWARD;
		}
	}

	/* -------- util ----------- */

	private Direction coordinateToDirection(Coordinate start, Coordinate end) {
		int deltaX = end.x - start.x;
		int deltaY = end.y - start.y;
		if (deltaX > 0) {
			return Direction.EAST;
		} else if (deltaX < 0) {
			return Direction.WEST;
		} else if (deltaY > 0) {
			return Direction.NORTH;
		} else if (deltaY < 0) {
			return Direction.SOUTH;
		}
		return null; // break
	}

	private Coordinate directionToCoordinate(Direction d) {
		Coordinate car = new Coordinate(getPosition());
		switch (d) {
		case NORTH:
			return new Coordinate(car.x, car.y + 1);
		case EAST:
			return new Coordinate(car.x + 1, car.y);
		case SOUTH:
			return new Coordinate(car.x, car.y - 1);
		case WEST:
			return new Coordinate(car.x - 1, car.y);
		default:
			return null;
		}
	}

}

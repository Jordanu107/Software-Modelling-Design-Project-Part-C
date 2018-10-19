package mycontroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import controller.CarController;
import tiles.MapTile;
import tiles.MapTile.Type;
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

	public ExploreController(Car car) {
		super(car);
		map = getMap();
		isRoadExplored = new HashMap<>();
		for (Entry<Coordinate, MapTile> entry : map.entrySet()) {
			if (entry.getValue().isType(Type.ROAD) || entry.getValue().isType(Type.START) || entry.getValue().isType(Type.FINISH))
				isRoadExplored.put(entry.getKey(), false);
		}
		path = new ArrayList<>();
		moveStatus = MoveStatus.STOP;
	}

	@Override
	public void update() {
		Map<Coordinate, MapTile> view = getView();
		recordView(view);
		Direction d = nextExploreDirection(view);
		moveInDirection(d);
	}

	private void recordView(Map<Coordinate, MapTile> view) {
		for (Entry<Coordinate, MapTile> entry : view.entrySet()) {
			if (entry.getValue().isType(Type.TRAP) || entry.getValue().isType(Type.ROAD) || entry.getValue().isType(Type.START) || entry.getValue().isType(Type.FINISH)) {
				map.put(entry.getKey(), entry.getValue());
				isRoadExplored.put(entry.getKey(), true);
			}
		}
	}

	private Direction nextExploreDirection(Map<Coordinate, MapTile> view) {
		Coordinate carPos = new Coordinate(getPosition());
		List<Coordinate> possibleOut = new ArrayList<>();
		
		/* record tiles on edges of 9*9 view that are not walls, which means possible move directions */
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

		for (Entry<Coordinate, Coordinate> entry : candidates.entrySet()) {
			Coordinate adjacent = null;
			int outX = entry.getKey().x;
			int outY = entry.getKey().y;
			if (outX - carPos.x == getViewSquare()) {
				adjacent = new Coordinate(outX + 1, outY);
			} else if (outX - carPos.x == -getViewSquare()) {
				adjacent = new Coordinate(outX - 1, outY);
			} else if (outY - carPos.y == getViewSquare()) {
				adjacent = new Coordinate(outX, outY + 1);
			} else if (outY - carPos.y == -getViewSquare()) {
				adjacent = new Coordinate(outX, outY - 1);
			}
			if (!map.get(adjacent).isType(Type.WALL) && !isRoadExplored.get(adjacent)) {
				return coordinateToDirection(carPos, entry.getValue());
			}
		}
		//TODO: if tiles around the view are all explored, back trace the path until see an unexplored tile
		
		return null;	// should't happen
	}

	private Map<Coordinate, Coordinate> nextExploreCoors(Coordinate car, List<Coordinate> outs,
			Map<Coordinate, MapTile> view) {
		Map<Coordinate, Boolean> visited = new HashMap<>();
		for (Coordinate key : view.keySet()) {
			visited.put(key, false);
		}
		Map<Coordinate, Coordinate> nextExploreCoors = new HashMap<>();
		List<Coordinate> availableNextMove = availableNextCoors(view);
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
				for (Coordinate w : availableNextMove) {
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
		if (!view.get(northCoordinate).isType(Type.WALL) && isFeasible(Direction.NORTH))
			coordinates.add(northCoordinate);

		/* check EAST */
		Coordinate eastCoordinate = new Coordinate(carPos.x + 1, carPos.y);
		if (!view.get(eastCoordinate).isType(Type.WALL) && isFeasible(Direction.EAST))
			coordinates.add(eastCoordinate);

		/* check SOUTH */
		Coordinate southCoordinate = new Coordinate(carPos.x, carPos.y - 1);
		if (!view.get(southCoordinate).isType(Type.WALL) && isFeasible(Direction.SOUTH))
			coordinates.add(southCoordinate);

		/* check WEST */
		Coordinate westCoordinate = new Coordinate(carPos.x - 1, carPos.y);
		if (!view.get(westCoordinate).isType(Type.WALL) && isFeasible(Direction.WEST))
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
		while(path.get(path.size()-1) != start) {
			path.add(parent.get(path.get(path.size()-1)));
		}
		Collections.reverse(path);
		return path;
	}

	private boolean isFeasible(Direction d) {
		Direction carFace = getOrientation();
		if (moveStatus == MoveStatus.STOP && (WorldSpatial.changeDirection(carFace, RelativeDirection.LEFT) == d
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
		} else {
			applyBrake();	// if direction is null
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
		return null;	// should't happen
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

package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Stack;

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

	private int wallSensitivity = 1;

	private enum MoveStatus {
		STOP, FORWARD, BACKWARD
	}

	private MoveStatus moveStatus;

	public ExploreController(Car car) {
		super(car);
		map = getMap();
		isRoadExplored = new HashMap<>();
		for (Entry<Coordinate, MapTile> entry : map.entrySet()) {
			if (entry.getValue().isType(Type.ROAD))
				isRoadExplored.put(entry.getKey(), false);
		}
		moveStatus = MoveStatus.STOP;
	}

	@Override
	public void update() {
		Map<Coordinate, MapTile> view = getView();
		recordView(view);
		Direction d = AIDirections(view);
		moveIn(d);
	}

	private void recordView(Map<Coordinate, MapTile> view) {
		for (Entry<Coordinate, MapTile> entry : view.entrySet()) {
			if (entry.getValue().isType(Type.TRAP) || entry.getValue().isType(Type.ROAD)) {
				map.put(entry.getKey(), entry.getValue());
				isRoadExplored.put(entry.getKey(), true);
			}
		}
	}

	private boolean isViewExplored(Map<Coordinate, MapTile> view) {
		for (Entry<Coordinate, MapTile> entry : view.entrySet()) {
			if (!entry.getValue().isType(Type.EMPTY) && !isRoadExplored.get(entry.getKey()))
				return false;
		}
		return true;
	}

	private Direction AIDirections(Map<Coordinate, MapTile> view) {
		Coordinate carPos = new Coordinate(getPosition());
		List<Coordinate> possibleOut = new ArrayList<>();
		for (int x = carPos.x - getViewSquare(); x <= carPos.x + getViewSquare(); x++) {
			int y = carPos.y + getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (tile.isType(Type.ROAD)) {
				possibleOut.add(c);
			}
		}
		for (int x = carPos.x - getViewSquare(); x <= carPos.x + getViewSquare(); x++) {
			int y = carPos.y - getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (tile.isType(Type.ROAD)) {
				possibleOut.add(c);
			}
		}
		for (int y = carPos.y - getViewSquare() + 1; y <= carPos.y + getViewSquare() - 1; y++) {
			int x = carPos.x + getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (tile.isType(Type.ROAD)) {
				possibleOut.add(c);
			}
		}
		for (int y = carPos.y - getViewSquare() + 1; y <= carPos.y + getViewSquare() - 1; y++) {
			int x = carPos.x - getViewSquare();
			Coordinate c = new Coordinate(x, y);
			MapTile tile = view.get(c);
			if (tile.isType(Type.ROAD)) {
				possibleOut.add(c);
			}
		}
		Map<Coordinate, Direction> candidates = nextMoveDirections(carPos, possibleOut, view);
		/*
		for (Entry<Coordinate, Direction> entry : candidates.entrySet()) {
			Coordinate adjacent = null;
			int adjacentX = entry.getKey().x;
			int adjacentY = entry.getKey().y;
			if (adjacentX - carPos.x == getViewSquare()) {
				adjacent = new Coordinate(adjacentX + 1, adjacentY);
			} else if (adjacentX - carPos.x == -getViewSquare()) {
				adjacent = new Coordinate(adjacentX - 1, adjacentY);
			} else if (adjacentY - carPos.y == getViewSquare()) {
				adjacent = new Coordinate(adjacentX, adjacentY + 1);
			} else if (adjacentY - carPos.y == -getViewSquare()) {
				adjacent = new Coordinate(adjacentX, adjacentY - 1);
			}
			if (!isRoadExplored.get(adjacent)){
				return entry.getValue();
			}
		}
		*/
		Direction nextDirection = null;
		int northCount = 0, eastCount = 0, southCount = 0, westCount = 0, max = 0;
		for (Entry<Coordinate, Direction> entry : candidates.entrySet()) {
			switch (entry.getValue()) {
			case NORTH:
				northCount ++;
				if (northCount > max) {
					max = northCount;
					nextDirection = Direction.NORTH;
				}
				break;
			case EAST:
				eastCount ++;
				if (eastCount > max) {
					max = eastCount;
					nextDirection = Direction.EAST;
				}
				break;
			case SOUTH:
				southCount ++;
				if (southCount > max) {
					max = southCount;
					nextDirection = Direction.SOUTH;
				}
				break;
			case WEST:
				westCount ++;
				if (westCount > max) {
					max = westCount;
					nextDirection = Direction.WEST;
				}
				break;
			default:
				break;
			}
		}
		return nextDirection;
		
	}

	private Map<Coordinate, Direction> nextMoveDirections(Coordinate car, List<Coordinate> outs, Map<Coordinate, MapTile> view) {
		Map<Coordinate, Boolean> visited = new HashMap<>();
		for (Coordinate key : view.keySet()) {
			visited.put(key, false);
		}
		Map<Coordinate, Direction> feasibleMove = new HashMap<>();
		Stack<Coordinate> stack = new Stack<>();
		stack.push(car);
		Coordinate carNeighbour = null;
		Map<Coordinate, Direction> availableNextMove = availableNextCoor(view);
		while (!stack.isEmpty()) {
			Coordinate v = stack.pop();
			if (!visited.get(v)) {
				visited.put(v, true);
				if (availableNextMove.containsKey(v)) {
					carNeighbour = v;
				}
				if (outs.contains(v)) {
					feasibleMove.put(v, availableNextMove.get(carNeighbour));
				}
				if (v == car) {
					for (Coordinate w : availableNextMove.keySet()) {
						stack.push(w);
					}
				} else {
					for (Coordinate w : getNeighbour(v, view)) {
						stack.push(w);
					}
				}
			}
		}
		return feasibleMove;
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
			if (!view.get(neighbour).isType(Type.ROAD)) {
				iter.remove();
			}
		}
		return neighbours;
	}

	private Map<Coordinate, Direction> availableNextCoor(Map<Coordinate, MapTile> view) {
		List<Direction> directions = availableDirections(view);
		Map<Coordinate, Direction> nextCoors = new HashMap<>();
		Coordinate car = new Coordinate(getPosition());
		for (Direction d : directions) {
			switch (d) {
			case NORTH:
				nextCoors.put(new Coordinate(car.x, car.y + 1), Direction.NORTH);
				break;
			case EAST:
				nextCoors.put(new Coordinate(car.x + 1, car.y), Direction.EAST);
				break;
			case SOUTH:
				nextCoors.put(new Coordinate(car.x, car.y - 1), Direction.SOUTH);
				break;
			case WEST:
				nextCoors.put(new Coordinate(car.x - 1, car.y), Direction.WEST);
				break;
			default:
				break;
			}
		}
		return nextCoors;
	}

	private List<Direction> availableDirections(Map<Coordinate, MapTile> view) {
		List<Direction> directions = new ArrayList<>();
		Coordinate carPos = new Coordinate(getPosition());
		boolean available;

		/* check if NORTH is feasible */
		available = true;
		for (int d = 1; d <= wallSensitivity; d++) {
			if (view.get(new Coordinate(carPos.x, carPos.y + d)).isType(Type.WALL)) {
				available = false;
			}
		}
		if (available && isFeasible(Direction.NORTH))
			directions.add(Direction.NORTH);

		/* check if EAST is feasible */
		available = true;
		for (int d = 1; d <= wallSensitivity; d++) {
			if (view.get(new Coordinate(carPos.x + d, carPos.y)).isType(Type.WALL)) {
				available = false;
			}
		}
		if (available && isFeasible(Direction.EAST))
			directions.add(Direction.EAST);

		/* check if SOUTH is feasible */
		available = true;
		for (int d = 1; d <= wallSensitivity; d++) {
			if (view.get(new Coordinate(carPos.x, carPos.y - d)).isType(Type.WALL)) {
				available = false;
			}
		}
		if (available && isFeasible(Direction.SOUTH))
			directions.add(Direction.SOUTH);

		/* check if WEST is feasible */
		available = true;
		for (int d = 1; d <= wallSensitivity; d++) {
			if (view.get(new Coordinate(carPos.x - d, carPos.y)).isType(Type.WALL)) {
				available = false;
			}
		}
		if (available && isFeasible(Direction.WEST))
			directions.add(Direction.WEST);

		return directions;
	}

	private boolean isFeasible(Direction d) {
		Direction carFace = getOrientation();
		if (moveStatus == MoveStatus.STOP && (WorldSpatial.changeDirection(carFace, RelativeDirection.LEFT) == d
				|| WorldSpatial.changeDirection(carFace, RelativeDirection.RIGHT) == d))
			return false;
		return true;
	}

	private void moveIn(Direction d) {
		System.out.println("Go " + d);
		if (getOrientation() == d) {
			System.out.println("Forward");
			moveForward();
		} else if (WorldSpatial.reverseDirection(getOrientation()) == d) {
			System.out.println("Backward");
			moveBackward();
		} else if (WorldSpatial.changeDirection(getOrientation(), RelativeDirection.LEFT) == d) {
			System.out.println("Left");
			turnLeft();
		} else if (WorldSpatial.changeDirection(getOrientation(), RelativeDirection.RIGHT) == d) {
			System.out.println("Right");
			turnRight();
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
}

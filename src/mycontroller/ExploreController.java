package mycontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

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
		List<Direction> directions = availableDirections(view);
		Direction d = directions.get(new Random().nextInt(directions.size()));
		moveInDirection(d);
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
		if (moveStatus == MoveStatus.STOP && 
				(WorldSpatial.changeDirection(carFace, RelativeDirection.LEFT) == d
				|| WorldSpatial.changeDirection(carFace, RelativeDirection.RIGHT) == d))
			return false;
		return true;
	}

	private void moveInDirection(Direction d) {
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

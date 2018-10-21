package mycontroller.navigation;

import controller.CarController;
import mycontroller.MyAIController;
import world.WorldSpatial;
import world.WorldSpatial.Direction;
import world.WorldSpatial.RelativeDirection;

/**
 * Facilitates car control in order to navigate along a specified path.
 * ########## Currently assumes the car is in the starting position of the path, facing towards the next position, at the start of navigation ################
 * @author Lawson Wang-Wills
 *
 */
public class Navigator {
	
	
	// input parameters
	private CarController car;
	private Path path;
	
	// runtime parameters
	private int currentStep;
	
	private enum MoveStatus {
		STOP, FORWARD, BACKWARD
	}

	private MoveStatus moveStatus;
	
	public Navigator(CarController car, Path path) {
		this.car = car;
		this.path = path;
		moveStatus = MoveStatus.STOP;
		currentStep = 0;
	}
	
	
	public void update() {
		System.out.println(currentStep + ": " + path.getStep(currentStep));
		if (!isNavigating()) {
			car.applyBrake();
			return;
		}

		Direction direction = path.getDirectionAtStep(currentStep);
		boolean repeatStep = false; // we may not be able to proceed to the next step of the path immediately
		
		// check for turns
		if (WorldSpatial.changeDirection(car.getOrientation(), RelativeDirection.LEFT) == direction) {
			car.turnLeft();
		}
		else if (WorldSpatial.changeDirection(car.getOrientation(), RelativeDirection.RIGHT) == direction) {
			car.turnRight();
		}
		// check for reverse
		else if (WorldSpatial.reverseDirection(car.getOrientation()) == direction) {
//			if (car.getSpeed() > 0) {
//				// gotta brake and continue next step
//				car.applyBrake();
//				repeatStep = true;
//			} else {
//				car.applyReverseAcceleration();
//			}
			moveBackward();
		}
		else if (car.getOrientation() == direction) {
//			if (car.getSpeed() < 0) {
//				// gotta brake and continue next step
//				car.applyBrake();
//				repeatStep = true;
//			} else {
//				car.applyForwardAcceleration();
//			}
			moveForward();
		} else if (null == direction){
			// the car remains stationary
			car.applyBrake();
		}
		
		
		if (!repeatStep) {
			currentStep++;
		}
	}
	
	private void moveForward() {
		if (moveStatus == MoveStatus.BACKWARD) {
			car.applyBrake();
			moveStatus = MoveStatus.STOP;
		} else if (moveStatus != MoveStatus.BACKWARD && car.getSpeed() < MyAIController.CAR_MAX_SPEED) {
			car.applyForwardAcceleration();
			moveStatus = MoveStatus.FORWARD;
		}
	}

	private void moveBackward() {
		if (moveStatus == MoveStatus.FORWARD) {
			car.applyBrake();
			moveStatus = MoveStatus.STOP;
		} else if (moveStatus != MoveStatus.FORWARD && car.getSpeed() < MyAIController.CAR_MAX_SPEED) {
			car.applyReverseAcceleration();
			moveStatus = MoveStatus.BACKWARD;
		}
	}
	
	public boolean isNavigating() {
		return path != null && currentStep < path.getLength()-1;
	}
	
	
	
	
	
}

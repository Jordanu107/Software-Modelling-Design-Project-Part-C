package mycontroller.navigation;

import controller.CarController;
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
	
	
	public Navigator(CarController car, Path path) {
		this.car = car;
		this.path = path;
		
		currentStep = 0;
	}
	
	
	public void update() {
		// if we are done, no need to move
		if (currentStep >= path.getLength() - 1) {
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
			if (car.getSpeed() > 0) {
				// gotta brake and continue next step
				car.applyBrake();
				repeatStep = true;
			} else {
				car.applyReverseAcceleration();
			}
		}
		else if (car.getOrientation() == direction) {
			if (car.getSpeed() < 0) {
				// gotta brake and continue next step
				car.applyBrake();
				repeatStep = true;
			} else {
				car.applyForwardAcceleration();
			}
		}
		
		
		if (!repeatStep) {
			currentStep++;
		}
	}
	
	public boolean isNavigating() {
		return currentStep < path.getLength();
	}
	
	
	
	
	
}
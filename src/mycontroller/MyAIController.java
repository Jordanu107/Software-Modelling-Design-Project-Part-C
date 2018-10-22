package mycontroller;

import controller.CarController;

import world.Car;

public class MyAIController extends CarController{
	
	private ExploreController exploreController;
	private EscapeController escapeController;
	
	// Car Speed to move at
	public static final int CAR_MAX_SPEED = 1;
	
	public enum MoveStatus {
		STOP, FORWARD, BACKWARD
	}
	
	public MyAIController(Car car) {
		super(car);
		
		exploreController = new ExploreController(car, this);
		escapeController = new EscapeController(car);
	}

	@Override
	public void update() {
		if (!escapeController.initialiseNavigation()) {
			exploreController.update();
		} else {
			escapeController.update();
		}

	}
}

package mycontroller;

import controller.CarController;
import world.Car;

public class MyAIController extends CarController{
	
	private ExploreController exploreController;
	
	// Car Speed to move at
	public static final int CAR_MAX_SPEED = 1;
	
	public MyAIController(Car car) {
		super(car);
		exploreController = new ExploreController(car);
	}

	@Override
	public void update() {
		exploreController.update();
	}

}

package mycontroller;

import controller.CarController;

import world.Car;

public class MyAIController extends CarController{
	
	private ExploreController exploreController;
	
	// Car Speed to move at
	public static final int CAR_MAX_SPEED = 1;
	
	public MyAIController(Car car) {
		super(car);
		exploreController = new ExploreController(car, this);

	}

	@Override
	public void update() {
		exploreController.update();

	}
	
	boolean lastAccelForward = true;  // Initial value doesn't matter as speed starts as zero
	
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

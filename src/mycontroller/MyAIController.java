package mycontroller;

import java.util.HashMap;

import controller.CarController;
import mycontroller.map.Mapping;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;

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

package mycontroller;

import controller.CarController;
import world.Car;

public class MyAIController extends CarController{

	public enum AIState {
		EXPLORE, ESCAPE
	}
	
	private AIState state;
	
	public MyAIController(Car car) {
		super(car);
	}

	@Override
	public void update() {
		switch(state) {
		case EXPLORE:
			explore();
			break;
		case ESCAPE:
			escape();
			break;
		}
	}
	
	public void explore() {
		//TODO: explore and record map
	}

	public void escape() {
		//TODO: get keys and find escape path
	}
}

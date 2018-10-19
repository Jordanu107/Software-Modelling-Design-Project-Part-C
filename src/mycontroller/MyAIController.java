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

	private Escape control;
	
	public MyAIController(Car car) {
		super(car);
		
		Mapping.getMap().pointsOfInterest = World.getMap();
		
		HashMap<Coordinate, Integer> keys = new HashMap<Coordinate, Integer>();
		for (Coordinate key : Mapping.getMap().pointsOfInterest.keySet()) {
			MapTile tile = Mapping.getMap().pointsOfInterest.get(key);
			
			if (tile instanceof LavaTrap && ((LavaTrap) tile).getKey() != 0) {
				keys.put(key, ((LavaTrap) tile).getKey());
			}
		}
		Mapping.getMap().keys = keys;
		
		control = new Escape(car);
	}

	@Override
	public void update() {
		control.update();
		
	}

}

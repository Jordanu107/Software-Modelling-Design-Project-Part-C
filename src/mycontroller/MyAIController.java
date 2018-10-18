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
		Mapping map = new Mapping();
		Mapping.map.pointsOfInterest = World.getMap();
		HashMap<Coordinate, Integer> keys = new HashMap<Coordinate, Integer>();
		for (Coordinate key : Mapping.map.pointsOfInterest.keySet()) {
			MapTile tile = Mapping.map.pointsOfInterest.get(key);
			
			if (tile instanceof LavaTrap && ((LavaTrap) tile).getKey() != 0) {
				keys.put(key, ((LavaTrap) tile).getKey());
			}
		}
		
		Mapping.map.keys = keys;
		
		control = new Escape(car);
	}

	@Override
	public void update() {
		control.update();
		
	}

}

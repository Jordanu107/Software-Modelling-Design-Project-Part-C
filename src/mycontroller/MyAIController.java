package mycontroller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import controller.CarController;
import mycontroller.map.Mapping;
import mycontroller.navigation.Navigator;
import mycontroller.navigation.Path;
import mycontroller.navigation.Pathfinding;
import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.World;

public class MyAIController extends CarController{

	private Navigator navigator;
	public ArrayList<Coordinate> goals = new ArrayList<>();
	public int progress;
	
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
		
		//control = new Escape(car);

		goals.add(new Coordinate(9,11));
		goals.add(new Coordinate(11,11));
		goals.add(new Coordinate(11,10));
		goals.add(new Coordinate(10,10));
		goals.add(new Coordinate(10,11));
		goals.add(new Coordinate(9,11));
		goals.add(new Coordinate(13,16));
		progress = 0;
		
		/*
		ArrayList<Coordinate> coords = new ArrayList<Coordinate>(Arrays.asList(new Coordinate(2,3), 
		                new Coordinate(3,3),
		                new Coordinate(4,3),
		                new Coordinate(5,3),
		                new Coordinate(6,3),
		                new Coordinate(6,4),
		                new Coordinate(7,4), 
		                new Coordinate(7,5), 
		                new Coordinate(7,6), 
		                new Coordinate(7,7), 
		                new Coordinate(7,8), 
		                new Coordinate(7,9), 
		                new Coordinate(7,10), 
		                new Coordinate(7,11), 
		                new Coordinate(8,11),
		                new Coordinate(9,11), 
		                new Coordinate(9,10), 
		                new Coordinate(8,10), 
		                new Coordinate(8,11), 
		                new Coordinate(8,12), 
		                new Coordinate(8,13), 
		                new Coordinate(9,13), 
		                new Coordinate(9,14), 
		                new Coordinate(10,14), 
		                new Coordinate(11,14), 
		                new Coordinate(12,14), 
		                new Coordinate(13,14), 
		                new Coordinate(13,15), 
		                new Coordinate(13,16)));
		Path path;
		try {
			path = new Path(coords);
			navigator = new Navigator(this, path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} */
		
		navigator = new Navigator(this,null);
	}

	@Override
	public void update() {
		//control.update();

		System.out.println(this.getPosition());
		
		if (navigator.isNavigating()) {
			System.out.println("n");
			
			navigator.update();
			return;
		}
		
		if (progress >= goals.size()) {
			return;
		}
		System.out.println("Find path from " + this.getPosition() + " to " + goals.get(progress));
		Path path = Pathfinding.linkPoints(this, goals.get(progress));
		
		if (path != null) {
			System.out.println(path.getCoords());
			navigator = new Navigator(this, path);
			navigator.update();
			progress++;
		} else {
			System.out.println("No path founds...");
		}
		
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

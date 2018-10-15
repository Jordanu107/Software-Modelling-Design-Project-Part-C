package mycontroller.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tiles.MapTile;
import utilities.Coordinate;

/**
 * Mapping expresses the current state of the map that has been explored,
 * keeping track of the relevant features that the map has
 * @author jordanung
 *
 */

public class Mapping {
	private ArrayList<Coordinate> keys;
	private ArrayList<Coordinate> deadEnds;
	
	public Mapping() {
		keys = new ArrayList<>();
		deadEnds = new ArrayList<>();
	}
	
	public ArrayList<Coordinate> getKeysSeen() {
		return keys;
	}
	
	public ArrayList<Coordinate> getDeadEnds() {
		return deadEnds;
	}
	
	public void addFoundKey(Coordinate coordinate) {
		keys.add(coordinate);
	}
	
	public void addDeadEnd(Coordinate coordinate) {
		deadEnds.add(coordinate);
	}
	
	public void articulateViewPoint(HashMap<Coordinate, MapTile> currentView) {
		for (Map.Entry<Coordinate, MapTile> mapInfo : currentView.entrySet()) {
			//System.out.println("Coordinates are " + mapInfo.getKey() + " and maptile is" + mapInfo.getValue());

			//TODO - Do something base on tile type
		}
	}
	// TODO - The logic behind finding a key
	// Should a sort of explore() via BFS/DFS function be implemented here
	// and it called upon every invocation of update in MyAIController?
	
}

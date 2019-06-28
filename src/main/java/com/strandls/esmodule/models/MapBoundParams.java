package com.strandls.esmodule.models;

import java.util.List;

public class MapBoundParams {

	private MapBounds bounds;
	
	private List<MapGeoPoint> polygon;

	public MapBoundParams() {}

	public MapBoundParams(MapBounds bounds, List<MapGeoPoint> polygon) {
		super();
		this.bounds = bounds;
		this.polygon = polygon;
	}

	public MapBounds getBounds() {
		return bounds;
	}

	public void setBounds(MapBounds bounds) {
		this.bounds = bounds;
	}

	public List<MapGeoPoint> getPolygon() {
		return polygon;
	}

	public void setPolygon(List<MapGeoPoint> polygon) {
		this.polygon = polygon;
	}

	@Override
	public String toString() {
		return "MapBoundParams [bounds=" + bounds + ", polygon=" + polygon + "]";
	}

}

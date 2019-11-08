

import { Component, OnInit} from '@angular/core';

declare var Cesium;

@Component({
    selector: 'satellite-device',
    templateUrl: './satellite.component.html',
    styleUrls: ['./satellite.component.css']
})
export class SatelliteDeviceComponent implements OnInit  {

 constructor() { }

  ngOnInit() {
	     	             Cesium.Ion.defaultAccessToken='eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI3NGVlZDdlZi0xYmVlLTRjMjEtOGY2OC05MWNhNjQwZGIzNzMiLCJpZCI6MTU3MjAsInNjb3BlcyI6WyJhc2wiLCJhc3IiLCJhc3ciLCJnYyJdLCJpYXQiOjE1NzI4MzQ0ODF9.2LqJBhBq4aAGyn1Z21Zl2TzRxb-6FRRkEZz7uqDQEno';
    const viewer = new Cesium.Viewer('cesiumContainer');

}

}

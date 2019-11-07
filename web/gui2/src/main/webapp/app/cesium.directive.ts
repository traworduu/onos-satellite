
import { Directive, OnInit, ElementRef } from '@angular/core';
declare var Cesium;

@Directive({
 selector: 'appCesium'
})
export class CesiumDirective implements OnInit {
 
 constructor(private el: ElementRef) { }

 ngOnInit() {
  // Put initialization code for the Cesium viewer here
          Cesium.Ion.defaultAccessToken='eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI3NGVlZDdlZi0xYmVlLTRjMjEtOGY2OC05MWNhNjQwZGIzNzMiLCJpZCI6MTU3MjAsInNjb3BlcyI6WyJhc2wiLCJhc3IiLCJhc3ciLCJnYyJdLCJpYXQiOjE1NzI4MzQ0ODF9.2LqJBhBq4aAGyn1Z21Zl2TzRxb-6FRRkEZz7uqDQEno';


 // var viewer = new Cesium.Viewer('cesiumContainer');    
  const viewer = new Cesium.Viewer(this.el.nativeElement);
 }

}

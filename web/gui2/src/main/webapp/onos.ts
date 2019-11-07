import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { OnosModule } from './app/onos.module';
import { environment } from './environments/environment';

if (environment.production) {
  enableProdMode();
}

window['CESIUM_BASE_URL']='src/main/webapp/assets/cesium';
Cesium.Ion.defaultAccessToken='eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiI3NGVlZDdlZi0xYmVlLTRjMjEtOGY2OC05MWNhNjQwZGIzNzMiLCJpZCI6MTU3MjAsInNjb3BlcyI6WyJhc2wiLCJhc3IiLCJhc3ciLCJnYyJdLCJpYXQiOjE1NzI4MzQ0ODF9.2LqJBhBq4aAGyn1Z21Zl2TzRxb-6FRRkEZz7uqDQEno';

platformBrowserDynamic().bootstrapModule(OnosModule)
  .catch(err => console.log(err));

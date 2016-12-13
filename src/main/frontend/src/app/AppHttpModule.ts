import { NgModule, Injector } from '@angular/core';
import { HttpModule, XHRBackend, BrowserXhr,
         ResponseOptions,  XSRFStrategy } from '@angular/http';

import { InMemoryBackendService, InMemoryDbService } from 'angular-in-memory-web-api';

import { InMemoryDataService }  from './in-memory-data-service';
import { environment } from '../environments/environment'

@NgModule({
  imports: [ HttpModule ],
  providers: [
    {
      provide: XHRBackend,
      useFactory: (injector: Injector, browser: BrowserXhr,
                   xsrf: XSRFStrategy, options: ResponseOptions): any => {
        if (!environment.mockData) {
          return new XHRBackend(browser, options, xsrf);
        } else {
          return new InMemoryBackendService(injector, new InMemoryDataService(), {
            // the configuration object
          });
        }
      },
      deps: [ Injector, BrowserXhr, XSRFStrategy, ResponseOptions ]
    }
  ]
})
export class AppHttpModule {
  
}

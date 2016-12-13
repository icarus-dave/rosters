import { TestBed, inject, async } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';

import { WebConfigService } from './webconfig.service';
import { InMemoryDataService } from '../in-memory-data-service';

describe('WebConfigServiceTest', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpModule ],
      providers: [
        WebConfigService,
        MockBackend,
        BaseRequestOptions,
        {
          provide: Http,
          useFactory: (backend, options) => new Http(backend, options),
          deps: [MockBackend, BaseRequestOptions]
        }
      ]
    });
  });

  it('get has correct value', async(inject(
    [WebConfigService,MockBackend],(service,mockBackend) => {
      mockBackend.connections.subscribe(conn => {
        conn.mockRespond(new Response(new ResponseOptions({ body: JSON.stringify(InMemoryDataService.getWebConfig()) })));
      });

      service.load().then(() => {
        expect(service.get("foo")).toEqual("baz");
        expect(service.get("baz")).toEqual("foo");
        expect(service.get("asdf")).toBe(undefined);
      })
    }
  )));

});

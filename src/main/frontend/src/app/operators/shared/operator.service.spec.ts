/*
Unit testing:
* get operators in alphabetical order
* get operator
* get unknown operator
* other error cases

*/
import { TestBed, inject, async } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';

import { OperatorService } from './operator.service';
import { InMemoryDataService } from '../../in-memory-data-service';

describe('OperatorServiceTest', () => {

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpModule ],
      providers: [
        OperatorService,
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

  it('should construct', async(inject(
    [OperatorService, MockBackend], (service, mockBackend) => {
      expect(service).toBeDefined();
  })));

  it('getOperators Empty List', async(inject(
    [OperatorService,MockBackend],(service,mockBackend) => {
      mockBackend.connections.subscribe(conn => {
        conn.mockRespond(new Response(new ResponseOptions({ body: "[]" })));
      });

      const result = service.getOperators();

      result.then(res => {
        expect(res.length).toEqual(0);
      })
    }
  )));

  it('getOperators List', async(inject(
    [OperatorService,MockBackend],(service,mockBackend) => {
      mockBackend.connections.subscribe(conn => {
        conn.mockRespond(new Response(new ResponseOptions({ body: JSON.stringify({data: InMemoryDataService.getOperators() }) })));
      });

      service.getOperators().then(res => {
        expect(res.length).toEqual(6);
      })
    }
  )));
});

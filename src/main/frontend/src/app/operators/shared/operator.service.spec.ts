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
        //ensure returning in correct order
        expect(res[0].lastName).toEqual("10");
        expect(res[0].id).toEqual(16);
        expect(res[0].firstName).toEqual("9");
        expect(res[0].email).toEqual('foo@baz.com');
        expect(res[0].active).toEqual(false);
        expect(res[5].lastName).toEqual("Baz");
      })
    }
  )));

  it('getOperators Error', async(inject(
    [OperatorService,MockBackend],(service,mockBackend) => {
      mockBackend.connections.subscribe(conn => {
        conn.mockError((new ErrorResponse (new ResponseOptions ({
              body: {code:500, message:'foo'},
              status: 500
          }))));
      });

      service.getOperators().catch(err => {
          expect(err).toContain("500");
          expect(err).toContain("foo");
      })
    }
  )));

  it('getOperators Error No Body', async(inject(
    [OperatorService,MockBackend],(service,mockBackend) => {
      mockBackend.connections.subscribe(conn => {
        conn.mockError((new ErrorResponse (new ResponseOptions ({
              status: 500,
              statusText: "Server error"
          }))));
      });

      service.getOperators().catch(err => {
          expect(err).toContain("500");
          expect(err).toContain("Server error");
      })
    }
  )));

  it('createOperator', () => {

  });

  it ('createOperator Error', () => {

  });

  it ('updateOperator',() => {
    
  });

  it ('updateOperator error',() => {

  });

});

class ErrorResponse extends Response implements Error {
    name: any
    message: any
}
import { TestBed, inject, async, fakeAsync } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { InMemoryWebApiModule,InMemoryBackendService } from 'angular-in-memory-web-api';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions, Headers, ResponseType } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { OperatorService } from './operator.service';
import { Operator } from './operator.model';
import { InMemoryDataService } from '../../in-memory-data-service';

describe('OperatorServiceTest', () => {

  beforeEach(() => {
    
    TestBed.configureTestingModule({
      imports: [ HttpModule, InMemoryWebApiModule.forRoot(InMemoryDataService), HttpModule ],
      providers: [
        OperatorService,
        InMemoryBackendService
      ]
    });
  });

  it('getOperators Empty List', (done) => { inject([OperatorService, Http],(service, http) => {
      //extra check to ensure the test is actually running
      spyOn(http,'get').and.returnValues(Observable.create( (observer) => {
        observer.next(new Response(new ResponseOptions({status:200,body:'{"data":[{"id":123,"firstName":"asdf"}]}', headers: new Headers({ "Content-Type":"application/json" })})));
        observer.complete();
      }),Observable.create( (observer) => {
        observer.next(new Response(new ResponseOptions({status:200,body:'{"data":[]}', headers: new Headers({ "Content-Type":"application/json" })})));
        observer.complete();
      }));

      service.getOperators().then(r1 => {
        expect(r1.length).toEqual(1);
        return service.getOperators()
      }).then((operators) =>  {
        expect(operators.length).toEqual(0);
        done();
      });
    })();
  });

  it('getOperators List', (done) => { inject([OperatorService],(service) => {
      service.getOperators().then(res => {
        expect(res.length).toEqual(6);
        //ensure returning in correct order
        expect(res[0].lastName).toEqual("10");
        expect(res[0].id).toEqual(16);
        expect(res[0].firstName).toEqual("9");
        expect(res[0].email).toEqual('foo@baz.com');
        expect(res[0].active).toEqual(false);
        expect(res[5].lastName).toEqual("Baz");
        done();
      });
    })();
  });
  
  it('getOperators Error', (done) => { inject([OperatorService, Http],(service, http) => {
    spyOn(http,'get').and.returnValue(Observable.create( (observer) => {
      observer.error(new Response(new ResponseOptions({status:504,statusText:'error',type:ResponseType.Error,body:'{"code":504, "message":"foo"}', headers: new Headers({ "Content-Type":"application/json" })})));
    }));
    //confirm the service is handling the error sensibly
    service.getOperators().catch(err => {
      expect(err).toContain("504");
      expect(err).toContain("foo");
      done();
    });
    })();
  });

  it('getOperator found', (done) => { inject([OperatorService],(service) => {
      service.getOperator(11).then(o2 => {
        expect(o2.firstName).toEqual("Foo");
        expect(o2.lastName).toEqual("Baz");
        expect(o2.email).toEqual("foo@baz.com");
        expect(o2.active).toEqual(true);
        expect(o2.id).toEqual(11);
        done(); 
      });
    })();
  });

  it('getOperator not found', (done) => { inject([OperatorService],(service) => {
      service.getOperator(9999)
        .catch(err => { 
          expect(err).toContain("not found")
          done();
        });
    })();
  });

  it('getOperator error', (done) => { inject([OperatorService, Http],(service, http) => {
    spyOn(http,'get').and.returnValue(Observable.create( (observer) => {
      observer.error(new Response(new ResponseOptions({status:504,statusText:'error',type:ResponseType.Error,body:'{"code":504, "message":"foo"}', headers: new Headers({ "Content-Type":"application/json" })})));
    }));

    //confirm the service is handling the error sensibly
    service.getOperator(1).catch(err => {
      expect(err).toContain("504");
      expect(err).toContain("foo");
      done();
    });
    })();
  });

  it('createOperator', (done) => { inject([OperatorService],(service) => {
      let operator = new Operator({ firstName:"abc", lastName:"def",email:"foo@baz.com",active:true } )
      service.create(operator).then(o1 => {
        expect(o1.firstName).toEqual("abc");
        expect(o1.lastName).toEqual("def");
        expect(o1.email).toEqual("foo@baz.com");
        expect(o1.active).toEqual(true);
        expect(o1.id).not.toBeNull();
        return service.getOperator(o1.id);
      }).then(o2 => {
        expect(o2.firstName).toEqual("abc");
        expect(o2.lastName).toEqual("def");
        expect(o2.email).toEqual("foo@baz.com");
        expect(o2.active).toEqual(true);
        expect(o2.id).not.toBeNull();
        done();       
      });
    })();
  });

  it ('createOperator Error', (done) => { inject([OperatorService, Http],(service, http) => {
    spyOn(http,'post').and.returnValue(Observable.create( (observer) => {
      observer.error(new Response(new ResponseOptions({status:400,statusText:'error',type:ResponseType.Error,body:'{"code":400, "message":"email already in use"}', headers: new Headers({ "Content-Type":"application/json" })})));
    }));

    //confirm the service is handling the error sensibly
    service.create(new Operator({ firstName:"abc", lastName:"def",email:"foo@baz.com",active:true } )).catch(err => {
      expect(err).toContain("400");
      expect(err).toContain("email already in use");
      done();
    });
    })();
  });

  it ('updateOperator',(done) => { inject([OperatorService, InMemoryBackendService],(service, dataService) => {
      service.getOperator(11).then(o1 => {
        o1.firstName="updated"
        return service.update(o1);
      }).then(o2 => {
        expect(o2.firstName).toEqual("updated");
        expect(o2.lastName).toEqual("Baz");
        expect(o2.email).toEqual("foo@baz.com");
        expect(o2.active).toEqual(true);
        expect(o2.id).toEqual(11);
        return service.getOperator(11);  
      }).then(o3 => {
        expect(o3.firstName).toEqual("updated");
        expect(o3.lastName).toEqual("Baz");
        expect(o3.email).toEqual("foo@baz.com");
        expect(o3.active).toEqual(true);
        expect(o3.id).toEqual(11);
        dataService.resetDb();
        done();  
      });
    })();
  });

  it('updateOperator error', (done) => { inject([OperatorService, Http],(service, http) => {
    spyOn(http,'put').and.returnValue(Observable.create( (observer) => {
      observer.error(new Response(new ResponseOptions({status:400,statusText:'error',type:ResponseType.Error,body:'{"code":400, "message":"email already in use"}', headers: new Headers({ "Content-Type":"application/json" })})));
    }));

    //confirm the service is handling the error sensibly
    service.getOperator(11).then( o1 => {
      o1.lastName = "updated";
      return service.update(o1);
    }).catch( err => {
      expect(err).toContain("400");
      expect(err).toContain("email already in use");
      done();    
    });
    
    })();
  });

});

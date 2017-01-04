import { InMemoryDbService, RequestInfo,  } from 'angular-in-memory-web-api';
import { ResponseOptions, RequestMethod } from '@angular/http';

export class InMemoryDataService implements InMemoryDbService {
  public static getOperators() { return [
    {id: 11, firstName: 'Foo', lastName: 'Baz',email:'foo@baz.com',active:true},
    {id: 12, firstName: '1', lastName: '2',email:'foo@baz.com',active:true},
    {id: 13, firstName: '3', lastName: '4',email:'foo@baz.com',active:true},
    {id: 14, firstName: '5', lastName: '6',email:'foo@baz.com',active:false},
    {id: 15, firstName: '7', lastName: '8',email:'foo@baz.com',active:true},
    {id: 16, firstName: '9', lastName: '10',email:'foo@baz.com',active:false},
  ]; };

  public static getWebConfig() {
    return {"foo":"baz",
            "baz":"foo",
            "auth.domain":"rosters.au.auth0.com",
            "auth.clientId":"YqDaq9tzPWsyhsfCzHFQqNWHdcl2X8dH"}
  };

  createDb() {
    var operator = InMemoryDataService.getOperators();
    var webconfig = InMemoryDataService.getWebConfig();
    return {operator,webconfig};
  };

  //unwrap the data element that the library adds, we leave arrays as is because of the security
  //issue re: unwrapped arrays and certain browsers
  responseInterceptor(res: ResponseOptions, ri: RequestInfo) : ResponseOptions {
    if (ri.req.method == RequestMethod.Put && res.status >= 200 && res.status < 300) {
      //by default the implementation returns an empty body for PUT, if we assume 
      //the update sets the resource correctly then returning the request is sufficient
      res.body = ri.req.getBody();
      return res;
    }

    if (res.body != null && res.status >= 200 && res.status < 300 && (res.body as any).data && !Array.isArray((res.body as any).data)) {
      res.body = (res.body as any).data;
    }
    return res;
  };
}

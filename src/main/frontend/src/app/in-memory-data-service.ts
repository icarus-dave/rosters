import { InMemoryDbService } from 'angular-in-memory-web-api';
export class InMemoryDataService implements InMemoryDbService {
  createDb() {
    let operator = [
      {id: 11, firstName: 'Foo', lastName: 'Baz',email:'foo@baz.com',active:true},
      {id: 12, firstName: '1', lastName: '2',email:'foo@baz.com',active:true},
      {id: 13, firstName: '3', lastName: '4',email:'foo@baz.com',active:true},
      {id: 14, firstName: '5', lastName: '6',email:'foo@baz.com',active:false},
      {id: 15, firstName: '7', lastName: '8',email:'foo@baz.com',active:true},
      {id: 16, firstName: '9', lastName: '10',email:'foo@baz.com',active:false},
    ];

    //development config parameters go here
    let webconfig = [
      {"foo":"baz",
      "baz":"foo"}
    ];

    return {operator, webconfig};
  }
}

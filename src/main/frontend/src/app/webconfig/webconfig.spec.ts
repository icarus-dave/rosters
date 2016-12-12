import { TestBed, inject, async } from '@angular/core/testing';
import { HttpModule } from '@angular/http';

import { WebConfigService } from './webconfig.service';
import { WebConfigModule } from './webconfig.module';

describe('WebConfigServiceTest', () => {
  let service;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpModule, WebConfigModule ],
      providers: [ WebConfigService ]
    });
    service = TestBed.get(WebConfigService);
  });


  it('get has correct value', done => {
    service.load().then(() => {
      expect(service).toBeDefined();
      expect(service.get("foo")).toEqual("baz");
      expect(service.get("baz")).toEqual("foo");
      expect(service.get("asdf")).toBe(undefined);
      done();
    });
  });

});

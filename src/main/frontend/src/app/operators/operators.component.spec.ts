import { TestBed, inject, async, ComponentFixture } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { DebugElement } from '@angular/core';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { By }              from '@angular/platform-browser';
import { NgbModule, NgbModal, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { OperatorService } from './shared/operator.service';
import { OperatorsComponent } from './operators.component';
import { InMemoryDataService } from '../in-memory-data-service';

describe('OperatorsComponentTest', () => {

  let fixture: ComponentFixture<OperatorsComponent>;
  let comp: OperatorsComponent;
  let de:   DebugElement;
  let el:   HTMLElement

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpModule, InMemoryWebApiModule.forRoot(InMemoryDataService), NgbModule.forRoot() ],
      providers: [
        OperatorService,
      ],
      declarations: [ OperatorsComponent ]
    });

    fixture = TestBed.createComponent(OperatorsComponent);
    comp = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('List operators', (done) => {
    fixture.whenStable().then(() => {
      fixture.detectChanges();
      const rows = fixture.debugElement.queryAll(By.css("#op-rows"))
      expect(rows.length).toBe(6);
      //check 1st row is loading correctly
      expect(rows[0].queryAll(By.css('td'))[0].nativeElement.textContent).toBe('10');
      expect(rows[0].queryAll(By.css('td'))[1].nativeElement.textContent).toBe('9');
      expect(rows[0].queryAll(By.css('td'))[2].nativeElement.textContent).toBe('foo@baz.com');
      expect(rows[0].queryAll(By.css('td'))[3].nativeElement.textContent).toBe('No');

      //check mailto link working
      expect(rows[2].query(By.css('a')).nativeElement.getAttribute('href')).toBe('mailto:foo@baz.com');

      //check active setting correctly
      expect(rows[1].queryAll(By.css('td'))[0].nativeElement.textContent).toBe('2');
      expect(rows[1].queryAll(By.css('td'))[3].nativeElement.textContent).toBe('Yes');

      //check there's no error message being displayed
      expect(fixture.debugElement.query(By.css("#error-message"))).toBeNull();
      
      done();
    });
  });



 /* it('List operators failure', async(inject([OperatorService], (service) => {
      fixture.detectChanges();
      expect(comp.errorMessage).toEqual('');
      expect(fixture.debugElement.query(By.css("#error-message"))).toBeNull();
      
      let spy = spyOn(service,'getOperators')
        .and.returnValues(Promise.reject("asdf"), service.getOperators());

      comp.getOperators().then(() => {
        console.log('1');
        expect(comp.errorMessage).toEqual("asdf");
        fixture.detectChanges();
        expect(fixture.debugElement.query(By.css("#error-message")).nativeElement.textContent).toContain('asdf');
        return new Promise((a)=>a());
      }).then(() => {
        console.log(comp.getOperators());
        comp.getOperators().then(() => {
          console.log('here');
        });
      });*/
      
      //.then(() => console.log("asdf"));

        /*return comp.getOperators().then(() => {
          console.log('here');
          spy.and.returnValue(service.getOperators());
          console.log('3');
          expect(comp.errorMessage).toEqual('');
          fixture.detectChanges();
          expect(fixture.debugElement.query(By.css("#error-message"))).toBeNull();
        });

      );*/


      /*let secondCheck = () => {
        comp.getOperators().then(() => {
          console.log('here');
          spy.and.returnValue(service.getOperators());
          console.log('3');
          expect(comp.errorMessage).toEqual('');
          fixture.detectChanges();
          expect(fixture.debugElement.query(By.css("#error-message"))).toBeNull();
          return Promise.resolve()
        })
      }

      firstCheck().then(() => secondCheck() );*/
  //})));

  //todo: click 'create' to display modal
  //todo: click 'edit' to display modal
});

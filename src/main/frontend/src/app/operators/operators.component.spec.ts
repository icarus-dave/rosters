import { TestBed, inject, async, ComponentFixture } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { DebugElement } from '@angular/core';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { By }              from '@angular/platform-browser';
import { NgbModule, NgbModal, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';

import { OperatorService } from './shared/operator.service';
import { Operator } from './shared/operator.model';
import { OperatorsComponent } from './operators.component';
import { OperatorFormComponent } from './operator-form.component';
import { InMemoryDataService } from '../in-memory-data-service';

describe('OperatorsComponentTest', () => {

  let fixture: ComponentFixture<OperatorsComponent>;
  let comp: OperatorsComponent;
  let de:   DebugElement;
  let el:   HTMLElement

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ HttpModule, InMemoryWebApiModule.forRoot(InMemoryDataService), NgbModule.forRoot(), ReactiveFormsModule ],
      providers: [
        OperatorService,
        NgbActiveModal,
        /*{
          provide: OperatorFormComponent,
          deps: [NgbActiveModal, OperatorService,FormBuilder],
          useFactory:
            (activeModal: NgbActiveModal, operatorService: OperatorService, fb: FormBuilder) => {
               return new OperatorFormComponent(activeModal,operatorService,fb);
             }
        }*/
      ],
      declarations: [ OperatorsComponent ]
    });

    fixture = TestBed.createComponent(OperatorsComponent);
    comp = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('List operators', (done) => {
    let op:Operator;
    spyOn(comp, "open").and.callFake((o) => { op = o });

    fixture.whenStable().then(() => {
      fixture.detectChanges();
      const rows = fixture.debugElement.queryAll(By.css("#op-rows"))
      expect(rows.length).toBe(6);
      //check 1st row is loading correctly
      expect(rows[0].queryAll(By.css('td'))[0].nativeElement.textContent).toBe('10');
      expect(rows[0].queryAll(By.css('td'))[1].nativeElement.textContent).toBe('9');
      expect(rows[0].queryAll(By.css('td'))[2].nativeElement.textContent).toBe('foo@baz.com');
      expect(rows[0].queryAll(By.css('td'))[3].nativeElement.textContent).toBe('No');

      //check button click as expected and operator passed through
      rows[0].query(By.css('button')).nativeElement.click();
      expect(comp.open).toHaveBeenCalled();
      expect(op.id).toEqual(16);

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

  it('Click operator create link', () => {
    let op:Operator
    spyOn(comp, "open").and.callFake((o?:Operator) => { op = o });

    fixture.nativeElement.querySelector('#createButton').click();
    expect(op).toBeUndefined();
    expect(comp.open).toHaveBeenCalled();
  });

 /*it('List operators failure', async(inject([OperatorService], (service) => {
    fixture.detectChanges();
    expect(comp.errorMessage).toEqual('');
    expect(fixture.debugElement.query(By.css("#error-message"))).toBeNull();
    
    let spy = spyOn(service,'getOperators')
      .and.returnValues(Promise.reject("asdf"), Promise.resolve([]));
    console.log('1');
    comp.getOperators().then(() => {
      expect(comp.errorMessage).toEqual("asdf");
      fixture.detectChanges();
      expect(fixture.debugElement.query(By.css("#error-message")).nativeElement.textContent).toContain('asdf');
      console.log('2');
      return comp.getOperators();
    })
    /*.then(() => {
      console.log('4');
      fixture.detectChanges();
      console.log('5');
      expect(fixture.debugElement.query(By.css("#error-message"))).toBeNull();
      console.log('6');
    });*/
  //})));

  /*it('Open no operator specified', () => { 
    let modalRef = comp.open()
    expect(modalRef.componentInstance).toEqual(jasmine.any(OperatorFormComponent));
    expect(modalRef.componentInstance.operator).toBeUndefined();
  });

  it('Open operator specified', () => {
    let operator = new Operator({id:123,firstName:'first name',lastName:'last name',email:'foo@baz.com',active:true});
    let modalRef = comp.open(operator)
    expect(modalRef.componentInstance).toEqual(jasmine.any(OperatorFormComponent));

    expect(modalRef.componentInstance.operator).toEqual(operator);
  });*/




});

import { TestBed, inject, async, ComponentFixture, fakeAsync, tick } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { DebugElement } from '@angular/core';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { By }              from '@angular/platform-browser';
import { NgbModule, NgbModal, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ReactiveFormsModule } from '@angular/forms';

import { OperatorService } from './shared/operator.service';
import { OperatorFormComponent } from './operator-form.component';
import { InMemoryDataService } from '../in-memory-data-service';
describe('OperatorFormComponentTest', () => {

  let fixture: ComponentFixture<OperatorFormComponent>;
  let comp: OperatorFormComponent;
  let de:   DebugElement;
  let el:   HTMLElement

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [  HttpModule, 
                  InMemoryWebApiModule.forRoot(InMemoryDataService), 
                  NgbModule.forRoot(), 
                  ReactiveFormsModule
                ],
      providers: [
        OperatorService,
        NgbActiveModal
      ],
      declarations: [ OperatorFormComponent ]
    });

    fixture = TestBed.createComponent(OperatorFormComponent);
    comp = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("submit new operator", () => {

  });

  it("submit operator error", () => {

  });

  it("submit existing operator", () => {

  });

  it("submit existing operator error",() => {

  });

  it ("update: should fill in values",() => {

  });

  it('create: should fill in values', ()  => {

    spyOn(comp, 'onSubmit');

    let emailEl = fixture.nativeElement.querySelector('input[formControlName=email]')
    let firstNameEl = fixture.nativeElement.querySelector('input[formControlName=firstName]')
    let lastNameEl = fixture.nativeElement.querySelector('input[formControlName=lastName]')
    let activeEl = fixture.nativeElement.querySelector('input[formControlName=active]')

    console.log(emailEl.classList.contains("ng-invalid"));

    emailEl.value = "foo@baz.com";
    firstNameEl.value = "first name";
    lastNameEl.value = "last name";
    activeEl.checked = true;

    emailEl.dispatchEvent(new Event('input'));
    firstNameEl.dispatchEvent(new Event('input'));
    //lastNameEl.dispatchEvent(new Event('input'));
    activeEl.dispatchEvent(new Event('change'));
     

    //todo: on select class 

    fixture.detectChanges();
    console.log(emailEl);

    console.log(fixture.nativeElement.querySelector('button[type=submit]').disabled);

    fixture.nativeElement.querySelector('button[type=submit]').click();

    fixture.detectChanges();
    console.log(comp.form.value);
    
    expect(comp.onSubmit).toHaveBeenCalled();


    //test submit button is not working

    //set email
    //set firstname
    //set lastname
    //set active
    //click save
    //confirm operator service called
    //get operator back from modal service and expect values


  });
});
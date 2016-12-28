import { TestBed, inject, async, ComponentFixture, fakeAsync, tick } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { DebugElement } from '@angular/core';
import { BaseRequestOptions, Http, HttpModule, Response, ResponseOptions } from '@angular/http';
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { By }              from '@angular/platform-browser';
import { NgbModule, NgbModal, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ReactiveFormsModule } from '@angular/forms';

import { Operator } from './shared/operator.model';
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

  it("submit new operator",(done) => { inject([OperatorService],(service) => {
    //create with service and close modal
    let params = {id: undefined, firstName:"abc",lastName:"def",active:true,email:"foo@baz.com"};
    let operator = new Operator(params);

    spyOn(service,"create").and.callFake((o) => Promise.resolve(o) );
    spyOn(comp.activeModal,"close").and.callFake((o) => Promise.resolve(o) );
    comp.form.patchValue(params);

    comp.onSubmit().then(() => {
      //normally this would set the ID but not for a unit test
      expect(service.create).toHaveBeenCalledWith(operator);
      expect(comp.activeModal.close).toHaveBeenCalledWith(operator);
      done();
    });
    })();
  });

  it("submit operator error", (done) => { inject([OperatorService],(service) => {
    //create with service and close modal
    let params = {id: undefined, firstName:"abc",lastName:"def",active:true,email:"foo@baz.com"};
    let operator = new Operator(params);

    spyOn(service,"create").and.callFake((o) => Promise.reject("some server error") );
    spyOn(comp.activeModal,"close");
    comp.form.patchValue(params);

    comp.onSubmit().then(() => {
      expect(service.create).toHaveBeenCalledWith(operator);
      expect(comp.activeModal.close).toHaveBeenCalledTimes(0);
      expect(comp.errorMessage).toContain("some server error");
      done();
    })
    })();
  });

  it("submit existing operator", (done) => { inject([OperatorService],(service) => {
    let params = {id: 123, firstName:"abc",lastName:"def",active:true,email:"foo@baz.com"};
    let operator = new Operator(params);

    spyOn(service,"update").and.callFake((o) => Promise.resolve(o) );
    spyOn(comp.activeModal,"close").and.callFake((o) => Promise.resolve(o) );
    comp.form.patchValue(params);

    comp.onSubmit().then(() => {
      expect(service.update).toHaveBeenCalledWith(operator);
      expect(comp.activeModal.close).toHaveBeenCalledWith(operator);
      done();
    });
    })();
  });

  it("submit existing operator error", (done) => { inject([OperatorService],(service) => {
    //create with service and close modal
    let params = {id: 123, firstName:"abc",lastName:"def",active:true,email:"foo@baz.com"};
    let operator = new Operator(params);

    spyOn(service,"update").and.callFake((o) => Promise.reject("some server error") );
    spyOn(comp.activeModal,"close");
    comp.form.patchValue(params);

    comp.onSubmit().then(() => {
      expect(service.update).toHaveBeenCalledWith(operator);
      expect(comp.activeModal.close).toHaveBeenCalledTimes(0);
      expect(comp.errorMessage).toContain("some server error");
      done();
    })
    })();
  });

  it('create: should fill in values', ()  => {
    spyOn(comp, 'onSubmit');

    let emailEl = fixture.nativeElement.querySelector('input[formControlName=email]')
    let firstNameEl = fixture.nativeElement.querySelector('input[formControlName=firstName]')
    let lastNameEl = fixture.nativeElement.querySelector('input[formControlName=lastName]')
    let activeEl = fixture.nativeElement.querySelector('input[formControlName=active]')
    let submitEl = fixture.nativeElement.querySelector('button[type=submit]')
    let errorEl = fixture.nativeElement.querySelector('#error-message');

    //first check the invalid ones will be marked as invalid
    expect(emailEl.classList).toContain("ng-invalid");
    expect(firstNameEl.classList).toContain("ng-invalid");
    expect(lastNameEl.classList).toContain("ng-invalid");
    expect(submitEl.disabled).toEqual(true);
    expect(activeEl.checked).toEqual(false);
    expect(errorEl).toBeNull();

    //now update the vlues
    emailEl.value = "foo";
    firstNameEl.value = "first name";
    lastNameEl.value = "last name";
    activeEl.checked = true;

    emailEl.dispatchEvent(new Event('input'));
    firstNameEl.dispatchEvent(new Event('input'));
    lastNameEl.dispatchEvent(new Event('input'));
    activeEl.dispatchEvent(new Event('change'));
     
    fixture.detectChanges();

    //check their CSS class is now as expected
    expect(emailEl.classList).toContain("ng-invalid");
    expect(emailEl.classList).toContain("ng-dirty");
    expect(firstNameEl.classList).toContain("ng-valid");
    expect(lastNameEl.classList).toContain("ng-valid");
    expect(submitEl.disabled).toEqual(true);

    //fix email and check submit button works
    emailEl.value = "foo@baz.com";
    emailEl.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(emailEl.classList).toContain("ng-valid");
    expect(submitEl.disabled).toEqual(false);

    //now we submit
    fixture.nativeElement.querySelector('button[type=submit]').click();

    fixture.detectChanges();

    expect(comp.onSubmit).toHaveBeenCalled();

    //now check everything is set correctly
    expect(comp.form.value.id).toBeUndefined();
    expect(comp.form.value.firstName).toEqual("first name");
    expect(comp.form.value.lastName).toEqual("last name");
    expect(comp.form.value.email).toEqual("foo@baz.com");
    expect(comp.form.value.active).toEqual(true);
  });

  it("create: error message on submit",() => {
    spyOn(comp, 'onSubmit').and.callFake(() => comp.errorMessage = "asdf");

    let emailEl = fixture.nativeElement.querySelector('input[formControlName=email]')
    let firstNameEl = fixture.nativeElement.querySelector('input[formControlName=firstName]')
    let lastNameEl = fixture.nativeElement.querySelector('input[formControlName=lastName]')
    let activeEl = fixture.nativeElement.querySelector('input[formControlName=active]')
    let submitEl = fixture.nativeElement.querySelector('button[type=submit]')
    let errorEl = fixture.nativeElement.querySelector('#error-message');

    //now update the vlues
    emailEl.value = "foo@baz.com";
    firstNameEl.value = "first name";
    lastNameEl.value = "last name";
    activeEl.checked = true;

    emailEl.dispatchEvent(new Event('input'));
    firstNameEl.dispatchEvent(new Event('input'));
    lastNameEl.dispatchEvent(new Event('input'));
    activeEl.dispatchEvent(new Event('change'));
     
    fixture.detectChanges();

    expect(errorEl).toBeNull();

    //now we submit
    fixture.nativeElement.querySelector('button[type=submit]').click();

    fixture.detectChanges();
    errorEl = fixture.nativeElement.querySelector('#error-message');
    //check values remain
    expect(firstNameEl.value).toEqual("first name");
    
    expect(errorEl).toBeTruthy();
    expect(errorEl.textContent).toContain("asdf");
  });

  it ("update: should fill in values",() => {
    spyOn(comp,'onSubmit');
    comp.operator = new Operator({'id':123,'firstName':"asdf",lastName:"jkl",email:"asdf@jkl.com",active:true});

    fixture.detectChanges();

    let idEl = fixture.nativeElement.querySelector('input[formControlName=id]')
    let emailEl = fixture.nativeElement.querySelector('input[formControlName=email]')
    let firstNameEl = fixture.nativeElement.querySelector('input[formControlName=firstName]')
    let lastNameEl = fixture.nativeElement.querySelector('input[formControlName=lastName]')
    let activeEl = fixture.nativeElement.querySelector('input[formControlName=active]')
    let submitEl = fixture.nativeElement.querySelector('button[type=submit]')
    let errorEl = fixture.nativeElement.querySelector('#error-message');   

    expect(idEl.value).toEqual('123');
    expect(emailEl.classList).toContain("ng-valid");
    expect(emailEl.value).toEqual("asdf@jkl.com");
    expect(firstNameEl.classList).toContain("ng-valid");
    expect(firstNameEl.value).toEqual("asdf");
    expect(lastNameEl.classList).toContain("ng-valid");
    expect(lastNameEl.value).toEqual("jkl");
    expect(submitEl.disabled).toEqual(false);
    expect(activeEl.checked).toEqual(true);
    expect(errorEl).toBeNull();

    emailEl.value = "foo";
    emailEl.dispatchEvent(new Event('input'));

    fixture.detectChanges();

    expect(emailEl.classList).toContain("ng-invalid");
    expect(submitEl.disabled).toEqual(true);

  });

});
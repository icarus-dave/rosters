import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';

//import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
//import { InMemoryDataService }  from './operators/shared/in-memory-data-service';

import { AppComponent } from './app.component';

import { OperatorsComponent } from './operators/operators.component';
import { OperatorService } from './operators/shared/operator.service';

@NgModule({
  declarations: [
    AppComponent,
    OperatorsComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppRoutingModule,
    //InMemoryWebApiModule.forRoot(InMemoryDataService),
    NgbModule.forRoot()
  ],
  providers: [OperatorService],
  bootstrap: [AppComponent]
})
export class AppModule { }

import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AppRoutingModule } from './app-routing.module';

import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InMemoryDataService }  from './in-memory-data-service';

import { AppComponent } from './app.component';

import { WebConfigModule } from './webconfig/webconfig.module';
import { WebConfigService } from './webconfig/webconfig.service';
import { OperatorsComponent } from './operators/operators.component';
import { OperatorService } from './operators/shared/operator.service';

export function init_app(webConfig: WebConfigService){
    return () => webConfig.load() //get external config before loading
}

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
    WebConfigModule,
    InMemoryWebApiModule.forRoot(InMemoryDataService),
    NgbModule.forRoot()
  ],
  providers: [OperatorService,
    {
      provide: APP_INITIALIZER,
      useFactory: init_app,
      deps: [WebConfigService],
      multi:true
    }],
  bootstrap: [AppComponent]
})
export class AppModule { }

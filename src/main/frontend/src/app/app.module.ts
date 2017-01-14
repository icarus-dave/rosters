import { BrowserModule } from '@angular/platform-browser';
import { APP_INITIALIZER, NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { AppRoutingModule } from './app-routing.module';

import { AUTH_PROVIDERS }      from 'angular2-jwt';

import { AppComponent } from './app.component';

import { AppHttpModule } from './AppHttpModule';
import { WebConfigService } from './webconfig/webconfig.service';
import { OperatorsComponent } from './operators/operators.component';
import { OperatorFormComponent } from './operators/operator-form.component';
import { OperatorService } from './operators/shared/operator.service';
import { AuthGuard } from './auth-guard.service'
import { LoginGuard } from './login-guard.service'
import { Auth } from './auth.service'
import { LoginComponent } from './login.component'

export function init_app(webConfig: WebConfigService){
    return () => webConfig.load() //get external config before loading
}

@NgModule({
  declarations: [
    AppComponent,
    OperatorsComponent,
    OperatorFormComponent,
    LoginComponent
  ],
  imports: [
    BrowserModule,
    ReactiveFormsModule,
    AppHttpModule,
    AppRoutingModule,
    NgbModule.forRoot()
  ],
  entryComponents: [
    OperatorFormComponent
  ],
  providers: [
    OperatorService,
    WebConfigService,
    {
      provide: APP_INITIALIZER,
      useFactory: init_app,
      deps: [WebConfigService],
      multi:true
    },
    AUTH_PROVIDERS,
    AuthGuard,
    LoginGuard,
    Auth
    ],
  bootstrap: [AppComponent]
})
export class AppModule { }

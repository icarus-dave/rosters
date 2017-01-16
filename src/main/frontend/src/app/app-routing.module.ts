import { NgModule }             from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { OperatorsComponent }      from './operators/operators.component';
import { LoginComponent } from './login.component';
import { AuthGuard } from './auth-guard.service';
import { LoginGuard } from './login-guard.service';

const routes: Routes = [
  { path: 'operators', component:OperatorsComponent, canActivate: [AuthGuard] },
  { path: 'login', component:LoginComponent, canActivate: [LoginGuard] },
  { path: '', redirectTo: '/operators', pathMatch: 'full' }
,
];
@NgModule({
  imports: [ RouterModule.forRoot(routes) ],
  exports: [ RouterModule ]
})
export class AppRoutingModule {}


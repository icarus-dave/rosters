import { NgModule }           from '@angular/core';
import { CommonModule }       from '@angular/common';

import { WebConfigService }     from './webconfig.service';
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { InMemoryDataService }  from '../in-memory-data-service';

@NgModule({
  imports:      [ CommonModule, InMemoryWebApiModule.forRoot(InMemoryDataService), ],
  providers:    [ WebConfigService ]
})
export class WebConfigModule { }

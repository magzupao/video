import { HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { VideoCreditoService } from '../service/video-credito.service';
import { IVideoCredito } from '../video-credito.model';

const videoCreditoResolve = (route: ActivatedRouteSnapshot): Observable<null | IVideoCredito> => {
  const id = route.params.id;
  if (id) {
    return inject(VideoCreditoService)
      .find(id)
      .pipe(
        mergeMap((videoCredito: HttpResponse<IVideoCredito>) => {
          if (videoCredito.body) {
            return of(videoCredito.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default videoCreditoResolve;

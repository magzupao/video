import { HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { VideoImagenService } from '../service/video-imagen.service';
import { IVideoImagen } from '../video-imagen.model';

const videoImagenResolve = (route: ActivatedRouteSnapshot): Observable<null | IVideoImagen> => {
  const id = route.params.id;
  if (id) {
    return inject(VideoImagenService)
      .find(id)
      .pipe(
        mergeMap((videoImagen: HttpResponse<IVideoImagen>) => {
          if (videoImagen.body) {
            return of(videoImagen.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default videoImagenResolve;

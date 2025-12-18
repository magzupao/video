import { HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, Router } from '@angular/router';

import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { VideoService } from '../service/video.service';
import { IVideo } from '../video.model';

const videoResolve = (route: ActivatedRouteSnapshot): Observable<null | IVideo> => {
  const id = route.params.id;
  if (id) {
    return inject(VideoService)
      .find(id)
      .pipe(
        mergeMap((video: HttpResponse<IVideo>) => {
          if (video.body) {
            return of(video.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default videoResolve;

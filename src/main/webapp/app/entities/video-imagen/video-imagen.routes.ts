import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import VideoImagenResolve from './route/video-imagen-routing-resolve.service';

const videoImagenRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/video-imagen').then(m => m.VideoImagen),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/video-imagen-detail').then(m => m.VideoImagenDetail),
    resolve: {
      videoImagen: VideoImagenResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/video-imagen-update').then(m => m.VideoImagenUpdate),
    resolve: {
      videoImagen: VideoImagenResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/video-imagen-update').then(m => m.VideoImagenUpdate),
    resolve: {
      videoImagen: VideoImagenResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default videoImagenRoute;

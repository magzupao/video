import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import VideoCreditoResolve from './route/video-credito-routing-resolve.service';

const videoCreditoRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/video-credito').then(m => m.VideoCredito),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/video-credito-detail').then(m => m.VideoCreditoDetail),
    resolve: {
      videoCredito: VideoCreditoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/video-credito-update').then(m => m.VideoCreditoUpdate),
    resolve: {
      videoCredito: VideoCreditoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/video-credito-update').then(m => m.VideoCreditoUpdate),
    resolve: {
      videoCredito: VideoCreditoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default videoCreditoRoute;

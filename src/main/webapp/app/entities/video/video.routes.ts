import { Routes } from '@angular/router';

import { ASC } from 'app/config/navigation.constants';
import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';

import VideoResolve from './route/video-routing-resolve.service';

const videoRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/video').then(m => m.Video),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/video-detail').then(m => m.VideoDetail),
    resolve: {
      video: VideoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/video-update').then(m => m.VideoUpdate),
    resolve: {
      video: VideoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/video-update').then(m => m.VideoUpdate),
    resolve: {
      video: VideoResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default videoRoute;

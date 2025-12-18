import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'videoApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'video-credito',
    data: { pageTitle: 'videoApp.videoCredito.home.title' },
    loadChildren: () => import('./video-credito/video-credito.routes'),
  },
  {
    path: 'video',
    data: { pageTitle: 'videoApp.video.home.title' },
    loadChildren: () => import('./video/video.routes'),
  },
  {
    path: 'video-imagen',
    data: { pageTitle: 'videoApp.videoImagen.home.title' },
    loadChildren: () => import('./video-imagen/video-imagen.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;

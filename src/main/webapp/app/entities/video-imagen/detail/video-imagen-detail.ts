import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IVideoImagen } from '../video-imagen.model';

@Component({
  selector: 'jhi-video-imagen-detail',
  templateUrl: './video-imagen-detail.html',
  imports: [SharedModule, RouterLink],
})
export class VideoImagenDetail {
  videoImagen = input<IVideoImagen | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}

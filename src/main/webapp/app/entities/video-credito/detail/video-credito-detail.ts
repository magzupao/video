import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IVideoCredito } from '../video-credito.model';

@Component({
  selector: 'jhi-video-credito-detail',
  templateUrl: './video-credito-detail.html',
  imports: [SharedModule, RouterLink],
})
export class VideoCreditoDetail {
  videoCredito = input<IVideoCredito | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}

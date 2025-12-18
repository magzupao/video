import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { FormatMediumDatetimePipe } from 'app/shared/date';
import SharedModule from 'app/shared/shared.module';
import { IVideo } from '../video.model';

@Component({
  selector: 'jhi-video-detail',
  templateUrl: './video-detail.html',
  imports: [SharedModule, RouterLink, FormatMediumDatetimePipe],
})
export class VideoDetail {
  video = input<IVideo | null>(null);

  previousState(): void {
    globalThis.history.back();
  }
}

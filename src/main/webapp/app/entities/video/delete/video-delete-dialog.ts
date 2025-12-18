import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import SharedModule from 'app/shared/shared.module';
import { VideoService } from '../service/video.service';
import { IVideo } from '../video.model';

@Component({
  templateUrl: './video-delete-dialog.html',
  imports: [SharedModule, FormsModule],
})
export class VideoDeleteDialog {
  video?: IVideo;

  protected videoService = inject(VideoService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.videoService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}

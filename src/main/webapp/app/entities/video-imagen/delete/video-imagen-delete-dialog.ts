import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import SharedModule from 'app/shared/shared.module';
import { VideoImagenService } from '../service/video-imagen.service';
import { IVideoImagen } from '../video-imagen.model';

@Component({
  templateUrl: './video-imagen-delete-dialog.html',
  imports: [SharedModule, FormsModule],
})
export class VideoImagenDeleteDialog {
  videoImagen?: IVideoImagen;

  protected videoImagenService = inject(VideoImagenService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.videoImagenService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}

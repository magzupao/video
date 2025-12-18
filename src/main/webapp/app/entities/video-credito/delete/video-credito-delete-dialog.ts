import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import SharedModule from 'app/shared/shared.module';
import { VideoCreditoService } from '../service/video-credito.service';
import { IVideoCredito } from '../video-credito.model';

@Component({
  templateUrl: './video-credito-delete-dialog.html',
  imports: [SharedModule, FormsModule],
})
export class VideoCreditoDeleteDialog {
  videoCredito?: IVideoCredito;

  protected videoCreditoService = inject(VideoCreditoService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.videoCreditoService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}

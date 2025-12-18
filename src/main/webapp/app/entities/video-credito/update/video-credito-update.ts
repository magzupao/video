import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import SharedModule from 'app/shared/shared.module';
import { VideoCreditoService } from '../service/video-credito.service';
import { IVideoCredito } from '../video-credito.model';

import { VideoCreditoFormGroup, VideoCreditoFormService } from './video-credito-form.service';

@Component({
  selector: 'jhi-video-credito-update',
  templateUrl: './video-credito-update.html',
  imports: [SharedModule, ReactiveFormsModule],
})
export class VideoCreditoUpdate implements OnInit {
  isSaving = false;
  videoCredito: IVideoCredito | null = null;

  usersSharedCollection = signal<IUser[]>([]);

  protected videoCreditoService = inject(VideoCreditoService);
  protected videoCreditoFormService = inject(VideoCreditoFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: VideoCreditoFormGroup = this.videoCreditoFormService.createVideoCreditoFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ videoCredito }) => {
      this.videoCredito = videoCredito;
      if (videoCredito) {
        this.updateForm(videoCredito);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving = true;
    const videoCredito = this.videoCreditoFormService.getVideoCredito(this.editForm);
    if (videoCredito.id === null) {
      this.subscribeToSaveResponse(this.videoCreditoService.create(videoCredito));
    } else {
      this.subscribeToSaveResponse(this.videoCreditoService.update(videoCredito));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVideoCredito>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(videoCredito: IVideoCredito): void {
    this.videoCredito = videoCredito;
    this.videoCreditoFormService.resetForm(this.editForm, videoCredito);

    this.usersSharedCollection.set(this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection(), videoCredito.user));
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.videoCredito?.user)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));
  }
}

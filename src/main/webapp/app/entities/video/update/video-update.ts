import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { EstadoVideo } from 'app/entities/enumerations/estado-video.model';
import { UserService } from 'app/entities/user/service/user.service';
import { IUser } from 'app/entities/user/user.model';
import SharedModule from 'app/shared/shared.module';
import { VideoService } from '../service/video.service';
import { IVideo } from '../video.model';

import { VideoFormGroup, VideoFormService } from './video-form.service';

@Component({
  selector: 'jhi-video-update',
  templateUrl: './video-update.html',
  imports: [SharedModule, ReactiveFormsModule],
})
export class VideoUpdate implements OnInit {
  isSaving = false;
  video: IVideo | null = null;
  estadoVideoValues = Object.keys(EstadoVideo);

  usersSharedCollection = signal<IUser[]>([]);

  protected videoService = inject(VideoService);
  protected videoFormService = inject(VideoFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: VideoFormGroup = this.videoFormService.createVideoFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ video }) => {
      this.video = video;
      if (video) {
        this.updateForm(video);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving = true;
    const video = this.videoFormService.getVideo(this.editForm);
    if (video.id === null) {
      this.subscribeToSaveResponse(this.videoService.create(video));
    } else {
      this.subscribeToSaveResponse(this.videoService.update(video));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVideo>>): void {
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

  protected updateForm(video: IVideo): void {
    this.video = video;
    this.videoFormService.resetForm(this.editForm, video);

    this.usersSharedCollection.set(this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection(), video.user));
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.video?.user)))
      .subscribe((users: IUser[]) => this.usersSharedCollection.set(users));
  }
}

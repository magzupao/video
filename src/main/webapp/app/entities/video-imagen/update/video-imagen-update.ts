import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import { VideoService } from 'app/entities/video/service/video.service';
import { IVideo } from 'app/entities/video/video.model';
import SharedModule from 'app/shared/shared.module';
import { VideoImagenService } from '../service/video-imagen.service';
import { IVideoImagen } from '../video-imagen.model';

import { VideoImagenFormGroup, VideoImagenFormService } from './video-imagen-form.service';

@Component({
  selector: 'jhi-video-imagen-update',
  templateUrl: './video-imagen-update.html',
  imports: [SharedModule, ReactiveFormsModule],
})
export class VideoImagenUpdate implements OnInit {
  isSaving = false;
  videoImagen: IVideoImagen | null = null;

  videosSharedCollection = signal<IVideo[]>([]);

  protected videoImagenService = inject(VideoImagenService);
  protected videoImagenFormService = inject(VideoImagenFormService);
  protected videoService = inject(VideoService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: VideoImagenFormGroup = this.videoImagenFormService.createVideoImagenFormGroup();

  compareVideo = (o1: IVideo | null, o2: IVideo | null): boolean => this.videoService.compareVideo(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ videoImagen }) => {
      this.videoImagen = videoImagen;
      if (videoImagen) {
        this.updateForm(videoImagen);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving = true;
    const videoImagen = this.videoImagenFormService.getVideoImagen(this.editForm);
    if (videoImagen.id === null) {
      this.subscribeToSaveResponse(this.videoImagenService.create(videoImagen));
    } else {
      this.subscribeToSaveResponse(this.videoImagenService.update(videoImagen));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVideoImagen>>): void {
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

  protected updateForm(videoImagen: IVideoImagen): void {
    this.videoImagen = videoImagen;
    this.videoImagenFormService.resetForm(this.editForm, videoImagen);

    this.videosSharedCollection.set(
      this.videoService.addVideoToCollectionIfMissing<IVideo>(this.videosSharedCollection(), videoImagen.video),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.videoService
      .query()
      .pipe(map((res: HttpResponse<IVideo[]>) => res.body ?? []))
      .pipe(map((videos: IVideo[]) => this.videoService.addVideoToCollectionIfMissing<IVideo>(videos, this.videoImagen?.video)))
      .subscribe((videos: IVideo[]) => this.videosSharedCollection.set(videos));
  }
}

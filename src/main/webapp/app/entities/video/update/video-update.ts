import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, inject, signal, ChangeDetectorRef } from '@angular/core';
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

// Nueva interfaz para manejar las imágenes con preview
interface ImageWithPreview {
  file: File;
  preview: string;
}

@Component({
  selector: 'jhi-video-update',
  templateUrl: './video-update.html',
  imports: [SharedModule, ReactiveFormsModule],
})
export class VideoUpdate implements OnInit {
  isSaving = false;
  video: IVideo | null = null;
  estadoVideoValues = Object.keys(EstadoVideo);

  // Propiedades para imágenes
  selectedImages: File[] = [];
  imagesWithPreview: ImageWithPreview[] = [];
  imagesError: string | null = null;

  // Propiedades para audio
  selectedAudio: File | null = null;
  audioError: string | null = null;

  usersSharedCollection = signal<IUser[]>([]);

  protected cdr = inject(ChangeDetectorRef);
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

    this.editForm.disable();

    if (!this.isImagesValid()) {
      this.isSaving = false;
      this.editForm.enable();
      this.imagesError = this.imagesError ?? 'Selecciona entre 1 y 10 imágenes.';
      return;
    }

    const video = this.videoFormService.getVideo(this.editForm);

    if (video.id === null) {
      this.subscribeToSaveResponse(this.videoService.create(video, this.selectedImages, this.selectedAudio));
    } else {
      this.subscribeToSaveResponse(this.videoService.update(video, this.selectedImages, this.selectedAudio));
    }
  }

  /**
   * Maneja la selección de archivo de audio
   */
  onAudioSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    const file = input.files[0];

    // Validar que sea un archivo de audio
    const validAudioTypes = ['audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/ogg', 'audio/mp4', 'audio/x-m4a'];
    const validExtensions = ['.mp3', '.wav', '.ogg', '.m4a'];

    const isValidType = validAudioTypes.includes(file.type);
    const hasValidExtension = validExtensions.some(ext => file.name.toLowerCase().endsWith(ext));

    if (!isValidType && !hasValidExtension) {
      this.audioError = 'Solo se permiten archivos de audio (MP3, WAV, OGG, M4A).';
      input.value = '';
      return;
    }

    // Validar tamaño máximo (por ejemplo, 50MB)
    const maxSize = 50 * 1024 * 1024; // 50MB
    if (file.size > maxSize) {
      this.audioError = 'El archivo de audio no debe superar los 50MB.';
      input.value = '';
      return;
    }

    this.selectedAudio = file;
    this.audioError = null;

    // Actualizar el campo audioFilename en el formulario
    this.editForm.patchValue({
      audioFilename: file.name,
      tieneAudio: true,
    });

    // Limpiar el input
    input.value = '';
  }

  /**
   * Elimina el archivo de audio seleccionado
   */
  removeAudio(): void {
    this.selectedAudio = null;
    this.audioError = null;

    // Actualizar el formulario
    this.editForm.patchValue({
      audioFilename: null,
      tieneAudio: false,
    });
  }

  /**
   * Maneja la selección de imágenes
   */
  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files) return;

    const files = Array.from(input.files);

    // valida tipo imagen
    if (files.some(f => !f.type.startsWith('image/'))) {
      this.imagesError = 'Solo se permiten archivos de imagen.';
      input.value = '';
      return;
    }

    // ACUMULAR en lugar de reemplazar
    const totalImages = this.selectedImages.length + files.length;

    if (totalImages > 10) {
      const available = 10 - this.selectedImages.length;
      if (available > 0) {
        const filesToAdd = files.slice(0, available);
        this.addFilesWithPreviews(filesToAdd);
        this.imagesError = `Solo puedes agregar ${available} imagen(es) más. Máximo 10 en total.`;
      } else {
        this.imagesError = 'Ya tienes 10 imágenes. No puedes agregar más.';
      }
    } else {
      // CONCATENAR con spread operator
      this.addFilesWithPreviews(files);
      this.imagesError = null;
    }

    // Limpiar el input
    input.value = '';
  }

  private addFilesWithPreviews(files: File[]): void {
    files.forEach(file => {
      this.selectedImages.push(file);

      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        // Crear un NUEVO array en lugar de hacer push
        this.imagesWithPreview = [
          ...this.imagesWithPreview,
          {
            file: file,
            preview: e.target?.result as string,
          },
        ];
        this.cdr.markForCheck();
      };
      reader.readAsDataURL(file);
    });
  }

  removeImage(index: number): void {
    this.selectedImages.splice(index, 1);
    this.imagesWithPreview.splice(index, 1);

    if (this.selectedImages.length === 0) {
      this.imagesError = 'Debes seleccionar al menos 1 imagen.';
    } else if (this.selectedImages.length <= 10) {
      this.imagesError = null;
    }
  }

  isImagesValid(): boolean {
    return this.selectedImages.length >= 1 && this.selectedImages.length <= 10 && !this.imagesError;
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVideo>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    //this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
    this.editForm.enable();
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

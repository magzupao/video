import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, OnDestroy, inject, signal, ChangeDetectorRef } from '@angular/core'; // ✏️ AÑADIDO OnDestroy
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Observable, interval, Subscription } from 'rxjs'; // ✏️ AÑADIDO interval, Subscription
import { finalize, map, switchMap, takeWhile } from 'rxjs/operators'; // ✏️ AÑADIDO switchMap, takeWhile

import { EstadoVideo } from 'app/entities/enumerations/estado-video.model';
import { FormatoVideo } from 'app/entities/enumerations/formato-video.model';
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
export class VideoUpdate implements OnInit, OnDestroy {
  // ✏️ AÑADIDO OnDestroy
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

  // 🆕 Propiedades para polling
  private pollingSubscription: Subscription | null = null;
  isProcessing = false;
  processingMessage = 'Guardando video, audio e imágenes...';
  videoEstado: string | null = null;

  usersSharedCollection = signal<IUser[]>([]);

  generatedVideoId: number | null = null;
  downloadUrl: string | null = null;
  outputFilename: string | null = null;
  isDownloading = false;
  lastPolledVideo: IVideo | null = null;

  formatoVideoValues = Object.keys(FormatoVideo);

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

    // ✨ SUSCRIBIRSE A CAMBIOS DEL CHECKBOX tieneAudio
    this.editForm.controls.tieneAudio.valueChanges.subscribe(tieneAudio => {
      if (tieneAudio) {
        // Si se marca Y el campo está vacío, establecer valor por defecto 5
        const duracionActual = this.editForm.controls.duracionTransicion.value;
        if (duracionActual === null || duracionActual === undefined) {
          this.editForm.patchValue({ duracionTransicion: 5 });
        }
        // Si ya tiene un valor (8, 10, 15, etc.), lo mantiene
      } else {
        // Si se desmarca, limpiar el valor
        this.editForm.patchValue({ duracionTransicion: null });
      }
    });
  }

  // 🆕 NUEVO MÉTODO: Cleanup al destruir el componente
  ngOnDestroy(): void {
    this.stopPolling();
  }

  previousState(): void {
    globalThis.history.back();
  }

  // ✏️ MODIFICADO: Método save() completo
  save(): void {
    this.isSaving = true;
    this.isProcessing = true;
    this.processingMessage = 'Guardando video...';

    this.editForm.disable();

    if (!this.isImagesValid()) {
      this.isSaving = false;
      this.isProcessing = false;
      this.editForm.enable();
      this.imagesError = this.imagesError ?? 'Selecciona entre 1 y 10 imágenes.';
      return;
    }

    // ✅ NUEVA VALIDACIÓN: Verificar que tenga audio O tenga duración
    const tieneAudio = this.editForm.controls.tieneAudio.value;
    const tieneArchivoAudio = this.selectedAudio !== null;

    if (!tieneArchivoAudio && !tieneAudio) {
      this.audioError = 'Debes subir un audio O marcar la opción "sin audio" y definir la duración de cada imagen.';
      this.isSaving = false;
      this.isProcessing = false;
      this.editForm.enable();
      return;
    }

    const video = this.videoFormService.getVideo(this.editForm);

    if (video.id === null) {
      // Crear nuevo video
      this.videoService.create(video, this.selectedImages, this.selectedAudio).subscribe({
        next: response => {
          console.log('✅ Respuesta del servidor:', response);

          if (response.status === 202 && response.body) {
            // Video creado, procesamiento iniciado
            const createdVideo = response.body;
            console.log('🚀 Video creado con ID:', createdVideo.id);
            console.log('⏳ Estado inicial:', createdVideo.estado);

            this.generatedVideoId = createdVideo.id ?? null;

            this.processingMessage = 'Generando video... esto puede tardar varios minutos';
            this.videoEstado = createdVideo.estado ?? null;

            // Iniciar polling para verificar el estado
            this.startPolling(createdVideo.id!);
          } else {
            console.warn('⚠️ Respuesta inesperada del servidor:', response.status);
            this.onSaveError();
          }
        },
        error: error => {
          console.error('❌ Error creando video:', error);
          this.onSaveError();
        },
      });
    } else {
      // Actualizar video existente
      this.subscribeToSaveResponse(this.videoService.update(video, this.selectedImages, this.selectedAudio));
    }
  }

  get mostrarDuracionTransicion(): boolean {
    return this.editForm.controls.tieneAudio.value === true;
  }

  // 🆕 NUEVO MÉTODO: Inicia el polling para verificar el estado del video
  private startPolling(videoId: number): void {
    console.log('🔄 Iniciando polling para video ID:', videoId);

    this.pollingSubscription = interval(3000) // Cada 3 segundos
      .pipe(
        switchMap(() => {
          console.log('🔍 Consultando estado del video...');
          return this.videoService.getVideoStatus(videoId);
        }),
        takeWhile(response => {
          const video = response.body;
          if (!video) {
            console.warn('⚠️ No se recibió información del video');
            return false;
          }

          const estado = video.estado;
          this.videoEstado = estado ?? null;
          this.lastPolledVideo = video; // ✅ guardo el DTO
          this.generatedVideoId = video.id ?? videoId; // ✅ aseguro el id

          console.log('📊 Estado actual:', estado);

          // Actualizar mensaje según el estado
          if (estado === 'EN_PROCESO') {
            this.processingMessage = 'Generando video... esto puede tardar varios minutos';
            return true; // Continuar polling
          } else if (estado === 'COMPLETADO') {
            this.processingMessage = '✅ Video generado exitosamente!';
            this.downloadUrl = (video as any).downloadUrl ?? `/api/videos/${video.id ?? videoId}/download`;
            this.outputFilename = (video as any).outputFilename ?? null;
            return false; // Detener polling
          } else if (estado === 'ERROR') {
            this.processingMessage = '❌ Error generando el video';
            return false; // Detener polling
          }

          return true; // Por defecto, continuar polling
        }, true), // El 'true' permite que emita el último valor antes de completar
        finalize(() => {
          console.log('🏁 Polling finalizado');
          this.onPollingComplete();
          this.onSaveFinalize();
          this.cdr.detectChanges();
        }),
      )
      .subscribe({
        next: response => {
          const video = response.body;
          if (video) {
            console.log('📦 Video recibido:', video);
          }
        },
        error: error => {
          console.error('❌ Error en polling:', error);
          this.onSaveError();
        },
      });
  }

  // 🆕 NUEVO MÉTODO: Se ejecuta cuando el polling termina
  private onPollingComplete(): void {
    console.log('✅ Procesamiento completado');

    if (this.videoEstado === 'COMPLETADO') {
      // ✅ red de seguridad
      if (!this.downloadUrl && this.lastPolledVideo?.id) {
        this.downloadUrl = (this.lastPolledVideo as any).downloadUrl ?? `/api/videos/${this.lastPolledVideo.id}/download`;
      }
      if (!this.outputFilename) {
        this.outputFilename = (this.lastPolledVideo as any).outputFilename ?? null;
      }

      this.onSaveSuccess();
    } else if (this.videoEstado === 'ERROR') {
      this.onSaveError();
    }
  }

  // 🆕 NUEVO MÉTODO: Detiene el polling
  private stopPolling(): void {
    if (this.pollingSubscription) {
      console.log('⏹️ Deteniendo polling');
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
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
    });

    // ✅ NUEVO: Si hay audio cargado, desmarcar el checkbox de "sin audio"
    if (this.editForm.controls.tieneAudio.value) {
      this.editForm.patchValue({
        tieneAudio: false,
      });
    }

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

  downloadGeneratedVideo(): void {
    if (!this.generatedVideoId || this.isDownloading) return;

    this.isDownloading = true;

    this.videoService.downloadVideo(this.generatedVideoId).subscribe({
      next: res => {
        const blob = res.body;
        if (!blob) {
          this.isDownloading = false;
          return;
        }

        const cd = res.headers.get('content-disposition');
        const headerName = cd?.match(/filename="(.+?)"/)?.[1] ?? null;

        const filename = headerName ?? this.outputFilename ?? 'video.mp4';

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);

        this.isDownloading = false;
      },
      error: err => {
        console.error('❌ Error descargando video', err);
        this.isDownloading = false;
      },
    });
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

  // ✏️ MODIFICADO: Añadido stopPolling()
  protected onSaveFinalize(): void {
    this.isSaving = false;
    this.isProcessing = false;
    this.editForm.enable();
    this.stopPolling();
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
